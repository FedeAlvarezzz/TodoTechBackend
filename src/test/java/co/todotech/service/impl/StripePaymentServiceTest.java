package co.todotech.service.impl;

import co.todotech.model.dto.pasarela.PaymentIntentRequestDto;
import co.todotech.model.dto.pasarela.PaymentIntentResponseDto;
import co.todotech.model.dto.pasarela.PaymentConfirmationDto;
import co.todotech.model.enums.TipoMetodo;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.param.PaymentIntentCreateParams;
import com.stripe.param.PaymentIntentConfirmParams;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Map;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StripePaymentServiceTest {

    @InjectMocks
    private StripePaymentService stripePaymentService;

    private final String testStripeKey = "sk_test_51SQ9bHRpZZ0VaxsYYaTxLhTgDxywCDKK5zhpxRm5Hbmkbexp7LoM7OYpvlnIqbfKtvUpa6NLwHRdfNGYAnHMuxew00d2CXQCyr";

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(stripePaymentService, "stripeSecretKey", testStripeKey);
        stripePaymentService.init();
    }

    @Test
    void createPaymentIntent_Success() throws StripeException {
        // Given
        PaymentIntentRequestDto request = new PaymentIntentRequestDto(
                100.0, "usd", TipoMetodo.STRIPE, 1L, "test@email.com", Map.of("test", "data")
        );

        PaymentIntent mockPaymentIntent = mock(PaymentIntent.class);
        when(mockPaymentIntent.getClientSecret()).thenReturn("pi_test_secret_123");
        when(mockPaymentIntent.getId()).thenReturn("pi_test_123");
        when(mockPaymentIntent.getStatus()).thenReturn("requires_payment_method");
        when(mockPaymentIntent.getAmountReceived()).thenReturn(0L);
        when(mockPaymentIntent.getPaymentMethodTypes()).thenReturn(List.of("card"));
        when(mockPaymentIntent.getCreated()).thenReturn(123456789L);
        when(mockPaymentIntent.getNextAction()).thenReturn(null);

        try (MockedStatic<PaymentIntent> mockedStatic = mockStatic(PaymentIntent.class)) {
            mockedStatic.when(() -> PaymentIntent.create(any(PaymentIntentCreateParams.class)))
                    .thenReturn(mockPaymentIntent);

            // When
            PaymentIntentResponseDto response = stripePaymentService.createPaymentIntent(request);

            // Then
            assertNotNull(response);
            assertEquals("pi_test_123", response.paymentIntentId());
            assertEquals("pi_test_secret_123", response.clientSecret());
            assertEquals("requires_payment_method", response.status());
            assertFalse(response.requiresAction());
            assertNull(response.errorMessage());
            assertNotNull(response.additionalData());
        }
    }

    @Test
    void createPaymentIntent_StripeException() {
        // Given
        PaymentIntentRequestDto request = new PaymentIntentRequestDto(
                100.0, "usd", TipoMetodo.STRIPE, 1L, "test@email.com", null
        );

        try (MockedStatic<PaymentIntent> mockedStatic = mockStatic(PaymentIntent.class)) {
            StripeException stripeException = mock(StripeException.class);
            when(stripeException.getMessage()).thenReturn("Invalid API Key");

            mockedStatic.when(() -> PaymentIntent.create(any(PaymentIntentCreateParams.class)))
                    .thenThrow(stripeException);

            // When
            PaymentIntentResponseDto response = stripePaymentService.createPaymentIntent(request);

            // Then
            assertNotNull(response);
            assertEquals("failed", response.status());
            assertNotNull(response.errorMessage());
            assertTrue(response.errorMessage().contains("Invalid API Key"));
            assertNull(response.clientSecret());
            assertNull(response.paymentIntentId());
        }
    }

    @Test
    void confirmPayment_Success() throws StripeException {
        // Given
        PaymentConfirmationDto confirmation = new PaymentConfirmationDto(
                "pi_test_123", "pm_test_123", Map.of()
        );

        PaymentIntent mockPaymentIntent = mock(PaymentIntent.class);
        when(mockPaymentIntent.getId()).thenReturn("pi_test_123");
        when(mockPaymentIntent.getClientSecret()).thenReturn("pi_test_secret_123");
        when(mockPaymentIntent.getStatus()).thenReturn("succeeded");
        when(mockPaymentIntent.getAmountReceived()).thenReturn(10000L);

        try (MockedStatic<PaymentIntent> mockedStatic = mockStatic(PaymentIntent.class)) {
            mockedStatic.when(() -> PaymentIntent.retrieve("pi_test_123"))
                    .thenReturn(mockPaymentIntent);

            // El método confirm retorna el mismo objeto, no necesitamos mock adicional
            when(mockPaymentIntent.confirm(any(PaymentIntentConfirmParams.class)))
                    .thenReturn(mockPaymentIntent);

            // When
            PaymentIntentResponseDto response = stripePaymentService.confirmPayment(confirmation);

            // Then
            assertNotNull(response);
            assertEquals("succeeded", response.status());
            assertEquals("pi_test_123", response.paymentIntentId());
            assertNotNull(response.additionalData());

            // Verificar que se llamó a los métodos esperados
            verify(mockPaymentIntent).confirm(any(PaymentIntentConfirmParams.class));
        }
    }

    @Test
    void getPaymentStatus_Success() throws StripeException {
        // Given
        String paymentIntentId = "pi_test_123";

        PaymentIntent mockPaymentIntent = mock(PaymentIntent.class);
        when(mockPaymentIntent.getId()).thenReturn(paymentIntentId);
        when(mockPaymentIntent.getClientSecret()).thenReturn("pi_test_secret_123");
        when(mockPaymentIntent.getStatus()).thenReturn("succeeded");
        when(mockPaymentIntent.getAmountReceived()).thenReturn(10000L);
        when(mockPaymentIntent.getPaymentMethod()).thenReturn("pm_test_123");
        when(mockPaymentIntent.getCreated()).thenReturn(123456789L);
        when(mockPaymentIntent.getCustomer()).thenReturn("cus_test_123");
        when(mockPaymentIntent.getDescription()).thenReturn("Test payment");
        when(mockPaymentIntent.getLastPaymentError()).thenReturn(null);
        when(mockPaymentIntent.getNextAction()).thenReturn(null);

        try (MockedStatic<PaymentIntent> mockedStatic = mockStatic(PaymentIntent.class)) {
            mockedStatic.when(() -> PaymentIntent.retrieve(paymentIntentId))
                    .thenReturn(mockPaymentIntent);

            // When
            PaymentIntentResponseDto response = stripePaymentService.getPaymentStatus(paymentIntentId);

            // Then
            assertNotNull(response);
            assertEquals("succeeded", response.status());
            assertEquals(paymentIntentId, response.paymentIntentId());
            assertNotNull(response.additionalData());
        }
    }

    @Test
    void getPaymentStatus_StripeException() {
        // Given
        String paymentIntentId = "pi_test_invalid";

        try (MockedStatic<PaymentIntent> mockedStatic = mockStatic(PaymentIntent.class)) {
            StripeException stripeException = mock(StripeException.class);
            when(stripeException.getMessage()).thenReturn("Payment intent not found");

            mockedStatic.when(() -> PaymentIntent.retrieve(paymentIntentId))
                    .thenThrow(stripeException);

            // When
            PaymentIntentResponseDto response = stripePaymentService.getPaymentStatus(paymentIntentId);

            // Then
            assertNotNull(response);
            assertEquals("failed", response.status());
            assertNotNull(response.errorMessage());
        }
    }

    @Test
    void supports_ValidMethods() {
        // When & Then
        assertTrue(stripePaymentService.supports(TipoMetodo.STRIPE));
        assertTrue(stripePaymentService.supports(TipoMetodo.TARJETA_CREDITO));
        assertTrue(stripePaymentService.supports(TipoMetodo.TARJETA_DEBITO));
    }

    @Test
    void supports_InvalidMethods() {
        // When & Then
        assertFalse(stripePaymentService.supports(TipoMetodo.EFECTIVO));
        assertFalse(stripePaymentService.supports(TipoMetodo.TRANSFERENCIA));
        assertFalse(stripePaymentService.supports(TipoMetodo.REDCOMPRA));
    }

    @Test
    void init_SetsStripeApiKey() {
        // Given
        StripePaymentService service = new StripePaymentService();
        ReflectionTestUtils.setField(service, "stripeSecretKey", testStripeKey);

        // When
        service.init();

        // Then
        assertEquals(testStripeKey, Stripe.apiKey);
    }

    @Test
    void createPaymentIntent_WithCOPCurrency() throws StripeException {
        // Given
        PaymentIntentRequestDto request = new PaymentIntentRequestDto(
                50000.0, "cop", TipoMetodo.TARJETA_CREDITO, 2L, null, null
        );

        PaymentIntent mockPaymentIntent = mock(PaymentIntent.class);
        when(mockPaymentIntent.getClientSecret()).thenReturn("pi_test_secret_cop");
        when(mockPaymentIntent.getId()).thenReturn("pi_test_cop");
        when(mockPaymentIntent.getStatus()).thenReturn("requires_payment_method");
        when(mockPaymentIntent.getAmountReceived()).thenReturn(0L);
        when(mockPaymentIntent.getPaymentMethodTypes()).thenReturn(List.of("card"));
        when(mockPaymentIntent.getCreated()).thenReturn(123456789L);
        when(mockPaymentIntent.getNextAction()).thenReturn(null);

        try (MockedStatic<PaymentIntent> mockedStatic = mockStatic(PaymentIntent.class)) {
            mockedStatic.when(() -> PaymentIntent.create(any(PaymentIntentCreateParams.class)))
                    .thenReturn(mockPaymentIntent);

            // When
            PaymentIntentResponseDto response = stripePaymentService.createPaymentIntent(request);

            // Then
            assertNotNull(response);
            assertEquals("pi_test_cop", response.paymentIntentId());
            assertEquals("pi_test_secret_cop", response.clientSecret());
        }
    }

    @Test
    void createPaymentIntent_WithNextAction() throws StripeException {
        // Given
        PaymentIntentRequestDto request = new PaymentIntentRequestDto(
                100.0, "usd", TipoMetodo.STRIPE, 1L, "test@email.com", null
        );

        PaymentIntent.NextAction mockNextAction = mock(PaymentIntent.NextAction.class);
        when(mockNextAction.getType()).thenReturn("redirect_to_url");

        PaymentIntent mockPaymentIntent = mock(PaymentIntent.class);
        when(mockPaymentIntent.getClientSecret()).thenReturn("pi_test_secret_123");
        when(mockPaymentIntent.getId()).thenReturn("pi_test_123");
        when(mockPaymentIntent.getStatus()).thenReturn("requires_action");
        when(mockPaymentIntent.getAmountReceived()).thenReturn(0L);
        when(mockPaymentIntent.getPaymentMethodTypes()).thenReturn(List.of("card"));
        when(mockPaymentIntent.getCreated()).thenReturn(123456789L);
        when(mockPaymentIntent.getNextAction()).thenReturn(mockNextAction);

        try (MockedStatic<PaymentIntent> mockedStatic = mockStatic(PaymentIntent.class)) {
            mockedStatic.when(() -> PaymentIntent.create(any(PaymentIntentCreateParams.class)))
                    .thenReturn(mockPaymentIntent);

            // When
            PaymentIntentResponseDto response = stripePaymentService.createPaymentIntent(request);

            // Then
            assertNotNull(response);
            assertEquals("pi_test_123", response.paymentIntentId());
            assertEquals("requires_action", response.status());
            assertTrue(response.requiresAction());
            assertEquals("redirect_to_url", response.nextActionType());
        }
    }

    @Test
    void confirmPayment_IntegrationFlow() throws StripeException {
        // Given
        PaymentConfirmationDto confirmation = new PaymentConfirmationDto(
                "pi_test_123", "pm_test_123", Map.of()
        );

        PaymentIntent mockPaymentIntent = mock(PaymentIntent.class);
        when(mockPaymentIntent.getId()).thenReturn("pi_test_123");
        when(mockPaymentIntent.getClientSecret()).thenReturn("pi_test_secret_123");
        when(mockPaymentIntent.getStatus()).thenReturn("succeeded");
        when(mockPaymentIntent.getAmountReceived()).thenReturn(10000L);

        try (MockedStatic<PaymentIntent> mockedStatic = mockStatic(PaymentIntent.class)) {
            mockedStatic.when(() -> PaymentIntent.retrieve("pi_test_123"))
                    .thenReturn(mockPaymentIntent);

            // El método confirm retorna el mismo objeto
            when(mockPaymentIntent.confirm(any(PaymentIntentConfirmParams.class)))
                    .thenReturn(mockPaymentIntent);

            // When
            PaymentIntentResponseDto response = stripePaymentService.confirmPayment(confirmation);

            // Then
            assertNotNull(response);
            assertEquals("succeeded", response.status());
            assertEquals("pi_test_123", response.paymentIntentId());
            assertNotNull(response.additionalData());

            // Verificar que se llamó a los métodos esperados
            verify(mockPaymentIntent).confirm(any(PaymentIntentConfirmParams.class));
        }
    }

    @Test
    void getPaymentStatus_WithEmptyCharges() throws StripeException {
        // Given
        String paymentIntentId = "pi_test_123";

        PaymentIntent mockPaymentIntent = mock(PaymentIntent.class);
        when(mockPaymentIntent.getId()).thenReturn(paymentIntentId);
        when(mockPaymentIntent.getClientSecret()).thenReturn("pi_test_secret_123");
        when(mockPaymentIntent.getStatus()).thenReturn("succeeded");
        when(mockPaymentIntent.getAmountReceived()).thenReturn(10000L);
        when(mockPaymentIntent.getPaymentMethod()).thenReturn("pm_test_123");
        when(mockPaymentIntent.getCreated()).thenReturn(123456789L);
        when(mockPaymentIntent.getCustomer()).thenReturn(null); // Customer puede ser null
        when(mockPaymentIntent.getDescription()).thenReturn(null); // Description puede ser null
        when(mockPaymentIntent.getLastPaymentError()).thenReturn(null);
        when(mockPaymentIntent.getNextAction()).thenReturn(null);

        try (MockedStatic<PaymentIntent> mockedStatic = mockStatic(PaymentIntent.class)) {
            mockedStatic.when(() -> PaymentIntent.retrieve(paymentIntentId))
                    .thenReturn(mockPaymentIntent);

            // When
            PaymentIntentResponseDto response = stripePaymentService.getPaymentStatus(paymentIntentId);

            // Then
            assertNotNull(response);
            assertEquals("succeeded", response.status());
            assertEquals(paymentIntentId, response.paymentIntentId());
            assertNotNull(response.additionalData());
        }
    }
}