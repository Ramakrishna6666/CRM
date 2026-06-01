package crm.service;

import crm.entity.CurrentUser;
import crm.entity.Role;
import crm.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SpringDataUserDetailsServiceTest {

    @Mock
    private UserService userService;

    @InjectMocks
    private SpringDataUserDetailsService springDataUserDetailsService;

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
        user.setPassword("encodedPassword");
        user.setFirstName("Test");
        user.setLastName("User");
        user.setEnabled(1);
        user.setRole(role);
    }

    @Test
    void testLoadUserByUsername_Success() {
        when(userService.findByUsername("testuser")).thenReturn(user);
        UserDetails result = springDataUserDetailsService.loadUserByUsername("testuser");
        assertNotNull(result);
        assertEquals("testuser", result.getUsername());
        assertEquals("encodedPassword", result.getPassword());
        verify(userService).findByUsername("testuser");
    }

    @Test
    void testLoadUserByUsername_UserNotFound() {
        when(userService.findByUsername("unknown")).thenReturn(null);
        assertThrows(UsernameNotFoundException.class,
                () -> springDataUserDetailsService.loadUserByUsername("unknown"));
        verify(userService).findByUsername("unknown");
    }

    @Test
    void testLoadUserByUsername_ReturnsCurrentUser() {
        when(userService.findByUsername("testuser")).thenReturn(user);
        UserDetails result = springDataUserDetailsService.loadUserByUsername("testuser");
        assertInstanceOf(CurrentUser.class, result);
    }

    @Test
    void testLoadUserByUsername_HasAuthorities() {
        when(userService.findByUsername("testuser")).thenReturn(user);
        UserDetails result = springDataUserDetailsService.loadUserByUsername("testuser");
        assertNotNull(result.getAuthorities());
        assertFalse(result.getAuthorities().isEmpty());
    }

    @Test
    void testLoadUserByUsername_AuthorityMatchesRole() {
        when(userService.findByUsername("testuser")).thenReturn(user);
        UserDetails result = springDataUserDetailsService.loadUserByUsername("testuser");
        boolean hasRole = result.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_USER"));
        assertTrue(hasRole);
    }

    @Test
    void testLoadUserByUsername_AdminRole() {
        role.setName("ROLE_ADMIN");
        user.setRole(role);
        when(userService.findByUsername("adminuser")).thenReturn(user);
        UserDetails result = springDataUserDetailsService.loadUserByUsername("adminuser");
        boolean hasAdminRole = result.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        assertTrue(hasAdminRole);
    }

    @Test
    void testLoadUserByUsername_CurrentUserHasUser() {
        when(userService.findByUsername("testuser")).thenReturn(user);
        UserDetails result = springDataUserDetailsService.loadUserByUsername("testuser");
        CurrentUser currentUser = (CurrentUser) result;
        assertNotNull(currentUser.getUser());
        assertEquals(user, currentUser.getUser());
    }

    @Test
    void testLoadUserByUsername_ThrowsWithMessage() {
        when(userService.findByUsername("ghost")).thenReturn(null);
        UsernameNotFoundException ex = assertThrows(UsernameNotFoundException.class,
                () -> springDataUserDetailsService.loadUserByUsername("ghost"));
        assertEquals("ghost", ex.getMessage());
    }
}
