package main

import (
	"bufio"
	"fmt"
	"math/rand"
	"os"
	"sync"
	"time"
)

// WaitGroup arra való, hogy a main függvény megvárhassa a goroutine-ok végét
var wg sync.WaitGroup

// ========== 1. Egyszerű szinkron kommunikáció ==========

// Ez a goroutine először küld egy üzenetet, majd várja a választ
func exchangeA(ch chan string) {
	ch <- "Üzenet A-tól" // Küldés a csatornára (blokkoló)
	reply := <-ch        // Várakozás a válaszra
	fmt.Println("A kapott válasza:", reply)
	wg.Done()            // Végeztünk, jelezzük a WaitGroup-nak
}

// Ez a goroutine először fogad egy üzenetet, majd válaszol
func exchangeB(ch chan string) {
	msg := <-ch           // Vár az üzenetre
	fmt.Println("B fogadott:", msg)
	ch <- "Üzenet B-től"  // Küldi a választ
	wg.Done()
}

// ========== 2. Pufferelt csatorna: termelő-fogyasztó modell ==========

// Termelő egyesével küldi a számokat a csatornába
func producer(ch chan<- int) {
	for i := 0; i < 5; i++ {
		fmt.Println("Termelő küld:", i)
		ch <- i                     // Küldés a pufferelt csatornára (nem mindig blokkol)
		time.Sleep(1 * time.Second) // Kis késleltetés
	}
	close(ch) // Fontos: jelezni kell, hogy nincs több adat
	wg.Done()
}

// Fogyasztó olvassa a csatornát, amíg az nyitva van
func consumer(ch <-chan int) {
	for item := range ch { // range automatikusan leáll, ha ch bezárul
		fmt.Println("Fogyasztó kapta:", item)
		time.Sleep(2 * time.Second) // Lassabban dolgozik
	}
	wg.Done()
}

// ========== 3. Select konstrukció: több csatorna figyelése egyszerre ==========

// Ez a kliens azonnal küld egy üzenetet, majd később még egyet
func fastClient(out chan<- string) {
	out <- "Gyors kliens (1)"                          // azonnali küldés
	time.Sleep(time.Duration(rand.Intn(4)) * time.Second) // random késés
	out <- "Gyors kliens (2)"
}

// Ez a kliens csak késleltetéssel küld egy üzenetet
func slowClient(out chan<- string) {
	time.Sleep(time.Duration(rand.Intn(4)) * time.Second)
	out <- "Lassú kliens (1)"
}

// ========== 4. Pipeline: karakterek feldolgozása csatornán át ==========

// Ez a goroutine karaktereket olvas be a felhasználótól
// Csak kisbetűket enged tovább a csatornára, '0'-ra megáll
func readerStage(input *bufio.Reader, output chan<- int) {
	defer close(output) // Ha kész vagyunk, lezárjuk a csatornát

	for {
		b, err := input.ReadByte() // Egy karakter beolvasása
		if err != nil {
			break // ha hiba (pl. EOF), leállunk
		}
		if b >= 'a' && b <= 'z' {
			output <- int(b) // csak kisbetűket engedünk tovább
		} else if b == '0' {
			break // '0' karakter a feldolgozás végét jelzi
		}
	}
}

// Ez a goroutine nagybetűre konvertálja a kapott karaktereket
func transformerStage(input <-chan int, writer *bufio.Writer) {
	defer wg.Done()

	for v := range input { // Várja a karaktereket
		writer.WriteByte(byte(v - 32)) // ASCII: kisbetű - 32 = nagybetű
		writer.Flush()                 // Azonnali kiírás
	}
}

// ========== main() ==========

func main() {
	// 1. Szinkron kommunikáció példa
	fmt.Println("== Szinkron kommunikáció két goroutine között ==")
	ch1 := make(chan string) // szinkron, nem pufferelt csatorna
	wg.Add(2)
	go exchangeA(ch1)
	go exchangeB(ch1)
	wg.Wait() // main itt megvárja, amíg A és B befejezik

	// 2. Pufferelt csatorna példa
	fmt.Println("\n== Termelő-fogyasztó modell pufferelt csatornával ==")
	ch2 := make(chan int, 1) // 1-es méretű pufferrel
	wg.Add(2)
	go producer(ch2)
	go consumer(ch2)
	wg.Wait()

	// 3. Select példa
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
			fmt.Println("Nincs új üzenet...") // ha egyik csatorna sem elérhető épp
		}
		time.Sleep(1 * time.Second) // kis szünet a következő select előtt
	}

	// 4. Pipeline példa: kisbetűk nagybetűsítése
	fmt.Println("\n== Pipeline példa: karakterek átalakítása ==")
	fmt.Println("Írj kisbetűket, majd '0'-t a lezáráshoz:")

	charChannel := make(chan int) // karakterek csatornája
	reader := bufio.NewReader(os.Stdin)
	writer := bufio.NewWriter(os.Stderr)

	wg.Add(1)
	go readerStage(reader, charChannel)
	go transformerStage(charChannel, writer)
	wg.Wait()

	fmt.Println("\nProgram vége.")
}
