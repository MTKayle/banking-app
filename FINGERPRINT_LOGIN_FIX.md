# Fingerprint Login Feature Fix

## Problem Description
When users enabled fingerprint login in Settings, the app only saved the state locally (`biometric_enabled = true` in SharedPreferences) but did NOT update the backend database. When users logged out and tried to login with fingerprint, the `LoginActivity` called the backend API `checkFingerprintEnabled(phone)` which returned `false`, blocking fingerprint login.

## Root Cause
The `SettingsActivity.toggleBiometric()` method only called `biometricManager.enableBiometric()` which updated local state, but never called the backend API to update the `fingerprintLoginEnabled` flag in the database.

## Solution

### 1. Created New DTOs
- **SmartFlagsRequest.java**: Request DTO for updating user smart flags
  - `fingerprintLoginEnabled`: Boolean flag to enable/disable fingerprint login
  - `faceRecognitionEnabled`: Boolean flag for face recognition (future use)
  - `smartEkycEnabled`: Boolean flag for smart eKYC (future use)

- **UserResponse.java**: Response DTO containing user information and smart flags

### 2. Created UserApiService
- **UserApiService.java**: Retrofit API service interface
  - `updateSmartFlags(userId, request)`: PATCH endpoint to update user smart flags
  - Endpoint: `PATCH /users/{userId}/settings`

### 3. Updated SettingsActivity
Modified `toggleBiometric()` method to:
- **When enabling fingerprint**:
  1. First authenticate with biometric prompt (local verification)
  2. On success, call `enableFingerprintOnBackend()` to update backend
  3. If backend call fails, rollback local state
  
- **When disabling fingerprint**:
  1. Show confirmation dialog
  2. Call `disableFingerprintOnBackend()` to update backend first
  3. On success, disable local state
  4. If backend call fails, keep local state unchanged

### 4. Added Helper Methods
- `enableFingerprintOnBackend()`: Calls backend API to set `fingerprintLoginEnabled = true`
- `disableFingerprintOnBackend()`: Calls backend API to set `fingerprintLoginEnabled = false`

Both methods include:
- User ID validation
- Error handling with user-friendly messages
- Rollback mechanism if API call fails

## Backend API Used
- **Endpoint**: `PATCH /users/{userId}/settings`
- **Request Body**: `SmartFlagsRequest` with `fingerprintLoginEnabled` field
- **Response**: `UserResponse` with updated user information
- **Authentication**: Requires valid JWT token (user must be logged in)

## Flow After Fix

### Enable Fingerprint Flow:
1. User clicks "Cài đặt vân tay" in Settings
2. App shows biometric prompt for authentication
3. User scans fingerprint successfully
4. App calls backend API to set `fingerprintLoginEnabled = true`
5. Backend updates database
6. App shows success message

### Login with Fingerprint Flow:
1. User opens app after logout
2. User clicks fingerprint icon in LoginActivity
3. App calls `checkFingerprintEnabled(phone)` API
4. Backend returns `true` (because flag was updated in Settings)
5. App shows biometric prompt
6. User scans fingerprint
7. App retrieves refresh token and logs in successfully

## Files Modified
- `FrontEnd/banking-app/app/src/main/java/com/example/mobilebanking/activities/SettingsActivity.java`

## Files Created
- `FrontEnd/banking-app/app/src/main/java/com/example/mobilebanking/api/dto/SmartFlagsRequest.java`
- `FrontEnd/banking-app/app/src/main/java/com/example/mobilebanking/api/dto/UserResponse.java`
- `FrontEnd/banking-app/app/src/main/java/com/example/mobilebanking/api/UserApiService.java`

## Testing Steps
1. Login to app with phone and password
2. Go to Settings (Profile screen)
3. Click "Cài đặt vân tay" or "Cài đặt sinh trắc học"
4. Scan fingerprint when prompted
5. Verify success message appears
6. Logout from app
7. Click fingerprint icon in login screen
8. Verify fingerprint prompt appears (not "chưa bật" error)
9. Scan fingerprint
10. Verify successful login

## Notes
- The fix ensures backend and frontend states are always synchronized
- If backend API fails, local state is rolled back to prevent inconsistency
- User must be logged in to enable/disable fingerprint (requires userId)
- The backend endpoint requires authentication (JWT token)
