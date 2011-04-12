/*
 * Copyright (c) 2010 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.

 * Author: Maxim Gorbunkov
 * Created: 18.11.2010 14:07:37
 *
 * $Id$
 */
package com.haulmont.workflow.core.app;

import com.haulmont.workflow.core.entity.Card;
import org.apache.commons.lang.CharUtils;


public class WfUtils {

    public static final String ENC = "UTF-8";

    /**
     * Checks whether card's state field contains sended state
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
            charTable['\u042A'] = "'";
            charTable['\u042B'] = "Y";
            charTable['\u042C'] = "'";
            charTable['\u042D'] = "E";
            charTable['\u042E'] = "U";
            charTable['\u042F'] = "YA";

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
                if((replace==null)&&(!CharUtils.isAscii(symbol))){
                    replace="_";
                }
                sb.append(replace == null ? symbol : replace);
            }
            return sb.toString();
        }


    }
}
