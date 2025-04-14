package com.github.oauth.service.impl;

import com.github.oauth.model.Message;
import com.github.oauth.model.User;
import com.github.oauth.service.MessageService;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.WriteResult;
import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.Timestamp;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class MessageServiceImpl implements MessageService {

    private static final Logger logger = LoggerFactory.getLogger(MessageServiceImpl.class);

    private Firestore firestore;


    public MessageServiceImpl(Firestore firestore) {
        this.firestore = firestore;
    }

    @Override
    public String addMessage(Message message, User user) {
        try {
            // Create a unique document ID
            String docId = user.getId() + "_" + message.getMessage() + "_" + message.getProjectId();
            
            // Create message data
            Map<String, Object> messageData = new HashMap<>();
            messageData.put("senderId", user.getId());
            messageData.put("projectId", message.getProjectId());
            messageData.put("message", message.getMessage());
            messageData.put("timestamp", Timestamp.now());

            // Save to Firestore
            DocumentReference docRef = firestore.collection("Messages").document(docId);
            ApiFuture<WriteResult> result = docRef.set(messageData);
            
            logger.info("Message saved successfully with ID: {}", docId);
            return "Message saved with updateTime: " + result.get().getUpdateTime();
        } catch (Exception e) {
            logger.error("Error saving message", e);
            throw new RuntimeException("Failed to save message", e);
        }
    }

    @Override
    public String deleteMessage(String messageId, User user) {
        try {
            // Get the message document
            DocumentReference docRef = firestore.collection("Messages").document(messageId);
            ApiFuture<DocumentSnapshot> future = docRef.get();
            DocumentSnapshot document = future.get();

            if (!document.exists()) {
                logger.warn("Message not found with ID: {}", messageId);
                return "Message not found";
            }

            // Check if the user is the sender
            Long senderId = document.getLong("senderId");
            if (!user.getId().equals(senderId)) {
                logger.warn("User {} attempted to delete message sent by {}", user.getId(), senderId);
                return "You can only delete your own messages";
            }

            // Delete the message
            // ApiFuture<WriteResult> result = docRef.delete();
            docRef.delete();
            logger.info("Message deleted successfully with ID: {}", messageId);
            return "Message deleted successfully";
        } catch (Exception e) {
            logger.error("Error deleting message", e);
            throw new RuntimeException("Failed to delete message", e);
        }
    }
}
