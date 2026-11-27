# Playlumi 0.110

Android version code: **601011**. Package: `nl.playlumi.app`.
Baseline: [c31400887005acd24c1d418eb6084f8156dbe9b5](https://github.com/ashot978/Playlumi/commit/c31400887005acd24c1d418eb6084f8156dbe9b5) (geverifieerde 0.109).
Geverifieerde bron: [50230b814b6ac9af35fbb1d6464aa831e37e5123](https://github.com/ashot978/Playlumi/commit/50230b814b6ac9af35fbb1d6464aa831e37e5123). Status: **Android-bouw en telefoon-/Fold-emulatoracceptatie geslaagd**.

## Meer mogelijkheden in de Tekenstudio

De Tekenstudio heeft een potlood, een gum en drie lijndiktes: dun, normaal en dik. De knoppen laten hun gekozen toestand ook aan Android-toegankelijkheid weten. Kleur, gereedschap en dikte worden lokaal bij het eigen profiel bewaard. De gum wist zowel nieuwe lijnen als delen van een eerder opgeslagen afbeelding, met het witte tekenpapier als ondergrond.

Een tekengebaar wordt als één actie bewaard. ‘Laatste lijn weg’ blijft beschikbaar en krijgt ‘Laatste lijn terug’ als tegenhanger. Ook beginnen met leeg papier kan binnen het actieve tekenblad worden teruggedraaid. Een nieuwe tekenactie vervangt een eerder teruggedraaide vervolgactie; alleen van kleur of gereedschap wisselen doet dat niet. De geschiedenis bewaart maximaal 32 acties. Ouder werk wordt in de onderliggende afbeelding opgenomen zodat de tekening zichtbaar blijft zonder onbeperkt geheugen voor alle eerdere gebaren te gebruiken.

Bewaren en een korte achtergrondonderbreking houden de actieve Undo/Redo-geschiedenis bruikbaar. Na het sluiten en opnieuw openen van het tekenblad blijft de bestaande PNG de opgeslagen tekening; oudere afzonderlijke gebaren worden niet als langdurige bewerkingsgeschiedenis bewaard. De bestaande profielbestandsnamen, AtomicFile-opslag en eenmalige eerste-bewaarbonus van negen sterren blijven behouden. Autosave en gereedschapskeuze geven geen extra beloning.

## Drie aanvullende leerroutes

| Route | Leeftijd | Oefening |
| --- | --- | --- |
| **Tel de vormen** · `0:0:0:5` | 3–4, 5–6 en 7+ | Een gevraagde vorm tellen tussen verschillende vormen. |
| **Dagen van de week** · `0:0:16:5` | 5–6 en 7+ | De volgorde van weekdagen herkennen. |
| **Lees en kies** · `1:0:6:5` | 7+ | Een korte tekst lezen en het passende antwoord kiezen. |

Het catalogusaanbod is **26, 45 en 71 routes** per leeftijdsgroep. Bestaande routecodes, opgeslagen vragen, voortgang en herhalingsaantallen houden hun betekenis. De nieuwe routes gebruiken dezelfde native antwoordknoppen, geselecteerde antwoordfeedback, leeftijdsfilters en herstelopslag als andere quizzen. Er komen geen hint- of spraakknoppen bij.

## Gerichte afwerking

De Tekenstudio heeft beter leesbare feedback op de paarse achtergrond en een kop die ook op de telefoon past. Het ouderoverzicht gebruikt enkelvoud bij één getelde ronde of één afgeronde oefening. De stadstegels hebben een passende uitlijning. Het tekenvlak wordt begrensd bij het tekenen, zodat het de knoppen ernaast of eronder niet overschildert.

## Geverifieerde resultaten

De [Android-run 46038701981](https://github.com/ashot978/Playlumi/actions/runs/46038701981) bouwde debug en release, controleerde de vijf oorspronkelijke illustratiebestanden en doorliep **264 unit-tests in 56 suites**, zonder failures, errors of overgeslagen tests. Android Lint rapporteerde **0 fouten en 176 waarschuwingen**. Alle vijf onafhankelijke Java-controles, waaronder `FoundationsCheck` en `DrawingHistoryCheck`, slaagden.

De [UI-run 46038701925](https://github.com/ashot978/Playlumi/actions/runs/46038701925) slaagde op telefoon en Fold met **56 native acceptatiegroepen**: 26 telefoongroepen, 24 Fold-groepen en drie bestaande quizgroepen per formaat. Dit omvat de nieuwe leerinhoud, gereedschap, Undo/Redo, bevestigd leegmaken, profielscheiding, grote tekst en opnieuw sluiten van de ouderpoort.

De werkelijke debug-update van exacte 0.109 naar 0.110 behield 28 voorkeuren/voortgangswaarden, 17 vraagvelden en de byte-identieke oude teken-PNG. De Android-rasterproef bevestigde dat gumwerk wit bleef en het ongemoeide deel dezelfde pixels hield na 32 verdere gebaren. Ook na leegmaken en nog 32 gebaren kwam oude inhoud niet terug. Negen gemeten knopregio's in echte screenshots bevatten hun gekleurde achtergrond en donkere tekst, ook bij 130% systeemtekst. De definitieve beelden zijn visueel beoordeeld.

Profielselectie was in deze versie een expliciet voorbereide testfixture; tekenen en bedienen gebeurde daarna via de echte UI. Deze proef claimt geen al aanwezige spelerwisselknop. De CI gebruikte uitsluitend tijdelijke debughandtekeningen en een niet ondertekende productie-APK; de oorspronkelijke privésleutel bleef buiten CI.

Een fysieke Samsung Fold is nog niet beschikbaar. De emulatorproeven bewijzen daarom de geteste configuraties en vervangen geen fysieke bediening, vouwen/uitvouwen of updateproef met de originele ondertekening op dat toestel. Een testbouw omvat geen merge, tag, openbare release of Play Store-publicatie.
