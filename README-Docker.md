# Financial Application - Docker Setup

This document provides instructions for running the Financial Application using Docker and Docker Compose.

## Prerequisites

- Docker Desktop installed and running
- Docker Compose (included with Docker Desktop)

## Quick Start

### 1. Build and Run with Docker Compose

```bash
# Build and start all services
docker-compose up --build

# Run in detached mode (background)
docker-compose up -d --build
```

### 2. Access the Application

- **API**: http://localhost:8081/api/hello
- **Swagger UI**: http://localhost:8081/swagger-ui.html
- **MySQL**: localhost:3306

### 3. Stop the Application

```bash
# Stop all services
docker-compose down

# Stop and remove volumes (WARNING: This will delete all data)
docker-compose down -v
```

## Services

### MySQL Database
- **Container**: financial-mysql
- **Port**: 3306
- **Database**: financial_db
- **Username**: financial_user
- **Password**: financial_password
- **Root Password**: 065516010@Ammar

### Spring Boot Application
- **Container**: financial-app
- **Port**: 8081
- **Profile**: docker
- **Health Check**: http://localhost:8081/api/hello

## Docker Commands

### Build the Application Image

```bash
# Build the Spring Boot application image
docker build -t financial-app .

# Build with specific tag
docker build -t financial-app:latest .
```

### Run Individual Services

```bash
# Run only MySQL
docker-compose up mysql

# Run only the application (requires MySQL to be running)
docker-compose up financial-app
```

### View Logs

```bash
# View all logs
docker-compose logs

# View logs for specific service
docker-compose logs financial-app
docker-compose logs mysql

# Follow logs in real-time
docker-compose logs -f financial-app
```

### Database Management

```bash
# Connect to MySQL container
docker-compose exec mysql mysql -u root -p065516010@Ammar

# Connect to MySQL as financial_user
docker-compose exec mysql mysql -u financial_user -pfinancial_password financial_db
```

### Health Checks

```bash
# Check service health
docker-compose ps

# Check application health
curl http://localhost:8081/api/hello

# Check MySQL health
docker-compose exec mysql mysqladmin ping -h localhost -u root -p065516010@Ammar
```

## Troubleshooting

### Common Issues

1. **Port Already in Use**
   ```bash
   # Check what's using port 8081
   netstat -ano | findstr :8081
   
   # Kill the process
   taskkill /PID <PID> /F
   ```

2. **MySQL Connection Issues**
   ```bash
   # Check MySQL container logs
   docker-compose logs mysql
   
   # Restart MySQL service
   docker-compose restart mysql
   ```

3. **Application Won't Start**
   ```bash
   # Check application logs
   docker-compose logs financial-app
   
   # Rebuild the application
   docker-compose up --build financial-app
   ```

### Clean Up

```bash
# Remove all containers and networks
docker-compose down

# Remove all containers, networks, and volumes
docker-compose down -v

# Remove all images
docker-compose down --rmi all

# Clean up everything (containers, networks, volumes, images)
docker system prune -a
```

## Development

### Hot Reload (Development)

For development with hot reload, you can mount the source code:

```yaml
# Add to docker-compose.yml under financial-app service
volumes:
  - ./src:/app/src
  - ./target:/app/target
```

### Environment Variables

You can override environment variables by creating a `.env` file:

```bash
# .env file
MYSQL_ROOT_PASSWORD=your_password
MYSQL_PASSWORD=your_app_password
```

## Production Considerations

1. **Security**: Change default passwords
2. **Volumes**: Use named volumes for data persistence
3. **Networks**: Use custom networks for service isolation
4. **Resources**: Set memory and CPU limits
5. **Health Checks**: Monitor application health
6. **Logging**: Configure centralized logging




