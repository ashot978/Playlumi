# Animaties integreren in de bestaande Playlumi-app

De huidige `motion-core` is platformonafhankelijk en bevat **alleen wiskundige bewegingen**. Niet in productie inzetten voordat de originele Android-projectbroncode is hersteld. De bestaande `MainActivity` en spelregels mogen niet op basis van deze documentatie worden gereconstrueerd of overschreven.

Wanneer de bestaande broncode beschikbaar is:
1. Voeg `MotionEffects.java` toe aan de Java-source set of een Android-module.
2. Trigger pas bij een aantoonbare bestaande gebeurtenis (correct antwoord, passende vorm losgelaten, 8 vragen voltooid); nooit tijdens antwoordberekening.
3. Sla starttijd lokaal in de huidige UI/renderlaag op. Vraag per frame `MotionEffects.sample(kind, elapsedMillis, reduceMotion)` op.
4. Teken de bijbehorende originele ster/vorm/mascotte op `Frame.x/y` in het beschikbare tekenvlak en pas schaal, draaihoek, transparantie toe. `Frame.accent` stuurt het fonkel- of highlightpercentage; geen nieuwe assets vereist.
5. Is `Frame.finished` waar? Stop het frameverzoek. Animatie mag geen extra timer of achtergrondproces houden.
6. Gebruik de huidige toegankelijkheidsinstelling `minder beweging` om de statische `reduceMotion=true` variant te tonen.
7. Alle UI-updates op de hoofdthread; geen wijziging van spelregels, persistentie of navigatie.
8. Test op emulator met crashlog en minimaal twee schermformaten, daarna op het doeltoestel.

`MotionEffects` kent geen scores, namen, netwerk, accounts, Android-permissies of opgeslagen voortgang.
