Отличное ТЗ! Давайте превратим его в профессиональную документацию для репозитория. Создам три файла: `README.md`, `docs/tz.md` и `docs/decisions.md`.

---

## 📄 1. README.md (корневой файл проекта)

```markdown
# wires — платформа подписок для авторов

[![CI](https://github.com/wires-ru/wires/workflows/CI/badge.svg)](https://github.com/wires-ru/wires/actions)
[![Java](https://img.shields.io/badge/Java-21-orange.svg)](https://openjdk.org/projects/jdk/21/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.4.5-brightgreen.svg)](https://spring.io/projects/spring-boot)

> Boosty-подобная платформа с гибкими уровнями доступа, предложениями автору и просмотром контента внутри платформы.

## 🎯 Что это

wires — это pet-project двух junior-разработчиков. Мы строим аналог Boosty, но с тремя ключевыми отличиями:

1. **Гибкие уровни доступа** — у каждого автора свои тиры, у каждого поста свой минимальный уровень
2. **Предложения автору** — фанат отправляет свою цену, автор принимает/отклоняет/делает контрпредложение
3. **Просмотр внутри платформы** — текст, фото, аудио и видео открываются без перехода на внешние сайты

## ⚡ Быстрый старт (5 минут)

### Требования
- Java 21 (Temurin рекомендуется)
- Maven 3.9+
- Docker Desktop

### Запуск
```bash
# 1. Клонируйте репозиторий
git clone https://github.com/wires-ru/wires.git
cd wires

# 2. Запустите Postgres и Redis
docker compose up -d

# 3. Соберите и запустите приложение
mvn clean spring-boot:run -Dspring-boot.run.profiles=local

# 4. Откройте Swagger UI
http://localhost:8080/swagger-ui.html
```

Приложение ответит на `http://localhost:8080/actuator/health` → `{"status":"UP"}`

## 🏗️ Архитектура

**Монолит с пакетами по доменам.** Каждый домен (`auth`, `tier`, `subscription`, `post`, `offer`, `payment`) содержит свои controller/service/repository/entity. Модули общаются только через сервисы, репозитории чужих модулей не трогаем.

```
src/main/java/ru/wires/wires/
├── auth/          # JWT, регистрация, логин
├── user/          # профили пользователей
├── creator/       # страницы авторов
├── tier/          # уровни подписки
├── subscription/  # подписки + AccessService (ядро доступа)
├── post/          # посты с уровнем доступа
├── media/         # загрузка/выдача файлов
├── offer/         # предложения (конечный автомат)
├── payment/       # интерфейс платежей + заглушка
└── common/        # ошибки, утилиты
```

**Почему так?** При росте проекта структура по слоям (`controller/service/repository`) размазывает одну фичу по пяти папкам. Доменная упаковка позволяет двум разработчикам работать параллельно без конфликтов.

## 🧪 Тестирование

```bash
# Все тесты
mvn verify

# Только юнит-тесты
mvn test

# Интеграционные тесты (требуют Docker)
mvn verify -Pintegration
```

**Что тестируем:**
- Юнит-тесты: правило доступа (`AccessService`), переходы статусов предложений (`OfferStateMachine`)
- Интеграционные: API эндпоинты, репозитории (Testcontainers)

## 📚 Документация

