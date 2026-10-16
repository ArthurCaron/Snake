import math

import numpy as np
import skia
from PIL import Image

from generate import CELL, EYE_FORWARD, EYE_SIDE, MID, PUPIL_SPRITE, PUPIL_TRAVEL, Canvas, col, fill, kotlin_palette, outlined_text, shade, text_path, tile_fill

PHONE_W = 450
PHONE_H = 975
UNIT = PHONE_W / 900
COLUMNS = 16
ROWS = 20
CELL_PX = PHONE_W / COLUMNS
BOARD_H = CELL_PX * ROWS
BOARD_Y = (PHONE_H - BOARD_H) / 2

SAMPLING = skia.SamplingOptions(skia.FilterMode.kLinear, skia.MipmapMode.kLinear)

SNAKE_GLOW = 3.2
FRUIT_GLOW = 4.5

ANGLES = {"UP": 270, "RIGHT": 180, "DOWN": 90, "LEFT": 0}

DEMO_SNAKE = [(5, 9), (6, 9), (7, 9), (8, 9), (8, 10), (8, 11), (9, 11), (10, 11), (11, 11), (11, 10), (11, 9),
              (11, 8), (11, 7), (10, 7), (9, 7), (8, 7), (7, 7), (6, 7), (5, 7), (4, 7), (4, 6), (4, 5)]
GAME_SNAKE = [(9, 13), (9, 12), (9, 11), (8, 11), (7, 11), (6, 11), (5, 11), (5, 10), (5, 9), (5, 8), (6, 8),
              (7, 8), (8, 8), (9, 8), (10, 8), (11, 8), (11, 7), (11, 6), (11, 5), (10, 5), (9, 5)]


class FileSprite:
    def __init__(self, path):
        self.pixels = np.array(Image.open(path).convert("RGBA"))
        self.height, self.width = self.pixels.shape[:2]
        self._image = skia.Image.fromarray(self.pixels)

    def image(self):
        return self._image


def direction(a, b):
    if b[0] > a[0]:
        return "RIGHT"
    if b[0] < a[0]:
        return "LEFT"
    if b[1] > a[1]:
        return "UP"
    return "DOWN"


def turn_angle(first, second):
    pair = {first, second}
    if pair == {"RIGHT", "DOWN"}:
        return 0
    if pair == {"UP", "RIGHT"}:
        return 90
    if pair == {"LEFT", "UP"}:
        return 180
    return 270


VECTORS = {"RIGHT": (1, 0), "LEFT": (-1, 0), "UP": (0, 1), "DOWN": (0, -1)}
MARK_SIZE = 40 / 64
TURN_MARK_OFFSET = 9.37 / 64
TAIL_MARK_OFFSET = 18 / 64
HEAD_MARK_OFFSET = 7.5 / 64


def segments(cells):
    result = []
    for i, cell in enumerate(cells):
        if i == 0:
            facing = direction(cells[1], cell)
            result.append(dict(cell=cell, name="snakeHead", angle=ANGLES[facing], head=facing))
        elif i == len(cells) - 1:
            toward_head = direction(cell, cells[i - 1])
            result.append(dict(cell=cell, name="snakeTail", angle=ANGLES[toward_head], head=toward_head))
        else:
            toward_head = direction(cell, cells[i - 1])
            toward_tail = direction(cell, cells[i + 1])
            opposite = {toward_head, toward_tail} in ({"LEFT", "RIGHT"}, {"UP", "DOWN"})
            if opposite:
                result.append(dict(cell=cell, name="snakeBodyStraight", angle=ANGLES[toward_head], head=toward_head, tail=toward_tail))
            else:
                result.append(dict(cell=cell, name="snakeBodyTurn", angle=turn_angle(toward_head, toward_tail), head=toward_head,
                                   tail=toward_tail))
    return result


def mark_placement(part):
    x, y = part["cell"][0] + 0.5, part["cell"][1] + 0.5
    hx, hy = VECTORS[part["head"]]
    if part["name"] == "snakeHead":
        return x - hx * HEAD_MARK_OFFSET, y - hy * HEAD_MARK_OFFSET, math.degrees(math.atan2(hy, hx)), 1.0
    if part["name"] == "snakeTail":
        return x + hx * TAIL_MARK_OFFSET, y + hy * TAIL_MARK_OFFSET, math.degrees(math.atan2(hy, hx)), 0.85
    tx, ty = VECTORS[part["tail"]]
    angle = math.degrees(math.atan2(hy - ty, hx - tx))
    if part["name"] == "snakeBodyTurn":
        return x + (hx + tx) * TURN_MARK_OFFSET, y + (hy + ty) * TURN_MARK_OFFSET, angle, 1.0
    return x, y, angle, 1.0


