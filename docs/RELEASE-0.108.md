# Playlumi 0.108

Android version code: **601009**. Package: `nl.playlumi.app`.
Baseline: `d18a6279672ef8b045028be8df25109f4c777d3f` (0.107).

## Wat verandert er?

### Een stad die zichtbaar groeit

De stad heeft zeven getekende plekken. Een nog niet verdiende plek is een groen bouwperceel; na de bijbehorende prestatie verschijnt het park, gebouw, podium, de brug of het monument. Tik op een plek voor de echte voorwaarde, voortgang en het bijbehorende cadeau. Vanuit de stad kun je naar passende spellen, vriendjes, de kledingkast en je schatten.

De oorspronkelijke geïllustreerde startpagina blijft behouden. De nieuwe kaart heeft afzonderlijke Android-knoppen voor aanraken, toetsenbord en schermlezer, en past het aantal kolommen aan aan schermbreedte en tekstgrootte.

### Lumi draagt zijn outfits

De cape, het sterrenpak, de regenboogtrui en het astronautenpak zijn nu getekende kleding om Lumi's stervorm. Strik en sjaal zijn eveneens getekend. Het gezicht blijft zichtbaar. Alle bestaande aankoop-, eigendoms- en kledingcodes blijven gelijk.

### Drie nieuwe spelvormen

| Spel | Leeftijdsgroepen | Wat oefen je? |
| --- | --- | --- |
| Evenveel | 3–4, 5–6, 7+ | Gelijke hoeveelheden herkennen met verschillende vormen; tot 3, 6 of 10. |
| Eerst en daarna | 5–6, 7+ | Redeneren over het resultaat van een beschreven handeling; 24 concrete situaties, waarvan 12 eenvoudigere. |
| Tientallen en eenheden | 7+ | Getallen van 10 tot en met 99 samenstellen uit tientallen en eenheden. |

Het aanbod is nu 25, 43 en 68 spelroutes per leeftijdsgroep. Oude spelcodes en bewaarde rondes veranderen niet.

### Dagopdrachten en opnieuw spelen

Een dagopdracht opent rechtstreeks een passende quiz, of hervat een begonnen quiz die aan die opdracht bijdraagt. Na spelen kun je terug naar de opdrachten. De dagelijkse teller wordt na terugkeer in de app en bij een nieuwe kalenderdag ververst. Er komen geen meldingen, tijdsdruk of verloren beloningen bij.

Het eindscherm van een gespeelde catalogusronde laat het werkelijke aantal herhalingen, het meesterschapsniveau en de volgende mijlpaal zien. Nieuw verdiende stadsplekken krijgen een verwijzing naar de stad. De bestaande sterrenbedragen blijven gelijk.

### Bewaren en bedienen

Meer speltypes bewaren de beloning, score, voortgang en rondegegevens samen. Dit geldt voor interactieve borden, minispellen, muziek, bellen, sterren vangen en de eenmalige vriendjes-/tuinbeloningen. De telefoonstartpagina heeft nu afzonderlijke bereikbare bedieningselementen boven de bestaande tekening. Lange antwoorden kunnen verticaal meegroeien en kleine liggende vensters krijgen een leesbaardere indeling.

## Verificatie

De verplichte Java-controle omvat de bestaande leerstof en herstelregels, de nieuwe inhoud, dagopdrachtroutes en stadsgeometrie. GitHub bouwt debug en release, controleert alle oorspronkelijke illustratiebestanden en draait unit-tests en Android Lint vóór het beschikbaar maken van de APK.

De telefoon- en Fold-emulatorproeven omvatten echte bediening, quizherstel, dagbeloningen, een vrijgespeeld park, gratis cadeaukeuze, interactieve bordherstel, nieuwe vragen en outfitvoorbeelden. Een afzonderlijke updateproef installeert 0.108 over 0.107 met dezelfde tijdelijke debughandtekening en vergelijkt bewaarde voortgang en een echte onvoltooide quiz.

De uiteindelijke APK wordt privé ondertekend met de bestaande Playlumi-releasehandtekening, waarna pakket, versie, handtekening, ZIP-uitlijning en ongewijzigde inhoud worden gecontroleerd. Deze technische en emulatorproeven vervangen geen bediening op een fysiek Samsung Fold-toestel. De bouw is geen Play Store-publicatie.
