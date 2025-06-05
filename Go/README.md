# Go - PP

## Alapfogalmak

### Goroutine

- Könnyű szál (lightweight thread), amit a `go` kulcsszóval indítunk.
- Nem blokkolja a hívó függvényt.
- Futása nem feltétlenül párhuzamos, de konkurens.

### Channel (Csatorna)

- Kommunikációs eszköz goroutine-ok között.
- Szinkron vagy pufferelt is lehet.
- Irányított csatornák: `chan<-` (küldés), `<-chan` (fogadás).

### Select

- Több csatornát figyel egyszerre.
- Nemdeterminisztikus választás történik a "készen álló" ágak között.
- Tartalmazhat `default` ágat is.

### WaitGroup (sync csomag)

- Goroutine-ok befejeződésének megvárására szolgál.
- Három fő művelet: `Add(n)`, `Done()`, `Wait()`.



## Haladóbb konstrukciók

### Defer

- Késleltetett függvényhívás.
- LIFO sorrendben fut le a függvény végén.
- Gyakran használják erőforrások (pl. fájl, I/O) lezárására.

### Mutex (Kölcsönös kizárás)

- A `sync.Mutex` lehetővé teszi globális erőforrások konfliktusmentes elérését.
- `Lock()` és `Unlock()` műveletekkel.

### Cond (Feltételváltozók)

- A `sync.Cond` lehetőséget ad feltételes várakozásra (`Wait()`, `Signal()`, `Broadcast()`).
- Jellemzően mutex-szel együtt használjuk.

### Atomic

- Az `sync/atomic` csomag atomi műveleteket tesz lehetővé, például: `atomic.AddInt32()`.



## Kommunikációs modellek

### Szinkron vs. Aszinkron kommunikáció

- Alapértelmezés szerint a csatornák szinkron módon működnek.
- Pufferelt csatornákkal aszinkron viselkedés valósítható meg.

### Egyirányú csatorna

- A kód olvashatóságát és biztonságát növeli, ha a csatorna csak küldésre vagy fogadásra szolgál.



## Párhuzamos minták

### Csővezeték (Pipeline)

- Adatok sorozatos feldolgozása több goroutine-láncon keresztül.
- A munkafázisokat külön goroutine-ok látják el.
- Ideális sok adat, kevés változatos művelet esetén.

### Multiplexer / Demultiplexer

- Multiplexer: több bemenő csatornát egyetlen kimenetbe irányít.
- Demultiplexer: egy bemenetet több kimenetre szór szét.
- A `select` kulcsszóval kombinálva valósítják meg a logikai csatornakezelést.



## Hivatkozások

- [Go hivatalos dokumentáció](https://golang.org/doc/)
- [Go standard könyvtárak](https://pkg.go.dev/std)
- [sync csomag](https://golang.org/pkg/sync/)
- [sync/atomic csomag](https://pkg.go.dev/sync/atomic)
