from __future__ import annotations

from pathlib import Path

from PIL import Image, ImageDraw


PROJECT_ROOT = Path(__file__).resolve().parents[1]
WORKSPACE_ROOT = PROJECT_ROOT.parent
OUTPUT_ROOT = (
    PROJECT_ROOT
    / "src/main/resources/assets/industrialcrops/textures/gui/container"
)

MEKANISM_BASE = (
    WORKSPACE_ROOT
    / "mek/Mekanism-1.21.x/Mekanism-1.21.x/src/main/resources/assets/mekanism/gui/base.png"
)
REFINED_FILTER = (
    WORKSPACE_ROOT
    / "rs2/Refined_Storage_images/assets/refinedstorage/textures/gui/generic_filter.png"
)

TRANSPARENT = (0, 0, 0, 0)
PANEL = (198, 198, 198, 255)
SECTION = (190, 190, 190, 255)
LIGHT_WELL = (219, 219, 219, 255)
SLOT = (139, 139, 139, 255)
SHADOW = (55, 55, 55, 255)
DARK_SHADOW = (85, 85, 85, 255)
HIGHLIGHT = (255, 255, 255, 255)
OUTLINE = (0, 0, 0, 255)
DISABLED = (91, 91, 91, 255)
ACTIVE = (184, 90, 48, 255)


def recolor_reference_base(image: Image.Image) -> Image.Image:
    """Normalize Mekanism's nine-slice geometry to the vanilla/RS palette."""
    result = image.convert("RGBA").copy()
    remap = {
        (190, 190, 190, 255): PANEL,
        (67, 67, 67, 255): DARK_SHADOW,
    }
    pixels = result.load()
    for y in range(result.height):
        for x in range(result.width):
            pixels[x, y] = remap.get(pixels[x, y], pixels[x, y])
    return result


def nine_slice(source: Image.Image, width: int, height: int, border: int = 4) -> Image.Image:
    if width < border * 2 or height < border * 2:
        raise ValueError(f"nine-slice target is too small: {width}x{height}")

    result = Image.new("RGBA", (width, height), TRANSPARENT)
    sw, sh = source.size
    areas = [
        ((0, 0, border, border), (0, 0, border, border)),
        ((sw - border, 0, sw, border), (width - border, 0, width, border)),
        ((0, sh - border, border, sh), (0, height - border, border, height)),
        ((sw - border, sh - border, sw, sh), (width - border, height - border, width, height)),
        ((border, 0, sw - border, border), (border, 0, width - border, border)),
        ((border, sh - border, sw - border, sh), (border, height - border, width - border, height)),
        ((0, border, border, sh - border), (0, border, border, height - border)),
        ((sw - border, border, sw, sh - border), (width - border, border, width, height - border)),
        ((border, border, sw - border, sh - border), (border, border, width - border, height - border)),
    ]
    for source_box, target_box in areas:
        target_width = target_box[2] - target_box[0]
        target_height = target_box[3] - target_box[1]
        piece = source.crop(source_box)
        if piece.size != (target_width, target_height):
            piece = piece.resize((target_width, target_height), Image.Resampling.NEAREST)
        result.alpha_composite(piece, (target_box[0], target_box[1]))
    return result


def draw_inset(
    image: Image.Image,
    x: int,
    y: int,
    width: int,
    height: int,
    fill: tuple[int, int, int, int] = SECTION,
) -> None:
    """Draw the same one-pixel bevel used by vanilla item slots."""
    draw = ImageDraw.Draw(image)
    draw.rectangle((x + 1, y + 1, x + width - 2, y + height - 2), fill=fill)
    draw.line((x, y, x + width - 2, y), fill=SHADOW)
    draw.line((x, y, x, y + height - 2), fill=SHADOW)
    draw.line((x + 1, y + height - 1, x + width - 1, y + height - 1), fill=HIGHLIGHT)
    draw.line((x + width - 1, y + 1, x + width - 1, y + height - 1), fill=HIGHLIGHT)
    draw.point((x + width - 1, y), fill=fill)
    draw.point((x, y + height - 1), fill=fill)


