$ErrorActionPreference = "Stop"
$container = "football-scouting-postgres"
$db = "football_scouting"
$user = "scouting_user"
$seedDir = Split-Path -Parent $MyInvocation.MyCommand.Path

Write-Host "Verification du conteneur PostgreSQL..."
docker inspect $container | Out-Null

$files = @(
  "00_reset.sql",
  "01_clubs.sql",
  "02_joueurs.sql",
  "03_rapports.sql",
  "04_notes.sql",
  "05_scores.sql",
  "06_verify.sql"
)

foreach ($file in $files) {
  $path = Join-Path $seedDir $file
  Write-Host "Execution de $file..."
  Get-Content -Raw -Encoding UTF8 $path | docker exec -i $container psql -v ON_ERROR_STOP=1 -U $user -d $db
  if ($LASTEXITCODE -ne 0) { throw "Echec pendant $file" }
}

Write-Host "Seed termine avec succes."
