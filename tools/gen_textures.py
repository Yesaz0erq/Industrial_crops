# -*- coding: utf-8 -*-
"""Industrial Crops texture generator.

Regenerates the industrial crop texture set (crop growth stages, items,
compressed crop blocks) in a consistent pixel-art style: base vegetation
colors accented with copper bands, iron frames and redstone glow.

Deterministic (no RNG) so re-runs produce identical files.
"""
import os
from PIL import Image

ASSETS = os.path.join(os.path.dirname(__file__), '..', 'src', 'main',
                      'resources', 'assets', 'industrialcrops', 'textures')
BLOCK = os.path.join(ASSETS, 'block')
ITEM = os.path.join(ASSETS, 'item')

# ---------------------------------------------------------------- palette --
PAL = {
    '.': None,
    'k': (30, 30, 34, 255),      # outline
    'i': (66, 68, 72, 255),      # iron dark
    'I': (122, 124, 130, 255),   # iron
    'n': (172, 176, 182, 255),   # iron light / rivet
    'c': (122, 72, 34, 255),     # copper dark
    'C': (196, 116, 58, 255),    # copper
    '+': (236, 166, 110, 255),   # copper light
    'r': (140, 22, 22, 255),     # red dark
    'R': (214, 52, 44, 255),     # redstone
    '*': (255, 132, 116, 255),   # redstone glow
    's': (52, 96, 32, 255),      # stem dark
    'g': (66, 122, 44, 255),     # green dark
    'G': (92, 162, 62, 255),     # green
    'l': (140, 200, 92, 255),    # green light
    'y': (168, 126, 34, 255),    # gold dark
    'Y': (222, 176, 68, 255),    # gold
    'W': (246, 226, 142, 255),   # gold light
    'o': (170, 82, 20, 255),     # orange dark
    'O': (222, 114, 34, 255),    # orange
    'Q': (244, 158, 66, 255),    # orange light
    'b': (86, 60, 42, 255),      # brown dark
    'B': (126, 90, 62, 255),     # brown
    't': (186, 146, 104, 255),   # tan
    'm': (26, 78, 32, 255),      # melon dark
    'M': (48, 124, 54, 255),     # melon mid
    'e': (106, 178, 94, 255),    # melon light
    'w': (204, 232, 236, 255),   # glass
}

K = PAL['k']; IRON_D = PAL['i']; IRON = PAL['I']; RIVET = PAL['n']
COP_D = PAL['c']; COP = PAL['C']; COP_L = PAL['+']
RED_D = PAL['r']; RED = PAL['R']; GLOW = PAL['*']
STEM = PAL['s']; GRN_D = PAL['g']; GRN = PAL['G']; GRN_L = PAL['l']
GLD_D = PAL['y']; GLD = PAL['Y']; GLD_L = PAL['W']
ORG_D = PAL['o']; ORG = PAL['O']; ORG_L = PAL['Q']
BRN_D = PAL['b']; BRN = PAL['B']; TAN = PAL['t']
MEL_D = PAL['m']; MEL = PAL['M']; MEL_L = PAL['e']


def img():
    return Image.new('RGBA', (16, 16), (0, 0, 0, 0))


def px(im, x, y, c):
    if 0 <= x < 16 and 0 <= y < 16 and c is not None:
        im.putpixel((x, y), c)


def from_grid(rows):
    assert len(rows) == 16, 'need 16 rows, got %d' % len(rows)
    im = img()
    for y, row in enumerate(rows):
        assert len(row) == 16, 'row %d has %d chars: %r' % (y, len(row), row)
        for x, ch in enumerate(row):
            c = PAL[ch]
            if c:
                im.putpixel((x, y), c)
    return im


def save(im, folder, name):
    path = os.path.join(folder, name + '.png')
    im.save(path)
    print('wrote', os.path.relpath(path, ASSETS))