def draw_at(c, image, x, y, angle, size, paint=None):
    sx, sy = x * CELL_PX, BOARD_Y + (ROWS - y) * CELL_PX
    half = size * CELL_PX / 2
    c.save()
    c.translate(sx, sy)
    c.rotate(-angle)
    c.drawImageRect(image, skia.Rect(-half, -half, half, half), SAMPLING, paint)
    c.restore()


def multiply_paint(color_hex):
    r, g, b = int(color_hex[1:3], 16), int(color_hex[3:5], 16), int(color_hex[5:7], 16)
    paint = skia.Paint(AntiAlias=True)
    paint.setColorFilter(skia.ColorFilters.Blend(skia.Color(r, g, b, 255), skia.BlendMode.kModulate))
    return paint


def cell_rect(x, y):
    left = x * CELL_PX
    top = BOARD_Y + (ROWS - 1 - y) * CELL_PX
    return skia.Rect(left, top, left + CELL_PX, top + CELL_PX)


def draw_sprite(c, image, x, y, angle=0.0, offset=(0.0, 0.0), paint=None):
    r = cell_rect(x, y)
    c.save()
    c.translate(r.centerX() + offset[0] * CELL_PX, r.centerY() - offset[1] * CELL_PX)
    c.rotate(-angle)
    half = CELL_PX / 2
    c.drawImageRect(image, skia.Rect(-half, -half, half, half), SAMPLING, paint)
    c.restore()


def tint_paint(color_hex, alpha):
    r, g, b = int(color_hex[1:3], 16), int(color_hex[3:5], 16), int(color_hex[5:7], 16)
    paint = skia.Paint(AntiAlias=True)
    paint.setColorFilter(skia.ColorFilters.Blend(skia.Color(r, g, b, int(alpha * 255)), skia.BlendMode.kSrcIn))
    return paint


def draw_board(c, p, sprites, tile, cells, fruit_cell, dead=False, snake_color=None, fruit_color=None):
    images = {k: v.image() for k, v in sprites.items()}
    palette = kotlin_palette(p)
    snake_color = snake_color or palette["snake"]
    fruit_color = fruit_color or palette["fruits"][0]
    tile_fill(c, tile.image(), 0, BOARD_Y, PHONE_W, BOARD_H, CELL_PX * 8 / tile.width)
    grid = images["gridCell"]
    paint = skia.Paint(AntiAlias=True, Alpha=int(0.10 * 255))
    for x in range(COLUMNS):
        for y in range(ROWS):
            draw_sprite(c, grid, x, y, paint=paint)
    c.drawImageRect(images["boardFrame"], skia.Rect(0, BOARD_Y, PHONE_W, BOARD_Y + BOARD_H), SAMPLING)
    if fruit_cell:
        draw_light(c, images["glow"], fruit_cell, FRUIT_GLOW, fruit_color, palette["fruitGlow"])
        draw_sprite(c, images["fruitBob_0"], *fruit_cell, paint=multiply_paint(fruit_color))
        draw_sprite(c, images["fruitBobDetail_0"], *fruit_cell)
    parts = segments(cells)
    if dead:
        parts[0]["name"] = "snakeHeadDead"
    else:
        for part in parts:
            draw_light(c, images["glow"], part["cell"], SNAKE_GLOW, snake_color, palette["snakeGlow"])
    shadow = tint_paint(shade(p), 0.28)
    for part in parts:
        draw_sprite(c, images[part["name"]], *part["cell"], part["angle"], offset=(0.07, -0.09), paint=shadow)
    body = multiply_paint(snake_color)
    for part in parts:
        draw_sprite(c, images[part["name"]], *part["cell"], part["angle"], paint=body)
    if not dead:
        marks = multiply_paint(palette["marks"])
        shape = images["mark" + palette["markShape"].capitalize()]
        for part in parts:
            x, y, angle, scale = mark_placement(part)
            image = images["snakeHeadMark"] if part["name"] == "snakeHead" else shape
            draw_at(c, image, x, y, angle, MARK_SIZE * scale, marks)
    for part in parts:
        draw_sprite(c, images[part["name"] + "Detail"], *part["cell"], part["angle"])
    if not dead and fruit_cell:
        draw_pupils(c, images["snakePupil"], parts[0]["cell"], parts[0]["angle"], fruit_cell)


