# Playlumi 0.46.0 — Een andere vraag kiezen

## Nieuw
- Bij een nog niet opgeloste leervraag staat nu een rustige knop **🍃 Andere vraag**.
- Een kind kan een lastige vraag overslaan en direct een andere vraag proberen.
- Overslaan telt **niet** als goed of fout antwoord: er komen geen sterren, badges of punten bij en er wordt niets afgetrokken.
- Een ronde bestaat nog altijd uit acht **goed beantwoorde** opdrachten. Overslaan verkort deze ronde niet.
- Zodra een vraag goed is beantwoord, verdwijnt de knop en blijft **Volgende!** de normale actie.
- Na overslaan zijn eventuele weggehaalde opties van **💡 Help mij** gereset.
- De volgende vraag komt uit de reeds bestaande geschudde offline kaartenstapel.

## Geluid en privacy
- Er wordt niets automatisch voorgelezen bij het overslaan, openen of vervangen van een vraag.
- Alleen een bewuste tik op een 🔊-knop kan de Nederlandse stem laten spreken.
- Bestaande lokale opgeslagen kindprofielen, scores, outfits, tuin, ouderinstellingen en app-ID blijven hetzelfde.
- Geen accounts, netwerkgebruik, abonnementen, advertenties of nieuwe Android-machtigingen.

## Kwaliteitscontroles
- Nieuwe unit-tests controleren verse vragen, alle leeftijden, meerdere leercategorieën, ongewijzigde scores/badges en het niet overslaan van al goed beantwoorde vragen.
- GitHub Actions moet unit-tests, lint, debugbuild, emulatorstart en een release-APK opleveren.
- APK wordt uitsluitend privé ondertekend met het oorspronkelijke Playlumi-certificaat, gevolgd door controle van app-ID, versiecode en handtekening.
- Een echte toesteltest met grote fonts en kindbediening blijft belangrijk.
