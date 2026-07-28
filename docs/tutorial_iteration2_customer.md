# Tutorial: Iteration 2 – Datenzugriffsschicht
## Beispiel: Customer Entity – vollständiger Walkthrough

Dieses Tutorial führt dich Schritt für Schritt durch die komplette Datenzugriffsschicht
anhand der `Customer` Tabelle. Du hast diese Tabelle bereits in Iteration 1 als SQL gebaut –
jetzt verbindest du sie mit Java.

Das Muster das du hier lernst gilt für **alle weiteren Entitäten** (`Order`, `OrderItem`,
`Article`, `Payment`). Wenn du `Customer` verstanden hast, kannst du den Rest selbstständig.

---

## Warum Spring Data JDBC und nicht JPA/Hibernate?

Das ist eine bewusste Entscheidung – und es lohnt sich, sie zu verstehen.

**JPA/Hibernate** ist das bekanntere Framework. Es ist mächtig, aber es versteckt sehr viel:
Lazy Loading, Caching, automatische Joins, Session-Management. Als Einsteigerin weisst du
oft nicht mehr, welches SQL tatsächlich auf der Datenbank ausgeführt wird.

**Spring Data JDBC** ist schlanker und ehrlicher:
- Jede Operation führt zu genau einem SQL-Statement
- Es gibt kein Lazy Loading, kein Caching, keine versteckte Magie
- Du siehst und kontrollierst was passiert

**Dein Lernziel für diese Iteration:**
> *"Ich weiss genau, welches SQL bei jeder Operation auf der Datenbank ausgeführt wird."*

Halte dieses Ziel im Hinterkopf während du arbeitest.

---

## Schritt 1 – Spring Boot Projekt aufsetzen

### 1a – Projekt generieren

