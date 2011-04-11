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
            charTable['�'] = "A";
            charTable['�'] = "B";
            charTable['�'] = "V";
            charTable['�'] = "G";
            charTable['�'] = "D";
            charTable['�'] = "E";
            charTable['�'] = "E";
            charTable['�'] = "ZH";
            charTable['�'] = "Z";
            charTable['�'] = "I";
            charTable['�'] = "I";
            charTable['�'] = "K";
            charTable['�'] = "L";
            charTable['�'] = "M";
            charTable['�'] = "N";
            charTable['�'] = "O";
            charTable['�'] = "P";
            charTable['�'] = "R";
            charTable['�'] = "S";
            charTable['�'] = "T";
            charTable['�'] = "U";
            charTable['�'] = "F";
            charTable['�'] = "H";
            charTable['�'] = "C";
            charTable['�'] = "CH";
            charTable['�'] = "SH";
            charTable['�'] = "SH";
            charTable['�'] = "'";
            charTable['�'] = "Y";
            charTable['�'] = "'";
            charTable['�'] = "E";
            charTable['�'] = "U";
            charTable['�'] = "YA";

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
