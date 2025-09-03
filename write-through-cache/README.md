# Write-Through Cache Pattern

Это максимально простой пример, демонстрирующий паттерн **Write-Through Cache**. 
Данные записываются **одновременно** и в кэш, и в базу данных, что гарантирует согласованность данных.

Паттерн Write-Through состоит из 2 основных принципов:
1. **Чтение** - как в Cache-Aside (проверка кэша → загрузка из БД → сохранение в кэш)
2. **Запись** - данные записываются одновременно и в кэш, и в БД

## 📊 Что демонстрирует пример:

```
1️⃣ ЧТЕНИЕ: CACHE MISS → загрузка из БД → сохранение в кэш
2️⃣ ПОВТОРНОЕ ЧТЕНИЕ: CACHE HIT → мгновенное получение из кэша
3️⃣ ЗАПИСЬ: WRITE-THROUGH → запись в БД → запись в кэш
4️⃣ ОБНОВЛЕНИЕ: WRITE-THROUGH → обновление в БД → обновление в кэше
```

### macOS / Linux Команды
#### 1️⃣ ПОЛУЧИТЬ ПОЛЬЗОВАТЕЛЯ (чтение)
```bash
curl "http://localhost:8082/api/users/1"
```

#### 2️⃣ СОЗДАТЬ НОВОГО ПОЛЬЗОВАТЕЛЯ (Write-Through)
```bash
curl -X POST "http://localhost:8082/api/users" \
  -H "Content-Type: application/json" \
  -d '{"name":"David","email":"david@example.com"}'
```

#### 3️⃣ ОБНОВИТЬ ПОЛЬЗОВАТЕЛЯ (Write-Through)
```bash
curl -X PUT "http://localhost:8082/api/users/1?name=Alice%20Updated&email=alice.updated@example.com"
```

#### 4️⃣ УДАЛИТЬ ПОЛЬЗОВАТЕЛЯ (Write-Through)
```bash
curl -X DELETE "http://localhost:8082/api/users/1"
```

#### 5️⃣ ПРОСМОТР СОСТОЯНИЯ КЭША
```bash
curl "http://localhost:8082/api/users/cache/show"
```

#### 6️⃣ ОЧИСТКА ВСЕГО КЭША
```bash
curl -X POST "http://localhost:8082/api/users/cache/clear"
```

### Windows Команды
#### 1️⃣ ПОЛУЧИТЬ ПОЛЬЗОВАТЕЛЯ (чтение)
```bash
Invoke-RestMethod -Uri "http://localhost:8082/api/users/1" -Method GET
```

#### 2️⃣ СОЗДАТЬ НОВОГО ПОЛЬЗОВАТЕЛЯ (Write-Through)
```bash
Invoke-RestMethod -Uri "http://localhost:8082/api/users" -Method POST -Body '{"name":"David","email":"david@example.com"}' -ContentType "application/json"
```

#### 3️⃣ ОБНОВИТЬ ПОЛЬЗОВАТЕЛЯ (Write-Through)
```bash
Invoke-RestMethod -Uri "http://localhost:8082/api/users/1?name=Alice%20Updated&email=alice.updated@example.com" -Method PUT
```

#### 4️⃣ УДАЛИТЬ ПОЛЬЗОВАТЕЛЯ (Write-Through)
```bash
Invoke-RestMethod -Uri "http://localhost:8082/api/users/1" -Method DELETE
```

#### 5️⃣ ПРОСМОТР СОСТОЯНИЯ КЭША
```bash
Invoke-RestMethod -Uri "http://localhost:8082/api/users/cache/show" -Method GET
```

#### 6️⃣ ОЧИСТКА ВСЕГО КЭША
```bash
Invoke-RestMethod -Uri "http://localhost:8082/api/users/cache/clear" -Method POST
```

### Преимущества Write-Through:
- ✅ **Высокая согласованность** - данные всегда синхронизированы
- ✅ **Быстрый доступ** - закэшированные данные доступны мгновенно
- ✅ **Надежность** - данные сохранены и в БД, и в кэше

### Недостатки:
- ❌ **Медленные операции записи** - нужно записывать в два места
- ❌ **Избыточное кэширование** - не все данные могут понадобиться
- ❌ **Сложность обработки ошибок** - при сбое нужно откатывать обе операции

### Когда использовать:
- 🔹 Когда требуется высокая согласованность данных
- 🔹 Для критически важных данных
- 🔹 Когда можно позволить себе замедление записи

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
         Записать в БД —→ Записать в кэш
              ↓               ↓
           Успех           Успех
              ↓               ↓
                 Клиент
```

