package com.realestate.utils;

public class MessagesUtil {

    public static String resultEnglishMessageBuilder(String description, String neighbourhood, String size, String price) {
        return description + "\n\n" +
                "Location : " + neighbourhood + "\n\n" +
                "Size: " + size + "\n\n" +
                "Price: " + PriceUtil.formatPriceToCommas(price) + " AED\n\n";
    }

    public static String resultRussianMessageBuilder(String description, String neighbourhood, String size, String price) {
        return description + "\n\n" +
                "Локация : " + neighbourhood + "\n\n" +
                "Размер: " + size + "\n\n" +
                "Цена: " + PriceUtil.formatPriceToCommas(price) + " AED\n\n";
    }

}
