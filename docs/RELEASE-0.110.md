# Playlumi 0.110

Android version code: **601011**. Package: `nl.playlumi.app`.
Baseline: [c31400887005acd24c1d418eb6084f8156dbe9b5](https://github.com/ashot978/Playlumi/commit/c31400887005acd24c1d418eb6084f8156dbe9b5) (geverifieerde 0.109).
Status: **in ontwikkeling**. Deze beschrijving legt het werkpakket vast; de definitieve Android-bouw, emulatoracceptatie en ondertekening moeten nog worden uitgevoerd.

## Meer mogelijkheden in de Tekenstudio

De Tekenstudio krijgt een potlood, een gum en drie lijndiktes: dun, normaal en dik. De knoppen laten hun gekozen toestand ook aan Android-toegankelijkheid weten. Kleur, gereedschap en dikte worden lokaal bij het eigen profiel bewaard. De gum moet zowel nieuwe lijnen als delen van een eerder opgeslagen afbeelding kunnen weghalen, met het witte tekenpapier als ondergrond.

Een tekengebaar wordt als één actie bewaard. ‘Laatste lijn weg’ blijft beschikbaar en krijgt ‘Laatste lijn terug’ als tegenhanger. Ook beginnen met leeg papier kan binnen het actieve tekenblad worden teruggedraaid. Een nieuwe tekenactie vervangt een eerder teruggedraaide vervolgactie; alleen van kleur of gereedschap wisselen doet dat niet. De geschiedenis bewaart maximaal 32 acties. Ouder werk wordt in de onderliggende afbeelding opgenomen zodat de tekening zichtbaar blijft zonder onbeperkt geheugen voor alle eerdere gebaren te gebruiken.

Bewaren en een korte achtergrondonderbreking houden de actieve Undo/Redo-geschiedenis bruikbaar. Na het sluiten en opnieuw openen van het tekenblad blijft de bestaande PNG de opgeslagen tekening; oudere afzonderlijke gebaren worden niet als langdurige bewerkingsgeschiedenis bewaard. De bestaande profielbestandsnamen, AtomicFile-opslag en eenmalige eerste-bewaarbonus van negen sterren blijven behouden. Autosave en gereedschapskeuze geven geen extra beloning.

## Drie aanvullende leerroutes

| Route | Leeftijd | Oefening |
| --- | --- | --- |
| **Tel de vormen** · `0:0:0:5` | 3–4, 5–6 en 7+ | Een gevraagde vorm tellen tussen verschillende vormen. |
| **Dagen van de week** · `0:0:16:5` | 5–6 en 7+ | De volgorde van weekdagen herkennen. |
| **Lees en kies** · `1:0:6:5` | 7+ | Een korte tekst lezen en het passende antwoord kiezen. |

Het beoogde catalogusaanbod wordt **26, 45 en 71 routes** per leeftijdsgroep. Bestaande routecodes, opgeslagen vragen, voortgang en herhalingsaantallen houden hun betekenis. De nieuwe routes gebruiken dezelfde native antwoordknoppen, geselecteerde antwoordfeedback, leeftijdsfilters en herstelopslag als andere quizzen. Er komen geen hint- of spraakknoppen bij.

## Gerichte afwerking

De Tekenstudio krijgt beter leesbare feedback op de paarse achtergrond en een kop die ook op de telefoon past. Het ouderoverzicht gebruikt enkelvoud bij één getelde ronde of één afgeronde oefening. De stadstegels krijgen een passende uitlijning. Deze ingrepen volgen uit de daadwerkelijk beoordeelde 0.109-emulatorbeelden.

## Verificatie vóór oplevering

Alle bestaande build-, inhouds-, voortgangs- en emulatorchecks blijven verplicht. `FoundationsCheck` en `DrawingHistoryCheck` voegen onafhankelijke controles toe voor de nieuwe leerinhoud, begrensde tekengeschiedenis en papiergeometrie. De native proeven bedienen de gereedschappen, Undo/Redo en bevestigd leegmaken, met afzonderlijk gelabelde profielvoorbereiding en controle van gescheiden bestanden, keuzes en beloningen.

De emulatorproef vergelijkt ook een echte in de gepinde 0.109-bouw getekende PNG vóór en na `install -r`, naast de bestaande wallet-, eigendoms-, favoriete-, ronde- en quizgegevens. Een gerichte Android-rasterproef wist een deel van een geladen PNG en maakt daarna 32 afzonderlijke gebaren: het gewiste deel moet wit blijven, het ongewijzigde deel dezelfde pixels behouden. Na leegmaken en nog 32 gebaren mag de oude afbeelding niet terugkomen. Daarmee wordt het daadwerkelijke Android-tekenwerk naast het onafhankelijke historiemodel gecontroleerd. Eventuele rechtstreeks voorbereide profiel- of tijdgegevens worden als testfixture benoemd; gespeelde gebeurtenissen worden alleen als zodanig beschreven wanneer de native UI ze werkelijk heeft uitgevoerd.

De CI bouwt zonder de privésleutel. Alleen de definitieve gecontroleerde productie-APK wordt daarna privé ondertekend met het oorspronkelijke releasecertificaat. Pakket, versie, certificaat, ZIP-uitlijning en de relatie met de gebouwde bron moeten vóór oplevering worden gecontroleerd. Er wordt pas een geslaagde 0.110-uitkomst gemeld wanneer deze controles op de definitieve bron zijn uitgevoerd.

Een fysieke Samsung Fold is nog niet beschikbaar. De emulatorproeven bewijzen daarom de geteste configuraties en vervangen geen fysieke bediening, vouwen/uitvouwen of updateproef met de originele ondertekening op dat toestel. Een testbouw omvat geen merge, tag, openbare release of Play Store-publicatie.
