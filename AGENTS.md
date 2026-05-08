# Hera Plugin Framework — Working Guide

## Propósito

Este proyecto existe para construir un framework interno modular para plugins Paper modernos. El foco está en remover boilerplate técnico y dar una base sólida, consistente y mantenible para los plugins del equipo.

## Enfoque de arquitectura

- Diseñar para **Paper moderno**.
- Usar **composición sobre herencia**.
- Organizar el framework en **capacidades modulares internas**.
- Mantener el **core pequeño, limpio y agnóstico del negocio**.
- Tratar cada capacidad como **módulo opcional** cuando aplique.
- Aislar zonas volátiles detrás de adapters claros.
- Mantener hooks e integraciones externas fuera del core.

## Principios de trabajo

- No introducir compatibilidad legacy.
- No diseñar alrededor de Bukkit antiguo si Paper ya ofrece una mejor API.
- No usar NMS salvo detección puntual o casos extremadamente acotados y aislados.
- No centralizar lógica de negocio dentro del framework.
- No agregar módulos por intuición; cada módulo debe tener responsabilidad clara.
- Priorizar claridad operativa, shutdown seguro y mantenibilidad a largo plazo.

## Decisiones de plataforma

- Texto y mensajes: **Adventure + MiniMessage**.
- Datos persistentes: **MySQL**.
- Infra distribuida: **Redis**.
- Hooks soportados: **PlaceholderAPI** y **zMenu**.
- Feedback centralizado: chat, actionbar, titles, subtitles, sonidos y broadcast.
- Empaquetado recomendado en consumidores: **shadow + relocate**.
- No tratar Hera como plugin base separado del servidor.

## Cómo pensar cambios nuevos

Antes de agregar una capacidad nueva, validar:

1. si pertenece al core o a un módulo opcional,
2. si Paper ya resuelve el problema de forma nativa,
3. si introduce acoplamiento innecesario,
4. si afecta versionado futuro,
5. si realmente simplifica a los plugins consumidores.
6. Determinar si ese cambio o modulo nuevo corresponde a este proyecto a el proyecto especifico de negocio.

## Referencias obligatorias

- Base Paper y APIs modernas,Config, lifecycle y command API de Paper: `docs/PaperMC/`
- Adventure y MiniMessage: `docs/Adventure/`
- PlaceholderAPI: `docs/PlaceholderAPI/`
- zMenu: `docs/zMenu/`

## Regla final

Si una decisión hace al framework más mágico, más acoplado o más difícil de evolucionar, probablemente está mal y debe rediseñarse.