# ============================================================ crop stages ==
def wheat_stage(stage):
    im = img()
    xs = [1, 4, 7, 10, 13]
    base_h = {0: 3, 1: 6, 2: 10, 3: 13}[stage]
    for k, x in enumerate(xs):
        h = base_h - (k * 5 % 3)          # deterministic per-stalk variation
        top = 16 - h
        for y in range(top, 16):
            if stage <= 1:
                c = STEM if y >= 14 else (GRN if (y + k) % 2 else GRN_L)
            elif stage == 2:
                c = STEM if y >= 14 else (GRN if y > top + 4 else GLD_D)
            else:
                c = GLD_D if y > top + 5 else GLD
            px(im, x, y, c)
        # grain heads on the two mature stages
        if stage >= 2:
            head_h = 4 if stage == 2 else 5
            side = 1 if k % 2 == 0 else -1
            for j in range(head_h):
                y = top + j
                main = GLD if stage == 3 else GRN_L
                px(im, x, y, main)
                px(im, x + side, y, GLD_L if (j + k) % 2 == 0 else
                   (GLD if stage == 3 else GRN))
            if stage == 3:
                px(im, x, top - 1, GLD_L)           # awn
        if stage == 3:
            px(im, x, 12, COP)                       # copper collar
            if k in (1, 3):                          # redstone-charged heads
                px(im, x + (1 if k % 2 == 0 else -1), top, GLOW)
                px(im, x, top, RED)
    return im


def carrot_stage(stage):
    im = img()
    xs = [3, 7, 12]
    h = {0: 2, 1: 4, 2: 6, 3: 7}[stage]
    for k, x in enumerate(xs):
        hh = h - (k % 2)
        for i in range(hh):
            y = 15 - i
            px(im, x, y, STEM if i < 2 else GRN)
            if i % 2 == 1:
                px(im, x - 1, y, GRN)
                px(im, x + 1, y, GRN_L)
            if stage >= 2 and i % 3 == 0 and i > 0:
                px(im, x - 2, y, GRN_D)
                px(im, x + 2, y, GRN)
        if stage == 3:                               # crowns pushing out
            px(im, x - 1, 15, ORG_D)
            px(im, x, 15, ORG)
            px(im, x + 1, 15, ORG_L)
            px(im, x, 14, ORG)
    if stage == 3:                                   # one redstone-laced root
        px(im, xs[1] + 1, 14, RED)
        px(im, xs[1], 15, GLOW)
    return im


def _blob(im, cx, cy, r, flower=False):
    for dy in range(-r, r + 1):
        for dx in range(-r, r + 1):
            if dx * dx + dy * dy <= r * r + 1:
                if dy < 0 and dx <= 0:
                    c = GRN_L
                elif dy > 0:
                    c = GRN_D
                else:
                    c = GRN
                px(im, cx + dx, cy + dy, c)
    if flower:
        px(im, cx, cy - r - 1, GLD_L)


def potato_stage(stage):
    im = img()
    r = {0: 1, 1: 2, 2: 3, 3: 3}[stage]
    clumps = [(4, 13), (8, 12), (12, 13)]
    for k, (cx, cy) in enumerate(clumps):
        _blob(im, cx, cy - (r - 1), r, flower=(stage == 3 and k != 1))
    if stage == 3:
        px(im, 8, 8, GLOW)                           # charged blossom
        px(im, 8, 9, RED)
        px(im, 3, 15, TAN)                           # tubers at the soil line
        px(im, 5, 15, BRN)
        px(im, 11, 15, TAN)
    return im


def _vine(im, tall):
    px(im, 4, 15, STEM)
    px(im, 4, 14, STEM)
    if tall:
        px(im, 4, 13, GRN)
        px(im, 4, 12, GRN)
        px(im, 5, 11, GRN)
        px(im, 6, 10, GRN_L)
        px(im, 3, 13, GRN_L)                         # leaves
        px(im, 2, 12, GRN)
        px(im, 5, 14, GRN)
        px(im, 6, 13, GRN_L)
    else:
        px(im, 3, 14, GRN_L)
        px(im, 5, 13, GRN)


