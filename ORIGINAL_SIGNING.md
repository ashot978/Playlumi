# Playlumi signing baseline — NIEUW VANAF 0.62.0

**Nieuwe, door de gebruiker goedgekeurde releaseidentiteit.** De gebruiker kiest bewust voor een schone herinstallatie om een nieuw releasecertificaat te gebruiken. Dit vervangt de oude v0.45-identiteit voor alle toekomstige updates. De oude sleutel is NIET vervangen en oude APK's kunnen niet rechtstreeks worden bijgewerkt met deze identiteit.

- Android package: `nl.playlumi.app`
- Nieuwe signing key: RSA 4096, PKCS12, alias `playlumi-release`
- Nieuwe certificate SHA-256: `09:99:D6:3A:1A:A1:D8:04:18:E0:B4:FA:1E:A0:15:13:3A:DD:F7:3B:50:75:76:61:71:A9:A6:73:3D:14:AA:85`
- De oude v0.45 handtekening was `D2:B7:4F:01:8B:F6:11:13:82:F9:01:C2:AC:DC:03:E7:87:81:1C:E2:07:D6:C4:43:5F:CC:A9:55:9A:E9:B0:EF` en is **niet uitwisselbaar** met deze nieuwe sleutel.
- Sleutel gegenereerd op 2026-11-22. Geldigheid circa tot april 2054.
- De gebruiker heeft de geheime keystore en wachtwoord apart als herstelbestanden ontvangen. **Deze zijn niet opgeslagen in deze publieke repository en zijn niet gegarandeerd beschikbaar in een latere ChatGPT-sessie**.
- Bewaar de `.p12` en het wachtwoord in een veilige persoonlijke back-up/passwordmanager.

## Eenmalige GitHub-configuratie

Zet via GitHub Actions repository secrets:
1. `PLAYLUMI_RELEASE_KEYSTORE_BASE64`: base64-inhoud van de nieuw gegenereerde PKCS12 keystore (zie herstelpakket)
2. `PLAYLUMI_SIGNING_PASSWORD`: het wachtwoord uit het aparte wachtwoordbestand.

De CI gebruikt deze secrets om release-APK's te ondertekenen en controleert de **nieuwe** SHA-256 fingerprint. Deze twee secrets zijn absoluut noodzakelijk voor iedere toekomstige gesigneerde release. Een succesvolle build met uitsluitend unsigned APK is geen installeerbare release.

## Oude app verwijderen

Als v0.45 of een andere APK met de vorige signing key op het toestel staat, zal Android de nieuwe APK weigeren als update (`INSTALL_FAILED_UPDATE_INCOMPATIBLE`). Maak eerst waar mogelijk een export/back-up van gebruikersgegevens; daarna moet de oude installatie verwijderd worden. Dit **kan kindprofielen, spelvoortgang en alle lokale gegevens definitief wissen**. Niet stilzwijgend de app verwijderen. De eerste nieuwe ondertekende APK is een schone installatie.

Daarna moeten alle toekomstige updates exact dezelfde nieuwe keystore gebruiken en steeds een hogere versionCode krijgen.

## Verplichte controles voor alle agenten

1. Controleer branch, exact HEAD SHA, AGENTS.md, RELEASE_CHECKLIST.md en GitHub Actions.
2. Ontwikkel/test, bouw debug + release en controleer emulator.
3. Vereis `playlumi-newkey-signed-release` uit de exact geslaagde GitHub run.
4. Vereis package `nl.playlumi.app`, versiecode voor upgrade en bovenstaande nieuwe SHA-256 signer; controleer handtekeningen met `apksigner verify`.
5. Haal de APK op voor rechtstreekse download. Geen unsigned of debug APK als normale installatie aanbieden.
6. Indien signing secrets ontbreken: meld de externe blokkade, genereer niet opnieuw een andere sleutel en presenteer geen APK als gereed.

**Nooit keystore, base64-keystore of wachtwoord committen of in agent memory / chatnotities plaatsen.** Alleen de publieke vingerafdruk en herstelprocedure mogen daar worden opgeslagen.
