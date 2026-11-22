# Playlumi 0.48.0 - De Muziekdoos

Kinderen kunnen nu zelfstandig uit twaalf vrolijke originele melodietjes
kiezen, zonder menselijke stem of ingewikkelde muziekvragen.

## Nieuw
- De Muziekdoos staat op het startscherm en bij Spelletjes.
- Twaalf korte, instrumentale melodieën op grote kleurrijke knoppen, verdeeld over twee pagina's.
- Verrassingsdeuntje kiest een andere melodie dan het laatst gekozen exemplaar.
- Stop muziek onderbreekt een lopende melodie onmiddellijk.
- Een andere melodie kiezen stopt de vorige. Navigeren, pauzeren en toestelrotatie stoppen ook de muziek.
- Niets speelt automatisch bij het openen van een scherm.

## Behoud en tests
De muziekdoos deelt geen punten, sterren of badges uit en verandert geen vraag of profiel. Alle leeractiviteiten, de 300-vragenstapels, het Muziekatelier, de tuin en ouderinstellingen blijven bestaan. De algemene geluidschakelaar blijft leidend; er zijn geen nieuwe permissies, internetverbindingen of downloads nodig.

Unit-tests controleren unieke WAV-melodieën, paginering en verrassingskeuzes zonder herhaling. GitHub Actions controleert de Android-tests, lint, debugbuild en emulatorstart. De APK wordt met het oorspronkelijke certificaat ondertekend en op app-ID, versiecode en handtekening gecontroleerd.
