# Playlumi 0.109

Android version code: **601010**. Package: `nl.playlumi.app`.
Baseline: `06bf09abae45cea79143bee00924cecabb474bea` (0.108).
Geverifieerde bron: [c31400887005acd24c1d418eb6084f8156dbe9b5](https://github.com/ashot978/Playlumi/commit/c31400887005acd24c1d418eb6084f8156dbe9b5).
Status: **afgeronde testbouw**, met [concept-PR 93](https://github.com/ashot978/Playlumi/pull/93).

## Speelideeën die aansluiten op de opgeslagen voortgang

Vanuit de spellenbibliotheek en Lumi's stad is er een pagina met drie concrete speelkeuzes. Een geldige begonnen ronde komt eerst. Waar mogelijk volgen een eerder gespeelde oefening en een oefening zonder afgeronde ronde. De keuzes komen uitsluitend uit de catalogus voor de gekozen leeftijd; tekenen heeft geen rondeteller en verschijnt daarom niet als een onvoltooide oefenronde.

Een begonnen herhaling blijft een hervatbare ronde, ook als eerdere rondes van hetzelfde spel al afgerond zijn. Oudere voltooiingsmarkeringen blijven bruikbaar zonder daar een verzonnen aantal rondes aan te koppelen. Andere ideeën kiezen verandert uitsluitend een lokale variatieteller. Alleen de pagina openen of verversen verdient geen sterren en verandert geen scores.

De speelknop opent het bestaande spel of hervat de opgeslagen ronde. Terug en de eindknop keren terug naar de speelideeën. Dit werkt voor quizzen, interactieve borden, muziek, minispellen, bellen en sterren vangen. Alle oefeningen blijven opnieuw speelbaar.

## Antwoordfeedback die ook zonder kleur herkenbaar is

Het goede antwoord krijgt een zichtbaar vinkje, de native geselecteerde status en de toegankelijkheidstekst ‘goed gekozen’. De andere antwoorden blijven leesbaar met minder nadruk. Na herstel van een beantwoorde vraag keert dezelfde markering terug zonder opnieuw sterren toe te kennen. De oorspronkelijke antwoordtekst en opgeslagen vraag veranderen niet. Er worden geen quizhint- of spraakknoppen toegevoegd.

## Duidelijker inzicht voor ouders

Het ouderoverzicht toont per speelwereld het werkelijke aantal getelde rondes, het aantal oefeningen met een afgeronde ronde en de begonnen rondes die verder gespeeld kunnen worden. Het overzicht hoort bij de gekozen leeftijd en bestaande lokale profielopslag. Rondetellers worden sinds 0.107 bijgehouden; oudere afgeronde oefeningen krijgen geen geschat historisch aantal rondes. Bestaande informatie over recente eerste antwoorden blijft zichtbaar.

De ouderzone wordt opnieuw vergrendeld wanneer de app naar de achtergrond gaat of de Activity opnieuw wordt gemaakt, bijvoorbeeld na een formaatwissel. Navigeren binnen een geopende ouderzone blijft mogelijk. De toegang is een eenvoudige ouderpoort op het toestel, geen account of persoonsverificatie.

## Betrouwbaarder tekenen

Ook het huisje in de kop bewaart nu de actuele tekening voordat het tekenblad wordt verlaten. Automatisch bewaren levert geen extra beloning op. Bij een mislukte opslag blijft het actieve tekenblad open met een melding om het opnieuw te proberen.

Het PNG-bestand wordt met Android AtomicFile geschreven; de oude afbeelding wordt niet meer eerst weggegooid. De gewijzigde PNG wordt na het schrijven gecontroleerd. Niet-gewijzigd werk hoeft niet opnieuw gecodeerd te worden. Bewaren houdt de lijnen van het huidige tekenblad in het geheugen, zodat ‘Laatste lijn weg’ ook na bewaren of een korte onderbreking blijft werken. Na opnieuw openen is de opgeslagen afbeelding, zoals voorheen, één geheel; afzonderlijke oudere lijnen kunnen dan niet worden teruggedraaid.

De eenmalige eerste-bewaarbonus, de bijbehorende markering, speelgegevens en wereldvoortgang worden samen bijgewerkt nadat opslaan is gelukt. De bestaande bonus blijft negen speelsterren.

## Lumi en de tekenprestaties

De sterrenoutfit, regenboogtrui en astronaut hebben een gouden opening rond Lumi's gezicht. De glimlach blijft binnen de stervorm. Strik en sjaal blijven herkenbare accessoires. Bestaande kledingcodes, eigendom en gekozen accessoires blijven behouden.

Avatarpaden en gradients worden hergebruikt. De stad gebruikt een begrensde cache voor identieke gebouwgradients; achtergronden worden bij een gewijzigde grootte opnieuw gemaakt. De native knoppen boven het geïllustreerde dorp bewaren hun vaste bronrechthoeken en worden in de meetfase gemeten. Dit verwijdert terugkerende tekenallocaties; er wordt geen gemeten FPS-winst geclaimd.

## Verificatie en grenzen

De Java-controle omvat onder meer 3.422 speelgeschiedenissen en variaties, 10.266 leeftijdspassende speelideeën en 20.538 wereldsamenvattingen. Grensgevallen omvatten corrupte of onbekende routecodes, oudere voltooiing zonder rondetelling, een begonnen herhaling, alle routes afgerond, lege geschiedenis en zeer grote tellers. De bestaande leerstof- en herstelcontroles blijven verplicht.

De [Android-run 46014835510](https://github.com/ashot978/Playlumi/actions/runs/46014835510) is geslaagd: debug en release gebouwd, oorspronkelijke illustraties gecontroleerd, **230 unit-tests in 53 suites geslaagd** en Android Lint met **0 fouten en 168 waarschuwingen**. De bestaande waarschuwingen zijn niet onderdrukt en worden niet als afgerond kwaliteitswerk voorgesteld.

De [UI-run 46014835542](https://github.com/ashot978/Playlumi/actions/runs/46014835542) is op telefoon en Fold geslaagd: **43 checks**, opgebouwd uit 19 telefoon- en 18 Fold-checks voor stad, update en lifecycle, plus drie bestaande quizchecks per formaat. De update van exact 0.108 naar 0.109 gebruikt de tijdelijke debughandtekening van dezelfde runner en vergelijkt 28 voorkeuren/voortgangswaarden en 17 vraagvelden. Speelideeën en het ouderoverzicht gebruiken in de proef werkelijk gespeelde rondes. Procesherstel wordt met een daadwerkelijk beëindigd achtergrondproces en een nieuwe PID gecontroleerd. Teken-PNG's na heropenen en Undo zijn bytegelijk aan de eerder bewaarde lijn; de negensterrenbonus blijft eenmalig.

De echte screenshots omvatten de drie soorten speelideeën, grote tekst, antwoordmarkering, pauze, ouderpoort, tekenen, stadskaart en vijf kledingstukken. Bekende afwerkingspunten zijn het lage contrast van de Tekenstudio-feedback, een onhandige titelafbreking op de telefoon en enkelvoudstekst in het ouderoverzicht. Zij blokkeerden de functionele acceptatie niet en vormen gericht vervolgwerk voor 0.110.

De opgeleverde APK is privé ondertekend met de oorspronkelijke Playlumi-releasehandtekening. Pakket, versienummer, certificaat, ZIP-uitlijning en de relatie met de gebouwde broncode zijn gecontroleerd. SHA-256 van deze test-APK:

```text
85e6ebcbea21f4a00d5a3ab7efab3ad365f354d0737ceccb3f97aaa8dd83fcc9
```

Emulatorresultaten gelden voor de geteste Android-configuraties; bediening en een oorspronkelijk ondertekende update op een fysiek Samsung Fold blijven afzonderlijke, nog niet uitgevoerde controles. Er is geen merge, tag, GitHub Release of Play Store-publicatie onderdeel van deze bouw.
