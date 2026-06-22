# Schichtenarchitektur & Package-Konventionen

## Package-Struktur

```
com.example.shop
├── domain          Entity-Klassen (plain Java objects)
├── repository      Spring Data JDBC Repositories
├── service         Business-Logik, @Transactional
├── rest
│   ├── controller  REST Controller (@RestController)
│   └── dto         Request / Response Objekte
├── scheduler       Periodische Jobs (@Scheduled)
└── exception       Exception-Klassen, @ControllerAdvice
```

---

## Abhängigkeitsregeln

Das Diagramm [`schichtenarchitektur.puml`](schichtenarchitektur.puml) zeigt die erlaubten
und verbotenen Abhängigkeiten zwischen den Schichten.

### Erlaubt ✅

| Von | Nach | Grund |
|-----|------|-------|
| `rest` | `service` | Controller ruft Service auf |
| `rest` | `dto` | Controller verwendet DTOs |
| `scheduler` | `service` | Job ruft Service auf |
| `service` | `repository` | Service greift auf Daten zu |
| `service` | `domain` | Service verwendet Entitäten |
| `service` | `exception` | Service wirft fachliche Exceptions |
| `repository` | `domain` | Repository liest / schreibt Entitäten |
| `exception` | – | Keine Abhängigkeiten nach oben |
| `domain` | – | Keine Abhängigkeiten nach oben |

### Verboten ✗

| Von | Nach | Grund |
|-----|------|-------|
| `rest` | `repository` | Controller überspringt Service-Schicht |
| `rest` | `domain` | Controller gibt Entitäten nach aussen – niemals DTOs vergessen |
| `scheduler` | `repository` | Job überspringt Service-Schicht |

---

## Grundprinzipien

**Domain ist der Kern – kennt niemanden**
Entitätsklassen haben keine Abhängigkeiten nach oben.
Sie enthalten keine Business-Logik und keine Annotations ausser `@Table`, `@Id`.

**Service ist der einzige Ort für Business-Logik**
Zahlungslogik, Validierungen, Statusübergänge – alles im Service.
Niemals im Controller, niemals im Repository.

**Repository macht nur Datenzugriff**
Keine if/else, keine Berechnungen, keine fachlichen Entscheidungen.
Nur: lesen, schreiben, suchen.

**Controller kennt keine Entitäten**
Was nach aussen geht (HTTP Response) ist immer ein DTO.
Was von aussen kommt (HTTP Request) ist immer ein DTO.
Entitäten bleiben intern.

**Scheduler kennt nur Services**
Ein `@Scheduled` Job ruft exakt eine Service-Methode auf – sonst nichts.

---

## Naming-Konventionen

### Klassen

| Package | Suffix | Beispiel |
|---------|--------|---------|
| `domain` | – | `Customer`, `Order`, `Payment` |
| `repository` | `Repository` | `CustomerRepository` |
| `service` | `Service` | `CustomerService`, `PaymentService` |
| `rest/controller` | `Controller` | `CustomerController` |
| `rest/dto` | `Request` / `Response` | `CreateCustomerRequest`, `CustomerResponse` |
| `scheduler` | `Job` | `OverdueBalanceJob` |
| `exception` | `Exception` | `CustomerNotFoundException` |

### Methoden im Repository

```java
findById(Long id)
findAll()
findByEmail(String email)
findByCustomerIdOrderByOrderDateAsc(Long customerId)
save(Entity entity)
deleteById(Long id)
```

### Methoden im Service

```java
// Use Case orientiert – spricht die Fachsprache
createCustomer(CreateCustomerRequest request)
placeOrder(Long customerId)
addItemToOrder(Long orderId, Long articleId, int quantity)
processPayment(Long customerId, BigDecimal amount)
```

### REST Endpoints

```
GET    /customers              Liste aller Kunden
GET    /customers/{id}         Kunde by ID
POST   /customers              Neuen Kunden erfassen
GET    /customers/{id}/orders  Bestellungen eines Kunden

POST   /orders                 Neue Bestellung
GET    /orders/{id}            Bestellung by ID
POST   /orders/{id}/items      Artikel hinzufügen
DELETE /orders/{id}/items/{itemId}  Artikel entfernen
POST   /orders/{id}/cancel     Bestellung stornieren
POST   /orders/{id}/place      Bestellung aufgeben

POST   /payments               Zahlung erfassen

GET    /articles               Alle Artikel
```

---

## HTTP Statuscodes

| Code | Wann |
|------|------|
| `200 OK` | Erfolgreiche GET, PUT |
| `201 Created` | Erfolgreiche POST (neue Ressource) |
| `400 Bad Request` | Ungültige Eingabe (Validierungsfehler) |
| `404 Not Found` | Ressource nicht gefunden |
| `409 Conflict` | Geschäftsregel verletzt (z.B. Kunde gesperrt) |
| `500 Internal Server Error` | Unerwarteter Fehler |
