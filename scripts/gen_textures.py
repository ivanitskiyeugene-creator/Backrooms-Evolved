#!/usr/bin/env python3
"""Генератор текстур для мода Backrooms Evolved (Forge 1.20.1).

Запуск:  python3 scripts/gen_textures.py
Все текстуры пишутся в src/main/resources/assets/backroomsevolved/textures/
"""
import os
import numpy as np
from PIL import Image, ImageFilter, ImageDraw

BASE = os.path.join(os.path.dirname(__file__), "..", "src", "main", "resources",
                    "assets", "backroomsevolved", "textures")
RNG = np.random.default_rng(1337)


def save(img: Image.Image, relpath: str):
    path = os.path.normpath(os.path.join(BASE, relpath))
    os.makedirs(os.path.dirname(path), exist_ok=True)
    img.save(path)
    print("wrote", os.path.relpath(path, os.path.join(BASE, "..", "..", "..", "..")))


def to_img(arr: np.ndarray) -> Image.Image:
    return Image.fromarray(np.clip(arr, 0, 255).astype(np.uint8), "RGBA")


def noise(base, amp, rng):
    """Мелкий шум поверх базового цвета."""
    out = base.astype(np.float32)
    out += rng.uniform(-amp, amp, size=out.shape[:2] + (1,))
    return out


def carpet_base(color, size=16):
    """Ворсистая база ковра: цвет + шум + тонкие вертикальные 'волокна'."""
    rng = np.random.default_rng(hash(tuple(color)) % 2**31)
    arr = np.zeros((size, size, 4), np.float32)
    arr[..., :3] = np.array(color, np.float32)
    arr = noise(arr, 14, rng)
    # волокна
    for x in range(size):
        if rng.random() < 0.5:
            arr[:, x, :3] += rng.uniform(-10, 6)
    arr[..., 3] = 255
    return arr


def add_blobs(arr, color, count, rmin=1.5, rmax=3.5, alpha=0.55, seed=1):
    """Тёмные влажные пятна (мягкие кляксы)."""
    rng = np.random.default_rng(seed)
    img = to_img(arr)
    overlay = Image.new("RGBA", img.size, (0, 0, 0, 0))
    d = ImageDraw.Draw(overlay)
    h, w = arr.shape[:2]
    for _ in range(count):
        cx, cy = rng.uniform(0, w), rng.uniform(0, h)
        r = rng.uniform(rmin, rmax)
        d.ellipse([cx - r, cy - r * rng.uniform(0.6, 1.0),
                   cx + r, cy + r * rng.uniform(0.6, 1.0)], fill=color)
    overlay = overlay.filter(ImageFilter.GaussianBlur(1.1))
    img.alpha_composite(overlay)
    return np.array(img).astype(np.float32)


def footprints_on(arr, pairs, seed=2):
    """Следы ботинков: пары тёмных овалов по диагонали."""
    rng = np.random.default_rng(seed)
    img = to_img(arr)
    overlay = Image.new("RGBA", img.size, (0, 0, 0, 0))
    d = ImageDraw.Draw(overlay)
    h, w = arr.shape[:2]
    for i in range(pairs):
        cx = rng.uniform(2, w - 2)
        cy = 3 + i * (h - 6) / max(pairs - 1, 1)
        for side in (-1.8, 1.8):
            x = cx + side
            d.ellipse([x - 1.1, cy - 1.9, x + 1.1, cy + 1.9], fill=(0, 0, 0, 110))
    overlay = overlay.filter(ImageFilter.GaussianBlur(0.6))
    img.alpha_composite(overlay)
    return np.array(img).astype(np.float32)


