# Hera Plugin Framework — PRD

## Producto

Hera Framework es un framework interno para desarrollar plugins de Paper modernos con una base técnica reutilizable, modular y consistente. Su objetivo es eliminar boilerplate repetitivo y permitir que cada plugin del equipo se concentre en su comportamiento de negocio sin reimplementar infraestructura común.

El framework debe servir como plataforma para construir plugins y modalidades con una experiencia de desarrollo estable, una arquitectura clara y una integración natural con el ecosistema Paper moderno.

## Objetivo del producto

- Centralizar infraestructura técnica repetida.
- Estandarizar cómo se construyen plugins del equipo.
- Reducir tiempo de arranque de nuevos proyectos.
- Mejorar mantenibilidad, consistencia y rendimiento.
- Aislar zonas volátiles de la API para facilitar soporte futuro a nuevas versiones.

## Filosofía del producto

- **Paper moderno only**: el producto está diseñado para Paper actual, no para Bukkit genérico ni compatibilidad legacy.
- **Composición sobre herencia**: los plugins consumen capacidades por módulos, no por una clase base monolítica.
- **Módulos opcionales**: cada plugin usa únicamente las capacidades que necesita.
- **Core limpio**: el núcleo no debe mezclar lógica de negocio, NMS ni integraciones externas innecesarias.
- **Abstracciones reales, no magia**: el framework debe simplificar, no esconder comportamiento crítico ni crear APIs confusas.
- **Estado del arte del ecosistema**: se prioriza Paper API, Adventure, MiniMessage y patrones modernos del ecosistema.

## Principios de diseño

- Separación estricta entre infraestructura reusable, integraciones y lógica de negocio consumidora.
- Dependencias explícitas entre módulos.
- Ciclo de vida claro para startup, reload y shutdown.
- Configuración tipada y validada.
- Operaciones costosas fuera del hilo principal cuando corresponda.
- Observabilidad suficiente para producción y debugging.
- Soporte futuro a nuevas versiones mediante adapters y fronteras bien definidas.

## Decisiones cerradas

- Plataforma objetivo: **Paper moderno**.
- Arquitectura base: **composición**.
- Organización interna: **modular**, con límites claros por capacidad.
- Composition root en consumidores: **`JavaPlugin`**, sin runtime global propio.
- Mensajería y texto: **Adventure + MiniMessage**, sin colores legacy.
- Compatibilidad legacy: **no soportada**.
- Folia: **fuera del alcance actual**.
- Multiversión artificial: **no**.
- NMS: **fuera del core**; solo aceptable para detección puntual de entorno o necesidades extremadamente acotadas.
- Base de datos SQL soportada: **MySQL**.
- Redis: **soportado como infraestructura opcional**.
- Hooks oficiales: **PlaceholderAPI** y **zMenu**.
- Distribución actual: **artefacto compilado consumido localmente**.
- Empaquetado final recomendado: **shadow + relocate** dentro del plugin consumidor.

## Capacidades del producto

### Composición modular
- Consumo explícito de módulos desde el `JavaPlugin` del plugin consumidor.
- Inicialización directa de capacidades que necesitan estado, lifecycle o recursos propios.
- APIs claras para usar cada capacidad sin contenedor ni runtime global.
- Contrato simple y predecible para plugins consumidores.

### Configuración
- Configuración tipada.
- Validación de configuración.
- Soporte para múltiples archivos.
- Defaults y estructura consistente.
- Reload controlado donde tenga sentido.

### Comandos
- Integración con la API moderna de comandos de Paper.
- Árbol de comandos y subcomandos.
- Argumentos tipados.
- Autocompletado.
- Permisos y ayuda consistente.

### Mensajes y feedback
- Sistema centralizado de feedback para:
  - mensajes de chat
  - mensajes a jugador
  - broadcast
  - actionbar
  - title / subtitle
  - sonidos
- El framework centraliza **cómo** se envía el feedback, no **a quién** se envía.
- La selección de destinatarios pertenece a cada plugin consumidor.

### Scheduler
- Scheduling centralizado por plugin.
- Cancelación segura en shutdown.
- API consistente para tareas internas.

### Datos y persistencia
- Soporte para MySQL.
- Pool de conexiones.
- Migraciones versionadas.
- Fronteras claras para acceso a datos.
- Reglas sanas para operaciones async.

### Redis
- Cliente Redis como capacidad opcional.
- Base para cache distribuida.
- Base para pub/sub y sincronización entre servidores.
- Base para locks livianos y estado efímero si un plugin lo requiere.

### Hooks
- **PlaceholderAPI** para placeholders e integración opcional con el ecosistema.
- **zMenu** para integraciones con menús externos cuando el plugin lo requiera.

## Alcance funcional esperado

El producto debe permitir construir plugins que puedan elegir solo las piezas necesarias. Un plugin simple puede usar únicamente config, comandos y feedback. Un plugin más complejo puede además componer MySQL, Redis y hooks externos. El framework no debe cargar infraestructura innecesaria por defecto.

La forma esperada de consumo es directa: el plugin agrega los módulos Hera que necesita como dependencias, instancia las capacidades vivas desde su `JavaPlugin` y usa sus APIs. Hera no debe introducir una segunda capa de runtime o contenedor por encima de Paper.

## Restricciones del producto

- No usar patrones de framework monolítico basados en una superclase obligatoria.
- No introducir un runtime global, contenedor interno o sistema de autowiring por metadata.
- No meter lógica de negocio de modalidades dentro del framework.
- No usar NMS como dependencia normal del diseño.
- No acoplar el core a hooks opcionales.
- No forzar compatibilidad con versiones antiguas a costa de degradar la arquitectura.
- No distribuir el framework como plugin separado del servidor.

## Referencias internas

- Paper: `docs/PaperMC/`
- Adventure: `docs/Adventure/`
- PlaceholderAPI: `docs/PlaceholderAPI/`
- zMenu: `docs/zMenu/`
