package com.realestate;

import com.realestate.config.BotConfig;
import com.realestate.entity.EqtRealEstates;
import com.realestate.entity.EqtUsersChoices;
import com.realestate.entity.EqtUsersErrors;
import com.realestate.repo.EqtRealEstatesRepo;
import com.realestate.repo.EqtUsersChoicesRepo;
import com.realestate.repo.EqtUsersErrorsRepo;
import com.realestate.service.realestate.RealEstateService;
import com.realestate.utils.MessagesUtil;
import com.realestate.utils.PriceUtil;
import com.realestate.utils.TimeUtil;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMediaGroup;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.media.InputMedia;
import org.telegram.telegrambots.meta.api.objects.media.InputMediaPhoto;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.ArrayList;
import java.util.List;

import static com.realestate.utils.Constants.*;
import static com.realestate.utils.Constants.Prices.Apartments.*;
import static com.realestate.utils.Constants.Prices.Duplex.*;
import static com.realestate.utils.Constants.Prices.Penthouses.*;
import static com.realestate.utils.Constants.Prices.Villas.*;

@Component
@Slf4j
public class TelegramBot extends TelegramLongPollingBot {

    private final BotConfig botConfig;

    private final RealEstateService realEstateService;

    @Autowired
    private EqtUsersChoicesRepo eqtUsersChoicesRepo;

    @Autowired
    private EqtRealEstatesRepo eqtRealEstatesRepo;

    @Autowired
    private EqtUsersErrorsRepo eqtUsersErrorsRepo;

