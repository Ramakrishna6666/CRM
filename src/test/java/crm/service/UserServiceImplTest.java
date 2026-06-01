package crm.service;

import crm.entity.Role;
import crm.entity.User;
import crm.repository.RoleRepository;
import crm.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private BCryptPasswordEncoder passwordEncoder;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private SpringDataUserDetailsService springDataUserDetailsService;

    @InjectMocks
    private UserServiceImpl userService;

    private User user;
    private Role role;

    @BeforeEach
    void setUp() {
        role = new Role();
        role.setId(1);
        role.setName("ROLE_USER");

        user = new User();
        user.setId(1L);
        user.setUsername("testuser");
        user.setEmail("test@test.com");
        user.setFirstName("Test");
        user.setLastName("User");
        user.setPassword("rawpassword");
        user.setEnabled(1);
        user.setRole(role);
    }

    @Test
    void testFindByUsername() {
        when(userRepository.findByUsername("testuser")).thenReturn(user);
        User result = userService.findByUsername("testuser");
        assertNotNull(result);
        assertEquals("testuser", result.getUsername());
        verify(userRepository).findByUsername("testuser");
    }

    @Test
    void testFindByUsername_NotFound() {
        when(userRepository.findByUsername("unknown")).thenReturn(null);
        User result = userService.findByUsername("unknown");
        assertNull(result);
        verify(userRepository).findByUsername("unknown");
    }

    @Test
    void testListAllUsers() {
        when(userRepository.findAllByEnabled(1)).thenReturn(Arrays.asList(user));
        Iterable<User> result = userService.listAllUsers();
        assertNotNull(result);
        verify(userRepository).findAllByEnabled(1);
    }

    @Test
    void testShowUser_Found() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        User result = userService.showUser(1L);
        assertNotNull(result);
        assertEquals(1L, result.getId());
        verify(userRepository).findById(1L);
    }

    @Test
    void testShowUser_NotFound() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());
        User result = userService.showUser(99L);
        assertNull(result);
        verify(userRepository).findById(99L);
    }

    @Test
    void testDeleteUser() {
        userService.deleteUser(user);
        assertEquals(0, user.getEnabled());
        assertNull(user.getPassword());
        verify(userRepository).save(user);
    }

    @Test
    void testDeleteUser_SetsEnabledToZero() {
        user.setEnabled(1);
        userService.deleteUser(user);
        assertEquals(0, user.getEnabled());
    }

    @Test
    void testDeleteUser_SetsPasswordToNull() {
        user.setPassword("somepassword");
        userService.deleteUser(user);
        assertNull(user.getPassword());
    }

    @Test
    void testEditUser_WithValidRole() {
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(roleRepository.findById(1)).thenReturn(Optional.of(role));
        when(roleRepository.findByName("ROLE_USER")).thenReturn(role);

        userService.editUser(user);

        verify(passwordEncoder).encode(anyString());
        verify(userRepository).save(user);
        assertEquals("encodedPassword", user.getPassword());
        assertEquals(1, user.getEnabled());
    }

    @Test
    void testEditUser_WithNullRole() {
        user.setRole(null);
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(roleRepository.findByName("ROLE_USER")).thenReturn(role);

        userService.editUser(user);

        verify(userRepository).save(user);
        assertEquals(role, user.getRole());
    }

    @Test
    void testSaveUser_NonFirstUser() {
        user.setId(2L);
        when(roleRepository.findByName("ROLE_USER")).thenReturn(role);
        when(passwordEncoder.encode("rawpassword")).thenReturn("encodedPassword");

        UserDetails mockUserDetails = mock(UserDetails.class);
        when(mockUserDetails.getUsername()).thenReturn("testuser");
        when(mockUserDetails.getAuthorities()).thenReturn(Collections.emptySet());
        when(springDataUserDetailsService.loadUserByUsername("testuser")).thenReturn(mockUserDetails);
        when(authenticationManager.authenticate(any())).thenReturn(null);

        userService.saveUser(user);

        verify(roleRepository).findByName("ROLE_USER");
        verify(passwordEncoder).encode("rawpassword");
        verify(userRepository, atLeastOnce()).save(user);
        assertEquals(1, user.getEnabled());
        assertEquals("encodedPassword", user.getPassword());
    }

    @Test
    void testListAllUsers_Empty() {
        when(userRepository.findAllByEnabled(1)).thenReturn(Collections.emptyList());
        Iterable<User> result = userService.listAllUsers();
        assertNotNull(result);
        assertFalse(result.iterator().hasNext());
    }
}
