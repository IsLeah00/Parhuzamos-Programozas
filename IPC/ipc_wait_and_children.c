#include <stdio.h>
#include <stdlib.h>
#include <unistd.h>
#include <sys/wait.h>

// gyermekfolyamatok befejezése a szülőfolyamatban példa

int main() {
    printf("=== 1. wait() használata ===\n");

    pid_t pid = fork();

    if (pid == 0) {
        // Gyermekfolyamat: egyszerű üzenet, majd kilép
        printf("[GYEREK] PID: %d elindult\n", getpid());
        sleep(2);
        printf("[GYEREK] Befejezte a futást.\n");
        exit(0);
    } else if (pid > 0) {
        // Szülő: megvárja a gyermek befejezését
        printf("[SZÜLŐ] Vár a gyermek befejezésére...\n");
        wait(NULL);
        printf("[SZÜLŐ] A gyermek befejeződött, folytatom.\n");
    }

    // ----------------------------------------------
    printf("\n=== 2. Korlátozott számú gyermek futtatása ===\n");

    int max_parallel = 3;     // egyszerre ennyi gyerek lehet aktív
    int total = 7;            // összesen ennyit szeretnénk indítani
    int active = 0;           // pillanatnyilag aktív gyermekek száma

    for (int i = 0; i < total; i++) {
        if (active == max_parallel) {
            wait(NULL); // várunk egy gyermek befejezésére
            active--;
        }

        pid_t cpid = fork();

        if (cpid == 0) {
            printf("[GYEREK #%d] PID: %d fut, PPID: %d\n", i, getpid(), getppid());
            sleep(1 + (rand() % 3)); // eltérő futásidő szimulálása
            printf("[GYEREK #%d] PID: %d befejez.\n", i, getpid());
            exit(0);
        } else if (cpid > 0) {
            active++;
        }
    }

    // Minden hátramaradt gyermek befejezésének megvárása
    while (active > 0) {
        wait(NULL);
        active--;
    }

    // ----------------------------------------------
    printf("\n=== 3. Gyermek exit kód és visszatérési érték kezelése ===\n");

    pid_t child1 = fork();

    if (child1 == 0) {
        // 1. gyermek – kilép 42-vel
        exit(42);
    } else {
        int status;
        waitpid(child1, &status, 0);

        if (WIFEXITED(status)) {
            int code = WEXITSTATUS(status);
            printf("[SZÜLŐ] A gyermek exit kódja: %d\n", code); // várhatóan 42
        }
    }

    pid_t child2 = fork();

    if (child2 == 0) {
        // 2. gyermek – kilép 7-tel
        exit(7);
    } else {
        int status;
        waitpid(child2, &status, 0);

        if (WIFEXITED(status)) {
            printf("[SZÜLŐ] Második gyermek exit kódja: %d\n", WEXITSTATUS(status));
        }
    }

    printf("\nProgram vége.\n");
    return 0;
}
