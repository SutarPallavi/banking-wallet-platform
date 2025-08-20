.PHONY: up down test

up:
	docker compose up -d --build

down:
	docker compose down -v

test:
	mvn test


