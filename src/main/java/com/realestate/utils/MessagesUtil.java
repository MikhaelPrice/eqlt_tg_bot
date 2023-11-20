package com.realestate.utils;

public class MessagesUtil {

    public static String resultMessageBuilder(String description,String neighbourhood, String size, String price) {
        return description + "\n" +
                "Location : " + neighbourhood + "\n" +
                "Size: " + size + "\n" +
                "Price: " + PriceUtil.formatPriceToCommas(price) + " AED\n";
    }

    public static String helloMessageBuilder() {
        return """
                What can this bot do?

                Вас приветствует виртуальный помощник "EQT Real Estate" \uD83D\uDE4C

                Я помогу вам подобрать недвижимость в Дубае, исходя из ваших условий.

                Для начала диалога нажмите /start""";
    }
}
