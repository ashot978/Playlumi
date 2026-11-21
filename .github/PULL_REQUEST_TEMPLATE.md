## Wat is er veranderd?
Omschrijf precies één feature (of één bugfix) en het bijbehorende spelscherm.

## Controle
- [ ] Bestaand gedrag en lokale voortgang blijven intact
- [ ] Kind kan doorspelen zonder animatie af te wachten
- [ ] Minder beweging werkt (statische feedback)
- [ ] Grote en kleine aanraakvlakken / geen afgesneden knop
- [ ] `bash scripts/test-motion.sh` geslaagd
- [ ] `python3 scripts/check-repository.py` geslaagd
- [ ] Originele Android-app vanuit bron gebouwd
- [ ] Emulator: app start en hele spelronde zonder crash
- [ ] Samsung-update inclusief scores en profielen getest
- [ ] Zelfde app-ID en releasecertificaat
- [ ] Geen sleutels, kindgegevens of binaire APK in openbare PR

**Let op:** als Android-broncode nog ontbreekt, markeer de laatste vijf controles niet als voltooid. Maak de PR *draft* en lever geen APK als 'stabiel' op.
