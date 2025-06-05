/**
 * Feladat összefoglalása:
 *   Közös memória: float típus, inicializálva negatív számmal
 *   2 gyerekfolyamat:
 *      1. gyerek: hozzáadja a saját PID-jét
 *      2. gyerek: ezután elosztja a memóriában lévő értéket a saját PID-jével
 *   Szinkronizáció: szemaforral jelez, nem kell kölcsönös kizárás
 *   Nincs altatás, nincs mutex
 *   Szülő: megvárja a gyerekeket, kiírja az értéket
*/

#include <stdio.h>
#include <stdlib.h>
#include <unistd.h>
#include <sys/ipc.h>
#include <sys/shm.h>
#include <sys/wait.h>
#include "semaphore.h"

// a gyakorlati header, nem kell újraimplementálni
int main() {
    // 1. közös memória létrehozása
    int shmid = shmget(IPC_PRIVATE, sizeof(float), 0600 | IPC_CREAT);
    if (shmid == -1) {
        perror("shmget");
        exit(EXIT_FAILURE);
    }

    float *value = (float *)shmat(shmid, NULL, 0);
    if (value == (void *)-1) {
        perror("shmat");
        exit(EXIT_FAILURE);
    }
    *value = -1.0f; // inicializálás negatív értékkel

    // 2. szemafor létrehozása
    int semid = CSinit(0); // kezdetben 0 → 2. gyerek vár

    pid_t pid1 = fork();
    if (pid1 == 0) {
        // 1. gyerek: hozzáadja a saját PID-jét
        *value += (float)getpid();
        // Jelzés a második gyereknek
        CSsignal(semid);
        _exit(0);
    }

    pid_t pid2 = fork();
    if (pid2 == 0) {
        // 2. gyerek: megvárja az 1. gyereket
        CSwait(semid);
        *value = *value / (float)getpid();
        _exit(0);
    }

    // Szülő: megvárja mindkét gyereket
    wait(NULL);
    wait(NULL);
    printf("Végső érték: %.6f\n", *value);

    // Erőforrások felszabadítása
    shmdt(value);
    shmctl(shmid, IPC_RMID, NULL);
    return 0;
}