def draw_light(c, image, cell, size, color_hex, alpha):
    if alpha <= 0:
        return
    h = color_hex.lstrip("#")
    r, g, b = (int(h[i:i + 2], 16) for i in (0, 2, 4))
    a = int(alpha * 255)
    paint = skia.Paint(AntiAlias=True, BlendMode=skia.BlendMode.kPlus)
    paint.setColorFilter(skia.ColorFilters.Blend(skia.Color(r, g, b, a), skia.BlendMode.kSrcIn))
    rect = cell_rect(*cell)
    half = size * CELL_PX / 2
    c.drawImageRect(image, skia.Rect(rect.centerX() - half, rect.centerY() - half, rect.centerX() + half, rect.centerY() + half),
                    SAMPLING, paint)


def draw_pupils(c, image, head, angle, fruit_cell):
    cos, sin = math.cos(math.radians(angle)), math.sin(math.radians(angle))
    center_x, center_y = head[0] + 0.5, head[1] + 0.5
    forward = (EYE_FORWARD - MID) / CELL
    for side in (-1, 1):
        lateral = side * EYE_SIDE / CELL
        eye_x = center_x + forward * cos - lateral * sin
        eye_y = center_y + forward * sin + lateral * cos
        dx, dy = fruit_cell[0] + 0.5 - eye_x, fruit_cell[1] + 0.5 - eye_y
        distance = math.hypot(dx, dy)
        travel = PUPIL_TRAVEL / CELL
        k = travel / distance if distance > travel else 1.0
        x, y = eye_x + dx * k, eye_y + dy * k
        half = PUPIL_SPRITE / CELL / 2 * CELL_PX
        sx, sy = x * CELL_PX, BOARD_Y + (ROWS - y) * CELL_PX
        c.drawImageRect(image, skia.Rect(sx - half, sy - half, sx + half, sy + half), SAMPLING)


def nine(c, image_path_img, rect):
    img = image_path_img
    w, h = img.width(), img.height()
    split = 24 if w < 90 else 32
    k = UNIT
    sx = [0, split, w - split, w]
    sy = [0, split, h - split, h]
    dx = [rect.left(), rect.left() + split * k, rect.right() - split * k, rect.right()]
    dy = [rect.top(), rect.top() + split * k, rect.bottom() - split * k, rect.bottom()]
    for i in range(3):
        for j in range(3):
            src = skia.Rect(sx[i], sy[j], sx[i + 1], sy[j + 1])
            dst = skia.Rect(dx[i], dy[j], dx[i + 1], dy[j + 1])
            c.drawImageRect(img, src, dst, SAMPLING, None, skia.Canvas.kStrict_SrcRectConstraint)


def load_nine(path):
    arr = np.array(Image.open(path).convert("RGBA"))[1:-1, 1:-1].copy()
    return skia.Image.fromarray(arr)


def button(c, p, skin, label, cx, cy, size=36, primary=False, min_width=0):
    advance = text_path(label, size * UNIT)[1]
    pad_x, pad_y = (72 if primary else 24) * UNIT, (24 if primary else 8) * UNIT
    width = max(advance + pad_x * 2, min_width)
    height = size * UNIT * 1.05 + pad_y * 2 + 10 * UNIT
    r = skia.Rect(cx - width / 2, cy - height / 2, cx + width / 2, cy + height / 2)
    nine(c, skin["buttonPrimary" if primary else "button"], r)
    outlined_text(c, label, cx, cy + size * UNIT * 0.32 - 4 * UNIT, size * UNIT, p, align="center")
    return r


def icon_button(c, skin, icon, right, top, size=80):
    s = size * UNIT
    r = skia.Rect(right - s, top, right, top + s)
    nine(c, skin["button"], r)
    inset = s * 0.18
    c.drawImageRect(icon, skia.Rect(r.left() + inset, r.top() + inset * 0.8, r.right() - inset, r.bottom() - inset * 1.2), SAMPLING)


