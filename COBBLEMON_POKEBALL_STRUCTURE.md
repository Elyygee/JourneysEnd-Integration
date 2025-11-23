# Cobblemon PokeBall Structure Reference

This document shows the exact structure Cobblemon uses for PokeBall models, textures, and animations.

## File Structure Overview

```
assets/cobblemon/
├── models/item/
│   ├── great_ball.json              # 2D inventory icon
│   └── great_ball_model.json        # 3D hand-held model
├── textures/item/poke_balls/
│   ├── great_ball.png               # 2D inventory icon texture (64x64)
│   └── models/
│       └── great_ball.png           # 3D hand-held model texture (64x32)
├── textures/poke_balls/
│   └── great_ball.png               # Throwing animation texture (64x32)
└── bedrock/poke_balls/
    ├── models/
    │   └── poke_ball.geo.json        # Base geometry (shared by all balls)
    ├── animations/
    │   └── poke_ball.animation.json  # Base animations (shared by all balls)
    └── variations/
        └── 0_great_ball_base.json     # Variation definition for throwing
```

## 1. 2D Inventory Model (GUI/Inventory)

**File:** `assets/cobblemon/models/item/great_ball.json`

```json
{
  "parent": "minecraft:item/generated",
  "textures": {
    "layer0": "cobblemon:item/poke_balls/great_ball"
  }
}
```

**Texture:** `assets/cobblemon/textures/item/poke_balls/great_ball.png`
- Size: 64x64 pixels
- Flat icon for inventory/hotbar/GUI

## 2. 3D Hand-Held Model (In Hand)

**File:** `assets/cobblemon/models/item/great_ball_model.json`

```json
{
  "parent": "cobblemon:item/poke_ball_model",
  "textures": {
    "layer0": "cobblemon:item/poke_balls/models/great_ball"
  }
}
```

**Texture:** `assets/cobblemon/textures/item/poke_balls/models/great_ball.png`
- Size: 64x32 pixels
- 3D model texture for when held in hand

**Parent Model:** `assets/cobblemon/models/item/poke_ball_model.json`
- Contains the full 3D geometry with elements
- All balls inherit this structure and just override the texture

## 3. Throwing Animation (Bedrock Model)

### Variation File

**File:** `assets/cobblemon/bedrock/poke_balls/variations/0_great_ball_base.json`

```json
{
  "pokeball": "cobblemon:great_ball",
  "order": 0,
  "variations": [
    {
      "aspects": [],
      "poser": "cobblemon:poke_ball",
      "model": "cobblemon:poke_ball.geo",
      "texture": "cobblemon:textures/poke_balls/great_ball.png",
      "layers": []
    }
  ]
}
```

**Key Points:**
- `pokeball`: The PokeBall identifier (matches the PokeBall registration)
- `order`: Priority order (0 = base/default)
- `poser`: Always `cobblemon:poke_ball` (shared poser)
- `model`: Always `cobblemon:poke_ball.geo` (shared geometry)
- `texture`: `cobblemon:textures/poke_balls/great_ball.png` (NOT in `item/poke_balls/`)

**Texture:** `assets/cobblemon/textures/poke_balls/great_ball.png`
- Size: 64x32 pixels
- Used for throwing animation
- **Different path** from hand-held model texture!

### Base Files (Shared)

**Geometry:** `assets/cobblemon/bedrock/poke_balls/models/poke_ball.geo.json`
- Shared by all PokeBalls
- Contains the 3D mesh structure

**Animations:** `assets/cobblemon/bedrock/poke_balls/animations/poke_ball.animation.json`
- Shared by all PokeBalls
- Contains opening/closing animations

## Summary of Texture Paths

| Usage | Texture Path | Size |
|-------|-------------|------|
| 2D Inventory Icon | `cobblemon:item/poke_balls/great_ball` | 64x64 |
| 3D Hand-Held Model | `cobblemon:item/poke_balls/models/great_ball` | 64x32 |
| Throwing Animation | `cobblemon:textures/poke_balls/great_ball.png` | 64x32 |

**Important:** The throwing animation texture is in `textures/poke_balls/` NOT `textures/item/poke_balls/models/`!

## Code Registration

In `ModPokeBalls.java`:

```java
new PokeBall(
    StarAcademyMod.id("great_safari_ball"),  // PokeBall identifier
    modifier,
    effects,
    waterDragValue,
    StarAcademyMod.id("item/great_safari_ball"),      // 2D model (inventory)
    new Identifier("cobblemon", "item/great_safari_ball_model"),  // 3D model (hand)
    throwPower,
    false
);
```

The 3D model uses `cobblemon` namespace to match Cobblemon's structure.

