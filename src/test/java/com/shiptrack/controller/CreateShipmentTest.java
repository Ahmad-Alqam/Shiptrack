package com.shiptrack.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import org.mockito.MockedStatic;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.springframework.ui.Model;

import com.shiptrack.model.Role;
import com.shiptrack.model.Shipment;
import com.shiptrack.model.ShipmentStatus;
import com.shiptrack.model.User;
import com.shiptrack.repository.ShipmentRepository;
import com.shiptrack.util.SessionHelper;

import jakarta.servlet.http.HttpSession;

class CreateShipmentTest {

    @Test
    void createShipmentWithValidDataSavesShipmentSuccessfully() {
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

            String result = controller.createShipment(
                    "Amman",
                    "Irbid",
                    "Box",
                    2.5,
                    "Handle carefully",
                    session,
                    model
            );

            ArgumentCaptor<Shipment> shipmentCaptor = ArgumentCaptor.forClass(Shipment.class);
            verify(shipmentRepository).save(shipmentCaptor.capture());

            Shipment savedShipment = shipmentCaptor.getValue();

            assertEquals("customer_create_shipment", result);
            assertNotNull(savedShipment);
            assertEquals("Amman", savedShipment.getPickupAddress());
            assertEquals("Irbid", savedShipment.getDeliveryAddress());
            assertEquals("Box", savedShipment.getPackageType());
            assertEquals(2.5, savedShipment.getPackageWeight());
            assertEquals("Handle carefully", savedShipment.getNotes());
            assertEquals(ShipmentStatus.PENDING, savedShipment.getStatus());
            assertEquals(customer, savedShipment.getCustomer());
            assertNotNull(savedShipment.getTrackingNumber());
            assertTrue(savedShipment.getTrackingNumber().startsWith("ST-"));
            assertNotNull(savedShipment.getCreatedAt());

            verify(model).addAttribute(eq("success"), contains("Shipment created successfully"));
        }
    }

    @Test
    void createShipmentWithMissingRequiredFieldReturnsError() {
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

            String result = controller.createShipment(
                    "",
                    "Irbid",
                    "Box",
                    2.5,
                    "Handle carefully",
                    session,
                    model
            );

            assertEquals("customer_create_shipment", result);
            verify(model).addAttribute("error", "All required fields must be filled.");
            verify(shipmentRepository, never()).save(any(Shipment.class));
        }
    }

    @Test
    void createShipmentWithoutCustomerSessionRedirectsToLogin() {
        ShipmentRepository shipmentRepository = mock(ShipmentRepository.class);
        Model model = mock(Model.class);
        HttpSession session = mock(HttpSession.class);

        CustomerController controller = new CustomerController(shipmentRepository);

        try (MockedStatic<SessionHelper> mockedSession = mockStatic(SessionHelper.class)) {
            mockedSession.when(() -> SessionHelper.isLoggedIn(session)).thenReturn(false);

            String result = controller.createShipment(
                    "Amman",
                    "Irbid",
                    "Box",
                    2.5,
                    "Handle carefully",
                    session,
                    model
            );

            assertEquals("redirect:/login", result);
            verify(shipmentRepository, never()).save(any(Shipment.class));
        }
    }

    @Test
    void createShipmentWithInvalidPackageWeightReturnsError() {
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

            String result = controller.createShipment(
                    "Amman",
                    "Irbid",
                    "Box",
                    -1.0,
                    "Handle carefully",
                    session,
                    model
            );

            assertEquals("customer_create_shipment", result);
            verify(model).addAttribute("error", "Package weight must be greater than 0.");
            verify(shipmentRepository, never()).save(any(Shipment.class));
        }
    }

    @Test
    void createShipmentInitializesStatusAsPending() {
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

            controller.createShipment(
                    "Amman",
                    "Irbid",
                    "Box",
                    2.5,
                    "",
                    session,
                    model
            );

            ArgumentCaptor<Shipment> shipmentCaptor = ArgumentCaptor.forClass(Shipment.class);
            verify(shipmentRepository).save(shipmentCaptor.capture());

            Shipment savedShipment = shipmentCaptor.getValue();

            assertEquals(ShipmentStatus.PENDING, savedShipment.getStatus());
        }
    }

    @Test
    void createShipmentIsLinkedToAuthenticatedCustomer() {
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

            controller.createShipment(
                    "Amman",
                    "Irbid",
                    "Box",
                    2.5,
                    "",
                    session,
                    model
            );

            ArgumentCaptor<Shipment> shipmentCaptor = ArgumentCaptor.forClass(Shipment.class);
            verify(shipmentRepository).save(shipmentCaptor.capture());

            Shipment savedShipment = shipmentCaptor.getValue();

            assertEquals(customer, savedShipment.getCustomer());
        }
    }

    @Test
    void createMultipleValidShipmentsCreatesTwoSeparateRecords() {
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

            controller.createShipment(
                    "Amman",
                    "Irbid",
                    "Box",
                    2.5,
                    "",
                    session,
                    model
            );

            controller.createShipment(
                    "Amman",
                    "Zarqa",
                    "Documents",
                    1.0,
                    "Important",
                    session,
                    model
            );

            verify(shipmentRepository, times(2)).save(any(Shipment.class));
        }
    }
}