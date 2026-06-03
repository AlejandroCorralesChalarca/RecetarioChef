# ChefApp - Sistema de Gestión para Restaurantes

ChefApp es una solución móvil nativa para Android diseñada para que chefs y administradores de restaurantes gestionen sus recetas, controlen el inventario en tiempo real y administren pedidos de mesa de manera eficiente.

## 🚀 Funcionalidades Principales

*   **Dashboard Premium**: Resumen visual de ventas, pedidos y sugerencias del día con un diseño moderno.
*   **Gestión de Recetas (CRUD)**: Creación de platos con ingredientes vinculados directamente al inventario.
*   **Control de Inventario**: Monitoreo de stock con alertas visuales de nivel bajo.
*   **Sistema de Pedidos**: Gestión de estados en tiempo real (Pendiente, En Preparación, Listo, Finalizado).
*   **Descuento de Insumos**: Al marcar un pedido como "Listo", la app descuenta automáticamente los ingredientes del inventario.
*   **Autenticación**: Sistema seguro con Firebase Auth (Login, Registro y Recuperación de Contraseña).

## 🛠️ Requisitos Técnicos y Arquitectura

*   **Lenguaje**: Kotlin.
*   **Arquitectura**: Implementación basada en capas (UI, ViewModel, Domain, Data) siguiendo los principios de Clean Architecture.
*   **Manejo de Estados**: StateFlow y LiveData para una UI reactiva.
*   **Base de Datos**: Firebase Firestore (NoSQL).
*   **Almacenamiento**: Firebase Storage para imágenes de recetas.
*   **Registro de Fallos**: Firebase Crashlytics para monitoreo de estabilidad.

## 📦 Instrucciones de Ejecución

1.  **Clonar el proyecto**:
    ```bash
    git clone https://github.com/tu-usuario/RecetarioChef.git
    ```
2.  **Configurar Firebase**:
    *   Registrar la app en [Firebase Console](https://console.firebase.google.com/).
    *   Habilitar los servicios de **Authentication**, **Firestore**, **Storage** y **Crashlytics**.
    *   Descargar el archivo `google-services.json` y colocarlo en la carpeta `app/`.
3.  **Compilar**: Abrir en Android Studio Jellyfish (o superior) y sincronizar Gradle.

## 👨‍💻 Autor
Natalia Velez Gutierrez
