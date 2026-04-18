package com.example.logs;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class LogsConsumer {

    @Autowired
    private MailService emailService;

    @KafkaListener(topics="transfer-topic", groupId = "logs-group")
    public void handleLogs(String message) {
        System.out.println("Log service a primit mesajul: " + message);
        try {
            emailService.sendNotification(
                    "laurentiutopai2004@gmail.com",
                    "Notificare Transfer",
                    "S-a procesat un transfer nou: " + message
            );
            System.out.println("Email trimis cu succes!");
        } catch (Exception e) {
            System.err.println("Eroare la trimiterea mail-ului: " + e.getMessage());
        }
    }
}