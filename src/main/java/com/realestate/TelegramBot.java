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
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import org.telegram.telegrambots.meta.api.methods.send.SendMediaGroup;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;
import org.telegram.telegrambots.meta.api.objects.commands.scope.BotCommandScopeDefault;
import org.telegram.telegrambots.meta.api.objects.media.InputMedia;
import org.telegram.telegrambots.meta.api.objects.media.InputMediaPhoto;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.ArrayList;
import java.util.List;

import static com.realestate.utils.Constants.*;
import static com.realestate.utils.Constants.Messages.*;
import static com.realestate.utils.Constants.Prices.Apartments.*;
import static com.realestate.utils.Constants.Prices.Duplex.*;
import static com.realestate.utils.Constants.Prices.Penthouses.*;
import static com.realestate.utils.Constants.Prices.Villas.*;

@Component
@Slf4j
public class TelegramBot extends TelegramLongPollingBot {

    private final BotConfig botConfig;
    private final List<BotCommand> commands;

    public static final String ADMIN_TG_LINK = "https://xn--80affa3aj0al.xn--80asehdb/#@psldvch";

    private final RealEstateService realEstateService;

    @Autowired
    private EqtUsersChoicesRepo eqtUsersChoicesRepo;

    @Autowired
    private EqtRealEstatesRepo eqtRealEstatesRepo;

    @Autowired
    private EqtUsersErrorsRepo eqtUsersErrorsRepo;

