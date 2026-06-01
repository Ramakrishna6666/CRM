package crm.service;

import crm.entity.Role;
import crm.repository.RoleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RoleServiceImplTest {

    @Mock
    private RoleRepository roleRepository;

    @InjectMocks
    private RoleServiceImpl roleService;

    private Role roleUser;
    private Role roleAdmin;

    @BeforeEach
    void setUp() {
        roleUser = new Role();
        roleUser.setId(1);
        roleUser.setName("ROLE_USER");

        roleAdmin = new Role();
        roleAdmin.setId(2);
        roleAdmin.setName("ROLE_ADMIN");
    }

    @Test
    void testListAllRoles() {
        when(roleRepository.findAll()).thenReturn(Arrays.asList(roleUser, roleAdmin));
        Iterable<Role> result = roleService.listAllRoles();
        assertNotNull(result);
        verify(roleRepository).findAll();
    }

    @Test
    void testListAllRoles_Empty() {
        when(roleRepository.findAll()).thenReturn(Collections.emptyList());
        Iterable<Role> result = roleService.listAllRoles();
        assertNotNull(result);
        assertFalse(result.iterator().hasNext());
        verify(roleRepository).findAll();
    }

    @Test
    void testListAllRoles_SingleRole() {
        when(roleRepository.findAll()).thenReturn(Arrays.asList(roleUser));
        Iterable<Role> result = roleService.listAllRoles();
        assertNotNull(result);
        assertTrue(result.iterator().hasNext());
        verify(roleRepository).findAll();
    }

    @Test
    void testListAllRoles_MultipleRoles() {
        when(roleRepository.findAll()).thenReturn(Arrays.asList(roleUser, roleAdmin));
        Iterable<Role> result = roleService.listAllRoles();
        int count = 0;
        for (Role r : result) {
            count++;
        }
        assertEquals(2, count);
        verify(roleRepository).findAll();
    }

    @Test
    void testListAllRoles_VerifyRoleNames() {
        when(roleRepository.findAll()).thenReturn(Arrays.asList(roleUser, roleAdmin));
        Iterable<Role> result = roleService.listAllRoles();
        boolean foundUser = false;
        boolean foundAdmin = false;
        for (Role r : result) {
            if ("ROLE_USER".equals(r.getName())) foundUser = true;
            if ("ROLE_ADMIN".equals(r.getName())) foundAdmin = true;
        }
        assertTrue(foundUser);
        assertTrue(foundAdmin);
    }
}
