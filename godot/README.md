# Godot-verkenning

Dit afzonderlijke prototype is de eerste stap naar een Godot-versie van Playlumi.
Het bevat alleen een telspel met aantallen 1 tot en met 3 en vervangt de Android-app niet.
Open deze map met Godot 4.3 en start de hoofdscene.

De bestaande spellen, profielen, instellingen, favorieten en voortgang zijn nog niet
overgezet. Er is geen migratie van Android SharedPreferences en geen APK-export.
Gebruik dit prototype niet als update van de bestaande installatie. Verwijder de
bestaande app niet: hierdoor kunnen lokale gegevens verloren gaan.

De bestaande Java-regels voor het telspel zijn de referentie voor de onafhankelijke
Godot-spelregels. Tests draaien met:
`godot --headless --path godot --script res://tests/counting_test.gd`.
