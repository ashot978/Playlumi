# Playlumi 0.32.0 — de levende Magische Groeituin

**Wat is nieuw**
- Iedere volledig afgeronde leer-speelronde laat automatisch precies één plant één groeifase verder komen.
- Een reeds begonnen plant wordt eerst afgemaakt, zodat kinderen duidelijk zien hoe een kiem via kleine blaadjes naar een bloeiende plant groeit.
- De bestaande water- en zonverzorging werkt nog steeds en kan de plant ook verder laten groeien.
- Op het eindscherm verschijnt de naam van de groeiende plant en de groeifase, of een bloeibericht met de bestaande eenmalige sterrenbonus.
- De tuin-scène toont een zachtere hemel, bloemen aan de rand en een echte overgang tussen oude en nieuwe plantfase, met licht en zachte fonkels. De animatie stopt vanzelf.
- In de instelling 'minder beweging' wordt de nieuwe groeifase direct zonder animatie getoond.

**Bescherming van bestaande gegevens**
- Groeifases worden met de bestaande `garden_stage_*` velden per lokaal kinderprofiel opgeslagen. Geen migratie van oude data nodig.
- De oude verzorgingsacties water / zon blijven leidend.
- Een bloeibeloning wordt alleen uitgereikt als het bestaande reward-mask aangeeft dat deze nog niet is uitgedeeld.
- Geen nieuwe permissies, reclame, account, trackers of netwerkverkeer.
- Eenmaal voltooide 12 planten blijven bloeien; extra speelrondes veranderen ze niet.

**Tests en release**
- Unit-tests voor 48 groeistappen, eerste bloei, dubbele-beloningpreventie en herstel van bestaande gegevens.
- Tests voor animatiebegin, eindpunt, minder beweging en 10.000 extreme tijdswaarden.
- Android-unit-tests, lint, debugbuild en emulator-opstarttest; pas daarna een ondertekende 0.32.0-APK.
- App-ID blijft `nl.playlumi.app`; de versiecode wordt hoger dan 17243.
- Ondertekening blijft uitsluitend lokaal, niet via GitHub.
- Fysieke Samsung-acceptatietest is niet uitgevoerd en blijft een apart aandachtspunt.
