# Playlumi 0.27.0 — A03 Mascotte-high-five

## Voor kinderen
- Na drie goede antwoorden achter elkaar doet de stervriend een korte high-five.
- De stervriend maakt een klein sprongetje, twee handjes komen naar elkaar toe
  en vijf kleine lichtpuntjes verschijnen kort.
- De 0,68 s animatie loopt vanzelf af, onderbreekt geen antwoord en blokkeert
  geen aanrakingen.
- Na nog vijf goede antwoorden (8, 13, ...) kan het terugkomen, behalve als
  de laatste vraag van de sessie al een grotere viering krijgt.
- Bij een gewone goede beurt blijft de vliegende ster uit Playlumi 0.26.0 bestaan.
- De sterrenbalk en score worden altijd direct bijgewerkt. Geen extra beloningen,
  kosten, advertenties of netwerkverkeer.

## Wat is aangepast
- HighFiveMotion.java: zuivere Java-animatieberekening plus triggerregel
- HighFiveMotionTest.java: testen voor uitkomsten, grenzen, timing en minder beweging
- PlaylumiHighFiveView.java: 680ms niet-interactief, zichzelf opruimend Canvas-effect
- PlaylumiCharacterView.java: herkenbare high-five-pose van bestaande mascotte
- MainActivity.java: kiest precies een effect bij een geslaagde vraag
- app/build.gradle: versionName 0.27.0; automatisch hoger versionCode

## Regressie- en releasebeleid
- De originele Android-code (Playlumi 0.26.0) is de basis, NIET een aangepaste DEX.
- app-ID blijft nl.playlumi.app; release ondertekend met onveranderde privésleutel.
- Bestaande scores, profielen en opgeslagen voortgang blijven onaangeraakt.
- GitHub Actions moet Android unit-tests, lint, debugbuild, emulator-opstarttest,
  releasebuild en unsigned artifact succesvol afronden.
- Ondertekening en v2 digest worden lokaal geverifieerd. Een echte Samsung-test
  blijft nodig voor volledig bewezen compatibiliteit.
- Elke afgeronde app-update moet ook een direct downloadbare APK opleveren.
