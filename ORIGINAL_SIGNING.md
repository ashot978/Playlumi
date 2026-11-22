# Installatie-updates met het originele Playlumi-certificaat

## Waarom versie 0.55 t/m 0.60 niet over v0.45 installeerde

Vanaf v0.55 bood de ontwikkelworkflow een debug-APK aan. Een debug-APK heeft een ander ondertekeningscertificaat dan de op het toestel geïnstalleerde release-APK. Android blokkeert een update met dezelfde pakketnaam en een andere ondertekening. **Verwijder de bestaande app niet**: daarmee kunnen lokale kindprofielen en spelvoortgang verdwijnen.

v0.45 is lokaal privé ondertekend met het oorspronkelijke Playlumi-certificaat. Die sleutel stond niet in GitHub. De normale GitHub workflow bouwde toen ook een *unsigned* release-APK.

- Android package: `nl.playlumi.app`
- Oorspronkelijke v0.45 versionCode: `24302`
- Oorspronkelijke SHA-256 certificaatvingerafdruk:
  `D2:B7:4F:01:8B:F6:11:13:82:F9:01:C2:AC:DC:03:E7:87:81:1C:E2:07:D6:C4:43:5F:CC:A9:55:9A:E9:B0:EF`

## Herstelde veilige buildroute

De CI behoudt de tests, lint, Android-emulator en de niet-ondertekende releasebuild. Een APK die over de bestaande installatie heen kan, mag alleen als `playlumi-original-signed-release-update` worden gepubliceerd als deze controles zijn voltooid en de originele sleutel beschikbaar is.

Benodigde GitHub Actions **repository secrets** (nooit in code, issues, logs of chat plakken):

- `PLAYLUMI_RELEASE_KEYSTORE_BASE64`: de bestaande originele PKCS12-keystore, als base64-encoded secret
- `PLAYLUMI_SIGNING_PASSWORD`: het wachtwoord voor de originele keystore en alias `playlumi-release`

De workflow bouwt daarna de ondertekende release-APK opnieuw, valideert de APK-ondertekening met `apksigner verify` en vergelijkt de certificaatvingerafdruk byte-voor-byte met die van v0.45. Bij ontbrekende secrets wordt **geen ondertekende APK** gepubliceerd; bij onvolledige of verkeerde secrets faalt de signingstap. Er wordt geen nieuwe sleutel aangemaakt.

De versiecode van de nieuwe APK moet bovendien hoger zijn dan die van de geïnstalleerde versie. Voor de bestaande ontwikkelworkflow is dit gekoppeld aan het oplopende GitHub runnummer. Voor uitgifte altijd de effectieve versiecode op de ondertekende APK verifiëren.

## Wanneer een download aanbieden

1. Hele GitHub Actions-run is groen: unit-tests, lint, debugbuild, emulator-opstart en releasebuild
2. Artifact `playlumi-original-signed-release-update` is aanwezig, niet verlopen en komt uit de exacte commit
3. De originele fingerprint is geverifieerd en de versiecode is geschikt voor de geïnstalleerde versie
4. Een echte toesteltest/acceptatie bevestigt dat de update werkt en profielgegevens behouden blijven

Tot die tijd **geen APK presenteren als installeerbare update**. De unsigned release is slechts bedoeld voor privéondertekening. Een debug-APK is niet geschikt als update over de release-app.

Als de originele privésleutel/keystore niet meer beschikbaar is, kan dezelfde pakketnaam met behoud van de bestaande lokale appdata niet met een willekeurig nieuwe sleutel worden bijgewerkt. Bewaar originele sleutels uitsluitend in een privéomgeving.
