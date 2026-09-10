# Tutorial: Iteration 0 – Spike
## Einmal durch alle Schichten: Kunde erfassen und anzeigen

Dieser Spike ist dein erster vollständiger Durchlauf durch alle Schichten der Applikation.
Du baust eine einzige, einfache Funktionalität – Kunden erfassen und auflisten – und zwar
**von der Datenbank bis zum Browser**.

Das Ziel ist nicht Perfektion. Das Ziel ist, dass du verstehst wie alle Teile zusammenspielen.

---

## Was du baust

```
Browser (React)
    ↓  POST /customers   (Formular absenden)
    ↑  GET  /customers   (Liste laden)
REST Controller
    ↓
Service
    ↓
Repository
    ↓
PostgreSQL (customer Tabelle)
```

**Funktionalität:**
- Formular: Vorname, Nachname, E-Mail eingeben → Kunde speichern
- Liste: Alle Kunden anzeigen

Kein Saldo, keine Bestellungen, keine Zahlungen. Bewusst simpel.

---

## Voraussetzungen

- Iteration 1 ist abgeschlossen: `customer` Tabelle existiert in der DB
- Iteration 2 Customer Entity und Repository sind implementiert
- Datenbank läuft: `docker compose -f docker-compose.db.yml up -d`

---

## Schritt 1 – Service Layer

Der Service ist der einzige Ort für Business-Logik. Für den Spike ist die Logik noch
minimal – aber die Struktur ist bereits korrekt und produktionsbereit.

### `CustomerService.java` im Package `service`

```java
package com.example.shop.service;

import com.example.shop.domain.Customer;
import com.example.shop.exception.CustomerAlreadyExistsException;
import com.example.shop.repository.CustomerRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CustomerService {

    private final CustomerRepository customerRepository;

    public CustomerService(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    @Transactional
    public Customer createCustomer(String firstName, String lastName, String email) {
        // Geschäftsregel: E-Mail muss eindeutig sein
        customerRepository.findByEmail(email).ifPresent(existing -> {
            throw new CustomerAlreadyExistsException(
                "Ein Kunde mit der E-Mail '" + email + "' existiert bereits."
            );
        });

        Customer customer = new Customer(firstName, lastName, email);
        return customerRepository.save(customer);
    }

    public List<Customer> getAllCustomers() {
        return customerRepository.findAll();
    }
}
```

### Was du hier verstehen sollst

**`@Service`**
Markiert die Klasse als Spring Bean – Spring verwaltet ihre Lebensdauer automatisch.

**Constructor Injection**
Du übergibst das Repository über den Konstruktor, nicht via `@Autowired` auf dem Feld.
Das ist die empfohlene Variante – besser testbar, klarer in den Abhängigkeiten.

**`@Transactional` auf `createCustomer`**
Die Methode macht zwei Datenbankoperationen: `findByEmail` + `save`. Mit `@Transactional`
laufen beide in derselben Transaktion – bei einem Fehler wird alles zurückgerollt.

**Geschäftsregel im Service**
Die E-Mail Eindeutigkeit wird hier geprüft – nicht im Repository, nicht im Controller.
Der Service ist der einzige Ort für solche Entscheidungen.

---

## Schritt 2 – Exception Klasse

### `CustomerAlreadyExistsException.java` im Package `exception`

```java
package com.example.shop.exception;

public class CustomerAlreadyExistsException extends RuntimeException {

    public CustomerAlreadyExistsException(String message) {
        super(message);
    }
}
```

Eigene Exception-Klassen machen Fehlerfälle explizit und lesbar. Im nächsten Schritt
fängst du diese Exception im Controller ab und gibst eine sinnvolle HTTP Response zurück.

---

## Schritt 3 – DTOs

DTOs (Data Transfer Objects) sind die Objekte die zwischen Controller und Aussen-Welt
ausgetauscht werden. Die Entity bleibt intern – sie verlässt nie den Server.

### `CreateCustomerRequest.java` im Package `rest/dto`

```java
package com.example.shop.rest.dto;

public class CreateCustomerRequest {

    private String firstName;
    private String lastName;
    private String email;

    public CreateCustomerRequest() {}

    public String getFirstName() { return firstName; }
    public String getLastName()  { return lastName; }
    public String getEmail()     { return email; }

    public void setFirstName(String firstName) { this.firstName = firstName; }
    public void setLastName(String lastName)   { this.lastName = lastName; }
    public void setEmail(String email)         { this.email = email; }
}
```

### `CustomerResponse.java` im Package `rest/dto`

```java
package com.example.shop.rest.dto;

import com.example.shop.domain.Customer;

public class CustomerResponse {

    private Long id;
    private String firstName;
    private String lastName;
    private String email;

    // Statische Factory-Methode: Entity → DTO
    public static CustomerResponse from(Customer customer) {
        CustomerResponse response = new CustomerResponse();
        response.id        = customer.getId();
        response.firstName = customer.getFirstName();
        response.lastName  = customer.getLastName();
        response.email     = customer.getEmail();
        return response;
    }

    public Long getId()          { return id; }
    public String getFirstName() { return firstName; }
    public String getLastName()  { return lastName; }
    public String getEmail()     { return email; }
}
```

