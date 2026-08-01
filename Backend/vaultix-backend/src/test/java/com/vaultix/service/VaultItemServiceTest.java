package com.vaultix.service;

import com.vaultix.dto.VaultItemRequest;
import com.vaultix.dto.VaultItemResponse;
import com.vaultix.entity.User;
import com.vaultix.entity.VaultCategory;
import com.vaultix.entity.VaultItem;
import com.vaultix.repository.UserRepository;
import com.vaultix.repository.VaultItemRepository;
import com.vaultix.service.impl.VaultItemServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VaultItemServiceTest {

    @Mock
    private VaultItemRepository vaultItemRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private AuditLogService auditLogService;

    @InjectMocks
    private VaultItemServiceImpl vaultItemService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setUserId(1L);
        testUser.setUsername("testuser");
        testUser.setEmail("test@vaultix.io");
    }

    @Test
    void testCreateVaultItem_Success() {
        VaultItemRequest req = new VaultItemRequest();
        req.setCategory(VaultCategory.CREDENTIAL);
        req.setTitle("GitHub Password");
        req.setUsernameOrIdentifier("alex_dev");
        req.setEncryptedPayload("EncryptedDataBase64String");
        req.setIv("RandomIvBase64");

        when(userRepository.findByEmail("test@vaultix.io")).thenReturn(Optional.of(testUser));
        when(vaultItemRepository.save(any(VaultItem.class))).thenAnswer(invocation -> {
            VaultItem item = invocation.getArgument(0);
            item.setVaultItemId(100L);
            return item;
        });

        VaultItemResponse resp = vaultItemService.createVaultItem("test@vaultix.io", req);

        assertNotNull(resp);
        assertEquals(100L, resp.getVaultItemId());
        assertEquals("GitHub Password", resp.getTitle());
        assertEquals(VaultCategory.CREDENTIAL, resp.getCategory());
        verify(auditLogService, times(1)).logEvent(eq(testUser), eq("VAULT_ITEM_CREATE"), anyString(), any(), any());
    }

    @Test
    void testToggleFavorite_Success() {
        VaultItem item = new VaultItem();
        item.setVaultItemId(50L);
        item.setUser(testUser);
        item.setIsFavorite(false);

        when(userRepository.findByEmail("test@vaultix.io")).thenReturn(Optional.of(testUser));
        when(vaultItemRepository.findByVaultItemIdAndUser(50L, testUser)).thenReturn(Optional.of(item));
        when(vaultItemRepository.save(any(VaultItem.class))).thenAnswer(i -> i.getArgument(0));

        VaultItemResponse resp = vaultItemService.toggleFavorite("test@vaultix.io", 50L);

        assertTrue(resp.getIsFavorite());
    }
}
