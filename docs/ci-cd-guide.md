
---

## ⚙️ 3. docs/ci-cd-guide.md (CI/CD для новичков)

```markdown
# CI/CD для двух junior-разработчиков

> Автоматизация без боли. GitHub Actions + Maven = зелёный билд на каждый PR.

---

## 🤔 Что такое CI/CD

**CI (Continuous Integration)** — автоматическая проверка кода при каждом изменении:
- Компиляция
- Тесты
- Статический анализ

**CD (Continuous Deployment)** — автоматический деплой на сервер.

**Для MVP:** делаем только CI. CD (автодеплой) добавим на этапе 6.

---

## 🚀 GitHub Actions за 5 минут

### Шаг 1: Создайте файл workflow
Создайте файл `.github/workflows/ci.yml`:

```yaml
name: CI

on:
  push:
    branches: [ main ]
  pull_request:
    branches: [ main ]

jobs:
  build:
    runs-on: ubuntu-latest
    
    services:
      postgres:
        image: postgres:16
        env:
          POSTGRES_DB: boosty_test
          POSTGRES_PASSWORD: test
          POSTGRES_USER: test
        ports:
          - 5432:5432
        options: >-
          --health-cmd pg_isready
          --health-interval 10s
          --health-timeout 5s
          --health-retries 5
      
      redis:
        image: redis:7-alpine
        ports:
          - 6379:6379
        options: >-
          --health-cmd "redis-cli ping"
          --health-interval 10s
          --health-timeout 5s
          --health-retries 5

    steps:
      - name: Checkout code
        uses: actions/checkout@v4

      - name: Set up JDK 21
        uses: actions/setup-java@v4
        with:
          java-version: '21'
          distribution: 'temurin'
          cache: 'maven'

      - name: Build and test
        run: mvn clean verify
        env:
          SPRING_DATASOURCE_URL: jdbc:postgresql://localhost:5432/boosty_test
          SPRING_DATASOURCE_USERNAME: test
          SPRING_DATASOURCE_PASSWORD: test
          SPRING_DATA_REDIS_HOST: localhost
          SPRING_DATA_REDIS_PORT: 6379

      - name: Upload test results
        uses: actions/upload-artifact@v4
        if: always()
        with:
          name: test-results
          path: target/surefire-reports/
```

Шаг 2: Закоммитьте и запушьте
```bash
git add .github/workflows/ci.yml
git commit -m "ci: добавить GitHub Actions workflow"
git push origin main
```
Шаг 3: Смотрите результат
Откройте GitHub → репозиторий → вкладка Actions
Увидите запуск workflow
Зелёная галочка ✅ = успех, красный крест ❌ = провал

🔍 Что делает этот workflow
1. Checkout code          → скачивает ваш код
2. Set up JDK 21          → устанавливает Java 21
3. Start Postgres         → поднимает PostgreSQL в Docker
4. Start Redis            → поднимает Redis в Docker
5. mvn clean verify       → компилирует + запускает тесты
6. Upload test results    → сохраняет отчёты о тестах

🛠️ Настройка защиты ветки (обязательно!)
Чтобы CI блокировал мерж при провале:
Settings → Branches → Add rule
Branch name pattern: main
Включите:
✅ Require a pull request before merging
✅ Require approvals: 1
✅ Require status checks to pass before merging
Выберите build из списка
✅ Include administrators
Результат: PR нельзя смержить если CI красный.

🧪 Локальный запуск CI
Перед пушем проверьте локально:

```
# Запустите Postgres и Redis
docker compose up -d

# Запустите те же команды что в CI
mvn clean verify

# Остановите контейнеры
docker compose down
```
#### 🔧 Расширение CI (потом)

Добавление статического анализа

Проверка покрытия тестами

Сборка Docker образа

Не добавляйте пока. MVP не требует этого.

Секреты настройте в Settings → Secrets and variables → Actions:
VPS_HOST — IP вашего сервера
VPS_USER — пользователь SSH
SSH_KEY — приватный ключ SSH