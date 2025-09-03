package com.prosoft.controller;

import com.prosoft.model.User;
import com.prosoft.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/users")
@Slf4j
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/{id}")
    public User getUser(@PathVariable Long id) {
        log.info("Получен HTTP GET запрос для пользователя ID: {}", id);

        User user = userService.getUserById(id);

        if (user != null) {
            log.info("Успешно возвращен пользователь: {} ({})", user.getName(), user.getId());
        } else {
            log.warn("Пользователь с ID {} не найден", id);
        }

        return user;
    }

    @PutMapping("/{id}")
    public User updateUser(@PathVariable Long id,
                           @RequestParam String name,
                           @RequestParam String email) {
        log.info("Получен HTTP PUT запрос для обновления пользователя ID: {}", id);

        User user = userService.updateUser(id, name, email);

        if (user != null) {
            log.info("Успешно обновлен пользователь: {} ({})", user.getName(), user.getId());
        } else {
            log.warn("Не удалось обновить пользователя с ID {}", id);
        }

        return user;
    }

    @PostMapping("/cache/clear")
    public String clearCache() {
        log.info("Получен запрос на очистку кэша");
        userService.clearCache();
        String response = "Кэш очищен!";
        log.info("{}", response);
        return response;
    }

    @GetMapping("/cache/show")
    public Map<Long, User> showCache() {
        log.info("Получен запрос на просмотр содержимого кэша");
        Map<Long, User> cacheContent = userService.getCache();
        log.info("Возвращено {} записей из кэша", cacheContent.size());
        return cacheContent;
    }
}