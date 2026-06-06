

# RecetarioChef (ChefApp)

Aplicación Android (MVVM) para gestión de recetas, inventario y pedidos.

## Descripción

RecetarioChef es una aplicación Android orientada a cocineros y pequeños negocios de comida. Permite gestionar recetas, inventario, pedidos y perfiles de usuario. El proyecto usa Kotlin, Android Jetpack (ViewBinding, Lifecycle, Navigation) y Firebase para autenticación, Firestore, Storage y Crashlytics.

## Características principales

- Gestión de recetas (crear, editar, ver detalles)
- Inventario de ingredientes
- Gestión de pedidos
- Autenticación con Firebase
- Arquitectura MVVM y navegación con componentes Jetpack

## Requisitos

- JDK 11
- Android Studio (recomendado) o línea de comandos con Gradle
- Android SDK (API 26+)

## Instalación y ejecución

1. Clona el repositorio:

```bash
git clone https://<tu-repo>/RecetarioChef.git
cd RecetarioChef
```

2. Abrir con Android Studio: File → Open → selecciona la carpeta raíz del proyecto.

3. Desde la línea de comandos (Windows):

```bash
./gradlew assembleDebug
./gradlew installDebug
# En Windows usa gradlew.bat si no tienes entorno Unix
```

4. En Android Studio: sincroniza Gradle y ejecuta la app en un emulador o dispositivo.

## Estructura importante del proyecto

- **Módulo app:** configuración y dependencias principales en [app/build.gradle.kts](app/build.gradle.kts#L1)
- Archivo de configuración del proyecto: [settings.gradle.kts](settings.gradle.kts#L1)
- Manifiesto de Android: [app/src/main/AndroidManifest.xml](app/src/main/AndroidManifest.xml#L1)
- Manager de autenticación (Firebase): [app/src/main/java/com/example/chefapp/auth/AuthManager.kt](app/src/main/java/com/example/chefapp/auth/AuthManager.kt#L1)
- Documentación adicional: [uso_ia.md](uso_ia.md#L1)

## Dependencias destacadas

- Firebase (Auth, Firestore, Storage, Crashlytics)
- AndroidX (core-ktx, appcompat, lifecycle, navigation)
- Glide, MPAndroidChart

Las versiones están centralizadas en `gradle/libs.versions.toml` y las referencias en los archivos Gradle.

## Desarrollo

- Habilita `viewBinding` en el módulo app (ya configurado).
- Sigue la arquitectura MVVM: separa UI, ViewModels, Repositorios y fuentes de datos (Firestore/Storage).

## Tests

Se incluyen dependencias para pruebas unitarias y de instrumentación. Ejecuta:

```bash
./gradlew test
./gradlew connectedAndroidTest
```

## Contribuir

1. Haz un fork y crea una rama con una descripción clara del cambio.
2. Abre un Pull Request con información sobre la funcionalidad y pasos para reproducir.

## Notas sobre despliegue

- Para subir a Google Play, firma el APK/AAB con una keystore y configura los parámetros de versión en `app/build.gradle.kts`.

## Licencia

Este repositorio no incluye un archivo de licencia explícito. Consulta con el propietario del proyecto antes de reutilizar el código.

---

Prototipo y maquetacion funcional: https://recipe-chef-kitchen.lovable.app. 

