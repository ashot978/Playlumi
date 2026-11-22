# Playlumi 0.45.0 — Rustig Muziekatelier

## Nieuwe verbetering
- Het **Muziekatelier** laat weer noten horen wanneer een kind bewust op *Speel de melodie af* of een instrument tikt, ook als decoratieve geluidseffecten uitstaan.
- De algemene instelling *Geluid: uit* maakt de hele app stil, inclusief het Muziekatelier.
- De melodie wordt **nooit automatisch** afgespeeld bij het openen van een spel of de volgende ronde.
- Nieuwe knop **⏹ Stop de melodie** onderbreekt direct het voorbeeld.
- Een tweede keer de melodie afspelen annuleert het eerste voorbeeld, zodat nooit meerdere voorbeelden door elkaar klinken.
- Tikken op een instrument onderbreekt het voorbeeld en laat alleen de zelf gekozen noot horen.
- Bij wisselen van scherm of app-pauze stoppen geplande en lopende tonen.

## Veiligheid en spelregels
- Geen nieuwe permissies, internet, accounts of advertenties.
- De vier bestaande muziekinstrumenten, alle melodierondes, punten, badges, profielen, data en ouderbeveiliging blijven ongewijzigd.
- De voorleesstem blijft alleen actief op uitdrukkelijk verzoek; geen automatische spraak.

## Kwaliteitscontrole
- Extra unit-tests controleren dat Muziekatelier bij geluidseffecten uit wel hoorbaar is, dat globaal dempen alle geluiden uitzet en dat oude afspeellijsten netjes stoppen.
- Android CI moet tests, lint, debugbuild, emulatoropstart en releasebuild voltooien.
- APK privé ondertekenen met het bestaande Playlumi-certificaat en de hogere versiecode controleren.
- Een echte toesteltest met muziekgeluid aan/uit blijft een zinvolle laatste controle.
