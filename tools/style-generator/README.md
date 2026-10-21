# Style generator

The game assets are fixed files: the sprites in `sourceAssets/textures/game/skin`, the splash and loading images, the icons, and the style classes in `core/src/main/kotlin/fr/mwet/snake/render`. The game uses `desert`.

This generator draws the same assets in code, one folder per style, in `tools/style-generator/out` (ignored by git). It never writes into the game. To use a generated asset, copy it by hand.

| Style | Look |
|---|---|
| `desert` | Cartoon cell shading: thick dark outlines, sand ground, teal snake. Used by the game. |
| `oasis` | Same style, green floor, orange snake, mango. |
| `tundra` | Same style, snow and ice, purple snake, red berry. |
| `volcano` | Same style, basalt and lava colors, yellow snake, ice fruit. |
| `dusk` | Same style, purple dunes at night, orange snake, turquoise fruit. |
| `lagoon` | Same style, sand and sea colors, coral snake, yellow fruit. |
| `neon` | Other style: glowing outlines on a night background. |
| `gameboy` | Other style: 4-tone green pixel art. Pair it with a pixel font in Settings. |
| `laser` | Other style: white-hot cyan and pink laser lines, light pools on the floor. |
| `laser-sunset` | Same as `laser`, orange snake and cyan fruit. |

`out/previews/<style>.png` shows the menu, a game and the end screen of each style. `previews/all.png` puts every style side by side. `previews/marks.png` shows the back marks. `previews/fruits.png` shows the ten fruits of every style.

## Generate the styles

```bash
python3 -m venv .venv && .venv/bin/pip install -r tools/style-generator/requirements.txt
.venv/bin/python tools/style-generator/generate.py oasis
```

Without a name, the script generates every style. Each `out/<style>` folder contains:

| Generated | Copy to |
|---|---|
| `textures/game/skin` | `sourceAssets/textures/game/skin` |
| `root/SplashScreen.png`, `root/LoadingAssets.png` | `sourceAssets/root` |
| `icons/icon16.png` to `icons/icon128.png` | `lwjgl3/src/main/resources` |
| `icons/drawable-*/ic_launcher.png` | `android/res/drawable-*` |
| `icons/logo.png`, `logo.ico`, `logo.icns` | `lwjgl3/icons` |
| `icons/favicon.ico` | `sourceAssets/web` |
| `kotlin/StyleColors.kt`, `kotlin/SnakeLook.kt`, `kotlin/FruitLook.kt` | `core/src/main/kotlin/fr/mwet/snake/render`, each file replaced as a whole |

Laser styles set `SnakeLook.glow` and `FruitLook.glow`: the game then draws an additive light under the snake and the fruit. The other styles set them to `0`.

The game reads the style classes through `Palette.kt`, one color per usage. `Palette.kt` is not generated: replacing the style classes keeps every usage.

## Change a style

Colors live in `tools/style-generator/palettes.py`. Add an entry to create a new environment, then run:

```bash
.venv/bin/python tools/style-generator/generate.py <name>
```

Without a name, the script regenerates every style.

## Colors and marks

The snake and the fruit are drawn in two layers: a gray layer that the game tints, and a fixed layer (outline, eyes, leaves, laser cores).
So colors and marks change without new images. Edit the style classes to try a value:

| Kotlin | `palettes.py` | Effect |
|---|---|---|
| `SnakeLook.body` | `snake_tint` | Snake color at the start of a game. |
| `SnakeLook.marks` | `mark_color` | Color of the marks on the back, independent from the snake. |
| `SnakeLook.markShape` | `mark_shape` | `Diamond`, `Circle`, `Square`, `Star`, `Chevron` or `Heart`. |
| `FruitLook.fruits` | `fruit_colors` | Fruits that can appear, as `FruitStyle(kind, color)`. Each new fruit is a random entry of the list. Kinds: `Apple`, `Strawberry`, `Cherry`, `Banana`, `Grapes`, `Orange`, `Pear`, `Lemon`, `Watermelon`, `Prickly`. Without `fruit_colors`, each kind keeps its natural color; with it, the colors are spread over the kinds. |
| `SnakeLook.takesFruitColor` | `snake_takes_fruit_color` | The snake takes the color of the fruit it eats, from the head to the tail. |
| `SnakeLook.glow`, `FruitLook.glow` | `snake_glow`, `fruit_glow` | Strength of the light on the floor; `0` turns it off. |

