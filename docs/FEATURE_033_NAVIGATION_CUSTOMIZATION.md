# Playlumi 0.33.0 — eenvoudig navigeren en aanpassen

## Navigatie
- Een vaste, duidelijke navigatiebalk met **Start | Spelen | Aanpassen | Tuin** op de gewone menu- en personalisatieschermen.
- Spelschermen houden voldoende ruimte voor hun opdrachten; bovenaan verschijnt een duidelijke startknop. De Android-terugknop blijft werken.
- Het scrollvlak krijgt extra ruimte onder de laatste knop, zodat de vaste navigatiebalk geen inhoud bedekt.
- De knop **Spelen** biedt zowel de leerwerelden als de minispellen.
- **Aanpassen** is één centrale plek voor stervriend, kleding, kamer, tuin, vriendjes, podium en trofeeën. De tuin is ook rechtstreeks beschikbaar via de balk.

## Customisatie
- Alle zes kledingcategorieën direct in een overzicht, zonder eerst drie pagina’s door te bladeren.
- Alle zeven opties per kledingcategorie rechtstreeks zichtbaar in een scrollbaar raster. Duidelijke labels: *nu aan*, *al in bezit en gratis*, of *aantal speelsterren*.
- Alle vijf kamercategorieën meteen zichtbaar.
- Alle zeven kameropties per categorie rechtstreeks zichtbaar, met dezelfde duidelijke statuslabels.
- Na ontgrendelen of aantrekken blijft de lijst op dezelfde scrollpositie staan: je raakt je plek niet kwijt.
- Kleiner voorbeeld van personage of kamer, zodat de opties eerder in beeld komen.
- Geen nieuw betaalmiddel of echt geld. Alle gekochte items en geselecteerde onderdelen blijven bestaan.

## Regressie en compatibiliteit
- Android-app-ID `nl.playlumi.app`, bestaande profielen en opgeslagen score/tuin/kleding/kamerdelen blijven ongemoeid.
- De bestaande ouderpoort blijft ongewijzigd en is niet via de navigatiebalk te omzeilen.
- Tests controleren alle categorieën, prijs/statuslabels en dat reeds gekochte items niet opnieuw betaald hoeven te worden.
- Gradle unit-tests, lint, debugbuild, emulator-opstart en releasebuild vereist.
- Versie 0.33.0 met hoger versionCode dan 18247, ondertekend met ongewijzigde private releasekey.
- Fysieke Samsung-toesteltest en volledig navigatiepad zijn nog niet handmatig gecontroleerd.
