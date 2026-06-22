# Sommerprojekt: Applikationsentwicklung – Lernprojekt

## Ziel
Fundamente der Applikationsprogrammierung üben und Praxis erwerben anhand einem modernen Enterprise Stack:
- **Datenbank:** PostgreSQL (via Docker)
- **Backend:** Spring Boot mit Spring Data JDBC (Java)
- **Frontend:** React (Browser UI)

Häufig gebrauchte Enterprise Patterns werden verwendet und Good-Practice-Architektur wird implementiert.

## Arbeitsweise
- Jede Iteration wird **vollständig selbstständig** erarbeitet
- Nach jeder Iteration: **Code Review mit Feedback**
- Feedback wird adressiert und überarbeitet
- Erst wenn die Iteration abgenommen ist → nächste Iteration beginnt

---

# Iteration 1 – „Pure SQL"

## Domäne
Kunden kaufen Artikel mit einer Bestellung. Die Bestellung kann aufgegeben oder als Ganzes storniert werden. Artikel können von der Bestellung entfernt oder hinzugefügt werden. Der Kunde kann die Bestellung als Ganzes oder mit Teilzahlungen bezahlen. Falls der Kunde zu viel bezahlt, wird ihm das gutgeschrieben und allenfalls mit anderen offenen Bestellungen verrechnet (FIFO nach Bestelldatum).

**Artikel** sind in dieser Iteration bewusst simpel gehalten: `id`, `name`, `Einheitspreis`. Kein eigenes Lager- oder Katalogmodul.

## Geschäftsregeln
1. Es kann keine Bestellung ohne Kunde geben
2. Bestellpositionen sind immer einem Artikel und einer Bestellung zugeordnet
3. Zahlungen sind immer dem Kunden zugeordnet (nicht einer einzelnen Bestellung)
4. Es kann nur ein Kunde mit der gleichen E-Mail-Adresse im System existieren
5. Der Kunde hat einen offenen Saldo, der negativ (Zahlungen offen) oder positiv (überbezahlt) sein kann
6. Wenn der Kunde 3 Monate nach seiner letzten Bestellung noch einen offenen Saldo hat, wird er verwarnt und kann **keine weitere Bestellung aufgeben**, bis der offene Saldo beglichen ist

## Zahlungslogik (Kernkonzept – vor dem Coding durchdenken)
Die Zahlungslogik ist bewusst komplex gewählt und ist das fachliche Herzstück der Applikation:
- Zahlungen sind dem **Kunden** zugeordnet, nicht einer Bestellung
- Guthaben (Überzahlungen) werden automatisch auf offene Bestellungen verrechnet
- Verrechnung erfolgt nach **FIFO** (älteste offene Bestellung zuerst)
- Der offene Saldo ergibt sich aus: `Summe aller Zahlungen − Summe aller bestellten Positionen`

## Tasks

### 1a – UML Datenmodell
- Vor jeder Datenbankimplementierung wird ein **UML-Klassendiagramm** erstellt
- Das Modell zeigt Entitäten, Attribute, Beziehungen und Kardinalitäten
- Review des Modells **vor** der SQL-Implementierung

### 1b – Datenbankinstallation
- PostgreSQL via **Docker** aufsetzen (`docker-compose.yml`)
- Eigenen Applikations-Schema-User einrichten (kein Superuser)
- Verbindung verifizieren

### 1c – Datenbankmodell implementieren
- Tabellen gemäss UML-Modell erstellen
- Alle notwendigen **Constraints** implementieren:
  - Primary Keys, Foreign Keys
  - UNIQUE auf E-Mail-Adresse
  - NOT NULL wo fachlich sinnvoll
  - CHECK-Constraints wo anwendbar
- Geldbeträge als `NUMERIC(10,2)`

### 1d – Testdaten
- SQL-Script, das alle Tabellen mit repräsentativen Testdaten befüllt
- Testdaten sollen relevante Geschäftssituationen abdecken:
  - Kunde mit offenem Saldo
  - Kunde mit Guthaben
  - Bestellung die älter als 3 Monate ist
  - Stornierte Bestellung
  - Kunde der gesperrt ist (offener Saldo > 3 Monate)

---

# Iteration 2 – Datenzugriffsschicht

