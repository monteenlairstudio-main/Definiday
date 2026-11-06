#!/usr/bin/env python3
"""Synthesizes Definiday's sounds (docs/son.md) from scratch: no samples, so no third-party licence.

    python3 tools/sounds/make_sounds.py   # needs numpy and ffmpeg (libvorbis)

Writes app/src/main/res/raw/son_*.ogg (mono 44.1 kHz Vorbis) and prints length, peak, loudness and size.
Every sound is level-matched (loudest 50 ms window at TARGET dBFS), peak <= -3 dBFS, 5 ms fades.
"""
import os, subprocess, tempfile, wave
import numpy as np

SR = 44100
TARGET = -20.0  # dBFS, RMS of the loudest 50 ms window
PEAK = -3.0     # dBFS, hard ceiling
OUT = os.path.join(os.path.dirname(__file__), "../../app/src/main/res/raw")
rng = np.random.default_rng(7)  # same files on every run


def t(sec): return np.arange(int(sec * SR)) / SR
def noise(sec): return rng.standard_normal(int(sec * SR))
def env(x, attack, decay):
    """Exponential-decay envelope with a linear attack (seconds)."""
    n = np.arange(len(x)) / SR
    return x * np.minimum(n / max(attack, 1e-4), 1) * np.exp(-np.maximum(n - attack, 0) / decay)


def band(x, lo=0.0, hi=None, order=2):
    """Butterworth-shaped band-pass in the frequency domain (zero phase)."""
    f = np.fft.rfftfreq(len(x), 1 / SR)
    g = np.ones_like(f)
    if hi: g /= np.sqrt(1 + (f / hi) ** (2 * order))
    if lo: g /= np.sqrt(1 + (lo / np.maximum(f, 1e-3)) ** (2 * order))
    return np.fft.irfft(np.fft.rfft(x) * g, len(x))


def bar(freq, sec, decay):
    """Marimba-like struck bar: fundamental, 4th and 10th partials, the upper ones dying fast."""
    n = t(sec)
    return (np.sin(2 * np.pi * freq * n) * np.exp(-n / decay)
            + 0.25 * np.sin(2 * np.pi * freq * 3.93 * n) * np.exp(-n / (decay / 4))
            + 0.06 * np.sin(2 * np.pi * freq * 9.2 * n) * np.exp(-n / (decay / 10)))


def glide(f0, f1, sec):
    """Phase of a tone gliding from f0 to f1 Hz."""
    return 2 * np.pi * np.cumsum(np.linspace(f0, f1, int(sec * SR))) / SR


def tic():  # right answer: small wooden knock, paper-dry
    return bar(784, 0.16, 0.035) + 0.15 * env(band(noise(0.16), 1500, 5000), 0.001, 0.004)


def hmm():  # wrong answer: soft low hum, sliding down a little, never a buzzer
    ph = glide(233, 196, 0.38)  # harmonics carry it on phone speakers, which drop the fundamental
    x = np.sin(ph) + 0.5 * np.sin(2 * ph) + 0.25 * np.sin(3 * ph) + 0.1 * np.sin(4 * ph)
    n = t(0.38)
    return x * np.sin(np.pi * n / n[-1]) ** 1.5  # swells then fades


def chime():  # quiz finished: two bars, a fifth apart, the second rings longer
    a, b = bar(659, 0.95, 0.18), bar(988, 0.95, 0.3)
    gap = int(0.12 * SR)
    return a + 0.8 * np.concatenate([np.zeros(gap), b[:-gap]])


def stamp():  # unlock: rubber stamp « tchak », a low thump and a dull slap
    n = t(0.2)
    thump = np.sin(glide(140, 55, 0.2)) * np.exp(-n / 0.03)
    slap = env(band(noise(0.2), 300, 2500), 0.001, 0.012)
    return thump + 0.5 * slap


