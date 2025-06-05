package main

import (
	"fmt"
	"math/rand"
	"sync"
	"time"
)

var wg sync.WaitGroup // Globális várakozó csoport goroutine-okhoz

// ------------------ Függvények és alapok ------------------

// Visszatér egy karakterrel, szám alapján
func categorize(n int) string {
	switch n {
	case 1:
		return "Első kategória"
	case 2:
		return "Második kategória"
	default:
		return "Ismeretlen kategória"
	}
}

// Kiszámítja az intervallumbeli számok összegét
func intervalSum(start, end int) (int, error) {
	if start > end {
		return 0, fmt.Errorf("Érvénytelen intervallum: start nagyobb mint end")
	}
	total := 0
	for i := start; i < end; i++ {
		total += i
	}
	return total, nil
}

// Megkeresi a legnagyobb elemet és annak indexét egy tömbben
func findMax(nums []int) (max int, index int) {
	max = nums[0]
	index = 0
	for i, v := range nums {
		if v > max {
			max = v
			index = i
		}
	}
	return
}

// ------------------ Goroutine + Defer példák ------------------

// Szöveget és sorszámot ír ki többször, kis várakozással
func repeatPrint(label string) {
	for i := 0; i < 10; i++ {
		fmt.Println(label, i)
		time.Sleep(200 * time.Millisecond)
	}
	wg.Done() // Jelzi a goroutine-nak, hogy végzett
}

// Defer segítségével késleltetett kiírás
func delayedPrint(tag string) {
	defer fmt.Println("Függvény vége:", tag)
	fmt.Println("Függvény kezdete:", tag)
}

// ------------------ main függvény ------------------

func main() {
	// Véletlenszám generálás
	fmt.Println("Üdv! Véletlenszámom:", rand.Intn(10))

	// Változók és konstans használata
	const pi float64 = 3.1415
	message := "Helló Go!"
	fmt.Println("Üzenet:", message, "| PI érték:", pi)

	// Egyszerű switch-alapú függvény
	fmt.Println("Kategória:", categorize(2))

	// Intervallumösszeg függvény teszt
	if result, err := intervalSum(2, 6); err != nil {
		fmt.Println("Hiba:", err)
	} else {
		fmt.Println("Intervallum összege:", result)
	}

	// Tömb legnagyobb elemének keresése
	numbers := []int{4, 9, 1, 12, 7}
	max, idx := findMax(numbers)
	fmt.Println("Max elem:", max, "| Pozíció:", idx, "| Tömb:", numbers)

	// Névtelen függvény egyszeri futtatása
	func() {
		fmt.Println("Ez egy egyszer futó, névtelen függvény.")
	}()

	// Defer példa
	delayedPrint("Defer teszt")

	// Goroutine indítása és szinkronizálása
	wg.Add(2) // Két goroutine-ra várunk
	go repeatPrint("Goroutine A") // új szál
	go repeatPrint("Goroutine B") // új szál
	wg.Wait() // Várakozás a goroutine-ok befejeződéséig

	fmt.Println("Program vége.")
}
