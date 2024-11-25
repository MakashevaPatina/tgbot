package pro.sky.telegrambot.scheduler;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.request.SendMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import pro.sky.telegrambot.model.NotificationTask;
import pro.sky.telegrambot.service.NotificationTaskService;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Component
public class NotificationScheduler {
    private final Logger logger = LoggerFactory.getLogger(NotificationScheduler.class);
    private final NotificationTaskService service;
    private final TelegramBot telegramBot;

    public NotificationScheduler(NotificationTaskService service, TelegramBot telegramBot) {
        this.service = service;
        this.telegramBot = telegramBot;
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


