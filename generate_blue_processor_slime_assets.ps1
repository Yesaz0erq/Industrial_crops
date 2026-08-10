param(
    [Parameter(Mandatory = $true)]
    [string]$ModelPath
)

$ErrorActionPreference = 'Stop'
Add-Type -AssemblyName System.Drawing

$projectRoot = $PSScriptRoot
$modelPath = (Resolve-Path -LiteralPath $ModelPath).Path
$assetRoot = Join-Path $projectRoot 'src\main\resources\assets\industrialcrops'

function New-Face([int[]]$uv, [int[]]$uvSize) {
    [pscustomobject][ordered]@{ uv = @($uv); uv_size = @($uvSize) }
}

function New-ProcessorUv {
    [pscustomobject][ordered]@{
        north = New-Face @(0, 16) @(16, 16)
        east = New-Face @(0, 16) @(16, 16)
        south = New-Face @(0, 16) @(16, 16)
        west = New-Face @(0, 16) @(16, 16)
        up = New-Face @(16, 32) @(-16, -16)
        down = New-Face @(16, 32) @(-16, -16)
    }
}

function Bitmap-FromDataUrl([string]$dataUrl) {
    $base64 = $dataUrl.Substring($dataUrl.IndexOf(',') + 1)
    $bytes = [Convert]::FromBase64String($base64)
    $stream = [System.IO.MemoryStream]::new($bytes)
    $source = [System.Drawing.Bitmap]::new($stream)
    $bitmap = [System.Drawing.Bitmap]::new($source)
    $source.Dispose()
    $stream.Dispose()
    return $bitmap
}

$project = Get-Content -LiteralPath $modelPath -Raw | ConvertFrom-Json
$bodyAtlas = Bitmap-FromDataUrl $project.textures[0].source
$processorAnimation = Bitmap-FromDataUrl $project.textures[1].source

# Use the brightest processing frame for the in-game atlas. The processor bone itself animates in GeckoLib.
for ($y = 0; $y -lt 16; $y++) {
    for ($x = 0; $x -lt 16; $x++) {
        $bodyAtlas.SetPixel($x, 16 + $y, $processorAnimation.GetPixel($x, 128 + $y))
    }
}
$processorAnimation.Dispose()
$texturePath = Join-Path $assetRoot 'textures\entity\diamond_processor_slime.png'
New-Item -ItemType Directory -Force -Path (Split-Path -Parent $texturePath) | Out-Null
$bodyAtlas.Save($texturePath, [System.Drawing.Imaging.ImageFormat]::Png)
$bodyAtlas.Dispose()

$sourceGeoPath = Join-Path $assetRoot 'geo\gray_gear_slime_v2.geo.json'
$geo = Get-Content -LiteralPath $sourceGeoPath -Raw | ConvertFrom-Json
$geometry = $geo.'minecraft:geometry'[0]
$geometry.description.identifier = 'geometry.diamond_processor_slime'

$rootBone = $geometry.bones | Where-Object name -eq 'root'
$bodyBone = $geometry.bones | Where-Object name -eq 'body'
$faceBone = $geometry.bones | Where-Object name -eq 'face'
$coreBone = $geometry.bones | Where-Object name -eq 'core'
$coreBone.cubes = @()
$processorBone = [pscustomobject][ordered]@{
    name = 'processor'
    parent = 'core'
    pivot = @(0.0, 8.0, 0.0)
    cubes = @(
        [pscustomobject][ordered]@{
            origin = @(-3.8, 4.2, -3.8)
            size = @(7.6, 7.6, 7.6)
            uv = New-ProcessorUv
        }
    )
}
$geometry.bones = @($rootBone, $bodyBone, $faceBone, $coreBone, $processorBone)

$geoPath = Join-Path $assetRoot 'geo\diamond_processor_slime.geo.json'
$geo | ConvertTo-Json -Depth 100 | Set-Content -LiteralPath $geoPath -Encoding utf8

$sourceAnimationPath = Join-Path $assetRoot 'animations\golden_gear_slime_v2.animation.json'
$animations = Get-Content -LiteralPath $sourceAnimationPath -Raw | ConvertFrom-Json
$updatedAnimations = [ordered]@{}
foreach ($property in $animations.animations.psobject.Properties) {
    $name = $property.Name -replace 'golden_redstone_lamp_slime', 'diamond_processor_slime'
    $animation = $property.Value | ConvertTo-Json -Depth 100 | ConvertFrom-Json
    if ($name -eq 'animation.diamond_processor_slime.idle') {
        $animation.bones | Add-Member -NotePropertyName processor -NotePropertyValue ([pscustomobject][ordered]@{
            rotation = [pscustomobject][ordered]@{
                '0.0' = [pscustomobject][ordered]@{ vector = @(0, 0, 0) }
                '0.5' = [pscustomobject][ordered]@{ vector = @(0, 90, 0) }
                '1.0' = [pscustomobject][ordered]@{ vector = @(0, 180, 0) }
                '1.5' = [pscustomobject][ordered]@{ vector = @(0, 270, 0) }
                '2.0' = [pscustomobject][ordered]@{ vector = @(0, 360, 0) }
            }
            scale = [pscustomobject][ordered]@{
                '0.0' = [pscustomobject][ordered]@{ vector = @(0.94, 0.94, 0.94) }
                '0.5' = [pscustomobject][ordered]@{ vector = @(1.04, 1.04, 1.04) }
                '1.0' = [pscustomobject][ordered]@{ vector = @(0.94, 0.94, 0.94) }
                '1.5' = [pscustomobject][ordered]@{ vector = @(1.04, 1.04, 1.04) }
                '2.0' = [pscustomobject][ordered]@{ vector = @(0.94, 0.94, 0.94) }
            }
        })
    }
    $updatedAnimations[$name] = $animation
}
$animations.animations = [pscustomobject]$updatedAnimations
$animationPath = Join-Path $assetRoot 'animations\diamond_processor_slime.animation.json'
$animations | ConvertTo-Json -Depth 100 | Set-Content -LiteralPath $animationPath -Encoding utf8

[pscustomobject]@{
    Geo = $geoPath
    Animation = $animationPath
    Texture = $texturePath
} | Format-List
