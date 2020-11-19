package com.ppuczka.teamsbot;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ConversationData {

    private String timestamp;
    private String channelId;
    private boolean userPromptedForName;
    private boolean didBotWelcomeUser;

}
