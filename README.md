# Playlumi

Playlumi is een Nederlandstalige, native Android-app met Lumi, een gouden ster, als hoofdpersonage. Kinderen kiezen een van zes speelwerelden, openen een spelgroep en gaan spelen. Het aanbod past bij **3–4 jaar, 5–6 jaar en 7+**. Tekenen en bouwen bieden ruimte om zelf iets te maken.

De app werkt lokaal en zonder accounts, advertenties, trackers of extra toestemmingen. Spelregels en voortgangsberekeningen staan zoveel mogelijk los van de Android-interface, zodat ze onafhankelijk getest kunnen worden.

## Huidige stand

**0.127 · versionCode 601028** vereenvoudigt de hele bediening van 0.126. De zes speelwerelden en de bestaande spellen blijven de kern. Iedere wereld krijgt twee of drie herkenbare spelgroepen, met korte teksten en grote knoppen. Stadsgroei, dagopdrachten, winkels, prestaties, verzamelingen en zichtbare sterrenadministratie verdwijnen uit de kinderervaring.

De app gebruikt geen verticale scrolschermen. Menu's tonen complete pagina's met vaste navigatie; horizontaal bladeren is mogelijk. Spelafronding biedt een korte bevestiging en de keuze om opnieuw te spelen of een ander spel te kiezen.

De gebruiker heeft voor deze ronde expliciet gevraagd **geen tests uit te voeren**. De 0.127-workflow compileert daarom alleen de productie-APK en voert de noodzakelijke pakketcontroles uit. Unit-tests, Java-controles, lint, emulators en upgradeproeven worden deze ronde niet gestart; beoordeling door de gebruiker staat open. Zie [de wijzigingen en opleverscope van 0.127](docs/RELEASE-0.127.md). Historische releasebeschrijvingen behouden hun toenmalige context.

## Spelen en ontdekken

| Wereld | Voorbeelden van activiteiten |
| --- | --- |
| Rekenraket | Tellen, hoeveelheden vergelijken, rekenen, patronen met getallen en tientallen/eenheden. |
| Letterhuis | Letters, woorden, rijmen en leeftijdspassende spelling. |
| Vormkasteel | Vormen, ruimtelijk denken, patronen en geheugen. |
| Muziekstudio | Melodieën beluisteren, onthouden en naspelen. |
| Speelplein | Interactieve spellen en oefeningen met handelingen en alledaagse situaties. |
| Kleurentuin | Kleuren, tekenen en creatieve activiteiten. |

De catalogus groepeert spellen per wereld, spelgroep en leeftijd, met maximaal vier grote speelkaarten per pagina. De onderdelen zoeken, favorieten, verder gaan, eerder gespeeld, spelideeën en spelers blijven uit de bediening. Opgeslagen spelvoortgang blijft bewaard: een spel hervat via zijn eigen speelkaart. De bestaande spelcodes en opslagkeys blijven stabiel.

Tekeningen worden lokaal bewaard. Potlood, gum en drie lijndiktes werken op nieuwe en bewaarde tekeningen. De laatste 32 acties blijven op het actieve tekenblad terug te draaien en opnieuw uit te voeren, ook na bewaren. Oudermogelijkheden blijven achter de ouderpoort.

Het Bouwatelier biedt bouwtekeningen en vrije ontwerpen, met draaien, ongedaan maken en opnieuw uitvoeren. Dierensudoku oefent logisch redeneren: een klein dierenraster voor 3–4 jaar en een 4×4-raster voor de volgende leeftijdsgroepen. De spelregels uit 0.126 blijven behouden.

De app gebruikt native Android-bediening en behoudt Lumi en de oorspronkelijke ingesloten illustraties. De indeling houdt rekening met schermbreedte en tekstgrootte. De app voegt geen gesproken instructies toe. Muziek, instelbare feedback en Android-toegankelijkheid blijven afzonderlijke functies.

## Uitgangspunten

- **Voortgang is van het kind.** Bestaande profielen, sterren, aankopen, favorieten, tekeningen, bouwontwerpen en begonnen rondes blijven bewaard wanneer hun oude menu verdwijnt.
- **Lokaal en rustig.** Geen account, advertenties, analytics of druk om dagelijks terug te komen. Er is geen verlies van verdiende spullen door gemiste dagen.
- **Eerlijke informatie.** Tellers tonen bewaarde gegevens. Oudere voltooiingsmarkeringen worden niet omgezet in geschatte historische aantallen.
- **Toegankelijk bedienen.** Native knoppen, voldoende raakvlakken, tekst die kan meegroeien, betekenisvolle schermlezerteksten en respect voor minder beweging. Toestelacceptatie blijft nodig.
- **Herkenbare wereld.** Lumi blijft de gouden ster. De goedgekeurde originele illustraties en de oorspronkelijke releasehandtekening blijven behouden.
- **Heldere opleverscope.** Compilatie, pakketcontroles, tests, privé ondertekening en toestelacceptatie zijn afzonderlijke stappen. De releasebeschrijving vermeldt welke stappen zijn uitgevoerd. Publicatie gebeurt alleen na uitdrukkelijke opdracht.

## Bouwen

Gebruik **JDK 17, Gradle 8.9 en Android SDK 35**. De technische minimumversie in de app is Android API 23; dit is geen verklaring dat elk toestel op die versie fysiek getest is.

Voor de huidige, op verzoek ongeteste 0.127-oplevering:

```sh
PLAYLUMI_BUILD_CODE=601028 gradle --no-daemon -PplaylumiPackagingOnly=true assembleRelease
```

De aparte [0.127-APK-workflow](.github/workflows/android-0127-build.yml) bouwt uitsluitend de productie-APK, controleert pakket en versie en neemt de oorspronkelijke illustraties mee. Zij voert geen tests of lint uit. De expliciete projectoptie `playlumiPackagingOnly=true` schakelt ook de impliciete release-lint van Android uit voor deze opdracht. Zonder die optie blijft die controle standaard ingeschakeld. De gewone Android-workflow slaat alleen deze releasebranch over; haar verificatiestappen blijven beschikbaar.

## Beschikbare controles voor volgende ontwikkelrondes

Deze hulpmiddelen blijven in het project aanwezig. Ze worden voor 0.127 niet uitgevoerd. De onafhankelijke Java-controles hebben geen Android-runtime nodig:

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

De bewaarde [Android-verificatieworkflow](.github/workflows/android.yml) bevat de debug- en productiebuild, unit-tests en Android Lint. De [emulatorworkflow](.github/workflows/clean-quiz-qa.yml) bevat telefoon- en Fold-proeven. De versiegebonden verwachtingen horen bij de laatste gecontroleerde release en moeten bij een volgende testronde worden bijgewerkt voor de dan geldende bediening.

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
