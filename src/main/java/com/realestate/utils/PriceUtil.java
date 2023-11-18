package com.realestate.utils;

import com.realestate.domain.EqtRealEstates;
import com.realestate.repos.EqtRealEstatesRepo;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class PriceUtil {

    private static final String HIGHEST_PRICE = "100000000000";

    public static List<String> getPricesRange(String priceString) {
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

    public long getPopularPrices(EqtRealEstatesRepo eqtRealEstatesRepo, long lowRange, long highRange){
        Iterable<EqtRealEstates> eqtRealEstates = eqtRealEstatesRepo.findAll();
        Iterator<EqtRealEstates> iterator = eqtRealEstates.iterator();
        List<Long> prices = new ArrayList<>();
        while (iterator.hasNext()){
            EqtRealEstates realEstate = iterator.next();
            prices.add(Long.parseLong(realEstate.getPrice()));
        }
        return prices.stream()
                .filter(num -> num >= lowRange && num <= highRange)
                .count();
    }
}
