# Android Spielesammlung

Eine native Android-App, in der jedes neue Spiel einen eigenen Eintrag auf dem
Startbildschirm erhält. Den Anfang macht **Snake** – inklusive Wischsteuerung,
Punktestand, Pausenfunktion und Neustart.

## App installieren

1. Öffne in GitHub den Tab **Actions**.
2. Wähle den neuesten erfolgreichen Lauf von **Build Android APK**.
3. Lade das Artefakt `android-games-debug-apk` herunter und entpacke es.
4. Übertrage `app-debug.apk` auf dein Android-Gerät und öffne die Datei.

Android fragt beim ersten Mal möglicherweise, ob Apps aus dieser Quelle
installiert werden dürfen. Das Debug-APK ist für Tests gedacht und wird von
GitHub Actions erzeugt.

## Lokal bauen

Voraussetzungen sind JDK 17, Gradle 8.9 und ein Android SDK mit API 35. Der
Gradle Wrapper wird absichtlich nicht eingecheckt, da dessen JAR-Datei binär
ist und Binärdateien im Pull Request nicht unterstützt werden.

```bash
gradle assembleDebug
```

Das APK liegt anschließend unter `app/build/outputs/apk/debug/app-debug.apk`.

## Spielen

Wähle auf dem Startbildschirm **Snake** aus. Wische auf dem Spielfeld in die
gewünschte Richtung. Über den Button oben rechts lässt sich das Spiel pausieren
und fortsetzen. Nach einer Kollision startet **Nochmal spielen** eine neue Runde.
