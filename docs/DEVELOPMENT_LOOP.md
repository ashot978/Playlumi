# Playlumi — herhaalbaar ontwikkelproces

## Status
Dit project bevat momenteel **geen oorspronkelijke Android-broncode en geen Gradle-app**. De bestaande 0.25.4 recovery-APK mag niet als brontekst worden behandeld. 0.25.3 crashte direct na het wijzigen van gecompileerde DEX. Daarom zijn nieuwe APK-releases **geblokkeerd** totdat de echte opstartcontrole is ingericht.

## Elke iteratie — exact deze volgorde
1. **Controleer basis en fouten.** Lees de laatste feedback, APK-status en open PR's. Bij crash/gegevensverlies: eerst repareren; geen nieuwe glitter toevoegen.
2. **Kies één kleine feature.** Pak hoogste prioriteit uit `docs/ANIMATION_BACKLOG.md`. Bepaal voor welke bestaande spelactie hij is en wat onveranderd moet blijven.
3. **Leg acceptatiecriteria vast.** Correcte trigger, niet blokkeren, 'minder beweging', groot genoeg aanraakgebied, kleine en grote schermen, voortgang intact.
4. **Bouw op een aparte branch.** Geen direct aanpassen van APK/DEX/Manifest. Alleen broncode; geen sleutels en geen persoonlijke kindgegevens in GitHub.
5. **Test de module.** Run `bash scripts/test-motion.sh` en `python3 scripts/check-repository.py`. Nieuwe effectvariant vereist tests voor grenzen, herhaling en minder beweging.
6. **Build de Android-app uit bron.** Wanneer `app/` en Gradle zijn teruggezet: compileer debug APK, run Android-lint en instrumentatietests.
7. **Android-emulator smoke test (hard gate).** Installeer de exacte build; open Start; controleer crashlog; speel een volledige ronde; oefen tikken, slepen, terug, pauze, profielwissel en draaien indien ondersteund; neem screenshots op twee schermformaten. APK-afgifte is verboden als dit niet slaagt of ontbreekt.
8. **Controle op Samsung (hard gate voor 'stabiel').** Update zonder uninstall met dezelfde app-ID/handtekening; controleer spelvoortgang, animaties en 'minder beweging'. Meld 'niet getest' totdat iemand dit werkelijk heeft gedaan.
9. **Review vóór release.** Maak PR met wat veranderd is, welke tests gelukt zijn, welke nog niet. Merge/publieke release uitsluitend na expliciete opdracht.
10. **Ronde afsluiten.** Noteer status en volgende prioriteit. Herhaal vanaf stap 1, niet vanaf een willekeurige nieuwe patch.

## Regressiechecklist
- Start zonder crash; terugknop werkt
- 'Speel meteen' start het laatst gespeelde spel
- Acht vragen / scores blijven kloppen
- Sleep de vormen blijft echt slepen en werkt met kleine vingers
- Sterren vangen, memory, eigen kamer, trofeeën blijven bruikbaar
- Profiel/oudersinstellingen en lokale opslag blijven intact
- Geen extra netwerkverkeer, accounts, permissies, advertenties of trackers
- Grote en kleine schermen; navigatieknoppen niet afgesneden
- Minder beweging toont de feedback statisch
- Update: package `nl.playlumi.app`, zelfde certificaat, oplopende versionCode

## Rapportage per ronde
Vertel tussendoor: (a) idee, (b) wat in broncode verandert, (c) testresultaten, (d) wat nog geblokkeerd is. Noem code 'module gereed' en NIET 'in de app' tot integratie en toestelsmoke aantoonbaar zijn.

## Geheimen en release
De privésleutel en bijbehorende wachtwoorden blijven buiten de publieke repository en uit alle logs. Bewaar originele appdata. Geen APK in publieke Actions-artifact, tenzij expliciet anders afgesproken. Geen autonome merge of publicatie.

## Wat nu daadwerkelijk kan
Motion core + automatische Java-tests + PR-review. Android-APK-build en emulator-tests vereisen de teruggewonnen originele Android-broncode. Een groene motion-test is **geen** bewijs dat de Playlumi-app opstart.
