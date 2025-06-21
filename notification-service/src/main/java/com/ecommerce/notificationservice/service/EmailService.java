package com.ecommerce.notificationservice.service;

import com.ecommerce.notificationservice.dto.OrderDetailsDTO;
import com.ecommerce.notificationservice.exception.SendEmailException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;

    public void sendReceiptEmail(String to, OrderDetailsDTO orderDetails) {
        try {
            Context context = new Context();
            context.setVariable("order", orderDetails);

            String htmlContent = templateEngine.process("receipt-template", context);

            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
            helper.setTo(to);
            helper.setSubject("Your Order Receipt #" + orderDetails.getOrderId());
            helper.setText(htmlContent, true);

            mailSender.send(mimeMessage);
        } catch (Exception e) {
            throw new SendEmailException("Failed to send email", e);
        }
    }
}