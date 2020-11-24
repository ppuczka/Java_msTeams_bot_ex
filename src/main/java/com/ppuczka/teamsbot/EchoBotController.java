package com.ppuczka.teamsbot;

import com.codepoetics.protonpack.collectors.CompletableFutures;
import com.microsoft.bot.builder.*;
import com.microsoft.bot.schema.*;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Component
@RequiredArgsConstructor
public class EchoBotController extends ActivityHandler {


    // Messages sent to the user.
    private static final String WELCOME_MESSAGE =
            "This is a simple Welcome DevOps channel info Bot sample. " +
                    "This Bot will provide information about: " +
                    "DevOps of the Day and also about DevOps team absence" +
                    "Type anything to get started";

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
    private final ConversationReferences references;
    private final ConversationState conversationState;

    @Override
    public CompletableFuture<Void> onTurn(TurnContext turnContext) {
        return super.onTurn(turnContext)
                .thenCompose(saveResult -> userState.saveChanges(turnContext))
                .thenCompose(saveResult -> conversationState.saveChanges(turnContext));
    }

    @Override
    protected CompletableFuture<Void> onMembersAdded(List<ChannelAccount> membersAdded,
                                                     TurnContext turnContext) {
        String manOfTheDay = getManOfTheDay();
        return membersAdded.stream()
                .filter(
                        member -> !StringUtils
                                .equals(member.getId(), turnContext.getActivity().getRecipient().getId())
                ).map(channel -> turnContext.sendActivities(
                        MessageFactory.text("Hi there! " + WELCOME_MESSAGE),
                        MessageFactory.text(manOfTheDay + " is man of the day")))
                        .collect(CompletableFutures.toFutureList())
                        .thenApply(resourceResponses -> null);
    }

    @Override
    protected CompletableFuture<Void> onMessageActivity(TurnContext turnContext) {
        StatePropertyAccessor<ConversationData> dataAccessor = conversationState.createProperty("data");
        CompletableFuture<ConversationData> dataFuture = dataAccessor.get(turnContext, ConversationData::new);

        StatePropertyAccessor<UserProfile> profileAccessor = userState.createProperty("profile");
        CompletableFuture<UserProfile> profileFuture = profileAccessor.get(turnContext, UserProfile::new);

        return dataFuture.thenCombine(profileFuture, ((conversationData, userProfile) -> {
            if (StringUtils.isEmpty(userProfile.getName())) {
                if (conversationData.isUserPromptedForName()) {
                    conversationData.setUserPromptedForName(false);
                    conversationData.setDidBotWelcomeUser(true);

                    userProfile.setName(turnContext.getActivity().getText());
                    return turnContext.sendActivities(MessageFactory.text(FIRST_WELCOME_ONE),
                        MessageFactory.text(String.format(FIRST_WELCOME_TWO, userProfile.getName())));

               } else {
                    conversationData.setUserPromptedForName(true);
                    conversationData.setDidBotWelcomeUser(true);
                    return turnContext.sendActivity(MessageFactory.text("What is your name"));
                }
            } else {
                conversationData.setUserPromptedForName(true);
                conversationData.setDidBotWelcomeUser(true);

                OffsetDateTime messageTimeOffset = turnContext.getActivity().getLocalTimestamp();
                LocalDateTime localMessageTime = messageTimeOffset.toLocalDateTime();
                conversationData.setTimestamp(localMessageTime.toString());
                conversationData.setChannelId(turnContext.getActivity().getChannelId());

                List<Activity> sendToUser = List.of(
                        MessageFactory.text(userProfile.getName() + " sent " + turnContext.getActivity().getText()),
                        MessageFactory.text(userProfile.getName() + " message received at " + conversationData.getTimestamp()),
                        MessageFactory.text(userProfile.getName() + " message received from " + conversationData.getChannelId()));

                return turnContext.sendActivities(sendToUser);
            }
        })).thenApply(response -> null);
    }

    public static String getManOfTheDay() {
        DayOfWeek dayOfWeek = LocalDateTime.now().getDayOfWeek();
        switch (dayOfWeek) {
            case MONDAY:
                return "Ewa";
            case TUESDAY:
                return "Tomek";
            case WEDNESDAY:
                return "Wojtek";
            case THURSDAY:
                return "Przemek";
            case FRIDAY:
                return "Zbyszek";
            default: return "Invalid date";
        }
    }

    @Override
    protected CompletableFuture<Void> onConversationUpdateActivity(TurnContext turnContext) {
        addConversationReference(turnContext.getActivity());
        return super.onConversationUpdateActivity(turnContext);
    }

    private void addConversationReference(Activity activity) {
        ConversationReference conversationReference = activity.getConversationReference();
        references.put(conversationReference.getUser().getId(), conversationReference);
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