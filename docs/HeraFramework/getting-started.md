# Hera Framework — Guía rápida para consumirlo en un plugin

Guía práctica para crear un plugin Paper nuevo, conectarlo a Hera y sacar un `.jar` final funcional. El ejemplo usa un plugin ficticio llamado `FriendsSystem` con Gradle Kotlin DSL e IntelliJ IDEA.

## Qué te llevás de esta guía

- cómo crear el plugin consumidor
- cómo agregar Hera como dependencia local
- cómo empaquetar el plugin final con `shadow`
- cómo instanciar módulos/capacidades de Hera en `onEnable`

## Quick path

1. Creá el plugin Paper con Gradle Kotlin DSL.
2. Publicá Hera en `mavenLocal()` o consumilo desde el mismo workspace.
3. Agregá los módulos Hera que necesitás.
4. En `onEnable`, instanciá las capacidades vivas que necesites.
5. En `onDisable`, cerrá solo las capacidades que mantengan recursos.

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

    implementation("com.stephanofer.hera:hera-command-paper:0.1.0-SNAPSHOT")
    implementation("com.stephanofer.hera:hera-config:0.1.0-SNAPSHOT")
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
| Comandos modernos | `hera-command-paper` |
| Configuración | `hera-config` |
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
import com.stephanofer.hera.config.HeraConfigModule;
import org.bukkit.plugin.java.JavaPlugin;

public final class FriendsSystemPlugin extends JavaPlugin {

    private HeraConfigModule configModule;
    private PaperCommandModule commandModule;

    @Override
    public void onEnable() {
        this.configModule = HeraConfigModule.builder(this)
            // .file(...)
            // .directory(...)
            .build();
        this.commandModule = new PaperCommandModule(this);
        // this.commandModule.register(FriendsCommands.create(...));
    }

    @Override
    public void onDisable() {
        if (this.configModule != null) {
            this.configModule.close();
        }
    }
}
```

## Qué hace cada parte

| Pieza | Rol |
|---|---|
| `HeraConfigModule` | prepara y expone configuración tipada/administrada para el plugin |
| `PaperCommandModule` | integra comandos con la Command API moderna de Paper |

---

## 6) Qué se instancia y qué no

Hoy hay dos tipos de piezas en Hera:

### A. Librerías/API puras

No necesitan inicialización explícita.

Ejemplos típicos:

- `CommandSpec`
- builders
- validators
- codecs

### B. Capacidades vivas

Necesitan `JavaPlugin`, lifecycle Paper, IO o recursos propios.

Ejemplos actuales:

- `PaperCommandModule`
- `HeraConfigModule`

Se instancian directamente en el plugin consumidor:

```java
this.configModule = HeraConfigModule.builder(this).build();
this.commandModule = new PaperCommandModule(this);
```

Eso es el enfoque oficial: `JavaPlugin` del consumidor como composition root, sin runtime global adicional.

---

## 7) Ejemplo práctico mínimo con comandos

```java
@Override
public void onEnable() {
    this.configModule = HeraConfigModule.builder(this).build();
    this.commandModule = new PaperCommandModule(this);
    this.commandModule.register(FriendsCommands.create(this.commandModule.visibilityRefresher()));
}
```

La idea es:

1. levantás solo las capacidades de infraestructura que tu plugin necesita
2. usás sus APIs listas para trabajar
3. registrás comandos, hooks o integraciones de plugin

---

## 8) Checklist antes de copiar el jar al servidor

- [ ] `plugin.yml` correcto
- [ ] dependencias Hera agregadas
- [ ] `shadowJar` configurado
- [ ] se usa el jar final, no el `plain`
- [ ] se cierran en `onDisable` solo las capacidades que mantengan recursos
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
5. inicializás solo las capacidades vivas necesarias
6. generás el shadow jar
7. copiás `build/libs/FriendsSystem-<version>.jar` al servidor

---

## Next step

Si tu próximo paso es registrar comandos, seguí con `docs/HeraFramework/command-module.md`.