### Was du hier verstehen sollst

**Warum DTOs und nicht direkt die Entity zurückgeben?**
- Die Entity kann interne Felder haben die nicht nach aussen sollen
- Das Response-Format kann sich ändern ohne die Entity zu berühren
- Du kontrollierst genau was der Client sieht

**`CustomerResponse.from(customer)`**
Die statische Factory-Methode übersetzt Entity → DTO an einem zentralen Ort.
Sauber, lesbar, wiederverwendbar.

---

## Schritt 4 – REST Controller

### `CustomerController.java` im Package `rest/controller`

```java
package com.example.shop.rest.controller;

import com.example.shop.exception.CustomerAlreadyExistsException;
import com.example.shop.rest.dto.CreateCustomerRequest;
import com.example.shop.rest.dto.CustomerResponse;
import com.example.shop.service.CustomerService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/customers")
@CrossOrigin(origins = "http://localhost:5173") // React Dev Server
public class CustomerController {

    private final CustomerService customerService;

    public CustomerController(CustomerService customerService) {
        this.customerService = customerService;
    }

    @PostMapping
    public ResponseEntity<?> createCustomer(@RequestBody CreateCustomerRequest request) {
        try {
            var customer = customerService.createCustomer(
                request.getFirstName(),
                request.getLastName(),
                request.getEmail()
            );
            return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(CustomerResponse.from(customer));
        } catch (CustomerAlreadyExistsException e) {
            return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(e.getMessage());
        }
    }

    @GetMapping
    public List<CustomerResponse> getAllCustomers() {
        return customerService.getAllCustomers()
            .stream()
            .map(CustomerResponse::from)
            .toList();
    }
}
```

### Was du hier verstehen sollst

**`@RestController`**
Kombiniert `@Controller` + `@ResponseBody` – jede Methode gibt automatisch JSON zurück.

**`@RequestMapping("/customers")`**
Alle Endpoints dieses Controllers beginnen mit `/customers`.

**`@CrossOrigin`**
Dein React Dev Server läuft auf Port 5173, dein Spring Backend auf 8080. Ohne diese
Annotation blockiert der Browser Requests von einer anderen Origin (CORS Policy).
Für den Spike reicht diese einfache Lösung.

**HTTP Statuscodes**
- `201 Created` → Kunde erfolgreich erstellt
- `409 Conflict` → E-Mail bereits vorhanden

**`@PostMapping` / `@GetMapping`**
Kurzformen für `@RequestMapping(method = POST)` / `@RequestMapping(method = GET)`.

> **Denk darüber nach:** Was passiert wenn `firstName` im Request leer ist?
> Wie könntest du das abfangen? (Stichwort: `@Valid`, `@NotBlank`)

---

## Schritt 5 – Backend testen mit Bruno

Bevor du React anfasst – verifiziere dass das Backend korrekt funktioniert.

Starte das Backend:
```bash
cd backend
./mvnw spring-boot:run
```

Erstelle in `bruno/` eine neue Collection `customers` mit zwei Requests:

**POST /customers – Kunde erstellen**
```
POST http://localhost:8080/customers
Content-Type: application/json

{
  "firstName": "Anna",
  "lastName": "Müller",
  "email": "anna@example.com"
}
```
Erwartete Response: `201 Created` mit dem neuen Kunden als JSON.

**GET /customers – Alle Kunden**
```
GET http://localhost:8080/customers
```
Erwartete Response: `200 OK` mit einer Liste aller Kunden.

**Teste auch den Fehlerfall:**
Sende denselben POST nochmal mit derselben E-Mail.
Erwartete Response: `409 Conflict`.

Erst wenn alle drei Fälle korrekt funktionieren – weiter mit React.

---

## Schritt 6 – React UI

### 6a – Projekt aufsetzen

```bash
cd frontend
npm create vite@latest . -- --template react
npm install
npm install axios
```

Starte den Dev Server:
```bash
npm run dev
```

React läuft jetzt auf `http://localhost:5173`.

### 6b – API Service

Erstelle `src/services/customerService.js`:

```javascript
import axios from 'axios';

const API_BASE = 'http://localhost:8080';

export const createCustomer = async (firstName, lastName, email) => {
    const response = await axios.post(`${API_BASE}/customers`, {
        firstName,
        lastName,
        email
    });
    return response.data;
};

export const getAllCustomers = async () => {
    const response = await axios.get(`${API_BASE}/customers`);
    return response.data;
};
```

### Was du hier verstehen sollst

**API-Calls in eigenen Service-Funktionen**
Die Komponenten rufen nie direkt `axios.get(...)` auf – das macht der Service.
Wenn sich die API-URL ändert, änderst du es an einem Ort.

