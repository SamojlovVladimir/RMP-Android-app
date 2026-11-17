"""Генератор шрифта иконок UIKit.

Иконки описаны линиями в сетке 24x24 (как в макете: тонкий контур, скруглённые концы).
Из одного описания получаются:
  - uikit/src/main/res/font/uikit_icons.ttf   (шрифт)
  - uikit/src/main/java/.../AppIcons.kt        (имена и коды символов)
  - вставка enum в attrs.xml не нужна: AppIcon принимает имя строкой
"""
import math, sys
from shapely.geometry import LineString, Polygon, Point, MultiPolygon
from shapely.ops import unary_union
from shapely.geometry.polygon import orient
from fontTools.fontBuilder import FontBuilder
from fontTools.pens.ttGlyphPen import TTGlyphPen

W = 1.5  # толщина линии в сетке 24


def arc(cx, cy, r, a0, a1, n=24):
    pts = []
    for i in range(n + 1):
        a = math.radians(a0 + (a1 - a0) * i / n)
        pts.append((cx + r * math.cos(a), cy + r * math.sin(a)))
    return pts


def circle(cx, cy, r):
    return arc(cx, cy, r, 0, 360, 48)


def rrect(x, y, w, h, r):
    pts = []
    pts += arc(x + w - r, y + r, r, -90, 0, 8)
    pts += arc(x + w - r, y + h - r, r, 0, 90, 8)
    pts += arc(x + r, y + h - r, r, 90, 180, 8)
    pts += arc(x + r, y + r, r, 180, 270, 8)
    pts.append(pts[0])
    return pts


def line(*pts):
    return list(pts)


def lens(cx, cy, hw, hh, n=32):
    """Контур глаза: верхняя и нижняя дуга."""
    up = [(cx - hw + 2 * hw * i / n, cy - hh * math.sin(math.pi * i / n) ** 0.85) for i in range(n + 1)]
    down = [(cx - hw + 2 * hw * i / n, cy + hh * math.sin(math.pi * i / n) ** 0.85) for i in range(n, -1, -1)]
    return up + down[1:]


# Каждая иконка: список ("stroke", точки) | ("fill", точки полигона)
ICONS = {}

