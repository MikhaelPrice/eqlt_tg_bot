package com.real_estate.eqlt;


import com.real_estate.eqlt.config.BotConfig;
import com.real_estate.eqlt.domain.EqtRealEstates;
import com.real_estate.eqlt.domain.EqtUsersChoices;
import com.real_estate.eqlt.repos.EqtRealEstatesRepo;
import com.real_estate.eqlt.repos.EqtUsersChoicesRepo;
import com.real_estate.eqlt.service.RealEstateService;
import com.real_estate.eqlt.utils.MessagesUtil;
import com.real_estate.eqlt.utils.PriceUtil;
import com.real_estate.eqlt.utils.TimeUtil;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;
import org.telegram.telegrambots.meta.api.objects.commands.scope.BotCommandScopeDefault;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.ArrayList;
import java.util.List;

@Component
@Slf4j
public class TelegramBot extends TelegramLongPollingBot {

    private final BotConfig botConfig;
    private final List<BotCommand> commands;

    public static final String CHOOSE_REAL_ESTATE = "choose real estate";
    public static final String CONTACT_FOR_MANAGER = "contact for manager";
    public static final String COMPLETED = "Completed";
    public static final String OFF_PLAN = "Off-Plan";
    public static final String NO_MATTER = "No matter";
    public static final String APARTMENTS = "Apartments";
    public static final String PENTHOUSES = "Penthouses";
    public static final String TOWNHOUSES = "Townhouses";
    public static final String VILLAS = "Villas";
    public static final String DUPLEX = "Duplex";
    public static final String SIMPLEX = "Simplex";
    public static final String TWO_FOUR_MILLIONS = "2000000 AED - 4000000 AED";
    public static final String FOUR_SIX_MILLIONS = "4000000 AED - 6000000 AED";
    public static final String SIX_EIGHT_MILLIONS = "6000000 AED - 8000000 AED";
    public static final String EIGHT_TEN_MILLIONS = "8000000 AED - 10000000 AED";
    public static final String TEN_PLUS_MILLIONS = "10000000 AED+";

    public static final String ADMIN_TG_LINK = "https://web.telegram.org/k/#@psldvch";
    public static final String BASIC_PICTURE_URL = "https://drive.google.com/uc?export=download&confirm=no_antivirus&id=1VmX8xOX4OUZv05n3lf8O7F__mxOm7Qfd";
    public static final String BASIC_RESPONSE_MESSAGE = "Here are several options for you " + "\\xF0\\x9F\\x91\\x87" + "\n";
    public static final String AFTER_RESULT_MESSAGE = "\\xE2\\x9C\\x85 For more additional information about object you can choose \"\\help\" in bot menu to contact our manager.";
    public static final String PRE_RESULT_MESSAGE = "\\xE2\\x8F\\xB0 One second... \n Searching for appropriate objects.";
    public static final String OBJECTS_NOT_FOUND = "There isn't any real estate according to your request";

    private final RealEstateService service;

    @Autowired
    private EqtUsersChoicesRepo eqtUsersChoicesRepo;

    @Autowired
    private EqtRealEstatesRepo eqtRealEstatesRepo;

