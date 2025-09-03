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

    // Write-Through Cache
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
     * WRITE-THROUGH PATTERN - данные записываются одновременно и в кэш, и в БД
     */
    public User getUserById(Long id) {
        log.info("🔍 Запрос пользователя с ID: {}", id);

        // Проверяем кэш
        User cachedUser = cache.get(id);
        if (cachedUser != null) {
            log.info("🎯 CACHE HIT: Пользователь найден в кэше!");
            return cachedUser;
        }

        // Если нет в кэше, загружаем из БД
        log.info("❌ CACHE MISS: Пользователь не найден в кэше, загружаем из базы...");
        User userFromDatabase = database.get(id);

        if (userFromDatabase == null) {
            log.warn("⚠️  Пользователь не найден в базе данных! ID: {}", id);
            return null;
        }

        // Сохраняем в кэш для будущих запросов
        log.info("💾 Сохраняем пользователя в кэш...");
        cache.put(id, userFromDatabase);
        log.info("✅ Пользователь сохранен в кэше!");

        return userFromDatabase;
    }

    /**
     * WRITE-THROUGH PATTERN - данные записываются одновременно и в кэш, и в БД
     */
    public User createUser(User user) {
        log.info("➕ Создаем нового пользователя: {}", user.getName());

        // WRITE-THROUGH: записываем сначала в БД
        if (user.getId() == null) {
            long maxId = database.keySet().stream().mapToLong(Long::longValue).max().orElse(0L);
            user.setId(maxId + 1);
        }

        log.info("💾 WRITE-THROUGH: Сохраняем пользователя в базу данных");
        database.put(user.getId(), user);

        // WRITE-THROUGH: затем в кэш (гарантируем согласованность)
        log.info("キャッシング: Сохраняем пользователя в кэш");
        cache.put(user.getId(), user);

        log.info("✅ Пользователь создан и закэширован: {} ({})", user.getName(), user.getId());
        return user;
    }

    /**
     * WRITE-THROUGH PATTERN - данные обновляются одновременно и в кэше, и в БД
     */
    public User updateUser(Long id, String name, String email) {
        log.info("✏️ Обновляем пользователя {} через Write-Through", id);

        // Проверяем существование пользователя
        User existingUser = database.get(id);
        if (existingUser == null) {
            log.warn("⚠️  Попытка обновления несуществующего пользователя ID: {}", id);
            return null;
        }

        // WRITE-THROUGH: обновляем сначала в БД
        log.info("💾 WRITE-THROUGH: Обновляем пользователя в базе данных");
        existingUser.setName(name);
        existingUser.setEmail(email);
        database.put(id, existingUser);

        // WRITE-THROUGH: затем в кэше (гарантируем согласованность)
        log.info("WRITE-THROUGH: Обновляем пользователя в кэше");
        cache.put(id, existingUser);

        log.info("✅ Пользователь обновлен в БД и кэше: {} ({})", existingUser.getName(), existingUser.getId());
        return existingUser;
    }

    public void deleteUser(Long id) {
        log.info("🗑️ Удаляем пользователя {} через Write-Through", id);

        // WRITE-THROUGH: удаляем сначала из БД
        log.info("💾 WRITE-THROUGH: Удаляем пользователя из базы данных");
        database.remove(id);

        // WRITE-THROUGH: затем из кэша
        log.info("WRITE-THROUGH: Удаляем пользователя из кэша");
        cache.remove(id);

        log.info("✅ Пользователь удален из БД и кэша: {}", id);
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