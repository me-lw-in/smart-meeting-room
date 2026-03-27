package com.example.smartmeetingroom.util;

public class StringCapitalizeUtil {

    public static String capitalizeEachWord(String str){
        String[] strArr = str.trim().split("\\s+");
        StringBuilder result = new StringBuilder();

        for (String word : strArr) {
            if (!word.isEmpty()) {
                result.append(Character.toUpperCase(word.charAt(0)))
                        .append(word.substring(1).toLowerCase())
                        .append(" ");
            }
        }

        return result.toString().trim();
    }
}
