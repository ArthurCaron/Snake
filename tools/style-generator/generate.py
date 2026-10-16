#!/usr/bin/env python3
import argparse
import json
import math
import random
import shutil
from pathlib import Path

import numpy as np
import skia
from PIL import Image, ImageFilter
from scipy import ndimage

from palettes import PALETTES

ROOT = Path(__file__).resolve().parents[2]
FONT_FILE = ROOT / "sourceAssets/fonts/lilitaone/LilitaOne-Regular.ttf"
CELL = 64
MID = CELL / 2
BODY = 19
OUT = 4.0
TILE_CELLS = 8
EYE_FORWARD = 22
EYE_SIDE = 14.5
EYE_RADIUS = 9
PUPIL_RADIUS = 4.4
PUPIL_TRAVEL = 3.6
PUPIL_SPRITE = 16
TYPEFACE = skia.Typeface.MakeFromFile(str(FONT_FILE))


def rgb(hex_color):
    h = hex_color.lstrip("#")
    return int(h[0:2], 16), int(h[2:4], 16), int(h[4:6], 16)


CLEAR = "clear"


def col(hex_color, alpha=1.0):
    if hex_color == CLEAR:
        return skia.ColorTRANSPARENT
    r, g, b = rgb(hex_color)
    h = hex_color.lstrip("#")
    if len(h) == 8:
        alpha *= int(h[6:8], 16) / 255
    return skia.Color(r, g, b, int(round(alpha * 255)))


def luminance(hex_color):
    r, g, b = rgb(hex_color)
    return (0.299 * r + 0.587 * g + 0.114 * b) / 255


def gray(hex_color, tint):
    value = int(min(1.0, luminance(hex_color) / max(luminance(tint), 1e-3)) * 255)
    return "#%02x%02x%02x" % (value, value, value)


def tint(p, part):
    return p.get(f"{part}_tint", p[part])


FRUIT_DETAIL_COLORS = dict(stem="#6b4a2a", rind="#4f9a46", rind_dark="#2d6233", pip="#2a1a18")
TINTED_KEYS = dict(snake=("snake", "snake_dark"), fruit=("fruit", "fruit_dark"))
FIXED_KEYS = dict(snake=("eye", "pupil", "tongue", "snake_light"),
                  fruit=("seed", "leaf", "leaf_dark", "fruit_shadow", "fruit_light", "stem", "rind", "rind_dark", "pip"))
HIGHLIGHTS = dict(snake=("snake_light", "#ffffff80"), fruit=("fruit_light", "#ffffffc0"))


def layer(p, part, kind):
    q = dict(p, tongue=p.get("tongue", "#e0475a"), fruit_shadow=p.get("fruit_shadow", shade(p)))
    q.update({key: p.get(key, value) for key, value in FRUIT_DETAIL_COLORS.items()})
    color = tint(p, part)
    for key in TINTED_KEYS[part]:
        q[key] = gray(p[key], color) if kind == "base" else CLEAR
    if kind == "base":
        for key in FIXED_KEYS[part]:
            q[key] = CLEAR
    else:
        key, highlight = HIGHLIGHTS[part]
        q[key] = highlight
    if p.get("tint_outline"):
        q["outline"] = "#ffffff" if kind == "base" else CLEAR
    elif kind == "base":
        q["outline"] = CLEAR
    return q


def with_line(p, group):
    return dict(p, outline=p.get(f"line_{group}", p["outline"]))


def shade(p):
    return p.get("shade", p["outline"])


def surround(p):
    r, g, b = rgb(p["clear"])
    return "#%02x%02x%02x" % tuple(int(v + (255 - v) * 0.35) for v in (r, g, b))


def erasing(paint):
    if skia.ColorGetA(paint.getColor()) == 0:
        paint.setBlendMode(skia.BlendMode.kClear)
    return paint


def fill(color):
    return erasing(skia.Paint(AntiAlias=True, Color=color))


def stroke(color, width):
    return erasing(skia.Paint(
        AntiAlias=True,
        Color=color,
        Style=skia.Paint.kStroke_Style,
        StrokeWidth=width,
        StrokeJoin=skia.Paint.kRound_Join,
        StrokeCap=skia.Paint.kRound_Cap,
    ))


def union(*paths):
    result = paths[0]
    for path in paths[1:]:
        result = skia.Op(result, path, skia.PathOp.kUnion_PathOp)
    return result


def oval(left, top, right, bottom):
    path = skia.Path()
    path.addOval(skia.Rect(left, top, right, bottom))
    return path


def circle(x, y, r):
    return oval(x - r, y - r, x + r, y + r)


def rect(left, top, right, bottom, radius=0.0):
    path = skia.Path()
    path.addRRect(skia.RRect.MakeRectXY(skia.Rect(left, top, right, bottom), radius, radius))
    return path


class Canvas:
    def __init__(self, width, height):
        self.width = width
        self.height = height
        self.surface = skia.Surface(width, height)
        self.c = self.surface.getCanvas()
        self.c.clear(skia.ColorTRANSPARENT)

    def array(self):
        image = self.surface.makeImageSnapshot()
        return image.toarray(colorType=skia.kRGBA_8888_ColorType, alphaType=skia.kUnpremul_AlphaType)

    def pil(self):
        return Image.fromarray(self.array(), "RGBA")

    def image(self):
        return self.surface.makeImageSnapshot()


def outlined(c, path, base, p, width=OUT, rim=None, rim_width=0.0):
    c.drawPath(path, stroke(col(p["outline"]), width * 2))
    c.drawPath(path, fill(col(base)))
    if rim:
        c.save()
        c.clipPath(path, doAntiAlias=True)
        c.drawPath(path, stroke(col(rim), rim_width * 2))
        c.restore()


def diamond(cx, cy, rx, ry):
    path = skia.Path()
    path.moveTo(cx - rx, cy)
    path.lineTo(cx, cy - ry)
    path.lineTo(cx + rx, cy)
    path.lineTo(cx, cy + ry)
    path.close()
    return path


def body_highlight(c, p, path):
    c.save()
    c.clipPath(path, doAntiAlias=True)
    c.drawPath(path, stroke(col(p["snake_dark"]), 12))
    c.restore()


def side_stripes(c, p, path, stripes):
    c.save()
    c.clipPath(path, doAntiAlias=True)
    for stripe in stripes:
        c.drawPath(stripe, fill(col(p["snake_light"], 0.6)))
    c.restore()


def straight_stripes(right=CELL + 12):
    return [rect(-12, MID - 11, right, MID - 8.5), rect(-12, MID + 8.5, right, MID + 11)]


def snake_body_straight(p):
    cv = Canvas(CELL, CELL)
    c = cv.c
    path = rect(-12, MID - BODY, CELL + 12, MID + BODY)
    outlined(c, path, p["snake"], p)
    body_highlight(c, p, path)
    side_stripes(c, p, path, straight_stripes())
    return cv


def snake_body_turn(p):
    cv = Canvas(CELL, CELL)
    c = cv.c
    path = skia.Path()
    path.setFillType(skia.PathFillType.kEvenOdd)
    path.addCircle(CELL, CELL, MID + BODY)
    path.addCircle(CELL, CELL, MID - BODY)
    outlined(c, path, p["snake"], p)
    body_highlight(c, p, path)
    c.save()
    c.clipPath(path, doAntiAlias=True)
    for radius in (MID - 9.75, MID + 9.75):
        arc = skia.Path()
        arc.addCircle(CELL, CELL, radius)
        c.drawPath(arc, stroke(col(p["snake_light"], 0.6), 2.5))
    c.restore()
    return cv


