/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jbatik.lsystem;

import com.jbatik.lsystem.api.parser.DetailParser;
import com.jbatik.lsystem.api.parser.LSystemStringParser;
import com.jbatik.lsystem.parser.exceptions.ParseRuleException;
import java.util.Collection;
import java.util.Map;
import org.openide.util.Lookup;

/**
 *
 * @author Dimas Danurwenda
 */
public class LSystem {

    public static final String ITERATION_PROP = "iteration";
    public static final String AXIOM_PROP = "axiom";
    public static final String RULES_PROP = "rules";
    protected String axiom;
    protected int iteration;

    public int getIteration() {
        return iteration;
    }

    public void setIteration(int iteration) {
        this.iteration = iteration;
    }

    /**
     * Get the value of axiom
     *
     * @return the value of axiom
     */
    public String getAxiom() {
        return axiom;
    }

    /**
     * Set the value of axiom
     *
     * @param axiom new value of axiom
     * @throws com.jbatik.lsystem.parser.exceptions.ParseRuleException
     */
    public void setAxiom(String axiom) throws ParseRuleException {
        Collection<? extends LSystemStringParser> allFilters = Lookup.getDefault().lookupAll(LSystemStringParser.class);
        if (!allFilters.isEmpty()) {
            try {
                for (LSystemStringParser p : allFilters) {
                    p.parseAxiom(axiom);
                }
            } catch (ParseRuleException ex) {
                throw ex;
            } finally {
                this.axiom = axiom;
            }
        } else {
            this.axiom = axiom;
        }
    }

    protected Map<Character, String> rules;

    public Map<Character, String> getRules() {
        return rules;
    }

    public String getStringRules() {
        StringBuilder details = new StringBuilder();
        rules.entrySet().stream().forEach((cursor) -> {
            details.append(cursor.getKey()).append("=").append(cursor.getValue()).append("\n");
        });
        return details.toString();
    }

    /**
     *
     * @param rules
     * @throws ParseRuleException
     */
    public void setRules(Map<Character, String> rules) throws ParseRuleException {
        Collection<? extends LSystemStringParser> allFilters = Lookup.getDefault().lookupAll(LSystemStringParser.class);
        if (!allFilters.isEmpty()) {
            LSystemStringParser parser = allFilters.iterator().next();
            try {
                parser.parseRules(rules);
            } catch (ParseRuleException ex) {
                throw ex;
            } finally {
                this.rules = rules;
            }
        } else {
            this.rules = rules;
        }
    }

    /**
     *
     * @param s
     * @throws ParseRuleException
     */
    public void setStringRules(String s) throws ParseRuleException {
        try {
            setRules(DetailParser.parseStringRules(s));
        } catch (ParseRuleException ex) {
            throw ex;
        }
    }

    public LSystem(String axiom, Map<Character, String> rules, int iteration) {
        this.axiom = axiom;
        this.rules = rules;
        this.iteration = iteration;
    }

}
