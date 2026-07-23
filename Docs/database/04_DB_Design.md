

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