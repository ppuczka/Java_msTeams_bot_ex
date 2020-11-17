package com.ppuczka.teamsbot;

import com.codepoetics.protonpack.collectors.CompletableFutures;
import com.microsoft.bot.builder.*;
import com.microsoft.bot.schema.*;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Component
@RequiredArgsConstructor
public class EchoBotController extends ActivityHandler {

    // Messages sent to the user.
    private static final String WELCOMEMESSAGE =
            "This is a simple Welcome Bot sample. This bot will introduce you "
                    + "to welcoming and greeting users. You can say 'intro' to see the "
                    + "introduction card. If you are running this bot in the Bot Framework "
                    + "Emulator, press the 'Start Over' button to simulate user joining "
                    + "a bot or a channel";

    private static final String INFOMESSAGE =
            "You are seeing this message because the bot received at least one "
                    + "'ConversationUpdate' event, indicating you (and possibly others) "
                    + "joined the conversation. If you are using the emulator, pressing "
                    + "the 'Start Over' button to trigger this event again. The specifics "
                    + "of the 'ConversationUpdate' event depends on the channel. You can "
                    + "read more information at: " + "https://aka.ms/about-botframework-welcome-user";

    private static final String LOCALEMESSAGE =
            "You can use the activity's GetLocale() method to welcome the user "
                    + "using the locale received from the channel. "
                    + "If you are using the Emulator, you can set this value in Settings.";

    private static final String PATTERNMESSAGE =
            "It is a good pattern to use this event to send general greeting"
                    + "to user, explaining what your bot can do. In this example, the bot "
                    + "handles 'hello', 'hi', 'help' and 'intro'. Try it now, type 'hi'";

    private static final String FIRST_WELCOME_ONE =
            "You are seeing this message because this was your first message ever to this bot.";

    private static final String FIRST_WELCOME_TWO =
            "It is a good practice to welcome the user and provide personal greeting. For example: Welcome %s";

    private final UserState userState;

    @Override
    public CompletableFuture<Void> onTurn(TurnContext turnContext) {
        return super.onTurn(turnContext)
                .thenCompose(saveResult -> userState.saveChanges(turnContext));
    }

    @Override
    protected CompletableFuture<Void> onMessageActivity(TurnContext turnContext) {
        StatePropertyAccessor<WelcomeUserState> stateAccessor = userState.createProperty("WelcomeUserState");
        CompletableFuture<WelcomeUserState> stateFuture = stateAccessor.get(turnContext, WelcomeUserState::new);

        return stateFuture.thenApply(thisUserState -> {
            if (!thisUserState.isDidBotWelcomeUser()) {
                thisUserState.setDidBotWelcomeUser(true);

                String userName = turnContext.getActivity().getFrom().getName();
                return turnContext.sendActivities(MessageFactory.text(FIRST_WELCOME_ONE),
                        MessageFactory.text(String.format(FIRST_WELCOME_TWO, userName)));
            } else {
                String text = turnContext.getActivity().getText().toLowerCase();
                switch (text) {
                    case "hello":
                    case "hi":
                        return turnContext.sendActivity(MessageFactory.text("You said " + text));
                    case "intro":
                    case "help":
                        return sendIntroCard(turnContext);

                    default:
                        return turnContext.sendActivity(MessageFactory.text(WELCOMEMESSAGE));
                }
            }
        })
                .thenApply(resourceResponse -> null);

    }


    @Override
    protected CompletableFuture<Void> onMembersAdded(List<ChannelAccount> membersAdded,
                                                     TurnContext turnContext) {
        return membersAdded.stream()
                .filter(
                        member -> !StringUtils
                                .equals(member.getId(), turnContext.getActivity().getRecipient().getId())
                ).map(channel -> turnContext.sendActivities(
                        MessageFactory.text("Hi there -" + channel.getName() + ". " + WELCOMEMESSAGE),
                        MessageFactory.text(LOCALEMESSAGE + " Current Locale is " + turnContext.getActivity().getLocale()),
                        MessageFactory.text(INFOMESSAGE),
                        MessageFactory.text(PATTERNMESSAGE))).collect(CompletableFutures.toFutureList())
                .thenApply(resourceResponses -> null);
    }


    private CompletableFuture<ResourceResponse> sendIntroCard(TurnContext turnContext) {
        HeroCard card = new HeroCard() {{
            setTitle("Welcome to Bot Framework!");
            setText(
                    "Welcome to Welcome Users bot sample! This Introduction card "
                            + "is a great way to introduce your Bot to the user and suggest "
                            + "some things to get them started. We use this opportunity to "
                            + "recommend a few next steps for learning more creating and deploying bots."
            );
        }};

        card.setImages(Collections.singletonList(new CardImage() {
            {
                setUrl("https://aka.ms/bf-welcome-card-image");
            }
        }));

        card.setButtons(Arrays.asList(
                new CardAction() {{
                    setType(ActionTypes.OPEN_URL);
                    setTitle("Get an overview");
                    setText("Get an overview");
                    setDisplayText("Get an overview");
                    setValue(
                            "https://docs.microsoft.com/en-us/azure/bot-service/?view=azure-bot-service-4.0"
                    );
                }},
                new CardAction() {{
                    setType(ActionTypes.OPEN_URL);
                    setTitle("Ask a question");
                    setText("Ask a question");
                    setDisplayText("Ask a question");
                    setValue("https://stackoverflow.com/questions/tagged/botframework");
                }},
                new CardAction() {{
                    setType(ActionTypes.OPEN_URL);
                    setTitle("Learn how to deploy");
                    setText("Learn how to deploy");
                    setDisplayText("Learn how to deploy");
                    setValue(
                            "https://docs.microsoft.com/en-us/azure/bot-service/bot-builder-howto-deploy-azure?view=azure-bot-service-4.0"
                    );
                }})
        );

        Activity response = MessageFactory.attachment(card.toAttachment());
        return turnContext.sendActivity(response);
    }
}