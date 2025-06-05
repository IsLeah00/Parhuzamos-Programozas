# IPC - PP

## Alapmechanizmusok

### `fork()`
- Új folyamat létrehozása a hívó folyamat másolataként.
- `#include <unistd.h>`
- Visszatérési érték:
  - 0 → gyermek folyamat
  - > 0 → szülő
  - < 0 → hiba

```c
pid_t pid = fork();
if (pid == 0) { /* gyerek */ }
else if (pid > 0) { /* szülő */ }
```

### `sleep(seconds)`
- Egy folyamat késleltetése.
- Használható folyamatok létrehozásának időzítésére, teszteléshez.

### Folyamatok azonosítói
- `getpid()` → aktuális folyamat PID-je
- `getppid()` → szülő PID-je



## Folyamatok kezelése

### `wait(NULL)`
- A szülő folyamat bevárja valamelyik gyermeket.
- `#include <sys/wait.h>`
- Több `wait()` hívás szükséges több gyermek bevárásához.

### Szülő-gyerek viszony
- A `fork()` során másolódik a szülő memóriája (copy-on-write).
- A változók külön másolatban szerepelnek, még ha a memóriacímük virtuálisan azonos is.



## Megosztott memória

### `shmget()`
- Új megosztott memóriaszegmens létrehozása.
```c
int shmid = shmget(IPC_PRIVATE, size, 0666 | IPC_CREAT);
```

### `shmat()`
- A szegmens hozzárendelése a folyamat címtartományához.
```c
void* mem = shmat(shmid, NULL, 0);
```

### `shmctl()`
- Memóriaszegmens törlése:
```c
shmctl(shmid, IPC_RMID, 0);
```



## Szemaforok

### Definíció
- Absztrakt szinkronizációs mechanizmus.
- Bináris (értékei: 0 vagy 1) vagy nem-bináris.
- Műveletek:
  - `wait()` → erőforrás lefoglalása
  - `signal()` → erőforrás felszabadítása

### `semget()`
```c
int semid = semget(IPC_PRIVATE, 1, 0666 | IPC_CREAT);
```

### `semctl()` – inicializálás
```c
union semun { int val; } arg;
arg.val = 1;
semctl(semid, 0, SETVAL, arg);
```

### `semop()` – művelet végrehajtása
```c
struct sembuf sb = {0, -1, 0}; // wait
semop(semid, &sb, 1);
```

### Törlés
```c
semctl(semid, 0, IPC_RMID);
```



## Szinkronizációs és kizárási minták

- **Szinkronizáció**: `CSinit(0)`, `CSwait()`, `CSsignal()`
- **Kölcsönös kizárás**: `CSinit(1)`, csak egy folyamat léphet be a kritikus szakaszba



## Tipikus hibák

| Hiba              | Leírás                                               |
|-------------------|------------------------------------------------------|
| **Fork bomb**     | Végtelen számú folyamat létrehozása – DoS            |
| **Race condition**| Több folyamat verseng ugyanazért az erőforrásért     |
| **Deadlock**      | Folyamatok egymásra várnak, és egyik sem tud haladni |



## Kapcsolódó parancsok

| Parancs     | Jelentés                             |
|-------------|--------------------------------------|
| `ipcs`      | IPC erőforrások listázása            |
| `ps -ao pid,ppid,psr,comm` | Folyamatok vizsgálata |



## További olvasnivalók

- [Linux manual](https://man7.org/linux/man-pages/dir_all_alphabetic.html)
- [Test-and-set - Wikipedia](https://en.wikipedia.org/wiki/Test-and-set)
- [Fork bomb - Wikipedia](https://en.wikipedia.org/wiki/Fork_bomb)
