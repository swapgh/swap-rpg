# Swap RPG Developer Guardrails

> Este documento es normativo, no solo orientativo.
> Si una implementación lo rompe, debe justificarse explícitamente.

---

## Objetivo

Mantener el proyecto modular, extensible y escalable.

El objetivo no es solo evitar lógica hardcodeada, sino también evitar que el crecimiento del juego concentre demasiadas responsabilidades en un solo archivo, carpeta o clase.

Una feature nueva debe poder agregarse sin convertir:

* una escena en un “archivo gigante”
* un system en un punto de acumulación
* una carpeta en un depósito caótico de contenido
* un registry en una lista interminable difícil de mantener

---

## Principios base

* Priorizar data-driven sobre hardcode.
* Separar contenido, estado runtime, comportamiento y presentación.
* Evitar que una sola clase concentre demasiadas decisiones.
* Evitar que una sola carpeta concentre cientos de archivos sin estructura.
* Diseñar pensando en crecimiento por categorías, no solo por cantidad.
* Cuando algo crezca mucho, dividir por dominio, familia, zona, tipo o responsabilidad.

---

## Regla principal de escalabilidad

Si una implementación nueva funciona, pero hace que:

* un archivo crezca demasiado
* una carpeta acumule demasiados recursos mezclados
* un registry central tenga demasiadas entradas manuales
* un system empiece a resolver demasiados casos especiales
* una escena empiece a conocer demasiado del juego

entonces esa implementación no está terminada, aunque compile.

---

## Qué problema queremos evitar

No queremos llegar a un proyecto donde exista:

* un `WorldScene.java` con demasiada lógica
* un `WorldSeeder.java` con todos los spawns del juego
* un `PrefabFactory.java` con cientos de `if` o casos especiales
* una carpeta `enemy/` con mil imágenes mezcladas
* una carpeta `sound/` con sonidos sin agrupar
* una carpeta `content/enemies/` con demasiados enemigos sin jerarquía
* un único archivo con todos los IDs, rutas, stats o animaciones

Escalar no es solo “soportar más contenido”.
Escalar es “seguir entendiendo el proyecto cuando haya mucho contenido”.

---

## Regla de modularidad

Cada parte nueva debe entrar en una estructura donde pueda crecer sin empujar todo al mismo lugar.

Preguntas obligatorias antes de agregar algo:

1. ¿Esto es dato editable?
2. ¿Esto es estado runtime?
3. ¿Esto es comportamiento continuo?
4. ¿Esto es visual o asset?
5. ¿Esto pertenece a una familia o categoría?
6. ¿Si existieran 100 variantes de esto, seguiría estando bien ubicado?

Si la respuesta a la última pregunta es “no”, la estructura todavía no es buena.

---

## Dónde debe vivir cada cosa

### Contenido editable

Usar `Swap/res/content` para contenido que idealmente pueda crecer sin reescribir lógica.

Ejemplos:

* player data
* enemy data
* npc data
* diálogos
* definiciones de attacks
* projectiles
* quests
* shops
* loot tables
* spawn definitions

Regla:
Si puede cambiar sin tocar lógica de motor, debe vivir como contenido.

---

### Estado runtime

Usar componentes para estado vivo del juego.

Ejemplos:

* health actual
* posición
* velocidad
* facción
* inventario
* hora del mundo
* estado de quest
* estado de AI

Regla:
Si cambia durante la ejecución y pertenece a una entidad o al mundo, debe vivir en componentes.

---

### Comportamiento continuo

Usar systems para comportamiento sostenido.

Ejemplos:

* movimiento
* combate
* IA
* respawn
* tiempo
* render
* inventario
* proyectiles

Regla:
Si corre continuamente o actualiza entidades en runtime, debe vivir en un system.

---

### Flujo y navegación

Usar escenas para flujo general y entrada a modos del juego.

Ejemplos:

* title
* login
* world
* game over

Regla:
Las escenas orquestan.
No definen reglas profundas de gameplay.

---

### Persistencia

Usar la capa de save para toda la lógica de guardado y carga.

Puntos actuales:

* `SaveManager`
* `SaveReference`
* `SaveSlotMetadata`
* `SaveKind`
* `SaveLoadSystem`
* `SaveDialogs`

Regla:
Nada de paths, slots o nombres hardcodeados fuera de esta capa.

---

## Regla contra archivos gigantes

Un archivo puede crecer, pero no debe convertirse en un “centro del universo”.

Señales de alarma:

* demasiados `if`
* demasiadas rutas hardcodeadas
* demasiadas constantes juntas
* demasiadas responsabilidades mezcladas
* scroll excesivo

Cuando eso pase, dividir por:

* tipo
* dominio
* familia
* responsabilidad
* zona
* feature

---

## Regla contra carpetas planas gigantes

No queremos carpetas con cientos de archivos mezclados.

Ejemplos malos:

* `res/enemy/`
* `res/sound/`
* `res/content/enemies/`
* `src/content/`

Cuando crezca, subdividir.

---

## Organización recomendada de contenido

### Enemigos

Mal:

```text
res/content/enemies/
  slime_green.json
  slime_red.json
  orc_warrior.json
```

Bien:

```text
res/content/enemies/
  slime/
    green.json
    red.json
  orc/
    warrior.json
    pyromancer.json
```

Escalado por zona:

```text
res/content/enemies/
  forest/
    slime/
      green.json
  dungeon/
    skeleton/
      lord.json
```

---

### Sprites de enemigos

Mal:

```text
res/enemy/
  slime_down_1.png
  orc_down_1.png
```

Bien:

```text
res/enemy/
  slime/
    green/
      walk_down_1.png
  orc/
    pyromancer/
      attack_down_1.png
```

---

### Sonidos

Mal:

```text
res/sound/
  coin.wav
  hitmonster.wav
```

Bien:

```text
res/sound/
  ui/
  player/
  enemy/
  world/
  music/
```

---

### NPCs

```text
res/content/npcs/
  town/
    merchant.json

res/npc/
  town/
    merchant/
```

---

### Items

```text
res/content/items/
  weapons/
  armor/
  consumables/

res/objects/
  weapons/
  armor/
```

---

### Mapas

```text
res/content/world/
  world01/
    spawns.json
    npcs.json
  dungeon01/
    loot.json
```

---

## Regla final

Si algo funciona pero:

* concentra demasiada lógica
* mezcla demasiadas cosas
* escala mal

entonces no está bien diseñado.

El proyecto debe crecer en tamaño sin perder orden.