## Konzept
**Spring Data JDBC** wird bewusst statt JPA/Hibernate gewählt:
- Kein „Magic" – jede SQL-Operation ist sichtbar und nachvollziehbar
- Das Modell (Entitätsklassen) ist von der Zugriffslogik (Repositories) getrennt
- **Lernziel:** *„Ich weiss genau, welches SQL bei jeder Operation ausgeführt wird."*

## Tasks

### 2a – Spring Boot Projekt aufsetzen
- Spring Boot Projekt erstellen (Spring Initializr)
- Dependencies: `spring-boot-starter-data-jdbc`, `postgresql`, `lombok` (optional)
- `application.properties` für Datenbankverbindung konfigurieren
- Docker-Compose so erweitern, dass Datenbank und App zusammen starten

### 2b – Datenmodell (Java)
- Entitätsklassen für alle Tabellen implementieren
- Klare Trennung: Entitäten sind **plain Java objects**, keine Business-Logik
- *Achtung auf das Aggregate-Design von Spring Data JDBC (Ownership von Entitäten)*

### 2c – Repository-Layer
- Spring Data JDBC Repositories für alle Entitäten
- CRUD-Operationen implementieren
- Eigene Query-Methoden wo nötig (z.B. `findByEmail`, `findByCustomerIdOrderByOrderDateAsc`)

### 2d – Tests
- Integrationstests für alle Repositories
- Tests laufen gegen eine echte (Test-)Datenbank (z.B. via `@DataJdbcTest` + Testcontainers oder separates Testschema)

---

# Iteration 3 – Service-Schicht & Transaktionsmanagement

## Konzept & Datenkonsistenz
Die Service-Schicht orchestriert die Business-Logik. Da die Zahlungslogik (FIFO-Verrechnung) mehrere Datenbank-Lese- und Schreiboperationen umfasst, ist ein robustes **Transaktionsmanagement** unerlässlich.

- **Lernziel:** Verstehen, wie ACID-Eigenschaften in einer Spring-Boot-Applikation sichergestellt werden und wie Datenkonsistenz bei gleichzeitigen Zugriffen (Concurrency) gewahrt bleibt.

## Use Cases

| #  | Use Case |
|----|----------|
| 1  | Erfassen eines neuen Kunden (Vorname, Nachname, E-Mail) |
| 2  | Erfassen einer neuen Bestellung für einen Kunden |
| 3  | Artikel zu einer Bestellung hinzufügen |
| 4  | Artikel von einer Bestellung entfernen |
| 5  | Bestellung stornieren |
| 6  | Bestellung aufgeben (prüft Geschäftsregel 6: Sperrung bei offenem Saldo > 3 Monate) |
| 7  | Kunde bezahlt einen Betrag (triggert FIFO-Verrechnung) |
| 8  | Periodisch: Warn-E-Mail an Kunden mit offenem Saldo und Bestellungen älter als 3 Monate |
| 9  | Ausweis: Kunde mit Saldo und allen bestellten Positionen |
| 10 | Ausweis: Bestellung eines Kunden |
| 11 | Ausweis: Alle Artikel |

## Tasks

### 3a – Service-Layer & Business-Logik
- Einen Service pro fachlichem Bereich (z.B. `CustomerService`, `OrderService`, `PaymentService`)
- Services nutzen ausschliesslich den Repository-Layer für Datenzugriff
- Zahlungslogik (FIFO-Verrechnung, Saldo-Berechnung) wird **im Service** implementiert, nicht in der Datenbank
- Geschäftsregel 6 (Sperrprüfung) wird im `OrderService` bei Use Case 6 erzwungen

### 3b – Transaktionsmanagement (`@Transactional`)

**Pflicht-Teil:**
- Deklaratives Transaktionsmanagement mittels `@Transactional` an den korrekten Service-Methoden umsetzen – insbesondere bei Use Case 7 (Zahlung + FIFO-Verrechnung)
- **Fehlerszenario durchdenken & testen:** Was passiert, wenn mitten in der FIFO-Verrechnung ein Fehler auftritt (z.B. DB-Constraint-Verletzung bei der 2. Bestellung)? Sicherstellen, dass ein vollständiger **Rollback** durchgeführt wird und keine inkonsistenten Teilbuchungen in der DB verbleiben
- Kurze Dokumentation im Code-Kommentar: Warum ist die Transaktion hier wichtig, und wo genau fängt sie an / hört sie auf?

