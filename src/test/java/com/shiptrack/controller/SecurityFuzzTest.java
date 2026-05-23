package com.shiptrack.controller;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import org.mockito.MockedStatic;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.ui.Model;

import com.shiptrack.model.Role;
import com.shiptrack.model.Shipment;
import com.shiptrack.model.User;
import com.shiptrack.repository.PasswordPolicyRepository;
import com.shiptrack.repository.ShipmentRepository;
import com.shiptrack.repository.UserRepository;
import com.shiptrack.util.SessionHelper;

import jakarta.servlet.http.HttpSession;

class SecurityFuzzTest {

    @Test
    void fuzzLoginWithInvalidUsernameInputs() {
        UserRepository userRepository = mock(UserRepository.class);
        PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);
        PasswordPolicyRepository passwordPolicyRepository = mock(PasswordPolicyRepository.class);
        Model model = mock(Model.class);

        AuthController controller = new AuthController(userRepository, passwordEncoder);
        ReflectionTestUtils.setField(controller, "passwordPolicyRepository", passwordPolicyRepository);

        String[] fuzzUsernames = {
                "",
                " ",
                "admin user",
                "admin<script>",
                "' OR '1'='1",
                "admin; DROP TABLE users;",
                "../../../etc/passwd",
                "user@name!",
                "محمد",
                "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa!"
        };

        for (String username : fuzzUsernames) {
            String result = controller.login(username, "Password123!", model, mock(HttpSession.class));

            assertEquals("login", result);
            verify(model, atLeastOnce()).addAttribute("error", "Invalid username format.");
        }

        verify(userRepository, never()).findByUsername(anyString());
    }

    @Test
    void fuzzLoginWithUnknownButValidUsernames() {
        UserRepository userRepository = mock(UserRepository.class);
        PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);
        PasswordPolicyRepository passwordPolicyRepository = mock(PasswordPolicyRepository.class);
        Model model = mock(Model.class);

        AuthController controller = new AuthController(userRepository, passwordEncoder);
        ReflectionTestUtils.setField(controller, "passwordPolicyRepository", passwordPolicyRepository);

        when(userRepository.findByUsername(anyString())).thenReturn(Optional.empty());

        String[] fuzzUsernames = {
                "unknownUser",
                "test_user",
                "ADMIN123",
                "normalusername",
                "user_999"
        };

        for (String username : fuzzUsernames) {
            String result = controller.login(username, "Password123!", model, mock(HttpSession.class));

            assertEquals("login", result);
            verify(model, atLeastOnce()).addAttribute("error", "Invalid username or password.");
        }
    }

    @Test
    void fuzzCreateShipmentWithMissingOrMalformedRequiredFields() {
        ShipmentRepository shipmentRepository = mock(ShipmentRepository.class);
        Model model = mock(Model.class);
        HttpSession session = mock(HttpSession.class);

        User customer = new User();
        customer.setId(1L);
        customer.setUsername("customer1");
        customer.setRole(Role.CUSTOMER);

        CustomerController controller = new CustomerController(shipmentRepository);

        try (MockedStatic<SessionHelper> mockedSession = mockStatic(SessionHelper.class)) {
            mockedSession.when(() -> SessionHelper.isLoggedIn(session)).thenReturn(true);
            mockedSession.when(() -> SessionHelper.hasRole(session, Role.CUSTOMER)).thenReturn(true);
            mockedSession.when(() -> SessionHelper.getLoggedInUser(session)).thenReturn(customer);

            String[][] fuzzInputs = {
                    {"", "Irbid", "Box"},
                    {"Amman", "", "Box"},
                    {"Amman", "Irbid", ""},
                    {"   ", "Irbid", "Box"},
                    {"Amman", "   ", "Box"},
                    {"Amman", "Irbid", "   "}
            };

            for (String[] input : fuzzInputs) {
                String result = controller.createShipment(
                        input[0],
                        input[1],
                        input[2],
                        2.5,
                        "Test notes",
                        session,
                        model
                );

                assertEquals("customer_create_shipment", result);
                verify(model, atLeastOnce()).addAttribute("error", "All required fields must be filled.");
            }

            verify(shipmentRepository, never()).save(any(Shipment.class));
        }
    }

    @Test
    void fuzzCreateShipmentWithInvalidPackageWeights() {
        ShipmentRepository shipmentRepository = mock(ShipmentRepository.class);
        Model model = mock(Model.class);
        HttpSession session = mock(HttpSession.class);

        User customer = new User();
        customer.setId(1L);
        customer.setUsername("customer1");
        customer.setRole(Role.CUSTOMER);

        CustomerController controller = new CustomerController(shipmentRepository);

        try (MockedStatic<SessionHelper> mockedSession = mockStatic(SessionHelper.class)) {
            mockedSession.when(() -> SessionHelper.isLoggedIn(session)).thenReturn(true);
            mockedSession.when(() -> SessionHelper.hasRole(session, Role.CUSTOMER)).thenReturn(true);
            mockedSession.when(() -> SessionHelper.getLoggedInUser(session)).thenReturn(customer);

            double[] fuzzWeights = {
                    0,
                    -1,
                    -9999,
                    Double.NaN,
                    Double.NEGATIVE_INFINITY
            };

            for (double weight : fuzzWeights) {
                String result = controller.createShipment(
                        "Amman",
                        "Irbid",
                        "Box",
                        weight,
                        "Test notes",
                        session,
                        model
                );

                assertEquals("customer_create_shipment", result);
                verify(model, atLeastOnce()).addAttribute("error", "Package weight must be greater than 0.");
            }

            verify(shipmentRepository, never()).save(any(Shipment.class));
        }
    }

    @Test
    void fuzzCreateShipmentWithExtremeButValidTextDoesNotCrash() {
        ShipmentRepository shipmentRepository = mock(ShipmentRepository.class);
        Model model = mock(Model.class);
        HttpSession session = mock(HttpSession.class);

        User customer = new User();
        customer.setId(1L);
        customer.setUsername("customer1");
        customer.setRole(Role.CUSTOMER);

        when(shipmentRepository.existsByTrackingNumber(anyString())).thenReturn(false);

        CustomerController controller = new CustomerController(shipmentRepository);

        try (MockedStatic<SessionHelper> mockedSession = mockStatic(SessionHelper.class)) {
            mockedSession.when(() -> SessionHelper.isLoggedIn(session)).thenReturn(true);
            mockedSession.when(() -> SessionHelper.hasRole(session, Role.CUSTOMER)).thenReturn(true);
            mockedSession.when(() -> SessionHelper.getLoggedInUser(session)).thenReturn(customer);

            String longInput = "A".repeat(500);

            String result = controller.createShipment(
                    longInput,
                    longInput,
                    "Box",
                    2.5,
                    "<script>alert('xss')</script>",
                    session,
                    model
            );

            assertEquals("customer_create_shipment", result);
            verify(shipmentRepository).save(any(Shipment.class));
        }
    }
}