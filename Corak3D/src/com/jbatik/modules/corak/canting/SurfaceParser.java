/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jbatik.modules.corak.canting;

import com.jbatik.lsystem.api.parser.LSystemStringParser;
import com.jbatik.lsystem.api.parser.Parser;
import com.jbatik.lsystem.parser.exceptions.ParseRuleException;
import com.jbatik.lsystem.turtle.Surface;
import com.jbatik.modules.corak.CorakLSystem;

/**
 *
 * @author Dimas Y. Danurwenda
 */
public class SurfaceParser extends LSystemStringParser{

    private CorakLSystem cor;

    public SurfaceParser(CorakLSystem cor) {
        this.cor = cor;
    }

    /**
     * Parse given LSystem string. Handles FUnit concept.
     *
     * @param lin the string that will be parsed
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
            if (i + 1 < lin.length()) {
                if (lin.charAt(i + 1) == '(')//get the value
                {
                    closeIdx = Parser.getCloseIdx(lin, i, l);//get the ) char idx
                    sVal = lin.substring(i + 2, closeIdx);//get the string representation of value

                    // F Unit handling
                    if (current == 'F') {

                        //cek apakah ada fUnit bernama sVal
                        if (findSurface(sVal) == null) {
                            throw new ParseRuleException(ParseRuleException.VALUE_FORMAT, l, i);
                        }
                    }
                    ////System.out.println("i v.s closeIdx "+i+" "+closeIdx);
                    i = closeIdx;
                }
            }
        }
    }

    private Surface findSurface(String sVal) {
        for (Surface s : cor.getSurfaces()) {
            if (s.getName().equals(sVal)) {
                return s;
            }
        }
        return null;
    }
}
