# A02 - Vormenmagneet (ontwikkelronde 1)

**Status: animatiebroncode en lokale Java-tests gereed. NIET geintegreerd in de Android-app.**

## Gedrag
Bij een **reeds correct bevonden** vormmatch mag de tekenlaag een loslaatpositie
(vanX, vanY) en het midden van het bijpassende huisjesvak (naarX, naarY)
doorgeven aan `ShapeMagnet.sample`.

- De vorm glijdt in 560 ms licht gebogen naar het midden van het vak.
- Bij aankomst wordt de vorm kort ongeveer 13% groter en veert dan terug.
- Drie kleine fonkels verschijnen rond de vorm, worden eenmaal zichtbaar
  en verdwijnen weer zonder felle flits.
- Met 'minder beweging' springt de vorm direct naar de eindpositie en
  wordt het resultaat statisch benadrukt. Er volgt geen animatie.
- De getoonde vorm mag **nooit** de aanraakbesturing, foutafhandeling of score
  van de bestaande app wijzigen.

## Acceptatiecriteria
1. Startpositie en doelpositie worden per individuele vormmatch gebruikt, niet
   een vaste voorbeeldpositie.
2. De bestaande spelregel beslist eerst of de vorm correct is; animatiecode
   bepaalt dat niet en schrijft geen scores of voortgang.
3. Geen blokkerende wachttijd voor de volgende actie.
4. Na 560 ms is de vorm exact op het doel en stoppen de frame-aanvragen.
5. Een loslaatpositie buiten het scherm, NaN of Infinity kan de animatie
   niet laten crashen. Waarden worden veilig omgezet naar 0..1.
6. Geen flikkerende fonkels; maximaal drie subtiele stippen/sterren.
7. 'Minder beweging' levert zonder tussentijdse frames een statische bevestiging.
8. De echte app moet opstarten en een volledige spelronde zonder crash spelen,
   voordat een nieuwe APK als klaar mag worden beschouwd.

## Testbewijs
- Lokaal: `javac` en `java` voor `ShapeMagnetTest`, 153.684 assertions PASS.
- Remote: GitHub Actions `Motion core checks` moet ook PASS zijn.
- Android-APK en Samsung: **niet getest**, originele Android-broncode ontbreekt.

## Concreet integratiecontract
```java
// Alleen in de bestaande callback NA een correcte match, op de UI-thread.
// De tekenlaag bepaalt fromX/fromY/toX/toY binnen hetzelfde tekenvlak.
ShapeMagnet.Frame frame = ShapeMagnet.sample(
    fromX, fromY, toX, toY,
    millisecondsSinceMatch, reduceMotion);

// Teken de bestaande vorm op frame.x/y, schaal, rotatie.
// Teken maximaal drie zachte fonkels rond frame.x/y met:
// frame.sparkleRadius en frame.sparkleOpacity.
// Een bescheiden statische gloed gebruikt frame.targetGlow.
// Stop frame-aanvragen wanneer frame.finished true is.
```

Deze code is illustratief: `ShapeMagnet` wordt pas aangeroepen nadat de
oorspronkelijke Android-code en de correcte game-event callback zijn hersteld.
