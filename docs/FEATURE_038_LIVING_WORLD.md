# Playlumi 0.38.0 — een levende speelwereld in plaats van blokken

## Visuele vernieuwing
- **Echte zachte speelbubbels** voor de zes directe spelkeuzes: grote geïllustreerde rondachtige vormen met een eigen zweefritme, lichte glans en subtiele schommeling. Een naamlabel blijft duidelijk leesbaar.
- **Een kleine geanimeerde wereld achter de menu's:** zonnige gloed, zacht bewegende wolken, rollende heuvels, bloemetjes, een vlinder en fonkelende accenten. Achtergrond is decoratief en onderschept geen tikken.
- **Organische wolkenvormen** in de kindermenu's, de vraagkaart, het illustratievlak en de antwoorden; niet langer overal dezelfde vierkante Android-tiles.
- **Aanraakreacties:** bubbel krimpt licht bij aanraking en veert terug; bestaande ster- en mascotteanimaties blijven werken.
- **Zonder extra afleiding:** alle effecten zijn rustig, pauzeren bij afkoppelen, en de instelling 'minder beweging' toont een volledige statische interface.

## Speelervaring en continuïteit
- Dezelfde zes leeftijdspassende snelkeuzes, uitgebreide spellen, alle 300 geschudde vragen per spelvorm en moeilijkheid en bestaande achtvragenrondes.
- Geen wijziging aan leerscores, badges, sterren, tuin, outfit, verzamelde spullen, favorieten of ouderinstellingen.
- Echte native Android Canvas-illustraties, zonder internet, externe beeldbibliotheken, trackers, accounts of advertenties.
- Focusbare bubbels met gesproken toegankelijkheidslabels en grote aanraakvlakken, zodat het niet alleen om bewegen draait.

## Kwaliteitscontrole en APK
- Unit-tests voor duizenden animatiefasen, correcte begrenzingen, verschillende ritmes en de volledige statische 'minder beweging'-stand.
- Bestaande Android-unit-tests, lint, debugbuild, emulatorstart en unsigned releasebuild moeten slagen.
- Nieuwe privé ondertekende Playlumi 0.38.0 APK, app-ID `nl.playlumi.app`, hogere versiecode dan 23271 en hetzelfde bestaande ondertekeningscertificaat.
- Een werkelijke Samsung-toesteltest met doorlopen schermen blijft aanvullend nodig; emulatorstart is geen bewijs dat elke animatie foutloos speelt.
