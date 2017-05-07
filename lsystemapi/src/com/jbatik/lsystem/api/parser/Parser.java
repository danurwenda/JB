/*
 * Copyright (C) 01-31-2010 Dimas Yusuf Danurwenda
 *
 */
package com.jbatik.lsystem.api.parser;

import com.jbatik.lsystem.parser.exceptions.ParseRuleException;

/**
 *
 * @author fusuysamid
 */
public class Parser {

    public static int getCloseIdx(String result, int i, int l) throws ParseRuleException {
        int res = result.indexOf(')', i);
        if (res != -1) {
            return res;
        } else {
            throw new ParseRuleException(ParseRuleException.MISSING_CLOSE_BRACKET, l, i);
        }
    }

    public static char getLeftSider(String string, int i) throws ParseRuleException {
        int eqIdx = string.indexOf('=');
        int commentIdx = string.indexOf('#');
        int nextIdx = string.indexOf('=', eqIdx + 1);
        if ((eqIdx == -1) || ((commentIdx != -1) && (eqIdx > commentIdx))) {
            //= non existent or appear only after comment
            throw new ParseRuleException(ParseRuleException.MISSING_EQUAL_SIGN, i, 0);
        } else if (eqIdx == 0) {
            throw new ParseRuleException(ParseRuleException.MISSING_MAPPED_SYMBOL, i, eqIdx);
        } else //ada dan bukan paling depan, cek dulu jangan2 muncul setelah #
        //cek double =
        //error double ini terjadi jika
        //ada nextIdx dan ((ada comment dan nextIdx < comment) atau (tidak ada comment))
        if ((nextIdx != -1) && (commentIdx == -1 || (commentIdx != -1 && nextIdx < commentIdx))) {
            throw new ParseRuleException(ParseRuleException.MULTIPLE_EQUAL_SIGN, i, nextIdx);
        } else {
            return string.charAt(0);
        }
    }

    public static float parseFloat(String sVal, int pos, int i) throws ParseRuleException {
        float f;
        try {
            f = Float.parseFloat(sVal);
            return f;
        } catch (NumberFormatException nfe) {
            throw new ParseRuleException(ParseRuleException.VALUE_FORMAT, i, pos);
        }
    }

    public static String getRightSider(String string, int i) throws ParseRuleException {
        int eqIdx = string.indexOf('=');
        int commentIdx = string.indexOf('#');
        int nextIdx = string.indexOf('=', eqIdx + 1);
        if ((eqIdx == -1) || ((commentIdx != -1) && (eqIdx > commentIdx))) {
            //= non existent or appear only after comment
            throw new ParseRuleException(ParseRuleException.MISSING_EQUAL_SIGN, i, 0);
        } else if (eqIdx == 0) {
            throw new ParseRuleException(ParseRuleException.MISSING_MAPPED_SYMBOL, i, eqIdx);
        } else if (string.length() - 1 == eqIdx) {
            throw new ParseRuleException(ParseRuleException.EMPTY_MAPPING, i, eqIdx);
        } else //ada dan bukan paling depan, cek dulu jangan2 muncul setelah #
        //cek double =
        //error double ini terjadi jika
        //ada nextIdx dan ((ada comment dan nextIdx < comment) atau (tidak ada comment))
        if ((nextIdx != -1) && (commentIdx == -1 || (commentIdx != -1 && nextIdx < commentIdx))) {
            throw new ParseRuleException(ParseRuleException.MULTIPLE_EQUAL_SIGN, i, nextIdx);
        } else {
            return string.substring(eqIdx + 1);
        }
    }
}
