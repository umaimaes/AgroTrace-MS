# AgroTrace API – Endpoints, Inputs, and Outputs

## Services

- **users_manage.server**: User registration, login, password reset, token info.
- **managingcaptors.service**: Captor-side test endpoints using token service.

---

## 1. `users_manage.server` – `/user` API

Base path: `/user`

### 1.1 Register User

- **Method**: `POST`
- **Path**: `/user/register`
- **Auth**: Not required
- **Description**: Register a new user.

**Request Body (JSON `RegisterRequest`)**:
- **`firstname`**: `string`
- **`lastname`**: `string`
- **`email`**: `string`
- **`phone`**: `string`
- **`password`**: `string`

**Response**:
- **200 OK** – Body: `true` or `false` (JSON boolean)
  - `true` → user created
  - `false` → email already exists or invalid request

---

### 1.2 Login

- **Method**: `POST`
- **Path**: `/user/login`
- **Auth**: Not required
- **Description**: Authenticate user and return user info with JWT token.

**Request Parameters** (form or query):
- **`email`**: `string` (required)
- **`password`**: `string` (required)

**Response**:
- **200 OK** – Body: `UserInfo` JSON  
  - **`user`**: `Users` object
    - `id`: `number`
    - `firstname`: `string`
    - `lastname`: `string`
    - `email`: `string`
    - `tel`: `string`
    - `localisation_id`: `string | null`
    - `captors_id`: `string | null`
    - `resetToken`: `string | null`
    - `resetTokenExpiry`: `number | null`
  - **`token`**: `string` (JWT)
- **401 Unauthorized** – Body: `null` (login failed)

---

### 1.3 Logout

- **Method**: `POST`
- **Path**: `/user/logout`
- **Auth**: Required (JWT)
- **Description**: Blacklist the current JWT (log out user).

**Request Headers**:
- **`Authorization`**: `Bearer <jwt-token>`

**Response**:
- **200 OK** – Body: `true`
- **400 Bad Request** – Body: `false` if header missing/invalid

---

### 1.4 Send Password Reset Code

- **Method**: `POST`
- **Path**: `/user/send-code`
- **Auth**: Not required
- **Description**: Send a 6-digit reset code to user’s email.

**Request Parameters**:
- **`email`**: `string` (required)

**Response**:
- **200 OK** – Body: `string` (the verification code)
- **400 Bad Request** – Body: `"User with this email not found."`

---

### 1.5 Verify Reset Code

- **Method**: `GET`
- **Path**: `/user/verification-code/{email}`
- **Auth**: Not required
- **Description**: Check if a reset code is valid for a given email.

**Path Variables**:
- **`email`**: `string`

**Query Parameters**:
- **`code`**: `string` (required)

**Response**:
- **200 OK** – Body: `true` or `false` (JSON boolean)

---

### 1.6 Reset Password

- **Method**: `POST`
- **Path**: `/user/reset-password`
- **Auth**: Not required (secured via code)
- **Description**: Set a new password using a valid reset code.

**Request Parameters**:
- **`code`**: `string` (required) – verification code
- **`password`**: `string` (required) – new password

**Response**:
- **200 OK** – Body: `true` or `false` (JSON boolean)

---

### 1.7 Get Token Info

- **Method**: `GET`
- **Path**: `/user/get-token-info`
- **Auth**: Currently **no auth required** (`permitAll` in `SecurityConfig`)
- **Description**: Return the last authenticated `UserInfo` (from login).

**Request**:
- No body
- No params

**Response**:
- **200 OK** – Body: `UserInfo` JSON:
  - `user`: `Users` object (same fields as login response)
  - `token`: `string` (JWT)

> Note: this returns the `info` stored in `UserService` (last successful login).

---

## 2. `managingcaptors.service` – Token Test API

Base path: `/token-test`

### 2.1 Get Token From `users_manage.server`

- **Method**: `GET`
- **Path**: `/token-test/from-users-manage`
- **Auth**: Depends on managingcaptors security
- **Description**: Calls `http://localhost:8081/user/get-token-info` and returns the token as a simple string.

**Request**:
- No body
- No query params
- No special headers

**Response**:
- **200 OK** – Body: plain text, for example:  
  - `"Token from users_manage.server: <token-or-null>"`

---

### 2.2 Verify Token and Email

- **Method**: `GET`
- **Path**: `/token-test/verify`
- **Auth**: You typically send the JWT in `Authorization` header
- **Description**: Compares client token + email with the `UserInfo` returned by `/user/get-token-info`.

**Request Headers**:
- **`Authorization`**: `Bearer <jwt-token>` (optional but recommended)

**Query Parameters**:
- **`email`**: `string` (required) – client email to compare

**Internal Logic**:
- Calls `/user/get-token-info` on `users_manage.server`.
- Extracts:
  - `serverToken` from response `token`
  - `serverEmail` from response `user.email`
- Returns `true` if:
  - `clientToken == serverToken` **and**
  - `clientEmail == serverEmail`

**Response**:
- **200 OK** – Body: `true` or `false` (JSON boolean)