    public TelegramBot(BotConfig botConfig) {
        this.botConfig = botConfig;
        this.realEstateService = new RealEstateService();
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
                    sendStartCommand(chatId, update.getMessage().getChat().getFirstName());
                    registerUser(update.getMessage());
                    sendLanguageChoice(chatId);
                }
                case "/help" -> sendMessage(chatId, ADMIN_TG_LINK);
                case "/site" -> sendMessage(chatId, COMPANY_SITE);
                case "/language" -> sendLanguageChoice(chatId);
                case "/support" -> sendMessage(chatId, SUPPORT_TG_LINK);
                default -> {
                    EqtUsersChoices eqtUsersChoice;
                    eqtUsersChoice = eqtUsersChoicesRepo.findById(chatId).orElseGet(() -> {
                        EqtUsersChoices newUserChoice = new EqtUsersChoices();
                        newUserChoice.setId(chatId);
                        return newUserChoice;
                    });
                    if (eqtUsersChoice.getLanguage().equals(RUSSIAN)) {
                        sendMessage(chatId, Russian.NO_COMMAND);
                    } else {
                        sendMessage(chatId, English.NO_COMMAND);
                    }
                }
            }
        } else if (update.hasCallbackQuery()) {
            CallbackQuery query = update.getCallbackQuery();
            long chatId = query.getMessage().getChatId();
            String callBackData = query.getData();
            EqtUsersChoices eqtUsersChoice;
            eqtUsersChoice = eqtUsersChoicesRepo.findById(chatId).orElseGet(() -> {
                EqtUsersChoices newUserChoice = new EqtUsersChoices();
                newUserChoice.setId(chatId);
                return newUserChoice;
            });
            switch (callBackData) {
                case RUSSIAN -> {
                    eqtUsersChoice.setLanguage(RUSSIAN);
                    sendRussianRealEstateCommand(chatId);
                    eqtUsersChoicesRepo.save(eqtUsersChoice);
                }
                case ENGLISH -> {
                    eqtUsersChoice.setLanguage(ENGLISH);
                    sendEnglishRealEstateCommand(chatId);
                    eqtUsersChoicesRepo.save(eqtUsersChoice);
                }
            }
            switch (callBackData) {
                case Russian.CHOOSE_REAL_ESTATE -> sendRussianRealEstateTypeCommand(chatId);
                case Russian.CONTACT_FOR_MANAGER -> sendMessage(chatId, ADMIN_TG_LINK);
                case Russian.VISIT_COMPANY_WEBSITE -> sendMessage(chatId, Russian.VISIT_COMPANY_WEBSITE);
            }
            switch (callBackData) {
                case English.CHOOSE_REAL_ESTATE -> sendEnglishRealEstateTypeCommand(chatId);
                case English.CONTACT_FOR_MANAGER -> sendMessage(chatId, ADMIN_TG_LINK);
                case English.VISIT_COMPANY_WEBSITE -> sendMessage(chatId, English.VISIT_COMPANY_WEBSITE);
            }
            switch (callBackData) {
                case Russian.APARTMENTS -> {
                    sendRussianRealEstatePriceCommand(chatId, TWO_FOUR_MILLIONS,
                            FOUR_SIX_MILLIONS, SIX_EIGHT_MILLIONS,
                            TEN_THIRTY_MILLIONS);
                    eqtUsersChoice.setType(callBackData);
                    eqtUsersChoicesRepo.save(eqtUsersChoice);
                }
            }
            switch (callBackData) {
                case English.APARTMENTS -> {
                    sendEnglishRealEstatePriceCommand(chatId, TWO_FOUR_MILLIONS,
                            FOUR_SIX_MILLIONS, SIX_EIGHT_MILLIONS,
                            TEN_THIRTY_MILLIONS);
                    eqtUsersChoice.setType(callBackData);
                    eqtUsersChoicesRepo.save(eqtUsersChoice);
                }
            }
            switch (callBackData) {
                case Russian.PENTHOUSES -> {
                    sendRussianRealEstatePriceCommand(chatId, FIFTEEN_TWENTY_FIVE_MILLIONS,
                            TWENTY_FIVE_THIRTY_MILLIONS, THIRTY_FORTY_MILLIONS,
                            FORTY_FIFTY_MILLIONS);
                    eqtUsersChoice.setType(callBackData);
                    eqtUsersChoicesRepo.save(eqtUsersChoice);
                }
            }
            switch (callBackData) {
                case English.PENTHOUSES -> {
                    sendEnglishRealEstatePriceCommand(chatId, FIFTEEN_TWENTY_FIVE_MILLIONS,
                            TWENTY_FIVE_THIRTY_MILLIONS, THIRTY_FORTY_MILLIONS,
                            FORTY_FIFTY_MILLIONS);
                    eqtUsersChoice.setType(callBackData);
                    eqtUsersChoicesRepo.save(eqtUsersChoice);
                }
            }
            switch (callBackData) {
                case Russian.DUPLEX -> {
                    sendRussianRealEstatePriceCommand(chatId, FORTY_FORTY_FIVE_MILLIONS,
                            FORTY_FIVE_FORTY_SEVEN_MILLIONS, FORTY_SEVEN_SEVENTY_MILLIONS,
                            SEVENTY_ONE_HUNDRED_TEN_MILLIONS);
                    eqtUsersChoice.setType(callBackData);
                    eqtUsersChoicesRepo.save(eqtUsersChoice);
                }
            }
            switch (callBackData) {
                case English.DUPLEX -> {
                    sendEnglishRealEstatePriceCommand(chatId, FORTY_FORTY_FIVE_MILLIONS,
                            FORTY_FIVE_FORTY_SEVEN_MILLIONS, FORTY_SEVEN_SEVENTY_MILLIONS,
                            SEVENTY_ONE_HUNDRED_TEN_MILLIONS);
                    eqtUsersChoice.setType(callBackData);
                    eqtUsersChoicesRepo.save(eqtUsersChoice);
                }
            }
            switch (callBackData) {
                case Russian.VILLAS -> {
                    sendRussianRealEstatePriceCommand(chatId, EIGHT_TEN_MILLIONS,
                            TEN_THIRTEEN_MILLIONS, THIRTEEN_FIFTEEN_MILLIONS,
                            FIFTEEN_ONE_HUNDRED_FORTY_MILLIONS);
                    eqtUsersChoice.setType(callBackData);
                    eqtUsersChoicesRepo.save(eqtUsersChoice);
                }
            }
            switch (callBackData) {
                case English.VILLAS -> {
                    sendEnglishRealEstatePriceCommand(chatId, EIGHT_TEN_MILLIONS,
                            TEN_THIRTEEN_MILLIONS, THIRTEEN_FIFTEEN_MILLIONS,
                            FIFTEEN_ONE_HUNDRED_FORTY_MILLIONS);
                    eqtUsersChoice.setType(callBackData);
                    eqtUsersChoicesRepo.save(eqtUsersChoice);
                }
            }
            switch (callBackData) {
                case TWO_FOUR_MILLIONS, FOUR_SIX_MILLIONS, SIX_EIGHT_MILLIONS, TEN_THIRTY_MILLIONS,
                        FIFTEEN_TWENTY_FIVE_MILLIONS, TWENTY_FIVE_THIRTY_MILLIONS,
                        THIRTY_FORTY_MILLIONS, FORTY_FIFTY_MILLIONS, FORTY_FORTY_FIVE_MILLIONS,
                        FORTY_FIVE_FORTY_SEVEN_MILLIONS, FORTY_SEVEN_SEVENTY_MILLIONS,
                        SEVENTY_ONE_HUNDRED_TEN_MILLIONS, EIGHT_TEN_MILLIONS,
                        TEN_THIRTEEN_MILLIONS, THIRTEEN_FIFTEEN_MILLIONS, FIFTEEN_ONE_HUNDRED_FORTY_MILLIONS -> {
                    if (eqtUsersChoice.getLanguage().equals(RUSSIAN)) {
                        sendRussianRealEstateWillingnessCommand(chatId);
                    } else {
                        sendEnglishRealEstateWillingnessCommand(chatId);
                    }
                    eqtUsersChoice.setPrice(callBackData);
                    eqtUsersChoicesRepo.save(eqtUsersChoice);
                }
            }
            switch (callBackData) {
                case Russian.SECONDARY, Russian.OFF_PLAN -> {
                    eqtUsersChoice.setWillingness(callBackData);
                    eqtUsersChoicesRepo.save(eqtUsersChoice);
                    sendMessage(chatId, Russian.PRE_RESULT_MESSAGE);
                    List<String> prices = PriceUtil.getPriceRange(eqtUsersChoice.getPrice());
                    List<Long> realEstatesIds = eqtRealEstatesRepo.findRealEstatesIds(
                            eqtUsersChoice.getType(),
                            eqtUsersChoice.getWillingness(), prices.get(0), prices.get(1));
                    eqtUsersChoice.setObjectsFound(String.valueOf(realEstatesIds.size() - 1));
                    eqtUsersChoicesRepo.save(eqtUsersChoice);
                    int objectsFound = Integer.parseInt(eqtUsersChoice.getObjectsFound());
                    if (realEstatesIds.size() > 0) {
                        sendRussianRealEstatesByOne(chatId, realEstatesIds, objectsFound);
                        sendRussianRequestResultRealEstatesCommand(chatId);
                    } else {
                        sendMessage(chatId, Russian.OBJECTS_NOT_FOUND);
                    }
                    eqtUsersChoice.setObjectsFound(String.valueOf(objectsFound - 1));
                    eqtUsersChoicesRepo.save(eqtUsersChoice);
                }
            }
            switch (callBackData) {
                case English.SECONDARY, English.OFF_PLAN -> {
                    eqtUsersChoice.setWillingness(callBackData);
                    eqtUsersChoicesRepo.save(eqtUsersChoice);
                    sendMessage(chatId, English.PRE_RESULT_MESSAGE);
                    List<String> prices = PriceUtil.getPriceRange(eqtUsersChoice.getPrice());
                    List<Long> realEstatesIds = eqtRealEstatesRepo.findRealEstatesIds(
                            eqtUsersChoice.getType(),
                            eqtUsersChoice.getWillingness(), prices.get(0), prices.get(1));
                    eqtUsersChoice.setObjectsFound(String.valueOf(realEstatesIds.size() - 1));
                    eqtUsersChoicesRepo.save(eqtUsersChoice);
                    int objectsFound = Integer.parseInt(eqtUsersChoice.getObjectsFound());
                    if (realEstatesIds.size() > 0) {
                        sendEnglishRealEstatesByOne(chatId, realEstatesIds, objectsFound);
                        sendEnglishRequestResultRealEstatesCommand(chatId);
                    } else {
                        sendMessage(chatId, English.OBJECTS_NOT_FOUND);
                    }
                    eqtUsersChoice.setObjectsFound(String.valueOf(objectsFound - 1));
                    eqtUsersChoicesRepo.save(eqtUsersChoice);
                }
            }
            switch (callBackData) {
                case Russian.NEXT_OBJECT -> {
                    List<String> prices = PriceUtil.getPriceRange(eqtUsersChoice.getPrice());
                    List<Long> realEstatesIds = eqtRealEstatesRepo.findRealEstatesIds(
                            eqtUsersChoice.getType(),
                            eqtUsersChoice.getWillingness(), prices.get(0), prices.get(1));
                    int objectsFound = Integer.parseInt(eqtUsersChoice.getObjectsFound());
                    if (objectsFound >= 0) {
                        sendRussianRealEstatesByOne(chatId, realEstatesIds, objectsFound);
                        sendRussianRequestResultRealEstatesCommand(chatId);
                        eqtUsersChoice.setObjectsFound(String.valueOf(objectsFound - 1));
                        eqtUsersChoicesRepo.save(eqtUsersChoice);
                    } else {
                        sendMessage(chatId, Russian.POST_RESULT_MESSAGE);
                    }

                }
            }
            switch (callBackData) {
                case English.NEXT_OBJECT -> {
                    List<String> prices = PriceUtil.getPriceRange(eqtUsersChoice.getPrice());
                    List<Long> realEstatesIds = eqtRealEstatesRepo.findRealEstatesIds(
                            eqtUsersChoice.getType(),
                            eqtUsersChoice.getWillingness(), prices.get(0), prices.get(1));
                    int objectsFound = Integer.parseInt(eqtUsersChoice.getObjectsFound());
                    if (objectsFound >= 0) {
                        sendEnglishRealEstatesByOne(chatId, realEstatesIds, objectsFound);
                        sendEnglishRequestResultRealEstatesCommand(chatId);
                        eqtUsersChoice.setObjectsFound(String.valueOf(objectsFound - 1));
                        eqtUsersChoicesRepo.save(eqtUsersChoice);
                    } else {
                        sendMessage(chatId, English.POST_RESULT_MESSAGE);
                    }

                }
            }
        }
    }

    private void sendEnglishRequestResultRealEstatesCommand(Long chatId) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chatId);
        sendMessage.setText(English.FOUNDED_OBJECTS_ACCORDING_TO_THE_REQUEST);
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> buttons = new ArrayList<>();
        List<InlineKeyboardButton> buttons1 = new ArrayList<>();
        List<InlineKeyboardButton> buttons2 = new ArrayList<>();
        List<InlineKeyboardButton> buttons3 = new ArrayList<>();
        InlineKeyboardButton button1 = new InlineKeyboardButton();
        InlineKeyboardButton button2 = new InlineKeyboardButton();
        InlineKeyboardButton button3 = new InlineKeyboardButton();
        button1.setText(English.NEXT_OBJECT);
        button1.setCallbackData(English.NEXT_OBJECT);
        button2.setText(English.CONTACT_FOR_MANAGER);
        button2.setCallbackData(English.CONTACT_FOR_MANAGER);
        button2.setUrl(ADMIN_TG_LINK);
        button3.setText(English.VISIT_COMPANY_WEBSITE);
        button3.setCallbackData(English.VISIT_COMPANY_WEBSITE);
        button3.setUrl(COMPANY_SITE);
        buttons1.add(button1);
        buttons2.add(button2);
        buttons3.add(button3);
        buttons.add(buttons1);
        buttons.add(buttons2);
        buttons.add(buttons3);
        inlineKeyboardMarkup.setKeyboard(buttons);
        sendMessage.setReplyMarkup(inlineKeyboardMarkup);
        try {
            execute(sendMessage);
        } catch (TelegramApiException e) {
            registerException(chatId, e.getMessage());
        }
    }

    private void sendRussianRequestResultRealEstatesCommand(Long chatId) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chatId);
        sendMessage.setText(Russian.FOUNDED_OBJECTS_ACCORDING_TO_THE_REQUEST);
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> buttons = new ArrayList<>();
        List<InlineKeyboardButton> buttons1 = new ArrayList<>();
        List<InlineKeyboardButton> buttons2 = new ArrayList<>();
        List<InlineKeyboardButton> buttons3 = new ArrayList<>();
        InlineKeyboardButton button1 = new InlineKeyboardButton();
        InlineKeyboardButton button2 = new InlineKeyboardButton();
        InlineKeyboardButton button3 = new InlineKeyboardButton();
        button1.setText(Russian.NEXT_OBJECT);
        button1.setCallbackData(Russian.NEXT_OBJECT);
        button2.setText(Russian.CONTACT_FOR_MANAGER);
        button2.setCallbackData(Russian.CONTACT_FOR_MANAGER);
        button2.setUrl(ADMIN_TG_LINK);
        button3.setText(Russian.VISIT_COMPANY_WEBSITE);
        button3.setCallbackData(Russian.VISIT_COMPANY_WEBSITE);
        button3.setUrl(COMPANY_SITE);
        buttons1.add(button1);
        buttons2.add(button2);
        buttons3.add(button3);
        buttons.add(buttons1);
        buttons.add(buttons2);
        buttons.add(buttons3);
        inlineKeyboardMarkup.setKeyboard(buttons);
        sendMessage.setReplyMarkup(inlineKeyboardMarkup);
        try {
            execute(sendMessage);
        } catch (TelegramApiException e) {
            registerException(chatId, e.getMessage());
        }
    }

    private void sendEnglishRealEstatesByOne(Long chatId, List<Long> realEstateIds, int index) {
        if (eqtRealEstatesRepo.findById(realEstateIds.get(index)).isPresent()) {
            EqtRealEstates realEstate = eqtRealEstatesRepo.findById(realEstateIds.get(index)).get();
            String neighbourhood = realEstate.getNeighbourhood();
            String description = realEstate.getDescription();
            String size = realEstate.getSize();
            String price = realEstate.getPrice().toString();
            String picture1 = realEstate.getPicture1();
            String picture2 = realEstate.getPicture2();
            String picture3 = realEstate.getPicture3();
            sendEnglishRealEstateObject(picture1, picture2, picture3,
                    neighbourhood, description, size, price, chatId);
        }
    }

    private void sendRussianRealEstatesByOne(Long chatId, List<Long> realEstateIds, int index) {
        if (eqtRealEstatesRepo.findById(realEstateIds.get(index)).isPresent()) {
            EqtRealEstates realEstate = eqtRealEstatesRepo.findById(realEstateIds.get(index)).get();
            String neighbourhood = realEstate.getNeighbourhood();
            String description = realEstate.getDescription();
            String size = realEstate.getSize();
            String price = realEstate.getPrice().toString();
            String picture1 = realEstate.getPicture1();
            String picture2 = realEstate.getPicture2();
            String picture3 = realEstate.getPicture3();
            sendRussianRealEstateObject(picture1, picture2, picture3,
                    neighbourhood, description, size, price, chatId);
        }
    }

    private void sendEnglishRealEstateObject(String picture1, String picture2, String picture3,
                                      String neighbourhood, String description, String size,
                                      String price, Long chatId) {
        List<InputMedia> photos = new ArrayList<>();
        InputMediaPhoto photo1 = new InputMediaPhoto();
        photo1.setMedia(realEstateService.getPictureFromResources(picture1).getNewMediaStream(), picture1);
        InputMediaPhoto photo2 = new InputMediaPhoto();
        photo2.setMedia(realEstateService.getPictureFromResources(picture2).getNewMediaStream(), picture2);
        InputMediaPhoto photo3 = new InputMediaPhoto();
        photo3.setMedia(realEstateService.getPictureFromResources(picture3).getNewMediaStream(), picture3);
        photos.add(photo1);
        photos.add(photo2);
        photos.add(photo3);

        SendMediaGroup sendMediaGroup = new SendMediaGroup();
        sendMediaGroup.setChatId(chatId);
        sendMediaGroup.setMedias(photos);

        try {
            execute(sendMediaGroup);
        } catch (TelegramApiException e) {
            registerException(chatId, e.getMessage());
        }
        sendMessage(chatId, MessagesUtil.resultEnglishMessageBuilder(description, neighbourhood, size, price));
    }

    private void sendRussianRealEstateObject(String picture1, String picture2, String picture3,
                                             String neighbourhood, String description, String size,
                                             String price, Long chatId) {
        List<InputMedia> photos = new ArrayList<>();
        InputMediaPhoto photo1 = new InputMediaPhoto();
        photo1.setMedia(realEstateService.getPictureFromResources(picture1).getNewMediaStream(), picture1);
        InputMediaPhoto photo2 = new InputMediaPhoto();
        photo2.setMedia(realEstateService.getPictureFromResources(picture2).getNewMediaStream(), picture2);
        InputMediaPhoto photo3 = new InputMediaPhoto();
        photo3.setMedia(realEstateService.getPictureFromResources(picture3).getNewMediaStream(), picture3);
        photos.add(photo1);
        photos.add(photo2);
        photos.add(photo3);

        SendMediaGroup sendMediaGroup = new SendMediaGroup();
        sendMediaGroup.setChatId(chatId);
        sendMediaGroup.setMedias(photos);

        try {
            execute(sendMediaGroup);
        } catch (TelegramApiException e) {
            registerException(chatId, e.getMessage());
        }
        sendMessage(chatId, MessagesUtil.resultRussianMessageBuilder(description, neighbourhood, size, price));
    }


    private void sendStartCommand(Long chatId, String username) {
        String answer = String.format(GREETING_COMMAND_TEMPLATE, username, username);
        sendMessage(chatId, answer);
    }

    private void sendRussianRealEstateWillingnessCommand(Long chatId) {
        SendPhoto photo = new SendPhoto();
        photo.setChatId(chatId);
        photo.setPhoto(realEstateService.getPictureFromResources("type_of_willingness.jpeg"));
        photo.setCaption(Russian.WILLINGNESS_QUERY);
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> buttons = new ArrayList<>();
        List<InlineKeyboardButton> button1 = new ArrayList<>();
        List<InlineKeyboardButton> button2 = new ArrayList<>();
        InlineKeyboardButton selectRealEstateWayToPay1 = new InlineKeyboardButton();
        InlineKeyboardButton selectRealEstateWayToPay2 = new InlineKeyboardButton();
        selectRealEstateWayToPay1.setText(Russian.SECONDARY);
        selectRealEstateWayToPay1.setCallbackData(Russian.SECONDARY);
        selectRealEstateWayToPay2.setText(Russian.OFF_PLAN);
        selectRealEstateWayToPay2.setCallbackData(Russian.OFF_PLAN);
        button1.add(selectRealEstateWayToPay1);
        button2.add(selectRealEstateWayToPay2);
        buttons.add(button1);
        buttons.add(button2);
        inlineKeyboardMarkup.setKeyboard(buttons);
        photo.setReplyMarkup(inlineKeyboardMarkup);
        try {
            execute(photo);
        } catch (TelegramApiException e) {
            registerException(chatId, e.getMessage());
        }
    }

    private void sendEnglishRealEstateWillingnessCommand(Long chatId) {
        SendPhoto photo = new SendPhoto();
        photo.setChatId(chatId);
        photo.setPhoto(realEstateService.getPictureFromResources("type_of_willingness.jpeg"));
        photo.setCaption(English.WILLINGNESS_QUERY);
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> buttons = new ArrayList<>();
        List<InlineKeyboardButton> button1 = new ArrayList<>();
        List<InlineKeyboardButton> button2 = new ArrayList<>();
        InlineKeyboardButton selectRealEstateWayToPay1 = new InlineKeyboardButton();
        InlineKeyboardButton selectRealEstateWayToPay2 = new InlineKeyboardButton();
        selectRealEstateWayToPay1.setText(English.SECONDARY);
        selectRealEstateWayToPay1.setCallbackData(English.SECONDARY);
        selectRealEstateWayToPay2.setText(English.OFF_PLAN);
        selectRealEstateWayToPay2.setCallbackData(English.OFF_PLAN);
        button1.add(selectRealEstateWayToPay1);
        button2.add(selectRealEstateWayToPay2);
        buttons.add(button1);
        buttons.add(button2);
        inlineKeyboardMarkup.setKeyboard(buttons);
        photo.setReplyMarkup(inlineKeyboardMarkup);
        try {
            execute(photo);
        } catch (TelegramApiException e) {
            registerException(chatId, e.getMessage());
        }
    }

    private void sendRussianRealEstateCommand(Long chatId) {
        SendPhoto photo = new SendPhoto();
        photo.setCaption(Russian.START_QUERY);
        photo.setChatId(chatId);
        photo.setPhoto(realEstateService.getPictureFromResources("greeting.jpeg"));
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> buttons = new ArrayList<>();
        List<InlineKeyboardButton> buttons1 = new ArrayList<>();
        List<InlineKeyboardButton> buttons2 = new ArrayList<>();
        List<InlineKeyboardButton> buttons3 = new ArrayList<>();
        InlineKeyboardButton button1 = new InlineKeyboardButton();
        InlineKeyboardButton button2 = new InlineKeyboardButton();
        InlineKeyboardButton button3 = new InlineKeyboardButton();
        button1.setText(Russian.CHOOSE_REAL_ESTATE);
        button1.setCallbackData(Russian.CHOOSE_REAL_ESTATE);
        button2.setText(Russian.CONTACT_FOR_MANAGER);
        button2.setCallbackData(Russian.CONTACT_FOR_MANAGER);
        button2.setUrl(ADMIN_TG_LINK);
        button3.setText(Russian.VISIT_COMPANY_WEBSITE);
        button3.setCallbackData(Russian.VISIT_COMPANY_WEBSITE);
        button3.setUrl(COMPANY_SITE);
        buttons1.add(button1);
        buttons2.add(button2);
        buttons3.add(button3);
        buttons.add(buttons1);
        buttons.add(buttons2);
        buttons.add(buttons3);
        inlineKeyboardMarkup.setKeyboard(buttons);
        photo.setReplyMarkup(inlineKeyboardMarkup);
        try {
            execute(photo);
        } catch (TelegramApiException e) {
            registerException(chatId, e.getMessage());
        }
    }

    private void sendEnglishRealEstateCommand(Long chatId) {
        SendPhoto photo = new SendPhoto();
        photo.setCaption(English.START_QUERY);
        photo.setChatId(chatId);
        photo.setPhoto(realEstateService.getPictureFromResources("greeting.jpeg"));
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> buttons = new ArrayList<>();
        List<InlineKeyboardButton> buttons1 = new ArrayList<>();
        List<InlineKeyboardButton> buttons2 = new ArrayList<>();
        List<InlineKeyboardButton> buttons3 = new ArrayList<>();
        InlineKeyboardButton button1 = new InlineKeyboardButton();
        InlineKeyboardButton button2 = new InlineKeyboardButton();
        InlineKeyboardButton button3 = new InlineKeyboardButton();
        button1.setText(English.CHOOSE_REAL_ESTATE);
        button1.setCallbackData(English.CHOOSE_REAL_ESTATE);
        button2.setText(English.CONTACT_FOR_MANAGER);
        button2.setCallbackData(English.CONTACT_FOR_MANAGER);
        button2.setUrl(ADMIN_TG_LINK);
        button3.setText(English.VISIT_COMPANY_WEBSITE);
        button3.setCallbackData(English.VISIT_COMPANY_WEBSITE);
        button3.setUrl(COMPANY_SITE);
        buttons1.add(button1);
        buttons2.add(button2);
        buttons3.add(button3);
        buttons.add(buttons1);
        buttons.add(buttons2);
        buttons.add(buttons3);
        inlineKeyboardMarkup.setKeyboard(buttons);
        photo.setReplyMarkup(inlineKeyboardMarkup);
        try {
            execute(photo);
        } catch (TelegramApiException e) {
            registerException(chatId, e.getMessage());
        }
    }

    private void sendEnglishRealEstateTypeCommand(Long chatId) {
        SendPhoto photo = new SendPhoto();
        photo.setChatId(chatId);
        photo.setPhoto(realEstateService.getPictureFromResources("type_of_object.jpeg"));
        photo.setCaption(English.REAL_ESTATE_TYPE_QUERY);
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> buttons = new ArrayList<>();
        List<InlineKeyboardButton> buttons1 = new ArrayList<>();
        List<InlineKeyboardButton> buttons2 = new ArrayList<>();
        List<InlineKeyboardButton> buttons3 = new ArrayList<>();
        List<InlineKeyboardButton> buttons4 = new ArrayList<>();
        InlineKeyboardButton button1 = new InlineKeyboardButton();
        InlineKeyboardButton button2 = new InlineKeyboardButton();
        InlineKeyboardButton button3 = new InlineKeyboardButton();
        InlineKeyboardButton button4 = new InlineKeyboardButton();
        button1.setText(English.APARTMENTS);
        button2.setText(English.PENTHOUSES);
        button3.setText(English.VILLAS);
        button4.setText(English.DUPLEX);
        button1.setCallbackData(English.APARTMENTS);
        button2.setCallbackData(English.PENTHOUSES);
        button3.setCallbackData(English.VILLAS);
        button4.setCallbackData(English.DUPLEX);
        buttons1.add(button1);
        buttons2.add(button2);
        buttons3.add(button3);
        buttons4.add(button4);
        buttons.add(buttons1);
        buttons.add(buttons2);
        buttons.add(buttons3);
        buttons.add(buttons4);
        inlineKeyboardMarkup.setKeyboard(buttons);
        photo.setReplyMarkup(inlineKeyboardMarkup);
        try {
            execute(photo);
        } catch (TelegramApiException e) {
            registerException(chatId, e.getMessage());
        }
    }

    private void sendRussianRealEstateTypeCommand(Long chatId) {
        SendPhoto photo = new SendPhoto();
        photo.setChatId(chatId);
        photo.setPhoto(realEstateService.getPictureFromResources("type_of_object.jpeg"));
        photo.setCaption(Russian.REAL_ESTATE_TYPE_QUERY);
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> buttons = new ArrayList<>();
        List<InlineKeyboardButton> buttons1 = new ArrayList<>();
        List<InlineKeyboardButton> buttons2 = new ArrayList<>();
        List<InlineKeyboardButton> buttons3 = new ArrayList<>();
        List<InlineKeyboardButton> buttons4 = new ArrayList<>();
        InlineKeyboardButton button1 = new InlineKeyboardButton();
        InlineKeyboardButton button2 = new InlineKeyboardButton();
        InlineKeyboardButton button3 = new InlineKeyboardButton();
        InlineKeyboardButton button4 = new InlineKeyboardButton();
        button1.setText(Russian.APARTMENTS);
        button2.setText(Russian.PENTHOUSES);
        button3.setText(Russian.VILLAS);
        button4.setText(Russian.DUPLEX);
        button1.setCallbackData(Russian.APARTMENTS);
        button2.setCallbackData(Russian.PENTHOUSES);
        button3.setCallbackData(Russian.VILLAS);
        button4.setCallbackData(Russian.DUPLEX);
        buttons1.add(button1);
        buttons2.add(button2);
        buttons3.add(button3);
        buttons4.add(button4);
        buttons.add(buttons1);
        buttons.add(buttons2);
        buttons.add(buttons3);
        buttons.add(buttons4);
        inlineKeyboardMarkup.setKeyboard(buttons);
        photo.setReplyMarkup(inlineKeyboardMarkup);
        try {
            execute(photo);
        } catch (TelegramApiException e) {
            registerException(chatId, e.getMessage());
        }
    }

    private void sendRussianRealEstatePriceCommand(Long chatId, String price1,
                                                   String price2, String price3, String price4) {
        SendPhoto photo = new SendPhoto();
        photo.setChatId(chatId);
        photo.setPhoto(realEstateService.getPictureFromResources("price_range.jpeg"));
        photo.setCaption(Russian.BUDGET_QUERY);
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> buttons = new ArrayList<>();
        List<InlineKeyboardButton> buttons1 = new ArrayList<>();
        List<InlineKeyboardButton> buttons2 = new ArrayList<>();
        List<InlineKeyboardButton> buttons3 = new ArrayList<>();
        List<InlineKeyboardButton> buttons4 = new ArrayList<>();
        InlineKeyboardButton button1 = new InlineKeyboardButton();
        InlineKeyboardButton button2 = new InlineKeyboardButton();
        InlineKeyboardButton button3 = new InlineKeyboardButton();
        InlineKeyboardButton button4 = new InlineKeyboardButton();
        button1.setText(price1);
        button2.setText(price2);
        button3.setText(price3);
        button4.setText(price4);
        button1.setCallbackData(price1);
        button2.setCallbackData(price2);
        button3.setCallbackData(price3);
        button4.setCallbackData(price4);
        buttons1.add(button1);
        buttons2.add(button2);
        buttons3.add(button3);
        buttons4.add(button4);
        buttons.add(buttons1);
        buttons.add(buttons2);
        buttons.add(buttons3);
        buttons.add(buttons4);
        inlineKeyboardMarkup.setKeyboard(buttons);
        photo.setReplyMarkup(inlineKeyboardMarkup);
        try {
            execute(photo);
        } catch (TelegramApiException e) {
            registerException(chatId, e.getMessage());
        }
    }

    private void sendEnglishRealEstatePriceCommand(Long chatId, String price1,
                                                   String price2, String price3, String price4) {
        SendPhoto photo = new SendPhoto();
        photo.setChatId(chatId);
        photo.setPhoto(realEstateService.getPictureFromResources("price_range.jpeg"));
        photo.setCaption(English.BUDGET_QUERY);
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> buttons = new ArrayList<>();
        List<InlineKeyboardButton> buttons1 = new ArrayList<>();
        List<InlineKeyboardButton> buttons2 = new ArrayList<>();
        List<InlineKeyboardButton> buttons3 = new ArrayList<>();
        List<InlineKeyboardButton> buttons4 = new ArrayList<>();
        InlineKeyboardButton button1 = new InlineKeyboardButton();
        InlineKeyboardButton button2 = new InlineKeyboardButton();
        InlineKeyboardButton button3 = new InlineKeyboardButton();
        InlineKeyboardButton button4 = new InlineKeyboardButton();
        button1.setText(price1);
        button2.setText(price2);
        button3.setText(price3);
        button4.setText(price4);
        button1.setCallbackData(price1);
        button2.setCallbackData(price2);
        button3.setCallbackData(price3);
        button4.setCallbackData(price4);
        buttons1.add(button1);
        buttons2.add(button2);
        buttons3.add(button3);
        buttons4.add(button4);
        buttons.add(buttons1);
        buttons.add(buttons2);
        buttons.add(buttons3);
        buttons.add(buttons4);
        inlineKeyboardMarkup.setKeyboard(buttons);
        photo.setReplyMarkup(inlineKeyboardMarkup);
        try {
            execute(photo);
        } catch (TelegramApiException e) {
            registerException(chatId, e.getMessage());
        }
    }

    private void sendMessage(Long chatId, String textToSend) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chatId);
        sendMessage.setText(textToSend);
        try {
            execute(sendMessage);
        } catch (TelegramApiException e) {
            registerException(chatId, e.getMessage());
        }
    }

    private void registerUser(Message msg) {
        if (eqtUsersChoicesRepo.findById(msg.getChatId()).isEmpty()) {
            long chatId = msg.getChatId();
            Chat chat = msg.getChat();
            EqtUsersChoices user = new EqtUsersChoices();
            user.setId(chatId);
            user.setUsername(chat.getUserName() == null ? chat.getFirstName() : chat.getUserName());
            user.setLanguage(RUSSIAN);
            user.setRegisteredAt(TimeUtil.currentTime());
            user.setObjectsFound("0");
            eqtUsersChoicesRepo.save(user);
        }
    }

    private void sendLanguageChoice(Long chatId) {
        try {
            SendMessage sendMessage = new SendMessage(chatId.toString(), LANGUAGE_CONSTANT);
            InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
            List<List<InlineKeyboardButton>> buttons = new ArrayList<>();
            List<InlineKeyboardButton> languages = new ArrayList<>();
            InlineKeyboardButton russian = new InlineKeyboardButton();
            InlineKeyboardButton english = new InlineKeyboardButton();
            russian.setText(RUSSIAN);
            russian.setCallbackData(RUSSIAN);
            english.setText(ENGLISH);
            english.setCallbackData(ENGLISH);
            languages.add(russian);
            languages.add(english);
            buttons.add(languages);
            inlineKeyboardMarkup.setKeyboard(buttons);
            sendMessage.setReplyMarkup(inlineKeyboardMarkup);
            execute(sendMessage);
        } catch (TelegramApiException e) {
            registerException(chatId, e.getMessage());
        }
    }

    private void registerException(Long chatId, String content) {
        EqtUsersErrors errors = new EqtUsersErrors();
        errors.setId(chatId);
        errors.setTimeCreated(TimeUtil.currentTime());
        errors.setErrorContent(content);
        eqtUsersErrorsRepo.save(errors);
    }

}
