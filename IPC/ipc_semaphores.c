#include <stdio.h>
#include <stdlib.h>
#include <unistd.h>
#include <sys/wait.h>
#include <sys/ipc.h>
#include <sys/sem.h>
#include <time.h>

// POSIX sem_op műveletekhez union példa
union semun {
    int val; // érték beállításához
};

void down(int semid) {
    struct sembuf op = {0, -1, 0}; // csökkenti a szemafort (vár, ha 0)
    semop(semid, &op, 1);
}

void up(int semid) {
    struct sembuf op = {0, +1, 0}; // növeli a szemafort (felszabadít)
    semop(semid, &op, 1);
}

int main() {
    printf("=== 1. Szemafor szinkronizáció (szülő ⇄ gyermek) ===\n");

    // Létrehozunk egy új szemafort (kulcs: IPC_PRIVATE, 1 darab, minden jogosultsággal)
    int semid = semget(IPC_PRIVATE, 1, IPC_CREAT | 0666);
    if (semid < 0) {
        perror("semget");
        exit(1);
    }

    // Inicializáljuk 0-ra: a gyermek majd vár rá
    union semun arg;
    arg.val = 0;
    semctl(semid, 0, SETVAL, arg);

    pid_t pid = fork();

    if (pid == 0) {
        // Gyermek: vár, amíg a szülő felszabadítja a szemafort
        printf("[GYEREK] Vár a szemaforra...\n");
        down(semid);
        printf("[GYEREK] Szinkronizált indulás!\n");
        exit(0);
    } else if (pid > 0) {
        // Szülő: késleltet, majd engedélyez
        sleep(2);
        printf("[SZÜLŐ] Jelzés a gyermeknek!\n");
        up(semid);
        wait(NULL); // gyermek befejezésére vár
        semctl(semid, 0, IPC_RMID); // szemafor törlése
    }

    // ----------------------------------------------
    printf("\n=== 2. Kritikus szakasz kezelése több folyamattal ===\n");

    // Új szemafor (érték: 1) – klasszikus bináris szemafor
    int mut_sem = semget(IPC_PRIVATE, 1, IPC_CREAT | 0666);
    if (mut_sem < 0) {
        perror("semget mut");
        exit(1);
    }

    arg.val = 1;
    semctl(mut_sem, 0, SETVAL, arg);

    srand(time(NULL)); // véletlen kezdőmag

    // Három folyamat indítása, mind ugyanazt a kritikus szakaszt használja
    for (int i = 0; i < 3; i++) {
        pid_t p = fork();
        if (p == 0) {
            for (int j = 0; j < 2; j++) {
                down(mut_sem); // belépés a kritikus szakaszba
                printf("[PID %d] Belép a kritikus szakaszba\n", getpid());
                sleep(1 + rand() % 2); // szimulált feldolgozás
                printf("[PID %d] Kilép a kritikus szakaszból\n", getpid());
                up(mut_sem); // kilépés után felszabadítás
                sleep(1);
            }
            exit(0);
        }
    }

    // Szülő: vár minden gyermekre
    for (int i = 0; i < 3; i++) wait(NULL);

    // Töröljük a szemafort
    semctl(mut_sem, 0, IPC_RMID);
    printf("Program vége (szemaforok eltávolítva).\n");

    return 0;
}
