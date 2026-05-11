# Hera Framework

Workspace base multi-módulo para construir el framework interno Hera sobre Paper moderno.

## Módulos base

- `hera-config`: base reservada para configuración tipada y validada.
- `hera-feedback`: base reservada para Adventure + MiniMessage.
- `hera-scheduler`: base reservada para scheduling centralizado.
- `hera-command-paper`: base reservada para Paper Command API + lifecycle.
- `hera-data-mysql`: base reservada para persistencia MySQL.
- `hera-data-redis`: base reservada para Redis opcional.
- `hera-hook-placeholderapi`: base reservada para hook opcional PlaceholderAPI.
- `hera-hook-zmenu`: base reservada para hook opcional zMenu.
- `hera-dev-sandbox`: plugin consumidor aislado para pruebas futuras del framework.

## Principios aplicados en esta base

- La raíz del repositorio ya no actúa como plugin Paper.
- El framework queda separado de cualquier plugin consumidor.
- La base usa Gradle multi-módulo con `build-logic` y version catalog.
- `JavaPlugin` del consumidor es el composition root; Hera no define un runtime global propio.
- JUnit 5 queda configurado transversalmente desde el arranque.
- Las capacidades del framework se consumen como módulos opcionales directos desde el plugin consumidor.
