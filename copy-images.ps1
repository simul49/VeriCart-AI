$src = "e:\CodeBuddy\VeriCart AI\frontend\src\images"
$dst = "e:\CodeBuddy\VeriCart AI\frontend\public\images"
New-Item -ItemType Directory -Force -Path $dst | Out-Null
$map = @()
Get-ChildItem $src | ForEach-Object {
  $ext = $_.Extension
  $clean = $_.BaseName -replace '[^A-Za-z0-9]+', '-' -replace '^-+', '' -replace '-+$', ''
  if ($clean -eq '') { $clean = 'img' }
  $n = "$clean$ext"
  $f = Join-Path $dst $n
  if (Test-Path $f) {
    $n = "$clean-$(Get-Random)$ext"
    $f = Join-Path $dst $n
  }
  Copy-Item $_.FullName $f
  $map += [pscustomobject]@{ original = $_.Name; url = "/images/$n" }
}
$map | Export-Csv -Path (Join-Path $dst "IMAGE_MAPPING.csv") -NoTypeInformation
Write-Host "Copied $($map.Count) files -> public/images/"
