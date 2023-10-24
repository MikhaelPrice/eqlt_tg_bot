package com.real_estate.eqlt;

import com.real_estate.eqlt.config.BotConfig;
import com.real_estate.eqlt.domain.EqtUsers;
import com.real_estate.eqlt.repos.EqtUserRepo;
import com.real_estate.eqlt.utils.TimeUtils;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.*;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;
import org.telegram.telegrambots.meta.api.objects.commands.scope.BotCommandScopeDefault;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

@Component
@Slf4j
public class TelegramBot extends TelegramLongPollingBot {

    private final BotConfig botConfig;
    private final List<BotCommand> commands;

    @Autowired
    private EqtUserRepo eqtUserRepo;

    public TelegramBot(BotConfig botConfig) {
        this.botConfig = botConfig;
        this.commands = initCommands();
    }

    @Override
    public String getBotUsername() {
        return botConfig.getBotName();
    }

    @Override
    public String getBotToken() {
        return botConfig.getToken();
    }

    @SneakyThrows
    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            String messageText = update.getMessage().getText();
            long chatId = update.getMessage().getChatId();
            switch (messageText) {
                case "/start" -> {
                    startCommand(chatId, update.getMessage().getChat().getFirstName());
                    registerUser(update.getMessage());
                    sendRealEstateCommand(chatId);
                }
                case "/end" -> endCommand(chatId, update.getMessage().getChat().getFirstName());
            }
        } else if (update.hasCallbackQuery()) {
            CallbackQuery query = update.getCallbackQuery();
            String callBackData = query.getData();
            long chatId = query.getMessage().getChatId();
            EqtUsers user = new EqtUsers();
            if (eqtUserRepo.findById(chatId).isPresent()) {
                user = eqtUserRepo.findById(chatId).get();
            } else {
                user.setId(chatId);
            }
            switch (callBackData) {
                case "Подобрать недвижимость" -> sendRealEstateType(chatId);
                case "Связаться с администратором" -> sendMessage(chatId, "Напиши https://web.telegram.org/k/#@psldvch");
            }
            switch (callBackData) {
                case "Аппартаменты", "Пентхаус", "Таунхаус", "Вилла", "Земельный участок" -> {
                    sendRealEstateRegionChoice(chatId);
                    user.setType(callBackData);
                    eqtUserRepo.save(user);
                }
            }
            switch (callBackData) {
                case "Интерка", "Минск-Мир", "Каменка" -> {
                    sendPriceCommand(chatId);
                    user.setRegion(callBackData);
                    eqtUserRepo.save(user);
                }
            }
            switch (callBackData) {
                case "40 гривен", "50 баксов", "100 рублей", "5 Никит", "0,00002 евра" -> {
                    sendRealEstateReadiness(chatId);
                    user.setPrice(callBackData);
                    eqtUserRepo.save(user);
                }
            }
            switch (callBackData) {
                case "Готовая", "На этапе строительства", "Неважно" -> {
                    user.setReadiness(callBackData);
                    eqtUserRepo.save(user);
                }
            }
        }
    }

    private void startCommand(Long chatId, String username) {
        String answer = "Hi, " + username + ", nice to meet you!" + "\n";
        sendMessage(chatId, answer);
    }

    private void endCommand(Long chatId, String username) {
        String answer = "Goodbye, " + username + ", thank you for using our bot!";
        sendMessage(chatId, answer);
    }

    private void sendRealEstateReadiness(Long chatId) {
        SendMessage message = new SendMessage();
        SendPhoto photo = addPictureToMessage(chatId, "https://th.bing.com/th/id/OIP.rGbzi6L-AkPLV2Umfqv2fQHaGX?pid=ImgDet&rs=1");
        message.setChatId(String.valueOf(chatId));
        message.setText("Тип готовности недвижимости");
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> buttons = new ArrayList<>();
        List<InlineKeyboardButton> button1 = new ArrayList<>();
        List<InlineKeyboardButton> button2 = new ArrayList<>();
        List<InlineKeyboardButton> button3 = new ArrayList<>();
        InlineKeyboardButton selectRealEstateWayToPay1 = new InlineKeyboardButton();
        InlineKeyboardButton selectRealEstateWayToPay2 = new InlineKeyboardButton();
        InlineKeyboardButton selectRealEstateWayToPay3 = new InlineKeyboardButton();
        selectRealEstateWayToPay1.setText("Готовая");
        selectRealEstateWayToPay1.setCallbackData("Готовая");
        selectRealEstateWayToPay2.setText("На этапе строительства");
        selectRealEstateWayToPay2.setCallbackData("На этапе строительства");
        selectRealEstateWayToPay3.setText("Неважно");
        selectRealEstateWayToPay3.setCallbackData("Неважно");
        button1.add(selectRealEstateWayToPay1);
        button2.add(selectRealEstateWayToPay2);
        button3.add(selectRealEstateWayToPay3);
        buttons.add(button1);
        buttons.add(button2);
        buttons.add(button3);
        inlineKeyboardMarkup.setKeyboard(buttons);
        message.setReplyMarkup(inlineKeyboardMarkup);
        try {
            execute(photo);
            execute(message);
        } catch (TelegramApiException e) {
            log.error("Error occurred {}", e.getMessage());
        }
    }

    private void sendRealEstateCommand(Long chatId) {
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText("Давай выбирать");
        SendPhoto photo = addPictureToMessage(chatId, "https://cdn.pixabay.com/photo/2019/04/15/14/22/choice-4129455_1280.jpg");
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> buttons = new ArrayList<>();
        List<InlineKeyboardButton> button1 = new ArrayList<>();
        List<InlineKeyboardButton> button2 = new ArrayList<>();
        InlineKeyboardButton selectRealEstateButton1 = new InlineKeyboardButton();
        InlineKeyboardButton selectRealEstateButton2 = new InlineKeyboardButton();
        selectRealEstateButton1.setText("Подобрать недвижимость");
        selectRealEstateButton2.setText("Связаться с администратором");
        selectRealEstateButton1.setCallbackData("Подобрать недвижимость");
        selectRealEstateButton2.setCallbackData("Связаться с администратором");
        selectRealEstateButton2.setUrl("https://web.telegram.org/k/#@psldvch");
        button1.add(selectRealEstateButton1);
        button2.add(selectRealEstateButton2);
        buttons.add(button1);
        buttons.add(button2);
        inlineKeyboardMarkup.setKeyboard(buttons);
        message.setReplyMarkup(inlineKeyboardMarkup);
        try {
            execute(photo);
            execute(message);
        } catch (TelegramApiException e) {
            log.error("Error occurred {}", e.getMessage());
        }
    }

    private void sendRealEstateRegionChoice(Long chatId) {
        SendMessage message = new SendMessage();
        SendPhoto photo = addPictureToMessage(chatId, "https://th.bing.com/th/id/OIP.1qfWWExYU6DQIfKVdS5KiwHaFj?pid=ImgDet&rs=1");
        message.setChatId(String.valueOf(chatId));
        message.setText("Где бы недвижимость хотел?");
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> buttons = new ArrayList<>();
        List<InlineKeyboardButton> button1 = new ArrayList<>();
        List<InlineKeyboardButton> button2 = new ArrayList<>();
        List<InlineKeyboardButton> button3 = new ArrayList<>();
        InlineKeyboardButton realEstateRegion1 = new InlineKeyboardButton();
        InlineKeyboardButton realEstateRegion2 = new InlineKeyboardButton();
        InlineKeyboardButton realEstateRegion3 = new InlineKeyboardButton();
        realEstateRegion1.setText("Интерка");
        realEstateRegion2.setText("Минск-Мир");
        realEstateRegion3.setText("Каменка");
        realEstateRegion1.setCallbackData("Интерка");
        realEstateRegion2.setCallbackData("Минск-Мир");
        realEstateRegion3.setCallbackData("Каменка");
        button1.add(realEstateRegion1);
        button2.add(realEstateRegion2);
        button3.add(realEstateRegion3);
        buttons.add(button1);
        buttons.add(button2);
        buttons.add(button3);
        inlineKeyboardMarkup.setKeyboard(buttons);
        message.setReplyMarkup(inlineKeyboardMarkup);
        try {
            execute(photo);
            execute(message);
        } catch (TelegramApiException e) {
            log.error("Error occurred {}", e.getMessage());
        }
    }

    private void sendRealEstateType(Long chatId) {
        SendMessage message = new SendMessage();
        SendPhoto photo = addPictureToMessage(chatId, "https://th.bing.com/th/id/OIP.83GCSDpACGl4_xw4QJVsHQHaEa?pid=ImgDet&rs=1");
        message.setChatId(String.valueOf(chatId));
        message.setText("Что будем брать ?");
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> buttons = new ArrayList<>();
        List<InlineKeyboardButton> button1 = new ArrayList<>();
        List<InlineKeyboardButton> button2 = new ArrayList<>();
        List<InlineKeyboardButton> button3 = new ArrayList<>();
        List<InlineKeyboardButton> button4 = new ArrayList<>();
        List<InlineKeyboardButton> button5 = new ArrayList<>();
        InlineKeyboardButton realEstateButton1 = new InlineKeyboardButton();
        InlineKeyboardButton realEstateButton2 = new InlineKeyboardButton();
        InlineKeyboardButton realEstateButton3 = new InlineKeyboardButton();
        InlineKeyboardButton realEstateButton4 = new InlineKeyboardButton();
        InlineKeyboardButton realEstateButton5 = new InlineKeyboardButton();
        realEstateButton1.setText("Аппартаменты");
        realEstateButton2.setText("Пентхаус");
        realEstateButton3.setText("Таунхаус");
        realEstateButton4.setText("Вилла");
        realEstateButton5.setText("Земельный участок");
        realEstateButton1.setCallbackData("Аппартаменты");
        realEstateButton2.setCallbackData("Пентхаус");
        realEstateButton3.setCallbackData("Таунхаус");
        realEstateButton4.setCallbackData("Вилла");
        realEstateButton5.setCallbackData("Земельный участок");
        button1.add(realEstateButton1);
        button2.add(realEstateButton2);
        button3.add(realEstateButton3);
        button4.add(realEstateButton4);
        button5.add(realEstateButton5);
        buttons.add(button1);
        buttons.add(button2);
        buttons.add(button3);
        buttons.add(button4);
        buttons.add(button5);
        inlineKeyboardMarkup.setKeyboard(buttons);
        message.setReplyMarkup(inlineKeyboardMarkup);
        try {
            execute(photo);
            execute(message);
        } catch (TelegramApiException e) {
            log.error("Error occurred {}", e.getMessage());
        }
    }

    private void sendPriceCommand(Long chatId) {
        String photoUrl = "https://www.meme-arsenal.com/memes/b4b2c9cb09b2c8310ba73625e3f2b600.jpg";
        SendPhoto photo = addPictureToMessage(chatId, photoUrl);
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText("По какой цене будем брать?");
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> buttons = new ArrayList<>();
        List<InlineKeyboardButton> button1 = new ArrayList<>();
        List<InlineKeyboardButton> button2 = new ArrayList<>();
        List<InlineKeyboardButton> button3 = new ArrayList<>();
        List<InlineKeyboardButton> button4 = new ArrayList<>();
        List<InlineKeyboardButton> button5 = new ArrayList<>();
        InlineKeyboardButton priceButton1 = new InlineKeyboardButton();
        InlineKeyboardButton priceButton2 = new InlineKeyboardButton();
        InlineKeyboardButton priceButton3 = new InlineKeyboardButton();
        InlineKeyboardButton priceButton4 = new InlineKeyboardButton();
        InlineKeyboardButton priceButton5 = new InlineKeyboardButton();
        priceButton1.setText("40 гривен");
        priceButton2.setText("50 баксов");
        priceButton3.setText("100 рублей");
        priceButton4.setText("5 Никит");
        priceButton5.setText("0,00002 евра");
        priceButton1.setCallbackData("40 гривен");
        priceButton2.setCallbackData("50 баксов");
        priceButton3.setCallbackData("100 рублей");
        priceButton4.setCallbackData("5 Никит");
        priceButton5.setCallbackData("0,00002 евра");
        button1.add(priceButton1);
        button2.add(priceButton2);
        button3.add(priceButton3);
        button4.add(priceButton4);
        button5.add(priceButton5);
        buttons.add(button1);
        buttons.add(button2);
        buttons.add(button3);
        buttons.add(button4);
        buttons.add(button5);
        inlineKeyboardMarkup.setKeyboard(buttons);
        message.setReplyMarkup(inlineKeyboardMarkup);
        try {
            execute(photo);
            execute(message);
        } catch (TelegramApiException e) {
            log.error("Error occurred {}", e.getMessage());
        }
    }

    private void sendMessage(Long chatId, String textToSend) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(String.valueOf(chatId));
        sendMessage.setText(textToSend);
        try {
            execute(sendMessage);
        } catch (TelegramApiException e) {
            log.error("Error occurred {}", e.getMessage());
        }
    }

    @SneakyThrows
    private List<BotCommand> initCommands() {
        List<BotCommand> commands = new ArrayList<>();
        commands.add(new BotCommand("/start", "start the bot"));
        commands.add(new BotCommand("/end", "end the bot"));
        try {
            this.execute(new SetMyCommands(commands, new BotCommandScopeDefault(), null));
        } catch (TelegramApiException e) {
            log.error("Error occurred {}", e.getMessage());
        }
        return commands;
    }

    @SneakyThrows
    private SendPhoto addPictureToMessage(Long chatId, String url) {
        URL imageUrl = null;
        try {
            imageUrl = new URL(url);
        } catch (MalformedURLException e) {
            log.error("Error occurred {}", e.getMessage());
        }
        InputStream imageStream = null;
        if (imageUrl != null) {
            imageStream = imageUrl.openStream();
        }
        InputFile inputFile = new InputFile(imageStream, "photo.jpg");
        return new SendPhoto(chatId.toString(), inputFile);
    }

    private void registerUser(Message msg) {
        if (eqtUserRepo.findById(msg.getChatId()).isEmpty()) {
            long chatId = msg.getChatId();
            Chat chat = msg.getChat();
            EqtUsers user = new EqtUsers();
            user.setId(chatId);
            user.setUsername(chat.getUserName());
            user.setRegisteredAt(TimeUtils.userRegisterDate());
            eqtUserRepo.save(user);
        }
    }

}
