# Playlumi 0.39.0 — luisteren voor je speelt

## Nieuw
- Houd een van de zes geillustreerde spelbubbels vast om de naam van het spel voor te laten lezen, bijvoorbeeld 'Dit is Tellen. Tik om te spelen.'
- Gewoon kort tikken opent zoals voorheen onmiddellijk het gekozen spel.
- De tip wordt alleen afgespeeld als het bestaande geluid aanstaat; de ruststand en alle andere kindinstellingen blijven behouden.
- De tekst wordt ter plaatse aan Android-tekst-naar-spraak aangeboden. Geen accounts, advertenties, netwerkverkeer of nieuwe machtigingen.

## Compatibiliteit en verificatie
- Werkt voor alle zes spellen bij alle drie leeftijdsgroepen.
- Dezelfde opgeslagen voortgang, 300-vragendecks, kortespelrondes, ouderinstellingen en app-ID.
- Unit-tests voor 18 gesproken labels, ongeldige invoer en telefoon/tablet-indeling.
- Android-tests, lint, debugbuild en emulatorstart als CI-poort voor de unsigned release.
- Maak daarna buiten GitHub een APK met het bestaande privecertificaat, hogere versiecode en gecontroleerde v1/v2-ondertekening.
- Controle op een echt toestel blijft noodzakelijk; een emulatorstart dekt niet elk TalkBack- of spraakscenario.

Geen merge naar main en geen publicatie in winkels.
