package crm.entity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class CurrentUserTest {

    private CurrentUser currentUser;
    private User user;
    private Set<GrantedAuthority> authorities;

    @BeforeEach
    void setUp() {
        Role role = new Role();
        role.setId(1);
        role.setName("ROLE_USER");

        user = new User();
        user.setId(1L);
        user.setUsername("testuser");
        user.setPassword("encodedPassword");
        user.setEmail("test@test.com");
        user.setFirstName("Test");
        user.setLastName("User");
        user.setEnabled(1);
        user.setRole(role);

        authorities = new HashSet<>();
        authorities.add(new SimpleGrantedAuthority("ROLE_USER"));

        currentUser = new CurrentUser();
        currentUser.setUser(user);
        currentUser.setAuthorities(authorities);
    }

    @Test
    void testDefaultConstructor() {
        CurrentUser cu = new CurrentUser();
        assertNotNull(cu);
    }

    @Test
    void testGetAuthorities() {
        Collection<? extends GrantedAuthority> auths = currentUser.getAuthorities();
        assertNotNull(auths);
        assertEquals(1, auths.size());
    }

    @Test
    void testGetPassword() {
        String password = currentUser.getPassword();
        assertEquals("encodedPassword", password);
    }

    @Test
    void testGetUsername() {
        String username = currentUser.getUsername();
        assertEquals("testuser", username);
    }

    @Test
    void testIsAccountNonExpired() {
        assertTrue(currentUser.isAccountNonExpired());
    }

    @Test
    void testIsAccountNonLocked() {
        assertTrue(currentUser.isAccountNonLocked());
    }

    @Test
    void testIsCredentialsNonExpired() {
        assertTrue(currentUser.isCredentialsNonExpired());
    }

    @Test
    void testIsEnabled() {
        assertTrue(currentUser.isEnabled());
    }

    @Test
    void testSetAndGetUser() {
        User newUser = new User();
        newUser.setId(2L);
        newUser.setUsername("newuser");
        newUser.setPassword("newpass");
        currentUser.setUser(newUser);
        assertEquals(newUser, currentUser.getUser());
    }

    @Test
    void testSetAndGetAuthorities() {
        Set<GrantedAuthority> newAuthorities = new HashSet<>();
        newAuthorities.add(new SimpleGrantedAuthority("ROLE_ADMIN"));
        currentUser.setAuthorities(newAuthorities);
        assertEquals(newAuthorities, currentUser.getAuthorities());
    }

    @Test
    void testGetUserReturnsCorrectUser() {
        assertEquals(user, currentUser.getUser());
    }

    @Test
    void testAuthoritiesContainRole() {
        boolean hasRole = currentUser.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_USER"));
        assertTrue(hasRole);
    }

    @Test
    void testMultipleAuthorities() {
        Set<GrantedAuthority> multiAuth = new HashSet<>();
        multiAuth.add(new SimpleGrantedAuthority("ROLE_USER"));
        multiAuth.add(new SimpleGrantedAuthority("ROLE_MANAGER"));
        currentUser.setAuthorities(multiAuth);
        assertEquals(2, currentUser.getAuthorities().size());
    }

    @Test
    void testToString() {
        String str = currentUser.toString();
        assertNotNull(str);
    }
}
