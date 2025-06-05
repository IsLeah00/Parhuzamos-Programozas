#include <stdio.h>
#include "pvm3.h"

int main() {
    int cube;

    // fogad a kéktől
    pvm_recv(-1, 3);
    pvm_upkint(&cube, 1, 1);

    int doubled = cube * 2;

    int server_tid = pvm_parent();
    pvm_initsend(PvmDataDefault);
    pvm_pkint(&doubled, 1, 1);
    pvm_send(server_tid, 2);
    pvm_exit();
    return 0;
}
