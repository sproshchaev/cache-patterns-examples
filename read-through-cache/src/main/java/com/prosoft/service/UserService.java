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

    // Read-Through Cache - кэш сам загружает данные при их отсутствии
    private final Map<Long, User> cache = new ConcurrentHashMap<>();

    @PostConstruct
    public void init() {
        // Добавим тестовые данные
        database.put(1L, new User(1L, "Alice", "alice@example.com"));
        database.put(2L, new User(2L, "Bob", "bob@example.com"));
        database.put(3L, new User(3L, "Charlie", "charlie@example.com"));

        log.info("🔧 База данных инициализирована с {} пользователями", database.size());
    }

    /**
     * READ-THROUGH PATTERN - приложение взаимодействует только с кэшем
     * Кэш сам загружает данные из источника при их отсутствии
     */
    public User getUserById(Long id) {
        log.info("🔍 Запрос пользователя с ID: {} через Read-Through кэш", id);

        // READ-THROUGH: computeIfAbsent автоматически загружает данные при их отсутствии
        User user = cache.computeIfAbsent(id, this::loadUserFromDatabase);

        if (user != null) {
            log.info("🎯 CACHE HIT: Пользователь {} найден в кэше", id);
        } else {
            log.warn("⚠️  Пользователь {} не найден в базе данных", id);
        }

        return user;
    }

    // Метод-загрузчик для Read-Through кэша
    private User loadUserFromDatabase(Long id) {
        log.info("📖 READ-THROUGH: Автоматически загружаем пользователя {} из базы данных", id);
        return database.get(id); // Просто возвращаем из in-memory Map
    }

    public User updateUser(Long id, String name, String email) {
        log.info("✏️ Обновляем пользователя {}", id);

        // Обновляем в БД (имитация)
        User existingUser = database.get(id);
        if (existingUser != null) {
            existingUser.setName(name);
            existingUser.setEmail(email);
            database.put(id, existingUser);

            // Инвалидируем кэш (удаляем старые данные)
            log.info("🗑️ Удаляем пользователя {} из кэша (инвалидация)", id);
            cache.remove(id);
        }

        return existingUser;
    }

    public void clearCache() {
        log.info("🧹 Очищаем весь кэш! Было записей: {}", cache.size());
        cache.clear();
        log.info("✅ Кэш успешно очищен");
    }

    public Map<Long, User> getCache() {
        log.info("📤 Возвращаем содержимое кэша клиенту. Размер кэша: {}", cache.size());
        return new HashMap<>(cache);
    }
}