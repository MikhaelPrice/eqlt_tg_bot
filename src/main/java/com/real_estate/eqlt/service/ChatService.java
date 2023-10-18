package com.real_estate.eqlt.service;

import com.real_estate.eqlt.model.CurrencyModel;
import org.apache.http.ParseException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Scanner;

public class ChatService {
    public static String getChatMessages(Long chatId) throws IOException, ParseException {
        URL url = new URL("https://api.telegram.org/bot6613924434:AAGfn2kHGtlXAwIdgUYlmIaBNfw4ZjbrsuE/getUpdates?chat_id=" + chatId);
        Scanner scanner = new Scanner((InputStream) url.getContent());
        StringBuilder result = new StringBuilder();
        while (scanner.hasNext()) {
            result.append(scanner.nextLine());
        }
        JSONObject object = new JSONObject(result.toString());

        return "Messages from chat: " + result;

    }

    private static String getFormatDate(CurrencyModel model) {
        return new SimpleDateFormat("dd MMM yyyy").format(model.getDate());
    }
}

