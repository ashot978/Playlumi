# Playlumi 0.41.0 - een vriendelijk hulpknopje in de leerwereld

## Kindvriendelijk leren
- Bij meerkeuzevragen met minimaal drie antwoorden verschijnt de knop "💡 Help mij".
- Elke tik haalt precies één verkeerd antwoord weg; in plaats daarvan verschijnt een rustig wolkje.
- Er blijven altijd minimaal twee antwoorden over, waarvan één het juiste is.
- Een kind blijft zelf het goede antwoord kiezen. Een hint start geen nieuwe ronde, kent geen sterren toe en verandert de scores niet.
- Het hulpknopje verdwijnt zodra nog twee keuzes overblijven of het kind de vraag goed beantwoordt.
- Ontbreekt er een derde antwoord, dan is de knop niet nodig en wordt hij niet getoond.

## Toegankelijk en privacyvriendelijk
- Het weggehaalde antwoord is niet meer aanraakbaar of focusbaar voor een schermlezer.
- Een korte tekst laat weten hoeveel opties overblijven; de melding wordt ook toegankelijk uitgesproken door het systeem.
- De oorspronkelijke vraag, het juiste antwoord en de 300-vragenverzamelingen blijven ongewijzigd.
- Hints gelden alleen voor de actuele spelvraag, worden niet opgeslagen en vragen geen internettoegang, account of nieuwe permissie.
- De app-ID en het bestaande ondertekeningscertificaat blijven gelijk.

## Tests en uitgave
- Unit-tests controleren alle juiste antwoordposities bij 2 tot 16 keuzes, dat er altijd twee overblijven, ongeldige maskers en dat punten/vragen niet wijzigen.
- Unit-tests, Android-lint, debugbuild, emulator-opstart en unsigned releasebuild verplicht.
- Privé ondertekenen met de permanente sleutel en daarna de nieuwe APK controleren.
- Geen merge naar main of store-publicatie.