ICONS["back"] = [("stroke", line((14.5, 6), (8.5, 12), (14.5, 18)))]
ICONS["chevron_down"] = [("stroke", line((6, 9.5), (12, 15.5), (18, 9.5)))]
ICONS["search"] = [("stroke", circle(11, 11, 6.5)), ("stroke", line((15.8, 15.8), (20, 20)))]
ICONS["plus"] = [("stroke", line((12, 5), (12, 19))), ("stroke", line((5, 12), (19, 12)))]
ICONS["minus"] = [("stroke", line((5, 12), (19, 12)))]
ICONS["close"] = [("stroke", line((6, 6), (18, 18))), ("stroke", line((18, 6), (6, 18)))]
ICONS["close_circle"] = [
    ("fill", circle(12, 12, 9.5)),
    ("cut", line((8.8, 8.8), (15.2, 15.2))),
    ("cut", line((15.2, 8.8), (8.8, 15.2))),
]
ICONS["comment"] = [
    ("stroke", rrect(4, 4, 16, 12, 3)),
    ("stroke", line((8, 16), (8, 20), (12, 16))),
]
ICONS["filter"] = [("stroke", line((4, 5), (20, 5), (14, 12.5), (14, 19), (10, 17), (10, 12.5), (4, 5)))]
ICONS["download"] = [
    ("stroke", line((12, 4), (12, 15))),
    ("stroke", line((7.5, 10.8), (12, 15.3), (16.5, 10.8))),
    ("stroke", line((4.5, 17), (4.5, 20), (19.5, 20), (19.5, 17))),
]
ICONS["map"] = [
    ("stroke", line((3.5, 6.5), (9, 4.5), (15, 6.5), (20.5, 4.5), (20.5, 17.5), (15, 19.5), (9, 17.5), (3.5, 19.5), (3.5, 6.5))),
    ("stroke", line((9, 4.5), (9, 17.5))),
    ("stroke", line((15, 6.5), (15, 19.5))),
]
ICONS["more"] = [("fill", circle(5.5, 12, 1.5)), ("fill", circle(12, 12, 1.5)), ("fill", circle(18.5, 12, 1.5))]
ICONS["trash"] = [
    ("stroke", line((4, 7), (20, 7))),
    ("stroke", line((9.5, 7), (9.5, 4.5), (14.5, 4.5), (14.5, 7))),
    ("stroke", line((6.2, 7), (7.2, 19.5), (16.8, 19.5), (17.8, 7))),
    ("stroke", line((10, 10.5), (10, 16))),
    ("stroke", line((14, 10.5), (14, 16))),
]
ICONS["cart"] = [
    ("stroke", line((3, 4.5), (5.8, 4.5), (8, 15.5), (18, 15.5), (19.8, 8), (6.6, 8))),
    ("fill", circle(9.5, 19.3, 1.4)),
    ("fill", circle(16.5, 19.3, 1.4)),
]
ICONS["check"] = [("stroke", line((5, 12.5), (10, 17.5), (19, 7)))]
ICONS["doc"] = [
    ("stroke", line((7, 3.5), (14, 3.5), (18.5, 8), (18.5, 20.5), (7, 20.5), (7, 3.5))),
    ("stroke", line((14, 3.5), (14, 8), (18.5, 8))),
    ("stroke", line((9.5, 12.5), (16, 12.5))),
    ("stroke", line((9.5, 16), (16, 16))),
]
ICONS["send"] = [("stroke", line((20.5, 3.5), (3.5, 10.5), (10.5, 13.5), (13.5, 20.5), (20.5, 3.5))), ("stroke", line((10.5, 13.5), (20.5, 3.5)))]
ICONS["mic"] = [
    ("stroke", rrect(9, 3, 6, 11, 3)),
    ("stroke", arc(12, 11, 7, 0, 180, 24)),
    ("stroke", line((12, 18), (12, 21))),
]
ICONS["attach"] = [
    ("stroke", line((16, 9), (16, 16)) + arc(12.5, 16, 3.5, 0, 180, 12)[1:]
        + [(9, 8)] + arc(11.5, 8, 2.5, 180, 360, 12)[1:]
        + [(14, 16)] + arc(12.5, 16, 1.5, 0, 180, 8)[1:] + [(11, 10)]),
]
ICONS["eye"] = [
    ("stroke", lens(12, 12, 9.5, 5.8)),
    ("fill", circle(12, 12, 2.6)),
]
ICONS["eye_off"] = [
    ("stroke", lens(12, 12, 9.5, 5.8)),
    ("stroke", circle(12, 12, 2.6)),
    ("stroke", line((4, 4), (20, 20))),
]
ICONS["backspace"] = [
    ("stroke", line((9, 6), (20, 6), (20, 18), (9, 18), (3.5, 12), (9, 6))),
    ("stroke", line((11.5, 9.5), (16, 14.5))),
    ("stroke", line((16, 9.5), (11.5, 14.5))),
]
ICONS["fingerprint"] = [
    ("stroke", arc(12, 12.5, 8.5, 190, 350, 20)),
    ("stroke", arc(12, 12.5, 5.5, 175, 365, 20)),
    ("stroke", line((12, 11), (12, 14.5), (11, 18.5))),
    ("stroke", line((8.6, 12.5), (8.6, 16), (7.3, 19.5))),
    ("stroke", line((15.4, 12.5), (15.4, 15.5), (16.4, 19))),
    ("stroke", arc(12, 12.5, 2.2, 190, 350, 8)),
]
# Вкладки TabBar
ICONS["tab_home"] = [
    ("stroke", line((3.5, 11), (12, 3.5), (20.5, 11))),
    ("stroke", line((5.5, 9.5), (5.5, 20.5), (18.5, 20.5), (18.5, 9.5))),
    ("stroke", rrect(9.3, 13, 5.4, 7.5, 1)),
]
ICONS["tab_catalog"] = [
    ("stroke", rrect(4, 4, 16, 16, 3)),
    ("stroke", line((7.8, 9), (11, 9))),
    ("stroke", line((7.8, 12.5), (16.2, 12.5))),
    ("stroke", line((7.8, 16), (16.2, 16))),
]
ICONS["tab_projects"] = [
    ("stroke", rrect(4.5, 3.5, 11, 17, 1.8)),
    ("stroke", line((7.5, 8), (9, 9.5), (11.5, 6.5))),
    ("stroke", line((7.5, 13.5), (9, 15), (11.5, 12))),
    ("stroke", rrect(13, 13.5, 7, 7, 1.5)),
]
ICONS["tab_profile"] = [
    ("stroke", circle(12, 8, 4)),
    ("stroke", arc(12, 20.5, 7.5, 180, 360, 24) + [(4.5, 20.5)]),
]

EM = 1000
ASC, DESC = 800, -200
SCALE = EM / 24.0


def to_font(x, y):
    return (round(x * SCALE), round((24 - y) * SCALE + DESC))


