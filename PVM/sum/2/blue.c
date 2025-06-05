#include <stdio.h>
#include <stdlib.h>
#include "pvm3.h"

int main() {
    double input;
    int red_tid;

    pvm_recv(-1, 1); // vár a szervertől
    pvm_upkdouble(&input, 1, 1);
    pvm_upkint(&red_tid, 1, 1);

    double doubled = input * 2;

    pvm_initsend(PvmDataDefault);
    pvm_pkdouble(&doubled, 1, 1);
    pvm_send(red_tid, 3); // tag 3: blue -> red

    pvm_exit();
    return 0;
}
