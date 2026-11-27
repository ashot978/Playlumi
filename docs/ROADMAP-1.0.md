# Playlumi — ontwikkeling en roadmap

Laatste geverifieerde testbouw: **0.110 / 601011**. In ontwikkeling: **0.111 / 601012**. Daarna volgen 0.112, 0.113 enzovoort met steeds hogere Android-versiecodes. Versie 1.0 blijft een afzonderlijk besluit na acceptatie; een geslaagde testbouw is geen productiepublicatie.

## Vaste uitgangspunten

- Lumi is de gouden ster en het hoofdpersonage. De zes werelden bieden leeftijdspassend spel voor 3–4, 5–6 en 7+.
- Spelregels blijven zoveel mogelijk onafhankelijk van de interface. Bestaande routes, kledingcodes, profielgegevens en opslagkeys behouden hun betekenis.
- De stad, beloningen, herhalingen en overzichten volgen echte opgeslagen gebeurtenissen. Een scherm weergeven of opnieuw openen verandert geen voortgang.
- De app blijft lokaal bruikbaar zonder accounts, advertenties, trackers of extra toestemmingen. Dagopdrachten zijn optioneel; verdiende spullen vervallen niet door een overgeslagen dag.
- De oorspronkelijke illustraties en de originele releasehandtekening blijven behouden. Native bediening en aangepaste indelingen vullen het tekenwerk aan.
- De app voegt geen gesproken instructies toe. Schermlezers, muziek, feedbackgeluiden en minder beweging blijven afzonderlijk te bedienen.
- Buildcontrole, emulatorproeven, privé ondertekenen, fysieke toestelacceptatie en publicatie zijn afzonderlijke stappen.

## 0.108 — afgeronde testbouw

