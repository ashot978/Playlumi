# Playlumi 0.40.0 - een zichtbare luisterknop bij ieder plaatje

## Wat verandert er?
- Naast elk van de zes plaatjes staat nu een aparte, grote 🔊-knop van 48 dp als geluid aanstaat.
- Tikken op 🔊 spreekt de spelnaam uit, zonder het spel te starten.
- Tikken op de grote illustratie start zoals voorheen direct een spel.
- Ingedrukt houden van de illustratie kan nog steeds voorlezen.
- Bij geluid uit verdwijnen de extra luisterknoppen volledig zodat de speelwereld rustig blijft.
- Een screenreader kan beide acties afzonderlijk aanklikken: 'Speel Tellen' en 'Luister naar Tellen zonder het spel te starten'.

## Techniek en privacy
- Java/Android native FrameLayout-overlays; nieuwe knoppen zijn onafhankelijk focusbaar en hebben een aanraakvlak van 48dp.
- Bestaande lokale Android text-to-speech wordt gebruikt, zonder online dienst, account of permissie.
- De 18 spelkeuzes (zes voor elk van de drie leeftijden), scores, badges, 300 kaarten per spel, tuinen, favorieten, avatars en kindgegevens blijven ongewijzigd.
- Alleen UI en menuteksten zijn gewijzigd; offline progressie en app-ID blijven gelijk.

## Testen en uitbrengen
- Extra unit-tests op alle afzonderlijke luisterlabels, unieke keuzes en ongeldige indices.
- GitHub Actions: unit-tests, lint, debugbuild, emulator-opstart, unsigned release-APK.
- APK uitsluitend lokaal ondertekenen met het bestaande Playlumi-certificaat, met hogere versionCode.
- Een echte toesteltest met geluid aan/uit, TalkBack en verschillende schermbreedtes blijft zinvol.
- Geen merge van ontwikkelbranches naar main en geen store-publicatie.