**Optional – Concurrency & Locking:**
- Was passiert, wenn zwei Benutzer gleichzeitig denselben Kundensaldo verändern?
- Konzept **Optimistic Locking** (via `@Version`) oder **Pessimistic Locking** (`SELECT FOR UPDATE`) kennenlernen
- Mindestens ein Szenario dokumentieren und einen einfachen Test schreiben, der das Problem sichtbar macht

### 3c – Scheduler (Use Case 8)
- `@Scheduled`-Job implementiert die periodische Prüfung
- In einem ersten Schritt: **simulierter E-Mail-Versand via Logging** (kein echter SMTP-Versand nötig)
- Klare Trennung: Scheduler ruft Service-Methode auf, nicht direkt Repository

### 3d – Tests
- Unit-Tests für alle Services (Repositories werden gemockt)
- **Integrationstests für die kritischen Pfade der Zahlungslogik unter Transaktionsbedingungen** (Verifikation, dass Rollbacks bei Laufzeit-Exceptions wie gewünscht funktionieren)

---

# Iteration 4 – RESTful Service-Schicht

## Tasks

### 4a – REST-Controller implementieren
- Einen REST-Controller pro Service
- Sauberes URL-Design (z.B. `GET /customers/{id}`, `POST /orders`, `DELETE /orders/{id}/items/{itemId}`)
- Korrekte HTTP-Statuscodes (`200`, `201`, `400`, `404`, `409`)
- Request/Response-Objekte als **DTOs** (getrennt von den Entitäten)

### 4b – Fehlerbehandlung
- Globaler Exception Handler (`@ControllerAdvice`)
- Fachliche Fehler werden als strukturierte JSON-Responses zurückgegeben

### 4c – Testscripts
- **Bruno** (oder HTTPie/curl) Testscripts für alle Endpoints
- Abdeckung: Happy Path + wichtigste Fehlerfälle
- Scripts sind ausführbar und dokumentiert

---

# Iteration 5 – Browser UI (React)

## Scope

| Seite | Funktion |
|-------|----------|
| Kundenliste | Suche, neuen Kunden erfassen |
| Kundendetail | Saldo, Bestellhistorie, Zahlung erfassen |
| Bestelldetail | Positionen anzeigen, Artikel hinzufügen/entfernen, Bestellung aufgeben / stornieren |
| Artikel-Lookup | Einfache Liste zur Auswahl beim Hinzufügen zu einer Bestellung |

## Technologie
- React mit **`fetch` / `axios`** für API-Calls
- State-Management: **`useState` / `useEffect`** – kein Redux
- Kein UI-Framework zwingend; einfaches, sauberes CSS reicht

## Tasks

### 5a – Projektsetup
- React-Projekt aufsetzen (Vite oder Create React App)
- API-Basis-URL konfigurierbar (Umgebungsvariable)

### 5b – Komponenten implementieren
- Eine Komponente pro Seite/Feature
- Klare Trennung: **API-Calls in eigenen Service-Funktionen**, nicht direkt in Komponenten

### 5c – Fehlerbehandlung im UI
- API-Fehler werden dem Benutzer sinnvoll angezeigt (inkl. Sperrhinweis bei Geschäftsregel 6)
- Ladezustände (Loading-Spinner oder ähnliches)

---

# Zeitplan (Orientierung)

| Wochen | Inhalt |
|--------|--------|
| 1 | Domäne verstehen, Zahlungslogik durchdenken, UML-Modell erstellen |
| 2–3 | PostgreSQL/Docker aufsetzen, SQL-Schema, Constraints, Testdaten → **Review 1** |
| 4–5 | Spring Boot Projekt, Spring Data JDBC, Repository-Layer, Aggregate-Strukturen, Tests → **Review 2** |
| 6–7 | Service-Layer, FIFO-Zahlungslogik, `@Transactional` & Rollback-Logik, Scheduler → **Review 3** |
| 8 | REST-Layer, DTOs, Fehlerbehandlung, Testscripts → **Review 4** |
| 9–10 | React UI Grundgerüst, Kundenliste, Kundendetail |
| 11–12 | Bestelldetail, Polishing, End-to-End Tests → **Review 5** |
