# Playlumi 0.29.0 — 300 vraagkaarten per spel, spelvorm en niveau

## Welke spellen
- 24 leeractiviteiten (Tellen t/m Slim nadenken).
- Alle 5 oefenvormen per activiteit, op alle 3 moeilijkheidsgraden.
- Dat zijn 24 × 5 × 3 = 360 onafhankelijk samengestelde vraagdecks.
- Ieder deck bevat **300 zichtbare, verschillende vraagkaarten**. Er staan geen 108.000 permanente gegevens in het geheugen: alleen het geselecteerde deck wordt opgebouwd, volledig offline.
- Bij basisonderwerpen zoals drie kleuren of vormen oefent het kind hetzelfde leerbegrip via verschillende keuzelijsten en korte thematische vraagkaarten. Er bestaan niet 300 onafhankelijke feiten per basiskleur; de vraagkaarten zijn gevarieerde oefenmomenten, geen 300 nieuwe leervaardigheden.

## Willekeurig maar zonder herhaling binnen een deck
- Het game-engine maakt bij het openen van een spelvorm een nieuw deck en husselt dat met een nieuwe willekeurige volgorde.
- Elke kaart komt maximaal eenmaal voor voordat het deck van 300 op is.
- Na 300 kaarten wordt het deck opnieuw geschud. Eerder opgeloste kaarten krijgen hierbij een nieuwe, onopgeloste toestand.
- De bestaande lokale historielijst beperkt kort op elkaar voorkomende oefeningen en blijft werken.
- Bij **Nog een ronde!** maakt de app ook een nieuwe shuffle. Dat geldt tevens voor **Speel meteen** en een nieuwe spelvorm/niveau.
- Een normale speelronde blijft kort: 8 vragen. De husselfunctie verandert geen score, badges, opgeslagen profielen of spelregels.

## Extra minispellen
- De 3 interactieve minispellen (Sleep de vormen, Tik de ballon, Memory) zijn geen gewone meerkeuzevraagspellen.
- Hun opstellingen worden al willekeurig gegenereerd bij het openen. Deze update wijzigt de spelborden niet en claimt daar nog geen deck van 300 inhoudelijke vragen.

## Veiligheid en verificatie
- Java-tests controleren alle 24×5×3 decks op 300 unieke, geldige vraagkaarten met één correct antwoord, plus een nieuwe shuffle en recent-history-compatibiliteit.
- Verder Android-unit-tests, lint, Gradle-build en emulator-opstarttest.
- Een fysieke Samsung-test en het doorlopen van alle vraagkaarten op het toestel blijven nodig om gedrag op dat toestel te bevestigen.
- De APK wordt lokaal ondertekend met het oorspronkelijke Playlumi-certificaat, nooit via GitHub-secrets, en krijgt een versiecode boven 14233.
