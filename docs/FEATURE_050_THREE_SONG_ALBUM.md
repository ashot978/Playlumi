# Playlumi 0.50.0 — Drie eigen liedjes en een zelfgekozen tempo

## Wat is nieuw?
- Kinderen krijgen in de Muziekdoos **drie eigen melodieplekjes**: ⭐ Sterrenliedje, 🌼 Bloemenliedje en 🌙 Maanliedje.
- Iedere plek bewaart een andere zelfgemaakte melodie van maximaal twaalf tonen.
- Het eigen deuntje uit 0.49.0 blijft vanzelf behouden als het eerste Sterrenliedje, zonder dat een bestaande opslagwaarde wordt overschreven.
- Met **🥁 Tempo** kunnen kinderen hun melodie rustig, normaal of snel laten afspelen.
- De **⏹ Stop**-knop staat direct naast **▶ Speel mijn deuntje**.
- Wisselen tussen liedjes, wisselen van tempo, stoppen of naar een ander scherm gaan onderbreekt eerdere afspeelacties.
- Alle drie de liedjes, de gekozen muziekplek en het gekozen tempo worden alleen op het apparaat en per kindprofiel bewaard.

## Wat blijft hetzelfde?
- Geen menselijke of synthetische voorleesstem: alle muziek blijft instrumentaal.
- De bestaande twaalf vrije Muziekdoos-deuntjes, de vier instrumenten en alle andere spelgeluiden blijven.
- Er start geen muziek vanzelf. Er zijn geen sterren, punten, badges of andere beloningen voor het componeren.
- Geen nieuwe accounts, netwerktoegang, advertenties, microfoon of permissies.
- Scores, vragen, spelwereld, ouderbeveiliging en alle andere voortgang blijven ongewijzigd.

## Tests
Nieuwe unit-tests controleren dat het oorspronkelijke liedje bij het upgraden behouden blijft, de drie liedjes onafhankelijk zijn, ongeldige opslag niet doorwerkt, en de tempo's veilig blijven. GitHub Actions voert unit-tests, Android-lint, debugbuild, emulatorstart en unsigned releasebuild uit. Daarna wordt de APK met het bestaande certificaat privé ondertekend en geverifieerd.
