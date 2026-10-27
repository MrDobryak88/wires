# Alpha - платформа подписок для авторов

[![CI](https://github.com/wires-ru/alpha/workflows/CI/badge.svg)](https://github.com/wires-ru/alpha/actions)
[![Java](https://img.shields.io/badge/Java-21-orange.svg)](https://openjdk.org/projects/jdk/21/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.4.5-brightgreen.svg)](https://spring.io/projects/spring-boot)

> Boosty-подобная платформа с гибкими уровнями доступа, предложениями автору и просмотром контента внутри платформы.

## 🎯 Что это

Alpha - это pet-project двух junior-разработчиков. Мы строим аналог Boosty, но с тремя ключевыми отличиями:

1. **Гибкие уровни доступа** - у каждого автора свои тиры, у каждого поста свой минимальный уровень
2. **Предложения автору** - фанат отправляет свою цену, автор принимает/отклоняет/делает контрпредложение
3. **Просмотр внутри платформы** - текст, фото, аудио и видео открываются без перехода на внешние сайты

## ⚡ Быстрый старт (5 минут)

### Требования
- Java 21 (Temurin рекомендуется)
- Maven 3.9+
- Docker Desktop

### Запуск
```bash
# 1. Клонируйте репозиторий
git clone https://github.com/wires-ru/alpha.git
cd alpha

# 2. Запустите Postgres и Redis
docker compose up -d

# 3. Соберите и запустите приложение
mvn clean spring-boot:run -Dspring-boot.run.profiles=local

# 4. Откройте Swagger UI
http://localhost:8080/swagger-ui.html
