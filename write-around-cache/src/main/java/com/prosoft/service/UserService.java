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

    // Кэш для чтения
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
     * CACHE-ASIDE PATTERN для чтения (как в оригинальном примере)
     */
    public User getUserById(Long id) {
        log.info("🔍 Запрос пользователя с ID: {}", id);

        // ШАГ 1: Проверяем кэш
        User cachedUser = cache.get(id);
        if (cachedUser != null) {
            log.info("🎯 CACHE HIT: Пользователь найден в кэше!");
            return cachedUser;
        }

        // ШАГ 2: Кэш-промах - загружаем из базы данных
        log.info("❌ CACHE MISS: Пользователь не найден в кэше, загружаем из базы...");
        User userFromDatabase = database.get(id);

        if (userFromDatabase == null) {
            log.warn("⚠️  Пользователь не найден в базе данных! ID: {}", id);
            return null;
        }

        // ШАГ 3: Сохраняем в кэш для будущих запросов
        log.info("💾 Сохраняем пользователя в кэш...");
        cache.put(id, userFromDatabase);
        log.info("✅ Пользователь сохранен в кэше!");

        return userFromDatabase;
    }

    /**
     * WRITE-AROUND PATTERN - данные записываются ТОЛЬКО в БД, минуя кэш
     */
    public User createUser(User user) {
        log.info("➕ Создаем нового пользователя через Write-Around: {}", user.getName());

        // WRITE-AROUND: записываем ТОЛЬКО в БД, НЕ записываем в кэш
        if (user.getId() == null) {
            long maxId = database.keySet().stream().mapToLong(Long::longValue).max().orElse(0L);
            user.setId(maxId + 1);
        }

        log.info("💾 WRITE-AROUND: Сохраняем пользователя ТОЛЬКО в базу данных (кэш пропущен)");
        database.put(user.getId(), user);

        // НЕ записываем в кэш - данные будут загружены при первом чтении
        log.info("⏭️  Кэш пропущен - данные будут загружены при первом чтении");

        log.info("✅ Пользователь создан в БД (без кэширования): {} ({})", user.getName(), user.getId());
        return user;
    }

    /**
     * WRITE-AROUND PATTERN - данные обновляются ТОЛЬКО в БД, кэш инвалидируется
     */
    public User updateUser(Long id, String name, String email) {
        log.info("✏️ Обновляем пользователя {} через Write-Around", id);

        // Проверяем существование пользователя
        User existingUser = database.get(id);
        if (existingUser == null) {
            log.warn("⚠️  Попытка обновления несуществующего пользователя ID: {}", id);
            return null;
        }

        // WRITE-AROUND: обновляем ТОЛЬКО в БД
        log.info("💾 WRITE-AROUND: Обновляем пользователя ТОЛЬКО в базе данных (кэш пропущен)");
        existingUser.setName(name);
        existingUser.setEmail(email);
        database.put(id, existingUser);

        // Инвалидируем кэш (удаляем старые данные)
        log.info("🗑️ Удаляем пользователя из кэша (инвалидация) - данные устарели");
        cache.remove(id);

        log.info("✅ Пользователь обновлен в БД, кэш инвалидирован: {} ({})", existingUser.getName(), existingUser.getId());
        return existingUser;
    }

    public void deleteUser(Long id) {
        log.info("🗑️ Удаляем пользователя {} через Write-Around", id);

        // WRITE-AROUND: удаляем ТОЛЬКО из БД
        log.info("💾 WRITE-AROUND: Удаляем пользователя ТОЛЬКО из базы данных (кэш пропущен)");
        database.remove(id);

        // Инвалидируем кэш
        log.info("🗑️ Удаляем пользователя из кэша (инвалидация)");
        cache.remove(id);

        log.info("✅ Пользователь удален из БД, кэш инвалидирован: {}", id);
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