# Playlumi

Een eerste Android-app voor kinderen van 3 tot 5 jaar. In het telspel telt het kind sterren, bloemen of ballonnen en kiest het juiste cijfer. Er zijn niveaus tot 3, 5 en 10. Er is geen tijdslimiet; een verkeerd antwoord mag opnieuw worden geprobeerd. Het aantal goed beantwoorde rondes en de keuze van wereld en niveau worden alleen op het toestel bewaard.

Er zijn ook twee zoekspelletjes: kleuren en vormen vergelijken en hoofdletters koppelen aan kleine letters (A, B, M, S en O). De kleurkeuzes hebben ook een eigen vorm, zodat kleur niet het enige onderscheid is. Deze eerste versie heeft nog geen ingesproken instructies; begeleiding door een volwassene kan voor jonge kinderen nodig zijn. Niveaus en thema's zijn op dit moment alleen beschikbaar bij tellen.

## Bouwen en testen

Open het project in Android Studio met Android SDK 35 en JDK 17. Gebruik Gradle 8.9: `gradle testDebugUnitTest lintDebug assembleDebug`. De GitHub Actions-workflow voert deze controles uit en bewaart bij succes de debug-APK als artifact. De debug-APK is niet bedoeld als Play Store-release.

De app gebruikt geen accounts, advertenties, trackers, netwerkaanroepen of extra toestemmingen. Een echte toesteltest blijft nodig, waaronder verschillende schermformaten, schermrotatie en toegankelijkheid.

## Test-APK downloaden (v0.55+)

Open de nieuwste geslaagde run van **Android tests and launch smoke** onder GitHub Actions. Download onder **Artifacts** het ZIP-bestand `playlumi-debug-apk-installable`, pak het uit en installeer de `app-debug.apk` op een testtoestel. Je moet voor het downloaden van GitHub Actions-artifacts meestal aangemeld zijn bij GitHub.

**Let op:** de debug-APK is met een debugcertificaat ondertekend. Hij kan niet als update over een eerder met het oorspronkelijke releasecertificaat geïnstalleerde Playlumi-versie worden gezet. Verwijder de bestaande release niet zomaar: daardoor kun je lokaal opgeslagen kindprofielen en spelvoortgang verliezen. Gebruik bij voorkeur een apart testtoestel of Android-emulator. De workflow bewaart daarnaast een `playlumi-unsigned-release-apk`; deze is pas na privé ondertekenen met de oorspronkelijke sleutel geschikt voor release-updates.

De automatische smoke-test bevestigt alleen dat de app in een emulator opstart. Alle spelletjes en installatie op een echte Samsung moeten nog handmatig worden getest.
