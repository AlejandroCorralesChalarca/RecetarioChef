# Registro de uso de Inteligencia Artificial - ChefApp

De acuerdo con el punto 15 de las especificaciones del proyecto final, se documenta el uso de herramientas de IA:

### Herramientas utilizadas
- Modelos de lenguaje para asistencia en codificación.

### Alcance de la asistencia
1. **Manejo de Estados:** Generación de la estructura de la Sealed Class `UiState` y su implementación en ViewModels.
2. **Refactorización MVVM:** Apoyo en la migración de lógica de manejo de errores desde los Fragments hacia los ViewModels para respetar la separación de responsabilidades.
3. **Documentación:** Asistencia en la redacción técnica de los archivos Markdown (decisiones.md y SRS).

### Validaciones realizadas por el equipo
- Se verificó manualmente la integración de Firebase para asegurar que los errores llegaran correctamente a Crashlytics.
- Se realizaron pruebas de regresión para asegurar que la lógica de búsqueda y filtrado de recetas no se viera afectada por la nueva estructura de estados.
- Se comprobó el ciclo de vida de las corrutinas para evitar fugas de memoria.