    public TelegramBot(BotConfig botConfig) {
        this.botConfig = botConfig;
        this.commands = initCommands();
        this.service = new RealEstateService();
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
        List<List<String>> values = service.getExcelRealEstateSheetData("D:/Java/Beach Front база EQT.xlsx", 71);
        for (List<String> strings : values) {
            EqtRealEstates eqtRealEstates = new EqtRealEstates();
            eqtRealEstates.setNeighbourhood(strings.get(0));
            eqtRealEstates.setProject(strings.get(1));
            eqtRealEstates.setType(strings.get(2));
            eqtRealEstates.setWillingness(strings.get(3));
            eqtRealEstates.setPrice(strings.get(4));
            eqtRealEstates.setSize(strings.get(5));
            eqtRealEstates.setDescription(strings.get(6));
            eqtRealEstates.setPicture1(strings.get(7));
            eqtRealEstates.setPicture2(strings.get(8));
            eqtRealEstates.setPicture3(strings.get(9));
            eqtRealEstatesRepo.save(eqtRealEstates);
        }
        if (update.hasMessage() && update.getMessage().hasText()) {
            String messageText = update.getMessage().getText();
            long chatId = update.getMessage().getChatId();
            switch (messageText) {
                case "/start" -> {
                    startCommand(chatId, update.getMessage().getChat().getFirstName());
                    registerUser(update.getMessage());
                    sendRealEstateCommand(chatId, BASIC_PICTURE_URL);
                }
                case "/help" -> sendMessage(chatId, ADMIN_TG_LINK);
                case "/end" -> endCommand(chatId, update.getMessage().getChat().getFirstName());
            }
        } else if (update.hasCallbackQuery()) {
            CallbackQuery query = update.getCallbackQuery();
            String callBackData = query.getData();
            long chatId = query.getMessage().getChatId();
            EqtUsersChoices eqtUsersChoices;
            eqtUsersChoices = eqtUsersChoicesRepo.findById(chatId).orElseGet(() -> {
                EqtUsersChoices newUserChoice = new EqtUsersChoices();
                newUserChoice.setId(chatId);
                return newUserChoice;
            });
            switch (callBackData) {
                case CHOOSE_REAL_ESTATE -> sendRealEstateTypeCommand(chatId, BASIC_PICTURE_URL);
                case CONTACT_FOR_MANAGER -> sendMessage(chatId, ADMIN_TG_LINK);
            }
            switch (callBackData) {
                case APARTMENTS, PENTHOUSES, TOWNHOUSES, VILLAS, DUPLEX, SIMPLEX -> {
                    sendRealEstatePriceCommand(chatId, BASIC_PICTURE_URL);
                    eqtUsersChoices.setType(callBackData);
                    eqtUsersChoicesRepo.save(eqtUsersChoices);
                }
            }
            switch (callBackData) {
                case TWO_FOUR_MILLIONS, FOUR_SIX_MILLIONS,
                        SIX_EIGHT_MILLIONS, EIGHT_TEN_MILLIONS, TEN_PLUS_MILLIONS -> {
                    sendRealEstateWillingnessCommand(chatId, BASIC_PICTURE_URL);
                    eqtUsersChoices.setPrice(callBackData);
                    eqtUsersChoicesRepo.save(eqtUsersChoices);
                }
            }
            switch (callBackData) {
                case COMPLETED, OFF_PLAN, NO_MATTER -> {
                    eqtUsersChoices.setWillingness(callBackData);
                    eqtUsersChoicesRepo.save(eqtUsersChoices);
                    sendMessage(chatId, PRE_RESULT_MESSAGE);
                    List<String> prices = PriceUtil.getPricesBorders(eqtUsersChoices.getPrice());
                    List<Long> realEstatesIds = eqtRealEstatesRepo.findRealEstatesIds(
                            eqtUsersChoices.getType(),
                            eqtUsersChoices.getWillingness(), prices.get(0), prices.get(1));
                    if (realEstatesIds.size() > 0) {
                        sendMessage(chatId, BASIC_RESPONSE_MESSAGE);
                        for (Long id : realEstatesIds) {
                            if (eqtRealEstatesRepo.findById(id).isPresent()) {
                                EqtRealEstates realEstate = eqtRealEstatesRepo.findById(id).get();
                                String neighbourhood = realEstate.getNeighbourhood();
                                String description = realEstate.getDescription();
                                String project = realEstate.getProject();
                                String size = realEstate.getSize();
                                String price = realEstate.getPrice();
                                String picture1 = realEstate.getPicture1();
                                String picture2 = realEstate.getPicture2();
                                String picture3 = realEstate.getPicture3();
                                sendRealEstate(picture1, picture2, picture3,
                                        neighbourhood, project,
                                        description, size, price, chatId);
                            }
                        }
                        sendMessage(chatId, AFTER_RESULT_MESSAGE);
                    } else {
                        sendMessage(chatId, OBJECTS_NOT_FOUND);
                    }
                }
            }
        }
    }

    private void sendRealEstate(String picture1, String picture2, String picture3, String project,
                                String neighbourhood, String description, String size,
                                String price, Long chatId) {
        SendPhoto photo = new SendPhoto();
        photo.setChatId(chatId);
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> picturesTable = new ArrayList<>();
        List<InlineKeyboardButton> picturesList = new ArrayList<>();
        InlineKeyboardButton pictureButton1 = new InlineKeyboardButton();
        InlineKeyboardButton pictureButton2 = new InlineKeyboardButton();
        InlineKeyboardButton pictureButton3 = new InlineKeyboardButton();

        inlineKeyboardMarkup.setKeyboard(picturesTable);
        photo.setReplyMarkup(inlineKeyboardMarkup);

        sendMessage(chatId, MessagesUtil.resultMessageBuilder(description, project, neighbourhood, size, price));
    }

