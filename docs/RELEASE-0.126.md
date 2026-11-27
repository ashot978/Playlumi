# Playlumi 0.126

Android versionCode: **601027**. Package: `nl.playlumi.app`.
Upgradebaseline: **0.123 / 601024**, bron `49899d604f65f70e57128f6dba5f3495f530a70a`.
Ontwikkelbasis: `e94ffdc1002a0938754a5a6d0b487a681a16d750` (de nog niet uitgeleverde 0.125).

## Nieuwe speelinhoud

- **Dierensudoku in het Vormkasteel.** De jongste kinderen vullen een 2×2-bord aan; de volgende leeftijdsgroepen krijgen een 4×4-bord met rijen, kolommen en vakken van 2×2. Het volgende lege vak licht op en het kind kiest het ontbrekende dier. De moeilijkheid groeit met het aantal lege vakken.
- Alle gegenereerde puzzels hebben één oplossing en zijn stap voor stap logisch oplosbaar. Een verkeerde keuze verandert niets aan het bord. De puzzel bewaart seed, voltooide stappen en ingevulde vakken zodat dezelfde ronde na heropenen terugkomt. Alleen een voltooide ronde verdient een beloning.

## Voltooide schermverbeteringen sinds APK 0.123

- **Benoemde horizontale pagina's** voor wereldmenu's, expeditie, ouderzone, kleding en bouwen. De navigatie blijft op een vaste plek. Een nieuwe schermmeting behoudt de zichtbare inhoud waar mogelijk.
- **Lumi blijft zichtbaar bij kleding kiezen**, samen met het echte sterrensaldo. Accessoires staan in afzonderlijke pagina's.
- **Het bouwraster blijft bedienbaar** tijdens het bekijken van onderdelen, de bouwtekening of acties. Onderdelen draaien, Undo/Redo en voortgang blijven behouden.
- **De expeditie opent bij de volgende opdracht.** Rechtstreekse knoppen brengen het kind naar de avonturenkaart en hoofdstukken.
- **Geen verticale scroll.** Verticale beweging kan niet omslaan in een paginawissel. Onzichtbare kaarten achter vaste bediening ontvangen geen nieuwe tikken.
- De compilatiefout uit de onafgeronde 0.125 is opgelost.

## Voortgang en identiteit

De bestaande app-ID, oorspronkelijke illustraties, bewaarde voortgang en oorspronkelijke releasehandtekening blijven in gebruik. Nieuwe spelroutes krijgen een eigen ID; de oude puzzelgeneratoren en opgeslagen versies blijven behouden. De eerder verwijderde zoek-, favorieten-, hervat-, historie-, spelideeën- en speleronderdelen komen niet terug in het menu.

## Controle en oplevering

De Android-workflow moet debug en productie bouwen, alle unit-tests en lint uitvoeren en de ingesloten illustraties en APK-versie verifiëren. De onafhankelijke Java-controles omvatten de nieuwe Sudoku-regels, exact herstel na elke zet en de paginaverdeling.

De gerichte native releasecontrole omvat een echte debug-upgrade vanaf 0.123, het nieuwe spel en de veranderde schermen op een telefoon- en Fold-emulator. De volledige bestaande regressiesuite blijft afzonderlijk beschikbaar; de gerichte controle mag niet als volledige regressie worden gepresenteerd. Productieondertekening gebeurt privé na de CI-gates. Definitieve runlinks, aantallen, bestandscontrole en handtekening worden bij de oplevering vastgelegd.

Een fysieke Samsung Fold-controle is afzonderlijk van emulatoracceptatie. Deze update is een rechtstreeks geleverde test-APK; een merge of Play Store-publicatie maakt geen deel uit van deze release.
