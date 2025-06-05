#include <sys/sem.h> 

int CSinit(int value) {

  int semid = semget(IPC_PRIVATE, 1, 0666 | IPC_CREAT);
  semctl(semid, 0, SETVAL, value); 

  return semid;
}

static void CSoper(int semid, int op) {
  struct sembuf sb;

  sb.sem_num = 0; 
  sb.sem_flg = 0; 
  sb.sem_op = op; 

  semop(semid, & sb, 1);
}

void CSwait(int semid) {
  CSoper(semid, -1);
}

void CSsignal(int semid) {
  CSoper(semid, 1);
}
