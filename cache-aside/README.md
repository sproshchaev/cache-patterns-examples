# Cache-Aside Pattern

Это максимально простой пример, демонстрирующий паттерн **Cache-Aside** (также известный как Lazy Loading Cache).

Паттерн Cache-Aside состоит из 3 основных шагов:
1. **Проверить кэш** - сначала ищем данные в кэше
2. **Cache Miss** - если данных нет, загружаем из источника данных
3. **Сохранить в кэш** - помещаем данные в кэш для будущих запросов

## 📊 Что демонстрирует пример:

```
1️⃣ ПЕРВЫЙ ЗАПРОС: CACHE MISS → загрузка из БД → сохранение в кэш
2️⃣ ВТОРОЙ ЗАПРОС: CACHE HIT → мгновенное получение из кэша
3️⃣ ОБНОВЛЕНИЕ ДАННЫХ: инвалидация кэша
4️⃣ ПОСЛЕ ОБНОВЛЕНИЯ: снова CACHE MISS для обновленных данных
```

### macOS / Linux Команды
#### 1️⃣ ПЕРВЫЙ ЗАПРОС - CACHE MISS (данные загружаются из БД)
```bash
curl "http://localhost:8080/api/users/1"
```

#### 2️⃣ ВТОРОЙ ЗАПРОС - CACHE HIT (данные берутся из кэша)
```bash
curl "http://localhost:8080/api/users/1"
```

#### 3️⃣ ЗАПРОС ДРУГОГО ПОЛЬЗОВАТЕЛЯ - CACHE MISS
```bash
curl "http://localhost:8080/api/users/2"
```

#### 4️⃣ ОБНОВЛЕНИЕ ДАННЫХ (инвалидирует кэш)
```bash
curl -X PUT "http://localhost:8080/api/users/1?name=Alice%20Updated&email=alice.new@example.com"
```

#### 5️⃣ ПОВТОРНЫЙ ЗАПРОС - СНОВА CACHE MISS (после обновления)
```bash
curl "http://localhost:8080/api/users/1"
```

#### 6️⃣ ОЧИСТКА ВСЕГО КЭША
```bash
curl -X POST "http://localhost:8080/api/users/cache/clear"
```

#### 7️⃣ ПРОСМОТР СОСТОЯНИЯ КЭША
```bash
curl "http://localhost:8080/api/users/cache/show"
```

### Windows Команды
#### 1️⃣ ПЕРВЫЙ ЗАПРОС - CACHE MISS
```bash
Invoke-RestMethod -Uri "http://localhost:8080/api/users/1" -Method GET
```

#### 2️⃣ ВТОРОЙ ЗАПРОС - CACHE HIT
```bash
Invoke-RestMethod -Uri "http://localhost:8080/api/users/1" -Method GET
```

#### 3️⃣ ЗАПРОС ДРУГОГО ПОЛЬЗОВАТЕЛЯ - CACHE MISS
```bash
Invoke-RestMethod -Uri "http://localhost:8080/api/users/2" -Method GET
```

#### 4️⃣ ОБНОВЛЕНИЕ ДАННЫХ (инвалидирует кэш)
```bash
Invoke-RestMethod -Uri "http://localhost:8080/api/users/1?name=Alice%20Updated&email=alice.new@example.com" -Method PUT
```

#### 5️⃣ ПОВТОРНЫЙ ЗАПРОС - СНОВА CACHE MISS
```bash
Invoke-RestMethod -Uri "http://localhost:8080/api/users/1" -Method GET
```

#### 6️⃣ ОЧИСТКА ВСЕГО КЭША
```bash
Invoke-RestMethod -Uri "http://localhost:8080/api/users/cache/clear" -Method POST
```

#### 7️⃣ ПРОСМОТР СОСТОЯНИЯ КЭША
```bash
Invoke-RestMethod -Uri "http://localhost:8080/api/users/cache/show" -Method GET
```

### Преимущества Cache-Aside:
- ✅ **Экономия ресурсов** - данные кэшируются только при первом запросе
- ✅ **Гибкость** - можно кэшировать только часто запрашиваемые данные
- ✅ **Простота** - понятная логика работы

### Недостатки:
- ❌ **Задержка при первом доступе** - первый запрос будет медленнее
- ❌ **Кэш-пропуски** - возможны при старте приложения или после инвалидации
- ❌ **Сложность согласованности** - нужно правильно управлять инвалидацией

### Когда использовать:
- 🔹 Read-heavy нагрузки
- 🔹 Данные, которые не требуют мгновенной согласованности
- 🔹 Когда можно позволить себе задержку при первом запросе

## 📈 Жизненный цикл запроса

```
Клиент → Controller → Service 
                    ↓
               Проверить кэш? — НЕТ —→ Загрузить из БД —→ Сохранить в кэш
                    ↓ ДА
               Вернуть из кэша
                    ↓
                 Клиент
```