Gehe auf [start.spring.io](https://start.spring.io) und wähle folgende Einstellungen:

| Feld | Wert |
|------|------|
| Project | Maven |
| Language | Java |
| Spring Boot | 3.x (aktuellste stabile Version) |
| Group | com.example |
| Artifact | shop |
| Packaging | Jar |
| Java | 21 |

Füge folgende **Dependencies** hinzu (Suchfeld oben rechts):
- `Spring Data JDBC`
- `PostgreSQL Driver`
- `Lombok` (optional – reduziert Boilerplate Code)

Klicke **Generate** → ZIP entpacken → Inhalt in den `backend/` Ordner deines Projekts legen.

### 1b – Package-Struktur anlegen

Erstelle folgende Packages unter `src/main/java/com/example/shop/`:

```
com.example.shop/
├── domain/
├── repository/
├── service/
├── rest/
│   ├── controller/
│   └── dto/
├── scheduler/
└── exception/
```

Diese Struktur ist in `docs/architektur_konventionen.md` dokumentiert und gilt für das
gesamte Projekt. Jedes Package hat eine klar definierte Verantwortung – lies das Dokument
durch bevor du weitermachst.

### 1c – Datenbankverbindung konfigurieren

Öffne `src/main/resources/application.properties` und ergänze:

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/shopdb
spring.datasource.username=shop_user
spring.datasource.password=shop_pass
spring.datasource.driver-class-name=org.postgresql.Driver

spring.sql.init.mode=never

logging.level.org.springframework.jdbc.core=DEBUG
```

> **Was bedeutet `spring.sql.init.mode=never`?**
> Spring Boot kann beim Start automatisch SQL-Scripts ausführen. Da du dein Schema selbst
> via `./db/schema.sh` verwaltest, schaltest du diese Funktion aus.

> **Was bewirkt das `logging.level`?**
> Spring Data JDBC loggt damit jedes SQL-Statement das es ausführt. Du siehst also genau
> was in der Datenbank passiert – das ist Gold wert beim Lernen und Debuggen.

---

## Schritt 2 – Die Entity Klasse

Eine Entity Klasse ist ein **Plain Java Object** das eine Zeile in der Datenbanktabelle
repräsentiert. Keine Business-Logik, keine Berechnungen – nur Daten.

### `Customer.java` im Package `domain`

```java
package com.example.shop.domain;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;

@Table("customer")
public class Customer {

    @Id
    private Long id;

    private String firstName;
    private String lastName;
    private String email;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // ── Konstruktoren ─────────────────────────────────────────

    public Customer() {}

    public Customer(String firstName, String lastName, String email) {
        this.firstName = firstName;
        this.lastName  = lastName;
        this.email     = email;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    // ── Getter / Setter ───────────────────────────────────────

    public Long getId()                   { return id; }
    public String getFirstName()          { return firstName; }
    public String getLastName()           { return lastName; }
    public String getEmail()              { return email; }
    public LocalDateTime getCreatedAt()   { return createdAt; }
    public LocalDateTime getUpdatedAt()   { return updatedAt; }

    public void setId(Long id)                        { this.id = id; }
    public void setFirstName(String firstName)        { this.firstName = firstName; }
    public void setLastName(String lastName)          { this.lastName = lastName; }
    public void setEmail(String email)                { this.email = email; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    @Override
    public String toString() {
        return "Customer{id=" + id + ", email='" + email + "'}";
    }
}
```

### Was du hier verstehen sollst

**`@Table("customer")`**
Diese Annotation sagt Spring Data JDBC: "Diese Klasse gehört zur Tabelle `customer`."
Ohne sie würde Spring nach einer Tabelle namens `CUSTOMER` suchen (Grossschreibung).

**`@Id`**
Markiert das Feld als Primary Key. Spring Data JDBC erkennt daran, ob ein Objekt neu ist
(kein `id` → INSERT) oder bereits existiert (hat `id` → UPDATE).

**camelCase → snake_case**
Spring Data JDBC übersetzt Feldnamen automatisch:
- `firstName` → `first_name`
- `lastName` → `last_name`
- `createdAt` → `created_at`

Du musst keine `@Column`-Annotations schreiben – solange du die Naming Convention einhältst.

**Keine Business-Logik in der Entity**
Die Entity kennt nur ihre eigenen Daten. Sie berechnet keinen Saldo, sie prüft keine
Geschäftsregeln. Das ist die Aufgabe der Service-Schicht.

> **Denk darüber nach:** Warum erbt `Customer` von keiner Basisklasse?
> Was würde in JPA/Hibernate anders aussehen?

---

## Schritt 3 – Das Repository

Das Repository ist die Brücke zwischen deiner Java-Applikation und der Datenbank.
Du definierst ein Interface – Spring Data JDBC generiert die Implementierung automatisch.

### `CustomerRepository.java` im Package `repository`

```java
package com.example.shop.repository;

import com.example.shop.domain.Customer;
import org.springframework.data.repository.ListCrudRepository;

import java.util.Optional;

public interface CustomerRepository extends ListCrudRepository<Customer, Long> {

    Optional<Customer> findByEmail(String email);

}
```

Das war es. Kein `@Repository`, keine Implementierungsklasse, kein SQL – Spring Data JDBC
generiert alles daraus.

### Was du hier verstehen sollst

**`ListCrudRepository<Customer, Long>`**
Der erste Typ-Parameter ist die Entity-Klasse, der zweite ist der Typ des Primary Keys.
`ListCrudRepository` gibt dir diese Methoden gratis:
- `save(customer)` → INSERT oder UPDATE
- `findById(id)` → SELECT WHERE id = ?
- `findAll()` → SELECT *
- `deleteById(id)` → DELETE WHERE id = ?
- `count()` → SELECT COUNT(*)

**`Optional<Customer> findByEmail(String email)`**
Spring Data JDBC liest den Methodennamen und generiert daraus:
```sql
SELECT * FROM customer WHERE email = ?
```
Das Naming-Pattern ist: `findBy` + Feldname. Spring übersetzt `Email` → `email` (Spaltenname).

**Warum `Optional<>` und nicht einfach `Customer`?**
Wenn kein Kunde mit dieser E-Mail existiert, gibt `findByEmail` ein leeres `Optional` zurück.
Das zwingt den Aufrufer, diesen Fall explizit zu behandeln – und verhindert `NullPointerException`.

**Das SQL im Log sehen**
Starte die Applikation und rufe `findByEmail` auf. In der Konsole siehst du dank dem
`logging.level` in `application.properties`:
```
DEBUG - Executing prepared SQL statement [SELECT ... FROM customer WHERE email = ?]
```
Das ist der Beweis: du weisst exakt was passiert.

> **Probiere aus:** Füge eine zweite eigene Methode hinzu – z.B. alle Kunden nach Nachname
> suchen. Wie würde der Methodenname heissen? Was generiert Spring daraus für SQL?

---

## Schritt 4 – Integrationstests

Tests sind kein Nice-to-have – sie sind der Beweis dass dein Code korrekt funktioniert.
Für die Repository-Schicht schreibst du **Integrationstests** – Tests die gegen die echte
PostgreSQL Datenbank laufen.

> **Voraussetzung:** Die Datenbank muss laufen wenn du die Tests ausführst.
> ```bash
> docker compose -f docker-compose.db.yml up -d
> ```

### `CustomerRepositoryTest.java` im Package `repository` unter `src/test/java`

```java
package com.example.shop.repository;

import com.example.shop.domain.Customer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.jdbc.DataJdbcTest;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJdbcTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class CustomerRepositoryTest {

    @Autowired
    private CustomerRepository customerRepository;

    @BeforeEach
    void setUp() {
        customerRepository.deleteAll();
    }

    @Test
    void shouldSaveAndFindCustomer() {
        // Arrange
        Customer customer = new Customer("Anna", "Müller", "anna@example.com");

        // Act
        Customer saved = customerRepository.save(customer);

        // Assert
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getFirstName()).isEqualTo("Anna");
        assertThat(saved.getEmail()).isEqualTo("anna@example.com");
    }

    @Test
    void shouldFindByEmail() {
        // Arrange
        customerRepository.save(new Customer("Anna", "Müller", "anna@example.com"));

        // Act
        Optional<Customer> found = customerRepository.findByEmail("anna@example.com");

        // Assert
        assertThat(found).isPresent();
        assertThat(found.get().getFirstName()).isEqualTo("Anna");
    }

    @Test
    void shouldReturnEmptyWhenEmailNotFound() {
        // Act
        Optional<Customer> found = customerRepository.findByEmail("nicht@vorhanden.com");

        // Assert
        assertThat(found).isEmpty();
    }

    @Test
    void shouldEnforceUniqueEmail() {
        // Arrange
        customerRepository.save(new Customer("Anna", "Müller", "anna@example.com"));

        // Act & Assert – dieselbe E-Mail nochmal speichern muss fehlschlagen
        org.junit.jupiter.api.Assertions.assertThrows(Exception.class, () -> {
            customerRepository.save(new Customer("Bob", "Smith", "anna@example.com"));
        });
    }

    @Test
    void shouldFindAllCustomers() {
        // Arrange
        customerRepository.save(new Customer("Anna", "Müller", "anna@example.com"));
        customerRepository.save(new Customer("Bob", "Smith", "bob@example.com"));

        // Act
        var customers = customerRepository.findAll();

        // Assert
        assertThat(customers).hasSize(2);
    }

    @Test
    void shouldDeleteCustomer() {
        // Arrange
        Customer saved = customerRepository.save(new Customer("Anna", "Müller", "anna@example.com"));

        // Act
        customerRepository.deleteById(saved.getId());

        // Assert
        assertThat(customerRepository.findById(saved.getId())).isEmpty();
    }
}
```

### Test-Konfiguration

Erstelle `src/test/resources/application.properties`:

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/shopdb
spring.datasource.username=shop_user
spring.datasource.password=shop_pass
spring.datasource.driver-class-name=org.postgresql.Driver

spring.sql.init.mode=never
```

### Was du hier verstehen sollst

**`@DataJdbcTest`**
Lädt nur den Teil des Spring Contexts der für Datenbankzugriff nötig ist – kein Web-Layer,
kein Service-Layer. Tests laufen dadurch schneller.

**`@AutoConfigureTestDatabase(replace = NONE)`**
Standardmässig versucht Spring Boot bei Tests eine In-Memory Datenbank (H2) zu verwenden.
Mit `replace = NONE` sagst du: "Nein, ich will meine echte PostgreSQL Datenbank."
Das ist realistischer – du testest gegen dieselbe Datenbank die auch in Produktion läuft.

**`@BeforeEach void setUp()`**
Vor jedem Test werden alle Kunden gelöscht. So sind Tests voneinander unabhängig –
ein fehlgeschlagener Test beeinflusst den nächsten nicht.

**Arrange / Act / Assert**
Das ist das Standard-Muster für Tests:
- **Arrange:** Testdaten vorbereiten
- **Act:** Die Methode aufrufen die du testen willst
- **Assert:** Das Resultat prüfen

**`shouldEnforceUniqueEmail`**
Dieser Test prüft ob dein Datenbank-Constraint (`UNIQUE` auf `email`) wirklich funktioniert.
Constraints in der DB zu testen ist wichtig – Java allein reicht nicht.

### Tests ausführen

```bash
cd backend
./mvnw test
```

Alle Tests grün? Dann hast du die Datenzugriffsschicht für `Customer` korrekt implementiert.

> **Denk darüber nach:** Was passiert im `shouldEnforceUniqueEmail` Test genau?
> Welche Exception wird geworfen und woher kommt sie – aus Java oder aus PostgreSQL?

---

## Schritt 5 – Alles zusammen verifizieren

### SQL im Log lesen

Starte die Applikation:
```bash
cd backend
./mvnw spring-boot:run
```

Jedes Mal wenn Spring Data JDBC eine Datenbankoperation ausführt, siehst du in der Konsole:
```
DEBUG o.s.jdbc.core.JdbcTemplate - Executing prepared SQL statement
      [SELECT "customer"."id", ... FROM "customer" WHERE "customer"."email" = ?]
```

Das ist dein Beweis: du weisst exakt was passiert.

### In psql nachschauen

```bash
./db/psql.sh
```

```sql
-- Tabellenstruktur anzeigen
\d customer

-- Alle gespeicherten Kunden anzeigen
SELECT * FROM customer;
```

---

## Das Muster – was du gelernt hast

```
Datenbank Tabelle (schema.sql)
        ↓
Domain Entity  →  @Table, @Id, plain Java object, camelCase→snake_case
        ↓
Repository     →  ListCrudRepository, eigene findBy-Methoden, Optional<>
        ↓
Integrationstest  →  @DataJdbcTest, Arrange/Act/Assert, echter PostgreSQL
```

Dieses Muster wiederholst du jetzt selbstständig für:
- `Order`
- `OrderItem`
- `Article`
- `Payment`

Jede Entität folgt demselben Weg. Wenn du irgendwo nicht weiterkommst – schau hier nach.

---

## Selbstreflexion vor dem Code Review

Bevor du den Pull Request öffnest, geh diese Punkte selbst durch:

- [ ] Package-Struktur korrekt – alle Klassen im richtigen Package?
- [ ] Jede Entity ist ein plain Java object – keine Business-Logik drin?
- [ ] `@Table` und `@Id` korrekt gesetzt?
- [ ] Feldnamen in camelCase – Mapping auf snake_case verstanden?
- [ ] Repository erweitert `ListCrudRepository` mit den richtigen Typ-Parametern?
- [ ] `Optional<>` konsequent verwendet wo eine Entität nicht gefunden werden kann?
- [ ] Tests decken CRUD + eigene Methoden + Constraint-Tests ab?
- [ ] SQL im Log verifiziert – weisst du was bei jeder Operation ausgeführt wird?
- [ ] Alle Tests laufen grün mit `./mvnw test`?