/*
	Feladat összefoglalása
		Beolvasás: stdin-ról byte-onként
		Pipeline:
			1. goroutine: csak a számjegyeket ('0'..'9') továbbítja
			2. goroutine: összegez, és ha ' ' (space) jön, kiírja az eddigi összeget
		Csatorna: chan byte buffere: 3
		Goroutine koordináció: sync.WaitGroup
*/

package main

import (
	"bufio"
	"fmt"
	"os"
	"sync"
)

func main() {
	reader := bufio.NewReader(os.Stdin)
	ch := make(chan byte, 3)
	var wg sync.WaitGroup
	wg.Add(2)

	// Goroutine 1 – számjegyek továbbítása
	go func() {
		defer wg.Done()
		for {
			b, err := reader.ReadByte()
			if err != nil {
				close(ch)
				return
			}
			if b >= '0' && b <= '9' {
				ch <- b
			} else if b == ' ' {
				ch <- b // továbbítjuk a szóközt is
		}
		}
	}()

	// Goroutine 2 – összegzés és kiírás
	go func() {
		defer wg.Done()
		sum := 0
		for b := range ch {
			if b == ' ' {
				fmt.Println("Összeg:", sum)
				sum = 0
			} else {
				sum += int(b - '0')
			}
		}
	}()

	wg.Wait()
}
