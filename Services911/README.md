# 911 Services - prototipo Android Studio

Proyecto académico en Java + XML. Incluye Splash, menú de emergencias, tickets clasificados, despacho combinado, mapa simulado, ticket activo, perfiles médicos/familiares y SQLite local.

## Abrir
1. Android Studio > Open > selecciona la carpeta Services911.
2. Espera Gradle Sync.
3. Ejecuta en Medium Phone / API 24+.

## Base de datos
SQLite se crea automáticamente como `services911.db` con tablas `profiles` y `tickets`.
Para una versión real con PostgreSQL/pgAdmin, NO conectes Android directamente a PostgreSQL: crea una API backend (REST) y haz que Android consuma esa API.

## Clasificación simulada
- Disparo -> prioridad CRÍTICA, Policía + Ambulancia.
- Incendio -> prioridad CRÍTICA, Bomberos + Ambulancia.
- Paro cardiaco, inconsciente o hemorragia -> prioridad CRÍTICA.
- Ambulancia privada -> costo simulado de $850 MXN.

## Pendiente para una versión posterior
Google Maps real + permisos de ubicación, backend/API, autenticación, panel de operador, unidades en tiempo real, notificaciones, cifrado y controles de privacidad.
