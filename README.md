# Financial Application - Quick Start

## Step 1: Install Docker Desktop
Download and install from: https://www.docker.com/products/docker-desktop

## Step 2: Run the Application
Open terminal/command prompt in the project folder and run:

```bash
docker-compose up -d --build
```

Wait 1-2 minutes for everything to start.

## Step 3: Access the Application
- Open browser: http://localhost:8081/swagger-ui.html
- API Documentation: http://localhost:8081/v3/api-docs

## Step 4: Authentication

### Default Users
The application includes two default users:

**Admin User:**
- Username: `admin`
- Password: `admin123`
- Role: ADMIN (Full access)

**Regular User:**
- Username: `user`
- Password: `user123`
- Role: USER (Basic access)

### How to Authenticate

1. **Get JWT Token:**
   - In Swagger UI, go to `Authentication` section
   - Use `POST /api/auth/login` endpoint
   - Login with username and password
   - Copy the JWT token from the response

2. **Use Token in Swagger:**
   - Click the "Authorize" button (lock icon at top)
   - Enter: `Bearer <your-jwt-token>`
   - Click "Authorize"
   - Now all protected endpoints will work

3. **Register New User (Optional):**
   - Use `POST /api/auth/register` to create new accounts
   - New users get USER role by default

### Security Features
- ✅ JWT-based authentication
- ✅ Role-based authorization (USER, ADMIN, MODERATOR)
- ✅ BCrypt password encryption
- ✅ Secure endpoints with Spring Security
- ✅ Token expiration (24 hours)
- ✅ User management APIs (Admin only)

For detailed security documentation, see [SECURITY.md](SECURITY.md)

## Step 5: Available Features

### Public Endpoints (No Auth)
- User Registration
- User Login
- API Documentation

### Protected Endpoints (Auth Required)
- Account Management
- Transaction Management
- Category Management
- Loan Management
- Recurring Expenses
- Dashboard & Reports

### Admin-Only Endpoints
- User Management (CRUD)
- View All Users
- Update/Delete Users

## Step 6: Stop the Application
```bash
docker-compose down
```

## Additional Resources
- [Security Guide](SECURITY.md) - Complete security documentation
- [Docker Guide](README-Docker.md) - Docker-specific instructions
- [Postman Collection](Financial-API.postman_collection.json) - API testing collection

## Troubleshooting

### Can't Access Application?
- Ensure Docker Desktop is running
- Check ports 8081 and 3306 are not in use
- Wait 1-2 minutes after `docker-compose up`

### Authentication Not Working?
- Verify you're using the correct default credentials
- Check token format: `Bearer <token>`
- Token expires after 24 hours - login again
- See [SECURITY.md](SECURITY.md) for more help

### Database Issues?
- Run `docker-compose down -v` to reset database
- Then `docker-compose up -d --build` to restart fresh

## Tech Stack
- Java 17
- Spring Boot 3.5.6
- Spring Security with JWT
- Spring Data JPA
- MySQL 8
- Flyway Migration
- Lombok
- Swagger/OpenAPI 3.0
