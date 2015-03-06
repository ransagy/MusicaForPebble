package com.ransagy.musicaforpebble;

public class RTLHelper {

    private static int MAX_CHARS_PER_LINE = 12;

    private RTLHelper() {
    }

    public static String ReorderTextForRTL(String source, boolean isMultiline) {
        String result;

        if (isMultiline && source.contains(" ")) {
            StringBuilder sbResult = new StringBuilder();
            StringBuilder sbTemp = new StringBuilder();
            String[] words = source.split(" ");

            // TODO source strings of (MAX_CHARS_PER_LINE-1) aren't displayed properly.
            for (int wordIndex = 0, charCount = 0; wordIndex < words.length; wordIndex++, sbTemp.setLength(0)) {
                sbTemp.append(words[wordIndex]);
                charCount += words[wordIndex].length();

                if (charCount < MAX_CHARS_PER_LINE) {
                    sbResult.insert(0, sbTemp.reverse() + " ");
                } else {
                    if (charCount == MAX_CHARS_PER_LINE) {
                        sbTemp.append(" ");
                        charCount++;
                    }

                    sbResult.append(sbTemp.reverse());
                    sbResult.append(" ");
                }

                charCount++;
            }

            result = sbResult.toString().trim();
        } else {
            result = new StringBuilder(source).reverse().toString().trim();
        }

        return result;
    }
}