def grounds_sheet(folder, names, cell=128):
    from PIL import ImageDraw
    from generate import GROUNDS
    files = ["ground", *GROUNDS]
    label_h, gap = 22, 8
    width = len(files) * (cell + gap) + 120
    height = label_h + len(names) * (cell + gap)
    sheet = Image.new("RGB", (width, height), (32, 32, 32))
    draw = ImageDraw.Draw(sheet)
    for i, name in enumerate(files):
        draw.text((120 + i * (cell + gap), 4), "checker" if name == "ground" else name[len("ground"):].lower(), fill=(230, 230, 230))
    for j, style in enumerate(names):
        y = label_h + j * (cell + gap)
        draw.text((6, y + cell // 2), style, fill=(230, 230, 230))
        for i, name in enumerate(files):
            tile = Image.open(folder / style / "textures/game/skin" / f"{name}.png").convert("RGB").resize((cell // 2, cell // 2), Image.LANCZOS)
            x = 120 + i * (cell + gap)
            for dx in (0, cell // 2):
                for dy in (0, cell // 2):
                    sheet.paste(tile, (x + dx, y + dy))
    return sheet


def phone(p, tile):
    cv = Canvas(int(PHONE_W), PHONE_H)
    c = cv.c
    c.clear(col(p["clear"]))
    tile_size = CELL_PX * 8
    start = BOARD_Y - tile_size * 2
    c.saveLayer()
    tile_fill(c, tile.image(), 0, start, PHONE_W, PHONE_H - start, tile_size / tile.width)
    c.drawRect(skia.Rect(0, 0, PHONE_W, PHONE_H), skia.Paint(Color=col(kotlin_palette(p)["surround"]), BlendMode=skia.BlendMode.kModulate))
    c.restore()
    return cv


def dim(c, p, alpha):
    c.drawRect(skia.Rect(0, 0, PHONE_W, PHONE_H), fill(col(shade(p), alpha)))


def menu_screen(p, sprites, tile, logo_cv, skin):
    cv = phone(p, tile)
    c = cv.c
    palette = kotlin_palette(p)
    fruits = palette["fruits"]
    snake = fruits[2 % len(fruits)] if palette["snakeTakesFruitColor"] else None
    draw_board(c, p, sprites, tile, DEMO_SNAKE, (13, 12), snake_color=snake, fruit_color=fruits[3 % len(fruits)])
    dim(c, p, 0.35)
    logo_w = PHONE_W * 0.78
    logo_h = logo_w * logo_cv.height / logo_cv.width
    c.drawImageRect(logo_cv.image(), skia.Rect((PHONE_W - logo_w) / 2, 190, (PHONE_W + logo_w) / 2, 190 + logo_h), SAMPLING)
    button(c, p, skin, "Jouer !", PHONE_W / 2, 520, size=60, primary=True)
    outlined_text(c, "Record : 42", PHONE_W / 2, 625, 36 * UNIT, p, align="center")
    button(c, p, skin, "Meilleurs scores", PHONE_W * 0.3, 700)
    button(c, p, skin, "Paramètres", PHONE_W * 0.73, 700)
    icon_button(c, skin, sprites["iconSoundOn"].image(), PHONE_W - 12, 12)
    icon_button(c, skin, sprites["iconMusicOn"].image(), PHONE_W - 12, 12 + 92 * UNIT)
    return cv


def game_screen(p, sprites, tile, skin):
    cv = phone(p, tile)
    c = cv.c
    draw_board(c, p, sprites, tile, GAME_SNAKE, (3, 14))
    outlined_text(c, "Score : 21", 10, 62, 36 * UNIT, p)
    outlined_text(c, "Record : 42", PHONE_W / 2, 62, 36 * UNIT, p, align="center")
    icon_button(c, skin, sprites["iconPause"].image(), PHONE_W - 10, 22)
    return cv


def panel_screen(p, sprites, tile, skin):
    cv = phone(p, tile)
    c = cv.c
    palette = kotlin_palette(p)
    fruits = palette["fruits"]
    snake = fruits[3 % len(fruits)] if palette["snakeTakesFruitColor"] else None
    draw_board(c, p, sprites, tile, GAME_SNAKE, None, dead=True, snake_color=snake)
    dim(c, p, 0.55)
    r = skia.Rect(PHONE_W * 0.14, 300, PHONE_W * 0.86, 690)
    nine(c, skin["panel"], r)
    outlined_text(c, "Perdu !", PHONE_W / 2, 380, 60 * UNIT, p, align="center")
    outlined_text(c, "Score : 21", PHONE_W / 2, 430, 36 * UNIT, p, align="center")
    outlined_text(c, "Record : 42", PHONE_W / 2, 470, 36 * UNIT, p, align="center")
    outlined_text(c, "Nouveau record !", PHONE_W / 2, 510, 36 * UNIT, p, align="center", color=p["primary_light"])
    width = r.width() - 64 * UNIT * 2
    button(c, p, skin, "Rejouer", PHONE_W / 2, 570, min_width=width)
    button(c, p, skin, "Menu principal", PHONE_W / 2, 635, min_width=width)
    return cv


def render(p, out):
    skin_dir = out / "textures/game/skin"
    sprites = {path.stem: FileSprite(path) for path in skin_dir.glob("*.png") if not path.stem.endswith(".9")}
    tile = sprites["ground"]
    logo_cv = sprites["logo"]
    skin = {name: load_nine(skin_dir / f"{name}.9.png") for name in ("button", "buttonPrimary", "panel")}
    screens = [menu_screen(p, sprites, tile, logo_cv, skin), game_screen(p, sprites, tile, skin),
               panel_screen(p, sprites, tile, skin)]
    gap = 24
    sheet = Image.new("RGBA", (int(PHONE_W) * 3 + gap * 4, PHONE_H + gap * 2 + 40), (30, 26, 28, 255))
    for i, screen in enumerate(screens):
        sheet.paste(screen.pil(), (gap + i * (int(PHONE_W) + gap), gap + 40))
    label = Canvas(sheet.width, 60)
    outlined_text(label.c, p["label"], gap, 46, 34, p)
    sheet.alpha_composite(label.pil(), (0, 0))
    return sheet


def overview(folder, names):
    gap = 24
    columns = 4
    sheets = [(name, Image.open(folder / f"{name}.png")) for name in names if (folder / f"{name}.png").exists()]
    rows = (len(sheets) + columns - 1) // columns
    header = gap + 40
    cell_w, cell_h = int(PHONE_W) + gap, PHONE_H + header + gap
    result = Image.new("RGBA", (columns * cell_w + gap, rows * cell_h + gap), (30, 26, 28, 255))
    for i, (name, sheet) in enumerate(sheets):
        x = gap + (i % columns) * cell_w
        y = gap + (i // columns) * cell_h
        result.paste(sheet.crop((gap, 0, int(PHONE_W) + gap, header)), (x, y))
        phone_left = gap + int(PHONE_W) + gap
        result.paste(sheet.crop((phone_left, header, phone_left + int(PHONE_W), header + PHONE_H)), (x, y + header))
    return result


MARK_SNAKE = [(9, 10), (8, 10), (7, 10), (6, 10), (6, 9), (7, 9), (8, 9), (9, 9), (10, 9)]


def marks_sheet(styles_folder, names):
    from generate import MARK_SHAPES, PALETTES
    crop_top, crop_bottom = BOARD_Y + (ROWS - 12) * CELL_PX, BOARD_Y + (ROWS - 7) * CELL_PX
    crop_left, crop_right = 4 * CELL_PX, 13 * CELL_PX
    width, height = int(crop_right - crop_left), int(crop_bottom - crop_top)
    gap = 16
    sheet = Image.new("RGBA", (gap + len(MARK_SHAPES) * (width + gap), len(names) * (height + gap + 40) + gap), (30, 26, 28, 255))
    for row, name in enumerate(names):
        folder = styles_folder / name / "textures/game/skin"
        sprites = {path.stem: FileSprite(path) for path in folder.glob("*.png") if not path.stem.endswith(".9")}
        for column, shape in enumerate(MARK_SHAPES):
            p = dict(PALETTES[name], mark_shape=shape)
            cv = phone(p, sprites["ground"])
            draw_board(cv.c, p, sprites, sprites["ground"], MARK_SNAKE, (12, 10))
            x = gap + column * (width + gap)
            y = gap + row * (height + gap + 40)
            label = Canvas(width, 40)
            outlined_text(label.c, f"{name} - {shape}", 4, 30, 20, PALETTES["desert"])
            sheet.alpha_composite(label.pil(), (x, y))
            sheet.paste(cv.pil().crop((int(crop_left), int(crop_top), int(crop_right), int(crop_bottom))), (x, y + 40))
    return sheet

