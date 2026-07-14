# Scratch 🎉

App de raspadita para niños — descubrí fotos raspando la pantalla.

## Requisitos
- Android 10+ (API 29+)
- Android Studio Hedgehog o superior (para desarrollo local)
- GitHub Actions (para build automático en la nube)

## Configuración del build en GitHub

### 1. Crear repositorio
1. En GitHub, creá un repositorio nuevo (privado o público)
2. Subí todo el contenido de esta carpeta al repositorio:
   ```
   git init
   git add .
   git commit -m "Initial commit"
   git remote add origin https://github.com/TU_USUARIO/scratch-app.git
   git push -u origin main
   ```

### 2. Build automático
Cada vez que hacés push a `main`, GitHub Actions compila automáticamente el APK.
Lo podés descargar en: **Actions → último workflow → Artifacts → scratch-release-apk**

También se crea un Release automático con el APK adjunto.

### 3. Instalar el APK en el celular
1. En el celular, ir a **Ajustes → Seguridad → Fuentes desconocidas** (o "Instalar apps desconocidas") y habilitarlo para tu navegador/explorador de archivos
2. Descargar el APK desde GitHub Releases
3. Abrir el APK y seguir los pasos de instalación

---

## Cómo usar la app

### Pantalla de configuración (adulto)
Al abrir la app, entrás directamente a la configuración.

- **Agregar fotos**: tocás "+ Agregar fotos" y seleccionás del carrete. Se optimizan automáticamente.
- **Activar/desactivar fotos**: tocás una foto en la grilla para incluirla o excluirla del juego
- **Borrar foto**: tocás el ✕ rojo en la esquina de la foto
- **Configurar opciones**: orden, tiempo de revelado, superficie y música
- **Comenzar**: tocás "¡Jugar!"

### Durante el juego
- El niño raspa la pantalla con el dedo para descubrir la foto
- Una manito animada muestra cómo hacerlo
- Cuando se revela la foto → celebración con confetti y vibración
- 5 segundos después pasa sola a la siguiente foto

### Desbloquear (volver a configuración)
Mantené presionados **simultáneamente** la parte superior e inferior de la pantalla por **3 segundos**.
La barra blanca en la parte superior indica el progreso del desbloqueo.

---

## Estructura del proyecto
```
app/src/main/
├── java/com/family/scratchapp/
│   ├── audio/          ScratchAudioManager.kt
│   ├── data/           SettingsPreferences.kt + Room DB
│   ├── textures/       TextureGenerator.kt (tierra, arena, pasto)
│   └── ui/
│       ├── game/       GameActivity, ScratchView, HandHintView
│       ├── settings/   SettingsActivity, PhotoManagerAdapter
│       └── celebration/ ConfettiView
├── res/
│   ├── font/           Nunito (embebida, offline)
│   ├── raw/            3 pistas de música WAV
│   └── ...
└── .github/workflows/  build.yml (GitHub Actions)
```

## Notas técnicas
- Las fotos se guardan en el storage interno de la app (privado, no accesible desde el carrete)
- Las texturas se generan proceduralmente en Kotlin (sin imágenes externas)
- El bloqueo de pantalla usa **startLockTask()** (Screen Pinning nativo de Android)
- La app funciona 100% offline