def build_geom(parts):
    shapes = []
    cuts = []
    for kind, pts in parts:
        if kind == "stroke":
            closed = len(pts) > 3 and pts[0] == pts[-1]
            g = LineString(pts).buffer(W / 2, cap_style=1, join_style=1, quad_segs=8)
            shapes.append(g)
        elif kind == "fill":
            shapes.append(Polygon(pts).buffer(0))
        elif kind == "cut":
            cuts.append(LineString(pts).buffer(W / 2, cap_style=1, join_style=1, quad_segs=8))
    g = unary_union(shapes)
    if cuts:
        g = g.difference(unary_union(cuts))
    return g


def polys(g):
    if isinstance(g, Polygon):
        return [g]
    if isinstance(g, MultiPolygon):
        return list(g.geoms)
    return [p for p in g.geoms if isinstance(p, Polygon)]


def glyph_from(g):
    pen = TTGlyphPen(None)
    for p in polys(g):
        p = orient(p, sign=-1.0)  # внешний контур по часовой, дырки против
        for ring in [p.exterior] + list(p.interiors):
            pts = [to_font(x, y) for x, y in list(ring.coords)[:-1]]
            # убираем подряд идущие дубли
            clean = [pts[0]]
            for q in pts[1:]:
                if q != clean[-1]:
                    clean.append(q)
            if len(clean) < 3:
                continue
            pen.moveTo(clean[0])
            for q in clean[1:]:
                pen.lineTo(q)
            pen.closePath()
    return pen.glyph()


def main(out_ttf, out_kt, out_png):
    names = list(ICONS.keys())
    glyph_order = [".notdef"] + names
    cmap = {}
    glyphs = {}
    metrics = {}
    notdef_pen = TTGlyphPen(None)
    glyphs[".notdef"] = notdef_pen.glyph()
    metrics[".notdef"] = (EM, 0)
    geoms = {}
    for i, n in enumerate(names):
        cp = 0xE000 + i
        cmap[cp] = n
        g = build_geom(ICONS[n])
        geoms[n] = g
        glyphs[n] = glyph_from(g)
        metrics[n] = (EM, 0)

    fb = FontBuilder(EM, isTTF=True)
    fb.setupGlyphOrder(glyph_order)
    fb.setupCharacterMap(cmap)
    fb.setupGlyf(glyphs)
    fb.setupHorizontalMetrics(metrics)
    fb.setupHorizontalHeader(ascent=ASC, descent=DESC)
    fb.setupNameTable({"familyName": "MatuleIcons", "styleName": "Regular"})
    fb.setupOS2(sTypoAscender=ASC, sTypoDescender=DESC, usWinAscent=ASC, usWinDescent=-DESC, sTypoLineGap=0)
    fb.setupPost()
    fb.save(out_ttf)

    with open(out_kt, "w", encoding="utf-8") as f:
        f.write("package ru.samoylov.matule.uikit\n\n")
        f.write("/**\n * AppIcons — коды символов шрифта иконок uikit_icons.ttf.\n")
        f.write(" * Файл создан скриптом из описания иконок, вручную не правится.\n")
        f.write(" * Используется в AppIcon: icon.setIcon(AppIcons.SEARCH)\n */\n")
        f.write("object AppIcons {\n")
        for i, n in enumerate(names):
            f.write('    const val %s = "\\u%04X"\n' % (n.upper(), 0xE000 + i))
        f.write("\n    /** Все иконки по имени — нужно для атрибута app:iconName в XML. */\n")
        f.write("    val byName: Map<String, String> = mapOf(\n")
        f.write(",\n".join('        "%s" to %s' % (n, n.upper()) for n in names))
        f.write("\n    )\n}\n")

    # Контрольная картинка: рисуем сами контуры (без шрифтового рендера) и шрифтом через Pillow
    from PIL import Image, ImageDraw, ImageFont
    cell = 96
    cols = 8
    rows = (len(names) + cols - 1) // cols
    img = Image.new("RGB", (cols * cell, rows * cell * 2), "white")
    font = ImageFont.truetype(out_ttf, 64)
    d = ImageDraw.Draw(img)
    for i, n in enumerate(names):
        x = (i % cols) * cell
        y = (i // cols) * cell * 2
        d.text((x + 16, y + 4), chr(0xE000 + i), font=font, fill=(0, 0, 0))
        d.text((x + 4, y + cell + 30), n, fill=(90, 90, 90))
    img.save(out_png)
    print("icons:", len(names))


if __name__ == "__main__":
    main(*sys.argv[1:4])
