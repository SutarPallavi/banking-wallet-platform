package com.bankingwallet.notification.read;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Document("notification_read")
public class NotificationRead {
	@Id
	private String id;
	private String userId;
	private String type;
	private String title;
	private String message;
	private Instant createdAt;

	public String getId() { return id; }
	public String getUserId() { return userId; }
	public String getType() { return type; }
	public String getTitle() { return title; }
	public String getMessage() { return message; }
	public Instant getCreatedAt() { return createdAt; }

	public void setId(String id) { this.id = id; }
	public void setUserId(String userId) { this.userId = userId; }
	public void setType(String type) { this.type = type; }
	public void setTitle(String title) { this.title = title; }
	public void setMessage(String message) { this.message = message; }
	public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}


