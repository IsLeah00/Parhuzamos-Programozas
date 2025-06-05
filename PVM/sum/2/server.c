#include <stdio.h>
#include <stdlib.h>
#include "pvm3.h"

int main() {
    int red_tid, blue_tid[3];
    double num = 2.5;

    // Piros kliens
    pvm_spawn("zh4_red", NULL, PvmTaskDefault, "", 1, &red_tid);

    // Kék kliensek
    pvm_spawn("zh4_blue", NULL, PvmTaskDefault, "", 3, blue_tid);

    // Küldés a kék klienseknek
    for (int i = 0; i < 3; i++) {
        pvm_initsend(PvmDataDefault);
        pvm_pkdouble(&num, 1, 1);
        pvm_pkint(&red_tid, 1, 1);
        pvm_send(blue_tid[i], 1);
    }

    // Eredmény fogadása a piros klienstől
    pvm_recv(red_tid, 2);
    double result;
    pvm_upkdouble(&result, 1, 1);
    printf("Összegzett érték: %.2f\n", result);

    pvm_exit();
    return 0;
}
