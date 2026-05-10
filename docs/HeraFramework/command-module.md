# Hera Command Module — `hera-command-paper`

`hera-command-paper` te da una forma declarativa y moderna de registrar comandos Paper sin caer en Bukkit legacy, sin parseo manual de `args[]` y sin convertir cada comando nuevo en un infierno de mantenimiento.

## Qué resuelve

- árbol de comandos explícito
- subcomandos y ramas claras
- argumentos tipados
- autocomplete reutilizable
- permisos y requirements por rama
- `sender` y `executor` separados
- integración moderna con `LifecycleEvents.COMMANDS`
- help metadata reutilizable

## Quick path

1. creá `PaperCommandModule`
2. definí un `CommandSpec`
3. registrá ramas con `LiteralNodeSpec` y argumentos
4. separá la lógica en handlers
5. registrá el spec en `onEnable`

---

## 1) Inicialización

```java
public final class FriendsSystemPlugin extends JavaPlugin {

    private PaperCommandModule commandModule;

    @Override
    public void onEnable() {
        this.commandModule = new PaperCommandModule(this);
        this.commandModule.register(FriendsCommands.create(this.commandModule.visibilityRefresher()));
    }
}
```

## Qué hace internamente

El módulo registra comandos usando la Command API moderna de Paper sobre `LifecycleEvents.COMMANDS`, así que no tenés que manejar el registro Brigadier crudo a mano.

---

## 2) Concepto central: árbol de comandos

No pienses el comando como una línea plana. Pensalo como un árbol.

```text
/friends add <player>
/friends remove <player>
/friends list
```

Eso se modela así:

```text
friends
├── add
│   └── <player>
├── remove
│   └── <player>
└── list
```

---

## 3) API principal

| Pieza | Rol |
|---|---|
| `CommandSpec` | raíz del comando |
| `LiteralNodeSpec` | subcomando literal como `add`, `remove`, `list` |
| `ArgumentNodeSpec` | nodo de argumento tipado |
| `CommandHandler` | lógica de ejecución |
| `CommandExecutionContext` | acceso a sender, executor, location y argumentos |
| `CommandArgument<T>` | definición de argumento reusable |
| `CommandArguments` | factories listas para usar |
| `SuggestionProviders` | autocomplete reutilizable |

---

## 4) Ejemplo práctico

```java
public final class FriendsCommands {

    private FriendsCommands() {
    }

    public static CommandSpec create(CommandVisibilityRefresher visibilityRefresher) {
        return CommandSpec.builder("friends")
            .aliases("f")
            .description("Comando principal de amigos")
            .usage("/friends <add|remove|list>")
            .child(LiteralNodeSpec.builder("add")
                .description("Envía una solicitud")
                .usage("/friends add <player>")
                .argument(CommandArguments.onlinePlayer("player"))
                .handler(context -> {
                    Player target = context.requireArgument("player", Player.class);
                    context.sender().sendRichMessage("<green>Solicitud enviada a " + target.getName() + "</green>");
                    return CommandResult.success();
                })
                .build())
            .child(LiteralNodeSpec.builder("list")
                .description("Lista tus amigos")
                .usage("/friends list")
                .handler(context -> {
                    context.sender().sendRichMessage("<yellow>Acá iría la lista de amigos.</yellow>");
                    return CommandResult.success();
                })
                .build())
            .build();
    }
}
```

---

## 5) Dónde poner la lógica

Regla sana:

- el **spec** define navegación
- el **handler** define comportamiento

Si la rama crece, sacá la lógica a una clase separada:

```java
final class FriendsAddHandler implements CommandHandler {
    @Override
    public int execute(CommandExecutionContext context) {
        Player target = context.requireArgument("player", Player.class);
        return CommandResult.success();
    }
}
```

---

## 6) Argumentos tipados

Factories disponibles hoy:

| Factory | Uso |
|---|---|
| `CommandArguments.word("name")` | palabra simple |
| `CommandArguments.greedyText("message")` | texto completo restante |
| `CommandArguments.integer("amount")` | entero |
| `CommandArguments.integer("amount", min, max)` | entero acotado |
| `CommandArguments.doubleArg("value")` | double |
| `CommandArguments.bool("flag")` | boolean |
| `CommandArguments.onlinePlayer("player")` | jugador online resolviendo selector Paper |
| `CommandArguments.enumValue("mode", Mode.class)` | enum con autocomplete |

