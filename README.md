# 🏦 Microservices Banking System


## 🖼️ Preview Aplicație
![Dashboard Preview](imagine.png)

Un sistem bancar distribuit complet, construit pentru a demonstra arhitectura bazată pe evenimente (Event-Driven Architecture) și comunicarea asincronă.

## 🚀 Tehnologii Utilizate
- **Backend:** Java 21, Spring Boot 3
- **Mesagerie:** Apache Kafka
- **Baze de Date:** PostgreSQL (separate pentru Accounts și Transactions)
- **Containerizare:** Docker & Docker Compose
- **Frontend:** React.js (Axios pentru comunicare API)
- **Notificări:** Integrare Mailtrap (SMTP)

## 🛠️ Arhitectura Sistemului
1. **Accounts Service:** Gestionează soldul și inițiază transferurile.
2. **Transactions Service:** Ascultă evenimentele din Kafka și menține istoricul tranzacțiilor.
3. **Logs Service:** Trimite confirmări prin email imediat ce un eveniment este detectat în Kafka.

## 📦 Pornirea Proiectului
Asigurați-vă că aveți Docker instalat, apoi rulați:

```bash
docker compose up --build
