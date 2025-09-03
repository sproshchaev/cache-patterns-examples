package com.prosoft.service;

import com.prosoft.model.User;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class UserService {

    // Имитация базы данных
    private final Map<Long, User> database = new HashMap<>();

    // Write-Back Cache - данные сначала в кэш, потом асинхронно в БД
    private final Map<Long, User> cache = new ConcurrentHashMap<>();

    // Планировщик для асинхронной записи в БД
    private final ScheduledExecutorService writeBackExecutor = Executors.newScheduledThreadPool(2);

    @PostConstruct
    public void init() {
        // Добавим тестовые данные
        database.put(1L, new User(1L, "Alice", "alice@example.com", false));
        database.put(2L, new User(2L, "Bob", "bob@example.com", false));
        database.put(3L, new User(3L, "Charlie", "charlie@example.com", false));

        log.info("🔧 База данных инициализирована с {} пользователями", database.size());

        // Запускаем фоновый процесс для Write-Back
        startWriteBackProcess();
    }

    /**
     * CACHE-ASIDE PATTERN для чтения
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
     * WRITE-BACK PATTERN - данные записываются сначала в кэш, потом асинхронно в БД
     */
    public User createUser(User user) {
        log.info("➕ Создаем нового пользователя через Write-Back: {}", user.getName());

        // WRITE-BACK: записываем сначала в кэш
        if (user.getId() == null) {
            long maxId = database.keySet().stream().mapToLong(Long::longValue).max().orElse(0L);
            user.setId(maxId + 1);
        }

        user.setDirty(true); // Помечаем как "грязные" данные

        log.info("キャッシング: Сохраняем пользователя в кэш (помечен как dirty)");
        cache.put(user.getId(), user);

        // Асинхронная запись в БД будет выполнена позже фоновым процессом
        log.info("⏭️  Асинхронная запись в БД запланирована");

        log.info("✅ Пользователь создан в кэше (ожидает записи в БД): {} ({})", user.getName(), user.getId());
        return user;
    }

    /**
     * WRITE-BACK PATTERN - данные обновляются в кэше, потом асинхронно в БД
     */
    public User updateUser(Long id, String name, String email) {
        log.info("✏️ Обновляем пользователя {} через Write-Back", id);

        // Проверяем существование пользователя в кэше
        User user = cache.get(id);
        if (user == null) {
            // Если нет в кэше, загружаем из БД
            user = database.get(id);
            if (user == null) {
                log.warn("⚠️  Пользователь не найден! ID: {}", id);
                return null;
            }
            cache.put(id, user);
        }

        // WRITE-BACK: обновляем только в кэше
        log.info("WRITE-BACK: Обновляем пользователя в кэше");
        user.setName(name);
        user.setEmail(email);
        user.setDirty(true); // Помечаем как "грязные" данные

        cache.put(id, user);

        // Асинхронная запись в БД будет выполнена позже
        log.info("⏭️  Асинхронное обновление БД запланировано");

        log.info("✅ Пользователь обновлен в кэше (ожидает записи в БД): {} ({})", user.getName(), user.getId());
        return user;
    }

    /**
     * WRITE-BACK PATTERN - удаление сначала из кэша, потом асинхронно из БД
     */
    public void deleteUser(Long id) {
        log.info("🗑️ Удаляем пользователя {} через Write-Back", id);

        // WRITE-BACK: удаляем из кэша
        User user = cache.get(id);
        if (user != null) {
            user.setDirty(true); // Помечаем для удаления
            cache.put(id, user); // Пока храним в кэше как "грязные" данные для удаления
        }

        // Асинхронное удаление из БД будет выполнено позже
        log.info("⏭️  Асинхронное удаление из БД запланировано");

        log.info("✅ Пользователь помечен для удаления из БД: {}", id);
    }

    /**
     * Фоновый процесс Write-Back - периодически записывает "грязные" данные в БД
     */
    private void startWriteBackProcess() {
        writeBackExecutor.scheduleAtFixedRate(() -> {
            try {
                processDirtyData();
            } catch (Exception e) {
                log.error("Ошибка в процессе Write-Back: ", e);
            }
        }, 5, 5, TimeUnit.SECONDS); // Каждые 5 секунд

        log.info("🚀 Запущен фоновый процесс Write-Back (каждые 5 секунд)");
    }

    /**
     * Обработка "грязных" данных - запись в БД
     */
    private void processDirtyData() {
        log.debug("🔁 Write-Back процесс: проверка наличия dirty данных...");

        cache.entrySet().stream()
                .filter(entry -> entry.getValue().isDirty())
                .forEach(entry -> {
                    Long userId = entry.getKey();
                    User user = entry.getValue();

                    try {
                        // Асинхронная запись в БД
                        CompletableFuture.runAsync(() -> {
                            log.info("💾 WRITE-BACK: Асинхронно записываем пользователя {} в БД", userId);
                            database.put(userId, new User(user.getId(), user.getName(), user.getEmail(), false));
                            user.setDirty(false); // Сбрасываем флаг dirty
                            log.info("✅ WRITE-BACK: Пользователь {} успешно записан в БД", userId);
                        });
                    } catch (Exception e) {
                        log.error("❌ Ошибка записи пользователя {} в БД: ", userId, e);
                    }
                });
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

    // Метод для демонстрации состояния БД
    public Map<Long, User> getDatabase() {
        log.info("🗄️ Возвращаем содержимое базы данных. Размер БД: {}", database.size());
        return new HashMap<>(database);
    }
}