### Argumento custom

```java
CommandArgument<Integer> amount = CommandArgument.builder(
        "amount",
        IntegerArgumentType.integer(1, 64),
        Integer.class,
        (ctx, name) -> IntegerArgumentType.getInteger(ctx, name)
    )
    .suggestions(SuggestionProviders.integers(1, 16, 32, 64))
    .build();
```

---

## 7) Autocomplete

Usá `SuggestionProviders` cuando puedas.

Helpers actuales:

| Provider | Uso |
|---|---|
| `SuggestionProviders.strings(...)` | valores fijos |
| `SuggestionProviders.integers(...)` | presets numéricos |
| `SuggestionProviders.enums(...)` | enums |
| `SuggestionProviders.onlinePlayers()` | jugadores online |
| `SuggestionProviders.worlds()` | mundos |
| `SuggestionProviders.snapshotStrings(...)` | snapshot en memoria |
| `SuggestionProviders.asyncStrings(...)` | generación async de strings |
| `SuggestionProviders.merge(...)` | combinar providers |

## Regla de oro

Autocomplete es hot path.

- sí a memoria/cache/snapshot
- no a MySQL directo
- no a trabajo pesado en main thread
- filtrá por prefijo

---

## 8) Permisos, policies y requirements

### Permiso simple

```java
.permission("friends.admin")
```

### Sender policy

```java
.senderPolicy(SenderPolicy.PLAYER_ONLY)
```

### Executor policy

```java
.executorPolicy(ExecutorPolicy.PLAYER_ONLY)
```

### Requirement custom

```java
.requirement(context -> context.sender().hasPermission("friends.use"))
```

### Restricted

```java
.restricted(true)
```

---

## 9) `sender` vs `executor`

No son lo mismo.

- `sender` = quién disparó el comando
- `executor` = para quién se ejecuta en el contexto actual

Esto importa con `/execute as ...` y otros casos donde Paper separa ambas nociones.

En handlers:

```java
CommandSender sender = context.sender();
Player player = context.requireExecutor(Player.class);
```

---

## 10) Visibilidad dinámica y `CommandVisibilityRefresher`

Si una rama depende de estado dinámico, el cliente puede quedar desincronizado.

Ejemplos:

- leader de party
- cola activa
- staff mode
- permisos que cambian en runtime

Para esos casos:

```java
visibilityRefresher.refresh(player);
visibilityRefresher.refreshAllOnline();
```

Usalo cuando cambie algo que altere qué comandos o ramas puede ver/usar un jugador.

---

## 11) Help metadata

Si cargás `description` y `usage`, podés reutilizar esa metadata con:

```java
List<CommandHelpEntry> entries = CommandHelpIndex.entries(spec);
```

Eso sirve para:

- `/help` propio
- menú de ayuda
- docs internas
- mensajes de uso consistentes

---

## 12) Buenas prácticas

- mantené ramas chicas y claras
- no metas la lógica de negocio en el builder si ya creció
- reutilizá argumentos y suggestion providers
- usá `visibilityRefresher` solo cuando haga falta
- no consultes infraestructura pesada desde autocomplete
- no uses `args[]`; leé argumentos tipados desde el contexto

---

## 13) Qué NO hacer

- no usar `BasicCommand` como base del framework
- no hacer parse manual de strings si ya existe argumento tipado
- no ejecutar I/O pesado en suggestions
- no mezclar root enorme con cincuenta `if`
- no olvidar que `sender` y `executor` pueden ser distintos

---

## 14) Checklist de implementación

- [ ] crear `PaperCommandModule` en `onEnable`
- [ ] definir `CommandSpec` con metadata mínima
- [ ] separar ramas con `LiteralNodeSpec`
- [ ] usar `CommandArguments` o `CommandArgument` custom
- [ ] registrar handlers claros
- [ ] agregar permissions/policies/requirements donde corresponda
- [ ] refrescar visibilidad solo si la rama depende de estado dinámico
- [ ] registrar el spec en el módulo

---

## Next step

Si querés levantar un plugin consumidor completo, seguí con `docs/HeraFramework/getting-started.md`.
