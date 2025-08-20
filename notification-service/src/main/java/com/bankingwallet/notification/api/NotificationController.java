package com.bankingwallet.notification.api;

import com.bankingwallet.notification.read.NotificationRead;
import com.bankingwallet.notification.read.NotificationRepository;
import com.bankingwallet.notification.service.DeliveryService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.Map;

@RestController
@RequestMapping
public class NotificationController {
	private final NotificationRepository repository;
	private final DeliveryService deliveryService;

	public NotificationController(NotificationRepository repository, DeliveryService deliveryService) {
		this.repository = repository;
		this.deliveryService = deliveryService;
	}

	@GetMapping("/notifications/{userId}")
	@PreAuthorize("authentication.token.claims['sub'] == #userId or hasRole('ADMIN')")
	public Page<NotificationRead> list(@PathVariable String userId,
	                                  @RequestParam(defaultValue = "0") int page,
	                                  @RequestParam(defaultValue = "20") int size) {
		return repository.findByUserIdOrderByCreatedAtDesc(userId, PageRequest.of(page, size));
	}

	@PostMapping("/notifications/test")
	@PreAuthorize("hasRole('ADMIN')")
	public Map<String, Object> test(@RequestBody Map<String, String> body) {
		String userId = body.getOrDefault("userId", "test-user");
		String title = body.getOrDefault("title", "Test Notification");
		String message = body.getOrDefault("message", "Hello from notification-service");
		deliveryService.deliver(userId, "TEST", title, message);
		NotificationRead doc = new NotificationRead();
		doc.setUserId(userId);
		doc.setType("TEST");
		doc.setTitle(title);
		doc.setMessage(message);
		doc.setCreatedAt(Instant.now());
		repository.save(doc);
		return Map.of("status", "ok");
	}
}