### 6c – Customer Komponente

Erstelle `src/components/CustomerApp.jsx`:

```jsx
import { useState, useEffect } from 'react';
import { createCustomer, getAllCustomers } from '../services/customerService';

function CustomerApp() {
    const [customers, setCustomers] = useState([]);
    const [firstName, setFirstName] = useState('');
    const [lastName, setLastName]   = useState('');
    const [email, setEmail]         = useState('');
    const [error, setError]         = useState('');
    const [loading, setLoading]     = useState(false);

    // Kunden laden beim ersten Render
    useEffect(() => {
        loadCustomers();
    }, []);

    const loadCustomers = async () => {
        try {
            const data = await getAllCustomers();
            setCustomers(data);
        } catch (err) {
            setError('Kunden konnten nicht geladen werden.');
        }
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        setError('');
        setLoading(true);

        try {
            await createCustomer(firstName, lastName, email);
            setFirstName('');
            setLastName('');
            setEmail('');
            await loadCustomers(); // Liste neu laden
        } catch (err) {
            if (err.response?.status === 409) {
                setError('Ein Kunde mit dieser E-Mail existiert bereits.');
            } else {
                setError('Ein Fehler ist aufgetreten.');
            }
        } finally {
            setLoading(false);
        }
    };

    return (
        <div style={{ maxWidth: '600px', margin: '40px auto', fontFamily: 'sans-serif' }}>
            <h1>Kunden</h1>

            {/* Formular */}
            <form onSubmit={handleSubmit}>
                <div>
                    <input
                        placeholder="Vorname"
                        value={firstName}
                        onChange={e => setFirstName(e.target.value)}
                        required
                    />
                </div>
                <div>
                    <input
                        placeholder="Nachname"
                        value={lastName}
                        onChange={e => setLastName(e.target.value)}
                        required
                    />
                </div>
                <div>
                    <input
                        type="email"
                        placeholder="E-Mail"
                        value={email}
                        onChange={e => setEmail(e.target.value)}
                        required
                    />
                </div>
                {error && <p style={{ color: 'red' }}>{error}</p>}
                <button type="submit" disabled={loading}>
                    {loading ? 'Wird gespeichert...' : 'Kunde erfassen'}
                </button>
            </form>

            {/* Kundenliste */}
            <h2>Alle Kunden ({customers.length})</h2>
            {customers.length === 0 ? (
                <p>Noch keine Kunden erfasst.</p>
            ) : (
                <ul>
                    {customers.map(customer => (
                        <li key={customer.id}>
                            {customer.firstName} {customer.lastName} – {customer.email}
                        </li>
                    ))}
                </ul>
            )}
        </div>
    );
}

export default CustomerApp;
```

### 6d – App.jsx anpassen

Ersetze den Inhalt von `src/App.jsx`:

```jsx
import CustomerApp from './components/CustomerApp';

function App() {
    return <CustomerApp />;
}

export default App;
```

---

## Schritt 7 – Alles zusammen testen

Du hast jetzt drei Prozesse laufen:

| Was | Wo | Port |
|-----|----|------|
| PostgreSQL | Docker | 5432 |
| Spring Boot Backend | Terminal 1 | 8080 |
| React Frontend | Terminal 2 | 5173 |

Öffne `http://localhost:5173` im Browser:

1. Erfasse einen neuen Kunden → erscheint in der Liste
2. Erfasse denselben Kunden nochmal → Fehlermeldung erscheint
3. Lade die Seite neu → Liste bleibt (Daten sind in der DB)

Schau im Backend-Log was passiert wenn du das Formular absendest – du siehst die SQL
Statements genau.

---

## Was du gelernt hast

Du hast einmal alle Schichten durchlaufen:

```
React Formular
    → axios POST /customers
        → @RestController empfängt Request als DTO
            → @Service prüft Geschäftsregel, ruft Repository auf
                → Repository führt INSERT aus
                    → PostgreSQL speichert den Kunden
                ← Repository gibt Entity zurück
            ← Service gibt Entity zurück
        ← Controller wandelt Entity → DTO, gibt 201 zurück
    ← axios bekommt Response
← React zeigt aktualisierten Kunden in der Liste
```

Das ist das Fundament. Alle weiteren Use Cases folgen demselben Muster.

---

## Selbstreflexion vor dem Code Review

- [ ] Backend startet ohne Fehler?
- [ ] Bruno Tests: POST, GET, Fehlerfall (409) – alle korrekt?
- [ ] React UI zeigt Formular und Liste?
- [ ] Neuer Kunde erscheint nach dem Speichern in der Liste?
- [ ] Fehlermeldung bei doppelter E-Mail?
- [ ] Nach Browser-Reload sind die Daten noch da (DB persistiert)?
- [ ] API-Calls im `customerService.js` – nicht direkt in der Komponente?
- [ ] SQL Statements im Backend-Log sichtbar?