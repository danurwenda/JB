/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jbatik.lsystem.api.parser;

import com.jbatik.lsystem.parser.exceptions.ParseRuleException;
import java.util.Map;

/**
 *
 * @author RAPID02
 */
public abstract class LSystemStringParser {

    public void parseAxiom(String axiom) throws ParseRuleException {
        parse(axiom, -1);
    }   

    public void parseRules(Map<Character, String> map) throws ParseRuleException {
        int line = 0;
        for (Map.Entry<Character, String> cursor : map.entrySet()) {
            //parse the String part only
            parse(cursor.getValue(), line++);
        }
    }

    /**
     * Parse given LSystem string
     *
     * @param lin the string that will be parsed
     * @param l line number, -1 for axiom
     * @throws com.jbatik.lsystem.parser.exceptions.ParseRuleException
     */
    public abstract void parse(String lin, int l) throws ParseRuleException;
}
