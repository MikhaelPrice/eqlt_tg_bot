package com.realestate.utils;

import com.realestate.entity.EqtRealEstates;
import com.realestate.repo.EqtRealEstatesRepo;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PriceUtil {

    private static final String HIGHEST_PRICE = "100000000000";

    public static List<String> getPriceRange(String priceString) {
        List<String> prices = new ArrayList<>();
        priceString = formatPriceOutOfCommas(priceString);
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

    public long getPopularPrices(EqtRealEstatesRepo eqtRealEstatesRepo, long lowRange, long highRange) {
        Iterable<EqtRealEstates> eqtRealEstates = eqtRealEstatesRepo.findAll();
        Iterator<EqtRealEstates> iterator = eqtRealEstates.iterator();
        List<Long> prices = new ArrayList<>();
        while (iterator.hasNext()) {
            EqtRealEstates realEstate = iterator.next();
            prices.add(Long.parseLong(realEstate.getPrice()));
        }
        return prices.stream()
                .filter(num -> num >= lowRange && num <= highRange)
                .count();
    }

    public static String formatPriceToCommas(String price) {
        return String.format(Locale.US, "%,d", Long.parseLong(price));
    }

    public static String formatPriceOutOfCommas(String price) {
        return price.replaceAll(",", "");
    }

}