def draw_raised(
    image: Image.Image,
    x: int,
    y: int,
    width: int,
    height: int,
    fill: tuple[int, int, int, int] = SECTION,
) -> None:
    draw = ImageDraw.Draw(image)
    draw.rectangle((x, y, x + width - 1, y + height - 1), fill=fill)
    draw.line((x, y, x + width - 1, y), fill=HIGHLIGHT)
    draw.line((x, y, x, y + height - 1), fill=HIGHLIGHT)
    draw.line((x, y + height - 1, x + width - 1, y + height - 1), fill=DARK_SHADOW)
    draw.line((x + width - 1, y, x + width - 1, y + height - 1), fill=DARK_SHADOW)


def make_disabled_slot(slot: Image.Image) -> Image.Image:
    result = slot.copy()
    pixels = result.load()
    for y in range(result.height):
        for x in range(result.width):
            if pixels[x, y] == SLOT:
                pixels[x, y] = DISABLED
    return result


def draw_arrow(width: int, active: bool) -> Image.Image:
    height = 17
    result = Image.new("RGBA", (width, height), TRANSPARENT)
    draw = ImageDraw.Draw(result)
    tip = width - 1
    shoulder = max(5, width - 8)
    points = [
        (0, 6),
        (shoulder, 6),
        (shoulder, 3),
        (tip, 8),
        (shoulder, 14),
        (shoulder, 11),
        (0, 11),
    ]
    draw.polygon(points, fill=SHADOW)
    inner = [
        (1, 7),
        (shoulder + 1, 7),
        (shoulder + 1, 5),
        (tip - 2, 8),
        (shoulder + 1, 12),
        (shoulder + 1, 10),
        (1, 10),
    ]
    draw.polygon(inner, fill=ACTIVE if active else SLOT)
    return result


def put_slot(image: Image.Image, slot: Image.Image, menu_x: int, menu_y: int) -> None:
    image.alpha_composite(slot, (menu_x - 1, menu_y - 1))


def put_slot_frame(image: Image.Image, slot: Image.Image, x: int, y: int) -> None:
    image.alpha_composite(slot, (x, y))


def draw_player_inventory(
    image: Image.Image,
    slot: Image.Image,
    x: int,
    inventory_y: int,
    hotbar_y: int,
) -> None:
    for row in range(3):
        for column in range(9):
            put_slot(image, slot, x + column * 18, inventory_y + row * 18)
    for column in range(9):
        put_slot(image, slot, x + column * 18, hotbar_y)


def save(image: Image.Image, name: str) -> None:
    OUTPUT_ROOT.mkdir(parents=True, exist_ok=True)
    image.save(OUTPUT_ROOT / name, optimize=True)


