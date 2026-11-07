"""Give U+202F (narrow no-break space) to the bundled fonts that lack it, when their licence allows it.

French text puts U+202F before « ; : ! ? » and inside « ». A font without it makes Android draw the space in a system
font, and « ! ; : ? » (Minikin's sticky punctuation) stay in that fallback: a straight sans « ! » after a script word.
Adds an empty glyph: advance of U+2009 when the font has one, else half a space (a full space in a monospaced font).
OFL: a font with a Reserved Font Name (in its name table or in assets/licences/<file>_OFL.txt) is never touched, since
a modified version would have to be renamed; those faces get U+00A0 at text level (Styles.NO_NNBSP, strings.xml).
Idempotent, timestamps kept: python3 -I tools/fonts_nnbsp.py  (from the repository root; needs fonttools)
"""
import glob, os, re
from fontTools.ttLib import TTFont
from fontTools.ttLib.tables._g_l_y_f import Glyph

RFN = re.compile(r"Reserved\s*Font\s*Names?", re.I)
for path in sorted(glob.glob("app/src/main/res/font/*.ttf")):
    name = os.path.basename(path)[:-4]
    font = TTFont(path, recalcTimestamp=False)
    cmap = font.getBestCmap()
    licence = open(f"app/src/main/assets/licences/{name}_OFL.txt", encoding="utf-8").read()
    head = lambda text: text.split("This Font Software is licensed")[0]  # the notice, not the licence's own definitions
    if 0x202F in cmap:
        print(f"{name}: has U+202F")
        continue
    if RFN.search(head(licence)) or any(RFN.search(head(r.toUnicode())) for r in font["name"].names if r.nameID in (0, 13)):
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
    print(f"{name}: U+202F added, advance {adv} of {font['head'].unitsPerEm}")
