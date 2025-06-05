/*
	Feladat összefoglalása
		Olvasd a billentyűleütéseket bufio.Reader-rel stdin-ről
		Stop karakter: TAB (\t)
		Karakterek osztályozása:
			Kisbetű (index 0)
			Nagybetű (index 1)
			Számjegy (index 2)
		Kommunikáció: egyirányú chan int
		Használj WaitGroup-ot a szabályos kilépéshez
*/

package main

import (
	"bufio"
	"fmt"
	"os"
	"sync"
	"unicode"
)

func main() {
	counts := make([]int, 3)	// [kisbetű, nagybetű, szám]
	ch := make(chan int)		// csak küldés
	var wg sync.WaitGroup
	wg.Add(2)

	// A: olvasó goroutine
	go func() {
		defer wg.Done()
		reader := bufio.NewReader(os.Stdin)
		for {
			r, _, err := reader.ReadRune()

			if err != nil {
				break
			}

			if r == '\t' {
				break
			}

			if unicode.IsLower(r) {
				ch <- 0
			} else if unicode.IsUpper(r) {
				ch <- 1
			} else if unicode.IsDigit(r) {
				ch <- 2
			}
		}
		close(ch)
	}()


	// B: számláló goroutine
	go func() {
		defer wg.Done()
		for i := range ch {
			counts[i]++
		}
	}()


	wg.Wait()
	fmt.Println("Statisztika:")
	fmt.Printf("Kisbetű: %d\n", counts[0])
	fmt.Printf("Nagybetű: %d\n", counts[1])
	fmt.Printf("Számjegy: %d\n", counts[2])
}