def snake_tail(p):
    cv = Canvas(CELL, CELL)
    c = cv.c
    path = skia.Path()
    path.moveTo(-12, MID - BODY)
    path.lineTo(14, MID - BODY)
    path.cubicTo(34, MID - BODY, 48, MID - 8, 58, MID)
    path.cubicTo(48, MID + 8, 34, MID + BODY, 14, MID + BODY)
    path.lineTo(-12, MID + BODY)
    path.close()
    outlined(c, path, p["snake"], p)
    body_highlight(c, p, path)
    side_stripes(c, p, path, straight_stripes(right=30))
    return cv


MARK_SPRITE = 40
MARK_SHAPES = ("diamond", "circle", "square", "star", "chevron", "heart")


def chevron(r):
    path = skia.Path()
    path.moveTo(r * 0.8, 0)
    path.lineTo(-r * 0.3, -r)
    path.lineTo(-r * 0.8, -r * 0.65)
    path.lineTo(0, 0)
    path.lineTo(-r * 0.8, r * 0.65)
    path.lineTo(-r * 0.3, r)
    path.close()
    return path


def mark_path(shape, r=11.0):
    if shape == "diamond":
        return diamond(0, 0, r, r)
    if shape == "circle":
        return circle(0, 0, r * 0.82)
    if shape == "square":
        return rect(-r * 0.72, -r * 0.72, r * 0.72, r * 0.72, 3)
    if shape == "chevron":
        return chevron(r)
    path = skia.Path()
    if shape == "star":
        for i in range(10):
            radius = r * 1.05 if i % 2 == 0 else r * 0.45
            angle = math.pi * i / 5
            point = (math.cos(angle) * radius, math.sin(angle) * radius)
            path.moveTo(*point) if i == 0 else path.lineTo(*point)
        path.close()
        return path
    path.moveTo(r, 0)
    path.cubicTo(r * 0.2, -r * 0.55, -r * 0.2, -r * 1.1, -r * 0.7, -r * 0.75)
    path.cubicTo(-r * 1.15, -r * 0.4, -r * 0.95, 0, -r * 0.55, 0)
    path.cubicTo(-r * 0.95, 0, -r * 1.15, r * 0.4, -r * 0.7, r * 0.75)
    path.cubicTo(-r * 0.2, r * 1.1, r * 0.2, r * 0.55, r, 0)
    path.close()
    return path


def mark_sprite(p, path):
    cv = Canvas(MARK_SPRITE, MARK_SPRITE)
    c = cv.c
    c.translate(MARK_SPRITE / 2, MARK_SPRITE / 2)
    if p.get("mark_style") == "tube":
        c.drawPath(path, stroke(col("#ffffff"), 3.5))
        return cv
    c.drawPath(path, stroke(col(p.get("mark_outline", p["outline"]), 0.9), 7))
    c.drawPath(path, stroke(col("#ffffff"), 3.5))
    c.drawPath(path, fill(col("#ffffff")))
    return cv


def head_shape(front):
    return union(rect(30, MID - BODY, CELL + 12, MID + BODY), oval(front, MID - 25, 54, MID + 25))


def pupil(c, p, x, y):
    c.drawPath(circle(x, y, PUPIL_RADIUS), fill(col(p["pupil"])))
    c.drawPath(circle(x - 1.5, y - 1.7, 1.5), fill(col("#ffffff")))


def snake_pupil(p):
    cv = Canvas(PUPIL_SPRITE, PUPIL_SPRITE)
    pupil(cv.c, p, PUPIL_SPRITE / 2, PUPIL_SPRITE / 2)
    return cv


def snake_head(p, dead=False, look=None):
    cv = Canvas(CELL, CELL)
    c = cv.c
    front = 9 if dead else 4
    if dead:
        tongue = skia.Path()
        tongue.moveTo(front + 6, MID)
        tongue.lineTo(2.5, MID)
        tongue.moveTo(5, MID)
        tongue.lineTo(1, MID - 4)
        tongue.moveTo(5, MID)
        tongue.lineTo(1, MID + 4)
        c.drawPath(tongue, stroke(col(p["outline"]), 4.5))
        c.drawPath(tongue, stroke(col(p["tongue"]), 2))
    path = head_shape(front)
    outlined(c, path, p["snake"], p)
    body_highlight(c, p, path)
    c.save()
    c.clipPath(path, doAntiAlias=True)
    c.drawPath(rect(40, MID - 11, CELL + 12, MID - 8.5), fill(col(p["snake_light"], 0.6)))
    c.drawPath(rect(40, MID + 8.5, CELL + 12, MID + 11), fill(col(p["snake_light"], 0.6)))
    c.restore()
    for side in (-1, 1):
        ex, ey = front + EYE_FORWARD - 4, MID + side * EYE_SIDE
        if dead:
            c.drawPath(circle(ex, ey, 8), stroke(col(p["outline"]), 6))
            c.drawPath(circle(ex, ey, 8), fill(col(p["eye"])))
            cross = skia.Path()
            cross.moveTo(ex - 3.5, ey - 3.5)
            cross.lineTo(ex + 3.5, ey + 3.5)
            cross.moveTo(ex + 3.5, ey - 3.5)
            cross.lineTo(ex - 3.5, ey + 3.5)
            c.drawPath(cross, stroke(col(p["pupil"]), 2.6))
        else:
            c.drawPath(circle(ex, ey, EYE_RADIUS), stroke(col(p["outline"]), 6))
            c.drawPath(circle(ex, ey, EYE_RADIUS), fill(col(p["eye"])))
            if look:
                pupil(c, p, ex + look[0], ey + look[1])
        c.drawPath(circle(front + 5, MID + side * 4.5, 1.6), fill(col(p["outline"])))
    return cv


def leaf_path(x, y, length, width, angle):
    path = skia.Path()
    path.moveTo(0, 0)
    path.cubicTo(length * 0.3, -width, length * 0.75, -width, length, 0)
    path.cubicTo(length * 0.75, width, length * 0.3, width, 0, 0)
    path.close()
    matrix = skia.Matrix()
    matrix.setRotate(angle)
    matrix.postTranslate(x, y)
    path.transform(matrix)
    return path


FRUIT_KINDS = ("apple", "strawberry", "cherry", "banana", "grapes", "orange", "pear", "lemon", "watermelon", "prickly")
NATURAL_FRUIT_COLORS = dict(apple="#e0443a", strawberry="#e8344a", cherry="#c0203f", banana="#f2cf3a", grapes="#8a4aa8",
                            orange="#f28a1f", pear="#a8c43a", lemon="#f5e04a", watermelon="#f0485a", prickly="#d8467b")


