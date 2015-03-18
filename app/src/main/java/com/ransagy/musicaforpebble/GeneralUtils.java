package com.ransagy.musicaforpebble;

import android.graphics.Paint;
import android.graphics.Rect;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GeneralUtils {

    public static int TryParseInteger(String source, int defaultValue) {
        try {
            return Integer.parseInt(source);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    public static float TryParseFloat(String source, float defaultValue) {
        try {
            return Float.parseFloat(source);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    public static float GetAndroidDefaultTextWidth(String source, boolean onlyHebrew) {
        android.graphics.Paint p = new Paint();
        String result = source;

        if (onlyHebrew) {
            Pattern hebrewPattern = Pattern.compile("[^\\p{InHebrew}]", Pattern.UNICODE_CASE);
            Matcher matchNonHebrew = hebrewPattern.matcher(source);
            result = matchNonHebrew.replaceAll("");
        }

        return p.measureText(result);
    }

    public static void DebugSomething() {
        String[] text = {"מי אם לא אני", "שירת הסטיקר", "הווליום עולה", "(רציתי שתדע"};
        ArrayList<Float> widths = new ArrayList<>();
        ArrayList<Rect> bounds = new ArrayList<>();

        android.graphics.Paint p = new Paint();

        for (int index = 0; index < text.length; index++) {
            widths.add(p.measureText(text[index]));
            Rect currBounds = new Rect();
            p.getTextBounds(text[index], 0, text[index].length(), currBounds);
            bounds.add(currBounds);
        }

        widths.toString();
    }
}
