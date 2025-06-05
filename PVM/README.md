# PVM - PP

## Alapfunkciók

### Daemon kezelés

A PVM működéséhez szükséges egy háttérfolyamat (daemon):

```bash
pvm       # belépés a PVM shellbe
quit      # kilépés, de a daemon fut
halt      # teljes leállítás
```

---

## Fordítás és futtatás

### Fordítás:

```bash
gcc -o program program.c -lpvm3
```

> A `server` és `client` fájlokat külön kell fordítani.

### Futtatás:

```bash
./program
```



## Fontos függvények

### Azonosítás és kilépés

| Függvény              | Leírás                                 |
|-----------------------|-----------------------------------------|
| `pvm_mytid()`         | Saját task azonosító lekérése          |
| `pvm_parent()`        | Szülő task azonosító lekérése          |
| `pvm_exit()`          | Kilépés a PVM rendszerből              |



### Task indítása

```c
int pvm_spawn(char* task, char** argv, int flag, char* where, int ntask, int* tids);
```

Feladatpéldányokat indít a `task` fájl alapján, eltárolva azonosítóikat `tids` tömbbe.



## Üzenetküldés és fogadás

### Küldés (3 lépés)

1. **Inicializálás** – `pvm_initsend(PvmDataDefault)`
2. **Csomagolás** – például:
   - `pvm_pkint(int* val, int nitem, int stride)`
   - `pvm_pkstr(char*)`
3. **Küldés** – `pvm_send(tid, msgtag)`

Vagy multicast:
```c
pvm_mcast(tids, ntask, msgtag);
```



### Fogadás

```c
pvm_recv(tid, msgtag); // tid=-1, msgtag=-1 → bárkitől, bármilyen üzenet
```

Csomag kinyerése:
```c
pvm_upkint(), pvm_upkstr(), stb.
```



## Programstruktúrák

### Kliens–szerver minta

- A szerver `pvm_spawn`-nal elindítja a klienst
- A kliens `pvm_send()` segítségével küld üzenetet a szervernek
- A szerver `pvm_recv()`-el fogad

### Master–slave minta

- A szerver több alfeladatot indít
- Kommunikáció külön függvényekbe (`sendint()`, `receiveint()`) kiszervezve



## Csővezeték (Pipeline) modell

- N darab szám rendezéséhez N task szükséges
- Minden task ismeri a következő task `tid`-jét
- Az üzenetek sorrendje **megőrződik** a PVM rendszerben
- A legutolsó task a szervernek küldi vissza az eredményt



## Debug és hibatippek

- A legtöbb hiba: **nem egyezik az üzenetküldés és fogadás száma**
- Fontos: **futtatható abszolút elérési útvonal** megadása a `pvm_spawn()`-nál
- Debugolás nehéz, mert elosztott rendszerben nincs globális nézet



## Kapcsolódó linkek

- [PVM hivatalos oldal](https://www.csm.ornl.gov/pvm/)
- [PVM könyv (netlib)](http://www.netlib.org/pvm3/book/node1.html)
