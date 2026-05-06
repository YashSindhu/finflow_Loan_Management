Write-Host "Starting FinFlow..." -ForegroundColor Cyan
docker-compose up -d
Write-Host ""
Write-Host "Services starting up. Wait 2-3 minutes then open:" -ForegroundColor Yellow
Write-Host "  Frontend:  http://localhost:4200" -ForegroundColor Green
Write-Host "  Eureka:    http://localhost:8761" -ForegroundColor Green
Write-Host "  Swagger:   http://localhost:8080/swagger-ui.html" -ForegroundColor Green
Write-Host ""
Write-Host "To check status: docker-compose ps" -ForegroundColor Gray