    public TelegramBot(BotConfig botConfig) {
        this.botConfig = botConfig;
        this.commands = initCommands();
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
                    sendRealEstateCommand(chatId);
                }
                case "/help" -> sendMessage(chatId, ADMIN_TG_LINK);
                case "/site" -> sendMessage(chatId, COMPANY_SITE);
                case "/end" -> sendEndCommand(chatId, update.getMessage().getChat().getFirstName());
                default -> sendMessage(chatId, "There isn't proceeding for such command. Ask for it manager");
            }
        } else if (update.hasCallbackQuery()) {
            CallbackQuery query = update.getCallbackQuery();
            long chatId = query.getMessage().getChatId();
            String callBackData = query.getData();
            EqtUsersChoices eqtUsersChoices;
            eqtUsersChoices = eqtUsersChoicesRepo.findById(chatId).orElseGet(() -> {
                EqtUsersChoices newUserChoice = new EqtUsersChoices();
                newUserChoice.setId(chatId);
                return newUserChoice;
            });
            switch (callBackData) {
                case CHOOSE_REAL_ESTATE -> sendRealEstateTypeCommand(chatId);
                case CONTACT_FOR_MANAGER -> sendMessage(chatId, ADMIN_TG_LINK);
                case VISIT_COMPANY_WEBSITE -> sendMessage(chatId, VISIT_COMPANY_WEBSITE);
            }
            switch (callBackData) {
                case APARTMENTS -> {
                    sendRealEstatePriceCommand(chatId, TWO_FOUR_MILLIONS,
                            FOUR_SIX_MILLIONS, SIX_EIGHT_MILLIONS,
                            TEN_THIRTY_MILLIONS);
                    eqtUsersChoices.setType(callBackData);
                    eqtUsersChoicesRepo.save(eqtUsersChoices);
                }
            }
            switch (callBackData) {
                case PENTHOUSES -> {
                    sendRealEstatePriceCommand(chatId, FIFTEEN_TWENTY_FIVE_MILLIONS,
                            TWENTY_FIVE_THIRTY_MILLIONS, THIRTY_FORTY_MILLIONS,
                            FORTY_FIFTY_MILLIONS);
                    eqtUsersChoices.setType(callBackData);
                    eqtUsersChoicesRepo.save(eqtUsersChoices);
                }
            }
            switch (callBackData) {
                case DUPLEX -> {
                    sendRealEstatePriceCommand(chatId, FORTY_FORTY_FIVE_MILLIONS,
                            FORTY_FIVE_FORTY_SEVEN_MILLIONS, FORTY_SEVEN_SEVENTY_MILLIONS,
                            SEVENTY_ONE_HUNDRED_TEN_MILLIONS);
                    eqtUsersChoices.setType(callBackData);
                    eqtUsersChoicesRepo.save(eqtUsersChoices);
                }
            }
            switch (callBackData) {
                case VILLAS -> {
                    sendRealEstatePriceCommand(chatId, EIGHT_TEN_MILLIONS,
                            TEN_THIRTEEN_MILLIONS, THIRTEEN_FIFTEEN_MILLIONS,
                            FIFTEEN_ONE_HUNDRED_FORTY_MILLIONS);
                    eqtUsersChoices.setType(callBackData);
                    eqtUsersChoicesRepo.save(eqtUsersChoices);
                }
            }
            switch (callBackData) {
                case TWO_FOUR_MILLIONS, FOUR_SIX_MILLIONS, SIX_EIGHT_MILLIONS, TEN_THIRTY_MILLIONS,
                        FIFTEEN_TWENTY_FIVE_MILLIONS, TWENTY_FIVE_THIRTY_MILLIONS,
                        THIRTY_FORTY_MILLIONS, FORTY_FIFTY_MILLIONS, FORTY_FORTY_FIVE_MILLIONS,
                        FORTY_FIVE_FORTY_SEVEN_MILLIONS, FORTY_SEVEN_SEVENTY_MILLIONS,
                        SEVENTY_ONE_HUNDRED_TEN_MILLIONS, EIGHT_TEN_MILLIONS,
                        TEN_THIRTEEN_MILLIONS, THIRTEEN_FIFTEEN_MILLIONS, FIFTEEN_ONE_HUNDRED_FORTY_MILLIONS -> {
                    sendRealEstateWillingnessCommand(chatId);
                    eqtUsersChoices.setPrice(callBackData);
                    eqtUsersChoicesRepo.save(eqtUsersChoices);
                }
            }
            switch (callBackData) {
                case SECONDARY, OFF_PLAN -> {
                    eqtUsersChoices.setWillingness(callBackData);
                    eqtUsersChoicesRepo.save(eqtUsersChoices);
                    sendMessage(chatId, PRE_RESULT_MESSAGE);
                    List<String> prices = PriceUtil.getPriceRange(eqtUsersChoices.getPrice());
                    List<Long> realEstatesIds = eqtRealEstatesRepo.findRealEstatesIds(
                            eqtUsersChoices.getType(),
                            eqtUsersChoices.getWillingness(), prices.get(0), prices.get(1));
                    eqtUsersChoices.setObjectsFound(String.valueOf(realEstatesIds.size() - 1));
                    eqtUsersChoicesRepo.save(eqtUsersChoices);
                    int objectsFound = Integer.parseInt(eqtUsersChoices.getObjectsFound());
                    if (realEstatesIds.size() > 0) {
                        sendRealEstatesByOne(chatId, realEstatesIds, objectsFound);
                        sendRequestResultRealEstatesCommand(chatId);
                    } else {
                        sendMessage(chatId, OBJECTS_NOT_FOUND);
                    }
                    eqtUsersChoices.setObjectsFound(String.valueOf(objectsFound - 1));
                    eqtUsersChoicesRepo.save(eqtUsersChoices);
                }
            }
            switch (callBackData) {
                case NEXT_OBJECT -> {
                    List<String> prices = PriceUtil.getPriceRange(eqtUsersChoices.getPrice());
                    List<Long> realEstatesIds = eqtRealEstatesRepo.findRealEstatesIds(
                            eqtUsersChoices.getType(),
                            eqtUsersChoices.getWillingness(), prices.get(0), prices.get(1));
                    int objectsFound = Integer.parseInt(eqtUsersChoices.getObjectsFound());
                    if (objectsFound >= 0) {
                        sendRealEstatesByOne(chatId, realEstatesIds, objectsFound);
                        sendRequestResultRealEstatesCommand(chatId);
                        eqtUsersChoices.setObjectsFound(String.valueOf(objectsFound - 1));
                        eqtUsersChoicesRepo.save(eqtUsersChoices);
                    } else {
                        sendMessage(chatId, AFTER_RESULT_MESSAGE);
                    }

                }
            }
        }
    }


    private void sendRequestResultRealEstatesCommand(Long chatId) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chatId);
        sendMessage.setText(MORE_OBJECTS_ACCORDING_TO_YOUR_REQUEST);
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> buttons = new ArrayList<>();
        List<InlineKeyboardButton> buttons1 = new ArrayList<>();
        List<InlineKeyboardButton> buttons2 = new ArrayList<>();
        List<InlineKeyboardButton> buttons3 = new ArrayList<>();
        InlineKeyboardButton button1 = new InlineKeyboardButton();
        InlineKeyboardButton button2 = new InlineKeyboardButton();
        InlineKeyboardButton button3 = new InlineKeyboardButton();
        button1.setText(NEXT_OBJECT);
        button1.setCallbackData(NEXT_OBJECT);
        button2.setText(CONTACT_FOR_MANAGER);
        button2.setCallbackData(CONTACT_FOR_MANAGER);
        button3.setText(VISIT_COMPANY_WEBSITE);
        button3.setCallbackData(VISIT_COMPANY_WEBSITE);
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

    private void sendRealEstatesByOne(Long chatId, List<Long> realEstateIds, int index) {
        if (eqtRealEstatesRepo.findById(realEstateIds.get(index)).isPresent()) {
            EqtRealEstates realEstate = eqtRealEstatesRepo.findById(realEstateIds.get(index)).get();
            String neighbourhood = realEstate.getNeighbourhood();
            String description = realEstate.getDescription();
            String size = realEstate.getSize();
            String price = realEstate.getPrice();
            String picture1 = realEstate.getPicture1();
            String picture2 = realEstate.getPicture2();
            String picture3 = realEstate.getPicture3();
            sendRealEstateObject(picture1, picture2, picture3,
                    neighbourhood, description, size, price, chatId);
        }
    }

    private void sendRealEstateObject(String picture1, String picture2, String picture3,
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
        sendMessage(chatId, MessagesUtil.resultMessageBuilder(description, neighbourhood, size, price));
    }

    private void sendStartCommand(Long chatId, String username) {
        String answer = "Hi, " + username + ", nice to meet you!" + "\n";
        sendMessage(chatId, answer);
    }

    private void sendEndCommand(Long chatId, String username) {
        String answer = "Goodbye, " + username + ", thank you for using our bot!";
        sendMessage(chatId, answer);
    }

    private void sendRealEstateWillingnessCommand(Long chatId) {
        SendPhoto photo = new SendPhoto();
        photo.setChatId(chatId);
        photo.setPhoto(realEstateService.getPictureFromResources("type_of_willingness.jpeg"));
        photo.setCaption("Willingness of real estate");
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> buttons = new ArrayList<>();
        List<InlineKeyboardButton> button1 = new ArrayList<>();
        List<InlineKeyboardButton> button2 = new ArrayList<>();
        InlineKeyboardButton selectRealEstateWayToPay1 = new InlineKeyboardButton();
        InlineKeyboardButton selectRealEstateWayToPay2 = new InlineKeyboardButton();
        selectRealEstateWayToPay1.setText(SECONDARY);
        selectRealEstateWayToPay1.setCallbackData(SECONDARY);
        selectRealEstateWayToPay2.setText(OFF_PLAN);
        selectRealEstateWayToPay2.setCallbackData(OFF_PLAN);
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

    private void sendRealEstateCommand(Long chatId) {
        SendPhoto photo = new SendPhoto();
        photo.setCaption("How can I help you?");
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
        button1.setText(CHOOSE_REAL_ESTATE);
        button1.setCallbackData(CHOOSE_REAL_ESTATE);
        button2.setText(CONTACT_FOR_MANAGER);
        button2.setCallbackData(CONTACT_FOR_MANAGER);
        button3.setText(VISIT_COMPANY_WEBSITE);
        button3.setCallbackData(VISIT_COMPANY_WEBSITE);
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

    private void sendRealEstateTypeCommand(Long chatId) {
        SendPhoto photo = new SendPhoto();
        photo.setChatId(chatId);
        photo.setPhoto(realEstateService.getPictureFromResources("type_of_object.jpeg"));
        photo.setCaption("What type of real estate are you interested in ?");
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
        button1.setText(APARTMENTS);
        button2.setText(PENTHOUSES);
        button3.setText(VILLAS);
        button4.setText(DUPLEX);
        button1.setCallbackData(APARTMENTS);
        button2.setCallbackData(PENTHOUSES);
        button3.setCallbackData(VILLAS);
        button4.setCallbackData(DUPLEX);
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

    private void sendRealEstatePriceCommand(Long chatId, String price1,
                                            String price2, String price3, String price4) {
        SendPhoto photo = new SendPhoto();
        photo.setChatId(chatId);
        photo.setPhoto(realEstateService.getPictureFromResources("price_range.jpeg"));
        photo.setCaption("How much are you ready to pay for it?");
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

    @SneakyThrows
    private List<BotCommand> initCommands() {
        List<BotCommand> commands = new ArrayList<>();
        commands.add(new BotCommand("/start", "start bot"));
        commands.add(new BotCommand("/end", "end bot"));
        commands.add(new BotCommand("/help", "administrator"));
        commands.add(new BotCommand("/site", "company website"));
        try {
            this.execute(new SetMyCommands(commands, new BotCommandScopeDefault(), null));
        } catch (TelegramApiException e) {
            registerException(this.getMe().getId(), e.getMessage());
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
            user.setRegisteredAt(TimeUtil.currentTime());
            user.setObjectsFound("0");
            eqtUsersChoicesRepo.save(user);
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
