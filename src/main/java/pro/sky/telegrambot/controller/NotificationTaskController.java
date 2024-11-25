package pro.sky.telegrambot.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import pro.sky.telegrambot.model.NotificationTask;
import pro.sky.telegrambot.service.NotificationTaskService;

@RestController
@RequestMapping("/tasks")
public class NotificationTaskController {

    private final NotificationTaskService service;

    @Autowired
    public NotificationTaskController(NotificationTaskService service) {
        this.service = service;
    }

    @PostMapping("/create")
    public NotificationTask createTask(@RequestParam String message, @RequestParam Long chatId) {
        return service.parseAndSave(message, chatId);
    }
}
