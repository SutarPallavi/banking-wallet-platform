package com.bankingwallet.notification.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;

@Service
public class DeliveryService {
	private static final Logger log = LoggerFactory.getLogger(DeliveryService.class);
	private final JavaMailSender mailSender;

	public DeliveryService(JavaMailSender mailSender) {
		this.mailSender = mailSender;
	}

	public void deliver(String userId, String type, String title, String message) {
		// Email
		try {
			SimpleMailMessage mail = new SimpleMailMessage();
			mail.setFrom("no-reply@wallet.local");
			mail.setTo(userId + "@wallet.local");
			mail.setSubject(title);
			mail.setText(message);
			mailSender.send(mail);
			log.info("Email sent to {} type={} title={}", userId, type, title);
		} catch (Exception e) {
			log.warn("Email send failed: {}", e.getMessage());
		}

		// SMS mock
		String line = Instant.now() + " | user=" + userId + " | " + type + " | " + title + " | " + message + "\n";
		log.info("SMS: {}", line);
		try {
			Path path = Path.of("/var/log/sms.log");
			Files.createDirectories(path.getParent());
			try (FileWriter fw = new FileWriter(path.toFile(), true)) {
				fw.write(line);
			}
		} catch (IOException e) {
			log.warn("SMS log write failed: {}", e.getMessage());
		}
	}
}