def generate_screen_backgrounds(base: Image.Image, slot: Image.Image) -> dict[str, Image.Image]:
    screens: dict[str, Image.Image] = {}

    root = nine_slice(base, 176, 166)
    put_slot(root, slot, 56, 17)
    put_slot(root, slot, 56, 53)
    put_slot(root, slot, 116, 35)
    draw_player_inventory(root, slot, 8, 84, 142)
    screens["root_ore_extractor.png"] = root

    compressor = nine_slice(base, 176, 166)
    put_slot(compressor, slot, 56, 35)
    put_slot(compressor, slot, 116, 35)
    draw_player_inventory(compressor, slot, 8, 84, 142)
    screens["crop_compressor.png"] = compressor

    controller = nine_slice(base, 176, 166)
    draw_inset(controller, 7, 16, 162, 52, LIGHT_WELL)
    draw_player_inventory(controller, slot, 8, 84, 142)
    screens["basic_control_device.png"] = controller

    reinforced = nine_slice(base, 176, 222)
    for row in range(6):
        for column in range(9):
            put_slot(reinforced, slot, 8 + column * 18, 18 + row * 18)
    draw_player_inventory(reinforced, slot, 8, 140, 198)
    screens["reinforced_control_device.png"] = reinforced

    incubator = nine_slice(base, 176, 166)
    put_slot(incubator, slot, 80, 35)
    draw_player_inventory(incubator, slot, 8, 84, 142)
    screens["incubator.png"] = incubator

    slime_incubator = nine_slice(base, 176, 166)
    put_slot(slime_incubator, slot, 44, 35)
    put_slot(slime_incubator, slot, 116, 35)
    draw_player_inventory(slime_incubator, slot, 8, 84, 142)
    screens["slime_incubator.png"] = slime_incubator

    silo = nine_slice(base, 176, 202)
    draw_inset(silo, 7, 17, 162, 100, SECTION)
    put_slot(silo, slot, 18, 36)
    for index in range(5):
        put_slot(silo, slot, 18 + index * 22, 84)
    draw_player_inventory(silo, slot, 8, 120, 178)
    screens["golden_launch_silo.png"] = silo

    advanced = nine_slice(base, 302, 240)
    draw_inset(advanced, 5, 24, 168, 128, PANEL)
    draw_inset(advanced, 173, 24, 25, 128, PANEL)
    draw_inset(advanced, 201, 24, 96, 128, PANEL)
    draw_inset(advanced, 5, 154, 168, 82, PANEL)
    for index in range(6):
        put_slot(advanced, slot, 176, 28 + index * 18)
    for row in range(3):
        for column in range(3):
            put_slot(advanced, slot, 208 + column * 18, 45 + row * 18)
    put_slot(advanced, slot, 276, 63)
    draw_player_inventory(advanced, slot, 8, 158, 216)
    screens["advanced_industrial_storage.png"] = advanced

    manipulator = nine_slice(base, 302, 240)
    draw_raised(manipulator, 6, 20, 96, 110, PANEL)
    draw_inset(manipulator, 14, 34, 80, 56, LIGHT_WELL)
    draw_raised(manipulator, 106, 20, 96, 110, PANEL)
    draw_raised(manipulator, 206, 20, 90, 110, PANEL)
    for x, y in ((216, 50), (254, 50), (216, 88), (254, 88)):
        put_slot_frame(manipulator, slot, x, y)
    draw_raised(manipulator, 67, 154, 168, 82, PANEL)
    draw_player_inventory(manipulator, slot, 70, 158, 216)
    screens["basic_manipulator.png"] = manipulator

    return screens


def generate_jei_backgrounds(base: Image.Image, inactive_arrow: Image.Image) -> dict[str, Image.Image]:
    backgrounds: dict[str, Image.Image] = {}

    compressor = nine_slice(base, 88, 36)
    compressor.alpha_composite(inactive_arrow.resize((20, 13), Image.Resampling.NEAREST), (34, 11))
    backgrounds["jei_crop_compressor.png"] = compressor

    extractor = nine_slice(base, 112, 62)
    extractor.alpha_composite(inactive_arrow, (42, 23))
    backgrounds["jei_root_ore_extractor.png"] = extractor

    manipulator = nine_slice(base, 118, 54)
    draw_inset(manipulator, 35, 8, 28, 38, LIGHT_WELL)
    backgrounds["jei_manipulator.png"] = manipulator

    return backgrounds


def generate_widgets(slot: Image.Image) -> Image.Image:
    atlas = Image.new("RGBA", (128, 64), TRANSPARENT)
    atlas.alpha_composite(slot, (0, 0))
    atlas.alpha_composite(make_disabled_slot(slot), (18, 0))
    atlas.alpha_composite(draw_arrow(28, False), (36, 0))
    atlas.alpha_composite(draw_arrow(28, True), (64, 0))

    draw = ImageDraw.Draw(atlas)
    # A compact lock used on storage slots that are not unlocked yet.
    draw.rectangle((98, 3, 103, 7), fill=SHADOW)
    draw.rectangle((97, 7, 104, 13), fill=DARK_SHADOW)
    draw.rectangle((99, 9, 102, 12), fill=OUTLINE)
    return atlas


def main() -> None:
    if not MEKANISM_BASE.is_file():
        raise FileNotFoundError(MEKANISM_BASE)
    if not REFINED_FILTER.is_file():
        raise FileNotFoundError(REFINED_FILTER)

    base = recolor_reference_base(Image.open(MEKANISM_BASE))
    refined = Image.open(REFINED_FILTER).convert("RGBA")
    slot = refined.crop((7, 19, 25, 37))

    for name, image in generate_screen_backgrounds(base, slot).items():
        save(image, name)
    inactive_arrow = draw_arrow(28, False)
    for name, image in generate_jei_backgrounds(base, inactive_arrow).items():
        save(image, name)
    save(generate_widgets(slot), "widgets.png")

    print(f"Generated GUI textures in {OUTPUT_ROOT}")


if __name__ == "__main__":
    main()