    private void startCommand(Long chatId, String username) {
        String answer = "Hi, " + username + ", nice to meet you!" + "\n";
        sendMessage(chatId, answer);
    }

    private void endCommand(Long chatId, String username) {
        String answer = "Goodbye, " + username + ", thank you for using our bot!";
        sendMessage(chatId, answer);
    }

    private void sendRealEstateWillingnessCommand(Long chatId, String pictureUrl) {
        SendPhoto photo = new SendPhoto();
        photo.setChatId(chatId);
        photo.setPhoto(service.addPicture(pictureUrl));
        photo.setCaption("Willingness of real estate");
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> buttons = new ArrayList<>();
        List<InlineKeyboardButton> button1 = new ArrayList<>();
        List<InlineKeyboardButton> button2 = new ArrayList<>();
        List<InlineKeyboardButton> button3 = new ArrayList<>();
        InlineKeyboardButton selectRealEstateWayToPay1 = new InlineKeyboardButton();
        InlineKeyboardButton selectRealEstateWayToPay2 = new InlineKeyboardButton();
        InlineKeyboardButton selectRealEstateWayToPay3 = new InlineKeyboardButton();
        selectRealEstateWayToPay1.setText(COMPLETED);
        selectRealEstateWayToPay1.setCallbackData(COMPLETED);
        selectRealEstateWayToPay2.setText(OFF_PLAN);
        selectRealEstateWayToPay2.setCallbackData(OFF_PLAN);
        selectRealEstateWayToPay3.setText(NO_MATTER);
        selectRealEstateWayToPay3.setCallbackData(NO_MATTER);
        button1.add(selectRealEstateWayToPay1);
        button2.add(selectRealEstateWayToPay2);
        button3.add(selectRealEstateWayToPay3);
        buttons.add(button1);
        buttons.add(button2);
        buttons.add(button3);
        inlineKeyboardMarkup.setKeyboard(buttons);
        photo.setReplyMarkup(inlineKeyboardMarkup);
        try {
            execute(photo);
        } catch (TelegramApiException e) {
            log.error("Error occurred {}", e.getMessage());
        }
    }

    private void sendRealEstateCommand(Long chatId, String pictureUrl) {
        SendPhoto photo = new SendPhoto();
        photo.setCaption("How can I help you?");
        photo.setChatId(chatId);
        photo.setPhoto(service.addPicture(pictureUrl));
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> buttons = new ArrayList<>();
        List<InlineKeyboardButton> button1 = new ArrayList<>();
        List<InlineKeyboardButton> button2 = new ArrayList<>();
        InlineKeyboardButton selectRealEstateButton1 = new InlineKeyboardButton();
        InlineKeyboardButton selectRealEstateButton2 = new InlineKeyboardButton();
        selectRealEstateButton1.setText(CHOOSE_REAL_ESTATE);
        selectRealEstateButton1.setCallbackData(CHOOSE_REAL_ESTATE);
        selectRealEstateButton2.setText(CONTACT_FOR_MANAGER);
        selectRealEstateButton2.setCallbackData(CONTACT_FOR_MANAGER);
        selectRealEstateButton2.setUrl(ADMIN_TG_LINK);
        button1.add(selectRealEstateButton1);
        button2.add(selectRealEstateButton2);
        buttons.add(button1);
        buttons.add(button2);
        inlineKeyboardMarkup.setKeyboard(buttons);
        photo.setReplyMarkup(inlineKeyboardMarkup);
        try {
            execute(photo);
        } catch (TelegramApiException e) {
            log.error("Error occurred {}", e.getMessage());
        }
    }

