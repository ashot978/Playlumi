# Playlumi 0.111

Android version code: **601012**. Package: `nl.playlumi.app`.
Baseline: de geverifieerde [0.110 op 50230b814b6ac9af35fbb1d6464aa831e37e5123](https://github.com/ashot978/Playlumi/commit/50230b814b6ac9af35fbb1d6464aa831e37e5123).
Status: **Android-bouw geverifieerd; native acceptatie in uitvoering**. De definitieve bron en acceptatieresultaten worden in [PR #95](https://github.com/ashot978/Playlumi/pull/95) bijgehouden.

## Wijzigingen

- **Drie lokale spelers.** Via de ouderzone kan een ouder kiezen tussen Speler 1, Speler 2 en Speler 3. Iedere speler heeft een eigen leeftijdskeuze, sterren, voortgang, begonnen spellen, favorieten, uiterlijk en tekening. De vaste anonieme labels vragen geen naam of account. Speler 1 behoudt de oorspronkelijke gegevens en bestanden.
- **Een nieuw spel: Wat hoort er niet bij?** In het Speelplein kiest het kind het antwoord dat buiten de genoemde groep valt. De route `4:0:15:5` is beschikbaar voor alle drie leeftijdsgroepen: drie keuzes voor 3–4 jaar, vier voor 5–6 en 7+. Bestaande routes blijven behouden; het beoogde aanbod is 27 / 46 / 72 routes.
- **Kleine tekenafwerking.** De naam van de donkerblauwe kleur krijgt een passende weergave in de smalle Fold-kolom.

## Gerichte controle

De [Android-run 46045790737](https://github.com/ashot978/Playlumi/actions/runs/46045790737) op appbron `5c2066fefbbbce55ae307487950edfebddbfcd4b` bouwde debug en productie, verifieerde de oorspronkelijke illustraties en doorliep **283 unit-tests in 57 suites**, zonder failures, errors of overgeslagen tests. Alle 56 vorige testsuites bleven behouden. Lint rapporteerde **0 fouten en 178 waarschuwingen**; alle vijf onafhankelijke Java-controles slaagden.

De native profielproef gebruikt nu de echte ouderpoort en spelerknoppen, gevolgd door afzonderlijke leeftijden, tekenbestanden, keuzes, sterren en exact herstel van een begonnen quiz. De nieuwe oefening heeft inhoudscontroles in de bestaande `FoundationsCheck` en een echte native antwoord-/herstelproef. De eerste Fold-job stopte vóór emulatorbediening door HTTP 429 bij het ophalen van bestaande buildafhankelijkheden. De baselinebuild herhaalt zulke tijdelijke serverfouten nu maximaal tweemaal; broncodefouten blijven blokkerend.

De upgradebaseline is exact 0.110. De emulatorproef vergelijkt opgeslagen voortgang, vraaginhoud en een echte oude teken-PNG vóór en na `install -r`, met dezelfde tijdelijke debughandtekening. De productie-APK wordt zonder privésleutel in CI gebouwd en pas na controle privé ondertekend. De definitieve resultaten worden bij de oplevering vastgelegd; de eerdere 0.110-resultaten gelden niet automatisch voor deze bouw.

Fysieke Samsung Fold-acceptatie blijft afzonderlijk nodig. Deze testbouw bevat geen opdracht tot merge, openbare publicatie of Play Store-release.
