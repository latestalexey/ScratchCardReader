package com.google.android.gms.samples.vision.ocrreader;

/**
 * Created by vlad on 01.08.17.
 */

public class Utils {


    public static boolean isPIN(String text) {

        int[] result = {0, 0, 0, 0, 1, 0, 0, 0, 1, 0, 0, 0, 0, 1, 0, 0, 0};
        char[] textSymbol = text.toCharArray();

        if (textSymbol.length == Preferences.PINSIZE) {

            int[] buf = new int[Preferences.PINSIZE];
            for (int i = 0; i < textSymbol.length; i++) {

                if (isNumber(textSymbol[i])) {
                    buf[i] = 0;
                } else {
                    if (textSymbol[i] != ' ') {
                        return false;
                    } else {
                        buf[i] = 1;
                    }
                }
            }
            for (int i = 0; i < Preferences.PINSIZE; i++) {
                if (buf[i] != result[i]) {
                    return false;
                }
            }
        } else {
            return false;
        }
        return true;
    }
    public static boolean isSerial(String text) {

        int[] result = {0,0,0,0, 1, 0,0,0, 1, 0,0,0};
        char[] textSymbol = text.toCharArray();

        if (textSymbol.length == Preferences.SERIALSIZE) {

            int[] buf = new int[Preferences.SERIALSIZE];
            for (int i = 0; i < textSymbol.length; i++) {

                if (isNumber(textSymbol[i])) {
                    buf[i] = 0;
                } else {
                    if (textSymbol[i] != ' ') {
                        return false;
                    } else {
                        buf[i] = 1;
                    }
                }
            }
            for (int i = 0; i < Preferences.SERIALSIZE; i++) {
                if (buf[i] != result[i]) {
                    return false;
                }
            }
        } else {
            return false;
        }
        return true;
    }
    private static boolean isNumber(char text) {

        switch (text) {
            case '0':
                return true;
            case '1':
                return true;
            case '2':
                return true;
            case '3':
                return true;
            case '4':
                return true;
            case '5':
                return true;
            case '6':
                return true;
            case '7':
                return true;
            case '8':
                return true;
            case '9':
                return true;
        }
        return false;
    }
    public static String prepareSt(String text)
    {
        String buf = "";
        char[] textSymbol = text.toCharArray();
        if(textSymbol.length == Preferences.PINSIZE + 1 && !isNumber(textSymbol[0]))
        {

            for (int i = 1; i < Preferences.PINSIZE + 1; i++) {
                buf = buf + textSymbol[i];
            }
        }
        return buf;
    }
}
