# Playlumi

Playlumi is een Nederlandstalige, native Android-speelwereld met Lumi, een gouden ster, als hoofdpersonage. Kinderen oefenen in zes werelden met aanbod voor **3–4 jaar, 5–6 jaar en 7+**. Ze kunnen een begonnen ronde verder spelen, oefeningen herhalen en met hun echte voortgang blijvende plekken in Lumi's stad ontdekken.

De app werkt lokaal en zonder accounts, advertenties, trackers of extra toestemmingen. Spelregels en voortgangsberekeningen staan zoveel mogelijk los van de Android-interface, zodat ze onafhankelijk getest kunnen worden.

## Huidige stand

**0.126 · versionCode 601027** is de volgende ontwikkelversie op de laatste downloadbare APK 0.123. Deze versie voegt **Dierensudoku** toe en voltooit de vaste horizontale schermen voor de expeditie, kleding, ouderzone en het Bouwatelier. De zelfstandige spelregels, Android-build, lint en native telefoon-/Fold-controles hebben elk hun eigen gate. De actuele bewijzen en eventuele beperkingen horen in de releasebeschrijving en pull request.

De app gebruikt geen verticale scrolschermen. Menu's tonen complete, benoemde pagina's met vaste navigatie; horizontaal bladeren is mogelijk. Tijdens het aankleden blijft Lumi in beeld. Tijdens het bouwen blijft het bouwraster bereikbaar naast of boven de pagina met onderdelen, voorbeeld of acties.

Zie [de wijzigingen en verificatie van 0.126](docs/RELEASE-0.126.md). Historische releasebeschrijvingen behouden hun toenmalige context en zijn geen beschrijving van het huidige menu.

## Spelen en ontdekken

| Wereld | Voorbeelden van activiteiten |
| --- | --- |
| Rekenraket | Tellen, hoeveelheden vergelijken, rekenen, patronen met getallen en tientallen/eenheden. |
| Letterhuis | Letters, woorden, rijmen en leeftijdspassende spelling. |
| Vormkasteel | Vormen, ruimtelijk denken, patronen en geheugen. |
| Muziekstudio | Melodieën beluisteren, onthouden en naspelen. |
| Speelplein | Interactieve spellen en oefeningen met handelingen en alledaagse situaties. |
| Kleurentuin | Kleuren, tekenen en creatieve activiteiten. |

De catalogus groepeert spellen per wereld en leeftijd, met maximaal vier grote speelkaarten per pagina. De onderdelen zoeken, favorieten, verder gaan, eerder gespeeld, spelideeën en spelers zijn uit de bediening gehaald. Opgeslagen spelvoortgang blijft bewaard: een spel hervat via zijn eigen speelkaart. Bestaande spelcodes blijven stabiel bij uitbreidingen.

Lumi's stad toont zeven verschillende plekken als gebouw, park, podium, brug of monument zodra de bijbehorende prestatie daadwerkelijk verdiend is. Kleding en vriendjes gebruiken de bestaande eigendomslijst. Drie optionele dagopdrachten wijzen naar passende spellen; behaalde cadeaus verdwijnen niet wanneer een dag wordt overgeslagen. Opnieuw spelen telt mee voor zichtbare herhalingsmijlpalen zonder oude, niet geregistreerde rondes te verzinnen.

Tekeningen worden lokaal per profiel bewaard, ook bij het huisje of achtergrondgebruik. Potlood, gum en drie lijndiktes werken op nieuwe en bewaarde tekeningen. De laatste 32 acties blijven op het actieve tekenblad terug te draaien en opnieuw uit te voeren, ook na bewaren. Automatisch bewaren geeft geen extra sterren. Het ouderoverzicht toont werkelijk getelde rondes en begonnen oefeningen; de ouderpoort sluit opnieuw na achtergrondgebruik of opnieuw maken van het scherm.

Lumi’s expeditie bevat zes hoofdstukken met dertig opdrachten en een eigen avonturenkaart. Het Bouwatelier biedt twaalf bouwtekeningen en drie vrije ontwerpen, met draaien, ongedaan maken, opnieuw uitvoeren en een controle of huizen met het station verbonden zijn. Dierensudoku oefent logisch redeneren: een klein dierenraster voor 3–4 jaar en een 4×4-raster voor de volgende leeftijdsgroepen. Iedere puzzel is zonder gokken op te lossen.

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
