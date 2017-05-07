/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jbatik.lsystem.api.parser;

import com.jbatik.lsystem.parser.exceptions.ParseRuleException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 *
 * @author Dimas Y. Danurwenda
 */
public class DetailParser {

    public static Map<Character, String> parseStringRules(String detail) throws ParseRuleException {
        HashMap ret = new HashMap<>();
        if (detail.length() > 0) {
            String[] splittedRules = detail.split("\n");
            //the number of lines
            int ruleNumber = splittedRules.length;
            //array of symbol character, which will be mapped into productions chars
            ArrayList<Character> lefts = new ArrayList<>();
            ArrayList<String> rights = new ArrayList<>();

            //assigning the leftSider
            for (int i = 0; i < ruleNumber; i++) {
                if (splittedRules[i].length() > 0) {
                    if (!splittedRules[i].startsWith("#")) {
                        rights.add(Parser.getRightSider(splittedRules[i], i));
                        lefts.add(Parser.getLeftSider(splittedRules[i], i));
                    }
                }
            }
            //done with splitting details into left side and right side
            //iterate thru rights and lefts
            Iterator<Character> li = lefts.iterator();
            Iterator<String> ri = rights.iterator();
            while (li.hasNext() && ri.hasNext()) {
                ret.put(li.next(), ri.next());
            }
        }
        return ret;
    }
}
