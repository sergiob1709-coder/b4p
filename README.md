# Battle4Play RSS Android App

Esta app de ejemplo en Android (Jetpack Compose) consume el sitemap de **Battle4Play** y muestra las noticias en tarjetas con imagen.

## Sitemap usado

- `https://www.battle4play.com/post-sitemap3.xml`

> Nota: el servidor puede devolver **403** a algunos clientes no-navegador. La app envía un `User-Agent` de navegador, pero si el feed está protegido, puede requerir ajustes adicionales en el servidor.

## Funcionalidades

- Paginación de 6 noticias por página.
- Tarjetas con imagen, título y resumen.
- Al pulsar una tarjeta se muestra el detalle completo en la misma pantalla.
- Botón para abrir la noticia en el navegador.

## Cómo abrir en Android Studio

1. Abre Android Studio.
2. Selecciona **Open** y elige la carpeta del proyecto.
3. Espera a que Gradle sincronice dependencias.
4. Ejecuta la app en un emulador o dispositivo físico.

## Solución si Gradle no sincroniza

- Si tu red bloquea `https://services.gradle.org`, configura el proxy en Android Studio (**Settings > Appearance & Behavior > System Settings > HTTP Proxy**) o usa una red sin restricciones.
- Si no puedes exportar binarios, este repo no incluye `gradle-wrapper.jar`. En ese caso instala Gradle localmente (8.5 o superior) y ejecuta `gradle build` desde la carpeta del proyecto (el script `gradlew` detecta si existe `gradle` en el PATH).

## Requisitos

- Android Studio Iguana o superior.
- SDK 34.
