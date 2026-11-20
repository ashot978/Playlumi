# Playlumi

Een eerste Android-app voor kinderen van 3 tot 5 jaar. In het telspel telt het kind de sterren en kiest het juiste cijfer. Er is geen tijdslimiet; een verkeerd antwoord mag opnieuw worden geprobeerd. Het aantal goed beantwoorde rondes wordt alleen op het toestel bewaard.

## Bouwen en testen

Open het project in Android Studio met Android SDK 35 en JDK 17. Gebruik Gradle 8.9: `gradle testDebugUnitTest lintDebug assembleDebug`. De GitHub Actions-workflow voert deze controles uit en bewaart bij succes de debug-APK als artifact. De debug-APK is niet bedoeld als Play Store-release.

De app gebruikt geen accounts, advertenties, trackers, netwerkaanroepen of extra toestemmingen. Een echte toesteltest blijft nodig, waaronder verschillende schermformaten, schermrotatie en toegankelijkheid.
