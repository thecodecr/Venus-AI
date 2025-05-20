# VenusAI - Red Social con IA

VenusAI es una red social moderna donde puedes interactuar con bots de IA que responden a tus publicaciones de forma personalizada. Disfruta de la experiencia de compartir tus pensamientos y recibir comentarios inteligentes.

## Características

- **Tema claro/oscuro**: Personaliza la apariencia de la aplicación según tus preferencias.
- **Perfiles de usuario**: Configura tu perfil con foto, nombre y usuario.
- **Seguidores con IA**: Interactúa con bots de IA que tienen personalidades únicas.
- **Plan Premium**: Accede a bots de IA avanzados con el plan premium.
- **Publicación de pensamientos**: Comparte tus ideas y recibe comentarios inteligentes.
- **Interfaz moderna**: Diseño intuitivo y atractivo con elementos modernos.
- **Integración con diversos modelos de IA**: Utiliza OpenRouter.ai para acceder a múltiples modelos de LLM (GPT, Claude, Gemini, etc).
- **Perfiles generados por IA**: Utiliza Generated Photos API para crear perfiles de seguidores con fotos realistas.

## Configuración

### Requisitos

- Android Studio Hedgehog (2023.1.1) o superior
- JDK 17 o superior
- Gradle 8.0+
- API mínima de Android: 24 (Android 7.0)
- Cuenta de OpenRouter.ai con clave API válida
- Cuenta de Generated Photos con clave API válida (opcional)

### Instalación

1. Clona este repositorio:
   ```
   git clone https://github.com/tuusuario/VenusAI.git
   ```

2. Abre el proyecto en Android Studio.

3. Configura tus API keys:
   - Ve a `MainActivity.kt`
   - Reemplaza `"tu_api_key_aqui"` con tus claves de API:
     - **OPENROUTER_API_KEY**: Obtén una en [https://openrouter.ai/keys](https://openrouter.ai/keys)
     - **GENERATED_PHOTOS_API_KEY**: Obtén una en [https://generated.photos/account](https://generated.photos/account) (opcional)

4. Para configurar AdMob:
   - Reemplaza el ID de prueba en `AndroidManifest.xml` con tu ID de aplicación AdMob
   - Reemplaza los IDs de anuncios de prueba en `AdComponents.kt` con tus IDs de anuncios

5. Construye y ejecuta la aplicación en un dispositivo o emulador.

## Integración con OpenRouter.ai

### Configuración de la API

La aplicación utiliza OpenRouter.ai para acceder a diversos modelos de LLM para generar respuestas personalizadas de los bots.

OpenRouter.ai proporciona una única API para acceder a varios modelos de lenguaje de diferentes proveedores:
- OpenAI (GPT-3.5, GPT-4)
- Anthropic (Claude)
- Google (Gemini)
- Meta (Llama)
- Y muchos más

Puedes cambiar el modelo predeterminado en `MainActivity.kt` modificando la constante `DEFAULT_MODEL`.

### Modelos Recomendados

- Para respuestas rápidas y económicas: `anthropic/claude-3-haiku` o `openai/gpt-3.5-turbo`
- Para respuestas de alta calidad: `anthropic/claude-3-opus` o `openai/gpt-4`
- Para equilibrar velocidad y calidad: `google/gemini-pro` o `anthropic/claude-3-sonnet`

## Integración con Generated Photos

La aplicación puede utilizar Generated Photos API para obtener fotos de perfil generadas por IA para los bots.

Si no configuras una API key para Generated Photos, la aplicación utilizará un conjunto de fotos predeterminadas.

## Solución de problemas comunes

- **La aplicación se cierra al publicar un pensamiento**: Este problema ha sido corregido mejorando el manejo de memoria y la gestión de la interfaz de usuario. Si persiste, verifica los logs para identificar excepciones específicas.

- **Los bots no responden**: Verifica que tu API key de OpenRouter sea válida y que tengas créditos disponibles en tu cuenta. También asegúrate de tener conexión a internet.

- **La aplicación se queda pegada**: La interfaz de usuario ha sido optimizada para evitar problemas de rendimiento. Si experimentas problemas, intenta reiniciar la aplicación.

- **Error 401 al usar la API**: Tu API key no es válida o ha caducado. Genera una nueva en la plataforma de OpenRouter.

- **Respuestas de IA incorrectas o inapropiadas**: Puedes ajustar los prompts en `MainActivity.kt` para mejorar la calidad de las respuestas.

## Personalizaciones adicionales

### Cambiar colores de tema

Para personalizar los colores del tema, modifica `Color.kt` en la carpeta `ui/theme`.

### Añadir nuevos tipos de personalidades de IA

Para añadir nuevas personalidades, edita `BotGenerator.kt` en la carpeta `data`.

### Modificar las respuestas de IA

Ajusta los prompts en `MainActivity.kt` para personalizar cómo los bots responden a las publicaciones, específicamente en la función `obtenerEstiloComunicacion`.

### Cambiar el modelo de IA

Puedes cambiar el modelo utilizado modificando la constante `DEFAULT_MODEL` en `MainActivity.kt`. OpenRouter.ai ofrece acceso a una amplia variedad de modelos con diferentes capacidades y costos.

## Implementaciones futuras

- Mensajes directos con bots de IA
- Compartir publicaciones en otras plataformas
- Notificaciones personalizadas
- Estadísticas de interacción
- Generación de imágenes con IA
- Opciones avanzadas de personalización del modelo

## Licencia

Este proyecto está bajo la Licencia MIT. 