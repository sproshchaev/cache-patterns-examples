# Write-Around Cache Pattern

Это максимально простой пример, демонстрирующий паттерн **Write-Around Cache**. 

Данные записываются **только в базу данных**, минуя кэш, что предотвращает "загрязнение" кэша редко 
используемыми данными.

Паттерн Write-Around состоит из 2 основных принципов:
1. **Чтение** - как в Cache-Aside (проверка кэша → загрузка из БД → сохранение в кэш)
2. **Запись** - данные записываются ТОЛЬКО в БД, кэш инвалидируется

## 📊 Что демонстрирует пример:

```
1️⃣ ЧТЕНИЕ: CACHE MISS → загрузка из БД → сохранение в кэш
2️⃣ ПОВТОРНОЕ ЧТЕНИЕ: CACHE HIT → мгновенное получение из кэша
3️⃣ ЗАПИСЬ: WRITE-AROUND → запись ТОЛЬКО в БД → инвалидация кэша
4️⃣ ПЕРВОЕ ЧТЕНИЕ ПОСЛЕ ЗАПИСИ: CACHE MISS → загрузка из БД
```

### macOS / Linux Команды
#### 1️⃣ ПОЛУЧИТЬ ПОЛЬЗОВАТЕЛЯ (чтение)
```bash
curl "http://localhost:8083/api/users/1"
```

#### 2️⃣ СОЗДАТЬ НОВОГО ПОЛЬЗОВАТЕЛЯ (Write-Around)
```bash
curl -X POST "http://localhost:8083/api/users" \
  -H "Content-Type: application/json" \
  -d '{"name":"David","email":"david@example.com"}'
```

#### 3️⃣ ОБНОВИТЬ ПОЛЬЗОВАТЕЛЯ (Write-Around)
```bash
curl -X PUT "http://localhost:8083/api/users/1?name=Alice%20Updated&email=alice.updated@example.com"
```

#### 4️⃣ УДАЛИТЬ ПОЛЬЗОВАТЕЛЯ (Write-Around)
```bash
curl -X DELETE "http://localhost:8083/api/users/1"
```

#### 5️⃣ ПРОСМОТР СОСТОЯНИЯ КЭША
```bash
curl "http://localhost:8083/api/users/cache/show"
```

#### 6️⃣ ОЧИСТКА ВСЕГО КЭША
```bash
curl -X POST "http://localhost:8083/api/users/cache/clear"
```

### Windows Команды
#### 1️⃣ ПОЛУЧИТЬ ПОЛЬЗОВАТЕЛЯ (чтение)
```bash
Invoke-RestMethod -Uri "http://localhost:8083/api/users/1" -Method GET
```

#### 2️⃣ СОЗДАТЬ НОВОГО ПОЛЬЗОВАТЕЛЯ (Write-Around)
```bash
Invoke-RestMethod -Uri "http://localhost:8083/api/users" -Method POST -Body '{"name":"David","email":"david@example.com"}' -ContentType "application/json"
```

#### 3️⃣ ОБНОВИТЬ ПОЛЬЗОВАТЕЛЯ (Write-Around)
```bash
Invoke-RestMethod -Uri "http://localhost:8083/api/users/1?name=Alice%20Updated&email=alice.updated@example.com" -Method PUT
```

#### 4️⃣ УДАЛИТЬ ПОЛЬЗОВАТЕЛЯ (Write-Around)
```bash
Invoke-RestMethod -Uri "http://localhost:8083/api/users/1" -Method DELETE
```

#### 5️⃣ ПРОСМОТР СОСТОЯНИЯ КЭША
```bash
Invoke-RestMethod -Uri "http://localhost:8083/api/users/cache/show" -Method GET
```

#### 6️⃣ ОЧИСТКА ВСЕГО КЭША
```bash
Invoke-RestMethod -Uri "http://localhost:8083/api/users/cache/clear" -Method POST
```

### Преимущества Write-Around:
- ✅ **Избежание загрязнения кэша** - редко читаемые данные не попадают в кэш
- ✅ **Экономия памяти кэша** - только часто используемые данные кэшируются
- ✅ **Подходит для write-heavy нагрузок** - когда много записей, но мало чтений

### Недостатки:
- ❌ **Кэш-промахи при первом чтении** - после записи данные не в кэше
- ❌ **Задержка при первом доступе** - первый запрос после записи будет медленнее
- ❌ **Может быть неэффективным** - если данные часто обновляются и читаются

### Когда использовать:
- 🔹 Когда данные редко читаются после записи
- 🔹 Для write-heavy нагрузок с редким чтением
- 🔹 Когда нужно избежать загрязнения кэша
- 🔹 Для логов и аналитических данных

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
         Записать в БД —→ Инвалидировать кэш
              ↓               ↓
           Успех           Успех
              ↓               ↓
                 Клиент
```
