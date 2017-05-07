/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jbatik.lsystem.util;

/**
 *
 * @author INTEL
 */
class Token {

    static boolean conflict(char o, char c) {
        if (o == '!' || o == '?') {
            return isDrawing(c);
        } else if (o == '\'' || o == '"') {
            return isMoving(c);
        } else if (o == '+' || o == '-') {
            return "&^;:".indexOf(c) > -1;
        }
        return true;
    }

    /**
     * Some operators can't be simplified, e.g a sequence of F
     *
     * @param o
     * @return
     */
    static boolean canBeSimplified(char o) {
        return "+-'\"!?^&<>:;".indexOf(o) > -1;
    }

    private static boolean isDrawing(char c) {
        return c == 'F' || c == 'Z';
    }

    private static boolean isMoving(char c) {
        return "fzFZg".indexOf(c) > -1;
    }

    static boolean canBeMultiplied(char o) {
        return "\"';:?!".indexOf(o) > -1;
    }

    static boolean isNegating(char c, char o) {
        if (o == c) {
            return false;
        }
        switch (c) {
            case '+':
                return o == '-';
            case '-':
                return o == '+';
            case '^':
                return o == '&';
            case '&':
                return o == '^';
            case '<':
                return o == '>';
            case '>':
                return o == '<';

            default:
                return false;
        }
    }

    static boolean isSupplement(char c, char o) {
        if (o == c) {
            return true;
        }
        switch (c) {
            case '!':
                return o == '?';
            case '?':
                return o == '!';
            case '\'':
                return o == '"';
            case '"':
                return o == '\'';
            case ':':
                return o == ';';
            case ';':
                return o == ':';

            default:
                return false;
        }
    }
    private char operator;
    private String valueStr;

    @Override
    public String toString() {
        return String.valueOf(operator).concat(valueStr.isEmpty() ? "" : "(".concat(valueStr).concat(")"));
    }

    public char getOperator() {
        return operator;
    }

    public void setOperator(char operator) {
        this.operator = operator;
    }

    public String getValueStr() {
        return valueStr;
    }

    public void setValueStr(String valueStr) {
        this.valueStr = valueStr;
    }

    public Token(char o) {
        this(o, "");
    }

    public Token(char operator, String valueStr) {
        this.operator = operator;
        this.valueStr = valueStr;
    }
}
