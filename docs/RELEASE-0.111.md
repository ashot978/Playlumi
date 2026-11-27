# Playlumi 0.111

Android version code: **601012**. Package: `nl.playlumi.app`.
Baseline: de geverifieerde [0.110 op 50230b814b6ac9af35fbb1d6464aa831e37e5123](https://github.com/ashot978/Playlumi/commit/50230b814b6ac9af35fbb1d6464aa831e37e5123).
Status: **in ontwikkeling**; de definitieve bouw en acceptatie moeten nog worden uitgevoerd.

## Wijzigingen

- **Drie lokale spelers.** Via de ouderzone kan een ouder kiezen tussen Speler 1, Speler 2 en Speler 3. Iedere speler heeft een eigen leeftijdskeuze, sterren, voortgang, begonnen spellen, favorieten, uiterlijk en tekening. De vaste anonieme labels vragen geen naam of account. Speler 1 behoudt de oorspronkelijke gegevens en bestanden.
- **Een nieuw spel: Wat hoort er niet bij?** In het Speelplein kiest het kind het antwoord dat buiten de genoemde groep valt. De route `4:0:15:5` is beschikbaar voor alle drie leeftijdsgroepen: drie keuzes voor 3–4 jaar, vier voor 5–6 en 7+. Bestaande routes blijven behouden; het beoogde aanbod is 27 / 46 / 72 routes.
- **Kleine tekenafwerking.** De naam van de donkerblauwe kleur krijgt een passende weergave in de smalle Fold-kolom.

## Gerichte controle

De bestaande Java-, unit-, lint-, artwork- en native UI-controles blijven staan. De profielproef gebruikt nu de echte ouderpoort en spelerknoppen, gevolgd door afzonderlijke leeftijden, tekenbestanden, keuzes, sterren en exact herstel van een begonnen quiz. De nieuwe oefening krijgt inhoudscontroles in de bestaande `FoundationsCheck` en een echte native antwoord-/herstelproef.

De upgradebaseline is exact 0.110. De emulatorproef vergelijkt opgeslagen voortgang, vraaginhoud en een echte oude teken-PNG vóór en na `install -r`, met dezelfde tijdelijke debughandtekening. De productie-APK wordt zonder privésleutel in CI gebouwd en pas na controle privé ondertekend. De definitieve resultaten worden bij de oplevering vastgelegd; de eerdere 0.110-resultaten gelden niet automatisch voor deze bouw.

Fysieke Samsung Fold-acceptatie blijft afzonderlijk nodig. Deze testbouw bevat geen opdracht tot merge, openbare publicatie of Play Store-release.