- [Техническое задание](docs/tz.md) — полное описание проекта, ограничений и плана работ
- [Журнал решений](docs/decisions.md) — почему выбрали именно эти технологии и подходы
- [API документация](http://localhost:8080/swagger-ui.html) — доступна при локальном запуске

## 🛠️ Стек

| Компонент | Технология |
|-----------|------------|
| Language | Java 21 (LTS) |
| Framework | Spring Boot 3.4.5 |
| Database | PostgreSQL 16 + Flyway |
| Cache | Redis 7 (опционально) |
| Security | Spring Security + JWT |
| API Docs | springdoc-openapi (Swagger UI) |
| Build | Maven 3.9 |
| CI | GitHub Actions |
| Infra | Docker Compose (только для БД) |

## 🤝 Как мы работаем (два junior-разработчика)

- **Задачи:** GitHub Projects (колонки To Do / In Progress / Done)
- **Ветки:** `feature/название-задачи` от `main`
- **PR:** обязательно ревью второго разработчика, зелёный CI
- **Созвоны:** раз в неделю, 15 минут — что сделано, что мешает

**Распределение:**
- Разработчик A: `auth`, `subscription`, `payment`, CI/инфраструктура
- Разработчик B: `tier`, `post`, `media`, `offer`

## 🚧 Статус проекта

**Текущий этап:** 0. Фундамент (неделя 1)

- [x] Структура проекта
- [x] Docker Compose для Postgres/Redis
- [ ] `mvn clean verify` проходит
- [ ] Приложение стартует и отвечает на `/actuator/health`
- [ ] CI на GitHub Actions

См. [ТЗ](docs/tz.md) для полного плана работ.

## ⚠️ Ограничения (честно)

Это pet-project двух джунов. Мы **не делаем**:
- Микросервисы, Kafka, Kubernetes
- Реальные платежи (только заглушка)
- Модерацию контента
- Мобильное приложение

Цель — довести MVP до конца, а не построить идеальный enterprise.

## 📝 Лицензия

MIT — используйте как хотите, но помните что это учебный проект.

---

**Вопросы?** Откройте issue или напишите в Telegram (контакты в профилях GitHub).
```

---

## 📋 2. docs/tz.md (полное техническое задание)

```markdown
# Техническое задание: платформа подписок wires

> Версия: 1.0  
> Дата: 27 октября 2026  
> Авторы: команда wires-ru (2 junior-разработчика)

---

## 1. Цель и рамки проекта

### Что строим
Платформу, где автор публикует контент, а фанаты получают к нему доступ по уровням подписки. Аналог Boosty, но с расширенным функционалом.

### Ключевые отличия от Boosty
1. **Гибкие уровни доступа** — у каждого автора свои тиры с числовым уровнем, у каждого поста свой минимальный тир
2. **Предложения автору** — фанат отправляет предложение со своей суммой, автор принимает/отклоняет/делает контрпредложение
3. **Просмотр внутри платформы** — весь контент (текст, фото, аудио, видео) открывается без перехода на внешние сайты

### Реальные ограничения
- **Команда:** 2 junior-разработчика
- **Цель:** довести MVP до конца, а не построить идеальный enterprise
- **Не делаем:** микросервисы, Kafka, Kubernetes, GraphQL, свой стриминг, мобильное приложение
- **«Рабочая обстановка»:** нужна, но в лёгком виде (GitHub Projects, PR, CI, базовые тесты)

---

## 2. Текущее состояние (на 27.10.2026)

### Что работает
- ✅ Структура проекта создана
- ✅ Maven Wrapper настроен
- ✅ Spring Boot 3.4.5 запускается (Tomcat на порту 8080)

### Что сломано (задача №0)
- ❌ **Версии разъехались:** в pom.xml были Spring Boot 4.1.2 + Java 25, сборка идёт на Boot 3.4.5 + Java 21, запуск на Java 24
- ❌ **Ошибка компиляции тестов:** `org.testcontainers.postgresql` не найден (несовместимость версий)
- ❌ **Нет datasource:** приложение падает при старте без конфигурации БД
- ❌ **Лишние блоки в pom.xml:** пустые `licenses`, `developers`, `scm`

### Задача №0 (до первой фичи)
**Критерий готовности:** `mvn clean verify` проходит, приложение стартует, `/actuator/health` отвечает `{"status":"UP"}`.

Пока этого нет — остальное не начинаем.

---

## 3. Принятые технологические решения

| Тема | Решение | Почему |
|------|---------|--------|
| **Архитектура** | Монолит, пакеты по доменам | Один деплой, одна точка отладки, параллельная работа без конфликтов |
| **API** | REST + Swagger (springdoc) | Документация бесплатно, просто для джунов |
| **БД** | PostgreSQL 16 + Flyway | Схема только через миграции, никаких `ddl-auto=update` |
| **Безопасность** | Spring Security + JWT | Без Keycloak (слишком сложно для старта) |
| **Кэш** | Redis, опционально | Не блокирует MVP, добавим когда будет нагрузка |
| **Брокер сообщений** | Не нужен | Фоновые задачи через `@Async`, Kafka избыточна |
| **Файлы** | Локальная папка + интерфейс `FileStorage` | Позже заменим на MinIO без правки бизнес-кода |
| **Платежи** | Интерфейс + заглушка «всегда успешно» | YooKassa/Stripe в тестовом режиме подключим позже |
| **Сборка** | Maven 3.9 | Gradle сложнее для новичков |
| **Задачи** | GitHub Projects | Jira — оверкилл для двух человек |
| **CI** | GitHub Actions: `mvn verify` на PR | Минимум настройки, бесплатно |
| **Docker** | Compose только для Postgres/Redis | Приложение через `mvn spring-boot:run`, логи перед глазами |
| **Тесты** | Testcontainers для интеграционных, JUnit для юнитов | «Реальная обстановка», но без фанатизма |
| **Фронтенд** | Чистый REST + простая статика | Без отдельного репозитория, решение примем позже |

### Спорные места (выбрали за вас)
- **Docker:** компромисс между «всё в Docker» и «без Docker». БД в контейнере, приложение локально.
- **Тесты:** Testcontainers оставлены, но только для интеграционных тестов. Юнит-тесты — для бизнес-логики.
- **Фронтенд:** отложен. Сначала бэкенд как чистый API.

---

## 4. Архитектурные решения

### 4.1. Пакеты по доменам, а не по слоям

**Проблема слоёв:** структура `controller/service/repository` размазывает одну фичу по пяти папкам. Два разработчика постоянно конфликтуют в одних файлах.

**Решение:** каждый домен в своей папке:
```
post/
├── PostController.java
├── PostService.java
├── PostRepository.java
├── Post.java
└── dto/
```

**Выгода:** границы модулей видны сразу, можно работать параллельно, из такого монолита потом легко выделить микросервис.

### 4.2. Правило границ модулей

Модуль обращается к другому **только через его service**. Репозитории и entity чужого модуля — приватные.

```java
// ✅ Правильно
@Service
public class PostService {
    private final TierService tierService; // обращение через сервис
    
    public void createPost(...) {
        Tier tier = tierService.getById(tierId);
    }
}

// ❌ Неправильно
@Service
public class PostService {
    private final TierRepository tierRepo; // прямой доступ к чужому репозиторию
}
```

### 4.3. Модель доступа (ядро проекта)

**Сущности:**
- **Tier** — уровень подписки автора. Имеет числовой `level` (чем выше, тем больше доступа).
- **Post** — пост. Имеет `minTierId` (минимальный тир для доступа) или `null` (публичный).
- **Subscription** — связь фаната, автора и тира. Имеет `status` и `expiresAt`.

**Правило доступа (в одном месте — `AccessService`):**
```
доступ_разрешён = 
    пост.публичный 
    ИЛИ (подписка.активна 
         И подписка.tier.level >= пост.minTier.level)
```

**Почему в одном месте:** правило можно покрыть тестами и поменять в одной точке. Все модули спрашивают у `AccessService`, никто не лезет в логику напрямую.

### 4.4. Предложения как конечный автомат

**Статусы:** `PENDING → ACCEPTED | REJECTED | COUNTERED | EXPIRED | CANCELLED`

**Допустимые переходы:**
```
PENDING   → ACCEPTED, REJECTED, COUNTERED, EXPIRED, CANCELLED
COUNTERED → ACCEPTED, REJECTED, EXPIRED
ACCEPTED  → (терминальное)
REJECTED  → (терминальное)
EXPIRED   → (терминальное)
CANCELLED → (терминальное)
```

**Реализация:** `OfferStateMachine` с явной картой переходов. Запрещённые переходы кидают `IllegalStateException`.

**Защита от спама:** лимит 5 предложений в сутки на фаната.

**Почему так:** это самый чистый кусок для юнит-тестов. Вся логика в одном классе, без зависимостей от БД.

### 4.5. Файлы и просмотр внутри платформы

**Принцип:** файл не отдаётся как публичная статика. Он идёт через эндпоинт, который сначала проверяет доступ через `AccessService`.

**MVP:** отдаём файл как есть, с поддержкой частичной загрузки (Range requests). Транскодинг видео (HLS) — вне рамок MVP.

**Плееры:** встроенные HTML5 (`<video>`, `<audio>`, `<img>`). Никаких внешних iframe.

### 4.6. Платежи за интерфейсом

```java
public interface PaymentService {
    PaymentResult charge(UUID userId, BigDecimal amount, String description);
}

@Service
public class StubPaymentService implements PaymentService {
    public PaymentResult charge(...) {
        return PaymentResult.success("stub-" + UUID.randomUUID());
    }
}
```

**Выгода:** когда подключим YooKassa/Stripe, бизнес-код (`SubscriptionService`, `OfferService`) не изменится. Просто подменим реализацию.

---

## 5. Структура проекта

```
wires/
├── .github/
│   ├── workflows/ci.yml            # mvn verify на каждый PR
│   └── pull_request_template.md    # чек-лист для ревью
├── docs/
│   ├── tz.md                       # этот файл
│   └── decisions.md                # журнал архитектурных решений
├── docker-compose.yml              # Postgres + Redis
├── pom.xml
├── README.md
└── src/
    ├── main/
    │   ├── java/ru/wires/wires/
    │   │   ├── wiresApplication.java
    │   │   ├── common/             # GlobalExceptionHandler, BaseEntity
    │   │   ├── config/             # SecurityConfig, OpenApiConfig, CorsConfig
    │   │   ├── auth/               # AuthController, JwtService
    │   │   ├── user/               # User, UserService
    │   │   ├── creator/            # CreatorProfile
    │   │   ├── tier/               # Tier, TierService
    │   │   ├── subscription/       # Subscription, AccessService ⭐
    │   │   ├── post/               # Post, PostService
    │   │   ├── media/              # FileStorage, MediaController
    │   │   ├── offer/              # Offer, OfferStateMachine ⭐
    │   │   └── payment/            # PaymentService, StubPaymentService
    │   └── resources/
    │       ├── application.yml
    │       ├── application-local.yml
    │       └── db/migration/       # V1__init.sql, V2__...
    └── test/
        ├── java/ru/wires/wires/    # зеркало main
        └── resources/
```

---

## 6. Функциональные требования (MVP)

### 6.1. Аккаунты
- Регистрация (email + пароль)
- Логин → JWT токен
- Роли: `FAN`, `CREATOR`
- Пароли в bcrypt хэше

### 6.2. Автор
- Профиль (имя, описание, аватар)
- CRUD своих тиров (название, цена, level)
- Просмотр своих подписчиков

### 6.3. Контент
- Создание поста (текст + фото)
- Выбор минимального тира (или публичный)
- Редактирование/удаление своих постов

### 6.4. Подписка
- Фанат оформляет подписку на тир автора
- Оплата через заглушку (`StubPaymentService`)
- Срок подписки (например, 30 дней)
- Отмена подписки

### 6.5. Лента и просмотр
- Фанат видит посты автора
- Посты без доступа показываются как «закрытые» (без содержимого)
- Встроенный просмотр фото/текста

### 6.6. Предложения
- Фанат отправляет предложение (сумма + сообщение)
- Автор видит входящие предложения
- Действия: принять / отклонить / контрпредложение
- Срок жизни предложения (7 дней)
- Лимит: 5 предложений в сутки на фаната

### 6.7. Документация API
- Swagger UI на `/swagger-ui.html`
- OpenAPI spec на `/v3/api-docs`

### Нефункциональные требования
- Пароли в хэше (bcrypt)
- Закрытый контент не отдаётся без проверки `AccessService`
- Схема БД только через Flyway миграции
- CI зелёный на каждый PR
- Покрытие юнит-тестами: `AccessService`, `OfferStateMachine`

---

## 7. План работ

| Этап | Недели | Результат | Критерий готовности |
|------|--------|-----------|---------------------|
| **0. Фундамент** | 1 | Версии зафиксированы, Postgres в Compose, приложение стартует, CI запущен | `mvn clean verify` зелёный, `/actuator/health` → UP |
| **1. Аккаунты** | 2–3 | Регистрация, логин, JWT, защищённые эндпоинты | Интеграционный тест логина проходит |
| **2. Тиры и посты** | 3–4 | CRUD тиров и постов с уровнем доступа | Автор создаёт пост с тиром через Swagger |
| **3. Подписки и доступ** | 5–6 | Подписка + `AccessService`, закрытый контент не виден | Юнит-тесты на все ветки правила доступа |
| **4. Файлы** | 6–7 | Загрузка фото, защищённая выдача, просмотр | Файл недоступен без подписки |
| **5. Предложения** | 7–9 | Конечный автомат, лимиты, сроки | Тесты на допустимые/запрещённые переходы |
| **6. Деплой** | 9–10 | VPS, README, багфиксы | Приложение доступно по ссылке |
| **Потом** | — | Аудио/видео плеер, MinIO, Redis-кэш, реальный шлюз, поиск | По желанию |

---

## 8. Как делим работу (два junior)

### Принцип: вертикальные срезы
Каждый берёт фичу целиком (от БД до эндпоинта), а не «ты бэкенд, я фронтенд». Так оба проходят весь стек.

### Распределение
- **Разработчик A:** фундамент + CI, `auth`, `subscription`/`AccessService`, `payment`
- **Разработчик B:** `tier`, `post`, `media`, затем `offer`

### Процесс
1. Задача → issue в GitHub Projects (To Do / In Progress / Done)
2. Ветка `feature/название` от `main`
3. PR → ревью второго разработчика → merge только при зелёном CI
4. Созвон раз в неделю (15 мин): что сделано, что мешает, что дальше
5. Размер задачи: 1–3 дня. Больше — дробить.

---

## 9. Риски и что НЕ делаем

### Риски
- **Раздувание объёма.** Любая идея вне раздела 6 → в «Потом», не в текущий этап.
- **Застрять на инфраструктуре.** Этап 0 = 1 неделя. Не заработало → упрощаем, не копаем глубже.
- **Деньги и право.** Реальные платежи, налоги, модерация — вне MVP. Перед настоящим запуском потребуется отдельная проработка.

### Что НЕ делаем (явно)
- Микросервисы
- Kafka / RabbitMQ
- Kubernetes
- GraphQL
- HLS-транскодинг видео
- Мобильное приложение
- Реальные платежи (только заглушка)
- Модерацию контента
- Elasticsearch / полнотекстовый поиск
- WebSocket / real-time уведомления

---

## 10. Определение готовности MVP

MVP готов, когда:
1. Автор регистрируется, создаёт тиры, публикует пост с ограничением по тиру
2. Фанат регистрируется, оформляет подписку (через заглушку), видит закрытый пост
3. Фанат отправляет предложение, автор принимает → деньги «списываются» через заглушку
4. Всё работает на VPS, ссылка открывается в браузере
5. README позволяет запустить проект за 5 минут

**Не критично для MVP:** красивый UI, аудио/видео плеер, реальные платежи, поиск, уведомления.

---

## Приложение A: Схема БД (V1 миграция)

```sql
-- Tiers
CREATE TABLE tiers (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(100) NOT NULL,
    level INTEGER NOT NULL,
    price NUMERIC(10,2) NOT NULL,
    creator_id UUID NOT NULL,
    active BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMP NOT NULL DEFAULT now()
);

-- Subscriptions
CREATE TABLE subscriptions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    fan_id UUID NOT NULL,
    creator_id UUID NOT NULL,
    tier_id UUID NOT NULL REFERENCES tiers(id),
    status VARCHAR(20) NOT NULL,
    expires_at TIMESTAMP NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT now()
);
CREATE INDEX idx_subscriptions_access ON subscriptions(fan_id, creator_id, status, expires_at);

-- Posts
CREATE TABLE posts (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    creator_id UUID NOT NULL,
    title VARCHAR(200) NOT NULL,
    content TEXT,
    min_tier_id UUID REFERENCES tiers(id),  -- NULL = публичный
    created_at TIMESTAMP NOT NULL DEFAULT now()
);

-- Offers
CREATE TABLE offers (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    fan_id UUID NOT NULL,
    creator_id UUID NOT NULL,
    amount NUMERIC(10,2) NOT NULL,
    message TEXT,
    status VARCHAR(20) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    expires_at TIMESTAMP NOT NULL
);
CREATE INDEX idx_offers_fan_daily ON offers(fan_id, created_at);
```

