/*
 * Copyright (C) 01-31-2010 Dimas Yusuf Danurwenda
 *
 */
package com.jbatik.lsystem.parser.exceptions;

/**
 *
 * @author fusuysamid
 */
public class ParseRuleException extends Exception {

    /**
     * For example, AbemC(90) as a line entry in rules
     */
    public static final int MISSING_EQUAL_SIGN = "MISSING_EQUAL_SIGN".hashCode();
    /**
     * For example, A=bemC(90F+
     */
    public static final int MISSING_CLOSE_BRACKET = "MISSING_CLOSE_BRACKET".hashCode();
    /**
     * For example, A=bemC(90)(45)
     */
    public static final int DANGLING_BRACKETS = "DANGLING_BRACKETS".hashCode();
    /**
     * For example, =cccc(3)
     */
    public static final int MISSING_MAPPED_SYMBOL = "MISSING_MAPPED_SYMBOL".hashCode();
    /**
     * For example, A=B+FF(80)=tcd
     */
    public static final int MULTIPLE_EQUAL_SIGN = "MULTIPLE_EQUAL_SIGN".hashCode();
    /**
     * For example, b(-2s)
     */
    public static final int VALUE_FORMAT = "VALUE_FORMAT".hashCode();
    /**
     * For example, A=b[CD] A=y
     */
    public static final int MULTIPLE_DEFINITION = "MULTIPLE_DEFINITION".hashCode();
    /**
     * For example, A=
     */
    public static final int EMPTY_MAPPING = "EMPTY_MAPPING".hashCode();
    /**
     * Type of error
     */
    private int type;
    /**
     * Line number of occuring error
     */
    private int lineNumber;
    /**
     * Position of character that caused error
     */
    private int errorPos;

    /**
     *
     * @param t
     * @param i
     * @param c
     */
    public ParseRuleException(int t, int i, int c) {
        type = t;
        lineNumber = i;
        errorPos = c;
    }

    @Override
    public String getMessage() {
        String s;
        if (type == MISSING_CLOSE_BRACKET) {
            s = "Kekurangan tanda ')'";
        } else if (type == MISSING_EQUAL_SIGN) {
            s = "Tanda '=' tidak ditemukan";
        } else if (type == DANGLING_BRACKETS) {
            s = "Tanda kurung tutup tidak terpakai";
        } else if (type == MISSING_MAPPED_SYMBOL) {
            s = "Kekurangan simbol yang dipetakan";
        } else if (type == MULTIPLE_EQUAL_SIGN) {
            s = "Tanda '=' berlebih";
        } else if (type == MULTIPLE_DEFINITION) {
            s = "Pendefinisian aturan berulang";
        } else if (type == EMPTY_MAPPING) {
            s = "Kekurangan simbol hasil pemetaan";
        } else {
            s = "Nilai di dalam tanda kurung tidak valid";
        }
        return s.concat(" pada baris " + (lineNumber + 1) + " karakter ke " + (errorPos + 1) + "\n");
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getLineNumber() {
        return lineNumber;
    }

    public void setLineNumber(int lineNumber) {
        this.lineNumber = lineNumber;
    }

    public int getErrorPos() {
        return errorPos;
    }

    public void setErrorPos(int errorPos) {
        this.errorPos = errorPos;
    }
}
