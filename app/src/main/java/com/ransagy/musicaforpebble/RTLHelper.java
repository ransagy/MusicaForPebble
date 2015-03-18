package com.ransagy.musicaforpebble;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RTLHelper {

    private static Pattern hebrewPattern = Pattern.compile("\\p{InHebrew}", Pattern.UNICODE_CASE);

    // 0 = default method, 1 = alt method. TEMP.
    public static int CurrentMode = 0;

    private RTLHelper() {
    }

    public static String ReorderTextForRTL(String source, int maxCharsPerLine) {
        return CurrentMode == 0 ? ReorderTextForRTLDefault(source, maxCharsPerLine) : ReorderTextForRTLAlt(source, maxCharsPerLine);
    }

    public static String ReorderTextForRTLDefault(String source, int maxCharsPerLine) {
        String result;

        if (maxCharsPerLine > 0 && source.contains(" ")) {
            StringBuilder sbFirstLine = new StringBuilder();
            StringBuilder sbSecondLine = new StringBuilder();
            StringBuilder sbTemp = new StringBuilder();
            String[] words = source.split(" ");
            int charCount = 0;

            // TODO handle too long strings so ellipsis doesn't cut them in the wrong place.
            // TODO issues with symbol (parenthesis, etc) display.
            // TODO certain cases of wrong padding, giving either too much space or blanking the display entirely.
            for (int wordIndex = 0; wordIndex < words.length; wordIndex++, sbTemp.setLength(0)) {
                sbTemp.append(words[wordIndex]);
                charCount += words[wordIndex].length();
                Matcher hebrewWord = hebrewPattern.matcher(words[wordIndex]);

                if (charCount <= maxCharsPerLine) {
                    sbFirstLine.insert(0, (hebrewWord.find() ? sbTemp.reverse() : sbTemp) + " ");
                } else {
                    sbSecondLine.insert(0, (hebrewWord.find() ? sbTemp.reverse() : sbTemp) + " ");
                }

                charCount++;
            }

            String firstLine = sbFirstLine.toString().trim();
            String secondLine = sbSecondLine.toString().trim();
            int paddingNeeded = secondLine.length() > 0 && firstLine.length() < maxCharsPerLine ? maxCharsPerLine - firstLine.length() : 1;
            result = firstLine + (secondLine.length() > 0 ? repeatString(" ", paddingNeeded) + secondLine : "");
        } else {
            result = new StringBuilder(source).reverse().toString().trim();
        }

        return result;
    }

    public static String ReorderTextForRTLAlt(String source, float maxCharsPerLine) {
        String result;

        if (maxCharsPerLine > 0 && source.contains(" ")) {
            StringBuilder sbFirstLine = new StringBuilder();
            StringBuilder sbSecondLine = new StringBuilder();
            StringBuilder sbTemp = new StringBuilder();
            String[] words = source.split(" ");

            // TODO handle too long strings so ellipsis doesn't cut them in the wrong place.
            // TODO issues with symbol (parenthesis, etc) display.
            for (int wordIndex = 0; wordIndex < words.length; wordIndex++, sbTemp.setLength(0)) {
                sbTemp.append(words[wordIndex]);
                Matcher hebrewWord = hebrewPattern.matcher(words[wordIndex]);
                float predictedWidth = GeneralUtils.GetAndroidDefaultTextWidth(words[wordIndex] + (sbFirstLine.length() > 0 ? " " + sbFirstLine.toString().trim() : ""), false);

                if (predictedWidth <= maxCharsPerLine) {
                    sbFirstLine.insert(0, (hebrewWord.find() ? sbTemp.reverse() : sbTemp) + " ");
                } else {
                    sbSecondLine.insert(0, (hebrewWord.find() ? sbTemp.reverse() : sbTemp) + " ");
                }
            }

            String firstLine = sbFirstLine.toString().trim();
            String secondLine = sbSecondLine.toString().trim();
            result = firstLine + (secondLine.length() > 0 ? " " + secondLine : "");
        } else {
            result = new StringBuilder(source).reverse().toString().trim();
        }

        return result;
    }

    private static String repeatString(String input, int count) {
        StringBuilder output = new StringBuilder();

        for (int countIndex = 0; countIndex < count; countIndex++) {
            output.append(input);
        }

        return output.toString();
    }
}
