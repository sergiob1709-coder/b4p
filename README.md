# Battle4Play RSS Android App

Esta app de ejemplo en Android (Jetpack Compose) consume el RSS de **Battle4Play** y muestra las noticias en tarjetas con imagen.

## RSS detectado

- `https://www.battle4play.com/feed/`

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

## Requisitos

- Android Studio Iguana o superior.
- SDK 34.
