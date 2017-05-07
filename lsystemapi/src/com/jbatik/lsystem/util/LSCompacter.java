/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jbatik.lsystem.util;

import com.jbatik.core.format.DotDecimalFormat;
import com.jbatik.lsystem.api.parser.LSystemStringParser;
import com.jbatik.lsystem.api.parser.Parser;
import com.jbatik.lsystem.parser.exceptions.ParseRuleException;
import java.text.DecimalFormat;
import java.util.Collection;
import java.util.LinkedList;
import org.openide.util.Lookup;

/**
 * Compacting string, usually by combining repeated function e.g rewrites
 * +(20)+(15) as +(35)
 *
 * @author INTEL
 */
public class LSCompacter {

    /**
     * For example, given original string !(1.1623)'(1.1623)!(1.5938)'(1.5938)A
     * and operator ! and ', returns !(1.8525)'(1.8535)A
     *
     * @param ori
     * @param operator
     * @return
     * @throws com.jbatik.lsystem.parser.exceptions.ParseRuleException
     */
    public static String simplifyPrefix(String ori, char... operator) throws ParseRuleException {
        //make sure it's valid
        Collection<? extends LSystemStringParser> allFilters = Lookup.getDefault().lookupAll(LSystemStringParser.class);
        if (!allFilters.isEmpty()) {
            try {
                for (LSystemStringParser p : allFilters) {
                    p.parse(ori, 0);
                }
            } catch (ParseRuleException ex) {
                throw ex;
            }
        }
        String ret = ori;
        for (char c : operator) {
            ret = simplifyPrefixImpl(ret, c);
        }
        return ret;
    }

    /**
     * Note that some operators may affect the behavior of another operator. For
     * example, operator ' (which stands for increment angle) will break a
     * sequence of + operators.
     *
     * @param ori
     * @param o
     * @return
     */
    private static String simplifyPrefixImpl(String ori, char o) throws ParseRuleException {
        if (!Token.canBeSimplified(o)) {
            return ori;
        }
        boolean conflict = false;
        int tokenReplacementPos = -1;
        int tokenCounter = 0;
        boolean mirror = false;
        boolean mirrored = false;
        Float val = null;
        LinkedList<Token> stack = new LinkedList<>();
        while (!conflict && !ori.isEmpty()) {
            Token first = substractToken(ori);
            ori = ori.substring(first.toString().length());
            char c = first.getOperator();
            if (c == '|') {
                mirrored = true;
                mirror = !mirror;
                stack.push(first);
            } else if ((Token.isNegating(c, o) || Token.isSupplement(c, o)) && !first.getValueStr().isEmpty()) {
                float tokenVal = Parser.parseFloat(first.getValueStr(), 0, 0);
                if (val != null) {
                    if (Token.canBeMultiplied(o)) {
                        val *= tokenVal;
                    } else {
                        if (Token.isSupplement(c, o)) {
                            if (mirror) {
                                val -= tokenVal;
                            } else {
                                val += tokenVal;
                            }
                        } else {
                            //negating
                            if (mirror) {
                                val += tokenVal;
                            } else {
                                val -= tokenVal;
                            }
                        }
                    }
                } else {
                    val = tokenVal;
                }
                if (tokenReplacementPos == -1) {
                    tokenReplacementPos = tokenCounter;
                }
            } else {
                //check conflict
                if (Token.conflict(o, c)) {
                    conflict = true;
                } else {
                    tokenCounter++;
                }
                //push to stack
                stack.push(first);
            }
        }
        //rebuilding the string
        String ret = "";
        if (tokenReplacementPos == -1) {
            //not found until conflict / last char of ori
            //just rebuild from stack
            while (!stack.isEmpty()) {
                Token poll = stack.pollLast();
                ret = ret.concat(poll.toString());
            }
            ret = ret.concat(ori);
        } else {
            int buildCounter = 0;
            while (!stack.isEmpty()) {
                if (buildCounter == tokenReplacementPos) {
                    //insert simplified token here
                    DecimalFormat fourDigit = new DecimalFormat("###.####",DotDecimalFormat.getSymbols());
                    Token simplified = new Token(o, fourDigit.format(val));
                    ret = ret.concat(simplified.toString());
                }
                Token poll = stack.pollLast();
                ret = ret.concat(poll.toString());
                buildCounter++;
            }
            ret = ret.concat(ori);
        }
        if (mirrored) {
            //eliminate double mirror which likely exists
            ret = ret.replace("||", "");
        }
        return ret;
    }

    private static Token substractToken(String ori) throws ParseRuleException {
        if (ori.isEmpty()) {
            return null;
        } else if (ori.length() == 1) {
            return new Token(ori.charAt(0));
        } else if (ori.charAt(1) != '(') {
            Token ret = new Token(ori.charAt(0));
            return ret;
        } else {
            //parse close bracket
            int close = Parser.getCloseIdx(ori, 1, 0);
            //get the value
            String sVal = ori.substring(2, close);//get the string representation of value
            Token ret = new Token(ori.charAt(0), sVal);
            return ret;
        }
    }
}
