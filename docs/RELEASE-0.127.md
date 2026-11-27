# Playlumi 0.127 — eenvoudiger spelen

## Opdracht en status

De gebruiker vroeg de hele app eenvoudiger te maken en gaf voor deze ronde expliciet aan dat testen niet nodig is. Deze versie concentreert de bediening op spellen kiezen en spelen, tekenen en bouwen. Dit is een vereenvoudiging van de bestaande app; er wordt geen nieuw spel toegevoegd.

- Versienaam: **0.127**.
- VersionCode: **601028**.
- Android-pakket: `nl.playlumi.app`.
- Bronbranch: `feature/playlumi-0.127`.
- Voorafgaande versie: **0.126 / 601027**.
- Status bij voorbereiding: bronwijzigingen in uitvoering; APK-build, privé ondertekening en oplevering volgen. Gebruikerstest staat open.

## Wijzigingen

- Eén eenvoudige ingang naar de zes speelwerelden.
- Spellen per wereld in maximaal drie herkenbare spelgroepen, zodat een kind eerst een kleine keuze maakt.
- Grote speelkaarten en korte teksten; horizontaal bladeren blijft mogelijk en er komen geen verticale scrolschermen.
- De kinderbediening richt zich op het spel zelf. Stadsgroei, sterrenpotten, winkels, verzamelingen, prestaties, dagopdrachten en hun zijmenu's verdwijnen uit de hoofdervaring.
- De bestaande spellen, waaronder Dierensudoku, blijven beschikbaar. Tekenen en bouwen blijven creatieve activiteiten.
- Afrondschermen bieden een korte bevestiging en een directe keuze om opnieuw te spelen of een ander spel te kiezen.
- Oudermogelijkheden blijven achter de ouderpoort.

## Bewaarde gegevens

De bestaande spelroutes en opslagkeys blijven gehandhaafd. Begonnen rondes, tekeningen, bouwontwerpen, voortgang en eerder verdiende of gekochte onderdelen worden niet gewist wanneer hun oude menu verdwijnt. Oude schermverwijzingen worden naar de vereenvoudigde bediening geleid.

Dit beschrijft het ontwerp en de bronwijzigingen. Voor 0.127 wordt op verzoek geen upgradeproef uitgevoerd.

## Bouwen en opleveren

De aparte workflow [android-0127-build.yml](../.github/workflows/android-0127-build.yml) bouwt alleen de productie-APK met:

```sh
PLAYLUMI_BUILD_CODE=601028 gradle --no-daemon -PplaylumiPackagingOnly=true assembleRelease
```

De expliciete projectoptie schakelt voor deze opdracht ook Androids impliciete release-lint uit via `lint.checkReleaseBuilds`. Zonder deze optie blijft die controle standaard ingeschakeld. Daarna controleert de workflow de pakketidentiteit, versie en ingesloten oorspronkelijke illustraties en berekent zij de bestandschecksum. Dit zijn verpakkingsstappen voor de gevraagde APK.

De workflow levert:

- `playlumi-0127-unsigned-release`: de ongetekende productie-APK, pakketgegevens, checksum en de vermelde uitvoeringsscope.
- `playlumi-0127-offline-signing-tools`: de officiële Android-tools om de APK privé met de bestaande sleutel te ondertekenen.

De bestaande Android-verificatieworkflow slaat uitsluitend pushes op `feature/playlumi-0.127` over. Haar test- en lintstappen blijven intact en handmatig beschikbaar. De bestaande native workflow blijft op haar eigen branch of via handmatige uitvoering beschikbaar.

Voor deze versie worden **geen unit-tests, onafhankelijke Java-controles, lint, emulators, native acceptatie of fysieke toestelproeven uitgevoerd**. Er wordt dus geen geslaagde teststatus voor 0.127 geclaimd. De APK wordt na compilatie en privé ondertekening aan de gebruiker aangeboden om zelf te beoordelen.

De oorspronkelijke sleutel blijft buiten GitHub en CI. De permanente certificaatvingerafdruk is:

`d2b74f018bf6111382f901c2acdc03e787811ce207d6c4435fcca9559ae9b0ef`

## Oplevergegevens

De definitieve broncommit, buildrun, APK-checksum en ondertekeningsresultaten worden ingevuld zodra de APK is gemaakt. Tot dat moment zijn dit voorbereide opleverinstructies.
