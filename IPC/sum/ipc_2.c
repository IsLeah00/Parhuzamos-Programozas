/**
 * Létrehoz shared memory-t egy egész számhoz
 * Létrehoz egy bináris szemafort (érték: 1)
 * 10 gyerekfolyamatot hoz létre, random alszanak
 * Páros PID: *3, páratlan: +7 a közös változóra
 * Kritikus szakaszban zárolnak
 * Szülő bevárja őket, majd kiírja az értéket
 */

#include <stdio.h>
#include <stdlib.h>
#include <unistd.h>
#include <sys/ipc.h>
#include <sys/shm.h>
#include <sys/wait.h>
#include <time.h>
#include "semaphore.h"

#define NUM_CHILD 10

int main() {
    srand(getpid()); // külön seed a szülő PID alapján

    // 1. shared memory létrehozása
    int shmid = shmget(IPC_PRIVATE, sizeof(int), 0666 | IPC_CREAT);
    if (shmid < 0) {
        perror("shmget");
        exit(EXIT_FAILURE);
    }

    int* value = (int*) shmat(shmid, NULL, 0);
    if (value == (void*) -1) {
        perror("shmat");
        exit(EXIT_FAILURE);
    }

    *value = 1; // inicializáljuk pozitív értékkel


    // 2. szemafor inicializálása
    int semid = CSinit(1); // bináris


    // 3. gyerekfolyamatok létrehozása
    for (int i = 0; i < NUM_CHILD; i++) {
        pid_t pid = fork();

        if (pid < 0) {
            perror("fork");
            exit(EXIT_FAILURE);
        }
        else if (pid == 0) {
            // gyerek ág
            int sleep_time = 1 + rand() % 3;
            sleep(sleep_time);
            CSwait(semid);

            if (getpid() % 2 == 0) {
                *value = (*value) * 3;
            } else {
                *value = (*value) + 7;
            }

            CSsignal(semid);
            _exit(EXIT_SUCCESS);
        }
        // szülő nem csinál semmit itt
    }


    // 4. szülő bevárja a gyerekeket
    for (int i = 0; i < NUM_CHILD; i++) {
        wait(NULL);
    }

    printf("Final value: %d\n", *value);

    // 5. memória felszabadítása
    shmdt(value);
    shmctl(shmid, IPC_RMID, NULL);

    return 0;
}
