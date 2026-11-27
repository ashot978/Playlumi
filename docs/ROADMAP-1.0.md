# Playlumi — ontwikkeling en roadmap

Laatste geverifieerde testbouw: **0.108 / 601009**. In ontwikkeling: **0.109 / 601010**. Daarna volgen 0.110, 0.111 enzovoort met steeds hogere Android-versiecodes. Versie 1.0 blijft een afzonderlijk besluit na acceptatie; een geslaagde testbouw is geen productiepublicatie.

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

## 0.109 — lopend werk

Baseline is de hierboven geverifieerde 0.108. De beschrijving van de nieuwe bouw staat in [RELEASE-0.109.md](RELEASE-0.109.md). De onderstaande bronwijzigingen moeten nog als één definitieve Android-bouw worden geverifieerd; er wordt hier geen geslaagde 0.109-CI-run geclaimd.

### Werkpakket

- Drie optionele speelideeën uit de werkelijke geschiedenis van het gekozen profiel en de gekozen leeftijd: verder spelen, opnieuw spelen en ontdekken. Geen extra beloning bij kiezen of verversen.
- Een ouderoverzicht met feitelijke gespeelde rondes, oefeningen met een afgeronde ronde en verder te spelen rondes per wereld. Oudere voltooiing zonder rondetelling blijft herkenbaar zonder schatting.
- Duidelijkere antwoordfeedback met een vinkje, native geselecteerde status en betekenisvolle schermlezertekst; bestaande antwoord- en herstelgegevens behouden.
- Betrouwbaarder bewaren van tekeningen, ook bij het huisje in de kop: eerst geslaagde opslag, daarna de bestaande eenmalige beloning. Actief werk blijft beschikbaar als opslaan mislukt.
- Hergebruik van avatarpaden, gradients en vaste navigatiegeometrie; een begrensde cache voor identieke gebouwgradients. Dit verwijdert terugkerende allocaties, zonder een gemeten FPS-winst te claimen.
- Een gouden gezichtsuitsparing voor de volledige outfits. Strik en sjaal blijven volledig herkenbaar; Lumi's glimlach ligt binnen de stervorm.
- De ouderpoort opnieuw sluiten bij achtergrondgebruik en Activity-herstel, met behoud van de bestaande lokale bediening.

### Nog vóór oplevering

- [ ] Alle bronwijzigingen en onafhankelijke controles op één vastgelegde commit samenbrengen en de verplichte regressiepoort opnieuw uitvoeren.
- [ ] Debug en release bouwen; volledige unit-tests, Android Lint, pakket-/versiecontrole en oorspronkelijke artworkcontrole laten slagen.
- [ ] De native speelideeën, terugnavigatie, ouderoverzichten, antwoordmarkering en tekenen bedienen op telefoon- en Fold-emulators.
- [ ] Nieuwe screenshots beoordelen, inclusief grote tekst en de gewijzigde kledingstukken: strik, sjaal, sterrenpak, regenboogtrui en astronaut.
- [ ] Een update van exact 0.108 naar 0.109 testen met controle van profielen, sterren, eigendom, favorieten en begonnen rondes.
- [ ] De gecontroleerde productie-APK privé met de oorspronkelijke sleutel ondertekenen en pakket, versie, certificaat, inhoud en uitlijning verifiëren.
- [ ] Definitieve commit, workflowreferenties, testresultaten, APK-hash en resterende grenzen bij de oplevering vastleggen.

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

Nieuwe leerstof en extra geïllustreerde details volgen op basis van feedback op de geteste bouwen. Wekelijkse doelen zijn geen onderdeel van 0.108 of het huidige 0.109-werkpakket. Advertenties, betaalde herstelopties, drukke terugkeermeldingen en verlies van verdiende badges passen niet binnen de uitgangspunten.

## 1.0 — na afzonderlijke acceptatie

- [ ] Geen bekende blokkerende crashes, inhoudsfouten, verloren voortgang of onverenigbare handtekening.
- [ ] Fysieke toestelacceptatie en inhoudelijke beoordeling afgerond, met bevestiging van de eigenaar/testers.
- [ ] Wanneer publicatie wordt gevraagd: goedgekeurde winkeltekst, screenshots, privacy-URL, leeftijds-/doelgroepverklaringen, releasebeschrijving en ondertekende productie-AAB voorbereiden.
- [ ] De eigenaar geeft uitdrukkelijk opdracht tot de gewenste indiening of publicatie. Geen automatische Play Store-publicatie.
