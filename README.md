# 💰 Control de Gastos Personales

Aplicación móvil Android desarrollada en Kotlin para la gestión y control de gastos personales con Firebase como backend.

![Kotlin](https://img.shields.io/badge/Kotlin-100%25-purple?style=flat-square&logo=kotlin)
![Android](https://img.shields.io/badge/Platform-Android-green?style=flat-square&logo=android)
![Firebase](https://img.shields.io/badge/Backend-Firebase-orange?style=flat-square&logo=firebase)
![License](https://img.shields.io/badge/License-Educational-blue?style=flat-square)

## 📱 Características Principales

### 🔐 Autenticación
- ✅ Login con email y contraseña
- ✅ Registro de nuevos usuarios
- ✅ Autenticación con Google Sign-In
- ✅ Sesión persistente y segura

### 💰 Gestión de Gastos
- ✅ Agregar gastos con categoría, monto, fecha y notas
- ✅ Editar gastos existentes
- ✅ Eliminar gastos con gesto swipe
- ✅ Visualización en tiempo real con Firestore
- ✅ Cálculo automático de totales mensuales

### 🔍 Filtros Avanzados
- ✅ Filtrar por múltiples categorías simultáneamente
- ✅ Filtrar por período (este mes, mes pasado, últimos 3 meses)
- ✅ Chips visuales mostrando filtros activos
- ✅ Eliminar filtros individualmente con un clic
- ✅ Botón "Limpiar todo" para resetear filtros

### 📊 Estadísticas y Gráficos
- ✅ Total de gastos acumulados
- ✅ Promedio diario de gastos
- ✅ Categoría más gastada con monto
- ✅ Gráfico circular (pie chart) por categorías con porcentajes
- ✅ Gráfico de barras mostrando gastos de los últimos 6 meses

## 🛠️ Tecnologías Utilizadas

### Core
- **Lenguaje:** Kotlin 100%
- **IDE:** Android Studio Hedgehog
- **SDK Mínimo:** API 24 (Android 7.0)
- **SDK Objetivo:** API 36

### Backend
- **Firebase Authentication** - Gestión de usuarios
- **Cloud Firestore** - Base de datos en tiempo real
- **Google Play Services** - Google Sign-In

### Librerías
- **Material Design Components** - Interfaz moderna
- **MPAndroidChart v3.1.0** - Gráficos interactivos
- **RecyclerView** - Lista eficiente de gastos
- **CardView** - Diseño con tarjetas

### Arquitectura
- Patrón **MVC** (Model-View-Controller)
- **Firebase Realtime Listeners** para sincronización en tiempo real
- **ItemTouchHelper** para gestos swipe

## 📂 Estructura del Proyecto
```
ControlGastosPersonales/
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/ch220048/controlgastospersonales/
│   │   │   │   ├── LoginActivity.kt          # Pantalla de login
│   │   │   │   ├── RegistroActivity.kt       # Registro de usuarios
│   │   │   │   ├── MainActivity.kt           # Pantalla principal
│   │   │   │   ├── AgregarGastoActivity.kt   # Agregar/editar gastos
│   │   │   │   ├── EstadisticasActivity.kt   # Gráficos y estadísticas
│   │   │   │   ├── Gasto.kt                  # Modelo de datos
│   │   │   │   ├── GastoAdapter.kt           # Adapter para RecyclerView
│   │   │   │   └── SwipeToDeleteCallback.kt  # Gesto de eliminación
│   │   │   ├── res/
│   │   │   │   ├── layout/                   # Layouts XML
│   │   │   │   ├── menu/                     # Menús de opciones
│   │   │   │   ├── drawable/                 # Recursos gráficos
│   │   │   │   └── values/                   # Colores, strings, estilos
│   │   │   ├── AndroidManifest.xml
│   │   │   └── google-services.json          # Configuración Firebase
│   │   └── build.gradle.kts                  # Dependencias del módulo
│   └── gradle/
├── build.gradle.kts                           # Configuración del proyecto
├── settings.gradle.kts
├── .gitignore
└── README.md
```

## 🚀 Instalación y Configuración

### Requisitos Previos
- ✅ Android Studio Hedgehog (2023.1.1) o superior
- ✅ JDK 11 o superior
- ✅ Cuenta de Firebase (gratuita)
- ✅ Dispositivo Android o Emulador con API 24+

### Pasos de Instalación

#### 1. Clonar el Repositorio
```bash
git clone https://github.com/David-301/ControlGastosPersonales.git
cd ControlGastosPersonales
```

#### 2. Configurar Firebase

**Crear proyecto en Firebase:**
1. Ve a (https://console.firebase.google.com/)
2. Clic en "Agregar proyecto"
3. Nombra el proyecto: `ControlGastosPersonales`
4. Sigue los pasos de configuración

**Habilitar servicios:**
1. **Authentication:**
   - Ve a Authentication → Sign-in method
   - Habilita "Correo electrónico/contraseña"
   - Habilita "Google"
   
2. **Firestore Database:**
   - Ve a Firestore Database
   - Clic en "Crear base de datos"
   - Selecciona "Modo de prueba" (para desarrollo)
   - Elige una ubicación cercana

**Configurar la app Android:**
1. En Firebase Console, clic en el ícono de Android
2. Nombre del paquete: `com.ch220048.controlgastospersonales`
3. Descarga el archivo `google-services.json`
4. Coloca `google-services.json` en la carpeta `app/`

#### 3. Abrir en Android Studio
```bash
# Desde la terminal
studio .

# O abre Android Studio y selecciona "Open"
```

#### 4. Sincronizar Gradle
- Android Studio sincronizará automáticamente
- Espera a que descargue todas las dependencias
- Si hay errores, haz clic en "Sync Project with Gradle Files"

#### 5. Ejecutar la Aplicación
1. Conecta un dispositivo Android o inicia un emulador
2. Haz clic en Run ▶️ (o Shift + F10)
3. Selecciona el dispositivo de destino
4. Espera a que compile e instale

## 📊 Funcionalidades Detalladas

### Pantalla de Login
- Formulario de login con email y contraseña
- Validación de campos en tiempo real
- Botón de "Login con Google" con diseño personalizado
- Navegación a pantalla de registro
- Manejo de errores con mensajes descriptivos

### Pantalla de Registro
- Formulario completo de registro
- Validación de email y contraseña
- Confirmación de contraseña
- Creación de cuenta en Firebase Authentication
- Redirección automática después del registro

### Pantalla Principal
- **Header:** Card con gradiente mostrando total del mes
- **Contador:** Cantidad de gastos registrados
- **Chips de filtros:** Visualización de filtros activos
- **Lista de gastos:** RecyclerView con scroll infinito
  - Nombre del gasto
  - Categoría con ícono
  - Fecha en formato dd/MM/yyyy
  - Monto con formato $XX.XX
  - Notas opcionales
- **Swipe to delete:** Deslizar para eliminar con confirmación
- **FAB:** Botón flotante para agregar nuevo gasto
- **Menú superior:**
  - Estadísticas
  - Filtros
  - Cerrar sesión

### Pantalla Agregar/Editar Gasto
- **Modo dual:** Agregar nuevo o editar existente
- **Campos:**
  - Nombre del gasto (obligatorio)
  - Monto en dólares (obligatorio)
  - Categoría con spinner (obligatorio)
  - Fecha con DatePicker (obligatorio)
  - Notas opcionales (textarea)
- **Validaciones:** Todos los campos obligatorios
- **Botones:** Cancelar y Guardar/Actualizar

### Diálogo de Filtros
- **Categorías:** Checkboxes múltiples
  - Comida 🍔
  - Transporte 🚗
  - Entretenimiento 🎬
  - Salud 🏥
  - Educación 📚
  - Servicios 🔧
  - Compras 🛍️
  - Vivienda 🏠
  - Otros 📦
- **Períodos:** Radio buttons
  - Todos los períodos
  - Este mes
  - Mes pasado
  - Últimos 3 meses
- **Botones:** Limpiar, Cancelar, Aplicar

### Pantalla de Estadísticas
- **Card de resumen:**
  - Total de gastos
  - Promedio diario (últimos 30 días)
  - Categoría más gastada con monto
- **Gráfico circular:**
  - Distribución por categorías
  - Porcentajes visuales
  - Colores personalizados
  - Leyenda interactiva
- **Gráfico de barras:**
  - Gastos por mes
  - Últimos 6 meses
  - Etiquetas de meses abreviadas
  - Animación de entrada

## 🎨 Diseño y UX

### Paleta de Colores
- **Primario:** `#667eea` (Morado/Azul)
- **Secundario:** `#764ba2` (Morado oscuro)
- **Acento:** `#f44336` (Rojo para eliminar)
- **Fondo:** `#F5F5F5` (Gris claro)
- **Texto:** `#212121` (Negro oscuro)

### Características de Diseño
- ✅ **Material Design 3** - Componentes modernos
- ✅ **Degradados** - Headers con gradientes
- ✅ **Cards elevadas** - Sombras y bordes redondeados (16dp)
- ✅ **Animaciones** - Transiciones suaves de 1000ms
- ✅ **Iconos** - Sistema de iconos de Material
- ✅ **Typography** - Jerarquía visual clara
- ✅ **Spacing** - Padding y margins consistentes

### Experiencia de Usuario
- **Feedback visual** - Snackbars y toasts informativos
- **Confirmaciones** - Diálogos para acciones destructivas
- **Loading states** - Indicadores de carga
- **Empty states** - Mensajes cuando no hay datos
- **Error handling** - Manejo elegante de errores

## 🔒 Seguridad

- ✅ **Firebase Authentication** - Gestión segura de usuarios
- ✅ **Firestore Security Rules** - Protección de datos
- ✅ **HTTPS** - Todas las comunicaciones encriptadas
- ✅ **Validación de entrada** - Prevención de datos inválidos
- ✅ **No contraseñas en código** - Uso de Firebase SDK
- ✅ **Sesión persistente** - Token seguro

### Reglas de Firestore Recomendadas
```javascript
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    match /gastos/{gasto} {
      allow read, write: if request.auth != null 
                         && request.auth.uid == resource.data.userId;
    }
  }
}
```

## 🧪 Testing

### Pruebas Manuales Realizadas
- ✅ Login con credenciales válidas e inválidas
- ✅ Registro de nuevos usuarios
- ✅ Google Sign-In
- ✅ CRUD completo de gastos
- ✅ Filtros por categorías
- ✅ Filtros por períodos
- ✅ Eliminación con swipe
- ✅ Edición de gastos
- ✅ Generación de gráficos
- ✅ Cálculos estadísticos

## 📝 Mejoras Futuras

- [ ] Exportar reportes a PDF
- [ ] Notificaciones push para recordatorios
- [ ] Modo oscuro (Dark Theme)
- [ ] Presupuestos por categoría
- [ ] Compartir gastos entre usuarios
- [ ] Soporte multi-idioma
- [ ] Respaldo en la nube
- [ ] Widget para home screen
- [ ] Búsqueda de gastos
- [ ] Filtros por rango de fechas personalizado

## 🤝 Contribuciones

Este proyecto es educativo. Si deseas contribuir:

1. Fork el repositorio
2. Crea una rama (`git checkout -b feature/nueva-funcionalidad`)
3. Commit tus cambios (`git commit -m 'Agregar nueva funcionalidad'`)
4. Push a la rama (`git push origin feature/nueva-funcionalidad`)
5. Abre un Pull Request

## 👨‍💻 Autor

**David Guillermo Campos H**
- 🎓 Carnet: CH220048
- 🏫 Universidad: Don Bosco (V)
- 📧 Email: davidguillermo2004@gmail.com

## 📄 Licencia

Este proyecto fue desarrollado con fines educativos como parte del curso de **Desarrollo de Aplicaciones Móviles**.

## 🙏 Agradecimientos

- [Firebase](https://firebase.google.com/) - Backend as a Service
- [MPAndroidChart](https://github.com/PhilJay/MPAndroidChart) - Librería de gráficos
- [Material Design](https://material.io/) - Sistema de diseño
- [Kotlin](https://kotlinlang.org/) - Lenguaje de programación

## 📚 Recursos Útiles

- [Documentación de Firebase](https://firebase.google.com/docs)
- [Guía de Material Design](https://material.io/design)
- [Kotlin para Android](https://developer.android.com/kotlin)
- [MPAndroidChart Wiki](https://github.com/PhilJay/MPAndroidChart/wiki)

📱 **Desarrollado con ❤️ en Android Studio usando Kotlin**
