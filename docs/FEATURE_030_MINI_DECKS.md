# Playlumi 0.30.0 — 300 willekeurig geschudde minispelborden

## De drie interactieve minispellen
- **Sleep de vormen**: 300 unieke combinaties van doelvorm, vormen en hun plekken op het bord, per moeilijkheidsniveau.
- **Tik de ballon**: 300 unieke cijfer- en doelcombinaties per moeilijkheidsniveau.
- **Memory**: 300 unieke dierensets en kaartverdelingen per moeilijkheidsniveau.
- Dit zijn **speelbordopstellingen**, niet 300 verschillende natuurkundige of taalkundige feiten. Ook een kind op beginnersniveau ziet voldoende afwisseling.

## Hoe het werkt
- Bij iedere opening van een van deze drie minispellen maakt de app offline een nieuw deck van 300.
- De opstellingen worden telkens in een andere volgorde gehusseld.
- Binnen het deck worden geen identieke borden herhaald.
- Na alle 300 opstellingen wordt vanzelf een vers deck gemaakt en geschud.
- De bestaande speelsessie blijft **20 uitdagingen**.
- De knop 'Nog eens spelen!' begint ook met een vers gehusseld deck.
- De eerste moeilijkheidsgraad heeft nog steeds maar drie tegelijk zichtbare keuzes; er zijn wel zeven duidelijke vormsymbolen in de mogelijke voorraad, zodat 300 daadwerkelijk verschillende vormborden mogelijk zijn.

## Wat onveranderd blijft
- Werkelijk slepen blijft verplicht voor Sleep de vormen; tikken telt niet.
- Memory blijft dierenparen zoeken, de juiste paren verdwijnen niet.
- Foute pogingen geven geen nieuwe straf.
- Scores, sterren, badges, animaties, opgeslagen profielen, familie-instellingen en volledig offline spelen blijven zoals ze waren.

## Tests en publicatie
- Nieuw: Java-tests voor **3 × 3 × 300 = 2.700** afzonderlijke minispelborden; elk met correcte doelen en kaartverdelingen.
- Daarnaast tests voor een nieuwe shuffle na heropenen, correcte Memory-completie en het automatisch opnieuw schudden na 300 borden.
- Behoud Gradle-tests, lint, Android emulator-opstartcheck en release-APK.
- Ongewijzigde package `nl.playlumi.app` en oorspronkelijke privé-ondertekeningssleutel; hogere versionCode dan 15236.
- **Nog niet inbegrepen:** Bellenfeest en Muziekatelier werken anders en worden apart verder uitgebreid; ze zijn geen deel van de 300 minispelbord-decks van deze release.
- Een Samsung-toesteltest en volledige handmatige spelronde blijven te verifiëren vóór een definitieve app-store-release.
