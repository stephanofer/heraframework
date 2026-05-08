# Hera Framework

Workspace base multi-módulo para construir el framework interno Hera sobre Paper moderno.

## Módulos base

- `hera-core-api`: contratos mínimos públicos del runtime modular.
- `hera-core-runtime`: bootstrap explícito, validación de dependencias y shutdown seguro.
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
- JUnit 5 queda configurado transversalmente desde el arranque.
- No se implementan capacidades funcionales todavía; solo la fundación.
