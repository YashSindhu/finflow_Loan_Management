Write-Host "Stopping FinFlow..." -ForegroundColor Yellow
docker-compose down
Write-Host "All containers stopped. Data is preserved." -ForegroundColor Green
Write-Host ""
Write-Host "NOTE: If you see login issues next time, run this in browser console:" -ForegroundColor Gray
Write-Host "  localStorage.clear()" -ForegroundColor Gray
