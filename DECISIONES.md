Registro de Decisiones Técnicas y Verificación - ChefApp

I. Decisiones Técnicas (Auditoría MVVM)

1. Migración a SSOT (Single Source of Truth) con StateFlow
Decisión: Se centralizó el estado de cada pantalla en un objeto `UiState` gestionado por `StateFlow` en el ViewModel.
Justificación:** Esto garantiza que la Vista sea una representación pura del estado del ViewModel. Resuelve la **Pregunta 1** de auditoría al eliminar variables mutables en la Vista y facilita la depuración al tener un único flujo de datos predecible.

2. Implementación de repeatOnLifecycle para Corrutinas
Decisión: Se utilizó el bloque `viewLifecycleOwner.lifecycleScope.launch` con `repeatOnLifecycle(Lifecycle.State.STARTED)` para la recolección de estados.
Justificación: Es la forma más segura de consumir flujos en Android. Evita fugas de memoria (Memory Leaks) y asegura que la App no gaste recursos recolectando datos cuando no es visible, resolviendo la Pregunta 2 sobre la sobrevivencia a la rotación y seguridad del ciclo de vida.

3. Control de Concurrencia mediante UI Throttling
Decisión: Se añadió un estado de carga (`isActionInProgress` / `isLoading`) vinculado a la propiedad `isEnabled` de los botones.
Justificación: Para resolver la Pregunta 3 de auditoría, se bloquea la interacción del usuario inmediatamente después del primer clic en acciones críticas. Esto evita condiciones de carrera y la creación de datos duplicados.

II. Verificación Documentada 

A. Pantallas con Datos e Interfaz
Acceso: Pantallas de Login y Registro funcionales con validación. 
Dashboard: Visualización de estadísticas, ingresos y gráfica de pedidos semanales. 
Gestión: Listados de Pedidos, Inventario y Recetas con datos cargados y diseño unificado. Perfil:** Información del Chef Natalia con el botón "+" ubicado consistentemente en la esquina derecha.

B. Rotación de Dispositivo 
Las capturas en modo horizontal demuestran que:
Persistencia de Filtros: Las búsquedas (ej: "ajo", "tiramisú") y los filtros de categorías (ej: "Italiana", "Aves") no se borran al girar el celular.
Estado de Diálogos: Los formularios de entrada permanecen abiertos y accesibles tras la rotación gracias a la gestión de estado en el `MainViewModel`.
Adaptabilidad: El buscador en Recetas se desplaza correctamente dentro del Scroll, permitiendo su uso en pantallas horizontales estrechas.

C. Comportamiento Doble Clic 
Se verificó que al pulsar "Iniciar Preparación" en Pedidos o "Guardar" en los diálogos, el botón se deshabilita visualmente (propiedad `isEnabled = false`) hasta que el ViewModel confirma el fin del proceso, cumpliendo con la protección de concurrencia.

III. Manejo de Estados de Interfaz y Errores 

1. Centralización de Errores con UiState (Sealed Class)
Decisión: Se expandió el uso de `UiState` a una Sealed Class global que contempla los 6 estados obligatorios: Loading, Success, Error, Empty, NoConnection y SessionExpired.
Justificación: Esto permite que cada Fragment reaccione de forma atómica. Por ejemplo, si el `RecetasViewModel` detecta una lista de tamaño cero, emite `UiState.Empty`, lo que activa automáticamente el layout de "No hay recetas" en la UI, eliminando la necesidad de lógica condicional compleja en el Fragment.

2. Registro de Fallos no fatales (Observabilidad)
Decisión: Integración de `CrashReporter` (Firebase Crashlytics) dentro de los bloques `catch` de los ViewModels.
Justificación: Cumpliendo con el Punto 10 de la guía, cada error capturado (especialmente fallos de red o de parseo de datos) se reporta a la consola de Firebase. Esto permite monitorear la salud de la App en producción sin que el usuario experimente un cierre forzado (Crash).

IV. Gestión de Seguridad y Sesión 

1. Validación de Sesión Activa
Decisión: Implementación del estado `SessionExpired` mediante la verificación constante de `auth.currentUser`.
Justificación: Si Firebase Auth invalida el token o el usuario es eliminado de la consola, el ViewModel detecta la nulidad del objeto User y emite el estado de expiración, provocando que el Fragment ejecute un `Intent` con `FLAG_ACTIVITY_CLEAR_TASK` para expulsar al usuario al Login, garantizando la seguridad de los datos.