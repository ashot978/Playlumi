# Playlumi 0.49.0 — Maak je eigen deuntje

## Nieuwe functie in de Muziekdoos
- **🎹 Maak je eigen deuntje** met vier grote dierenknoppen: kikker, katje, kuikentje en vlinder.
- Tik de instrumenten aan en stel een melodie samen van maximaal twaalf tonen.
- **▶ Speel mijn deuntje** laat de noten in dezelfde volgorde horen, alleen na een bewuste tik.
- **↩ Laatste noot weg** maakt een foutje ongedaan.
- **🗑 Begin opnieuw** maakt de compositie leeg.
- De compositie wordt automatisch lokaal bewaard voor het huidige kind. Na het heropenen van de app is de melodie er nog.
- Stop, wijzig, een andere melodie kiezen of de app verlaten onderbreekt lopende muziek. Er klinkt niets automatisch bij het openen van de Muziekdoos.

## Belangrijke uitgangspunten
De nieuwe functie gebruikt de al bestaande vier originele instrumenttonen. De twaalf losse deuntjes uit 0.48.0 en de andere vrolijke geluidseffecten blijven. De menselijke voorleesstem blijft volledig afwezig. Geen microfoon, accounts, punten, sterren, advertenties of netwerkgebruik.

## Controle
Nieuwe Java-tests controleren het maximum van twaalf tonen, de vier instrumenten, veilig lokaal opslaan/herstellen, onafhankelijke composities en ongeldige opslagdata. GitHub Actions moet unit-tests, lint, debugbuild, emulatorstart en unsigned release doorlopen. De APK wordt daarna met het oorspronkelijke privé-certificaat ondertekend en op versiecode en handtekening gecontroleerd.
