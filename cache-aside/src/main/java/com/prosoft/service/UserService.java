package com.prosoft.service;

import com.prosoft.model.User;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Slf4j
public class UserService {

    // Имитация базы данных
    private final Map<Long, User> database = new HashMap<>();

    // Имитация кэша
    private final Map<Long, User> cache = new ConcurrentHashMap<>();

    @PostConstruct
    public void init() {
        // Добавим тестовые данные
        database.put(1L, new User(1L, "Alice", "alice@example.com"));
        database.put(2L, new User(2L, "Bob", "bob@example.com"));
        database.put(3L, new User(3L, "Charlie", "charlie@example.com"));

        log.info("База данных инициализирована с {} пользователями", database.size());
    }

    /**
     * CACHE-ASIDE PATTERN - 3 основных шага
     */
    public User getUserById(Long id) {
        log.info("Запрос пользователя с ID: {}", id);

        // ШАГ 1: Проверяем кэш
        User cachedUser = cache.get(id);
        if (cachedUser != null) {
            log.info("CACHE HIT: Пользователь найден в кэше!");
            return cachedUser;
        }

        // ШАГ 2: Кэш-промах - загружаем из базы данных
        log.info("CACHE MISS: Пользователь не найден в кэше, загружаем из базы...");
        User userFromDatabase = database.get(id);

        if (userFromDatabase == null) {
            log.warn("Пользователь не найден в базе данных! ID: {}", id);
            return null;
        }

        // ШАГ 3: Сохраняем в кэш для будущих запросов
        log.info("Сохраняем пользователя в кэш...");
        cache.put(id, userFromDatabase);
        log.info("Пользователь сохранен в кэше!");

        return userFromDatabase;
    }

    public User updateUser(Long id, String name, String email) {
        log.info("Обновляем пользователя {}", id);

        User user = database.get(id);
        if (user != null) {
            user.setName(name);
            user.setEmail(email);
            database.put(id, user);

            log.info("Удаляем пользователя из кэша (инвалидация)");
            cache.remove(id);
        } else {
            log.warn("Попытка обновления несуществующего пользователя ID: {}", id);
        }

        return user;
    }

    public void clearCache() {
        log.info("Очищаем весь кэш! Было записей: {}", cache.size());
        cache.clear();
        log.info("Кэш успешно очищен");
    }

    public Map<Long, User> getCache() {
        log.info("Возвращаем содержимое кэша клиенту. Размер кэша: {}", cache.size());
        return new HashMap<>(cache);
    }
}