# ---------------------------------------------------------------- ковры
def gen_carpets():
    # Влажный жёлтый ковёр (классика уровня 0)
    wet = carpet_base((186, 168, 84))
    wet = add_blobs(wet, (110, 96, 44, 150), count=5, seed=11)
    save(to_img(wet), "block/damp_carpet.png")

    # Ковёр с тёмными пятнами неизвестного происхождения
    stained = carpet_base((176, 160, 88))
    stained = add_blobs(stained, (58, 38, 30, 200), count=4, rmin=1.8, rmax=3.0, seed=22)
    stained = add_blobs(stained, (30, 22, 20, 160), count=2, rmin=1.0, rmax=1.8, seed=23)
    save(to_img(stained), "block/stained_carpet.png")

    # Старый тёмный ковёр со следами (4 стадии)
    old = carpet_base((142, 128, 74))
    old = add_blobs(old, (96, 86, 48, 120), count=3, seed=33)
    for stage in range(4):
        save(to_img(footprints_on(old, stage, seed=40 + stage)), f"block/old_carpet_{stage}.png")


# ---------------------------------------------------------------- лампы
def lamp_panel(body, glow, lit=True, burned=False):
    rng = np.random.default_rng(abs(hash(tuple(body))) % 2**31)
    arr = np.zeros((16, 16, 4), np.float32)
    arr[..., :3] = np.array(body, np.float32)
    arr = noise(arr, 6, rng)
    img = to_img(arr)
    d = ImageDraw.Draw(img)
    if lit:
        # светящаяся панель с решёткой
        d.rectangle([2, 2, 13, 13], fill=glow)
        for i in (5, 8, 11):
            d.line([(2, i), (13, i)], fill=(body[0] - 30, body[1] - 30, body[2] - 30, 255))
        d.rectangle([2, 2, 13, 13], outline=(body[0] - 50, body[1] - 50, body[2] - 50, 255))
    if burned:
        # подпалины
        d.ellipse([4, 4, 11, 10], fill=(28, 24, 20, 235))
        d.ellipse([7, 7, 13, 13], fill=(18, 16, 14, 200))
    arr = np.array(img).astype(np.float32)
    return arr


def gen_lamps():
    yellow_body = (168, 158, 128)
    save(to_img(lamp_panel(yellow_body, (255, 240, 168), lit=True)), "block/fluorescent_lamp_lit.png")
    save(to_img(lamp_panel((120, 116, 100), (150, 144, 118), lit=False)), "block/fluorescent_lamp_off.png")
    save(to_img(lamp_panel((86, 84, 76), (110, 106, 92), lit=False, burned=True)), "block/fluorescent_lamp_burned.png")

    white_body = (176, 178, 180)
    save(to_img(lamp_panel(white_body, (244, 248, 255), lit=True)), "block/white_lamp_lit.png")
    save(to_img(lamp_panel((124, 126, 128), (150, 152, 154), lit=False)), "block/white_lamp_off.png")
    save(to_img(lamp_panel((88, 88, 90), (108, 110, 112), lit=False, burned=True)), "block/white_lamp_burned.png")

    save(to_img(lamp_panel((150, 70, 64), (255, 70, 52), lit=True)), "block/red_lamp_lit.png")
    save(to_img(lamp_panel((96, 52, 50), (130, 60, 56), lit=False)), "block/red_lamp_off.png")


# ---------------------------------------------------------------- стены
def gen_walls():
    rng = np.random.default_rng(7)
    # Стеновые панели: светлые, с вертикальными швами
    arr = np.zeros((16, 16, 4), np.float32)
    arr[..., :3] = (214, 208, 184)
    arr = noise(arr, 8, rng)
    img = to_img(arr)
    d = ImageDraw.Draw(img)
    d.line([(0, 0), (0, 15)], fill=(176, 170, 148, 255))
    d.line([(8, 0), (8, 15)], fill=(186, 180, 158, 255))
    d.line([(0, 12), (15, 12)], fill=(196, 190, 168, 255))
    save(img, "block/wall_panel.png")

    # Старые обои: желтоватые, с узором и потёками
    arr = np.zeros((16, 16, 4), np.float32)
    arr[..., :3] = (206, 188, 122)
    arr = noise(arr, 10, np.random.default_rng(8))
    img = to_img(arr)
    d = ImageDraw.Draw(img)
    for x in range(0, 16, 4):  # вертикальный узор
        for y in range(0, 16, 4):
            d.rectangle([x + 1, y + 1, x + 2, y + 2], fill=(188, 168, 104, 255))
    # потёк
    d.line([(11, 0), (11, 9)], fill=(150, 132, 84, 255))
    d.line([(12, 0), (12, 6)], fill=(160, 142, 90, 255))
    save(img, "block/old_wallpaper.png")


