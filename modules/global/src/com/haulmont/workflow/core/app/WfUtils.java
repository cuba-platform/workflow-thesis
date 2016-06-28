/*
 * Copyright (c) 2008-2016 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/commercial-software-license for details.
 */

package com.haulmont.workflow.core.app;

import com.haulmont.workflow.core.entity.Card;
import org.apache.commons.lang.CharUtils;
import org.apache.commons.lang.StringUtils;

public class WfUtils {

    /**
     * Checks whether card's state field contains sended state
     *
     * @param card
     * @param state
     * @return
     */
    public static boolean isCardInState(Card card, String state) {
        String currentState = card.getState();
        if (currentState == null) {
            if (state == null)
                return true;
            else
                return false;
        }

        return currentState.contains("," + state + ",");
    }

    /**
     * Checks whether card's state field contains one or more state from states param
     *
     * @param card
     * @param states
     * @return
     */
    public static boolean isCardInStateList(Card card, String... states) {
        for (String state : states) {
            if (isCardInState(card, state)) return true;
        }
        return false;
    }

    public static String trimState(Card card) {
        String[] states = StringUtils.substringsBetween(card.getState(), ",", ",");
        return (states != null && states.length != 0) ? states[0] : null;
    }

    public static String encodeKey(String msg) {
        return Translit.toTranslit(msg);
    }

    public static class Translit {

        private static final String[] charTable = new String[65536];

        static {
            charTable['\u0410'] = "A";
            charTable['\u0411'] = "B";
            charTable['\u0412'] = "V";
            charTable['\u0413'] = "G";
            charTable['\u0414'] = "D";
            charTable['\u0415'] = "E";
            charTable['\u0401'] = "E";
            charTable['\u0416'] = "ZH";
            charTable['\u0417'] = "Z";
            charTable['\u0418'] = "I";
            charTable['\u0419'] = "I";
            charTable['\u041A'] = "K";
            charTable['\u041B'] = "L";
            charTable['\u041C'] = "M";
            charTable['\u041D'] = "N";
            charTable['\u041E'] = "O";
            charTable['\u041F'] = "P";
            charTable['\u0420'] = "R";
            charTable['\u0421'] = "S";
            charTable['\u0422'] = "T";
            charTable['\u0423'] = "U";
            charTable['\u0424'] = "F";
            charTable['\u0425'] = "H";
            charTable['\u0426'] = "C";
            charTable['\u0427'] = "CH";
            charTable['\u0428'] = "SH";
            charTable['\u0429'] = "SH";
            charTable['\u042A'] = "";
            charTable['\u042B'] = "Y";
            charTable['\u042C'] = "";
            charTable['\u042D'] = "E";
            charTable['\u042E'] = "U";
            charTable['\u042F'] = "YA";
            charTable['\u0020'] = "_";
            charTable['\u0022'] = "_";
            charTable['\u002C'] = "_";
            charTable['\u002E'] = "_";

            for (int i = 0; i < charTable.length; i++) {
                char idx = (char) i;
                char lower = new String(new char[]{idx}).toLowerCase().charAt(0);
                if (charTable[i] != null) {
                    charTable[lower] = charTable[i].toLowerCase();
                }
            }
        }

        public static String toTranslit(String text) {
            char charBuffer[] = text.toCharArray();
            StringBuilder sb = new StringBuilder(text.length());
            for (char symbol : charBuffer) {
                String replace = charTable[symbol];
                if ((replace == null) && (!CharUtils.isAsciiAlphanumeric(symbol))) {
                    replace = "_";
                }
                sb.append(replace == null ? symbol : replace);
            }
            return sb.toString();
        }
    }
}