def stem_and_leaf(c, p, x, y, leaf_angle=-25, leaf_length=15):
    stem = skia.Path()
    stem.moveTo(x, y + 6)
    stem.quadTo(x + 1, y - 1, x + 4, y - 5)
    c.drawPath(stem, stroke(col(p["outline"]), 6))
    c.drawPath(stem, stroke(col(p["stem"]), 2.5))
    leaf = leaf_path(x + 3, y - 1, leaf_length, 5.5, leaf_angle)
    outlined(c, leaf, p["leaf"], p, width=2.5)
    vein = skia.Path()
    vein.moveTo(x + 5, y - 2)
    vein.lineTo(x + 5 + leaf_length * 0.65 * math.cos(math.radians(leaf_angle)), y - 2 + leaf_length * 0.65 * math.sin(math.radians(leaf_angle)))
    c.drawPath(vein, stroke(col(p["leaf_dark"]), 1.5))


def crown(c, p, x, y, angles, length, width):
    for angle in angles:
        outlined(c, leaf_path(x, y, length, width, angle), p["leaf"], p, width=2.5)


def fruit_body(kind):
    if kind == "prickly":
        return oval(MID - 20, 13, MID + 20, 58)
    if kind == "strawberry":
        path = skia.Path()
        path.moveTo(MID, 58)
        path.cubicTo(MID - 12, 54, MID - 22, 36, MID - 19, 24)
        path.cubicTo(MID - 16, 13, MID + 16, 13, MID + 19, 24)
        path.cubicTo(MID + 22, 36, MID + 12, 54, MID, 58)
        path.close()
        return path
    if kind == "cherry":
        return union(circle(21, 45, 11.5), circle(43, 43, 11.5))
    if kind == "banana":
        path = skia.Path()
        path.moveTo(9, 25)
        path.cubicTo(12, 54, 46, 60, 57, 30)
        path.lineTo(54, 25)
        path.cubicTo(42, 45, 22, 44, 16, 22)
        path.close()
        return path
    if kind == "grapes":
        centers = ((22, 25), (32, 24), (42, 25), (27, 35), (37, 35), (32, 46), (21, 35), (43, 35))
        return union(*(circle(x, y, 8.2) for x, y in centers))
    if kind == "orange":
        return circle(MID, 37, 20)
    if kind == "pear":
        path = skia.Path()
        path.moveTo(MID, 16)
        path.cubicTo(MID + 8, 16, MID + 8, 27, MID + 12, 33)
        path.cubicTo(MID + 23, 43, MID + 18, 58, MID, 58)
        path.cubicTo(MID - 18, 58, MID - 23, 43, MID - 12, 33)
        path.cubicTo(MID - 8, 27, MID - 8, 16, MID, 16)
        path.close()
        return path
    if kind == "lemon":
        path = skia.Path()
        path.moveTo(7, 38)
        path.cubicTo(12, 18, 52, 18, 57, 38)
        path.cubicTo(52, 56, 12, 56, 7, 38)
        path.close()
        return union(path, circle(8, 38, 3.5), circle(56, 38, 3.5))
    if kind == "watermelon":
        path = skia.Path()
        path.moveTo(5, 24)
        path.lineTo(59, 24)
        path.arcTo(skia.Rect(5, -3, 59, 51), 0, 180, False)
        path.close()
        return path
    path = skia.Path()
    path.moveTo(MID, 20)
    path.cubicTo(MID + 10, 12, MID + 24, 18, MID + 22, 36)
    path.cubicTo(MID + 20, 52, MID + 8, 58, MID, 55)
    path.cubicTo(MID - 8, 58, MID - 20, 52, MID - 22, 36)
    path.cubicTo(MID - 24, 18, MID - 10, 12, MID, 20)
    path.close()
    return path


def fruit_inside(c, p, kind, body):
    bounds = body.computeTightBounds()
    rng = random.Random(7)
    if kind in ("prickly", "strawberry"):
        for _ in range(9):
            x = bounds.left() + bounds.width() * rng.uniform(0.2, 0.8)
            y = bounds.top() + bounds.height() * rng.uniform(0.3, 0.9)
            if kind == "prickly":
                c.drawPath(circle(x, y, 2.2), fill(col(p["fruit_dark"])))
                c.drawPath(circle(x, y - 0.6, 1.4), fill(col(p["seed"])))
            else:
                c.drawPath(oval(x - 1.2, y - 2, x + 1.2, y + 2), fill(col(p["seed"])))
    if kind == "orange":
        for _ in range(14):
            x, y = rng.uniform(16, 48), rng.uniform(22, 54)
            c.drawPath(circle(x, y, 0.9), fill(col(p["fruit_dark"])))
    if kind == "watermelon":
        rind = skia.Path()
        rind.addArc(skia.Rect(5, -3, 59, 51), 0, 180)
        c.drawPath(rind, stroke(col(p["rind"]), 13))
        c.drawPath(rind, stroke(col(p["rind_dark"]), 4))
        for x, y in ((20, 31), (32, 35), (44, 31), (26, 40), (38, 40)):
            c.drawPath(oval(x - 1.6, y - 2.6, x + 1.6, y + 2.6), fill(col(p["pip"])))
    if kind == "banana":
        c.drawPath(circle(10.5, 25, 3.2), fill(col(p["stem"])))
        c.drawPath(circle(55.5, 27.5, 3.2), fill(col(p["stem"])))


def fruit_top(c, p, kind):
    if kind == "prickly":
        crown(c, p, MID, 15, (-120, -90, -60), 11, 4.5)
    elif kind == "strawberry":
        crown(c, p, MID, 16, (-160, -125, -90, -55, -20), 12, 4.5)
    elif kind == "cherry":
        stems = skia.Path()
        stems.moveTo(21, 35)
        stems.quadTo(24, 18, 34, 10)
        stems.moveTo(43, 33)
        stems.quadTo(40, 18, 34, 10)
        c.drawPath(stems, stroke(col(p["outline"]), 6))
        c.drawPath(stems, stroke(col(p["stem"]), 2.5))
        outlined(c, leaf_path(34, 10, 13, 5, -20), p["leaf"], p, width=2.5)
    elif kind == "grapes":
        stem_and_leaf(c, p, MID, 14, leaf_angle=-30, leaf_length=13)
    elif kind == "orange":
        outlined(c, leaf_path(MID, 18, 13, 5, -35), p["leaf"], p, width=2.5)
    elif kind == "pear":
        stem_and_leaf(c, p, MID, 13, leaf_angle=-20, leaf_length=13)
    elif kind == "lemon":
        outlined(c, leaf_path(MID + 2, 22, 12, 4.5, -30), p["leaf"], p, width=2.5)
    elif kind == "apple":
        stem_and_leaf(c, p, MID, 13)


def fruit_sprite(p, kind, squash=0.0, shadow=True):
    cv = Canvas(CELL, CELL)
    c = cv.c
    if shadow:
        c.drawPath(oval(MID - 20 - squash * 20, 53, MID + 20 + squash * 20, 63), fill(col(p.get("fruit_shadow", shade(p)), 0.25)))
    c.save()
    c.translate(MID, 58)
    c.scale(1 + squash, 1 - squash)
    c.translate(-MID, -58)
    body = fruit_body(kind)
    bounds = body.computeTightBounds()
    c.drawPath(body, stroke(col(p["outline"]), OUT * 2))
    c.save()
    c.clipPath(body, doAntiAlias=True)
    c.drawPath(body, fill(col(p["fruit_dark"])))
    shifted = skia.Path(body)
    shifted.offset(-4, -5)
    c.drawPath(shifted, fill(col(p["fruit"])))
    left, top, w, h = bounds.left(), bounds.top(), bounds.width(), bounds.height()
    c.drawPath(oval(left + w * 0.16, top + h * 0.14, left + w * 0.36, top + h * 0.3), fill(col(p["fruit_light"])))
    fruit_inside(c, p, kind, body)
    c.restore()
    if kind == "watermelon":
        c.drawPath(body, stroke(col(p["outline"]), 2.5))
    fruit_top(c, p, kind)
    c.restore()
    return cv


