# FocusUp - Plataforma de Gestión Académica (Kotlin Multiplatform + Ktor)

## 1. Definición del Problema y Objetivos

### Problema
La falta de herramientas centralizadas y multiplataforma para la gestión de roles en entornos educativos dificulta la comunicación fluida entre administradores, docentes y estudiantes.

### Objetivos del Proyecto
* **General:** Desarrollar una aplicación multiplataforma (Android/Desktop) conectada a un servicio REST en Ktor para autenticar y canalizar usuarios según su perfil.
* **Específicos:**
  1. Implementar un backend en Ktor con almacenamiento dinámico en formato JSON (`usuarios.json`).
  2. Diseñar un cliente compartido con Compose Multiplatform y Ktor Client.
  3. Garantizar el control de acceso basado en roles (`ADMINISTRADOR`, `DOCENTE`, `ESTUDIANTE`).

---

## 2. Arquitectura del Proyecto

El proyecto está estructurado en módulos independientes siguiendo las mejores prácticas de KMP:
* `backend-ktor`: Servidor REST con motor Netty y serialización Kotlinx Json.
* `shared`: Lógica de negocio compartida (Modelos, Servicio de Autenticación HTTP y Pantallas Compose).
* `androidApp`: Punto de entrada y configuración nativa para dispositivos Android.
* `desktopApp`: Ejecutable para aplicaciones de escritorio (JVM).
* `iosApp`: Punto de entrada para iOS.

---

## 3. Credenciales de Prueba

Para validar el funcionamiento del sistema, utiliza los siguientes usuarios preconfigurados:

| Rol | Correo Electrónico | Contraseña |
| :--- | :--- | :--- |
| **Administrador** | `admin@focusup.com` | `admin123` |
| **Docente** | `docente@focusup.com` | `docente123` |
| **Estudiante** | `estudiante@focusup.com` | `estudiante123` |

---

## 4. Bitácora de Pruebas y Corrección de Errores

| Error Detectado | Causa Raíz | Solución Aplicada |
| :--- | :--- | :--- |
| `Serializer for class 'LoginRequest' is not found` | Faltaba el plugin de compilación de serialización en el módulo `shared`. | Se agregó `kotlin("plugin.serialization") version "2.0.20"` en `shared/build.gradle.kts`. |
| `Error de conexión: null` / `CLEARTEXT communication not permitted` | Bloqueo de tráfico HTTP no cifrado y falta de permisos de red en Android. | Se incluyó `INTERNET` y `usesCleartextTraffic="true"` en el `AndroidManifest.xml`. |
| Fallo de red en dispositivo físico por USB | Incompatibilidad de rutas local/host desde dispositivo externo. | Se configuró el redireccionamiento mediante `adb reverse tcp:8080 tcp:8080` utilizando la IP/puerto local. |