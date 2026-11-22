# Playlumi 0.51.0 — Echte ritmes maken

## Nieuwe functies
- **🤫 Rust:** kinderen kunnen in hun eigen deuntje een stille tel toevoegen. Daardoor krijgen zelfgemaakte melodieën meer ritme en afwisseling.
- **🎲 Probeer een ritme-idee:** vier speelse patronen van vier stappen, met een pauze ertussen. Iedere tik voegt het volgende patroon toe aan het gekozen liedje.
- Als er minder dan vier plekken beschikbaar zijn, blijft het liedje volledig ongewijzigd en verschijnt een vriendelijke melding.
- De 12-stappenlimiet en drie afzonderlijke opgeslagen liedjes blijven bestaan. Oude deuntjes uit 0.49/0.50 blijven exact geldig en blijven bewaard.
- De stille tel wordt weergegeven met 🤫, zowel tijdens componeren als tijdens afspelen. Het geluid stopt dan even zodat er echt een pauze klinkt.
- Ritme-ideeën worden niet automatisch afgespeeld: de bestaande afspeelknop blijft de bewuste startactie.

## Vertrouwd en veilig
- Alle bestaande muzikale geluidseffecten en originele deuntjes blijven.
- Geen menselijke voorleesstem, microfoon, nieuwe rechten, online accounts, advertenties of downloads.
- Kindprofielen, scores, badges, ouderbeveiliging en spelvoortgang worden niet gewijzigd.

## Verificatie
Extra Java-unit-tests voor compatibel opslaan, stille rustmomenten, unieke ritmepatronen, vullen tot twaalf stappen en het voorkomen van onbedoeld overschrijven. GitHub Actions moet de tests, Android-lint, debugbuild, emulatorstart en unsigned releasebuild laten slagen. De APK wordt alleen buiten GitHub met het bestaande Playlumi-certificaat ondertekend.
