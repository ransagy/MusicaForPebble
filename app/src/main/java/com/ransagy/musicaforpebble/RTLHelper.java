package com.ransagy.musicaforpebble;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RTLHelper {

    private static Pattern hebrewPattern = Pattern.compile("\\p{InHebrew}", Pattern.UNICODE_CASE);

    private RTLHelper() {
    }

    public static String ReorderRTLTextForPebble(String source) {
        StringBuilder sbResult = new StringBuilder();

        if (!hebrewPattern.matcher(source).find()) {
            return source;
        } else {

            StringBuilder sbTemp = new StringBuilder();
            String[] words = source.split(" ");

            for (int wordIndex = 0; wordIndex < words.length; wordIndex++, sbTemp.setLength(0)) {
                sbTemp.append(words[wordIndex]);
                Matcher hebrewWord = hebrewPattern.matcher(words[wordIndex]);
                sbResult.insert(0, (hebrewWord.find() ? sbTemp.reverse() : sbTemp) + " ");
            }
        }

        return sbResult.toString().trim();
    }
}
