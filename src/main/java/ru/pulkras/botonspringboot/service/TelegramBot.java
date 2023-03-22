package ru.pulkras.botonspringboot.service;

import com.vdurmont.emoji.EmojiParser;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;
import org.telegram.telegrambots.meta.api.objects.commands.scope.BotCommandScopeDefault;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import ru.pulkras.botonspringboot.config.BotConfig;
import ru.pulkras.botonspringboot.model.User;
import ru.pulkras.botonspringboot.model.UserRepository;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;


@Slf4j
@Component
public class TelegramBot extends TelegramLongPollingBot {

    @Autowired
    private UserRepository userRepository;
    final BotConfig config;

    static final String HELPING_TEXT = "This is my first bot on spring boot. \nit demonstrates Spring Boot capabilities\n" +
            "you can use a menu to launch some commands or do it manually. \n" +
            "you can press /start to get a welcome message\n" +
            "you can press /mydata to get data about yourself\n" +
            "you can press /deletedata to delete data about yourself\n" +
            "you can press /help to see this message))\n" +
            "you can press /settings to see current settings or set yours\n" +
            "or you can press /author to see info about author";

    static final String AUTHOR_INFO = "The author is pulkras(Mikhail Malygin). I learn to use new technologies and this is my creation";

    public TelegramBot(BotConfig config) {
        this.config = config;
        List<BotCommand> listOfCommands = new ArrayList<>();
        listOfCommands.add(new BotCommand("/start", "get a welcome message"));
        listOfCommands.add(new BotCommand("/mydata", "get data about yourself"));
        listOfCommands.add(new BotCommand("/deletedata", "delete data about yourself"));
        listOfCommands.add(new BotCommand("/help", "explaining how to use this bot"));
        listOfCommands.add(new BotCommand("/settings", "see settings or set your preferences"));
        listOfCommands.add(new BotCommand("/author", "see info about author"));
        try {
            this.execute(new SetMyCommands(listOfCommands, new BotCommandScopeDefault(), null));
        } catch(TelegramApiException tae) {
            log.error("Error setting bot's command list: " + tae.getMessage());
        }
    }

    @Override
    public String getBotToken() {
        return config.getKey();
    }

    @Override
    public String getBotUsername() {
        return config.getBotName();
    }

    @Override
    public void onUpdateReceived(@NotNull Update update) {
        if(update.hasMessage() && update.getMessage().hasText()) {
            String messageText = update.getMessage().getText();
            long chatId = update.getMessage().getChatId();
            switch(messageText) {
                case "/start":
                    reactionToStartCommand(chatId, update.getMessage().getChat().getFirstName(), messageText);
                    userRegistration(update.getMessage());
                    break;
                case "/help":
                    sendMessage(chatId, HELPING_TEXT, messageText);
                    break;
                case "/author":
                    sendMessage(chatId, AUTHOR_INFO, messageText);
                default:
                    sendMessage(chatId, "Sorry, command was not recognized", messageText);
            }
        }
    }


    private void userRegistration(@NotNull Message message) {
        if (userRepository.findById(message.getChatId()).isEmpty()) {
            Long chatId = message.getChatId();

            Chat chat = message.getChat();

            User user = new User();

            user.setChatId(chatId);
            user.setFirstName(chat.getFirstName());
            user.setSurname(chat.getLastName());
            user.setRegisteredAt(new Timestamp(System.currentTimeMillis()));

            userRepository.save(user);

            log.info("user saved: " + user);
        }
    }
    private void reactionToStartCommand(long chatId, String name, String command) {

        String answer = EmojiParser.parseToUnicode("Hello, " + name + " and welcome to our telegram bot!" + ":blush:");

        log.info("replied to user " + name);

        sendMessage(chatId, answer, command);
    }

    private void sendMessage(long chatId, String text, String command) {
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText(text);

        setKeyboard(message, command);

        try {
            execute(message);
        } catch (TelegramApiException tae) {
            log.error("Error occurrred. " + tae.getMessage());
        }
    }
    private void setKeyboard(SendMessage message, String command) {
        ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();

        List<KeyboardRow> keyboardRows = new ArrayList<>();

        KeyboardRow row = new KeyboardRow();

        row.add("try to use other buttons");

        if (command.equals("/start") || command.equals("/help")) {
            row.add("click here to see settings");

        }
        else if(command.equals("/settings")) {
            row.add("click here to see help message");
        }

        else if(!command.equals("/author")) {
                row.add("click me to show info about author");
        }

        keyboardRows.add(row);

        row = new KeyboardRow();

        row.add("check my data");
        row.add("delete my data");

        keyboardRows.add(row);
        
        row = new KeyboardRow();

        row.add("register or sign in");
        row.add("write @pulkras to ask your question");

        keyboardRows.add(row);

        keyboardMarkup.setKeyboard(keyboardRows);

        message.setReplyMarkup(keyboardMarkup);
    }
}