def page():  # new random word: a page turning, filtered noise swelling then settling
    n = t(0.45)
    swell = np.sin(np.pi * np.minimum(n / 0.32, 1)) ** 2 * (n < 0.32)
    flutter = 1 + 0.35 * band(noise(0.45), 0, 30)  # paper crinkle, slow wobble
    rustle = band(noise(0.45), 500, 3000, order=3) * swell * flutter
    flick = env(band(noise(0.45), 800, 3500, order=3), 0.002, 0.02)
    flick = np.roll(flick, int(0.3 * SR)) * (n >= 0.3)  # the page lands
    return rustle + 0.6 * flick


def mrr():  # Glose tapped: short rising trill « mrr », voiced and furry
    sec = 0.32
    ph = glide(170, 250, sec)
    voice = sum(np.sin(k * ph) / k for k in range(1, 9))
    n = t(sec)
    trill = 0.6 + 0.4 * np.sin(2 * np.pi * 28 * n)
    shape = np.sin(np.pi * n / n[-1]) ** 0.8
    return band(voice, 120, 1400) * trill * shape + 0.1 * band(noise(sec), 200, 900) * shape


def purr():  # Glose's special 4 taps: purr, two breaths of pulsed low noise over a low tone
    sec = 1.7
    n = t(sec)
    pulse = (0.5 + 0.5 * np.sin(2 * np.pi * 25 * n)) ** 3  # ~25 Hz, like a real purr
    breath = np.sin(np.pi * np.clip(n / 0.8, 0, 1)) + 0.8 * np.sin(np.pi * np.clip((n - 0.85) / 0.8, 0, 1))
    body = band(noise(sec), 60, 450) + 0.6 * np.sin(2 * np.pi * 95 * n)
    return body * pulse * breath


SOUNDS = {"son_tic": tic, "son_hmm": hmm, "son_carillon": chime, "son_tampon": stamp,
          "son_page": page, "son_mrr": mrr, "son_ronron": purr}


def db(v): return 20 * np.log10(max(v, 1e-9))


def loud(x):
    """RMS of the loudest 50 ms window: short sounds are judged by their body, not their silence."""
    w = int(0.05 * SR)
    return max(np.sqrt(np.mean(x[i:i + w] ** 2)) for i in range(0, max(len(x) - w, 1), w // 4))


def centroid(x):
    """Spectral centroid: a rough « warmth » check, kept under ~3 kHz."""
    s = np.abs(np.fft.rfft(x))
    return s @ np.fft.rfftfreq(len(x), 1 / SR) / s.sum()


def master(x):
    """Level-match, cap the peak, 5 ms fades in and out."""
    x = x * 10 ** (TARGET / 20) / loud(x)
    x *= min(1, 10 ** (PEAK / 20) / np.max(np.abs(x)))
    f = int(0.005 * SR)
    x[:f] *= np.linspace(0, 1, f); x[-f:] *= np.linspace(1, 0, f)
    return x


def main():
    os.makedirs(OUT, exist_ok=True)
    total = 0
    print(f"{'son':14} {'durée':>6} {'crête':>8} {'RMS50ms':>8} {'taille':>7} {'centre':>8}")
    for name, make in SOUNDS.items():
        x = master(make())
        pcm = np.round(x * 32767).astype("<i2")
        assert np.max(np.abs(pcm)) < 32767 * 10 ** (PEAK / 20) + 1, name  # no clipping
        with tempfile.NamedTemporaryFile(suffix=".wav") as tmp:
            with wave.open(tmp.name, "wb") as wf:
                wf.setnchannels(1); wf.setsampwidth(2); wf.setframerate(SR); wf.writeframes(pcm.tobytes())
            ogg = os.path.join(OUT, name + ".ogg")
            subprocess.run(["ffmpeg", "-y", "-loglevel", "error", "-i", tmp.name, "-c:a", "libvorbis", "-q:a", "3",
                            "-map_metadata", "-1", "-fflags", "+bitexact", ogg], check=True)
        size = os.path.getsize(ogg)
        total += size
        print(f"{name:14} {len(x) / SR:5.2f}s {db(np.max(np.abs(x))):7.1f}dB {db(loud(x)):7.1f}dB {size / 1024:5.1f}Ko {centroid(x):6.0f}Hz")
        assert size < 20_000, name
    print(f"total {total / 1024:.1f} Ko")
    assert total < 100_000


if __name__ == "__main__":
    main()
