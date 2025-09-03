# Write-Back (Write-Behind) Cache Pattern

Это максимально простой пример, демонстрирующий паттерн **Write-Back Cache** (также известный как Write-Behind). 

Данные записываются **сначала в кэш**, а затем **асинхронно** в базу данных, что обеспечивает очень высокую 
производительность записи.

Паттерн Write-Back состоит из 2 основных принципов:
1. **Чтение** - как в Cache-Aside (проверка кэша → загрузка из БД → сохранение в кэш)
2. **Запись** - данные записываются сначала в кэш, потом асинхронно в БД

## 📊 Что демонстрирует пример:

```
1️⃣ ЧТЕНИЕ: CACHE MISS → загрузка из БД → сохранение в кэш
2️⃣ ПОВТОРНОЕ ЧТЕНИЕ: CACHE HIT → мгновенное получение из кэша
3️⃣ ЗАПИСЬ: WRITE-BACK → запись в кэш → асинхронная запись в БД (через 5 секунд)
4️⃣ ФОНОВЫЙ ПРОЦЕСС: каждые 5 секунд проверяет "грязные" данные и записывает их в БД
```

### macOS / Linux Команды
#### 1️⃣ ПОЛУЧИТЬ ПОЛЬЗОВАТЕЛЯ (чтение)
```bash
curl "http://localhost:8084/api/users/1"
```

#### 2️⃣ СОЗДАТЬ НОВОГО ПОЛЬЗОВАТЕЛЯ (Write-Back)
```bash
curl -X POST "http://localhost:8084/api/users" \
  -H "Content-Type: application/json" \
  -d '{"name":"David","email":"david@example.com"}'
```

#### 3️⃣ ОБНОВИТЬ ПОЛЬЗОВАТЕЛЯ (Write-Back)
```bash
curl -X PUT "http://localhost:8084/api/users/1?name=Alice%20Updated&email=alice.updated@example.com"
```

#### 4️⃣ УДАЛИТЬ ПОЛЬЗОВАТЕЛЯ (Write-Back)
```bash
curl -X DELETE "http://localhost:8084/api/users/1"
```

#### 5️⃣ ПРОСМОТР СОСТОЯНИЯ КЭША
```bash
curl "http://localhost:8084/api/users/cache/show"
```

#### 6️⃣ ПРОСМОТР СОСТОЯНИЯ БАЗЫ ДАННЫХ
```bash
curl "http://localhost:8084/api/users/database/show"
```

#### 7️⃣ ОЧИСТКА ВСЕГО КЭША
```bash
curl -X POST "http://localhost:8084/api/users/cache/clear"
```

### Windows Команды
#### 1️⃣ ПОЛУЧИТЬ ПОЛЬЗОВАТЕЛЯ (чтение)
```bash
Invoke-RestMethod -Uri "http://localhost:8084/api/users/1" -Method GET
```

#### 2️⃣ СОЗДАТЬ НОВОГО ПОЛЬЗОВАТЕЛЯ (Write-Back)
```bash
Invoke-RestMethod -Uri "http://localhost:8084/api/users" -Method POST -Body '{"name":"David","email":"david@example.com"}' -ContentType "application/json"
```

#### 3️⃣ ОБНОВИТЬ ПОЛЬЗОВАТЕЛЯ (Write-Back)
```bash
Invoke-RestMethod -Uri "http://localhost:8084/api/users/1?name=Alice%20Updated&email=alice.updated@example.com" -Method PUT
```

#### 4️⃣ УДАЛИТЬ ПОЛЬЗОВАТЕЛЯ (Write-Back)
```bash
Invoke-RestMethod -Uri "http://localhost:8084/api/users/1" -Method DELETE
```

#### 5️⃣ ПРОСМОТР СОСТОЯНИЯ КЭША
```bash
Invoke-RestMethod -Uri "http://localhost:8084/api/users/cache/show" -Method GET
```

#### 6️⃣ ПРОСМОТР СОСТОЯНИЯ БАЗЫ ДАННЫХ
```bash
Invoke-RestMethod -Uri "http://localhost:8084/api/users/database/show" -Method GET
```

#### 7️⃣ ОЧИСТКА ВСЕГО КЭША
```bash
Invoke-RestMethod -Uri "http://localhost:8084/api/users/cache/clear" -Method POST
```

### Преимущества Write-Back:
- ✅ **Очень быстрые операции записи** - только в кэш
- ✅ **Группировка операций** - можно объединять несколько операций
- ✅ **Подходит для write-heavy нагрузок** - максимальная производительность записи
- ✅ **Асинхронная обработка** - не блокирует основной поток

### Недостатки:
- ❌ **Риск потери данных** - если кэш упадет до записи в БД
- ❌ **Сложность обеспечения согласованности** - данные могут быть неактуальны
- ❌ **Задержка синхронизации** - данные в БД обновляются с задержкой
- ❌ **Сложная обработка ошибок** - нужно реализовывать механизм повторных попыток

### Когда использовать:
- 🔹 Write-heavy нагрузки
- 🔹 Когда можно позволить себе риск потери данных
- 🔹 Для логов и аналитических данных
- 🔹 Когда нужна максимальная производительность записи

## 📈 Жизненный цикл запроса

```
Операция чтения:
Клиент → Controller → Service 
                    ↓
               Проверить кэш? — НЕТ —→ Загрузить из БД —→ Сохранить в кэш
                    ↓ ДА
               Вернуть из кэша
                    ↓
                 Клиент

Операция записи:
Клиент → Controller → Service 
                    ↓
         Записать в кэш —→ Асинхронно записать в БД (через 5 секунд)
              ↓               ⏱️
           Успех           Фоновый процесс
              ↓               ↓
                 Клиент
```
