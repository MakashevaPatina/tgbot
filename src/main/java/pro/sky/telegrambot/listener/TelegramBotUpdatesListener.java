package pro.sky.telegrambot.listener;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.SendMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import pro.sky.telegrambot.model.NotificationTask;
import pro.sky.telegrambot.service.NotificationTaskService;

import javax.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
public class TelegramBotUpdatesListener implements UpdatesListener {

    private Logger logger = LoggerFactory.getLogger(TelegramBotUpdatesListener.class);
    private final NotificationTaskService service;
    @Autowired
    private TelegramBot telegramBot;

    public TelegramBotUpdatesListener(NotificationTaskService service) {
        this.service = service;
    }

    @PostConstruct
    public void init() {
        telegramBot.setUpdatesListener(this);
    }
    @Override
    public int process(List<Update> updates) {
        updates.forEach(update -> {
            logger.info("Processing update: {}", update);

            String messageText = update.message().text();
            Long chatId = update.message().chat().id();

            if ("/start".equals(messageText)) {
                sendWelcomeMessage(chatId);
            } else {
                processReminderMessage(messageText, chatId);
            }
            // Process your updates here
        });
        return UpdatesListener.CONFIRMED_UPDATES_ALL;
    }


    private void sendWelcomeMessage(Long chatId) {
        String welcomeText = "Привет! \nДобро пожаловать в бот для напоминаний! \nВведи время и название задачи в формате: \n01.01.2022 20:00 Сделать домашнюю работу \nИ я пришлю тебе напоминание в нужное время!";
        SendMessage message = new SendMessage(chatId, welcomeText);
        try {
            telegramBot.execute(message);
        } catch (Exception e) {
            logger.error("Failed to send welcome message to chatId: {}", chatId, e);
        }
    }

    private void processReminderMessage(String messageText, Long chatId) {
        try {
            NotificationTask task = service.parseAndSave(messageText, chatId);
            String confirmationText = "Напоминание создано: " + task.getMessageText() + " на " +
                    task.getNotificationTime().format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm"));
            telegramBot.execute(new SendMessage(chatId, confirmationText));
            
        } catch (IllegalArgumentException e) {
            telegramBot.execute(new SendMessage(chatId, "Ошибка: " + e.getMessage()));
        } catch (Exception e) {
            telegramBot.execute(new SendMessage(chatId, "Произошла ошибка при обработке сообщения."));
        }
    }

    @Scheduled(cron = "0 0/1 * * * *")
    public void sendNotifications() {
        LocalDateTime now = LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES);

        List<NotificationTask> notificationTaskList = service.getNotificationTasksByTaskTime(now);
        notificationTaskList.forEach(task -> {
            try {
                SendMessage message = new SendMessage(task.getChatId(), task.getMessageText());
                telegramBot.execute(message);
                logger.info("Уведомление отправлено для задачи: {}", task);
            } catch (Exception e) {
                logger.error("Ошибка при отправке уведомления для задачи: {}", task, e);
            }
        });
    }
}
