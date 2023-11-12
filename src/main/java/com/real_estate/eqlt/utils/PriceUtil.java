package com.real_estate.eqlt.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PriceUtil {

    private static final String HIGHEST_PRICE = "100000000000";

    public static List<String> getPricesBorders(String priceString) {
        List<String> prices = new ArrayList<>();
        Pattern pattern = Pattern.compile("[0-9]+");
        Matcher matcher = pattern.matcher(priceString);
        while (matcher.find()) {
            prices.add(matcher.group());
        }
        if (prices.size() == 1) {
            prices.add(HIGHEST_PRICE);
        }
        return prices;
    }
}
