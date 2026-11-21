# Playlumi — animatiebacklog

Eerstvolgende fase: **veilige animaties**, niet nóg een bytecode-patch. De animatiecurves zijn een aparte, testbare module; ze zijn **niet geïntegreerd in de bestaande Android-APK** zolang de broncode ontbreekt.

| Nr | Animatie | Waar | Beweging | Implementatie | Appstatus |
|---|---|---|---|---|---|
| A01 | Sterrenspoor | Goed antwoord | Ster vliegt op boog naar sterrenteller; fonkelspoor | Motion core: STAR_TRAIL | Nog niet geïntegreerd |
| A02 | Vormenmagneet | Sleep de vormen | Vorm veert zacht in het huisje | Motion core: SHAPE_MAGNET | Nog niet geïntegreerd |
| A03 | Mascotte-high-five | Reeks goede antwoorden | Korte sprong met handje omhoog | Motion core: MASCOT_HIGH_FIVE | Nog niet geïntegreerd |
| A04 | Magische groeituin | Einde speelronde | Plantje krijgt blaadje / bloem | Motion core: GARDEN_BLOOM | Nog niet geïntegreerd |
| A05 | Regenboogbrug | Level voltooid | Glijdende regenboog over podium | Motion core: RAINBOW_BRIDGE | Nog niet geïntegreerd |
| A06 | Schatkist | Trofee vrijgespeeld | Kist springt open, ster komt omhoog | Motion core: TREASURE_CHEST | Nog niet geïntegreerd |
| A07 | Memory-flip | Memory | Kaart klapt in twee fasen om, gezicht wisselt halverwege | Motion core: MEMORY_FLIP | Nog niet geïntegreerd |
| A08 | Raketlancering | Speciale mijlpaal | Raket stijgt op met zacht stofspoor | Motion core: ROCKET_LAUNCH | Nog niet geïntegreerd |
| A09 | Kiekeboe-vriendje | In eigen kamer | Vriendje piept van achter een meubel | Concept | Niet gestart |
| A10 | Kleurenwaterval | Kleurspel goed | Kleur stroomt als lint in passend vak | Concept | Niet gestart |
| A11 | Letterballon | Letterspel goed | Letter groeit als luchtballon | Concept | Niet gestart |
| A12 | Zachte sterrennacht | Rustig thema | Trage twinkels, 1 tegelijk | Concept | Niet gestart |

## Regels voor elke animatie
- Beweging duurt normaal 0,45–1,2 s; automatisch ophouden.
- Geen gedwongen pauze, geen flitsen, geen stroboscopen, geen knoppen die verplaatsen terwijl je erop drukt.
- Kind kan direct verder spelen: animaties mogen input, scoring of voortgang niet blokkeren.
- Tikfeedback binnen 100 ms; bevestiging mag naast animatie via tekst/vorm/symbool.
- Per gebeurtenis maximaal één hoofdeffect en één subtiel neveneffect.
- Schakel volledig naar een stilstaand eindbeeld bij 'minder beweging'.
- Blijf offline, zonder reclame, aankopen, accounts, trackers of nieuwe rechten.

## Eerste integratievolgorde
1. A02 Vormenmagneet — direct nuttig voor het bestaande sleep-spel.
2. A01 Sterrenspoor — duidelijkere beloning.
3. A03 Mascotte-high-five — variatie zonder extra scherm.
4. A04 Magische groeituin — pas daarna opslag van decoraties testen.

Release van een nieuwe APK is geblokkeerd tot Android-broncode, emulator-opstarttest en een toesteltest beschikbaar zijn.
