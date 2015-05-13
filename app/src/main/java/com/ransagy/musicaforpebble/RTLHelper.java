package com.ransagy.musicaforpebble;

import android.util.Pair;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RTLHelper {

    private static Pattern hebrewPattern = Pattern.compile("\\p{InHebrew}", Pattern.UNICODE_CASE);

    private RTLHelper() {
    }

    public static Pair<String, String> ReorderRTLTextForPebble(String source, int charLimit) {
        if (source == null || source.equals("")) return new Pair<>("","");

        if (!hebrewPattern.matcher(source).find()) {
            return new Pair<>(source, "");
        } else {

            StringBuilder sbResult = new StringBuilder();
            StringBuilder sbExtraResult = new StringBuilder();
            StringBuilder sbTemp = new StringBuilder();
            String[] words = source.split(" ");
            int charCount = 0;

            for (int wordIndex = 0; wordIndex < words.length; wordIndex++, sbTemp.setLength(0)) {
                sbTemp.append(words[wordIndex]);
                charCount += sbTemp.length();
                Matcher hebrewWord = hebrewPattern.matcher(words[wordIndex]);

                if (hebrewWord.find()) {
                    sbTemp.reverse();
                    if (sbTemp.charAt(0) == ')') {
                        sbTemp.replace(0,1,"(");
                    }
                    if (sbTemp.charAt(sbTemp.length()-1) == '(') {
                        sbTemp.replace(sbTemp.length()-1,sbTemp.length(),")");
                    }
                }

                if (charCount <= charLimit) {
                    sbResult.insert(0, sbTemp + " ");
                } else {
                    sbExtraResult.insert(0, sbTemp + " ");
                }

                charCount++;
            }

            if (sbExtraResult.length() > charLimit) {
                sbExtraResult.replace(0,sbExtraResult.length()-charLimit,"...");
            }

            return new Pair<>(sbResult.toString().trim(), sbExtraResult.toString().trim());
        }
    }
}
