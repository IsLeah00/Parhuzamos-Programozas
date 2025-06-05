#include <stdio.h>
#include <unistd.h>
#include <stdlib.h>
#include <sys/types.h>

// fork() + folyamatazonosítók példa

int main() {
    printf("=== 1. Egyszerű fork + PID ===\n");

    // Létrehozunk egy új folyamatot
    pid_t pid = fork();

    if (pid < 0) {
        perror("fork sikertelen");
        return 1;
    } else if (pid == 0) {
        // Ez a rész a gyermekfolyamatban fut
        printf("[GYEREK] PID: %d, PPID (szülő PID): %d\n", getpid(), getppid());
    } else {
        // Ez a rész a szülőfolyamatban fut
        printf("[SZÜLŐ] PID: %d, gyermek PID: %d\n", getpid(), pid);
        wait(NULL); // várakozunk a gyermekfolyamat befejezésére
    }

    printf("→ Folyamat [%d] kilép.\n", getpid());

    // ----------------------------------------------
    printf("\n=== 2. Folyamat azonosítók részletesebben ===\n");

    // Új fork - új gyermekfolyamat
    pid_t child = fork();

    if (child == 0) {
        printf("[GYEREK] PID: %d\n", getpid());
        printf("[GYEREK] PPID: %d\n", getppid()); // szülő PID-je
    } else if (child > 0) {
        printf("[SZÜLŐ] PID: %d\n", getpid());
        printf("[SZÜLŐ] PPID: %d\n", getppid()); // ennek a szülője (pl. bash)
        wait(NULL);
    }

    // ----------------------------------------------
    printf("\n=== 3. Memória különbség fork után ===\n");

    int x = 5;

    pid_t pid2 = fork();

    if (pid2 == 0) {
        // gyermek
        printf("[GYEREK] Kezdeti x: %d\n", x);
        x += 10;
        printf("[GYEREK] Módosított x: %d\n", x);
    } else if (pid2 > 0) {
        wait(NULL);
        printf("[SZÜLŐ] Saját x értéke: %d (nincs változás)\n", x);
    }

    // Ez bemutatja, hogy a szülő és gyermek külön memóriát használ a fork után.

    // ----------------------------------------------
    printf("\n=== 4. Processz fa létrehozása fork-okkal ===\n");

    // A folyamatfa szerkezete:
    //
    //         A
    //        / \
    //       B   C
    //      /
    //     D
    //

    pid_t p1 = fork(); // B vagy C létrehozása

    if (p1 == 0) {
        // gyermek: B vagy C
        pid_t p2 = fork();

        if (p2 == 0) {
            // gyermek: D
            printf("[D] PID: %d, PPID: %d\n", getpid(), getppid());
        } else {
            // B
            wait(NULL);
            printf("[B] PID: %d, PPID: %d\n", getpid(), getppid());
        }
    } else {
        // szülő: A
        pid_t p3 = fork();

        if (p3 == 0) {
            // C
            printf("[C] PID: %d, PPID: %d\n", getpid(), getppid());
        } else {
            // A
            wait(NULL);
            wait(NULL);
            printf("[A] PID: %d\n", getpid());
        }
    }

    return 0;
}
