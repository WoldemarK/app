DOCKER_COMPOSE = docker-compose
COMPOSE_FILE ?= docker-compose.yml
NEXUS_URL = http://localhost:8082/service/rest/v1/status
KEYCLOAK_URL = http://localhost:9000/health/ready
PERSONS_API_URL = http://localhost:8092/actuator/health

# Инфраструктурные сервисы БЕЗ nexus (он стартует первым отдельно)
INFRA_SERVICES_NO_NEXUS = keycloak person-postgres prometheus grafana tempo loki zookeeper kafka1 schema-registry postgres-ds0 postgres-ds1
ALL_INFRA_SERVICES = nexus $(INFRA_SERVICES_NO_NEXUS)

# Кроссплатформенные команды ожидания
ifeq ($(OS),Windows_NT)
	WAIT_NEXUS_CMD = powershell -Command "while ($$true) { try { Invoke-WebRequest -UseBasicParsing -Uri $(NEXUS_URL) -ErrorAction Stop; break } catch { Write-Host 'Nexus not ready...'; Start-Sleep -Seconds 5 } }"
	WAIT_KEYCLOAK_CMD = powershell -Command "while ($$true) { try { Invoke-WebRequest -UseBasicParsing -Uri $(KEYCLOAK_URL) -ErrorAction Stop; break } catch { Write-Host 'Keycloak not ready...'; Start-Sleep -Seconds 5 } }"
	WAIT_PERSONS_API_CMD = powershell -Command "while ($$true) { try { Invoke-WebRequest -UseBasicParsing -Uri $(PERSONS_API_URL) -ErrorAction Stop; break } catch { Write-Host 'Persons API not ready...'; Start-Sleep -Seconds 5 } }"
else
	WAIT_NEXUS_CMD = until curl -sf $(NEXUS_URL) > /dev/null 2>&1; do echo "⏳ Nexus not ready, sleeping..."; sleep 5; done
	WAIT_KEYCLOAK_CMD = until curl -sf $(KEYCLOAK_URL) > /dev/null 2>&1; do echo "⏳ Keycloak not ready, sleeping..."; sleep 5; done
	WAIT_PERSONS_API_CMD = until curl -sf $(PERSONS_API_URL) > /dev/null 2>&1; do echo "⏳ Persons API not ready, sleeping..."; sleep 5; done
endif

.PHONY: all nexus infra build-artifacts start-apps start stop clean logs rebuild \
		wait-nexus wait-keycloak wait-persons-api status logs-% restart-% validate help

# ==================== ОСНОВНОЙ ВОРКФЛОУ ====================
# 1. Сначала стартуем ТОЛЬКО nexus
nexus:
	@echo "🚀 Starting Nexus (standalone)..."
	$(DOCKER_COMPOSE) -f $(COMPOSE_FILE) up -d nexus
	@echo "⏳ Waiting for Nexus to be ready at $(NEXUS_URL)..."
	@$(WAIT_NEXUS_CMD)
	@echo "✅ Nexus is ready!"

# 2. Собираем артефакты (теперь можно — Nexus доступен как репозиторий)
build-artifacts: nexus
	@echo "📦 Building application artifacts (using Nexus repository)..."
	$(DOCKER_COMPOSE) -f $(COMPOSE_FILE) build persons-api individuals-api transaction-service-api --no-cache
	@echo "✅ Artifacts built successfully"

# 3. Запускаем остальную инфраструктуру (без nexus — он уже работает)
infra: nexus
	@echo "🏗️ Starting remaining infrastructure services..."
	$(DOCKER_COMPOSE) -f $(COMPOSE_FILE) up -d $(INFRA_SERVICES_NO_NEXUS)
	@echo "⏳ Waiting for Keycloak..."
	@$(WAIT_KEYCLOAK_CMD)
	@echo "✅ Keycloak is ready"
	@echo "✅ Infrastructure services are running"

