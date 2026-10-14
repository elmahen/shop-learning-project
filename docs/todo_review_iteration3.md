# TODO – Review der Service-Layer-Änderungen (Commit `111fbb7`)

Quelle: Code-Review vom 28.09.2026 zu BalanceService, OrderService, PaymentService und CustomerService.

## Hoch

- [ ] **1. Kunden werden fälschlich gesperrt** (`BalanceService.java:37`)
  - Problem: Im Saldo werden nur `CANCELLED`-Bestellungen ausgefiltert. Offene Warenkörbe (`PENDING`) zählen mit, auch die Bestellung, die gerade in `placeOrder` aufgegeben wird. Zahlungen werden nur in `processPayment` verrechnet, nicht in `placeOrder`, deshalb bleiben vorausbezahlte Bestellungen auf `PLACED`.
  - Beispiel: 100 vorausbezahlt → Bestellung über 100 bleibt `PLACED` → 4 Monate später neue Bestellung über 50 → Saldo −50 und eine „überfällige“ Bestellung → `CustomerBlockedException`, obwohl der Kunde nichts schuldet.
  - [ ] Im Saldo nur `PLACED` und `PAID` zählen
  - [ ] FIFO-Verrechnung als `allocatePayments(customerId)` auslagern und in `processPayment` **und** `placeOrder` aufrufen

## Mittel

- [ ] **2. CustomerService prüft das Überfällig-Sein noch nach der alten Regel** (`CustomerService.java:68`)
  - Problem: Es wird `lastOrder` genommen (neueste Bestellung, beliebiger Status). Die Regel „älteste offene Bestellung“ gibt es nur in `OrderService.java:127`. Ein Kunde mit alter unbezahlter und neuer Bestellung bekommt keine Warnung, wird aber in `placeOrder` gesperrt.
  - [ ] Regel einmal als `BalanceService.isOverdue(customerId)` schreiben
  - [ ] In `CustomerService` und `OrderService` verwenden

- [ ] **3. `orderDate` ist das Anlegedatum des Warenkorbs** (`Order.java:23`)
  - Problem: `orderDate = now()` wird schon beim Anlegen (`PENDING`) gesetzt. Die 3-Monats-Frist läuft dadurch ab dem Anlegen des Warenkorbs.
  - [ ] In `placeOrder` `order.setOrderDate(LocalDateTime.now())` setzen

- [ ] **6. Tests für die neuen Änderungen fehlen**
  - [ ] `@Mock BalanceService` in `OrderServiceTest` und `CustomerServiceTest` ergänzen (sonst NPE durch `@InjectMocks`)
  - [ ] Tests für `BalanceService`
  - [ ] Test: Sperre in `placeOrder`
  - [ ] Test: FIFO-Logik mit bereits bezahlten Bestellungen
  - [ ] Test: `quantity <= 0`
  - [ ] Test: `removeItemFromOrder` mit Position aus einer anderen Bestellung

## Niedrig

- [ ] **4. Bestellsumme wird doppelt berechnet**
  - Problem: `PaymentService.calculateOrderTotal` und `BalanceService` rechnen dasselbe (`BigDecimal.valueOf` vs. `new BigDecimal(int)`).
  - [ ] Berechnung nur im `BalanceService`, `PaymentService` ruft sie dort auf

- [ ] **5. Aufräumen**
  - [ ] `OrderService`: unbenutztes `PaymentRepository` (Feld + Konstruktor) und Import von `Payment` entfernen
  - [ ] `BalanceService`: Einrückung korrigieren
  - [ ] `BalanceService`: N+1-Abfragen vermeiden (`findByOrderId` pro Order)
  - [ ] `BalanceService`: `@Transactional(readOnly = true)` ergänzen
  - [ ] `BalanceService`: Variable `saldo` direkt zurückgeben
  - [ ] `CustomerService.java:72`: schliessende `}` aus der `log.warn`-Zeile auf eine eigene Zeile
  - [ ] `removeItemFromOrder`: zuerst die Bestellung prüfen, dann die Position (eindeutigere Fehlermeldungen; funktional schon korrekt)
