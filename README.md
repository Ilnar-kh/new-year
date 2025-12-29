# Дед Мороз — видео поздравление (Telegram Bot)

Проект на Java 21 / Spring Boot 3 для бота, который продаёт и генерирует видео-поздравления через FAL AI и принимает оплату через ЮKassa (СБП).

## Возможности
- Лонг-поллинг Telegram бот с минимальным UX.
- Принимает платежи 99 ₽ через ЮKassa (СБП), с вебхуком и идемпотентностью.
- Хранит состояния и балансы пользователей в PostgreSQL.
- Генерирует видео через FAL AI с опросом статуса, ретраями и таймаутом.
- Dockerfile и `docker-compose.yml` для быстрого запуска.

## Подготовка токенов и ключей
1. **Telegram Bot**: создайте бота через @BotFather, получите `TG_BOT_TOKEN` и `TG_BOT_USERNAME`.
2. **ЮKassa**:
   - Получите `shopId` и `secretKey` в кабинете ЮKassa.
   - Включите СБП в способах оплаты.
   - Создайте вебхук на адрес: `https://<APP_BASE_URL>/yookassa/webhook` с секретом `YOOKASSA_WEBHOOK_SECRET`.
3. **FAL AI**: получите `FAL_API_KEY` и название модели (`FAL_MODEL`).
4. **Sample видео**: отправьте видео вашему боту, прочитайте `file_id` из апдейтов (можно временно включить debug лог бота) и положите в `TG_SAMPLE_VIDEO_FILE_ID`. Если оставить пустым, используется `TG_SAMPLE_VIDEO_URL`.

## Переменные окружения
```
TG_BOT_TOKEN
TG_BOT_USERNAME
TG_SAMPLE_VIDEO_FILE_ID (или TG_SAMPLE_VIDEO_URL)
YOOKASSA_SHOP_ID
YOOKASSA_SECRET_KEY
YOOKASSA_RETURN_URL (например https://t.me/<username>)
YOOKASSA_WEBHOOK_SECRET
FAL_API_KEY
FAL_MODEL (например fal-ai/placeholder-model)
APP_BASE_URL (http://localhost:8080 для локали)
DB_URL (по умолчанию jdbc:postgresql://localhost:5432/dedmoroz)
DB_USER (dedmoroz)
DB_PASSWORD (dedmoroz)
```

## Локальный запуск (без Docker)
```bash
./mvnw spring-boot:run  # или mvn spring-boot:run если mvnw нет
```
Требуются запущенные PostgreSQL с БД `dedmoroz` и применёнными миграциями Flyway (применяются автоматически при старте).

## Запуск через Docker Compose
```bash
docker compose up --build
```
Сервис `app` слушает `8080`, БД поднимается как `db`.

## Настройка вебхуков ЮKassa
- URL: `${APP_BASE_URL}/yookassa/webhook`
- Метод: POST, Content-Type: application/json
- Заголовок: `X-Webhook-Secret: <YOOKASSA_WEBHOOK_SECRET>`
- События: `payment.succeeded` (MVP)

## Переключение на webhook Telegram (опционально)
В коде используется long polling. Для webhook можно поднять публичный HTTPS и зарегистрировать адрес методом `setWebhook` из TelegramBots API, затем отключить регистрацию бота через `TelegramBotsApi` и использовать `WebhookBot` вместо `TelegramLongPollingCommandBot`.

## Типичные проблемы
- **Webhook ЮKassa не приходит**: проверьте публичную доступность `${APP_BASE_URL}`, секрет заголовка, HTTPS и логи приложения.
- **FAL AI таймаут**: сервис автоматически вернёт кредит пользователю. Увеличьте `fal.timeout-seconds` в `application.yml` при необходимости.
- **Нет sample видео**: бот отправит видео по URL. Чтобы использовать `file_id`, отправьте видео боту и смотрите `file_id` в логах обновлений.

## Структура репозитория
- `src/main/java/com/example/dedmoroz` — код приложения (config, telegram, service, client, repository, domain, web, util)
- `src/main/resources/db/migration` — миграции Flyway
- `Dockerfile`, `docker-compose.yml` — контейнеризация
- `application.yml` — конфиг с env placeholders

## Очистка данных
PostgreSQL данные сохраняются в volume `db-data`. Удалите его для полной очистки:
```bash
docker compose down -v
```
