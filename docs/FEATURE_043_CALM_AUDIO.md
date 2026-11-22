# Playlumi 0.43.0 — Rustig geluid, voorlezen alleen op aanvraag

## Nieuw
- De app praat niet langer automatisch bij het openen van vragen, plaatjesspellen, minispellen of interactieve verhalen.
- Een oude instelling 'automatisch voorlezen' wordt volledig genegeerd en kan niet opnieuw geactiveerd worden.
- Voorlezen gebeurt alleen na een bewuste tik op 🔊 of na lang indrukken op een spelkeuze of antwoord.
- De illustratie bij een vraag is geen verborgen voorleesknop meer.
- De begroeting en het badge-scherm openen zonder onverwachte geluidjes.

## Aangenamere geluidsinstellingen
- Nederlands (Nederland) krijgt de voorkeur; de Android-stem klinkt neutraler door normale toonhoogte en minder trage spraak.
- Playlumi probeert de beste offline Nederlandse TTS-stem te gebruiken die het toestel al aanbiedt. De werkelijk hoorbare stem hangt af van de Android-spraakengine en geïnstalleerde stemmen.
- Synthetische spelgeluidjes zijn zachter. Scherpe noodpiepjes zijn verwijderd.
- Ouderinstelling **Geluidseffecten aan/uit** staat los van de bestaande geluids- en voorleesfunctie.
- Geen audio-downloads, advertenties, accounts, analytics, nieuwe permissies of netwerkfuncties.

## Behouden
- Alle spelregels, 300-vragenstapels, lokale voortgang, profielen, stars/badges, tuin, favorieten, kleding, privacy en ouderbeveiliging.
- Hetzelfde Android-app-ID en dezelfde privé-ondertekeningssleutel.

## Releasekwaliteit
- Unit-tests voor Nederlandse offline-stemselectie en regressietest die automatisch voorlezen tegenhoudt.
- GitHub Actions vereist groene tests, lint, debugbuild, emulator-opstart en unsigned releasebuild.
- Nieuw privé ondertekend APK-bestand met v1/v2-verificatie. Een echte toesteltest blijft nodig om stemkwaliteit te beoordelen.