Bronversie: [06bf09abae45cea79143bee00924cecabb474bea](https://github.com/ashot978/Playlumi/commit/06bf09abae45cea79143bee00924cecabb474bea). De volledige functiebeschrijving staat in [RELEASE-0.108.md](RELEASE-0.108.md).

### Uitgevoerd

- [x] Een groeiende stadskaart met zeven afzonderlijke gebouwen, parken, een podium, brug en monument. Verdiende prestaties bepalen wat verschijnt; ieder perceel heeft een native knop met de echte voorwaarde, voortgang en cadeau.
- [x] Getekende cape, sterrenpak, regenboogtrui en astronautenpak rond Lumi's stervorm, plus strik en sjaal. Bestaande eigendom en kledingcodes blijven gelijk.
- [x] Drie nieuwe vaste spelroutes: Evenveel, Eerst en daarna, Tientallen en eenheden. Het aanbod telt 25 / 43 / 68 routes per leeftijdsgroep, met afzonderlijke inhoudscontroles.
- [x] Dagopdrachten openen passende quizzen en geven voorrang aan een bruikbare begonnen ronde. Kalenderafhankelijke informatie wordt na terugkeer en bij een nieuwe dag vernieuwd.
- [x] Herhalingsmijlpalen, werkelijk bewaarde sterren en een verwijzing naar nieuw verdiende stadsplekken op de eindpanelen. Geen extra mijlpaalvaluta of verzonnen historische rondes.
- [x] Gezamenlijke opslag van score, beloning, voortgang en rondegegevens voor meer speltypes, waaronder interactieve borden, minispellen, muziek, bellen en sterren vangen.
- [x] Native bediening over de oorspronkelijke telefoonillustratie, een leesbaardere indeling in smal landschap, tekstafhankelijke stadskolommen en antwoorden die verticaal kunnen groeien.

### Geverifieerd en opgeleverd

- [x] [Android-run 45992312167](https://github.com/ashot978/Playlumi/actions/runs/45992312167): debug en release gebouwd; pakket, versie en oorspronkelijke illustraties gecontroleerd; **205 unit-tests in 51 suites geslaagd**.
- [x] Android Lint op dezelfde bron: **0 fouten en 167 waarschuwingen**. De resterende meldingen zijn geen afgerond kwaliteitswerk en worden niet als een foutloos rapport voorgesteld.
- [x] [UI-run 45992312107](https://github.com/ashot978/Playlumi/actions/runs/45992312107): telefoon- en Fold-emulatorproeven geslaagd, inclusief quizherstel, dagbeloningen, stad, kleding, nieuwe oefeningen en herstel van interactieve spellen.
- [x] Updateproef van vastgepinde 0.107 naar 0.108 met dezelfde tijdelijke debughandtekening binnen één runner; opgeslagen voortgang en een echte begonnen quiz behouden.
- [x] Echte screenshots beoordeeld van home, vergrendelde en vrijgespeelde stadsplekken, grote tekst, outfits en de volledig zichtbare onderkant van de stad.
- [x] De productie-APK privé ondertekend met het oorspronkelijke releasecertificaat; pakket, versie, certificaat, ZIP-uitlijning en ongewijzigde inhoud gecontroleerd.

SHA-256 van de opgeleverde 0.108-APK:

```text
b6f2fcc48c2ce2c7be2d888788007179eedf3aef629966f086ab51fb44c23c3b
```

SHA-256 van het oorspronkelijke releasecertificaat:

```text
d2b74f018bf6111382f901c2acdc03e787811ce207d6c4435fcca9559ae9b0ef
```

[PR 92](https://github.com/ashot978/Playlumi/pull/92) is een concept en is niet gemerged. De APK is een testoplevering; er is geen openbare release of Play Store-publicatie. Fysieke Samsung-bediening en een update met de oorspronkelijke releasehandtekening op dat toestel zijn nog niet uitgevoerd.

## 0.109 — afgeronde testbouw

Bronversie: [c31400887005acd24c1d418eb6084f8156dbe9b5](https://github.com/ashot978/Playlumi/commit/c31400887005acd24c1d418eb6084f8156dbe9b5). Baseline is de hierboven geverifieerde 0.108. De volledige beschrijving staat in [RELEASE-0.109.md](RELEASE-0.109.md).

### Uitgevoerd

- [x] Drie optionele speelideeën uit de werkelijke geschiedenis van het gekozen profiel en de gekozen leeftijd: verder spelen, opnieuw spelen en ontdekken. Kiezen of verversen geeft geen extra beloning.
- [x] Een ouderoverzicht met feitelijk getelde rondes, oefeningen met een afgeronde ronde en verder te spelen rondes per wereld. Oudere voltooiing zonder rondetelling blijft herkenbaar zonder schatting.
- [x] Antwoordfeedback met een vinkje, native geselecteerde status en betekenisvolle schermlezertekst; oorspronkelijke antwoord- en herstelgegevens blijven behouden.
- [x] Tekeningen ook bij het huisje bewaren met AtomicFile en controle van de geschreven PNG. Undo blijft tijdens hetzelfde tekenblad bruikbaar na bewaren en achtergrondgebruik. Automatisch bewaren geeft geen extra beloning; de eerste expliciete bonus en voortgang worden samen opgeslagen.
- [x] Hergebruik van avatarpaden, gradients en vaste navigatiegeometrie; een begrensde cache voor identieke gebouwgradients. Geen onbewezen FPS-winst geclaimd.
- [x] Gouden gezichtsuitsparingen voor de volledige outfits; strik en sjaal volledig zichtbaar en de glimlach binnen de stervorm.
- [x] De ouderpoort opnieuw sluiten bij achtergrondgebruik en Activity-herstel, met behoud van navigatie binnen de geopende ouderzone.

### Geverifieerd en opgeleverd

- [x] [Android-run 46014835510](https://github.com/ashot978/Playlumi/actions/runs/46014835510): debug en release gebouwd, oorspronkelijke illustraties gecontroleerd, **230 unit-tests in 53 suites geslaagd**.
- [x] Android Lint op dezelfde bron: **0 fouten en 168 waarschuwingen**, zonder algemene onderdrukking.
- [x] Onafhankelijke Java-controle: 3.422 speelgeschiedenissen/variaties, 10.266 leeftijdspassende ideeën en 20.538 feitelijke wereldsamenvattingen, naast de bestaande inhouds- en herstelcontroles.
- [x] [UI-run 46014835542](https://github.com/ashot978/Playlumi/actions/runs/46014835542): **43 emulatorchecks geslaagd** — 19 telefoon- en 18 Fold-checks voor stad, update en lifecycle, plus drie quizchecks per formaat.
- [x] Update van exacte 0.108 naar 0.109 met dezelfde tijdelijke debughandtekening: 28 voorkeuren/voortgangswaarden en 17 velden van een echte begonnen quiz blijven gelijk.
- [x] Speelideeën en ouderoverzicht gecontroleerd met werkelijk gespeelde rondes; exact vraagherstel en pauze gecontroleerd na beëindiging van het achtergrondproces en herstel in een nieuwe PID.
- [x] Echte tekenbeweging blijft bewaard na huisje en heropenen. Undo na Bewaar en na achtergrondgebruik herstelt dezelfde PNG-bytes; bonus blijft eenmaal negen sterren.
- [x] Screenshots beoordeeld van speelideeën, grote tekst, antwoordmarkering, pauze, ouderpoort, tekenstudio, stad en de vijf outfitvarianten strik, sjaal, sterrenpak, regenboog en astronaut.
- [x] APK privé ondertekend met het oorspronkelijke releasecertificaat; bronrelatie, pakket, versie, certificaat, inhoud en ZIP-uitlijning gecontroleerd.

SHA-256 van de opgeleverde 0.109-APK:

```text
85e6ebcbea21f4a00d5a3ab7efab3ad365f354d0737ceccb3f97aaa8dd83fcc9
```

[PR 93](https://github.com/ashot978/Playlumi/pull/93) is een concept. De geteste bron is niet gemerged of openbaar gepubliceerd. De fletse Tekenstudio-feedback, titelafbreking op telefoon en enkelvoudstekst in het ouderoverzicht zijn bekende afwerkingspunten voor de volgende bouw. De emulatorproef vervangt geen oorspronkelijk ondertekende update op een fysiek Samsung-toestel.

## 0.110 — afgeronde testbouw

Geverifieerde bron: [50230b814b6ac9af35fbb1d6464aa831e37e5123](https://github.com/ashot978/Playlumi/commit/50230b814b6ac9af35fbb1d6464aa831e37e5123). De beschrijving staat in [RELEASE-0.110.md](RELEASE-0.110.md).

- [x] Potlood, gum en drie lijndiktes, met native geselecteerde status en bewaarde keuzes per profiel.
- [x] Undo/Redo voor volledige gebaren en leegmaken; maximaal 32 actieve acties, met behoud van oudere zichtbare pixels. Bewaren en kort achtergrondgebruik behouden de actieve geschiedenis.
- [x] Gum op geladen PNG's, ongewijzigde profielbestanden, veilige opslag en eenmalige tekenbeloning.
- [x] Tel de vormen, Dagen van de week en Lees en kies. Catalogus: **26 / 45 / 71 routes**, met bestaande routecodes behouden.
- [x] Leesbare Tekenstudio-feedback, passende kop, enkelvoud in het ouderoverzicht, uitgelijnde stadstegels en een tekenvlak dat de gereedschappen niet overschildert.
- [x] [Android-run 46038701981](https://github.com/ashot978/Playlumi/actions/runs/46038701981): debug/release, oorspronkelijke illustraties en vijf onafhankelijke Java-controles geslaagd; **264 unit-tests in 56 suites**, **0 lintfouten en 176 waarschuwingen**.
- [x] [UI-run 46038701925](https://github.com/ashot978/Playlumi/actions/runs/46038701925): **56 native acceptatiegroepen** — 26 telefoon, 24 Fold en drie bestaande quizgroepen per formaat.
- [x] Update van exacte 0.109 met dezelfde tijdelijke debughandtekening: 28 voorkeuren, 17 vraagvelden en een werkelijk getekende oude PNG behouden.
- [x] Daadwerkelijke geladen-PNG-gum, Undo/Redo, leegmaken en 32 + 32 verdere gebaren gecontroleerd op Android-pixels. Afzonderlijk voorbereide profielen behouden hun eigen bestanden, keuzes en beloningen.
- [x] Zichtbare gereedschappen ook met grote tekst bevestigd door pixelmetingen en beoordeling van echte screenshots; de nieuwe oefeningbeelden zijn inhoudelijk beoordeeld.

De profielproef van 0.110 gebruikte een expliciete opslagfixture om het actieve profiel te kiezen. Een echte spelerwissel via de ouderzone volgt in 0.111. Fysieke Samsung-bediening blijft afzonderlijke open acceptatie.

## 0.111 — lopend werk

Baseline is de hierboven geverifieerde 0.110. Het korte werkpakket staat in [RELEASE-0.111.md](RELEASE-0.111.md).

- Drie vaste anonieme spelers via de ouderpoort, met eigen leeftijd, voortgang, voorkeuren en tekening. Speler 1 behoudt de oorspronkelijke opslag.
- De bestaande profielproef vervangen door echte spelerknoppen en controleren dat een begonnen quiz en tekening na terugwisselen exact behouden blijven.
- ‘Wat hoort er niet bij?’ voor alle leeftijdsgroepen, met beoogd catalogusaanbod **27 / 46 / 72** en uitbreiding van de bestaande inhoudscontrole.
- De donkerblauwe kleurnaam passend tonen op de brede tekenindeling.
- Definitieve bouw, volledige bestaande gates, update van exact 0.110, privé ondertekening en oplevering van de gecontroleerde APK met korte changelog.

## Eerdere mijlpalen

De individuele veranderingen blijven terug te vinden in de Git-geschiedenis. Deze samenvatting vervangt verouderde open vakjes voor inmiddels opgevolgde testbouwen.

| Bouw | Belangrijkste ontwikkeling |
| --- | --- |
| 0.99.1–0.99.6 | Herstel per profiel/leeftijd/spel, zoeken en begonnen spellen, leeftijdspassende verdieping, instelbaar quiztempo, uitgebreid rekenen en consistente eindpanelen. |
| 0.100 | Eenvoudigere quizbediening, geen hint- of spraakrij, werkelijk versienummer in instellingen en voortzetting van de gevraagde 0.xxx-nummering. |
| 0.101–0.106 | Quizzen vanaf de eerste vraag bewaren, exacte keuzes en afgewezen antwoorden herstellen, consistente beloningsopslag en afzonderlijke telefoon-/Fold-proeven. |
| 0.107 | Lumi als gouden ster, optionele dagopdrachten, echte herhalingsmijlpalen en zeven blijvende stadsbeloningen met bestaande outfits en vriendjes. |

Bekende verificatiereferenties: 0.106 [build 45909894600](https://github.com/ashot978/Playlumi/actions/runs/45909894600) en [telefoon/Fold 45909894635](https://github.com/ashot978/Playlumi/actions/runs/45909894635); 0.107 [build 45986125945](https://github.com/ashot978/Playlumi/actions/runs/45986125945) en [telefoon/Fold 45986125973](https://github.com/ashot978/Playlumi/actions/runs/45986125973).

## Open acceptatie en verdere verbetering

- [ ] Op een fysiek Samsung Fold-toestel de oorspronkelijk ondertekende APK bijwerken en bestaande gegevens controleren. Ook oudere geïnstalleerde versies moeten hun voortgang behouden.
- [ ] Op echte compacte telefoons, Fold en tablet: navigatie, vouwen/uitvouwen, rotatie, terug, profielscheiding, ouderpoort en systeeminsets controleren.
- [ ] Schermlezer, grote tekst, focusvolgorde, raakvlakken, gedempt geluid en minder beweging daadwerkelijk bedienen.
- [ ] Lange sessies en achtergrond/voorgrondwissels controleren op exact herstel, herhaalde beloningen en opgeslagen tekeningen.
- [ ] Iedere aangeboden oefening voor de betreffende leeftijd handmatig beoordelen op begrijpelijkheid en een verdedigbaar antwoord. Gegenereerde tests alleen zijn daarvoor onvoldoende.
- [ ] Overgebleven lintmeldingen inhoudelijk beoordelen; herstel op basis van werkelijk risico en effect, zonder algemene onderdrukking.
- [ ] Teksten, artworkrechten en de privacy-informatie laten aansluiten op de uiteindelijke app en de gekozen distributie.

Nieuwe leerstof en extra geïllustreerde details volgen op basis van feedback op de geteste bouwen. Wekelijkse doelen zijn geen onderdeel van de geverifieerde 0.110 of het huidige 0.111-werkpakket. Advertenties, betaalde herstelopties, drukke terugkeermeldingen en verlies van verdiende badges passen niet binnen de uitgangspunten.

## 1.0 — na afzonderlijke acceptatie

- [ ] Geen bekende blokkerende crashes, inhoudsfouten, verloren voortgang of onverenigbare handtekening.
- [ ] Fysieke toestelacceptatie en inhoudelijke beoordeling afgerond, met bevestiging van de eigenaar/testers.
- [ ] Wanneer publicatie wordt gevraagd: goedgekeurde winkeltekst, screenshots, privacy-URL, leeftijds-/doelgroepverklaringen, releasebeschrijving en ondertekende productie-AAB voorbereiden.
- [ ] De eigenaar geeft uitdrukkelijk opdracht tot de gewenste indiening of publicatie. Geen automatische Play Store-publicatie.
