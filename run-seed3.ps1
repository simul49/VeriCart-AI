$OutputEncoding = New-Object System.Text.UTF8Encoding($false)
$sql = @"
-- Remove the 5 redundant inserts (keep 190501 Echo Dot, which was genuinely new)
DELETE FROM product WHERE id IN (190499,190500,190502,190503,190504);

-- Push real local images onto ALL existing rows of these brands (by name, catches every duplicate copy)
UPDATE product SET images = '["/images/53-1-1-Anker-30W-Nano-GaN-Charger-1.jpg","/images/53-1-1-Anker-30W-Nano-GaN-Charger-2.jpg"]' WHERE name = 'Anker 30W Nano GaN Charger';
UPDATE product SET images = '["/images/54-2-Xiaomi-67W-Turbo-Fast-Charger.png","/images/Xiaomi-67W-Turbo-Fast-Charger.jpeg"]' WHERE name = 'Xiaomi 67W Turbo Fast Charger';
UPDATE product SET images = '["/images/Xiaomi-Smart-Speaker-2.png"]' WHERE name = 'Xiaomi Smart Speaker 2';
UPDATE product SET images = '["/images/57-1-1-Gap-Kids-Logo-Tee.png","/images/57-1-2-Gap-Kids-Logo-Tee.png"]' WHERE name = 'Gap Kids Logo Tee';
UPDATE product SET images = '["/images/58-2-Carter-s-Baby-Romper.png"]' WHERE name = 'Carter''s Baby Romper';
"@

$sql | & "C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe" -u root -pparadox49 vericart
Write-Host "final cleanup finished"
