# Playlumi 0.109

Android version code: **601010**. Package: `nl.playlumi.app`.
Baseline: `06bf09abae45cea79143bee00924cecabb474bea` (0.108).

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

De buildworkflow bouwt debug en release, controleert de oorspronkelijke illustraties, draait alle unit-tests en Android Lint en stelt alleen de ongetekende productie-APK beschikbaar voor privé ondertekenen. De emulatorworkflow controleert telefoon- en Fold-indeling, een update van exact 0.108 naar 0.109, herstel van echte spelgegevens, speelideeën, antwoordmarkering, tekenen en de ouderpoort.

De uiteindelijke APK gebruikt de oorspronkelijke Playlumi-releasehandtekening. Pakket, versienummer, certificaat, ZIP-uitlijning en de relatie met de gebouwde broncode worden gecontroleerd. Emulatorresultaten gelden voor de geteste Android-configuraties; bediening op een fysiek Samsung Fold blijft een afzonderlijke controle. Er is geen merge, tag, GitHub Release of Play Store-publicatie onderdeel van deze bouw.
