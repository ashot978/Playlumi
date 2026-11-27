# Playlumi

Playlumi is een Nederlandstalige, native Android-speelwereld met Lumi, een gouden ster, als hoofdpersonage. Kinderen oefenen in zes werelden met aanbod voor **3–4 jaar, 5–6 jaar en 7+**. Ze kunnen een begonnen ronde verder spelen, oefeningen herhalen en met hun echte voortgang blijvende plekken in Lumi's stad ontdekken.

De app werkt lokaal en zonder accounts, advertenties, trackers of extra toestemmingen. Spelregels en voortgangsberekeningen staan zoveel mogelijk los van de Android-interface, zodat ze onafhankelijk getest kunnen worden.

## Huidige stand

| Bouw | Status |
| --- | --- |
| **0.109 · 601010** | Laatst geverifieerde test-APK, gebouwd vanaf [c314008](https://github.com/ashot978/Playlumi/commit/c31400887005acd24c1d418eb6084f8156dbe9b5), privé ondertekend met het oorspronkelijke releasecertificaat. |
| **0.110 · 601011** | In ontwikkeling: meer tekengereedschap, drie aanvullende leerroutes en gerichte interface-afwerking. De definitieve bron krijgt eigen build-, test-, update- en handtekeningcontroles. |

Voor 0.109 zijn de [Android-build en volledige tests](https://github.com/ashot978/Playlumi/actions/runs/46014835510) en de [telefoon- en Fold-emulatorproeven](https://github.com/ashot978/Playlumi/actions/runs/46014835542) geslaagd: **230 unit-tests in 53 suites**, **0 lintfouten en 168 waarschuwingen**, en **43 emulatorchecks**. [PR 93](https://github.com/ashot978/Playlumi/pull/93) is een concept. Een emulatorproef is geen bediening op een fysiek Samsung Fold-toestel; die acceptatie staat nog open. Dit project bevindt zich in testontwikkeling. Een test-APK geeft geen opdracht tot een merge of Play Store-publicatie.

Zie [de geverifieerde wijzigingen in 0.109](docs/RELEASE-0.109.md), [het werk aan 0.110](docs/RELEASE-0.110.md) en [de roadmap met bewijs en open controles](docs/ROADMAP-1.0.md).

## Spelen en ontdekken

| Wereld | Voorbeelden van activiteiten |
| --- | --- |
| Rekenraket | Tellen, hoeveelheden vergelijken, rekenen, patronen met getallen en tientallen/eenheden. |
| Letterhuis | Letters, woorden, rijmen en leeftijdspassende spelling. |
| Vormkasteel | Vormen, ruimtelijk denken, patronen en geheugen. |
| Muziekstudio | Melodieën beluisteren, onthouden en naspelen. |
| Speelplein | Interactieve spellen en oefeningen met handelingen en alledaagse situaties. |
| Kleurentuin | Kleuren, tekenen en creatieve activiteiten. |

De geverifieerde catalogus van 0.109 bevat **25, 43 en 68 spelroutes** voor de drie leeftijdsgroepen. Zoeken, favorieten en een lijst van begonnen spellen helpen bij het kiezen. Speelideeën geeft drie concrete keuzes uit de werkelijke speelgeschiedenis, met voorrang voor een begonnen ronde. Bestaande spelcodes en opgeslagen rondes blijven bij uitbreiding behouden.

Lumi's stad toont zeven verschillende plekken als gebouw, park, podium, brug of monument zodra de bijbehorende prestatie daadwerkelijk verdiend is. Kleding en vriendjes gebruiken de bestaande eigendomslijst. Drie optionele dagopdrachten wijzen naar passende spellen; behaalde cadeaus verdwijnen niet wanneer een dag wordt overgeslagen. Opnieuw spelen telt mee voor zichtbare herhalingsmijlpalen zonder oude, niet geregistreerde rondes te verzinnen.

Tekeningen worden lokaal per profiel bewaard, ook bij het huisje of achtergrondgebruik. De actieve tekenlijnen blijven na bewaren terug te draaien. Automatisch bewaren geeft geen extra sterren. Het ouderoverzicht toont werkelijk getelde rondes en begonnen oefeningen; de ouderpoort sluit opnieuw na achtergrondgebruik of opnieuw maken van het scherm.

De oorspronkelijke geïllustreerde startpagina blijft behouden. Native Android-bediening maakt gebouwen en acties afzonderlijk bereikbaar. Brede schermen hebben een aangepaste indeling; de stadskaart houdt rekening met de beschikbare breedte en tekstgrootte. De app voegt geen gesproken instructies toe. Muziek, instelbare feedback en Android-toegankelijkheid blijven afzonderlijke functies.

## Uitgangspunten

- **Voortgang is van het kind.** Bestaande profielen, sterren, aankopen, favorieten en begonnen rondes blijven behouden. Beloningen horen bij gespeelde gebeurtenissen; een scherm tekenen of opnieuw openen levert niets op.
- **Lokaal en rustig.** Geen account, advertenties, analytics of druk om dagelijks terug te komen. Er is geen verlies van verdiende spullen door gemiste dagen.
- **Eerlijke informatie.** Tellers tonen bewaarde gegevens. Oudere voltooiingsmarkeringen worden niet omgezet in geschatte historische aantallen.
- **Toegankelijk bedienen.** Native knoppen, voldoende raakvlakken, tekst die kan meegroeien, betekenisvolle schermlezerteksten en respect voor minder beweging. Toestelacceptatie blijft nodig.
- **Herkenbare wereld.** Lumi blijft de gouden ster. De goedgekeurde originele illustraties en de oorspronkelijke releasehandtekening blijven behouden.
- **Controle vóór oplevering.** Broncodecontroles, Android-builds, emulatorproeven, privé ondertekenen en fysieke toestelacceptatie zijn afzonderlijke stappen. Publicatie gebeurt alleen na uitdrukkelijke opdracht.

## Bouwen en testen

Gebruik **JDK 17, Gradle 8.9 en Android SDK 35**. De technische minimumversie in de app is Android API 23; dit is geen verklaring dat elk toestel op die versie fysiek getest is.

De onafhankelijke Java-controles hebben geen Android-runtime nodig:

```sh
mkdir -p build/rc-check
javac -encoding UTF-8 -d build/rc-check -sourcepath app/src/main/java tools/qa/ReleaseCandidateCheck.java tools/qa/ThinkingPracticeCheck.java tools/qa/PlayIdeasCheck.java tools/qa/FoundationsCheck.java tools/qa/DrawingHistoryCheck.java
java -cp build/rc-check nl.playlumi.app.ReleaseCandidateCheck
java -cp build/rc-check nl.playlumi.app.ThinkingPracticeCheck
java -cp build/rc-check nl.playlumi.app.PlayIdeasCheck
java -cp build/rc-check nl.playlumi.app.FoundationsCheck
java -cp build/rc-check nl.playlumi.app.DrawingHistoryCheck
```

Met de Android SDK beschikbaar:

```sh
gradle --no-daemon testDebugUnitTest lintDebug assembleDebug assembleRelease
```

De [Android-workflow](.github/workflows/android.yml) bouwt beide varianten, controleert pakket en versie, verifieert de oorspronkelijke illustratiebestanden en voert de volledige unit-tests en Android Lint uit vóór het aanbieden van de productie-APK. De [emulatorworkflow](.github/workflows/clean-quiz-qa.yml) bewaart logs en screenshots voor de telefoon- en Fold-proeven. De Java-controles, tests en screenshots vullen elkaar aan; gegenereerde voorbeelden vervangen geen inhoudelijke beoordeling van alle oefeningen.

Een release wordt zonder sleutel in GitHub gebouwd en daarna privé ondertekend. De originele privésleutel en wachtwoorden horen niet in broncode, logs of artifacts. Pakket, versienummer, certificaat, APK-inhoud en ZIP-uitlijning worden vóór oplevering gecontroleerd. Zie [RELEASING.md](RELEASING.md) en [SIGNING_SETUP.md](SIGNING_SETUP.md).

## Wegwijs in de broncode

| Locatie | Inhoud |
| --- | --- |
| `app/src/main/java/nl/playlumi/app/` | Native schermen, tekenwerk, spelregels, catalogus, lokale opslag en voortgang. |
| `app/src/test/java/nl/playlumi/app/` | Unit-tests voor spelregels, herstel, beloningen en geometrie. |
| `tools/qa/` | Onafhankelijke Java-controles met vaste seeds en grensgevallen. |
| `ci/` | Emulatorbediening, updateproeven en bewijsopname. |
| `docs/` | Releasebeschrijvingen en de actuele roadmap. |

Breid spelregels bij voorkeur uit in een onafhankelijke klasse met controleerbare voorbeelden. Houd routecodes, kledingcodes en opslagkeys stabiel. Visuele wijzigingen aan home of de ingesloten illustraties mogen de goedgekeurde oorspronkelijke wereld niet vervangen.
