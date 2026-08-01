/**
 * Vaultix Client-Side Zero-Knowledge Encryption Library
 * Web Crypto API (PBKDF2 + AES-GCM-256)
 */

class VaultixCrypto {

    // Backwards-compatible deriveKey (keeps existing behavior)
    static async deriveKey(masterPassword, saltString) {
        const encoder = new TextEncoder();
        const passwordBytes = encoder.encode(masterPassword);
        const saltBytes = encoder.encode(saltString);

        // Import raw master password as secret key material
        const baseKey = await window.crypto.subtle.importKey(
            'raw',
            passwordBytes,
            { name: 'PBKDF2' },
            false,
            ['deriveKey']
        );

        // Derive AES-GCM 256-bit key using PBKDF2 with 100,000 iterations
        const key = await window.crypto.subtle.deriveKey(
            {
                name: 'PBKDF2',
                salt: saltBytes,
                iterations: 100000,
                hash: 'SHA-256'
            },
            baseKey,
            { name: 'AES-GCM', length: 256 },
            false,
            ['encrypt', 'decrypt']
        );

        // Try to clear sensitive buffers (best-effort; JS GC still applies)
        if (passwordBytes && typeof passwordBytes.fill === 'function') passwordBytes.fill(0);
        if (saltBytes && typeof saltBytes.fill === 'function') saltBytes.fill(0);

        return key;
    }

    // Generate a cryptographically-random salt and return as base64
    static generateSaltBase64() {
        const salt = window.crypto.getRandomValues(new Uint8Array(16));
        return VaultixCrypto.arrayBufferToBase64(salt.buffer);
    }

    // New recommended deriveKey that accepts a base64 salt and configurable iterations
    static async deriveKeyFromPassword(masterPassword, saltBase64, iterations = 300000) {
        const encoder = new TextEncoder();
        const passwordBytes = encoder.encode(masterPassword);
        const saltBytes = new Uint8Array(VaultixCrypto.base64ToArrayBuffer(saltBase64));

        // Import raw master password as secret key material
        const baseKey = await window.crypto.subtle.importKey(
            'raw',
            passwordBytes,
            { name: 'PBKDF2' },
            false,
            ['deriveKey']
        );

        // Derive AES-GCM 256-bit key using PBKDF2
        const key = await window.crypto.subtle.deriveKey(
            {
                name: 'PBKDF2',
                salt: saltBytes,
                iterations: iterations,
                hash: 'SHA-256'
            },
            baseKey,
            { name: 'AES-GCM', length: 256 },
            false,
            ['encrypt', 'decrypt']
        );

        // Best-effort zeroing
        if (passwordBytes && typeof passwordBytes.fill === 'function') passwordBytes.fill(0);
        if (saltBytes && typeof saltBytes.fill === 'function') saltBytes.fill(0);

        return key;
    }

    // Optional aad (string) binds additional authenticated data to the ciphertext
    static async encrypt(plaintext, key, aad = null) {
        const encoder = new TextEncoder();
        const dataBytes = encoder.encode(plaintext);

        // Generate 12-byte random Initialization Vector (IV)
        const iv = window.crypto.getRandomValues(new Uint8Array(12));

        const algo = { name: 'AES-GCM', iv: iv };
        if (aad) algo.additionalData = encoder.encode(aad);

        const ciphertextBuffer = await window.crypto.subtle.encrypt(
            algo,
            key,
            dataBytes
        );

        return {
            encryptedPayload: VaultixCrypto.arrayBufferToBase64(ciphertextBuffer),
            iv: VaultixCrypto.arrayBufferToBase64(iv.buffer)
        };
    }

    static async decrypt(encryptedPayloadBase64, ivBase64, key, aad = null) {
        try {
            const ciphertextBuffer = VaultixCrypto.base64ToArrayBuffer(encryptedPayloadBase64);
            const ivBuffer = VaultixCrypto.base64ToArrayBuffer(ivBase64);

            const algo = { name: 'AES-GCM', iv: new Uint8Array(ivBuffer) };
            if (aad) algo.additionalData = new TextEncoder().encode(aad);

            const decryptedBuffer = await window.crypto.subtle.decrypt(
                algo,
                key,
                ciphertextBuffer
            );

            const decoder = new TextDecoder();
            return decoder.decode(decryptedBuffer);
        } catch (err) {
            console.error("Decryption failed:", err);
            return "[Decryption Error: Invalid Master Password]";
        }
    }

    // Helper functions for Base64 conversion
    static arrayBufferToBase64(buffer) {
        let binary = '';
        const bytes = new Uint8Array(buffer);
        for (let i = 0; i < bytes.byteLength; i++) {
            binary += String.fromCharCode(bytes[i]);
        }
        return window.btoa(binary);
    }

    static base64ToArrayBuffer(base64) {
        const binaryString = window.atob(base64);
        const len = binaryString.length;
        const bytes = new Uint8Array(len);
        for (let i = 0; i < len; i++) {
            bytes[i] = binaryString.charCodeAt(i);
        }
        return bytes.buffer;
    }
}
