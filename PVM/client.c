#include <stdio.h>
#include "pvm3.h"

// pvmcc merged-client.c -o client
// pvm -> spawn client

int main() {
    int mytid = pvm_mytid();
    int x;

    // == 1. Azonosító elküldése ==
    pvm_initsend(PvmDataDefault);
    pvm_pkint(&mytid, 1, 1);
    pvm_send(pvm_parent(), 1);

    // == 2. Hello üzenet fogadása ==
    char buffer[100];
    pvm_recv(-1, 2);
    pvm_upkstr(buffer);
    printf("Kliens üzenetet kapott: %s\n", buffer);

    // == 3. Csővezeték fogadása ==
    printf("Kliens számokat kap:\n");
    while (1) {
        pvm_recv(-1, 2);
        pvm_upkint(&x, 1, 1);
        if (x == -1) break;
        printf("  -> %d\n", x);
    }

    // == 4. Számok küldése összeadásra ==
    int data[] = {3, 5, 7, -1};
    for (int i = 0; data[i] != -1; ++i) {
        pvm_initsend(PvmDataDefault);
        pvm_pkint(&data[i], 1, 1);
        pvm_send(pvm_parent(), 2);
    }
    // Végjel -1 küldése
    x = -1;
    pvm_initsend(PvmDataDefault);
    pvm_pkint(&x, 1, 1);
    pvm_send(pvm_parent(), 2);

    // == 5. Összeg fogadása ==
    pvm_recv(-1, 3);
    pvm_upkint(&x, 1, 1);
    printf("Kliens megkapta az összeget: %d\n", x);

    pvm_exit();
    return 0;
}
