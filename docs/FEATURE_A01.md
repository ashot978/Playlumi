# A01 – Sterrenspoor op de bestaande Playlumi-app

## Aanleiding
In Playlumi versie 0.25.3 crashte de app direct door binaire DEX-patches.
Vanaf deze ronde passen we uitsluitend **echte Java-broncode** aan in de
bestaande Android-codebasis op branch `playlumi/eerste-telspel`.
Het app-ID blijft `nl.playlumi.app`; er zijn geen nieuwe permissies,
netwerkaanroepen, tracking of wijzigingen in opgeslagen spelvoortgang.

## Wat verandert in het spel
Na een correct antwoord:
1. Playlumi verwerkt het antwoord en de score volgens de bestaande regels.
2. De bestaande sterrenbalk krijgt de behaalde ster en blijft voor screenreaders leesbaar.
3. Op **gewone antwoorden** vliegt een gouden ster vanaf de echte antwoordknop
   in 720 ms langs een zachte boog naar het juiste ster-slot.
4. Er lopen drie kleine, vervagende lichtpuntjes achteraan. Ze blokkeren niets.
5. Bij mijlpalen blijft de bestaande, grotere viering het enige hoofdeffect.
6. De speler kan altijd direct verder spelen. De animatie is alleen een
   visuele overlay die wordt verwijderd bij einde of navigatie.
7. Bij 'minder beweging' of uitgeschakelde systeemanimaties wordt de
   vliegende ster overgeslagen; de gewone voortgangsster verschijnt direct.

## Acceptatiecriteria
1. Alleen een correct antwoord kan de vlucht starten.
2. Het echte antwoordvak is het vertrekpunt; het nieuw behaalde
   ster-slot is het doel, ook bij andere aantallen sterren.
3. De vliegende ster wordt niet gebruikt om spelvoortgang toe te kennen.
4. Bij scrollen wordt de vlucht in het zichtbare scherm begrensd, zonder
   antwoordknoppen of sterrenbalk te verplaatsen.
5. De overlay accepteert geen aanrakingen en is niet zichtbaar voor TalkBack.
6. De animatie stopt na 720 ms of zodra het scherm sluit.
7. Verkeerde, niet-geldige of extreem grote posities veroorzaken geen crash.
8. Reduced motion is direct stilstaand, zonder flitsen.
9. Bestaande scores, profielen, spelregels, bestanden en toetsenbordgedrag
   mogen niet wijzigen.
10. Android-unit-tests, lint, compilatie én echte emulator-opstarttest
    moeten slagen; een Samsung-test is nog vereist vóór een stabiele APK.

## Aanpassingen
- `StarFlightMotion.java`: platformonafhankelijke, testbare animatieberekening.
- `StarFlightMotionTest.java`: eindposities, invalid input, minder beweging,
  tijdsgrenzen en duizenden gegenereerde frames.
- `PlaylumiFlyingStar.java`: Android-overlay die na een correct antwoord
  tijdelijk een ster en drie fonkels tekent, daarna zichzelf opruimt.
- `MainActivity.java`: bestaande scorelogica onaangeroerd, alleen de
  beloningsanimatie bij gewone correcte antwoorden vervangen.
- `.github/workflows/android.yml`: unit-tests, lint, debugbuild en
  afzonderlijke emulator-opstartcontrole; geen automatisch APK-artifact
  bij normale push/pull request.

## Releasestatus
**Nog geen nieuwe APK voor installatie of publicatie.**
De broncode is op een losse featurebranch gezet. CI-tests en emulator-status
moeten per commit worden gecontroleerd. Ook na geslaagde emulatorstart zijn
een volledige spelronde en een test op een echte Samsung nog nodig.
De versiecode voor een release moet aantoonbaar hoger zijn dan de
laatst geïnstalleerde recoveryversie.
