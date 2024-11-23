package pro.sky.telegrambot.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pro.sky.telegrambot.model.NotificationTask;
import pro.sky.telegrambot.repository.NotifTaskRepository;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class NotificationTaskService {

    private final NotifTaskRepository repository;

    @Autowired
    public NotificationTaskService(NotifTaskRepository repository) {
        this.repository = repository;
    }

    private static final String REGEX = "(\\d{2}\\.\\d{2}\\.\\d{4}\\s\\d{2}:\\d{2})(\\s+)(.+)";
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

    public NotificationTask parseAndSave(String message, Long chatId) {
        Pattern pattern = Pattern.compile(REGEX);
        Matcher matcher = pattern.matcher(message);

        if (matcher.matches()) {
            String dateTimeString = matcher.group(1);
            String reminderText = matcher.group(3);
            LocalDateTime notificationTime = LocalDateTime.parse(dateTimeString, FORMATTER);

            NotificationTask task = new NotificationTask(notificationTime, reminderText, chatId);
            return repository.save(task);
        }else {
            throw new IllegalArgumentException("Неверный формат сообщения. Ожидается: 'дд.мм.гггг чч:мм текст'.");
        }
    }

    public List<NotificationTask> getNotificationTasksByTaskTime(LocalDateTime time) {
        return repository.getNotificationTasksByNotificationTime(time);
    }
}
