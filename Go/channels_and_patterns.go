package main

import (
	"bufio"
	"fmt"
	"math/rand"
	"os"
	"sync"
	"time"
)

var wg sync.WaitGroup // Globális WaitGroup, amely segít megvárni minden goroutine végét

// ------------------ Egyszerű üzenetcsere szinkron csatornával ------------------

// Ez a goroutine küld egy üzenetet a csatornára, majd vár egy választ
func exchangeA(ch chan string) {
	ch <- "Üzenet A-tól"      // Elküldi saját üzenetét
	reply := <-ch             // Várja a másik válaszát
	fmt.Println("A kapott válasza:", reply)
	wg.Done()                 // Jelzi, hogy befejezte a munkát
}

// Ez a goroutine először fogad egy üzenetet, majd válaszol
func exchangeB(ch chan string) {
	msg := <-ch               // Fogad egy üzenetet
	fmt.Println("B fogadott:", msg)
	ch <- "Üzenet B-től"      // Küld egy választ vissza
	wg.Done()                 // Jelzi a befejezést
}

// ------------------ Pufferelt csatorna példa ------------------

// Termelő: számokat küld egy pufferelt csatornába
func producer(ch chan<- int) {
	for i := 0; i < 5; i++ {
		fmt.Println("Termelő küld:", i)
		ch <- i               // Küldi az aktuális számot
		time.Sleep(1 * time.Second) // Lassítja a küldést, hogy a fogyasztó el tudja érni
	}
	close(ch) // Fontos: a csatorna lezárása jelzi a fogyasztónak, hogy nincs több adat
	wg.Done()
}

// Fogyasztó: olvas a csatornából, amíg van mit
func consumer(ch <-chan int) {
	for item := range ch {   // range automatikusan leáll, ha a csatorna lezáródik
		fmt.Println("Fogyasztó kapta:", item)
		time.Sleep(2 * time.Second)
	}
	wg.Done()
}

// ------------------ Select konstrukció példa ------------------

// Ez a "gyors kliens" rögtön küld egy üzenetet, majd még egyet kis várakozás után
func fastClient(out chan<- string) {
	out <- "Gyors kliens (1)"
	time.Sleep(time.Duration(rand.Intn(4)) * time.Second) // random késleltetés
	out <- "Gyors kliens (2)"
}

// Ez a "lassú kliens" csak késleltetés után küld
func slowClient(out chan<- string) {
	time.Sleep(time.Duration(rand.Intn(4)) * time.Second)
	out <- "Lassú kliens (1)"
}

// ------------------ Csővezeték (pipeline) példa ------------------

// Olvas betűket a standard bemenetről, és ha az betű (a-z), elküldi egy csatornán
// Ha '0'-át olvas, leáll a feldolgozás
func readerStage(input *bufio.Reader, output chan<- int) {
	defer close(output) // Fontos: lezárjuk a kimeneti csatornát, ha már nem küldünk többet

	for {
		b, err := input.ReadByte()
		if err != nil {
			break // Ha nem tudunk tovább olvasni, kilépünk
		}
		if b >= 'a' && b <= 'z' {
			output <- int(b) // csak kisbetűket küldünk tovább
		} else if b == '0' {
			break // '0' jelzi a feldolgozás végét
		}
	}
}

// Fogad egy számot, amit karakterként kezel, és kiírja a stderr-re nagybetűs változatban
func transformerStage(input <-chan int, writer *bufio.Writer) {
	defer wg.Done()

	for v := range input {
		writer.WriteByte(byte(v - 32)) // átalakítás kisbetűből nagybetűvé ASCII alapján
		writer.Flush()                 // azonnal kiírjuk
	}
}

// ------------------ main függvény ------------------

func main() {
	// --- Szinkron csatorna példa ---
	fmt.Println("== Szinkron kommunikáció két goroutine között ==")
	ch1 := make(chan string) // Nincs puffer → mindig blokk, amíg a másik fél nem fogad
	wg.Add(2)                // Két goroutine-ra várunk
	go exchangeA(ch1)
	go exchangeB(ch1)
	wg.Wait()

	// --- Pufferelt csatorna példa ---
	fmt.Println("\n== Termelő-fogyasztó modell pufferelt csatornával ==")
	ch2 := make(chan int, 1) // 1 elem méretű puffer: nem kell azonnal olvasni
	wg.Add(2)
	go producer(ch2)
	go consumer(ch2)
	wg.Wait()

	// --- Select példa ---
	fmt.Println("\n== Select: aszinkron fogadás több forrásból ==")
	client1 := make(chan string)
	client2 := make(chan string)

	go fastClient(client1)
	go slowClient(client2)

	for i := 0; i < 3; i++ {
		select {
		case msg := <-client1:
			fmt.Println("Fogadtuk:", msg)
		case msg := <-client2:
			fmt.Println("Fogadtuk:", msg)
		default:
			fmt.Println("Nincs új üzenet...") // ha egyik csatorna sem elérhető, ez fut
		}
		time.Sleep(1 * time.Second)
	}

	// --- Pipeline példa ---
	fmt.Println("\n== Pipeline példa: karakterek átalakítása ==")
	fmt.Println("Írj kisbetűket, majd '0'-t a lezáráshoz:")

	charChannel := make(chan int)
	reader := bufio.NewReader(os.Stdin)
	writer := bufio.NewWriter(os.Stderr)

	wg.Add(1)
	go readerStage(reader, charChannel)
	go transformerStage(charChannel, writer)
	wg.Wait()

	fmt.Println("\nProgram vége.")
}
