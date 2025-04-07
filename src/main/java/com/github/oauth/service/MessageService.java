package com.github.oauth.service;

import com.github.oauth.model.Message;
import com.github.oauth.model.User;

public interface MessageService {

    String addMessage(Message message, User user);
    
    String deleteMessage(String messageId, User user);
}
