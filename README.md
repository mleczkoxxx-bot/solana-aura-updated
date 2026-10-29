# Solana Aura – Fabric 1.21.11 (Yarn), mod kliencki

- Ciągła fioletowa linia (smuga) za graczem, zanikająca po ~1,5 s
- 6 orbitujących logo Solany wokół gracza – widoczne także w pierwszej osobie

Wszystko oparte o cząsteczki, więc w ustawieniach grafiki "Cząsteczki" muszą być na "Wszystkie".

## Ustawienia w kodzie
- SolanaLogoParticle: COUNT, RADIUS, SIZE, SPEED
- TrailParticle: LIFETIME, WIDTH, GRADIENT_TO_GREEN (fiolet -> zieleń)
- SolanaAuraClient: TRAIL_SPACING, TRAIL_HEIGHT

## Budowanie
GitHub Actions (plik .github/workflows/build.yml) buduje jar automatycznie po każdym wgraniu zmian.
Gotowy jar: Actions -> ostatnie uruchomienie -> Artifacts -> solana-aura-jar.
Lokalnie: JDK 21 + `gradle build` (Gradle 9.x).
