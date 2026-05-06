Write-Host "Building all services..." -ForegroundColor Cyan

Set-Location auth-service
mvn clean package -DskipTests -q
Write-Host "auth-service done" -ForegroundColor Green
Set-Location ..

Set-Location application-service
mvn clean package -DskipTests -q
Write-Host "application-service done" -ForegroundColor Green
Set-Location ..

Set-Location document-service
mvn clean package -DskipTests -q
Write-Host "document-service done" -ForegroundColor Green
Set-Location ..

Set-Location admin-service
mvn clean package -DskipTests -q
Write-Host "admin-service done" -ForegroundColor Green
Set-Location ..

Set-Location api-gateway
mvn clean package -DskipTests -q
Write-Host "api-gateway done" -ForegroundColor Green
Set-Location ..

Set-Location config-server
mvn clean package -DskipTests -q
Write-Host "config-server done" -ForegroundColor Green
Set-Location ..

Set-Location eureka-server
mvn clean package -DskipTests -q
Write-Host "eureka-server done" -ForegroundColor Green
Set-Location ..

Write-Host "Building Angular frontend..." -ForegroundColor Cyan
Set-Location finclient
ng build --configuration production
Set-Location ..
Write-Host "finclient done" -ForegroundColor Green

Write-Host "All built! Starting Docker..." -ForegroundColor Cyan
docker-compose down
docker-compose up --build -d
Write-Host "Done! All services running" -ForegroundColor Green

Write-Host ""
Write-Host "Services starting up. Wait 2-3 minutes then open:" -ForegroundColor Yellow
Write-Host "  Frontend:  http://localhost:4200" -ForegroundColor Green
Write-Host "  Eureka:    http://localhost:8761" -ForegroundColor Green
Write-Host "  Swagger:   http://localhost:8080/swagger-ui.html" -ForegroundColor Green
Write-Host ""
Write-Host "To check status: docker-compose ps" -ForegroundColor Gray