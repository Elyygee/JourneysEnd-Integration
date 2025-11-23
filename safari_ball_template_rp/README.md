# Safari Ball Resource Pack Template

This resource pack provides models for custom Great Safari Ball and Golden Safari Ball.

## Structure

```
safari_ball_template_rp/
├── pack.mcmeta                          # Resource pack metadata
├── assets/
│   └── journeysend/
│       ├── models/
│       │   └── item/
│       │       ├── great_safari_ball.json          ✓ 2D model (flat icon)
│       │       ├── golden_safari_ball.json         ✓ 2D model (flat icon)
│       │       ├── great_safari_ball_model.json   ✓ 3D model (thrown entity)
│       │       └── golden_safari_ball_model.json   ✓ 3D model (thrown entity)
│       └── textures/
│           └── item/
│               ├── great_safari_ball.png              ⚠ NEED PNG (64x64 flat icon)
│               ├── golden_safari_ball.png              ⚠ NEED PNG (64x64 flat icon)
│               └── poke_balls/
│                   └── models/
│                       ├── great_safari_ball.png       ⚠ NEED PNG (64x32 3D model texture)
│                       └── golden_safari_ball.png       ⚠ NEED PNG (64x32 3D model texture)
└── README.md                              # This file
```

## Texture Requirements

You need to create 4 PNG texture files:

### Flat Icons (64x64 PNG)
These are displayed in the inventory GUI as flat sprites:
- `assets/journeysend/textures/item/great_safari_ball.png`
- `assets/journeysend/textures/item/golden_safari_ball.png`

### 3D Model Textures (64x32 PNG)
These are used for the 3D model when the ball is thrown/held:
- `assets/journeysend/textures/item/poke_balls/models/great_safari_ball.png`
- `assets/journeysend/textures/item/poke_balls/models/golden_safari_ball.png`

The 3D model texture uses a UV map layout:
- Top half (pixels 0-15): Lid/half of the ball
- Bottom half (pixels 16-31): Other half of the ball

## How to Use

1. **Add your PNG textures** to the locations listed above
2. **Place this resource pack** in your server's `resourcepacks` folder
3. **Enable it** in resource pack settings (should load above Cobblemon)

## Notes

- All model files already point to the correct `journeysend` namespace
- Model paths match the mod's expectations
- Texture references use `journeysend:` namespace (not `cobblemon:`)
- The 3D models use the same geometry as Cobblemon's standard Poke Ball

## Model Configuration in Code

The mod uses:
- `journeysend:item/great_safari_ball` - 2D flat model ID
- `journeysend:item/great_safari_ball_model` - 3D model ID

Both are already configured in `ModPokeBalls.java`.