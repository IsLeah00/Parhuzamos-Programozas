#include <stdio.h>
#include <stdlib.h>
#include "pvm3.h"

int main() {
    double total = 0.0;

    for (int i = 0; i < 3; i++) {
        pvm_recv(-1, 3); // fogad kékektől
        double val;
        pvm_upkdouble(&val, 1, 1);
        total += val;
    }

    int server_tid = pvm_parent();

    pvm_initsend(PvmDataDefault);
    pvm_pkdouble(&total, 1, 1);
    pvm_send(server_tid, 2); // tag 2: red -> server

    pvm_exit();
    return 0;
}
