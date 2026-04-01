# RPG Progression

Sistema lineal y legible de progresion para clases RPG.

## Estado actual

- `hero` usa el sprite actual blue-boy y ahora esta marcado como clase `warrior`
- las clases viven en `Swap/res/content/progression/classes`
- las formulas base viven en `Swap/res/content/progression/rules/core.json`
- el calculo runtime vive en `Swap/src/progression/ProgressionCalculator.java`

## Clases iniciales

- `warrior`: tank / sustained melee
- `mage`: burst caster / glass cannon
- `druid`: hybrid / sustain

## Formula de atributos

`Attribute(level) = Base + Growth * (level - 1)`

## Derivados

- `HP = BaseHP + 12*STA + 2*SPI`
- `Mana = BaseMana + 10*INT + 4*SPI`
- `AP = 2*STR + 0.5*AGI`
- `Attack = WeaponPower + AP`
- `AttackSpeed = BaseSpeed + AGI*0.02`
- `DPS = Attack * AttackSpeed`
- `AbP = 2*INT + SPI`
- `DEF = Armor + 1.5*STA + 0.5*STR`
- `HealingPower = 1.2*SPI + 0.8*INT`

## TTK targets

- normal enemy: `4 * player DPS`
- elite enemy: `8 * player DPS`
- boss enemy: `20 * player DPS`

## Vista rapida 1-20

### Warrior

- L1: HP 132, Attack 22.0, DPS 21.6, DEF 21.5
- L10: HP 366, Attack 62.5, DPS 72.5, DEF 57.5
- L20: HP 626, Attack 107.5, DPS 146.2, DEF 97.5

### Mage

- L1: HP 78, Attack 8.0, DPS 7.8, AbP 24.0
- L10: HP 114, Attack 12.5, DPS 14.5, AbP 96.0
- L20: HP 154, Attack 17.5, DPS 23.8, AbP 176.0

### Druid

- L1: HP 104, Attack 14.0, DPS 13.7, Heal 13.2
- L10: HP 248, Attack 36.5, DPS 42.3, Heal 49.2
- L20: HP 408, Attack 61.5, DPS 83.6, Heal 89.2

## Habilidades sugeridas

### Warrior

- `Shield Slam`: 1.3x AP
- `Cleave`: 1.7x AP
- `Last Stand`: defensa temporal

### Mage

- `Fire Bolt`: 1.3x AbP
- `Arcane Burst`: 1.8x AbP
- `Mana Shield`: escudo basado en AbP

### Druid

- `Thorn Strike`: 0.8x AP + 0.6x AbP
- `Rejuvenation`: 1.4x HealingPower
- `Moon Bloom`: 1.25x AbP + heal secundario

## Utilidad

Para revisar los numeros por consola:

```bash
java -cp build_classes:Swap/res debug.ProgressionBalancePreview
```
