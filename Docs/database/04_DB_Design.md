

---

# VAULTIX DATABASE DESIGN

### Database Name

`vaultix_db`

---

## Database Modules

The Vaultix database is divided into logical modules to improve maintainability, scalability, and security.

### 1. User Management

* Users
* User Profile
* User Settings
* User Sessions
* User Devices

---

### 2. Authentication & Security

* Login History
* Activity Logs
* Password Reset
* Email Verification
* Security Questions

---

### 3. Credential Management

* Password Categories
* Password Entries
* Password History
* Password Favorites
* Password Tags

---

### 4. Secure Notes

* Note Categories
* Notes
* Note Tags

---

### 5. Document Management

* Document Categories
* Documents

---

### 6. Financial Vault

* Payment Cards
* Bank Accounts

---

### 7. Developer Vault

* API Keys
* SSH Keys
* Database Credentials
* Software Licenses

---

### 8. System

* Notifications
* Audit Logs

---

## Estimated Database Size

* **Total Tables:** Approximately 25–30 normalized tables.
* **Target Normalization:**
* First Normal Form (1NF)
* Second Normal Form (2NF)
* Third Normal Form (3NF)





# Module 1 – User Management

## Purpose

The User Management module is responsible for authentication, user profile management, login sessions, trusted devices, and user preferences.

## Tables

### 1. users
Stores user account information and login credentials.

### 2. user_profiles
Stores personal information about the user.

### 3. user_sessions
Stores login session history and active sessions.

### 4. user_devices
Stores trusted devices used by the user.

### 5. user_preferences
Stores user settings such as theme, language, and security preferences.

## Relationships

- One User has one User Profile.
- One User can have many User Sessions.
- One User can have many User Devices.
- One User has one User Preferences record.