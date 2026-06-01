package crm.controller;

import crm.service.UserService;
import crm.entity.User;
import crm.entity.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ui.Model;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ExportTest {

    @Mock
    private UserService userService;

    @Mock
    private Model model;

    @InjectMocks
    private Export export;

    private User user;

    @BeforeEach
    void setUp() {
        Role role = new Role();
        role.setId(1);
        role.setName("ROLE_USER");

        user = new User();
        user.setId(1L);
        user.setUsername("testuser");
        user.setRole(role);
    }

    @Test
    void testDownload() {
        when(userService.listAllUsers()).thenReturn(Arrays.asList(user));
        String view = export.download(model);
        assertNotNull(view);
        verify(model).addAttribute(eq("users"), any());
    }

    @Test
    void testDownload_CallsListAllUsers() {
        when(userService.listAllUsers()).thenReturn(Arrays.asList(user));
        export.download(model);
        verify(userService).listAllUsers();
    }

    @Test
    void testDownload_AddsUsersToModel() {
        when(userService.listAllUsers()).thenReturn(Arrays.asList(user));
        export.download(model);
        verify(model).addAttribute("users", Arrays.asList(user));
    }
}
