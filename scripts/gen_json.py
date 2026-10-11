#!/usr/bin/env python3
"""Генерация blockstates / models / recipes для Backrooms Evolved.

Запуск:  python3 scripts/gen_json.py
"""
import json
import os

ROOT = os.path.join(os.path.dirname(__file__), "..", "src", "main", "resources",
                    "assets", "backroomsevolved")
DATA = os.path.join(os.path.dirname(__file__), "..", "src", "main", "resources",
                    "data", "backroomsevolved")
MOD = "backroomsevolved"


def w(path, obj):
    path = os.path.normpath(path)
    os.makedirs(os.path.dirname(path), exist_ok=True)
    with open(path, "w") as f:
        json.dump(obj, f, indent=2)
    print("wrote", os.path.relpath(path, os.path.join(ROOT, "..", "..")))


def bs_simple(name):
    return {"variants": {"": {"model": f"{MOD}:block/{name}"}}}


def cube_all(tex):
    return {"parent": "block/cube_all", "textures": {"all": f"{MOD}:block/{tex}"}}


def item_block(name):
    return {"parent": f"{MOD}:block/{name}"}


def item_layer0(name):
    return {"parent": "item/generated", "textures": {"layer0": f"{MOD}:item/{name}"}}


def gen():
    # ------------------------------------------------ blockstates
    w(f"{ROOT}/blockstates/damp_carpet.json", bs_simple("damp_carpet"))
    w(f"{ROOT}/blockstates/stained_carpet.json", bs_simple("stained_carpet"))
    w(f"{ROOT}/blockstates/wall_panel.json", bs_simple("wall_panel"))
    w(f"{ROOT}/blockstates/old_wallpaper.json", bs_simple("old_wallpaper"))
    w(f"{ROOT}/blockstates/sanity_anchor.json", bs_simple("sanity_anchor"))

    w(f"{ROOT}/blockstates/old_carpet.json", {"variants": {
        "footprints=0": {"model": f"{MOD}:block/old_carpet_0"},
        "footprints=1": {"model": f"{MOD}:block/old_carpet_1"},
        "footprints=2": {"model": f"{MOD}:block/old_carpet_2"},
        "footprints=3": {"model": f"{MOD}:block/old_carpet_3"},
    }})

    def lamp_bs(prefix):
        return {"variants": {
            "burned=false,lit=true": {"model": f"{MOD}:block/{prefix}_lit"},
            "burned=false,lit=false": {"model": f"{MOD}:block/{prefix}_off"},
            "burned=true,lit=false": {"model": f"{MOD}:block/{prefix}_burned"},
            "burned=true,lit=true": {"model": f"{MOD}:block/{prefix}_burned"},
        }}

    w(f"{ROOT}/blockstates/fluorescent_lamp.json", lamp_bs("fluorescent_lamp"))
    w(f"{ROOT}/blockstates/white_lamp.json", lamp_bs("white_lamp"))
    w(f"{ROOT}/blockstates/red_lamp.json", {"variants": {
        "lit=true": {"model": f"{MOD}:block/red_lamp_lit"},
        "lit=false": {"model": f"{MOD}:block/red_lamp_off"},
    }})

    # ------------------------------------------------ models/block
    for tex in ["damp_carpet", "stained_carpet", "wall_panel", "old_wallpaper",
                "sanity_anchor", "old_carpet_0", "old_carpet_1", "old_carpet_2", "old_carpet_3",
                "fluorescent_lamp_lit", "fluorescent_lamp_off", "fluorescent_lamp_burned",
                "white_lamp_lit", "white_lamp_off", "white_lamp_burned",
                "red_lamp_lit", "red_lamp_off"]:
        w(f"{ROOT}/models/block/{tex}.json", cube_all(tex))

    # ------------------------------------------------ models/item
    for name in ["damp_carpet", "stained_carpet", "old_carpet", "wall_panel",
                 "old_wallpaper", "sanity_anchor"]:
        w(f"{ROOT}/models/item/{name}.json", item_block(name))
    w(f"{ROOT}/models/item/fluorescent_lamp.json", item_block("fluorescent_lamp_lit"))
    w(f"{ROOT}/models/item/white_lamp.json", item_block("white_lamp_lit"))
    w(f"{ROOT}/models/item/red_lamp.json", item_block("red_lamp_lit"))
    w(f"{ROOT}/models/item/camera.json", item_layer0("camera"))
    w(f"{ROOT}/models/item/battery.json", item_layer0("battery"))

    # ------------------------------------------------ recipes
    def shapeless(out, count, ings):
        return {"type": "minecraft:crafting_shapeless",
                "ingredients": [{"item": i} for i in ings],
                "result": {"item": f"{MOD}:{out}", "count": count}}

    def shaped(out, count, pattern, key):
        return {"type": "minecraft:crafting_shaped", "pattern": pattern,
                "key": {k: {"item": v} for k, v in key.items()},
                "result": {"item": f"{MOD}:{out}", "count": count}}

    M = MOD
    w(f"{DATA}/recipes/damp_carpet.json", shapeless("damp_carpet", 1, ["minecraft:yellow_wool"]))
    w(f"{DATA}/recipes/stained_carpet.json",
      shapeless("stained_carpet", 1, [f"{M}:damp_carpet", "minecraft:cocoa_beans"]))
    w(f"{DATA}/recipes/old_carpet.json",
      shapeless("old_carpet", 1, [f"{M}:damp_carpet", "minecraft:gray_dye"]))
    w(f"{DATA}/recipes/wall_panel.json",
      shaped("wall_panel", 4, ["QQ", "QQ"], {"Q": "minecraft:smooth_quartz"}))
    w(f"{DATA}/recipes/old_wallpaper.json",
      shaped("old_wallpaper", 4, ["PP", "PP"], {"P": "minecraft:paper"}))
    w(f"{DATA}/recipes/fluorescent_lamp.json",
      shaped("fluorescent_lamp", 2,
             ["GGG", "IRI"],
             {"G": "minecraft:glass", "I": "minecraft:iron_ingot",
              "R": "minecraft:glowstone_dust"}))
    w(f"{DATA}/recipes/white_lamp.json",
      shapeless("white_lamp", 1, [f"{M}:fluorescent_lamp", "minecraft:white_dye"]))
    w(f"{DATA}/recipes/red_lamp.json",
      shapeless("red_lamp", 1, [f"{M}:fluorescent_lamp", "minecraft:red_dye"]))
    w(f"{DATA}/recipes/sanity_anchor.json",
      shaped("sanity_anchor", 1,
             [" I ", "IEI", " G "],
             {"I": "minecraft:iron_ingot", "E": "minecraft:ender_pearl",
              "G": "minecraft:glowstone"}))
    w(f"{DATA}/recipes/camera.json",
      shaped("camera", 1,
             ["IGI", "IRI"],
             {"I": "minecraft:iron_ingot", "G": "minecraft:glass",
              "R": "minecraft:repeater"}))
    w(f"{DATA}/recipes/battery.json",
      shapeless("battery", 1, ["minecraft:iron_ingot", "minecraft:redstone_block"]))
    # зарядка камеры — кастомный сериализатор (рецепт без формы)
    w(f"{DATA}/recipes/camera_charge.json", {
        "type": f"{M}:camera_charge",
    })


if __name__ == "__main__":
    gen()
    print("done.")
