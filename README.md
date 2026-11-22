# Playlumi

Een eerste Android-app voor kinderen van 3 tot 5 jaar. In het telspel telt het kind sterren, bloemen of ballonnen en kiest het juiste cijfer. Er zijn niveaus tot 3, 5 en 10. Er is geen tijdslimiet; een verkeerd antwoord mag opnieuw worden geprobeerd. Het aantal goed beantwoorde rondes en de keuze van wereld en niveau worden alleen op het toestel bewaard.

Er zijn ook twee zoekspelletjes: kleuren en vormen vergelijken en hoofdletters koppelen aan kleine letters (A, B, M, S en O). De kleurkeuzes hebben ook een eigen vorm, zodat kleur niet het enige onderscheid is. Deze eerste versie heeft nog geen ingesproken instructies; begeleiding door een volwassene kan voor jonge kinderen nodig zijn. Niveaus en thema's zijn op dit moment alleen beschikbaar bij tellen.

## Bouwen en testen

Open het project in Android Studio met Android SDK 35 en JDK 17. Gebruik Gradle 8.9: `gradle testDebugUnitTest lintDebug assembleDebug`. De GitHub Actions-workflow voert deze controles uit en maakt een niet-ondertekende release voor privéondertekening. Alleen een APK die met het oorspronkelijke Playlumi-certificaat is ondertekend en gecontroleerd, wordt als installeerbare update gepubliceerd.

De app gebruikt geen accounts, advertenties, trackers, netwerkaanroepen of extra toestemmingen. Een echte toesteltest blijft nodig, waaronder verschillende schermformaten, schermrotatie en toegankelijkheid.

## Installatie en updates vanaf v0.62.0

De gebruiker heeft bewust een **nieuwe vaste PKCS12-releasekeystore** gekozen. De eerste APK die met deze nieuwe sleutel is ondertekend, kan niet rechtstreeks over de oorspronkelijke v0.45-installatie worden geïnstalleerd. Een schone herinstallatie kan lokale kindprofielen, scores en voortgang wissen. Maak waar mogelijk eerst een back-up; verwijder bestaande apps nooit automatisch.

Na de eerste schone installatie worden alle volgende releases met exact dezelfde nieuwe sleutel ondertekend. De openbare certificaatvingerafdruk en veilige GitHub Actions-configuratie staan in [ORIGINAL_SIGNING.md](ORIGINAL_SIGNING.md). De geheime keystore en het wachtwoord staan **niet in de publieke repository**.

De GitHub Actions-run moet volledig slagen en het artifact `playlumi-newkey-signed-release` publiceren. Als de vereiste repository secrets ontbreken, faalt de release-controle bewust. Een debug-APK of een niet-ondertekende release is geen geschikte installatie-update.
