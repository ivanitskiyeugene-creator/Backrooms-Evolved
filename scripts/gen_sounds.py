#!/usr/bin/env python3
"""Генератор звуков (OGG Vorbis) для мода Backrooms Evolved.

Запуск:  python3 scripts/gen_sounds.py
Пишет файлы в src/main/resources/assets/backroomsevolved/sounds/
"""
import os
import numpy as np
import soundfile as sf

BASE = os.path.join(os.path.dirname(__file__), "..", "src", "main", "resources",
                    "assets", "backroomsevolved", "sounds")
SR = 44100


def save(data, name, volume=1.0):
    data = np.clip(data * volume, -0.98, 0.98).astype(np.float32)
    path = os.path.join(BASE, name + ".ogg")
    os.makedirs(BASE, exist_ok=True)
    sf.write(path, data, SR, format="OGG", subtype="VORBIS")
    print("wrote", os.path.relpath(path, os.path.join(BASE, "..", "..", "..", "..")))


def sine(freq, dur, sr=SR):
    t = np.arange(int(dur * sr)) / sr
    return np.sin(2 * np.pi * freq * t)


def noise(dur, sr=SR, seed=0):
    rng = np.random.default_rng(seed)
    return rng.uniform(-1, 1, int(dur * sr))


def envelope(n, attack=0.01, release=0.05, sr=SR):
    env = np.ones(n)
    a = max(int(attack * sr), 1)
    r = max(int(release * sr), 1)
    env[:a] = np.linspace(0, 1, a)
    env[-r:] = np.linspace(1, 0, r)
    return env


def loopify(data, fade=0.05):
    """Бесшовный луп через перекрёстное затухание краёв."""
    f = int(fade * SR)
    out = data.copy()
    head, tail = data[:f].copy(), data[-f:].copy()
    ramp = np.linspace(1, 0, f)
    out[:f] = head * ramp + tail * (1 - ramp)
    return out[:-f]


def lowpass(data, freq, sr=SR):
    """Простой рекурсивный ФНЧ."""
    rc = 1.0 / (2 * np.pi * freq)
    dt = 1.0 / sr
    alpha = dt / (rc + dt)
    out = np.empty_like(data)
    acc = data[0]
    for i in range(len(data)):
        acc += alpha * (data[i] - acc)
        out[i] = acc
    return out


# ----------------------------------------------------------- звуки
def lamp_hum():
    """Гул флуоресцентной лампы: 120 Гц + гармоники, бесшовный луп."""
    dur = 4.0
    d = (0.5 * sine(120, dur) + 0.18 * sine(240, dur) + 0.07 * sine(360, dur))
    d += 0.03 * lowpass(noise(dur, seed=1), 400)
    d *= envelope(len(d), 0.02, 0.02)
    save(loopify(d), "lamp_hum", volume=0.5)


def camera_click():
    d = noise(0.07, seed=2)
    d = d * envelope(len(d), 0.001, 0.05)
    save(d, "camera_click", volume=0.7)


def camera_power_down():
    dur = 0.6
    t = np.arange(int(dur * SR)) / SR
    freq = 820 * np.exp(-4.5 * t) + 60
    d = np.sin(2 * np.pi * np.cumsum(freq) / SR) * np.exp(-3.2 * t)
    save(d, "camera_power_down", volume=0.8)


def camera_static():
    """Треск/помехи: шум, промодулированный рваными вспышками."""
    dur = 2.0
    rng = np.random.default_rng(3)
    mod = np.ones(int(dur * SR))
    pos = 0
    while pos < len(mod):
        seg = rng.integers(200, 2200)
        lvl = rng.choice([0.15, 0.5, 1.0])
        mod[pos:pos + seg] = lvl
        pos += seg
    d = noise(dur, seed=4) * mod
    d *= envelope(len(d), 0.02, 0.04)
    save(loopify(d), "camera_static", volume=0.45)


def whisper():
    """Шёпот: фильтрованный шум с медленной модуляцией."""
    dur = 2.6
    rng = np.random.default_rng(5)
    d = lowpass(noise(dur, seed=6), 1800)
    am = 0.5 + 0.5 * np.sin(2 * np.pi * 1.7 * np.arange(len(d)) / SR
                             + rng.uniform(0, 6))
    am2 = 0.4 + 0.6 * np.abs(np.sin(2 * np.pi * 0.6 * np.arange(len(d)) / SR))
    d = d * am * am2
    d *= envelope(len(d), 0.25, 0.5)
    save(d, "whisper", volume=0.6)


def heartbeat():
    """Два глухих удара."""
    dur = 1.5
    d = np.zeros(int(dur * SR))

    def thump(start, freq=48, amp=1.0):
        s = int(start * SR)
        t = np.arange(int(0.22 * SR)) / SR
        pulse = np.sin(2 * np.pi * freq * t) * np.exp(-16 * t) * amp
        d[s:s + len(pulse)] += pulse

    thump(0.05, 50, 1.0)
    thump(0.42, 44, 0.7)
    save(d, "heartbeat", volume=0.9)


def basement_ambience():
    """Низкий гул подвала: коричневый шум, ФНЧ, луп."""
    dur = 8.0
    n = noise(dur, seed=7)
    brown = np.cumsum(n) / 80
    d = lowpass(brown, 140)
    d *= envelope(len(d), 0.1, 0.1)
    save(loopify(d), "basement_ambience", volume=0.55)


def rec_beep():
    d = sine(1180, 0.14) * envelope(int(0.14 * SR), 0.005, 0.03)
    save(d, "camera_rec_beep", volume=0.5)


if __name__ == "__main__":
    lamp_hum()
    camera_click()
    camera_power_down()
    camera_static()
    whisper()
    heartbeat()
    basement_ambience()
    rec_beep()
    print("done.")
