package com.realestate.utils;

public class MessagesUtil {

    public static String resultMessageBuilder(String description, String project, String neighbourhood, String size, String price) {
        return description + "\n" +
                "Location : " + neighbourhood + "\n" +
                "Project : " + project + "\n" +
                "Size: " + size + "\n" +
                "Price: " + price + " AED\n";
    }
}
