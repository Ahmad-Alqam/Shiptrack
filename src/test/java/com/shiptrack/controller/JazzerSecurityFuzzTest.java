package com.shiptrack.controller;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import org.mockito.MockedStatic;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.ui.Model;

import com.code_intelligence.jazzer.api.FuzzedDataProvider;
import com.code_intelligence.jazzer.junit.FuzzTest;
import com.shiptrack.model.Role;
import com.shiptrack.model.User;
import com.shiptrack.repository.PasswordPolicyRepository;
import com.shiptrack.repository.ShipmentRepository;
import com.shiptrack.repository.UserRepository;
import com.shiptrack.util.SessionHelper;

import jakarta.servlet.http.HttpSession;

class JazzerSecurityFuzzTest {

    @FuzzTest(maxDuration = "30s")
    void fuzzLoginInputs(FuzzedDataProvider data) {
        UserRepository userRepository = mock(UserRepository.class);
        PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);
        PasswordPolicyRepository passwordPolicyRepository = mock(PasswordPolicyRepository.class);
        Model model = mock(Model.class);
        HttpSession session = mock(HttpSession.class);

        AuthController controller = new AuthController(userRepository, passwordEncoder);
        ReflectionTestUtils.setField(controller, "passwordPolicyRepository", passwordPolicyRepository);

        String username = data.consumeString(100);
        String password = data.consumeString(100);

        when(userRepository.findByUsername(anyString())).thenReturn(Optional.empty());

        String result = controller.login(username, password, model, session);

        assertEquals("login", result);
    }

    @FuzzTest(maxDuration = "30s")
    void fuzzCreateShipmentInputs(FuzzedDataProvider data) {
        ShipmentRepository shipmentRepository = mock(ShipmentRepository.class);
        Model model = mock(Model.class);
        HttpSession session = mock(HttpSession.class);

        User customer = new User();
        customer.setId(1L);
        customer.setUsername("customer1");
        customer.setRole(Role.CUSTOMER);

        CustomerController controller = new CustomerController(shipmentRepository);

        String pickupAddress = data.consumeString(100);
        String deliveryAddress = data.consumeString(100);
        String packageType = data.consumeString(50);
        Double packageWeight = data.consumeDouble();
        String notes = data.consumeString(200);

        when(shipmentRepository.existsByTrackingNumber(anyString())).thenReturn(false);

        try (MockedStatic<SessionHelper> mockedSession = mockStatic(SessionHelper.class)) {
            mockedSession.when(() -> SessionHelper.isLoggedIn(session)).thenReturn(true);
            mockedSession.when(() -> SessionHelper.hasRole(session, Role.CUSTOMER)).thenReturn(true);
            mockedSession.when(() -> SessionHelper.getLoggedInUser(session)).thenReturn(customer);

            String result = controller.createShipment(
                    pickupAddress,
                    deliveryAddress,
                    packageType,
                    packageWeight,
                    notes,
                    session,
                    model
            );

            assertEquals("customer_create_shipment", result);
        }
    }

    @FuzzTest(maxDuration = "30s")
    void fuzzCreateShipmentWithoutLogin(FuzzedDataProvider data) {
        ShipmentRepository shipmentRepository = mock(ShipmentRepository.class);
        Model model = mock(Model.class);
        HttpSession session = mock(HttpSession.class);

        CustomerController controller = new CustomerController(shipmentRepository);

        String pickupAddress = data.consumeString(100);
        String deliveryAddress = data.consumeString(100);
        String packageType = data.consumeString(50);
        Double packageWeight = data.consumeDouble();
        String notes = data.consumeString(200);

        try (MockedStatic<SessionHelper> mockedSession = mockStatic(SessionHelper.class)) {
            mockedSession.when(() -> SessionHelper.isLoggedIn(session)).thenReturn(false);

            String result = controller.createShipment(
                    pickupAddress,
                    deliveryAddress,
                    packageType,
                    packageWeight,
                    notes,
                    session,
                    model
            );

            assertEquals("redirect:/login", result);
            verify(shipmentRepository, never()).save(any());
        }
    }
}