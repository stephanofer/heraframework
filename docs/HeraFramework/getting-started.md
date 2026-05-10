# Hera Framework — Guía rápida para consumirlo en un plugin

Guía práctica para crear un plugin Paper nuevo, conectarlo a Hera y sacar un `.jar` final funcional. El ejemplo usa un plugin ficticio llamado `FriendsSystem` con Gradle Kotlin DSL e IntelliJ IDEA.

## Qué te llevás de esta guía

- cómo crear el plugin consumidor
- cómo agregar Hera como dependencia local
- cómo empaquetar el plugin final con `shadow`
- cómo inicializar el runtime modular
- cómo levantar módulos/capacidades de Hera en `onEnable`

## Quick path

1. Creá el plugin Paper con Gradle Kotlin DSL.
2. Publicá Hera en `mavenLocal()` o consumilo desde el mismo workspace.
3. Agregá los módulos Hera que necesitás.
4. En `onEnable`, construí el runtime y luego inicializá las capacidades que no entran al runtime.
5. En `onDisable`, cerrá el runtime.

---

## 1) Crear el plugin consumidor

Estructura mínima sugerida:

```text
FriendsSystem/
├─ build.gradle.kts
├─ settings.gradle.kts
├─ src/main/java/com/example/friendssystem/
└─ src/main/resources/plugin.yml
```

`plugin.yml` mínimo:

```yml
name: FriendsSystem
version: '${version}'
main: com.example.friendssystem.FriendsSystemPlugin
api-version: '26.1.2'
load: POSTWORLD
description: Plugin consumidor de ejemplo usando Hera Framework.
```

---

## 2) Tener Hera disponible como dependencia

Hoy Hera está pensado para consumo local compilado.

### Opción A — proyecto externo a este workspace

Publicá Hera en Maven Local:

```bash
./gradlew publishToMavenLocal
```

Las coordenadas actuales del workspace son:

| Campo | Valor |
|---|---|
| Group | `com.stephanofer.hera` |
| Version | `0.1.0-SNAPSHOT` |

### Opción B — plugin dentro del mismo workspace multi-módulo

Usá `project(...)` directamente.

---

## 3) Configurar `build.gradle.kts`

Ejemplo para un plugin externo usando Maven Local:

```kotlin
import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    java
    id("com.gradleup.shadow") version "9.4.1"
}

group = "com.example"
version = "1.0.0-SNAPSHOT"

repositories {
    mavenLocal()
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:26.1.2.build.60-stable")

    implementation("com.stephanofer.hera:hera-core-api:0.1.0-SNAPSHOT")
    implementation("com.stephanofer.hera:hera-core-runtime:0.1.0-SNAPSHOT")
    implementation("com.stephanofer.hera:hera-command-paper:0.1.0-SNAPSHOT")
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(25))
}

tasks.processResources {
    filesMatching("plugin.yml") {
        expand(mapOf("version" to project.version))
    }
}

tasks.jar {
    archiveClassifier.set("plain")
}

tasks.named<ShadowJar>("shadowJar") {
    archiveClassifier.set("")
}
```

## Importante sobre el jar final

El plugin final que va al servidor es el **shadow jar**, no el `-plain.jar`.

Hera **no** se distribuye como plugin base separado del servidor. El consumidor debe embutir los módulos que usa dentro de su propio jar.

---

## 4) Qué módulos agregar

Sumá solo lo que realmente uses.

| Caso | Módulos mínimos |
|---|---|
| Runtime modular | `hera-core-api`, `hera-core-runtime` |
| Comandos modernos | `hera-command-paper` |
| Config futura | `hera-config` |
| Feedback futuro | `hera-feedback` |
| Scheduler futuro | `hera-scheduler` |
| MySQL futuro | `hera-data-mysql` |
| Redis futuro | `hera-data-redis` |

---

## 5) Inicializar Hera en `onEnable`

Patrón recomendado:

```java
package com.example.friendssystem;

import com.stephanofer.hera.command.paper.PaperCommandModule;
import com.stephanofer.hera.core.api.HeraRuntime;
import com.stephanofer.hera.core.runtime.HeraRuntimeBuilder;
import org.bukkit.plugin.java.JavaPlugin;

public final class FriendsSystemPlugin extends JavaPlugin {

    private HeraRuntime heraRuntime;
    private PaperCommandModule commandModule;

    @Override
    public void onEnable() {
        this.heraRuntime = new HeraRuntimeBuilder()
            // .register(new FriendsConfigModule(...))
            // .register(new FriendsDomainModule(...))
            .build();

        this.commandModule = new PaperCommandModule(this);
        // this.commandModule.register(FriendsCommands.create(...));
    }

    @Override
    public void onDisable() {
        if (this.heraRuntime != null) {
            this.heraRuntime.close();
        }
    }
}
```

## Qué hace cada parte

| Pieza | Rol |
|---|---|
| `HeraRuntimeBuilder` | ordena, configura y arranca `HeraModule`s |
| `HeraRuntime` | runtime vivo del framework; se debe cerrar en shutdown |
| `PaperCommandModule` | integra comandos con la Command API moderna de Paper |

---

## 6) Qué entra al runtime y qué no

Hoy hay dos estilos de integración:

### A. Módulos runtime (`HeraModule`)

Van a `HeraRuntimeBuilder`.

Un módulo runtime tiene este contrato base:

```java
public interface HeraModule {
    ModuleDescriptor descriptor();
    default void configure(HeraRuntime runtime) throws Exception {}
    default void start() throws Exception {}
    default void stop() throws Exception {}
}
```

### B. Capacidades con bootstrap propio

Ejemplo actual: `hera-command-paper`.

No se registra en `HeraRuntimeBuilder`; se inicializa explícitamente:

```java
this.commandModule = new PaperCommandModule(this);
```

Eso está bien. No fuerces una capacidad al runtime si hoy su integración real vive mejor afuera.

---

## 7) Ejemplo práctico mínimo con comandos

```java
@Override
public void onEnable() {
    this.heraRuntime = new HeraRuntimeBuilder().build();

    this.commandModule = new PaperCommandModule(this);
    this.commandModule.register(FriendsCommands.create(this.commandModule.visibilityRefresher()));
}
```

La idea es:

1. arrancás runtime base
2. levantás capacidades de infraestructura necesarias
3. registrás comandos, hooks o integraciones de plugin

---

## 8) Checklist antes de copiar el jar al servidor

- [ ] `plugin.yml` correcto
- [ ] dependencias Hera agregadas
- [ ] `shadowJar` configurado
- [ ] se usa el jar final, no el `plain`
- [ ] `HeraRuntime` se cierra en `onDisable`
- [ ] solo se agregaron los módulos/capacidades que el plugin necesita

---

## 9) Qué NO hacer

- no trates Hera como plugin base separado
- no copies jars internos de Hera sueltos al `plugins/`
- no uses el `-plain.jar` como plugin final
- no metas lógica de negocio dentro del framework
- no registres módulos “por si acaso”

---

## 10) Ejemplo de flujo completo

### Crear FriendsSystem

1. creás el proyecto Gradle en IntelliJ
2. agregás repos + dependencias
3. publicás Hera en `mavenLocal()` si el plugin es externo
4. armás tu `FriendsSystemPlugin`
5. inicializás runtime + capacidades
6. generás el shadow jar
7. copiás `build/libs/FriendsSystem-<version>.jar` al servidor

---

## Next step

Si tu próximo paso es registrar comandos, seguí con `docs/HeraFramework/command-module.md`.
