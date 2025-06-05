#include <stdio.h>
#include <stdlib.h>
#include "pvm3.h"

int main() {
    int num = 3; // küldendő érték
    int blue_tid, red_tid;

    pvm_spawn("zh4_blue", NULL, PvmTaskDefault, "", 1, &blue_tid);
    pvm_spawn("zh4_red", NULL, PvmTaskDefault, "", 1, &red_tid);

    // küldés a kék kliensnek
    pvm_initsend(PvmDataDefault);
    pvm_pkint(&num, 1, 1);
    pvm_pkint(&red_tid, 1, 1); // adjuk át a piros tid-jét is
    pvm_send(blue_tid, 1);

    // válasz fogadása a piros klienstől
    pvm_recv(red_tid, 2);
    int result;
    pvm_upkint(&result, 1, 1);

    printf("Végső eredmény: %d\n", result);
    pvm_exit();
    return 0;
}
