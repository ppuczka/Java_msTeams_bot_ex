package com.ppuczka.teamsbot;

import com.microsoft.bot.integration.BotFrameworkHttpAdapter;
import com.microsoft.bot.integration.Configuration;
import com.microsoft.bot.schema.ConversationReference;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
public class NotifyController {

    private final BotFrameworkHttpAdapter adapter;

    private final ConversationReferences conversationReferences;
    private String appId;

    @Autowired
    public NotifyController(BotFrameworkHttpAdapter adapter, Configuration withConfiguration,
                            ConversationReferences conversationReferences) {
        this.adapter = adapter;
        this.conversationReferences = conversationReferences;
        this.appId = withConfiguration.getProperty("MicrosoftAppId");
        if (StringUtils.isEmpty(this.appId)) {
            appId = UUID.randomUUID().toString();
        }
    }

    @GetMapping("/api/notify")
    public ResponseEntity<Object> proactiveMessage() {
        for (ConversationReference reference: conversationReferences.values()) {
            adapter.continueConversation(appId, reference, turnContext ->
                    turnContext.sendActivity("proactive hello").thenApply(resourceResponse -> null));
        }
    return new ResponseEntity<>(
            "<html><body><h1>Proactive messages have been sent.</h1></body></html>", HttpStatus.ACCEPTED);
    }
}