def light_glow(size=128):
    yy, xx = np.mgrid[0:size, 0:size].astype(np.float32) + 0.5
    r = np.hypot(xx - size / 2, yy - size / 2) / (size / 2)
    alpha = np.clip(1 - r, 0, 1) ** 2.2
    image = np.full((size, size, 4), 255, np.uint8)
    image[..., 3] = (alpha * 255).astype(np.uint8)
    return Image.fromarray(image)


def grid_cell(p):
    cv = Canvas(CELL, CELL)
    cv.c.drawPath(rect(2, 2, CELL - 2, CELL - 2, 12), stroke(col(p["outline"]), 2.5))
    return cv


def ground_tile(p):
    size = TILE_CELLS * CELL
    cv = Canvas(size, size)
    c = cv.c
    c.drawRect(skia.Rect(0, 0, size, size), fill(col(p["ground"])))
    alternate = fill(col(p["grout"], 0.35))
    for i in range(TILE_CELLS):
        for j in range(TILE_CELLS):
            if (i + j) % 2:
                c.drawRect(skia.Rect(i * CELL, j * CELL, (i + 1) * CELL, (j + 1) * CELL), alternate)
    return cv


GROUND_SIZE = TILE_CELLS * CELL


def tint_hex(hex_color, amount):
    r, g, b = rgb(hex_color)
    target = 255 if amount > 0 else 0
    t = abs(amount)
    return "#%02x%02x%02x" % tuple(int(round(v + (target - v) * t)) for v in (r, g, b))


def ground_line(p, alpha=1.0):
    if abs(luminance(p["grout"]) - luminance(p["ground"])) > 0.04:
        return col(p["grout"], alpha)
    return col(p["outline"], alpha * 0.3)


def ground_canvas(p):
    cv = Canvas(GROUND_SIZE, GROUND_SIZE)
    cv.c.drawRect(skia.Rect(0, 0, GROUND_SIZE, GROUND_SIZE), fill(col(p["ground"])))
    return cv


def wrapped(draw, x, y):
    for dx in (-GROUND_SIZE, 0, GROUND_SIZE):
        for dy in (-GROUND_SIZE, 0, GROUND_SIZE):
            draw(x + dx, y + dy)


def ground_plain(p):
    return ground_canvas(p)


def ground_grain(p):
    cv = ground_canvas(p)
    c = cv.c
    rng = random.Random(7)
    dark = fill(ground_line(p, 0.9))
    light = fill(col(tint_hex(p["ground"], 0.25), 0.8))
    for _ in range(320):
        x, y = rng.uniform(0, GROUND_SIZE), rng.uniform(0, GROUND_SIZE)
        radius = rng.uniform(1.4, 3.0)
        paint = dark if rng.random() < 0.7 else light
        wrapped(lambda px, py: c.drawCircle(px, py, radius, paint), x, y)
    return cv


def ground_stripes(p):
    cv = ground_canvas(p)
    c = cv.c
    paint = fill(ground_line(p, 0.45))
    half = CELL / 2
    for k in range(-TILE_CELLS * 2, TILE_CELLS + 1):
        x0 = k * CELL
        band = skia.Path()
        band.moveTo(x0, 0)
        band.lineTo(x0 + half, 0)
        band.lineTo(x0 + half + GROUND_SIZE, GROUND_SIZE)
        band.lineTo(x0 + GROUND_SIZE, GROUND_SIZE)
        band.close()
        c.drawPath(band, paint)
    return cv


