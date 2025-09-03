# Read-Through Cache Pattern

Это максимально простой пример, демонстрирующий паттерн **Read-Through Cache**. 

В отличие от Cache-Aside, приложение взаимодействует **только с кэшем**, а кэш сам загружает данные из источника 
при их отсутствии.

Паттерн Read-Through состоит из 1 основного шага:
1. **Обращение к кэшу** - приложение всегда обращается к кэшу, который сам решает, нужно ли загружать данные

## 📊 Что демонстрирует пример:

```
1️⃣ ПЕРВЫЙ ЗАПРОС: READ-THROUGH → кэш автоматически загружает из БД
2️⃣ ВТОРОЙ ЗАПРОС: CACHE HIT → мгновенное получение из кэша
3️⃣ ОБНОВЛЕНИЕ ДАННЫХ: инвалидация кэша
4️⃣ ПОСЛЕ ОБНОВЛЕНИЯ: снова READ-THROUGH для обновленных данных
```

### macOS / Linux Команды
#### 1️⃣ ПЕРВЫЙ ЗАПРОС - READ-THROUGH (кэш автоматически загружает из БД)
```bash
curl "http://localhost:8081/api/users/1"
```

#### 2️⃣ ВТОРОЙ ЗАПРОС - CACHE HIT (данные берутся из кэша)
```bash
curl "http://localhost:8081/api/users/1"
```

#### 3️⃣ ЗАПРОС ДРУГОГО ПОЛЬЗОВАТЕЛЯ - READ-THROUGH
```bash
curl "http://localhost:8081/api/users/2"
```

#### 4️⃣ ОБНОВЛЕНИЕ ДАННЫХ (инвалидирует кэш)
```bash
curl -X PUT "http://localhost:8081/api/users/1?name=Alice%20Updated&email=alice.updated@example.com"
```

#### 5️⃣ ПОВТОРНЫЙ ЗАПРОС - СНОВА READ-THROUGH
```bash
curl "http://localhost:8081/api/users/1"
```

#### 6️⃣ ОЧИСТКА ВСЕГО КЭША
```bash
curl -X POST "http://localhost:8081/api/users/cache/clear"
```

#### 7️⃣ ПРОСМОТР СОСТОЯНИЯ КЭША
```bash
curl "http://localhost:8081/api/users/cache/show"
```

### Windows Команды
#### 1️⃣ ПЕРВЫЙ ЗАПРОС - READ-THROUGH
```bash
Invoke-RestMethod -Uri "http://localhost:8081/api/users/1" -Method GET
```

#### 2️⃣ ВТОРОЙ ЗАПРОС - CACHE HIT
```bash
Invoke-RestMethod -Uri "http://localhost:8081/api/users/1" -Method GET
```

#### 3️⃣ ЗАПРОС ДРУГОГО ПОЛЬЗОВАТЕЛЯ - READ-THROUGH
```bash
Invoke-RestMethod -Uri "http://localhost:8081/api/users/2" -Method GET
```

#### 4️⃣ ОБНОВЛЕНИЕ ДАННЫХ (инвалидирует кэш)
```bash
Invoke-RestMethod -Uri "http://localhost:8081/api/users/1?name=Alice%20Updated&email=alice.updated@example.com" -Method PUT
```

#### 5️⃣ ПОВТОРНЫЙ ЗАПРОС - СНОВА READ-THROUGH
```bash
Invoke-RestMethod -Uri "http://localhost:8081/api/users/1" -Method GET
```

#### 6️⃣ ОЧИСТКА ВСЕГО КЭША
```bash
Invoke-RestMethod -Uri "http://localhost:8081/api/users/cache/clear" -Method POST
```

#### 7️⃣ ПРОСМОТР СОСТОЯНИЯ КЭША
```bash
Invoke-RestMethod -Uri "http://localhost:8081/api/users/cache/show" -Method GET
```

### Преимущества Read-Through:
- ✅ **Простота использования** - приложение работает только с кэшем
- ✅ **Автоматическое заполнение** - кэш сам управляет загрузкой данных
- ✅ **Единообразный интерфейс** - одинаковый способ доступа ко всем данным

### Недостатки:
- ❌ **Меньше контроля** - нельзя выбрать, что кэшировать
- ❌ **Все данные через кэш** - даже одноразовые запросы
- ❌ **Автоматическое кэширование** - может кэшировать ненужные данные

### Когда использовать:
- 🔹 Когда нужна абстракция от источника данных
- 🔹 Для предсказуемых паттернов доступа
- 🔹 Когда можно позволить себе автоматическое кэширование
- 🔹 Для упрощения логики приложения

## 📈 Жизненный цикл запроса

```
Клиент → Controller → Read-Through Cache 
                    ↓
         Данные в кэше? — НЕТ —→ Загрузить из БД (автоматически)
              ↓ ДА
         Вернуть из кэша
              ↓
            Клиент
```

