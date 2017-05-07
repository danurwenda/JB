/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jbatik.lsystem.api.parser;

import com.jbatik.lsystem.parser.exceptions.ParseRuleException;
import org.openide.util.lookup.ServiceProvider;

/**
 * Default implementation of LSystemStringParser
 *
 * @author RAPID02
 */
@ServiceProvider(service = LSystemStringParser.class)
public class LSystemParserImpl extends LSystemStringParser {

    /**
     * Parse given LSystem string
     *
     * @param lin the string that will be interpreted as TokenList
     * @param l line number, -1 for axiom
     * @throws com.jbatik.lsystem.parser.exceptions.ParseRuleException
     */
    @Override
    public void parse(String lin, int l) throws ParseRuleException {
        char current;
        int closeIdx;
        String sVal;
        for (int i = 0; i < lin.length(); i++) {
            //get current symbol and its value (if any)
            current = lin.charAt(i);
            if ((current == '(') || (current == ')')) {
                throw new ParseRuleException(ParseRuleException.DANGLING_BRACKETS, l, i);
            }
            if (i + 1 < lin.length()) {
                if (lin.charAt(i + 1) == '(')//get the value
                {
                    closeIdx = Parser.getCloseIdx(lin, i, l);//get the ) char idx
                    sVal = lin.substring(i + 2, closeIdx);//get the string representation of value

                    if ("+-><^&?!'\"".indexOf(current) > -1) {
                        Parser.parseFloat(sVal, i + 2, l);//persentase
                    }
                    ////System.out.println("i v.s closeIdx "+i+" "+closeIdx);
                    i = closeIdx;
                }
            }
        }
    }

}
