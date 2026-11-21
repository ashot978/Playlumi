# Playlumi 0.34.0 — veilig uitproberen en sneller aanpassen

## Wat is veranderd
- **Probeer eerst:** tik een nog niet gekocht hoedje, outfit, kleur, bril of ander accessoire aan en je ziet direct hoe je stervriend eruitziet, zonder een enkele ster uit te geven.
- **Bewust vrijspelen:** een opvallende knop met de exacte prijs is nodig voordat sterren van je saldo afgaan.
- **Mijn kamer:** dezelfde veilige preview toont hoe meubels, muren, vloer, raam of versiering eruitzien voordat je iets vrijspeelt.
- **Eigen items blijven gratis:** wat je al hebt gekocht, kun je direct blijven gebruiken.
- **Snel wisselen:** horizontale categoriekeuzes boven beide aanpassingsschermen; je hoeft niet terug naar het grote categorieoverzicht.
- **Handige filters:** Alles, Van mij, Te sparen, met aantallen per categorie.
- **Kies en blijf:** de lijst behoudt na aantikken zoveel mogelijk je scrollpositie.
- Bestaande sterren, aankopen en de daadwerkelijk geselecteerde kleding en kamer blijven ongewijzigd totdat het kind een keuze bevestigt.
- Alles werkt volledig offline en vereist geen nieuwe Android-permissies, accounts, advertenties of tracking.

## Technische controles
- Tests tonen dat previewkopieën nooit het echte bezit, de sterren of de geselecteerde spullen wijzigen.
- Tests voor alle filters, categorieën en gratis hergebruik van items.
- Android-unit-tests, lint, debugbuild, emulator-opstarttest en daarna unsigned releasebuild.
- Ondertekening van APK gebeurt uitsluitend lokaal met bestaande Playlumi-releasekey; controle op app-ID, hogere versionCode en APK v1/v2.
- Een complete handmatige navigatie- en inrichtingstest op het fysieke Samsung-toestel is nog niet gedaan.