# 4. Запускаем прикладные сервисы
start-apps: build-artifacts infra
	@echo "🚀 Starting application services..."
	$(DOCKER_COMPOSE) -f $(COMPOSE_FILE) up -d persons-api individuals-api transaction-service-api
	@echo "⏳ Waiting for Persons API..."
	@$(WAIT_PERSONS_API_CMD)
	@echo "✅ Application services are ready!"

# Полный старт: nexus → сборка → инфраструктура → приложения
all: start-apps
	@echo "✨ Full environment is ready!"
	@echo "   Nexus:      http://localhost:8082"
	@echo "   Keycloak:   http://localhost:8080"
	@echo "   Grafana:    http://localhost:3000"
	@echo "   Persons API: http://localhost:8092"

# ==================== УПРАВЛЕНИЕ ====================
start: all

stop:
	$(DOCKER_COMPOSE) -f $(COMPOSE_FILE) stop

clean: stop
	$(DOCKER_COMPOSE) -f $(COMPOSE_FILE) down -v --remove-orphans
	docker volume prune -f 2>/dev/null || true
	rm -rf ./person-service/build ./individuals-api/build ./transaction-service-api/build
	@echo "🧹 Environment cleaned"

logs:
	$(DOCKER_COMPOSE) -f $(COMPOSE_FILE) logs -f --tail=200

logs-%:
	$(DOCKER_COMPOSE) -f $(COMPOSE_FILE) logs -f --tail=200 $*

rebuild: clean all

# ==================== УТИЛИТЫ ОЖИДАНИЯ ====================
wait-nexus: nexus

wait-keycloak:
	@$(WAIT_KEYCLOAK_CMD)

wait-persons-api:
	@$(WAIT_PERSONS_API_CMD)

# ==================== ДИАГНОСТИКА ====================
status:
	@echo "📊 Service Status:"
	@$(DOCKER_COMPOSE) -f $(COMPOSE_FILE) ps --format "table {{.Name}}\t{{.State}}\t{{.Ports}}"

validate:
	@echo "🔍 Validating docker-compose configuration..."
	$(DOCKER_COMPOSE) -f $(COMPOSE_FILE) config --quiet && echo "✅ Configuration is valid" || (echo "❌ Configuration errors found"; exit 1)

restart-%:
	$(DOCKER_COMPOSE) -f $(COMPOSE_FILE) restart $*

exec-%:
	$(DOCKER_COMPOSE) -f $(COMPOSE_FILE) exec $* sh

shell: exec-persons-api

# ==================== СПРАВКА ====================
help:
	@echo "🐳 Makefile для управления окружением"
	@echo ""
	@echo "Основные команды:"
	@echo "  make all          # Полный запуск: nexus → сборка → инфра → приложения"
	@echo "  make nexus        # Запустить ТОЛЬКО Nexus (первый этап)"
	@echo "  make infra        # Запустить инфраструктуру (после Nexus)"
	@echo "  make start-apps   # Запустить прикладные сервисы"
	@echo "  make build-artifacts  # Собрать образы (требует работающий Nexus)"
	@echo ""
	@echo "Управление:"
	@echo "  make stop         # Остановить все сервисы"
	@echo "  make clean        # Полная очистка (контейнеры, тома, build-артефакты)"
	@echo "  make logs         # Логи всех сервисов"
	@echo "  make logs-persons-api  # Логи конкретного сервиса"
	@echo "  make restart-persons-api  # Перезапуск сервиса"
	@echo ""
	@echo "Диагностика:"
	@echo "  make status       # Статус всех контейнеров"
	@echo "  make validate     # Валидация docker-compose.yml"
	@echo "  make wait-nexus   # Дождаться готовности Nexus"
	@echo ""
	@echo "💡 Рекомендуемый воркфлоу:"
	@echo "   1. make nexus          # Убедиться, что Nexus запущен"
	@echo "   2. make build-artifacts # Собрать образы через Nexus"
	@echo "   3. make infra          # Запустить остальную инфраструктуру"
	@echo "   4. make start-apps     # Запустить приложения"