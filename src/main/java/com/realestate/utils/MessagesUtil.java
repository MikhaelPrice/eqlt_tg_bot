package com.realestate.utils;

import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

import java.io.IOException;

public class MessagesUtil {

    public static void sendText(Long chatId, String botToken, String description, String neighbourhood, String size, String price) {
        try {
            CloseableHttpClient httpClient = HttpClients.createDefault();
            HttpPost httpPost = new HttpPost("https://api.telegram.org/bot"
                    + botToken + "/sendMessage");

            String message = description + "\n\n" +
                    "*Location* : " + neighbourhood + "\n\n" +
                    "*Size*: " + size + "\n\n" +
                    "*Price*: " + PriceUtil.formatPriceToCommas(price) + " AED\n\n";
            StringEntity params = new StringEntity("chat_id=" + chatId +
                    "&text=" + message + "&parse_mode=Markdown");
            httpPost.addHeader("content-type", "application/x-www-form-urlencoded");
            httpPost.setEntity(params);

            httpClient.execute(httpPost);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
