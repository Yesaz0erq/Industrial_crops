Add-Type -AssemblyName System.Drawing

$texturePath = Join-Path $PSScriptRoot '..\src\main\resources\assets\industrialcrops\textures\gui\container\basic_manipulation_device.png'
$resolvedPath = (Resolve-Path -LiteralPath $texturePath).Path
$source = [System.Drawing.Bitmap]::new($resolvedPath)
$bitmap = [System.Drawing.Bitmap]::new($source)
$source.Dispose()

$changed = 0
for ($y = 0; $y -lt $bitmap.Height; $y++) {
    for ($x = 0; $x -lt $bitmap.Width; $x++) {
        $pixel = $bitmap.GetPixel($x, $y)
        if ($pixel.A -eq 255 -and $pixel.R -eq 198 -and $pixel.G -eq 198 -and $pixel.B -eq 198) {
            $bitmap.SetPixel($x, $y, [System.Drawing.Color]::FromArgb(255, 190, 190, 190))
            $changed++
        }
    }
}

$bitmap.Save($resolvedPath, [System.Drawing.Imaging.ImageFormat]::Png)
$bitmap.Dispose()
Write-Output "Normalized $changed background pixels in $resolvedPath"
