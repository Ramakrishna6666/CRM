package crm.controller;

import crm.entity.Role;
import crm.entity.User;
import crm.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    @Mock
    private UserService userService;

    @Mock
    private Model model;

    @Mock
    private BindingResult bindingResult;

    @Mock
    private UserDetails userDetails;

    @InjectMocks
    private UserController userController;

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
        user.setPassword("password");
        user.setEnabled(1);
        user.setRole(role);
    }

    @Test
    void testShowAllUsers() {
        when(userDetails.getUsername()).thenReturn("testuser");
        when(userService.findByUsername("testuser")).thenReturn(user);
        when(userService.listAllUsers()).thenReturn(Arrays.asList(user));

        String view = userController.showAllUsers(model, userDetails);
        assertEquals("user/list", view);
        verify(model).addAttribute(eq("currentUser"), eq(user));
        verify(model).addAttribute(eq("users"), any());
    }

    @Test
    void testShowFormEditUser() {
        when(userService.showUser(1L)).thenReturn(user);
        String view = userController.showFormEditUser(model, 1L);
        assertEquals("user/edit", view);
        verify(model).addAttribute(eq("user"), eq(user));
    }

    @Test
    void testProcessRequestEditUser_NoErrors() {
        when(bindingResult.hasErrors()).thenReturn(false);
        String view = userController.processRequestEditUser(1L, user, bindingResult);
        assertEquals("redirect:/user/list", view);
        verify(userService).editUser(user);
    }

    @Test
    void testProcessRequestEditUser_WithErrors() {
        when(bindingResult.hasErrors()).thenReturn(true);
        String view = userController.processRequestEditUser(1L, user, bindingResult);
        assertEquals("redirect:/user/edit/1", view);
        verify(userService, never()).editUser(any());
    }

    @Test
    void testDeleteUser() {
        when(userService.showUser(1L)).thenReturn(user);
        String view = userController.deleteUser(1L);
        assertEquals("redirect:/user/list", view);
        verify(userService).deleteUser(user);
    }

    @Test
    void testDeleteUser_CallsShowUserFirst() {
        when(userService.showUser(2L)).thenReturn(user);
        userController.deleteUser(2L);
        verify(userService).showUser(2L);
        verify(userService).deleteUser(user);
    }

    @Test
    void testShowAllUsers_AddsCurrentUserToModel() {
        when(userDetails.getUsername()).thenReturn("testuser");
        when(userService.findByUsername("testuser")).thenReturn(user);
        when(userService.listAllUsers()).thenReturn(Arrays.asList(user));

        userController.showAllUsers(model, userDetails);
        verify(model).addAttribute("currentUser", user);
    }

    @Test
    void testShowFormEditUser_WithDifferentId() {
        User anotherUser = new User();
        anotherUser.setId(5L);
        anotherUser.setUsername("anotheruser");
        when(userService.showUser(5L)).thenReturn(anotherUser);
        String view = userController.showFormEditUser(model, 5L);
        assertEquals("user/edit", view);
        verify(model).addAttribute("user", anotherUser);
    }
}
