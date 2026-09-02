Add-Type -AssemblyName System.Drawing

$textureRoot = Join-Path $PSScriptRoot '..\src\main\resources\assets\industrialcrops\textures'
$itemRoot = Join-Path $textureRoot 'item'
$blockRoot = Join-Path $textureRoot 'block'
$substratePath = Join-Path $itemRoot 'component_substrate.png'

function Copy-Substrate([string] $outputPath) {
    $source = [Drawing.Bitmap]::new((Resolve-Path -LiteralPath $substratePath).Path)
    $copy = [Drawing.Bitmap]::new($source)
    $source.Dispose()
    return $copy
}

function Pixel($bitmap, [int] $x, [int] $y, [string] $html) {
    $bitmap.SetPixel($x, $y, [Drawing.ColorTranslator]::FromHtml($html))
}

function Shift-OverlayLeft([Drawing.Bitmap] $bitmap) {
    $base = Copy-Substrate
    $changes = @()
    for ($y = 0; $y -lt 16; $y++) {
        for ($x = 0; $x -lt 16; $x++) {
            $color = $bitmap.GetPixel($x, $y)
            if ($color.ToArgb() -ne $base.GetPixel($x, $y).ToArgb()) {
                $changes += ,@($x, $y, $color)
                $bitmap.SetPixel($x, $y, $base.GetPixel($x, $y))
            }
        }
    }
    foreach ($change in $changes) {
        $bitmap.SetPixel([Math]::Max(0, $change[0] - 1), $change[1], $change[2])
    }
    $base.Dispose()
}

# Automatic component: two balanced chasing arrows in an exact 8x8 centered field.
$auto = Copy-Substrate
foreach ($p in @(@(5,5),@(6,4),@(7,4),@(8,4),@(9,5),@(10,5),@(10,6),@(9,6))) { Pixel $auto $p[0] $p[1] '#30CFF2' }
foreach ($p in @(@(10,5),@(11,6),@(10,7))) { Pixel $auto $p[0] $p[1] '#BFF8FF' }
foreach ($p in @(@(10,10),@(9,11),@(8,11),@(7,11),@(6,10),@(5,10),@(5,9),@(6,9))) { Pixel $auto $p[0] $p[1] '#F2A72B' }
foreach ($p in @(@(5,10),@(4,9),@(5,8))) { Pixel $auto $p[0] $p[1] '#FFE89A' }
foreach ($p in @(@(5,6),@(4,7),@(4,8),@(11,7),@(11,8),@(10,9))) { Pixel $auto $p[0] $p[1] '#176080' }
Shift-OverlayLeft $auto
$auto.Save((Join-Path $itemRoot 'automatic_component.png'), [Drawing.Imaging.ImageFormat]::Png)
$auto.Dispose()

# Dimension component: a centered 8x8 cyan portal with a violet frame.
$dimension = Copy-Substrate
foreach ($p in @(@(6,4),@(7,4),@(8,4),@(9,4),@(5,5),@(10,5),@(4,6),@(11,6),@(4,7),@(11,7),@(4,8),@(11,8),@(4,9),@(11,9),@(5,10),@(10,10),@(6,11),@(7,11),@(8,11),@(9,11))) { Pixel $dimension $p[0] $p[1] '#7058D6' }
foreach ($p in @(@(6,5),@(7,5),@(8,5),@(9,5),@(5,6),@(10,6),@(5,7),@(10,7),@(5,8),@(10,8),@(5,9),@(10,9),@(6,10),@(7,10),@(8,10),@(9,10))) { Pixel $dimension $p[0] $p[1] '#2CAFE0' }
foreach ($p in @(@(7,6),@(8,6),@(6,7),@(7,7),@(8,7),@(9,7),@(6,8),@(7,8),@(8,8),@(9,8),@(7,9),@(8,9))) { Pixel $dimension $p[0] $p[1] '#BDF7FF' }
foreach ($p in @(@(6,4),@(5,5),@(10,10),@(9,11))) { Pixel $dimension $p[0] $p[1] '#D6B4FF' }
Shift-OverlayLeft $dimension
$dimension.Save((Join-Path $itemRoot 'dimension_upgrade_component.png'), [Drawing.Imaging.ImageFormat]::Png)
$dimension.Dispose()

# Infinite dimension component: interlocked cyan/violet infinity loops.
$infinite = Copy-Substrate
foreach ($p in @(@(4,7),@(5,6),@(6,6),@(7,7),@(8,8),@(9,9),@(10,9),@(11,8),@(11,7),@(10,6),@(9,6),@(8,7),@(7,8),@(6,9),@(5,9),@(4,8))) { Pixel $infinite $p[0] $p[1] '#8B63E6' }
foreach ($p in @(@(5,7),@(6,7),@(7,8),@(8,7),@(9,7),@(10,7),@(10,8),@(9,8),@(8,8),@(7,7),@(6,8),@(5,8))) { Pixel $infinite $p[0] $p[1] '#2CD8ED' }
foreach ($p in @(@(4,7),@(7,7),@(8,8),@(11,8))) { Pixel $infinite $p[0] $p[1] '#DDFBFF' }
Shift-OverlayLeft $infinite
$infinite.Save((Join-Path $itemRoot 'infinite_dimension_upgrade_component.png'), [Drawing.Imaging.ImageFormat]::Png)
$infinite.Dispose()

