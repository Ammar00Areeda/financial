# Loan API DTOs Documentation

This document describes the request and response DTOs for the Loan API endpoints.

## Overview

The Loan API now has separate DTOs for different operations:

1. **LoanCreateRequestDto** - For POST requests (creating loans)
2. **LoanUpdateRequestDto** - For PUT requests (updating loans)
3. **LoanResponseDto** - For GET requests (retrieving loan details)
4. **LoanListDTO** - For paginated list responses

## API Endpoints

### Version 2 Endpoints (New - Recommended)

These endpoints use the new request/response DTOs with better separation of concerns.

#### 1. Create Loan (POST /api/loans/v2)

**Request Body (LoanCreateRequestDto):**
```json
{
  "personName": "John Doe",
  "phoneNumber": "+962791234567",
  "email": "john.doe@example.com",
  "loanType": "LENT",
  "principalAmount": 1000.00,
  "interestRate": 5.0,
  "loanDate": "2025-10-11T10:00:00",
  "dueDate": "2026-10-11T10:00:00",
  "description": "Personal loan",
  "notes": "Monthly payments agreed",
  "accountId": 1,
  "isUrgent": false,
  "reminderEnabled": true,
  "nextReminderDate": "2025-11-11T10:00:00"
}
```

**Response (LoanResponseDto):**
```json
{
  "id": 1,
  "personName": "John Doe",
  "phoneNumber": "+962791234567",
  "email": "john.doe@example.com",
  "loanType": "LENT",
  "principalAmount": 1000.00,
  "interestRate": 5.0,
  "totalAmount": 1050.00,
  "paidAmount": 0.00,
  "remainingAmount": 1050.00,
  "loanDate": "2025-10-11T10:00:00",
  "dueDate": "2026-10-11T10:00:00",
  "lastPaymentDate": null,
  "status": "ACTIVE",
  "description": "Personal loan",
  "notes": "Monthly payments agreed",
  "isUrgent": false,
  "reminderEnabled": true,
  "nextReminderDate": "2025-11-11T10:00:00",
  "createdAt": "2025-10-11T10:00:00",
  "updatedAt": "2025-10-11T10:00:00",
  "accountId": 1,
  "accountName": "Main Checking",
  "accountType": "CHECKING",
  "percentagePaid": 0.00,
  "isOverdue": false,
  "isFullyPaid": false
}
```

#### 2. Update Loan (PUT /api/loans/{id}/v2)

**Request Body (LoanUpdateRequestDto):**
```json
{
  "personName": "John Doe",
  "phoneNumber": "+962791234567",
  "email": "john.doe@example.com",
  "loanType": "LENT",
  "principalAmount": 1000.00,
  "interestRate": 5.0,
  "loanDate": "2025-10-11T10:00:00",
  "dueDate": "2026-10-11T10:00:00",
  "status": "ACTIVE",
  "description": "Personal loan - Updated",
  "notes": "Monthly payments agreed",
  "accountId": 1,
  "isUrgent": true,
  "reminderEnabled": true,
  "nextReminderDate": "2025-11-11T10:00:00"
}
```

**Response (LoanResponseDto):**
Same structure as Create response with updated values.

#### 3. Get Loan Details (GET /api/loans/{id}/details)

**Response (LoanResponseDto):**
Full loan details with all computed fields (percentagePaid, isOverdue, isFullyPaid).

### Version 1 Endpoints (Legacy - Still Available)

These endpoints use the original LoanDto for both request and response.

- **POST /api/loans** - Create loan (using LoanDto)
- **PUT /api/loans/{id}** - Update loan (using LoanDto)
- **GET /api/loans/{id}** - Get loan (returns LoanDto)

### Other Endpoints

- **GET /api/loans** - Get all loans (paginated, returns Page<LoanListDTO>)
- **GET /api/loans/type/{type}** - Get loans by type (returns List<LoanListDTO>)
- **GET /api/loans/status/{status}** - Get loans by status (returns List<LoanListDTO>)
- **DELETE /api/loans/{id}** - Delete loan

## Field Descriptions

### Request DTOs

#### LoanCreateRequestDto
Fields that can be provided when creating a loan:
- `personName` (required) - Name of the person involved in the loan
- `phoneNumber` - Contact phone number
- `email` - Contact email address
- `loanType` (required) - Either "LENT" (you lent money) or "BORROWED" (you borrowed money)
- `principalAmount` (required) - The original loan amount
- `interestRate` - Annual interest rate percentage (optional)
- `loanDate` (required) - Date when the loan was given/taken
- `dueDate` - Expected repayment date
- `description` - Description of the loan
- `notes` - Additional notes
- `accountId` - ID of the account used for the loan
- `isUrgent` - Whether the loan requires urgent attention
- `reminderEnabled` - Enable/disable reminders
- `nextReminderDate` - Date for next reminder

