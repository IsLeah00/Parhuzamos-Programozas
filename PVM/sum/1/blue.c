#include <stdio.h>
#include "pvm3.h"

int main() {
    int val, red_tid;

    // szervertől fogadjuk
    pvm_recv(-1, 1);
    pvm_upkint(&val, 1, 1);
    pvm_upkint(&red_tid, 1, 1);

    int cube = val * val * val;

    pvm_initsend(PvmDataDefault);
    pvm_pkint(&cube, 1, 1);
    pvm_send(red_tid, 3); // továbbítjuk a pirosnak
    pvm_exit();
    return 0;
}