    private void sendRealEstateTypeCommand(Long chatId, String pictureUrl) {
        SendPhoto photo = new SendPhoto();
        photo.setChatId(chatId);
        photo.setPhoto(service.addPicture(pictureUrl));
        photo.setCaption("What type of real estate are you interested in ?");
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> buttons = new ArrayList<>();
        List<InlineKeyboardButton> button1 = new ArrayList<>();
        List<InlineKeyboardButton> button2 = new ArrayList<>();
        List<InlineKeyboardButton> button3 = new ArrayList<>();
        List<InlineKeyboardButton> button4 = new ArrayList<>();
        List<InlineKeyboardButton> button5 = new ArrayList<>();
        List<InlineKeyboardButton> button6 = new ArrayList<>();
        InlineKeyboardButton realEstateButton1 = new InlineKeyboardButton();
        InlineKeyboardButton realEstateButton2 = new InlineKeyboardButton();
        InlineKeyboardButton realEstateButton3 = new InlineKeyboardButton();
        InlineKeyboardButton realEstateButton4 = new InlineKeyboardButton();
        InlineKeyboardButton realEstateButton5 = new InlineKeyboardButton();
        InlineKeyboardButton realEstateButton6 = new InlineKeyboardButton();
        realEstateButton1.setText(APARTMENTS);
        realEstateButton2.setText(PENTHOUSES);
        realEstateButton3.setText(TOWNHOUSES);
        realEstateButton4.setText(VILLAS);
        realEstateButton5.setText(DUPLEX);
        realEstateButton6.setText(SIMPLEX);
        realEstateButton1.setCallbackData(APARTMENTS);
        realEstateButton2.setCallbackData(PENTHOUSES);
        realEstateButton3.setCallbackData(TOWNHOUSES);
        realEstateButton4.setCallbackData(VILLAS);
        realEstateButton5.setCallbackData(DUPLEX);
        realEstateButton6.setCallbackData(SIMPLEX);
        button1.add(realEstateButton1);
        button2.add(realEstateButton2);
        button3.add(realEstateButton3);
        button4.add(realEstateButton4);
        button5.add(realEstateButton5);
        button6.add(realEstateButton6);
        buttons.add(button1);
        buttons.add(button2);
        buttons.add(button3);
        buttons.add(button4);
        buttons.add(button5);
        buttons.add(button6);
        inlineKeyboardMarkup.setKeyboard(buttons);
        photo.setReplyMarkup(inlineKeyboardMarkup);
        try {
            execute(photo);
        } catch (TelegramApiException e) {
            log.error("Error occurred {}", e.getMessage());
        }
    }

    private void sendRealEstatePriceCommand(Long chatId, String pictureUrl) {
        SendPhoto photo = new SendPhoto();
        photo.setChatId(chatId);
        photo.setPhoto(service.addPicture(pictureUrl));
        photo.setCaption("How much are you ready to pay for it?");
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
        priceButton1.setText(TWO_FOUR_MILLIONS);
        priceButton2.setText(FOUR_SIX_MILLIONS);
        priceButton3.setText(SIX_EIGHT_MILLIONS);
        priceButton4.setText(EIGHT_TEN_MILLIONS);
        priceButton5.setText(TEN_PLUS_MILLIONS);
        priceButton1.setCallbackData(TWO_FOUR_MILLIONS);
        priceButton2.setCallbackData(FOUR_SIX_MILLIONS);
        priceButton3.setCallbackData(SIX_EIGHT_MILLIONS);
        priceButton4.setCallbackData(EIGHT_TEN_MILLIONS);
        priceButton5.setCallbackData(TEN_PLUS_MILLIONS);
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
        photo.setReplyMarkup(inlineKeyboardMarkup);
        try {
            execute(photo);
        } catch (TelegramApiException e) {
            log.error("Error occurred {}", e.getMessage());
        }
    }

    private void sendPhoto(Long chatId, String photoPath) {
        SendPhoto sendPhoto = new SendPhoto();
        sendPhoto.setChatId(chatId);
        sendPhoto.setPhoto(service.addPicture(photoPath));
        try {
            execute(sendPhoto);
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
        commands.add(new BotCommand("/start", "start bot"));
        commands.add(new BotCommand("/end", "end bot"));
        commands.add(new BotCommand("/help", "administrator"));
        try {
            this.execute(new SetMyCommands(commands, new BotCommandScopeDefault(), null));
        } catch (TelegramApiException e) {
            log.error("Error occurred {}", e.getMessage());
        }
        return commands;
    }

    private void registerUser(Message msg) {
        if (eqtUsersChoicesRepo.findById(msg.getChatId()).isEmpty()) {
            long chatId = msg.getChatId();
            Chat chat = msg.getChat();
            EqtUsersChoices user = new EqtUsersChoices();
            user.setId(chatId);
            user.setUsername(chat.getUserName());
            user.setRegisteredAt(TimeUtil.userRegisterDate());
            eqtUsersChoicesRepo.save(user);
        }
    }

}
