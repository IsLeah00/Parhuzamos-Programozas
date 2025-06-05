# How to Install & Run - Parallel Programming Environments


## GCC (C language)

### Install

```bash
sudo apt update
sudo apt install build-essential
```

### Version Check

```bash
gcc --version
```

### Compile and Run

```bash
gcc hello.c -o hello

./hello
```


## Java (JDK 21+)

### Install

```bash
sudo apt update
sudo apt install openjdk-21-jdk
```

### Version Check

```bash
java -version
```

### Compile and Run

```bash
javac Hello.java

java Hello
```


## Go

### Install

```bash
sudo snap install go --classic
```

or

```bash
wget https://go.dev/dl/go1.22.0.linux-amd64.tar.gz

sudo tar -C /usr/local -xzf go1.22.0.linux-amd64.tar.gz

echo 'export PATH=$PATH:/usr/local/go/bin' >> $HOME/.profile

source $HOME/.profile
```

### Version Check

```bash
go version
```

### Compile and Run

```bash
go build hello.go

./hello
```

or

```bash
go run hello.go
```


## PVM (Parallel Virtual Machine)

### Install

```bash
sudo apt update
sudo apt install pvm pvm-dev
```

### Base

#### Run PVM
```bash
pvm
```

#### Add Machines (eg. hosts)
```bash
conf
```

#### Compile
```bash
pvmcc program.c -o program
```

or

```bash
gcc -I/usr/include/pvm3 program.c -L/usr/lib/x86_64-linux-gnu -lpvm3 -o program
```

or add manually
```bash
sudo nano /usr/local/bin/pvmcc

sudo chmod +x /usr/local/bin/pvmcc
```

and add this content into pvmcc
```bash
#!/bin/bash
gcc -I/usr/include/pvm3 "$@" -L/usr/lib/x86_64-linux-gnu -lpvm3
```

#### Kill
```bash
pkill pvmd
```

#### Run
```bash
spawn program
```

#### Exit PVM
```bash
quit
```
