Registro de Decisiones Técnicas - ChefApp

1. Migración a SSOT (Single Source of Truth) con StateFlow
Decisión: Se centralizó el estado de cada pantalla en un objeto `UiState` gestionado por `StateFlow` en el ViewModel.
Justificación: Esto garantiza que la Vista sea una representación pura del estado del ViewModel. Resuelve la Pregunta 1 de auditoría al eliminar variables mutables en la Vista y facilita la depuración al tener un único flujo de datos predecible.

2. Implementación de repeatOnLifecycle para Corrutinas 
Decisión:Se utilizó el bloque `viewLifecycleOwner.lifecycleScope.launch` con `repeatOnLifecycle(Lifecycle.State.STARTED)` para la recolección de estados.
Justificación: Es la forma más segura de consumir flujos en Android. Evita fugas de memoria (Memory Leaks) y asegura que la App no gaste recursos recolectando datos cuando no es visible, resolviendo la Pregunta 2 sobre la sobrevivencia a la rotación y seguridad del ciclo de vida.

3. Control de Concurrencia mediante UI Throttling
Decisión: Se añadió un estado de carga (`isActionInProgress` / `isLoading`) vinculado a la propiedad `isEnabled` de los botones.
Justificación: Para resolver la Pregunta 3 de auditoría, se bloquea la interacción del usuario inmediatamente después del primer clic en acciones críticas. Esto evita condiciones de carrera y la creación de datos duplicados en la base de datos o peticiones de red innecesarias.
