package dev.project.bedtimestory.service.notification;

import dev.project.bedtimestory.exception.ApiException;
import dev.project.bedtimestory.repository.UserRepository;
import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

@Service
@Slf4j
@RequiredArgsConstructor
public class NotificationService {
    private final EmailSenderService emailSenderService;
    private final UserRepository userRepository;
    private final TemplateEngine templateEngine;
    @Async
    public void sendNotification(String storyTitle, String storyDescription) {
        var emails = userRepository.getSubscribedEmails();
        emails.forEach(email -> {
            String title = "New story published!";
            String htmlBody = renderEmailTemplate(storyTitle, storyDescription);
            try {
                emailSenderService.send(email, title, htmlBody);
            } catch (final MessagingException e) {
                log.error("Failed to send email to {}: {}", email, e.getMessage());
                throw new ApiException(e.getMessage());
            }
        });
    }
    private String renderEmailTemplate(String title, String description) {
        Context context = new Context();
        context.setVariable("storyTitle", title);
        context.setVariable("storyDescription", description);
        return templateEngine.process("notification-email-template", context);
    }
}