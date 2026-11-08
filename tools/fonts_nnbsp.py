"""Give U+202F (narrow no-break space) to the bundled fonts that lack it, when their licence allows it.

French text puts U+202F before « ; : ! ? » and inside « ». A font without it makes Android draw the space in a system
font, and « ! ; : ? » (Minikin's sticky punctuation) stay in that fallback: a straight sans « ! » after a script word.
Adds an empty glyph: advance of U+2009 when the font has one, else half a space (a full space in a monospaced font).
OFL: a font with a Reserved Font Name (in its name table or in assets/licences/<file>_OFL.txt) may only be modified under
another name. The interface serif is (RENAME): libre_baskerville.ttf becomes definiday_serif.ttf, family « Definiday
Serif », copyright, trademark, authors and licence records kept (its licence: assets/licences/definiday_serif_OFL.txt;
the original is in the repository history, app/src/main/res/font/libre_baskerville.ttf before this step, or Google Fonts).
The other RFN faces are left alone and get U+00A0 at text level (Styles.NO_NNBSP, strings.xml).
Idempotent, timestamps kept: python3 -I tools/fonts_nnbsp.py  (from the repository root; needs fonttools)
"""
import glob, os, re
from fontTools.ttLib import TTFont
from fontTools.ttLib.tables._g_l_y_f import Glyph

RFN = re.compile(r"Reserved\s*Font\s*Names?", re.I)
RENAME = {"libre_baskerville": ("definiday_serif", "Libre Baskerville", "Definiday Serif")}  # file, reserved name, new name
for path in sorted(glob.glob("app/src/main/res/font/*.ttf")):
    name = os.path.basename(path)[:-4]
    font = TTFont(path, recalcTimestamp=False)
    cmap = font.getBestCmap()
    head = lambda text: text.split("This Font Software is licensed")[0]  # the notice, not the licence's own definitions
    if 0x202F in cmap:
        print(f"{name}: has U+202F")
        continue
    src = path
    if name in RENAME:
        new_file, old, new = RENAME[name]
        for r in font["name"].names:
            if r.nameID not in (0, 7, 8, 9, 13, 14):  # copyright, trademark, authors and licence stay as they are
                r.string = r.toUnicode().replace(old, new).replace(old.replace(" ", ""), new.replace(" ", ""))
        font["name"].setName(f"Modified version of {old} (U+202F added) for Definiday, renamed per the SIL OFL.", 10, 3, 1, 0x409)
        path = path.replace(name, new_file)
    elif RFN.search(head(open(f"app/src/main/assets/licences/{name}_OFL.txt", encoding="utf-8").read())) \
            or any(RFN.search(head(r.toUnicode())) for r in font["name"].names if r.nameID in (0, 13)):
        print(f"{name}: Reserved Font Name, left alone")
        continue
    hmtx = font["hmtx"]
    space = hmtx[cmap[0x20]][0]
    adv = hmtx[cmap[0x2009]][0] if 0x2009 in cmap else space if font["post"].isFixedPitch else space // 2
    glyph = "uni202F"
    for tag in font.keys():
        font[tag]  # every table read before the glyph order grows
    font.setGlyphOrder(font.getGlyphOrder() + [glyph])
    font["glyf"].glyphs[glyph] = Glyph()  # no contour
    hmtx[glyph] = (adv, 0)
    for table in font["cmap"].tables:
        if table.isUnicode():
            table.cmap[0x202F] = glyph
    if "gvar" in font:  # variable font: no variation for an empty glyph; HVAR maps glyph ids directly (no AdvWidthMap)
        assert font["HVAR"].table.AdvWidthMap is None, name
        font["gvar"].variations[glyph] = []
        data = font["HVAR"].table.VarStore.VarData[0]
        data.Item.append([0] * data.VarRegionCount)
        data.ItemCount = len(data.Item)
    font.save(path)
    if path != src:
        os.remove(src)
    print(f"{name}: U+202F added, advance {adv} of {font['head'].unitsPerEm}" + (f", saved as {os.path.basename(path)}" if path != src else ""))
