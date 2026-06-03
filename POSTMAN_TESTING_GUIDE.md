# RushTix Postman Testing Guide

## Step 1: Signup (Create an ORGANIZER account)

**Endpoint:** `POST http://localhost:8080/api/v1/auth/signup`

**Headers:**
```
Content-Type: application/json
```

**Body (JSON):**
```json
{
  "email": "organizer@example.com",
  "fullName": "John Organizer",
  "password": "Password123@",
  "role": "ORGANIZER"
}
```

**Expected Response (201 Created):**
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiI1NTBlODQwMC1lMjliLTQxZDQtYTcxNi00NDY2NTU0NDAwMDAiLCJlbWFpbCI6Im9yZ2FuaXplckBleGFtcGxlLmNvbSIsInJvbGUiOiJST0xFX09SR0FOSVpFUiIsImlhdCI6MTc0OTEyMzQ1MCwiZXhwIjoxNzQ5MTI3MDUwfQ.xyz...",
  "userId": "550e8400-e29b-41d4-a716-446655440000",
  "email": "organizer@example.com",
  "role": "ORGANIZER"
}
```

**SAVE the token!** You'll need it for all subsequent requests.

---

## Step 2: Get JWT Token (Alternative - Login)

If you already have an account, use login instead:

**Endpoint:** `POST http://localhost:8080/api/v1/auth/login`

**Headers:**
```
Content-Type: application/json
```

**Body (JSON):**
```json
{
  "email": "organizer@example.com",
  "password": "Password123@"
}
```

**Expected Response (200 OK):**
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "userId": "550e8400-e29b-41d4-a716-446655440000",
  "email": "organizer@example.com",
  "role": "ORGANIZER"
}
```

---

## Step 3: Test VENUE Endpoints (Using JWT Token)

### 3.1 CREATE VENUE

**Endpoint:** `POST http://localhost:8080/api/v1/organizer/venues`

**Headers:**
```
Content-Type: application/json
Authorization: Bearer YOUR_JWT_TOKEN_HERE
```

**Body (JSON):**
```json
{
  "name": "Madison Square Garden",
  "city": "New York",
  "state": "NY",
  "addressLine": "33 Penn Plaza, New York, NY 10001",
  "pincode": "10001",
  "latitude": 40.7505,
  "longitude": -73.9934,
  "totalCapacity": 20000,
  "timezone": "America/New_York",
  "seatMapConfig": {
    "rows": [
      {
        "label": "A",
        "capacity": 50,
        "wheelchair_accessible": 3
      },
      {
        "label": "B",
        "capacity": 50,
        "wheelchair_accessible": 3
      },
      {
        "label": "C",
        "capacity": 50,
        "wheelchair_accessible": 3
      }
    ]
  }
}
```

**Expected Response (201 Created):**
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440001",
  "name": "Madison Square Garden",
  "city": "New York",
  "organizerId": "550e8400-e29b-41d4-a716-446655440000",
  ...
}
```

**SAVE the venue ID!** You'll need it for creating events.

---

### 3.2 GET ALL MY VENUES

**Endpoint:** `GET http://localhost:8080/api/v1/organizer/venues`

**Headers:**
```
Authorization: Bearer YOUR_JWT_TOKEN_HERE
```

**Expected Response (200 OK):**
```json
[
  {
    "id": "550e8400-e29b-41d4-a716-446655440001",
    "name": "Madison Square Garden",
    "city": "New York",
    ...
  }
]
```

---

### 3.3 GET SINGLE VENUE

**Endpoint:** `GET http://localhost:8080/api/v1/organizer/venues/550e8400-e29b-41d4-a716-446655440001`

**Headers:**
```
Authorization: Bearer YOUR_JWT_TOKEN_HERE
```

---

### 3.4 UPDATE VENUE

**Endpoint:** `PUT http://localhost:8080/api/v1/organizer/venues/550e8400-e29b-41d4-a716-446655440001`

**Headers:**
```
Content-Type: application/json
Authorization: Bearer YOUR_JWT_TOKEN_HERE
```

**Body (JSON):**
```json
{
  "name": "Madison Square Garden Updated",
  "city": "New York",
  "state": "NY",
  "addressLine": "33 Penn Plaza, New York, NY 10001",
  "pincode": "10001",
  "latitude": 40.7505,
  "longitude": -73.9934,
  "totalCapacity": 25000,
  "timezone": "America/New_York",
  "seatMapConfig": {
    "rows": [
      {
        "label": "A",
        "capacity": 50,
        "wheelchair_accessible": 3
      }
    ]
  }
}
```

---

### 3.5 UPDATE SEAT MAP

**Endpoint:** `PATCH http://localhost:8080/api/v1/organizer/venues/550e8400-e29b-41d4-a716-446655440001/seatmap`

**Headers:**
```
Content-Type: application/json
Authorization: Bearer YOUR_JWT_TOKEN_HERE
```

**Body (JSON):**
```json
{
  "seatMapConfig": {
    "rows": [
      {
        "label": "A",
        "capacity": 100,
        "wheelchair_accessible": 5
      },
      {
        "label": "B",
        "capacity": 100,
        "wheelchair_accessible": 5
      }
    ]
  }
}
```

---

### 3.6 DELETE VENUE

**Endpoint:** `DELETE http://localhost:8080/api/v1/organizer/venues/550e8400-e29b-41d4-a716-446655440001`

**Headers:**
```
Authorization: Bearer YOUR_JWT_TOKEN_HERE
```

**Expected Response (204 No Content)**

---

## Key Points for Testing

1. **Always include the Authorization header** with the JWT token for protected endpoints
2. **Format:** `Authorization: Bearer <your_jwt_token>`
3. **Tokens expire in 1 hour** - If you get a 401 error, you need to login again
4. **Save important IDs** like venue ID, event ID, etc., for use in subsequent requests
5. **Check response status codes:**
   - 201 = Created successfully
   - 200 = Success
   - 204 = No Content (successful deletion)
   - 400 = Bad Request
   - 401 = Unauthorized (missing/invalid token)
   - 403 = Forbidden (don't have permission)
   - 404 = Not Found
   - 409 = Conflict (resource already exists)
   - 500 = Server Error

---

## Testing Flow

1. ✅ Signup → Get JWT token
2. ✅ Create Venue → Save venue ID
3. ✅ Get All Venues → Verify venue is listed
4. ✅ Get Single Venue → Verify details
5. ✅ Update Venue → Verify changes
6. ✅ Update Seat Map → Change layout
7. ✅ Delete Venue → Verify it's removed

Once venue testing is complete, you can test event creation using the venue ID!
