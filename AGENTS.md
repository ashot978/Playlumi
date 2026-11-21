# Playlumi – afspraken voor ontwikkelagenten

Playlumi is een bestaande Android-kinderapp. Herbouw haar nooit als nieuwe app; werk door op de originele code en spelregels. Communiceer in het Nederlands.

## Harde beperkingen
- Clash Timer Hub is een ander project: niet aanraken.
- Geen directe aanpassingen in `classes.dex`, binaire AndroidManifest, resource-tabellen, APK of zip-signature.
- Houd `nl.playlumi.app`, dezelfde ondertekening en lokale voortgang bij updates.
- Keystore en wachtwoorden zijn privé; nooit committen, uploaden of loggen.
- Offline en kindvriendelijk: geen ads, aankopen, trackers, chat, accounts of onnodige permissies.
- Kleine, veilige wijzigingen per PR; geen merge of publicatie zonder expliciete opdracht.
- Stop nieuwe functionaliteit wanneer er een gemelde crash is; eerst reproduceren en repareren.
- Gebruik de herhaalcyclus in `docs/DEVELOPMENT_LOOP.md`. Rapporteer helder wat is gebouwd, getest en nog niet geïntegreerd.

Een geslaagde `motion-core`-test ≠ bewijs dat de APK opent. Een release vereist echte Android-build + emulator smoke + Samsung-acceptatie.