def ground_tiles(p):
    cv = Canvas(GROUND_SIZE, GROUND_SIZE)
    c = cv.c
    c.drawRect(skia.Rect(0, 0, GROUND_SIZE, GROUND_SIZE), fill(col(p["ground"])))
    c.drawRect(skia.Rect(0, 0, GROUND_SIZE, GROUND_SIZE), fill(ground_line(p)))
    rng = random.Random(3)
    size = CELL * 2
    for i in range(TILE_CELLS // 2):
        for j in range(TILE_CELLS // 2):
            x, y = i * size, j * size
            base = tint_hex(p["ground"], rng.uniform(-0.05, 0.05))
            c.drawPath(rect(x + 3, y + 3, x + size - 3, y + size - 3, 8), fill(col(base)))
            c.drawPath(rect(x + 7, y + 7, x + size - 7, y + size - 7, 6), stroke(col(tint_hex(base, 0.2), 0.7), 2))
    return cv


def ground_scales(p):
    cv = ground_canvas(p)
    c = cv.c
    radius = CELL / 2
    line = stroke(ground_line(p), 3)
    light = stroke(col(tint_hex(p["ground"], 0.25), 0.7), 2)
    for j in range(-2, TILE_CELLS * 2 + 2):
        y = j * radius
        offset = radius if j % 2 else 0
        base = fill(col(tint_hex(p["ground"], -0.03 if j % 2 else 0.02)))
        for i in range(-1, TILE_CELLS + 1):
            x = i * CELL + offset
            c.drawCircle(x, y, radius, base)
            c.drawCircle(x, y, radius - 1.5, line)
            c.drawArc(skia.Rect(x - radius + 8, y - radius + 8, x + radius - 8, y + radius - 8), 30, 120, False, light)
    return cv


def ground_dunes(p):
    cv = ground_canvas(p)
    c = cv.c
    dark = stroke(ground_line(p), 3.5)
    light = stroke(col(tint_hex(p["ground"], 0.3), 0.8), 2)
    for k in range(TILE_CELLS * 2):
        base = k * CELL / 2 + CELL / 4
        for paint, shift in ((light, -3.5), (dark, 0)):
            path = skia.Path()
            for step, x in enumerate(range(-8, GROUND_SIZE + 9, 4)):
                t = x / GROUND_SIZE * math.tau
                y = base + shift + 7 * math.sin(2 * t + k * 0.7) + 3 * math.sin(5 * t + k * 1.3)
                if step == 0:
                    path.moveTo(x, y)
                else:
                    path.lineTo(x, y)
            c.drawPath(path, paint)
    return cv


def ground_hex(p):
    cv = ground_canvas(p)
    c = cv.c
    columns, rows = TILE_CELLS, 7
    step_x = GROUND_SIZE / columns
    step_y = GROUND_SIZE / rows
    rx = step_x / 1.5
    half = step_y / 2
    line = stroke(ground_line(p), 3)
    for i in range(-1, columns + 1):
        for j in range(-1, rows + 1):
            cx = i * step_x
            cy = j * step_y + (half if i % 2 else 0)
            hexagon = skia.Path()
            hexagon.moveTo(cx - rx, cy)
            hexagon.lineTo(cx - rx / 2, cy - half)
            hexagon.lineTo(cx + rx / 2, cy - half)
            hexagon.lineTo(cx + rx, cy)
            hexagon.lineTo(cx + rx / 2, cy + half)
            hexagon.lineTo(cx - rx / 2, cy + half)
            hexagon.close()
            tone = ((i % columns) * 3 + (j % rows) * 5) % 4
            c.drawPath(hexagon, fill(col(tint_hex(p["ground"], (tone - 1.5) * 0.02))))
            c.drawPath(hexagon, line)
    return cv


GROUNDS = dict(
    groundPlain=ground_plain,
    groundGrain=ground_grain,
    groundStripes=ground_stripes,
    groundTiles=ground_tiles,
    groundScales=ground_scales,
    groundDunes=ground_dunes,
    groundHex=ground_hex,
)


def board_frame(p):
    w, h = 256, 320
    cell = w / 16
    yy, xx = np.mgrid[0:h, 0:w].astype(np.float32) + 0.5
    d = np.minimum.reduce([xx, yy, w - xx, h - yy]) / cell
    alpha = np.clip(1 - d / 0.9, 0, 1) ** 2 * 0.45
    alpha[d * cell < 2.0] = 1.0
    image = np.zeros((h, w, 4), np.uint8)
    image[..., :3] = rgb(p["frame"])
    image[..., 3] = (alpha * 255).astype(np.uint8)
    return Image.fromarray(image, "RGBA")


def nine_patch(img, left, right, top, bottom):
    w, h = img.size
    out = Image.new("RGBA", (w + 2, h + 2), (0, 0, 0, 0))
    out.paste(img, (1, 1))
    px = out.load()
    for x in range(1 + left, 1 + w - right):
        px[x, 0] = (0, 0, 0, 255)
    for y in range(1 + top, 1 + h - bottom):
        px[0, y] = (0, 0, 0, 255)
    return out


def button_image(p, base, light, dark, pressed=False, size=72, line=None):
    cv = Canvas(size, size)
    c = cv.c
    o = 5
    if p.get("button_style") == "tube":
        shape = rect(o, o, size - o, size - o, 18)
        c.drawPath(shape, fill(col(dark)))
        if pressed:
            c.drawPath(shape, fill(col(light, 0.3)))
        c.drawPath(shape, stroke(col(line or p["outline"]), 4.5))
        return nine_patch(cv.pil(), 24, 24, 24, 24)
    top = o + (5 if pressed else 0)
    bevel = 4 if pressed else 10
    shape = rect(o, top, size - o, size - o, 18)
    c.drawPath(shape, stroke(col(p["outline"]), o * 2))
    c.save()
    c.clipPath(shape, doAntiAlias=True)
    c.drawPath(shape, fill(col(dark)))
    c.drawPath(rect(o, top, size - o, size - o - bevel, 16), fill(col(base)))
    c.drawPath(rect(o + 12, top + 4, size - o - 12, top + 7, 1.5), fill(col(light, 0.9)))
    c.restore()
    return nine_patch(cv.pil(), 24, 24, 24, 24)


def panel_image(p, size=96):
    cv = Canvas(size, size)
    c = cv.c
    o = 6
    shape = rect(o, o, size - o, size - o, 22)
    c.drawPath(shape, stroke(col(p["outline"]), o * 2))
    c.save()
    c.clipPath(shape, doAntiAlias=True)
    c.drawPath(shape, fill(col(p["panel_dark"])))
    c.drawPath(rect(o, o, size - o, size - o - 8, 20), fill(col(p["panel"])))
    c.restore()
    c.drawPath(rect(o + 9, o + 9, size - o - 9, size - o - 15, 13), stroke(col(p["panel_line"]), 2.5))
    for x in (o + 9, size - o - 9):
        for y in (o + 9, size - o - 15):
            c.drawPath(circle(x, y, 4.5), stroke(col(p["outline"]), 4))
            c.drawPath(circle(x, y, 4.5), fill(col(p["rivet"])))
            c.drawPath(circle(x - 1.2, y - 1.2, 1.5), fill(col("#ffffff", 0.7)))
    return nine_patch(cv.pil(), 32, 32, 32, 32)


def icon_canvas(p, draw, size=64):
    cv = Canvas(size, size)
    shape = draw()
    cv.c.drawPath(shape, stroke(col(p["outline"]), 9))
    cv.c.drawPath(shape, fill(col(p["text"])))
    return cv


def icon_pause(p):
    return icon_canvas(p, lambda: union(rect(17, 14, 28, 50, 3.5), rect(36, 14, 47, 50, 3.5)))


def icon_play(p):
    def shape():
        path = skia.Path()
        path.moveTo(21, 13)
        path.lineTo(50, 32)
        path.lineTo(21, 51)
        path.close()
        return path
    cv = Canvas(64, 64)
    path = shape()
    cv.c.drawPath(path, stroke(col(p["outline"]), 13))
    cv.c.drawPath(path, stroke(col(p["text"]), 4))
    cv.c.drawPath(path, fill(col(p["text"])))
    return cv


def speaker():
    path = skia.Path()
    path.moveTo(9, 25)
    path.lineTo(19, 25)
    path.lineTo(31, 14)
    path.lineTo(31, 50)
    path.lineTo(19, 39)
    path.lineTo(9, 39)
    path.close()
    return path


def icon_sound(p, on):
    cv = Canvas(64, 64)
    c = cv.c
    outline = col(p["outline"])
    text = col(p["text"])
    shape = speaker()
    c.drawPath(shape, stroke(outline, 9))
    marks = skia.Path()
    if on:
        for radius in (9, 18):
            marks.addArc(skia.Rect(33 - radius, 32 - radius, 33 + radius, 32 + radius), -45, 90)
    else:
        marks.moveTo(39, 24)
        marks.lineTo(55, 40)
        marks.moveTo(55, 24)
        marks.lineTo(39, 40)
    c.drawPath(marks, stroke(outline, 12))
    c.drawPath(shape, fill(text))
    c.drawPath(shape, stroke(text, 1.5))
    c.drawPath(marks, stroke(text, 4.5))
    return cv


def music_note():
    head = oval(13, 39, 31, 52)
    head.transform(skia.Matrix.RotateDeg(-20, skia.Point(22, 45.5)))
    stem = rect(27, 12, 32, 46, 1.5)
    flag = skia.Path()
    flag.moveTo(29, 12)
    flag.cubicTo(36, 19, 49, 21, 45, 37)
    flag.cubicTo(43, 29, 37, 26, 29, 26)
    flag.close()
    return union(head, stem, flag)


def icon_music(p, on):
    cv = Canvas(64, 64)
    c = cv.c
    outline = col(p["outline"])
    text = col(p["text"])
    shape = music_note()
    c.drawPath(shape, stroke(outline, 9))
    slash = skia.Path()
    if not on:
        slash.moveTo(13, 13)
        slash.lineTo(51, 51)
        c.drawPath(slash, stroke(outline, 12))
    c.drawPath(shape, fill(text))
    c.drawPath(shape, stroke(text, 1.5))
    if not on:
        c.drawPath(slash, stroke(text, 4.5))
    return cv


def text_path(text, size):
    font = skia.Font(TYPEFACE, size)
    path = skia.Path()
    x = 0.0
    glyphs = font.textToGlyphs(text)
    widths = font.getWidths(glyphs)
    for glyph, width in zip(glyphs, widths):
        glyph_path = font.getPath(glyph)
        if glyph_path is not None:
            glyph_path.offset(x, 0)
            path.addPath(glyph_path)
        x += width
    return path, x


LOGO_SHADOW = 14


def logo(p, width=640, height=260):
    cv = Canvas(width, height)
    c = cv.c
    path, advance = text_path("SNEK", 190)
    bounds = path.computeTightBounds()
    # Shift the text up and left by about half the shadow offset: text and shadow together stay centred.
    dx = (width - bounds.width()) / 2 - bounds.left() - LOGO_SHADOW / 2
    dy = (height - bounds.height()) / 2 - bounds.top() - 12
    path.offset(dx, dy)
    bounds = path.computeTightBounds()
    if p.get("logo_style") == "tube":
        return tube_logo(cv, p, path, bounds)
    shadow = skia.Path(path)
    shadow.offset(LOGO_SHADOW, LOGO_SHADOW)
    c.drawPath(shadow, stroke(col(p["outline"]), 34))
    c.drawPath(shadow, fill(col(p["outline"])))
    c.drawPath(path, stroke(col(p["outline"]), 34))
    c.drawPath(path, stroke(col(p["text"]), 16))
    c.save()
    c.clipPath(path, doAntiAlias=True)
    c.drawPath(path, fill(col(p["primary_dark"])))
    split = bounds.top() + bounds.height() * 0.55
    wave = skia.Path()
    wave.moveTo(bounds.left() - 20, bounds.top() - 20)
    wave.lineTo(bounds.right() + 20, bounds.top() - 20)
    wave.lineTo(bounds.right() + 20, split)
    steps = 8
    for i in range(steps, -1, -1):
        x = bounds.left() - 20 + (bounds.width() + 40) * i / steps
        wave.lineTo(x, split + (6 if i % 2 else -6))
    wave.close()
    c.drawPath(wave, fill(col(p["primary"])))
    c.drawPath(rect(bounds.left(), bounds.top() + 10, bounds.right(), bounds.top() + 22), fill(col(p["primary_light"], 0.8)))
    c.restore()
    logo_eyes(c, p, bounds, 9)
    return cv


def logo_eyes(c, p, bounds, outline_width):
    eye_y = bounds.top() + 30
    eye_x = bounds.left() + 62
    for ox in (0, 26):
        c.drawPath(circle(eye_x + ox, eye_y, 13), stroke(col(p["outline"]), outline_width))
        c.drawPath(circle(eye_x + ox, eye_y, 13), fill(col(p["eye"])))
        c.drawPath(circle(eye_x + ox + 4, eye_y + 2, 6.5), fill(col(p["pupil"])))
        c.drawPath(circle(eye_x + ox + 1.5, eye_y - 1, 2.2), fill(col("#ffffff")))


def tube_logo(cv, p, path, bounds):
    c = cv.c
    c.drawPath(path, fill(col(p["panel"])))
    c.drawPath(path, stroke(col(p["outline"]), 8))
    logo_eyes(c, p, bounds, 5)
    return cv


def outlined_text(c, text, x, y, size, p, color=None, align="left", outline_scale=1.0):
    path, advance = text_path(text, size)
    if align == "center":
        x -= advance / 2
    elif align == "right":
        x -= advance
    path.offset(x, y)
    border = max(2.0, size / 12) * outline_scale
    shadow = skia.Path(path)
    shadow.offset(0, border * 0.9)
    c.drawPath(shadow, stroke(col(shade(p)), border * 2))
    c.drawPath(shadow, fill(col(shade(p))))
    c.drawPath(path, stroke(col(shade(p)), border * 2))
    c.drawPath(path, fill(col(color or p["text"])))
    return advance


def loading_text(p):
    cv = Canvas(1500, 120)
    outlined_text(cv.c, "Loading...", 750, 92, 92, p, align="center")
    return cv


def tile_fill(c, tile_image, x, y, w, h, scale=1.0):
    c.save()
    c.clipRect(skia.Rect(x, y, x + w, y + h))
    size = tile_image.width() * scale
    ty = y
    while ty < y + h:
        tx = x
        while tx < x + w:
            c.drawImageRect(tile_image, skia.Rect(tx, ty, tx + size, ty + size), skia.SamplingOptions(skia.FilterMode.kLinear))
            tx += size
        ty += size
    c.restore()


def splash(p, tile, logo_cv):
    cv = Canvas(640, 400)
    c = cv.c
    tile_fill(c, tile.image(), 0, 0, 640, 400, 0.75)
    frame = Image.fromarray(np.array(board_frame(p).resize((640, 400), Image.LANCZOS)))
    c.drawImage(skia.Image.fromarray(np.array(frame)), 0, 0)
    c.drawImageRect(logo_cv.image(), skia.Rect(80, 70, 560, 265), skia.SamplingOptions(skia.FilterMode.kLinear))
    outlined_text(c, "Loading...", 320, 340, 34, p, align="center")
    return cv


def app_icon(p, head_cv, size=512):
    cv = Canvas(size, size)
    c = cv.c
    k = size / 128
    c.scale(k, k)
    shape = rect(6, 6, 122, 122, 28)
    c.drawPath(shape, stroke(col(p["outline"]), 10))
    c.save()
    c.clipPath(shape, doAntiAlias=True)
    c.drawPath(shape, fill(col(p["button_dark"])))
    c.drawPath(rect(6, 6, 122, 110, 26), fill(col(p["button"])))
    c.drawPath(rect(18, 14, 110, 20, 3), fill(col(p["button_light"], 0.9)))
    c.restore()
    c.save()
    c.translate(64, 66)
    c.rotate(90)
    c.scale(1.7, 1.7)
    c.translate(-MID, -MID)
    c.drawImage(head_cv.image(), 0, 0)
    c.restore()
    return cv


def save(img, path):
    path.parent.mkdir(parents=True, exist_ok=True)
    if isinstance(img, Canvas):
        img = img.pil()
    img.save(path)


def generate(name, out):
    p = PALETTES[name]
    skin = out / "textures/game/skin"
    if out.exists():
        shutil.rmtree(out)
    skin.mkdir(parents=True)
    snake, fruit, ui = with_line(p, "snake"), with_line(p, "fruit"), with_line(p, "ui")
    sprites = dict(
        snakePupil=snake_pupil(snake),
        snakeHeadMark=mark_sprite(snake, chevron(10)),
        gridCell=grid_cell(p),
        glow=light_glow(),
        iconPause=icon_pause(ui),
        iconPlay=icon_play(ui),
        iconSoundOn=icon_sound(ui, True),
        iconSoundOff=icon_sound(ui, False),
        iconMusicOn=icon_music(p, True),
        iconMusicOff=icon_music(p, False),
    )
    for shape in MARK_SHAPES:
        sprites[f"mark{shape.capitalize()}"] = mark_sprite(snake, mark_path(shape))
    for kind in ("base", "detail"):
        suffix = "" if kind == "base" else "Detail"
        snake_layer, fruit_layer = layer(snake, "snake", kind), layer(fruit, "fruit", kind)
        sprites[f"snakeHead{suffix}"] = snake_head(snake_layer)
        sprites[f"snakeHeadDead{suffix}"] = snake_head(snake_layer, dead=True)
        sprites[f"snakeBodyStraight{suffix}"] = snake_body_straight(snake_layer)
        sprites[f"snakeBodyTurn{suffix}"] = snake_body_turn(snake_layer)
        sprites[f"snakeTail{suffix}"] = snake_tail(snake_layer)
        for fruit_kind in FRUIT_KINDS:
            name = f"fruit{fruit_kind.capitalize()}"
            sprites[f"{name}Still{suffix}"] = fruit_sprite(fruit_layer, fruit_kind, shadow=False)
            sprites[f"{name}{suffix}_0"] = fruit_sprite(fruit_layer, fruit_kind, 0.0)
            sprites[f"{name}{suffix}_1"] = fruit_sprite(fruit_layer, fruit_kind, 0.07)
    for sprite_name, cv in sprites.items():
        save(cv, skin / f"{sprite_name}.png")
    tile = ground_tile(p)
    save(tile, skin / "ground.png")
    for ground_name, draw_ground in GROUNDS.items():
        save(draw_ground(p), skin / f"{ground_name}.png")
    save(board_frame(p), skin / "boardFrame.png")
    logo_cv = logo(with_line(p, "logo"))
    save(logo_cv, skin / "logo.png")
    save(button_image(ui, p["button"], p["button_light"], p["button_dark"]), skin / "button.9.png")
    save(button_image(ui, p["button_dark"], p["button"], p["button_dark"], pressed=True), skin / "buttonDown.9.png")
    save(button_image(ui, p["primary"], p["primary_light"], p["primary_dark"], line=p["primary"]), skin / "buttonPrimary.9.png")
    save(button_image(ui, p["primary_dark"], p["primary"], p["primary_dark"], pressed=True, line=p["primary"]),
         skin / "buttonPrimaryDown.9.png")
    save(panel_image(ui), skin / "panel.9.png")
    (skin / "pack.json").write_text(PACK_JSON)

    save(splash(p, tile, logo_cv), out / "root/SplashScreen.png")
    save(loading_text(p), out / "root/LoadingAssets.png")

    icon = app_icon(p, snake_head(snake, look=(-PUPIL_TRAVEL, 0))).pil()
    icons = out / "icons"
    for size in (16, 32, 64, 128):
        save(icon.resize((size, size), Image.LANCZOS), icons / f"icon{size}.png")
    for density, size in (("mdpi", 48), ("hdpi", 72), ("xhdpi", 96), ("xxhdpi", 144), ("xxxhdpi", 192)):
        save(icon.resize((size, size), Image.LANCZOS), icons / f"drawable-{density}/ic_launcher.png")
    save(icon, icons / "logo.png")
    icon.save(icons / "logo.ico", sizes=[(16, 16), (32, 32), (48, 48), (64, 64), (128, 128), (256, 256)])
    icon.save(icons / "favicon.ico", sizes=[(16, 16), (32, 32), (48, 48)])
    icon.save(icons / "logo.icns")

    (out / "palette.json").write_text(json.dumps(kotlin_palette(p), indent=2) + "\n")

    if "post" in p:
        POST[p["post"]](out)

    import preview
    sheet = preview.render(p, out)
    if "post" in p:
        sheet = POST_PREVIEW[p["post"]](sheet)
    previews = out.parent / "previews"
    previews.mkdir(exist_ok=True)
    sheet.save(previews / f"{name}.png")


GAMEBOY = ["#0f380f", "#306230", "#8bac0f", "#9bbc0f"]
PIXEL = 4


def gameboy_pixels(img, factor=PIXEL, hard_alpha=True):
    w, h = img.size
    small = np.array(img.convert("RGBA").resize((w // factor, h // factor), Image.BOX)).astype(np.float32)
    luminance = small[..., 0] * 0.299 + small[..., 1] * 0.587 + small[..., 2] * 0.114
    levels = np.digitize(luminance, [60, 120, 185])
    colors = np.array([rgb(c) for c in GAMEBOY], np.uint8)
    out = np.zeros(small.shape, np.uint8)
    out[..., :3] = colors[levels]
    out[..., 3] = np.where(small[..., 3] > 110, 255, 0) if hard_alpha else small[..., 3]
    return Image.fromarray(out).resize((w // factor * factor, h // factor * factor), Image.NEAREST)


def strip_nine(path):
    img = Image.open(path).convert("RGBA")
    marks = np.array(img)
    return img.crop((1, 1, img.width - 1, img.height - 1)), marks


def restore_nine(img, marks, path):
    arr = marks.copy()
    arr[1:-1, 1:-1] = np.array(img)
    Image.fromarray(arr).save(path)


def post_gameboy(out):
    skin = out / "textures/game/skin"
    for path in list(skin.glob("*.png")) + list((out / "root").glob("*.png")):
        if path.name.endswith(".9.png"):
            img, marks = strip_nine(path)
            restore_nine(gameboy_pixels(img), marks, path)
        else:
            gameboy_pixels(Image.open(path)).save(path)
    pack = skin / "pack.json"
    pack.write_text(pack.read_text().replace("filterMin: MipMapLinearLinear", "filterMin: Nearest").replace("filterMag: Linear", "filterMag: Nearest"))


def glow(img, radius, strength, wrap=False):
    arr = np.array(img.convert("RGBA")).astype(np.float32)
    premultiplied = arr.copy()
    premultiplied[..., :3] *= arr[..., 3:4] / 255
    source = np.tile(premultiplied, (3, 3, 1)) if wrap else premultiplied
    channels = [np.array(Image.fromarray(source[..., i].astype(np.uint8)).filter(ImageFilter.GaussianBlur(radius))).astype(np.float32)
                for i in range(4)]
    blurred = np.stack(channels, axis=-1)
    if wrap:
        h, w = arr.shape[:2]
        blurred = blurred[h:2 * h, w:2 * w]
    blurred = np.clip(blurred * strength, 0, 255)
    halo = np.zeros_like(arr)
    alpha = blurred[..., 3:4] / 255
    halo[..., :3] = np.where(alpha > 0, blurred[..., :3] / np.maximum(alpha, 1e-3), 0)
    halo[..., 3] = blurred[..., 3]
    result = Image.fromarray(np.clip(halo, 0, 255).astype(np.uint8))
    result.alpha_composite(Image.fromarray(arr.astype(np.uint8)))
    return result


def post_neon(out):
    skin = out / "textures/game/skin"
    for path in skin.glob("*.png"):
        if path.name.endswith(".9.png"):
            img, marks = strip_nine(path)
            restore_nine(glow(img, 3, 1.4), marks, path)
        elif path.stem not in ("boardFrame", "gridCell"):
            glow(Image.open(path), 3, 1.5).save(path)


LASER_LAYERS = dict(
    snake=("snakeHead", "snakeHeadDead", "snakeBodyStraight", "snakeBodyTurn", "snakeTail"),
    fruit=tuple(f"fruit{kind.capitalize()}{suffix}" for kind in FRUIT_KINDS for suffix in ("Still", "_0", "_1")),
)
LASER_GROUPS = dict(
    ui=("button.9", "buttonDown.9", "buttonPrimary.9", "buttonPrimaryDown.9", "panel.9", "iconPause", "iconPlay",
        "iconSoundOn", "iconSoundOff", "iconMusicOn", "iconMusicOff"),
    logo=("logo",),
)
LASER_BLOOM = dict(snake=((2, 1.6), (5, 1.1)), fruit=((2, 1.6), (6, 1.2)), marks=((2, 1.4), (4, 0.8)), ui=((2, 1.8),),
                   logo=((2, 1.0), (7, 0.6)))
LASER_CORE = dict(snake=1.4, fruit=1.4, ui=1.4, logo=2.4)


def blur(channel, radius):
    return np.array(Image.fromarray(np.clip(channel, 0, 255).astype(np.uint8)).filter(ImageFilter.GaussianBlur(radius))).astype(np.float32)


def bloom_under(arr, bright, bloom):
    source = np.zeros_like(arr)
    source[bright] = arr[bright]
    source[..., :3] *= source[..., 3:4] / 255
    halo = np.zeros_like(arr)
    for radius, strength in bloom:
        for i in range(4):
            halo[..., i] += blur(source[..., i], radius) * strength
    halo = np.clip(halo, 0, 255)
    alpha = halo[..., 3:4] / 255
    halo[..., :3] = np.where(alpha > 0, halo[..., :3] / np.maximum(alpha, 1e-3), 0)
    result = Image.fromarray(np.clip(halo, 0, 255).astype(np.uint8))
    result.alpha_composite(Image.fromarray(arr.astype(np.uint8)))
    return result


def rgba(img):
    return np.array(img.convert("RGBA")).astype(np.float32)


def brightness(arr):
    return arr[..., :3] @ np.array([0.299, 0.587, 0.114], np.float32)


def laser(img, line_hex, bloom, core_depth):
    arr = rgba(img)
    line = np.array(rgb(line_hex), np.float32)
    on_line = (np.abs(arr[..., :3] - line).sum(-1) < 70) & (arr[..., 3] > 128)
    core = ndimage.distance_transform_edt(on_line) >= core_depth
    arr[core, :3] = arr[core, :3] * 0.25 + 255 * 0.75
    bright = (on_line | (brightness(arr) > 170)) & (arr[..., 3] > 128)
    return bloom_under(arr, bright, bloom)


def laser_layers(base_path, detail_path, bloom, core_depth):
    base = rgba(Image.open(base_path))
    line = (brightness(base) > 235) & (base[..., 3] > 128)
    core = ndimage.distance_transform_edt(line) >= core_depth
    detail = Image.open(detail_path).convert("RGBA")
    glow_core = np.zeros(base.shape, np.uint8)
    glow_core[core] = (255, 255, 255, 150)
    core_image = Image.fromarray(glow_core)
    core_image.alpha_composite(detail)
    core_image.save(detail_path)
    bloom_under(base, line, bloom).save(base_path)


def detail_name(stem):
    if "_" in stem:
        name, index = stem.split("_")
        return f"{name}Detail_{index}"
    return f"{stem}Detail"


def post_laser(out):
    p = PALETTES[out.name]
    skin = out / "textures/game/skin"
    for group, stems in LASER_LAYERS.items():
        for stem in stems:
            laser_layers(skin / f"{stem}.png", skin / f"{detail_name(stem)}.png", LASER_BLOOM[group], LASER_CORE[group])
    for path in list(skin.glob("mark*.png")) + [skin / "snakeHeadMark.png"]:
        arr = rgba(Image.open(path))
        bloom_under(arr, arr[..., 3] > 128, LASER_BLOOM["marks"]).save(path)
    for group, stems in LASER_GROUPS.items():
        color = p.get(f"line_{group}", p["outline"])
        for stem in stems:
            path = skin / f"{stem}.png"
            if stem.endswith(".9"):
                img, marks = strip_nine(path)
                restore_nine(laser(img, color, LASER_BLOOM[group], LASER_CORE[group]), marks, path)
            else:
                laser(Image.open(path), color, LASER_BLOOM[group], LASER_CORE[group]).save(path)


POST = dict(gameboy=post_gameboy, neon=post_neon, laser=post_laser)
POST_PREVIEW = dict(gameboy=lambda sheet: gameboy_pixels(sheet, factor=2, hard_alpha=False), neon=lambda sheet: sheet,
                    laser=lambda sheet: sheet)


PACK_JSON = """{
	pot: true,
	paddingX: 4,
	paddingY: 4,
	bleed: true,
	bleedIterations: 4,
	edgePadding: true,
	duplicatePadding: true,
	rotation: false,
	maxWidth: 2048,
	maxHeight: 2048,
	stripWhitespaceX: false,
	stripWhitespaceY: false,
	filterMin: MipMapLinearLinear,
	filterMag: Linear,
	wrapX: ClampToEdge,
	wrapY: ClampToEdge,
	format: RGBA8888,
	useIndexes: true,
	premultiplyAlpha: false,
	legacyOutput: true,
	prettyPrint: true
}
"""

def fruit_entries(p):
    colors = p.get("fruit_colors")
    if colors:
        return [(kind, colors[i % len(colors)]) for i, kind in enumerate(FRUIT_KINDS)]
    return [(kind, NATURAL_FRUIT_COLORS[kind]) for kind in FRUIT_KINDS]


def kotlin_palette(p):
    palette = dict(
        clear=p["clear"],
        outline=shade(p),
        text=p["text"],
        primary=p["primary_light"],
        surround=surround(p),
        snake=tint(p, "snake"),
        marks=p.get("mark_color", p["snake_pattern"]),
        markShape=p.get("mark_shape", "diamond"),
        fruits=fruit_entries(p),
        snakeTakesFruitColor=p.get("snake_takes_fruit_color", False),
        snakeGlow=p.get("snake_glow", 0.0),
        fruitGlow=p.get("fruit_glow", 0.0),
    )
    palette.update(p.get("kotlin", {}))
    return palette


def install(name, out):
    p = PALETTES[name]
    skin = ROOT / "sourceAssets/textures/game/skin"
    if skin.exists():
        shutil.rmtree(skin)
    shutil.copytree(out / "textures/game/skin", skin)
    for file in ("SplashScreen.png", "LoadingAssets.png"):
        shutil.copy(out / "root" / file, ROOT / "sourceAssets/root" / file)
    icons = out / "icons"
    for size in (16, 32, 64, 128):
        shutil.copy(icons / f"icon{size}.png", ROOT / f"lwjgl3/src/main/resources/icon{size}.png")
    for density in ("mdpi", "hdpi", "xhdpi", "xxhdpi", "xxxhdpi"):
        shutil.copy(icons / f"drawable-{density}/ic_launcher.png", ROOT / f"android/res/drawable-{density}/ic_launcher.png")
    for file in ("logo.png", "logo.ico", "logo.icns"):
        shutil.copy(icons / file, ROOT / "lwjgl3/icons" / file)
    shutil.copy(icons / "favicon.ico", ROOT / "sourceAssets/web/favicon.ico")


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("styles", nargs="*", default=list(PALETTES))
    parser.add_argument("--out", type=Path, default=ROOT / "sourceAssets/styles")
    parser.add_argument("--install", help="copy this style into the game")
    args = parser.parse_args()
    for name in args.styles:
        generate(name, args.out / name)
        print(f"{name}: {args.out / name}")
    import preview
    preview.overview(args.out / "previews", list(PALETTES)).save(args.out / "previews/all.png")
    preview.marks_sheet(args.out, ["desert", "laser"]).save(args.out / "previews/marks.png")
    preview.fruits_sheet(args.out, list(PALETTES)).save(args.out / "previews/fruits.png")
    if args.install:
        install(args.install, args.out / args.install)
        print(f"installed {args.install}")
    preview.grounds_sheet(args.out, list(PALETTES)).save(args.out / "previews/grounds.png")


if __name__ == "__main__":
    main()