def _fruit(im, x0, y0, size, kind):
    for dy in range(size):
        for dx in range(size):
            x, y = x0 + dx, y0 + dy
            edge = dx in (0, size - 1) or dy in (0, size - 1)
            if edge:
                c = K
            elif kind == 'melon':
                c = MEL_L if dx % 2 == 0 else MEL
                if dy == size - 2:
                    c = MEL_D
            else:
                c = ORG_L if dx % 2 == 0 else ORG
                if dy == size - 2:
                    c = ORG_D
            px(im, x, y, c)
    if kind == 'pumpkin':
        px(im, x0 + size // 2, y0 - 1, COP_D)        # stem
    if size >= 6:                                    # copper band + glow
        bx = x0 + size - 3
        for dy in range(1, size - 1):
            px(im, bx, y0 + dy, COP if dy % 2 else COP_D)
        px(im, bx, y0 + 1, COP_L)
        px(im, x0 + 2, y0 + 2, GLOW)


def gourd_stage(stage, kind):
    im = img()
    if stage == 0:
        _vine(im, False)
    elif stage == 1:
        _vine(im, True)
    elif stage == 2:
        _vine(im, True)
        _fruit(im, 9, 11, 4, kind)
    else:
        _vine(im, True)
        _fruit(im, 8, 8, 7, kind)
    return im


# ================================================================== items ==
ITEM_GRIDS = {
    'industrial_carrot': [
        '................',
        '......l..G......',
        '....G.lG.l.G....',
        '.....sGGGl......',
        '......sGs.......',
        '.....koOQOk.....',
        '....koOQQQOk....',
        '....koOQQOOk....',
        '....kcCC+Ck.....',
        '.....koOQOk.....',
        '.....koOOOk.....',
        '......koOOk.....',
        '......koOk......',
        '.......kRk......',
        '.......k*k......',
        '........k.......',
    ],
    'industrial_potato': [
        '................',
        '................',
        '.....kBBBk......',
        '....kBtttBk.....',
        '...kBtttttBk....',
        '..kBttcCCttBk...',
        '..kBtttCttttBk..',
        '..kBtttCttttBk..',
        '...kBtt*tttBk...',
        '...kBbttttbBk...',
        '....kBbttbBk....',
        '.....kBbbBk.....',
        '......kkkk......',
        '................',
        '................',
        '................',
    ],
    'industrial_wheat': [
        '................',
        '....WW.*W.WW....',
        '....YY.YY.YY....',
        '....YY.YY.YY....',
        '....yY.yY.yY....',
        '.....y..y..y....',
        '.....y..y..y....',
        '....cC+cC+Cc....',
        '.....y..y..y....',
        '.....y..y..y....',
        '....y..y..y.....',
        '....y..y..y.....',
        '...y...y...y....',
        '...y...y...y....',
        '................',
        '................',
    ],
    'industrial_melon': [
        '................',
        '................',
        '.....kkkkk......',
        '...kkMeMeMkk....',
        '..kMeMMeMMeMk...',
        '..kmMeM*eMMmk...',
        '.kmMecC+MeMMmk..',
        '.kmMecC+MeMMmk..',
        '.kmMmcC+eMMmmk..',
        '..kmMmC+MMmmk...',
        '..kmmMmMMmmk....',
        '...kmmmmmmk.....',
        '.....kkkkk......',
        '................',
        '................',
        '................',
    ],
    'industrial_pumpkin': [
        '................',
        '.......cc.......',
        '......ccc.......',
        '....kkOOOkk.....',
        '..kkOQQOQQOkk...',
        '..kOQQoOQoQQOk..',
        '.kOQQoO*QoQQOk..',
        '.kOQCC+QQoQQOk..',
        '.kOQQoOQQoQQOk..',
        '.koQQoOQQoQQok..',
        '..koQooOooQok...',
        '...kkoooook.....',
        '.....kkkk.......',
        '................',
        '................',
        '................',
    ],
    'carrot_mechanical_core': [
        '................',
        '....I..II..I....',
        '...kIIIIIIIIk...',
        '..kIInnkknnIIk..',
        '..kInk....knIk..',
        '.IIn..sGGs..nII.',
        '.IIn..kOOk..nII.',
        '.IIn.koQQOk.nII.',
        '.IIn.koQ*Ok.nII.',
        '.IIn..koOk..nII.',
        '..kInk.kRk.knIk.',
        '...kIInnnnIIk...',
        '....kIIIIIIk....',
        '....I..II..I....',
        '................',
        '................',
    ],
}


# ================================================================= blocks ==
def _frame(im):
    """Iron frame + corner rivets shared by industrial crop blocks."""
    for v in range(16):
        for (x, y) in ((v, 0), (v, 15), (0, v), (15, v)):
            px(im, x, y, IRON_D)
    for (x, y) in ((1, 1), (14, 1), (1, 14), (14, 14)):
        px(im, x, y, RIVET)
        px(im, x, y, RIVET)


def wheat_block_side():
    im = img()
    for y in range(16):
        for x in range(16):
            if y % 4 == 3:
                c = GLD_D
            elif (x * 7 + y * 3) % 11 == 0:
                c = GLD_L
            elif (x * 5 + y * 9) % 13 == 0:
                c = GLD_D
            else:
                c = GLD
            px(im, x, y, c)
    for sx in (3, 12):                               # copper straps
        for y in range(16):
            px(im, sx, y, COP if y % 4 else COP_D)
        for y in (2, 7, 12):
            px(im, sx, y, COP_L)
    _frame(im)
    return im


def wheat_block_top():
    im = img()
    for y in range(16):
        for x in range(16):
            if (x * 11 + y * 7) % 13 == 0:
                c = BRN_D                            # cut-stalk holes
            elif (x + y) % 2 == 0:
                c = GLD
            elif (x * 5 + y) % 7 == 0:
                c = GLD_L
            else:
                c = GLD_D
            px(im, x, y, c)
    _frame(im)
    return im


def melon_block_side():
    im = img()
    stripes = [1, 1, 0, 1, 0, 0, 1, 1, 0, 1, 1, 0, 0, 1, 0, 1]
    for y in range(16):
        for x in range(16):
            c = MEL if stripes[x] else MEL_L
            if (y * 3 + x * 5) % 9 == 0:
                c = MEL_D if stripes[x] else MEL
            px(im, x, y, c)
    _frame(im)
    return im


def melon_block_top():
    im = img()
    for y in range(16):
        for x in range(16):
            d = max(abs(x - 7.5), abs(y - 7.5))
            if d <= 1.5:
                c = MEL_D
            elif d <= 3.5:
                c = MEL
            elif d <= 5.5:
                c = MEL_L
            else:
                c = MEL
            if (x * 3 + y * 7) % 11 == 0:
                c = MEL_D
            px(im, x, y, c)
    _frame(im)
    return im


def pumpkin_block_side():
    im = img()
    for y in range(16):
        for x in range(16):
            m = x % 4
            c = (ORG_D, ORG, ORG_L, ORG)[m]
            if (y * 5 + x) % 9 == 0:
                c = ORG_D if m else BRN_D
            px(im, x, y, c)
    _frame(im)
    return im


def pumpkin_block_top():
    im = img()
    for y in range(16):
        for x in range(16):
            d = max(abs(x - 7.5), abs(y - 7.5))
            if d <= 1.5:
                c = COP_D                            # dried stem
            elif d <= 3.5:
                c = ORG
            elif d <= 5.5:
                c = ORG_L
            else:
                c = ORG
            if (x + y * 3) % 7 == 0 and d > 2:
                c = ORG_D
            px(im, x, y, c)
    _frame(im)
    return im


def _crate(im):
    for v in range(16):
        for (x, y) in ((v, 0), (v, 15), (0, v), (15, v)):
            px(im, x, y, BRN)
    for v in range(16):
        px(im, v, 0, TAN if v % 3 else BRN)
        px(im, v, 15, BRN_D)
    for (x, y) in ((0, 0), (15, 0), (0, 15), (15, 15)):
        px(im, x, y, BRN_D)


def carrot_block_side():
    im = img()
    for y in range(1, 15):
        for x in range(1, 15):
            m = (x + y) % 3
            c = (ORG, ORG_L, ORG_D)[m]
            if (x * 5 + y * 3) % 11 == 0:
                c = GRN                              # leaf tufts in the pile
            px(im, x, y, c)
    _crate(im)
    for y in range(1, 15):                           # copper strap
        px(im, 8, y, COP if y % 4 else COP_D)
    px(im, 8, 3, COP_L)
    px(im, 8, 11, COP_L)
    return im


def carrot_block_top():
    im = img()
    for y in range(1, 15):
        for x in range(1, 15):
            mx, my = x % 4, y % 4
            if mx == 0 or my == 0:
                c = ORG_D
            elif mx == 2 and my == 2:
                c = ORG_L
            else:
                c = ORG
            px(im, x, y, c)
    _crate(im)
    return im


def potato_block_side():
    im = img()
    for y in range(1, 15):
        for x in range(1, 15):
            if (x * 3 + y) % 7 < 2:
                c = BRN
            elif (x * 7 + y * 5) % 13 == 0:
                c = BRN_D                            # potato eyes
            else:
                c = TAN
            px(im, x, y, c)
    _crate(im)
    for y in range(1, 15):
        px(im, 8, y, COP if y % 4 else COP_D)
    px(im, 8, 3, COP_L)
    px(im, 8, 11, COP_L)
    return im


def potato_block_top():
    im = img()
    for y in range(1, 15):
        for x in range(1, 15):
            mx, my = x % 5, y % 4
            if mx in (0, 4) or my in (0, 3):
                c = BRN
            elif mx == 2 and my == 1:
                c = TAN
            else:
                c = BRN if (x + y) % 5 == 0 else TAN
            px(im, x, y, c)
    _crate(im)
    return im


def incubator_front():
    """Front hatch texture.

    The block entity renderer draws the UV window x=[3,13), y=[4,12) of this
    texture as a translucent glass pane in front of the incubator opening, so
    that whole region must carry semi-transparent alpha baked into the PNG
    (this replaces the old build.gradle generateIncubatorTexture task).
    The full 16x16 face is still shown opaque-framed on the block item model.
    """
    base = Image.open(os.path.join(BLOCK, 'iron_device_side.png')).convert('RGBA')
    im = base.copy()
    GLASS = (188, 224, 228, 110)
    GLASS_EDGE = (150, 196, 204, 150)
    SHEEN = (240, 250, 252, 170)
    TINT = (170, 220, 180, 118)                      # faint green from the vat
    # clear the hatch opening, then lay in translucent glass
    for y in range(4, 12):
        for x in range(3, 13):
            edge = x in (3, 12) or y in (4, 11)
            c = GLASS_EDGE if edge else GLASS
            if not edge and (x + y) % 5 == 0:
                c = TINT
            im.putpixel((x, y), c)
    # diagonal sheen streak
    for (x, y) in ((5, 6), (6, 7), (4, 5), (10, 9), (11, 10)):
        im.putpixel((x, y), SHEEN)
    # opaque iron frame with copper corner bolts around the hatch
    for v in range(2, 14):
        for (x, y) in ((v, 3), (v, 12), (2, v), (13, v)):
            px(im, x, y, IRON_D)
    for (x, y) in ((2, 3), (13, 3), (2, 12), (13, 12)):
        px(im, x, y, COP)
    # status lights
    px(im, 13, 1, RED)
    px(im, 2, 1, GLOW)
    return im


def main():
    # --- crop growth stages -------------------------------------------
    for stage in range(4):
        save(wheat_stage(stage), BLOCK, 'industrial_wheat_crop_stage%d' % stage)
        save(carrot_stage(stage), BLOCK, 'industrial_carrot_crop_stage%d' % stage)
        save(potato_stage(stage), BLOCK, 'industrial_potato_crop_stage%d' % stage)
        save(gourd_stage(stage, 'melon'), BLOCK, 'industrial_melon_crop_stage%d' % stage)
        save(gourd_stage(stage, 'pumpkin'), BLOCK, 'industrial_pumpkin_crop_stage%d' % stage)
    # --- items ---------------------------------------------------------
    for name, grid in ITEM_GRIDS.items():
        save(from_grid(grid), ITEM, name)
    # --- compressed crop blocks -----------------------------------------
    save(wheat_block_side(), BLOCK, 'industrial_wheat_block_side')
    save(wheat_block_top(), BLOCK, 'industrial_wheat_block_top')
    save(melon_block_side(), BLOCK, 'industrial_melon_block_side')
    save(melon_block_top(), BLOCK, 'industrial_melon_block_top')
    save(pumpkin_block_side(), BLOCK, 'industrial_pumpkin_block_side')
    save(pumpkin_block_top(), BLOCK, 'industrial_pumpkin_block_top')
    save(carrot_block_side(), BLOCK, 'carrot_block_side')
    save(carrot_block_top(), BLOCK, 'carrot_block_top')
    save(potato_block_side(), BLOCK, 'potato_block_side')
    save(potato_block_top(), BLOCK, 'potato_block_top')
    # --- machine face that was missing entirely --------------------------
    save(incubator_front(), BLOCK, 'incubator_front')
    print('done')


if __name__ == '__main__':
    main()
