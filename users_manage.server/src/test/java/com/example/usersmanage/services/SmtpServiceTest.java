package com.example.usersmanage.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.javamail.JavaMailSender;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("SMTP Service Tests")
class SmtpServiceTest {

    @Mock
    private JavaMailSender mailSender;

    @Mock
    private MimeMessage mimeMessage;

    @InjectMocks
    private SmtpService smtpService;

    @BeforeEach
    void setUp() {
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
    }

    @Test
    @DisplayName("Send Email - Success")
    void testSendEmail_Success() throws MessagingException {
        smtpService.SendEmail("test@example.com", "Subject", "Body");

        verify(mailSender, times(1)).createMimeMessage();
        verify(mailSender, times(1)).send(any(MimeMessage.class));
    }

    @Test
    @DisplayName("Send Email - MessagingException")
    void testSendEmail_Exception() {
        doThrow(new RuntimeException(new MessagingException("Error")))
                .when(mailSender).send(any(MimeMessage.class));

        assertThrows(RuntimeException.class, () -> {
            smtpService.SendEmail("test@example.com", "Subject", "Body");
        });
    }
}
