# Playlumi 0.44.0 — Voorleesstem instellen en beluisteren

## Nieuw in ouderinstellingen
- **🔊 Test de voorleesstem** speelt alleen na een bewuste tik een korte Nederlandse voorbeeldzin af.
- **Spreektempo** wisselt tussen Rustig, Normaal en Vlot; standaard wordt het natuurlijke normale tempo gebruikt.
- **Nederlandse stem** kan bij meerdere aanwezige offline stemmen worden gewisseld. De knop toont hoeveel opties Android aanbiedt.
- De gekozen stem en snelheid worden lokaal per kindprofiel onthouden. Ontbreekt de gekozen stem na een Android-update, dan kiest Playlumi opnieuw een beschikbare Nederlandse stem.
- **Geluidseffecten staan standaard uit** bij nieuwe installaties en profielen zonder bestaande voorkeur. Een eerdere bewuste keuze blijft behouden.
- De bestaande aan/uit-knoppen voor geluid en spelgeluiden blijven behouden.

## Bewuste stilte
Vragen, verhalen, spelbubbels en antwoorden worden nooit automatisch voorgelezen. Alleen de bestaande 🔊-knoppen, het doelbewust lang indrukken en de nieuwe testknop kunnen de stem activeren. De voorleeskwaliteit hangt nog steeds af van de Android-spraakengine en de geïnstalleerde Nederlandse stemmen; de app downloadt niets.

## Veilig en compatibel
Geen nieuwe permissies, advertentietrackers, accounts of onlinestemmen. De 24 spellen, 300-vragendecks, badges, spelregels, ouderslot, lokale scores, outfits, tuin en andere voortgang blijven ongewijzigd.

## Controles
Extra tests voor veilige spreektempo's, het wisselen van offline stemmen en het niet opnieuw introduceren van automatisch voorlezen. CI moet unit-tests, lint, debugbuild, emulatorstart en releasebuild laten slagen. Privé ondertekenen met oorspronkelijke sleutel en controleren op versiecode, app-ID en certificaat. Een echte toesteltest blijft nodig voor de subjectieve klankkwaliteit.
