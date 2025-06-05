#include <stdio.h>
#include "pvm3.h"

// pvmcc merged-server.c -o server
// pvm -> spawn server
int main() {
    int tids[1];
    int x, sum = 0;

    // == 1. Azonosító fogadása ==
    pvm_recv(-1, 1);
    pvm_upkint(tids, 1, 1);

    // == 2. Hello üzenet küldése ==
    char hello[] = "Üdv a szervertől!";
    pvm_initsend(PvmDataDefault);
    pvm_pkstr(hello);
    pvm_send(tids[0], 2);

    // == 3. Csővezetékes küldés (számok -1-ig) ==
    int values[] = {2, 4, 6, 8, -1};
    for (int i = 0; values[i] != -1; ++i) {
        pvm_initsend(PvmDataDefault);
        pvm_pkint(&values[i], 1, 1);
        pvm_send(tids[0], 2);
    }
    // -1 küldése a lezáráshoz
    pvm_initsend(PvmDataDefault);
    x = -1;
    pvm_pkint(&x, 1, 1);
    pvm_send(tids[0], 2);

    // == 4. Összegzés fogadása (-1 jelig) ==
    while (1) {
        pvm_recv(tids[0], 2);
        pvm_upkint(&x, 1, 1);
        if (x == -1) break;
        sum += x;
    }

    // == 5. Eredmény visszaküldése ==
    pvm_initsend(PvmDataDefault);
    pvm_pkint(&sum, 1, 1);
    pvm_send(tids[0], 3);

    printf("Szerver kiszámolta az összeget: %d\n", sum);

    pvm_exit();
    return 0;
}
