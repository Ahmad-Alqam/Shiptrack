package com.shiptrack.controller;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.ui.Model;

import com.shiptrack.model.PasswordPolicy;
import com.shiptrack.model.Role;
import com.shiptrack.model.User;
import com.shiptrack.repository.PasswordPolicyRepository;
import com.shiptrack.repository.UserRepository;

class LoginTest {

    @Test
    void loginInvalidUsernameFormatReturnsLoginPage() {
        UserRepository userRepository = mock(UserRepository.class);
        PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);
        PasswordPolicyRepository passwordPolicyRepository = mock(PasswordPolicyRepository.class);
        Model model = mock(Model.class);

        AuthController controller = new AuthController(userRepository, passwordEncoder);
        ReflectionTestUtils.setField(controller, "passwordPolicyRepository", passwordPolicyRepository);

        String result = controller.login("bad username!", "Password123!", model, new MockHttpSession());

        assertEquals("login", result);
        verify(model).addAttribute("error", "Invalid username format.");
    }

    @Test
    void loginUsernameNotFoundReturnsLoginPage() {
        UserRepository userRepository = mock(UserRepository.class);
        PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);
        PasswordPolicyRepository passwordPolicyRepository = mock(PasswordPolicyRepository.class);
        Model model = mock(Model.class);

        when(userRepository.findByUsername("unknownUser")).thenReturn(Optional.empty());

        AuthController controller = new AuthController(userRepository, passwordEncoder);
        ReflectionTestUtils.setField(controller, "passwordPolicyRepository", passwordPolicyRepository);

        String result = controller.login("unknownUser", "Password123!", model, new MockHttpSession());

        assertEquals("login", result);
        verify(model).addAttribute("error", "Invalid username or password.");
    }

    @Test
    void loginLockedAccountReturnsLoginPage() {
        UserRepository userRepository = mock(UserRepository.class);
        PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);
        PasswordPolicyRepository passwordPolicyRepository = mock(PasswordPolicyRepository.class);
        Model model = mock(Model.class);

        User user = new User();
        user.setUsername("customer1");
        user.setLocked(true);

        when(userRepository.findByUsername("customer1")).thenReturn(Optional.of(user));

        AuthController controller = new AuthController(userRepository, passwordEncoder);
        ReflectionTestUtils.setField(controller, "passwordPolicyRepository", passwordPolicyRepository);

        String result = controller.login("customer1", "Password123!", model, new MockHttpSession());

        assertEquals("login", result);
        verify(model).addAttribute("error", "Account is locked.");
    }

    @Test
    void loginWrongPasswordIncrementsFailedAttempts() {
        UserRepository userRepository = mock(UserRepository.class);
        PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);
        PasswordPolicyRepository passwordPolicyRepository = mock(PasswordPolicyRepository.class);
        Model model = mock(Model.class);

        User user = new User();
        user.setUsername("driver1");
        user.setPassword("hashedPassword");
        user.setRole(Role.DRIVER);
        user.setLocked(false);
        user.setFailedAttempts(1);

        PasswordPolicy policy = new PasswordPolicy();
        policy.setMaxLoginAttempts(3);

        when(userRepository.findByUsername("driver1")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("WrongPass", "hashedPassword")).thenReturn(false);
        when(passwordPolicyRepository.findAll()).thenReturn(List.of(policy));

        AuthController controller = new AuthController(userRepository, passwordEncoder);
        ReflectionTestUtils.setField(controller, "passwordPolicyRepository", passwordPolicyRepository);

        String result = controller.login("driver1", "WrongPass", model, new MockHttpSession());

        assertEquals("login", result);
        assertEquals(2, user.getFailedAttempts());
        assertFalse(user.isLocked());
        verify(userRepository).save(user);
        verify(model).addAttribute("error", "Invalid username or password.");
    }

    @Test
    void loginWrongPasswordLocksAccountWhenMaxAttemptsReached() {
        UserRepository userRepository = mock(UserRepository.class);
        PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);
        PasswordPolicyRepository passwordPolicyRepository = mock(PasswordPolicyRepository.class);
        Model model = mock(Model.class);

        User user = new User();
        user.setUsername("driver1");
        user.setPassword("hashedPassword");
        user.setRole(Role.DRIVER);
        user.setLocked(false);
        user.setFailedAttempts(2);

        PasswordPolicy policy = new PasswordPolicy();
        policy.setMaxLoginAttempts(3);

        when(userRepository.findByUsername("driver1")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("WrongPass", "hashedPassword")).thenReturn(false);
        when(passwordPolicyRepository.findAll()).thenReturn(List.of(policy));

        AuthController controller = new AuthController(userRepository, passwordEncoder);
        ReflectionTestUtils.setField(controller, "passwordPolicyRepository", passwordPolicyRepository);

        String result = controller.login("driver1", "WrongPass", model, new MockHttpSession());

        assertEquals("login", result);
        assertEquals(3, user.getFailedAttempts());
        assertTrue(user.isLocked());
        verify(userRepository).save(user);
    }

    @Test
    void loginSuccessfulCustomerRedirectsToCustomerDashboard() {
        UserRepository userRepository = mock(UserRepository.class);
        PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);
        PasswordPolicyRepository passwordPolicyRepository = mock(PasswordPolicyRepository.class);
        Model model = mock(Model.class);

        User user = new User();
        user.setUsername("customer1");
        user.setPassword("hashedPassword");
        user.setRole(Role.CUSTOMER);
        user.setLocked(false);
        user.setFailedAttempts(2);

        when(userRepository.findByUsername("customer1")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("Password123!", "hashedPassword")).thenReturn(true);

        AuthController controller = new AuthController(userRepository, passwordEncoder);
        ReflectionTestUtils.setField(controller, "passwordPolicyRepository", passwordPolicyRepository);

        String result = controller.login("customer1", "Password123!", model, new MockHttpSession());

        assertEquals("redirect:/customer/dashboard", result);
        assertEquals(0, user.getFailedAttempts());
        verify(userRepository).save(user);
    }

    @Test
    void loginSuccessfulAdminRedirectsToAdminDashboard() {
        UserRepository userRepository = mock(UserRepository.class);
        PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);
        PasswordPolicyRepository passwordPolicyRepository = mock(PasswordPolicyRepository.class);
        Model model = mock(Model.class);

        User user = new User();
        user.setUsername("admin1");
        user.setPassword("hashedPassword");
        user.setRole(Role.ADMIN);
        user.setLocked(false);

        when(userRepository.findByUsername("admin1")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("Admin123!", "hashedPassword")).thenReturn(true);

        AuthController controller = new AuthController(userRepository, passwordEncoder);
        ReflectionTestUtils.setField(controller, "passwordPolicyRepository", passwordPolicyRepository);

        String result = controller.login("admin1", "Admin123!", model, new MockHttpSession());

        assertEquals("redirect:/admin/dashboard", result);
        verify(userRepository).save(user);
    }

    @Test
    void loginSuccessfulDispatcherRedirectsToDispatcherDashboard() {
        UserRepository userRepository = mock(UserRepository.class);
        PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);
        PasswordPolicyRepository passwordPolicyRepository = mock(PasswordPolicyRepository.class);
        Model model = mock(Model.class);

        User user = new User();
        user.setUsername("dispatcher1");
        user.setPassword("hashedPassword");
        user.setRole(Role.DISPATCHER);
        user.setLocked(false);
        user.setFailedAttempts(1);

        when(userRepository.findByUsername("dispatcher1")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("Dispatcher123!", "hashedPassword")).thenReturn(true);

        AuthController controller = new AuthController(userRepository, passwordEncoder);
        ReflectionTestUtils.setField(controller, "passwordPolicyRepository", passwordPolicyRepository);

        String result = controller.login("dispatcher1", "Dispatcher123!", model, new MockHttpSession());

        assertEquals("redirect:/dispatcher/dashboard", result);
        assertEquals(0, user.getFailedAttempts());
        verify(userRepository).save(user);
    }

    @Test
    void loginSuccessfulDriverRedirectsToDriverDashboard() {
        UserRepository userRepository = mock(UserRepository.class);
        PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);
        PasswordPolicyRepository passwordPolicyRepository = mock(PasswordPolicyRepository.class);
        Model model = mock(Model.class);

        User user = new User();
        user.setUsername("driver1");
        user.setPassword("hashedPassword");
        user.setRole(Role.DRIVER);
        user.setLocked(false);
        user.setFailedAttempts(1);

        when(userRepository.findByUsername("driver1")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("Driver123!", "hashedPassword")).thenReturn(true);

        AuthController controller = new AuthController(userRepository, passwordEncoder);
        ReflectionTestUtils.setField(controller, "passwordPolicyRepository", passwordPolicyRepository);

        String result = controller.login("driver1", "Driver123!", model, new MockHttpSession());

        assertEquals("redirect:/driver/dashboard", result);
        assertEquals(0, user.getFailedAttempts());
        verify(userRepository).save(user);
    }
}