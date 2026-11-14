# Reglas del Proyecto Ultreia (Protocolo Obligatorio de Finalización)

Cada vez que se realice cualquier modificación en el proyecto y se vaya a dar por terminada la tarea, se DEBE ejecutar estrictamente el siguiente flujo de trabajo:

## 1. Revisión y Verificación de Modificaciones
- Revisar a fondo los cambios implementados para comprobar su funcionamiento y coherencia con la arquitectura.
- Realizar pruebas o compilación de comprobación para garantizar que no existan errores de compilación ni regresiones.

## 2. Compilación y Generación del APK
- Compilar la aplicación en modo Release (`assembleRelease`).
- Verificar que el archivo ejecutable se genere y sobreescriba en la raíz del proyecto bajo el nombre y ruta exactos:
  `C:\Users\perez\Desktop\basura\Aplicaciones\Ultreia\Ultreia.apk`

## 3. Sincronización con GitHub
- Añadir los archivos modificados al control de versiones (`git add`).
- Crear un commit con un mensaje descriptivo y claro sobre los cambios realizados.
- Subir los cambios a GitHub en la rama principal (`git push origin main`).

## 4. Resumen Diario al Usuario
- Presentar al usuario un resumen claro, estructurado y detallado de lo que se ha realizado en la jornada / sesión, para que sepa exactamente en qué estado queda el proyecto.

---

## Directrices Adicionales del Usuario
- **Sin Bloques de Código en Respuestas:** No imprimir bloques de código fuente en las respuestas al usuario a menos que éste lo pida expresamente.
- **Soporte Bimodal (Caminos y Viajes):** Salvo que se indique lo contrario, cualquier funcionalidad general debe aplicarse a ambos perfiles (`isTravelGuide = false` para Caminos e `isTravelGuide = true` para Viajes) discriminando sus particularidades.
- **Detección de Proyectos Cruzados:** El usuario gestiona dos proyectos; si menciona algo que no corresponda a este proyecto (Ultreia), advertírselo antes de realizar cambios.
- **Términos Prohibidos:** No utilizar la palabra "oficial" en las guías ni exportaciones de la aplicación.
