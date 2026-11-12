# Schach-Mod (Fabric, Minecraft 1.21.11)

Ein Schachbrett aus Blöcken für zwei Spieler. Keine Regelprüfung, keine KI – ihr zieht selbst.

## Bauen
1. Java 21 installieren.
2. Gradle 9 installieren.
3. `gradle build` -> `build/libs/schachmod-1.0.0.jar`
4. Jar + Fabric API in den mods-Ordner legen.

## Spielen
- `/schach neu` Brett vor dir aufbauen
- Rechtsklick (leere Hand) auf eine Figur, dann Rechtsklick auf das Zielfeld
- `/schach zug e2 e4` Zug per Befehl
- `/schach zurueck` letzten Zug/Eingriff zurücknehmen
- `/schach entfernen e5` Figur entfernen
- `/schach umwandeln e8 dame` Bauernumwandlung
- `/schach neustart` Grundstellung
- `/schach loeschen` Brett abbauen
- `/chess ...` funktioniert als Alias.

Rochade = erst König ziehen, dann Turm ziehen.