def gen_anchor():
    # Якорь рассудка: тёплый спокойный свет
    arr = np.zeros((16, 16, 4), np.float32)
    for y in range(16):
        for x in range(16):
            dx, dy = x - 7.5, y - 7.5
            dist = (dx * dx + dy * dy) ** 0.5
            k = max(0.0, 1.0 - dist / 8.0)
            arr[y, x] = (255 * (0.55 + 0.45 * k), 190 * (0.45 + 0.55 * k),
                         110 * (0.35 + 0.65 * k), 255)
    img = to_img(noise(arr, 5, np.random.default_rng(99)))
    d = ImageDraw.Draw(img)
    d.rectangle([0, 0, 15, 15], outline=(120, 90, 50, 255))
    d.rectangle([2, 2, 13, 13], outline=(200, 160, 90, 255))
    save(img, "block/sanity_anchor.png")


# ---------------------------------------------------------------- предметы
def gen_items():
    # Камера
    img = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    d = ImageDraw.Draw(img)
    d.rectangle([1, 4, 14, 13], fill=(46, 48, 54, 255))          # корпус
    d.rectangle([1, 4, 14, 5], fill=(70, 72, 80, 255))          # верхняя грань
    d.rectangle([4, 2, 9, 4], fill=(36, 38, 44, 255))           # видоискатель
    d.ellipse([5, 6, 12, 13], fill=(24, 26, 30, 255))           # объектив
    d.ellipse([7, 8, 10, 11], fill=(90, 140, 150, 255))         # линза
    d.point((13, 6), fill=(255, 60, 50, 255))                   # индикатор записи
    save(img, "item/camera.png")

    # Батарея
    img = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    d = ImageDraw.Draw(img)
    d.rectangle([5, 2, 10, 3], fill=(120, 120, 120, 255))       # клемма
    d.rectangle([4, 3, 11, 14], fill=(60, 130, 60, 255))        # корпус
    d.rectangle([4, 3, 11, 14], outline=(24, 60, 24, 255))
    d.line([(6, 6), (9, 6)], fill=(220, 240, 220, 255))         # маркировка
    d.line([(6, 8), (9, 8)], fill=(220, 240, 220, 255))
    save(img, "item/battery.png")


def gen_figure():
    """Силуэт 'того, кого лучше не видеть' — для галлюцинаций и ИК-камеры."""
    w, h = 32, 64
    img = Image.new("RGBA", (w, h), (0, 0, 0, 0))
    arr = np.zeros((h, w, 4), np.float32)
    rng = np.random.default_rng(666)
    # тело: вытянутый вертикальный градиент
    for y in range(h):
        body_w = 3 + 5 * np.sin(np.pi * min(y / h + 0.08, 1.0)) + rng.uniform(-0.5, 0.5)
        cx = w / 2 + rng.uniform(-0.7, 0.7)
        for x in range(w):
            dist = abs(x - cx)
            if dist < body_w:
                k = 1.0 - dist / body_w
                alpha = 235 * k
                arr[y, x] = (6, 6, 8, alpha)
    # 'голова'
    for y in range(4, 14):
        for x in range(w):
            dx, dy = x - w / 2, y - 9
            if dx * dx + dy * dy < 16:
                arr[y, x] = (4, 4, 6, 245)
    img = to_img(arr).filter(ImageFilter.GaussianBlur(1.2))
    save(img, "misc/figure.png")


if __name__ == "__main__":
    gen_carpets()
    gen_lamps()
    gen_walls()
    gen_anchor()
    gen_items()
    gen_figure()
    print("done.")
