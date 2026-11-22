# Playlumi

Een eerste Android-app voor kinderen van 3 tot 5 jaar. In het telspel telt het kind sterren, bloemen of ballonnen en kiest het juiste cijfer. Er zijn niveaus tot 3, 5 en 10. Er is geen tijdslimiet; een verkeerd antwoord mag opnieuw worden geprobeerd. Het aantal goed beantwoorde rondes en de keuze van wereld en niveau worden alleen op het toestel bewaard.

Er zijn ook twee zoekspelletjes: kleuren en vormen vergelijken en hoofdletters koppelen aan kleine letters (A, B, M, S en O). De kleurkeuzes hebben ook een eigen vorm, zodat kleur niet het enige onderscheid is. Deze eerste versie heeft nog geen ingesproken instructies; begeleiding door een volwassene kan voor jonge kinderen nodig zijn. Niveaus en thema's zijn op dit moment alleen beschikbaar bij tellen.

## Bouwen en testen

Open het project in Android Studio met Android SDK 35 en JDK 17. Gebruik Gradle 8.9: `gradle testDebugUnitTest lintDebug assembleDebug`. De GitHub Actions-workflow voert deze controles uit en bewaart bij succes de debug-APK als artifact. De debug-APK is niet bedoeld als Play Store-release.

De app gebruikt geen accounts, advertenties, trackers, netwerkaanroepen of extra toestemmingen. Een echte toesteltest blijft nodig, waaronder verschillende schermformaten, schermrotatie en toegankelijkheid.

## Release-update installeren (v0.61+)

**Gebruik geen debug-APK als update over de bestaande Playlumi-installatie.** De v0.45-app is ondertekend met het originele Playlumi-certificaat. Een debug-APK heeft een andere handtekening en wordt door Android geweigerd.

De GitHub Actions-workflow publiceert een installatie-update uitsluitend als de oorspronkelijke keystore via privé repository secrets beschikbaar is en het ondertekeningscertificaat exact overeenkomt met v0.45. Het artifact heet dan `playlumi-original-signed-release-update`. Als die controle niet kan worden uitgevoerd, verschijnt **geen installeerbare release-update**. De unsigned release-APK mag nooit direct worden geïnstalleerd.

Meer informatie en de exacte configuratie staan in [ORIGINAL_SIGNING.md](ORIGINAL_SIGNING.md). Verwijder de bestaande app niet: lokaal opgeslagen profielen en spelvoortgang kunnen verloren gaan. De emulatorcontrole is geen vervanging voor een test op het echte toestel.
