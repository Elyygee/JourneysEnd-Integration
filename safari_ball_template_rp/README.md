# Custom Safari Ball Resource Pack Template

This resource pack provides assets for custom Great Safari Ball and Golden Safari Ball.

## Structure

```
safari_ball_template_rp/
├── pack.mcmeta                          # Resource pack metadata
├── assets/
│   └── journeysend/
│       ├── bedrock/
│       │   └── poke_balls/
│       │       └── variations/
│       │           ├── great_safari_ball.json      # Maps to Cobblemon's model with our texture
│       │           └── golden_safari_ball.json      # Maps to Cobblemon's model with our texture
│       ├── models/
│       │   └── item/
│       │       ├── great_safari_ball.json      # Item model for Great Safari Ball
│       │       └── golden_safari_ball.json     # Item model for Golden Safari Ball
│       └── textures/
│           ├── gui/
│           │   └── ball/
│           │       ├── great_safari_ball.png   # GUI icon for Great Safari Ball
│           │       └── golden_safari_ball.png  # GUI icon for Golden Safari Ball
│           └── item/
│               └── poke_balls/
│                   ├── great_safari_ball.png   # Inventory icon for Great Safari Ball
│                   ├── golden_safari_ball.png  # Inventory icon for Golden Safari Ball
│                   └── models/
│                       ├── great_safari_ball.png   # 3D model texture for Great Safari Ball
│                       └── golden_safari_ball.png  # 3D model texture for Golden Safari Ball
└── README.md                                # This file
```

## How to Use

**Important**: This resource pack uses Cobblemon's Poke Ball model, only with custom textures. You only need to edit the PNG files.

1. **Customize Textures**
   - Edit the PNG files in your image editor to create custom textures
   - Great Safari Ball could have a blue/green tint
   - Golden Safari Ball could have a gold/yellow tint
   - Files to edit:
     - `assets/journeysend/textures/gui/ball/` - GUI icons (party/Pokémon selection)
     - `assets/journeysend/textures/item/poke_balls/` - Inventory icons (item in inventory)
     - `assets/journeysend/textures/item/poke_balls/models/` - 3D model textures (for thrown entity)

2. **Install the Resource Pack**
   - Place this folder in your server's `resourcepacks` directory
   - Enable it in your server config or client

## Current Model Configuration

The resource pack uses Cobblemon's Poke Ball model with your custom textures:
- Both balls use Cobblemon's standard Poke Ball model (`cobblemon:bedrock/poke_balls/models/poke_ball`)
- Variations file maps the model to your custom textures
- Simply edit the PNG texture files to customize the appearance
- No Blockbench editing needed unless you want custom geometry

## Note

This resource pack works in conjunction with the main mod, which handles:
- Catch rate mechanics
- Usage restrictions (only in Safari dimension)
- HP calculation logic
- Entity rendering

