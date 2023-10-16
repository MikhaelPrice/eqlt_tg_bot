package com.real_estate.eqlt.service;

import com.real_estate.eqlt.model.CurrencyModel;
import org.apache.http.ParseException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Scanner;

public class CurrencyService {
    public static String getCurrencyRate(String message, CurrencyModel model) throws IOException, ParseException, java.text.ParseException {
        URL url = new URL("https://www.nbrb.by/api/exrates/rates/" + message + "?parammode=2");
        Scanner scanner = new Scanner((InputStream) url.getContent());
        StringBuilder result = new StringBuilder();
        while (scanner.hasNext()) {
            result.append(scanner.nextLine());
        }
        JSONObject object = new JSONObject(result.toString());

        model.setCurId(object.getInt("Cur_ID"));
        model.setDate(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").parse(object.getString("Date")));
        model.setCurAbbreviation(object.getString("Cur_Abbreviation"));
        model.setCurScale(object.getInt("Cur_Scale"));
        model.setCurName(object.getString("Cur_Name"));
        model.setCurOfficialRate(object.getDouble("Cur_OfficialRate"));


        return "Official rate of BYN to " + model.getCurAbbreviation() + "\n" +
                "on the date: " + getFormatDate(model) + "\n" +
                "is: " + model.getCurOfficialRate() + " BYN per " + model.getCurScale() + " " + model.getCurAbbreviation();

    }

    private static String getFormatDate(CurrencyModel model) {
        return new SimpleDateFormat("dd MMM yyyy").format(model.getDate());
    }
}