**Note:** Fields like `id`, `totalAmount`, `paidAmount`, `remainingAmount`, `status`, and timestamps are auto-generated.

#### LoanUpdateRequestDto
Same fields as create, plus:
- `status` (required) - Loan status (ACTIVE, PAID_OFF, OVERDUE, CANCELLED, PARTIALLY_PAID)

**Note:** Payment amounts (`paidAmount`, `remainingAmount`) should be updated via the payment endpoint (`POST /api/loans/{id}/payment`), not through the update endpoint.

### Response DTOs

#### LoanResponseDto
Complete loan information including:
- All input fields
- Computed fields:
  - `totalAmount` - Principal + interest
  - `paidAmount` - Total amount paid so far
  - `remainingAmount` - Amount still owed
  - `percentagePaid` - Percentage of loan paid (0-100)
  - `isOverdue` - Whether the loan is past its due date
  - `isFullyPaid` - Whether the loan is fully repaid
- Timestamps:
  - `createdAt` - When the loan was created
  - `updatedAt` - When the loan was last updated
  - `lastPaymentDate` - Date of most recent payment
- Account information:
  - `accountId`, `accountName`, `accountType`

#### LoanListDTO
Lightweight DTO for list views containing only essential fields:
- `id`, `personName`, `loanType`
- `principalAmount`, `totalAmount`, `paidAmount`, `remainingAmount`
- `loanDate`, `dueDate`, `status`, `isUrgent`
- Minimal account info: `accountId`, `accountName`

## Loan Types

- **LENT** - Money you lent to someone else
- **BORROWED** - Money you borrowed from someone else

## Loan Status

- **ACTIVE** - Loan is active and unpaid
- **PARTIALLY_PAID** - Some payments have been made
- **PAID_OFF** - Loan is fully repaid
- **OVERDUE** - Loan is past its due date
- **CANCELLED** - Loan was cancelled

## Examples

### Example: Create a Loan for Money You Lent

```bash
POST /api/loans/v2
Content-Type: application/json
Authorization: Bearer <token>

{
  "personName": "Jane Smith",
  "phoneNumber": "+962791234567",
  "email": "jane@example.com",
  "loanType": "LENT",
  "principalAmount": 5000.00,
  "interestRate": 3.5,
  "loanDate": "2025-10-11T10:00:00",
  "dueDate": "2026-10-11T10:00:00",
  "description": "Loan for business startup",
  "isUrgent": false,
  "reminderEnabled": true
}
```

### Example: Update a Loan

```bash
PUT /api/loans/1/v2
Content-Type: application/json
Authorization: Bearer <token>

{
  "personName": "Jane Smith",
  "phoneNumber": "+962791234567",
  "email": "jane@example.com",
  "loanType": "LENT",
  "principalAmount": 5000.00,
  "interestRate": 3.5,
  "loanDate": "2025-10-11T10:00:00",
  "dueDate": "2026-04-11T10:00:00",
  "status": "ACTIVE",
  "description": "Loan for business startup",
  "notes": "Extended due date by 6 months",
  "isUrgent": true,
  "reminderEnabled": true
}
```

### Example: Get Loan Details

```bash
GET /api/loans/1/details
Authorization: Bearer <token>

Response: Full LoanResponseDto with all computed fields
```

### Example: Record a Payment

```bash
POST /api/loans/1/payment?paymentAmount=500.00
Authorization: Bearer <token>

Response: Updated Loan entity with new payment information
```

## Migration Guide

If you're currently using the v1 endpoints:

1. **For CREATE operations:**
   - Change endpoint from `POST /api/loans` to `POST /api/loans/v2`
   - Remove auto-generated fields from request body: `id`, `totalAmount`, `paidAmount`, `remainingAmount`, `status`, `createdAt`, `updatedAt`
   - Use `LoanCreateRequestDto` structure

2. **For UPDATE operations:**
   - Change endpoint from `PUT /api/loans/{id}` to `PUT /api/loans/{id}/v2`
   - Include `status` field in the request
   - Remove payment-related fields - use payment endpoint instead
   - Use `LoanUpdateRequestDto` structure

3. **For GET operations:**
   - Change endpoint from `GET /api/loans/{id}` to `GET /api/loans/{id}/details`
   - Response includes computed fields (percentagePaid, isOverdue, isFullyPaid)

## Benefits of New DTOs

1. **Clearer API Contract** - Separate request and response structures
2. **Better Validation** - Only allow fields that should be modified
3. **Computed Fields** - Response includes helpful calculated values
4. **Immutable Fields** - Prevent accidental modification of auto-generated fields
5. **Better Documentation** - Clear distinction between input and output