function Copy-BlockTexture([string] $name) {
    $sourcePath = Join-Path $blockRoot $name
    $source = [Drawing.Bitmap]::new((Resolve-Path -LiteralPath $sourcePath).Path)
    $copy = [Drawing.Bitmap]::new($source)
    $source.Dispose()
    return $copy
}

function Draw-ManipulatorFront([Drawing.Bitmap] $bitmap, [string[]] $palette) {
    $dark = $palette[0]; $mid = $palette[1]; $bright = $palette[2]
    for ($y = 3; $y -le 12; $y++) {
        for ($x = 3; $x -le 12; $x++) { Pixel $bitmap $x $y '#211E1C' }
    }
    foreach ($p in @(@(3,3),@(4,3),@(11,3),@(12,3),@(3,12),@(4,12),@(11,12),@(12,12))) { Pixel $bitmap $p[0] $p[1] $mid }
    foreach ($p in @(@(7,4),@(8,4),@(6,5),@(7,5),@(8,5),@(9,5),@(5,6),@(10,6),@(4,7),@(5,7),@(10,7),@(11,7),@(4,8),@(5,8),@(10,8),@(11,8),@(5,9),@(6,9),@(9,9),@(10,9),@(6,10),@(9,10))) { Pixel $bitmap $p[0] $p[1] $dark }
    foreach ($p in @(@(7,4),@(8,4),@(6,5),@(9,5),@(5,6),@(10,6),@(4,7),@(11,7),@(5,9),@(10,9),@(6,10),@(9,10))) { Pixel $bitmap $p[0] $p[1] $mid }
    foreach ($p in @(@(6,5),@(9,5),@(5,6),@(10,6),@(4,7),@(11,7))) { Pixel $bitmap $p[0] $p[1] $bright }
    foreach ($p in @(@(7,7),@(8,7),@(7,8),@(8,8))) { Pixel $bitmap $p[0] $p[1] '#26CBE8' }
    Pixel $bitmap 7 7 '#B8FBFF'
}

# Matching basic/advanced manipulation fronts, both built from their own casing side texture.
$basicFront = Copy-BlockTexture 'device_side.png'
Draw-ManipulatorFront $basicFront @('#6E291D', '#B84F31', '#F08255')
$basicFront.Save((Join-Path $blockRoot 'manipulator_front.png'), [Drawing.Imaging.ImageFormat]::Png)
$basicFront.Dispose()

$advancedFront = Copy-BlockTexture 'processor_gold_device_side.png'
Draw-ManipulatorFront $advancedFront @('#7A4A00', '#C48809', '#FFE05A')
$advancedFront.Save((Join-Path $blockRoot 'advanced_manipulation_device_front.png'), [Drawing.Imaging.ImageFormat]::Png)
$advancedFront.Dispose()

function Remove-BlackGrooves([Drawing.Bitmap] $bitmap) {
    for ($y = 0; $y -lt 16; $y++) {
        for ($x = 0; $x -lt 16; $x++) {
            $color = $bitmap.GetPixel($x, $y)
            $luma = [int](0.2126 * $color.R + 0.7152 * $color.G + 0.0722 * $color.B)
            if ($luma -lt 72) {
                $replacement = if ((($x + $y) % 3) -eq 0) { '#783524' } else { '#8D432D' }
                Pixel $bitmap $x $y $replacement
            }
        }
    }
}

# Dedicated side, top, and bottom materials preserve the original machine-face UV language
# while replacing black grooves with copper shades.
$frame = Copy-BlockTexture 'device_side.png'
Remove-BlackGrooves $frame
$frame.Save((Join-Path $blockRoot 'copper_fluid_storage_cabinet_frame.png'), [Drawing.Imaging.ImageFormat]::Png)
$frame.Dispose()

$cabinetTop = Copy-BlockTexture 'device_top.png'
Remove-BlackGrooves $cabinetTop
$cabinetTop.Save((Join-Path $blockRoot 'copper_fluid_storage_cabinet_top.png'), [Drawing.Imaging.ImageFormat]::Png)
$cabinetTop.Dispose()

$cabinetBottom = Copy-BlockTexture 'device_bottom.png'
Remove-BlackGrooves $cabinetBottom
$cabinetBottom.Save((Join-Path $blockRoot 'copper_fluid_storage_cabinet_bottom.png'), [Drawing.Imaging.ImageFormat]::Png)
$cabinetBottom.Dispose()

# Cutout glass: fully transparent pane with a restrained cyan rim/highlight.
$glass = [Drawing.Bitmap]::new(16, 16, [Drawing.Imaging.PixelFormat]::Format32bppArgb)
for ($i = 0; $i -lt 16; $i++) {
    $edge = if (($i -eq 0) -or ($i -eq 15)) { '#A6E7ED' } else { '#568D96' }
    Pixel $glass $i 0 $edge
    Pixel $glass $i 15 $edge
    Pixel $glass 0 $i $edge
    Pixel $glass 15 $i $edge
}
foreach ($p in @(@(2,2),@(3,2),@(2,3),@(11,12),@(12,12),@(12,11))) { Pixel $glass $p[0] $p[1] '#D5FAFF' }
$glass.Save((Join-Path $blockRoot 'copper_fluid_tank_glass.png'), [Drawing.Imaging.ImageFormat]::Png)
$glass.Dispose()
