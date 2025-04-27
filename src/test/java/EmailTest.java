package com.ntn.quanlykhoahoc.services;

import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Properties;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmailTest {

    @InjectMocks
    private EmailService emailService;

    @Mock
    private Session mockSession;

    @Captor
    private ArgumentCaptor<Properties> propertiesCaptor;

    @Captor
    private ArgumentCaptor<InternetAddress> addressCaptor;

    @Captor
    private ArgumentCaptor<String> stringCaptor;

    @Test
    void testIsValidEmail_ValidEmail() {
        assertTrue(emailService.isValidEmail("nhatlovely2017@gmail.com"));
        assertTrue(emailService.isValidEmail("user123@company.co.vn"));
        assertTrue(emailService.isValidEmail("test.email+label@subdomain.example.com"));
        assertTrue(emailService.isValidEmail("2251052082nhat@ou.edu.vn"));
    }

    @Test
    void testIsValidEmail_InvalidEmail() {
        assertFalse(emailService.isValidEmail("invalid.email@"));
        assertFalse(emailService.isValidEmail("user@domain"));
        assertFalse(emailService.isValidEmail("user@domain.123"));
        assertFalse(emailService.isValidEmail("@domain.com"));
        assertFalse(emailService.isValidEmail("user@.com"));
    }

    @Test
    void testSendOtpEmail_CallsSendEmailWithCorrectParameters() throws MessagingException {
        String toEmail = "2251052082nhat@ou.edu.vn";
        String otp = "123456";

        try (MockedStatic<Session> mockedSession = mockStatic(Session.class);
             MockedStatic<InternetAddress> mockedInternetAddress = mockStatic(InternetAddress.class);
             MockedConstruction<InternetAddress> mockedInternetAddressConstruction = mockConstruction(InternetAddress.class);
             MockedStatic<Transport> mockedTransport = mockStatic(Transport.class);
             MockedConstruction<MimeMessage> mockedMimeMessage = mockConstruction(MimeMessage.class,
                     (mock, context) -> {
                         when(mock.getSession()).thenReturn(mockSession);
                     })) {

            mockedSession.when(() -> Session.getInstance(any(Properties.class), any(Authenticator.class)))
                    .thenReturn(mockSession);

            InternetAddress[] addresses = new InternetAddress[]{new InternetAddress(toEmail)};
            mockedInternetAddress.when(() -> InternetAddress.parse(toEmail)).thenReturn(addresses);

            mockedTransport.when(() -> Transport.send(any(Message.class))).thenAnswer(invocation -> null);

            boolean result = emailService.sendOtpEmail(toEmail, otp);

            assertTrue(result);
            
            MimeMessage messageMock = mockedMimeMessage.constructed().get(0);
            verify(messageMock).setFrom(any(InternetAddress.class));
            verify(messageMock).setRecipients(eq(Message.RecipientType.TO), eq(addresses));
            verify(messageMock).setSubject("Mã OTP xác thực");
            verify(messageMock).setText(contains(otp));
        }
    }

    @Test
    void testSendOtpEmail_InvalidEmail() {
        String toEmail = "invalid@domain";
        String otp = "123456";

        boolean result = emailService.sendOtpEmail(toEmail, otp);

        assertFalse(result);
    }

    @Test
    void testSendEmail_Success() throws MessagingException {
        String toEmail = "2251052082nhat@ou.edu.vn";
        String subject = "Test Subject";
        String content = "Test Content";

        try (MockedStatic<Session> mockedSession = mockStatic(Session.class);
             MockedStatic<InternetAddress> mockedInternetAddress = mockStatic(InternetAddress.class);
             MockedConstruction<InternetAddress> mockedInternetAddressConstruction = mockConstruction(InternetAddress.class);
             MockedStatic<Transport> mockedTransport = mockStatic(Transport.class);
             MockedConstruction<MimeMessage> mockedMimeMessage = mockConstruction(MimeMessage.class,
                     (mock, context) -> {
                         when(mock.getSession()).thenReturn(mockSession);
                     })) {

            mockedSession.when(() -> Session.getInstance(any(Properties.class), any(Authenticator.class)))
                    .thenReturn(mockSession);

            InternetAddress[] addresses = new InternetAddress[]{new InternetAddress(toEmail)};
            mockedInternetAddress.when(() -> InternetAddress.parse(toEmail)).thenReturn(addresses);

            mockedTransport.when(() -> Transport.send(any(Message.class))).thenAnswer(invocation -> null);

            boolean result = emailService.sendEmail(toEmail, subject, content);

            assertTrue(result);
            
            MimeMessage messageMock = mockedMimeMessage.constructed().get(0);
            verify(messageMock).setFrom(any(InternetAddress.class));
            verify(messageMock).setRecipients(eq(Message.RecipientType.TO), eq(addresses));
            verify(messageMock).setSubject(subject);
            verify(messageMock).setText(content);

            mockedSession.verify(() -> Session.getInstance(propertiesCaptor.capture(), any(Authenticator.class)));
            Properties props = propertiesCaptor.getValue();
            assertEquals("true", props.getProperty("mail.smtp.auth"));
            assertEquals("true", props.getProperty("mail.smtp.starttls.enable"));
            assertEquals("smtp.gmail.com", props.getProperty("mail.smtp.host"));
            assertEquals("587", props.getProperty("mail.smtp.port"));
        }
    }

    @Test
    void testSendEmail_InvalidEmail() {
        String toEmail = "invalid@domain";
        String subject = "Test Subject";
        String content = "Test Content";

        boolean result = emailService.sendEmail(toEmail, subject, content);

        assertFalse(result);
    }

    @Test
    void testSendEmail_MessagingException() throws MessagingException {
        String toEmail = "2251052082nhat@ou.edu.vn";
        String subject = "Test Subject";
        String content = "Test Content";

        try (MockedStatic<Session> mockedSession = mockStatic(Session.class);
             MockedStatic<InternetAddress> mockedInternetAddress = mockStatic(InternetAddress.class);
             MockedConstruction<InternetAddress> mockedInternetAddressConstruction = mockConstruction(InternetAddress.class);
             MockedStatic<Transport> mockedTransport = mockStatic(Transport.class);
             MockedConstruction<MimeMessage> mockedMimeMessage = mockConstruction(MimeMessage.class,
                     (mock, context) -> {
                         when(mock.getSession()).thenReturn(mockSession);
                         doThrow(new MessagingException("SMTP error")).when(mock)
                                 .setRecipients(any(Message.RecipientType.class), any(InternetAddress[].class));
                     })) {

            mockedSession.when(() -> Session.getInstance(any(Properties.class), any(Authenticator.class)))
                    .thenReturn(mockSession);

            InternetAddress[] addresses = new InternetAddress[]{new InternetAddress(toEmail)};
            mockedInternetAddress.when(() -> InternetAddress.parse(toEmail)).thenReturn(addresses);

            boolean result = emailService.sendEmail(toEmail, subject, content);

            assertFalse(result);
            
            MimeMessage messageMock = mockedMimeMessage.constructed().get(0);
            verify(messageMock).setFrom(any(InternetAddress.class));
            verify(messageMock).setRecipients(any(Message.RecipientType.class), any(InternetAddress[].class));
        }
    }
}