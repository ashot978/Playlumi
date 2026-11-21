# Technische nulmeting (21 november 2026)

- Bestaande app: `nl.playlumi.app`.
- Laatste herstel-APK die in gesprekken is gemaakt: Playlumi 0.25.4, versionCode 10225.
- Die versie gebruikt de gamecode en resources van 0.25.0; de tussentijdse 0.25.1–0.25.3 presentatiepatches zijn teruggedraaid.
- 0.25.3 crashte direct bij het openen, ook na reinstall. Geen crashlog en geen bevestigde oorzaak.
- Ook van 0.25.4 is **geen** echte Android-opstarttest of bevestigde Samsung-test.
- Publieke GitHub-repository had bij aanvang alleen README; broncode, Gradle, Android-tests en CI waren afwezig.
- Bewaar Playlumi-releasekeystore en wachtwoorden uitsluitend privé. **Niet** toevoegen aan GitHub, issues, Actions logs of publieke artifacts.

## Geblokkeerde stappen
- Een nieuwe werkende APK claimen
- Broncodewijzigingen integreren in de bestaande Android-app
- UI/animatie op een Samsung als getest aanmerken
- Release of merge zonder expliciete opdracht

## Veilige voortgang
De motion-core kan al worden gebouwd en getest als apart stuk broncode. Dit is voorbereidend werk voor latere integratie en komt **niet** terecht in de bestaande APK tot die integratie aantoonbaar is uitgevoerd.
