# Playlumi 0.42.0 — antwoordkeuzes horen voordat je kiest

## Nieuw
- In leeropdrachten komt de knop **🔊 Hoor de antwoorden**, mits geluid aanstaat.
- Tik op de knop en Playlumi leest alle zichtbare antwoordkeuzes van linksboven naar rechtsonder voor.
- Houd een antwoord langer ingedrukt om alleen dat antwoord te horen. Gewoon tikken blijft een antwoord kiezen.
- Kinderen die nog niet goed kunnen lezen, kunnen eerst luisteren en daarna zelfstandig het juiste antwoord aanwijzen.
- Na **💡 Help mij** worden verdwenen antwoorden ook niet meer voorgelezen.
- Na een goed antwoord verdwijnt de luisterknop; de gewone volgende-vraagknop blijft beschikbaar.

## Privacy en veiligheid
- Alle spraak gebruikt de bestaande Android-voorleesfunctie. Er komen geen servers, accounts, trackers, abonnementen of permissies bij.
- Voorlezen geeft geen sterren, punten of badges; het verandert ook nooit het juiste antwoord, vraagvolgorde of moeilijkheid.
- De 300 kaarten per categorie, lokale profielen, tuin, outfits en ouderinstellingen blijven behouden.
- De audio-ui verschijnt alleen wanneer geluid is ingeschakeld. De bestaande offline instelling blijft leidend.

## Verificatie
- Unit-tests voor alle antwoordposities, zichtbare en verborgen opties, ongeldige antwoorden en de echte speltypes.
- GitHub Actions moet slagen voor unit-tests, lint, debugbuild, emulator-opstart en unsigned releasebuild.
- Privé ondertekenen buiten GitHub met exact het bestaande Playlumi-certificaat; controleer pakket, versiecode en v1/v2-handtekeningen.
- Een echte toesteltest van TTS, aanraken en TalkBack blijft aanvullend nodig.
- Geen merge naar main of store-publicatie.
