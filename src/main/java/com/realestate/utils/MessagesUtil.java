package com.realestate.utils;

public class MessagesUtil {

    public static String resultMessageBuilder(String description, String neighbourhood, String size, String price) {
        return description + "\n\n" +
                "Location : " + neighbourhood + "\n\n" +
                "Size: " + size + "\n\n" +
                "Price: " + PriceUtil.formatPriceToCommas(price) + " AED\n\n";
    }

}
