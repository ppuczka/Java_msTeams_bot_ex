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
                    HeroCard introCard = createIntroCard(userProfile.getName());
                    return turnContext.sendActivity(MessageFactory.attachment(introCard.toAttachment()));
                } else {
                    conversationData.setUserPromptedForName(true);
                    conversationData.setDidBotWelcomeUser(true);
                    return turnContext.sendActivity(MessageFactory.text("What is your name ?"));
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
        return switch (dayOfWeek) {
            case MONDAY -> "Ewa";
            case TUESDAY -> "Tomek";
            case WEDNESDAY -> "Wojtek";
            case THURSDAY -> "Przemek";
            case FRIDAY -> "Zbyszek";
            default -> "Invalid date";
        };
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

    private static HeroCard createIntroCard(String userName) {
        HeroCard card = new HeroCard() {{
            setTitle(String.format("Welcome to Bot Framework !", userName));
            setText("What do You wand to do ?");
        }};

        card.setImages(Collections.singletonList(new CardImage() {
            {
                setUrl("https://aka.ms/bf-welcome-card-image");
            }
        }));

        card.setButtons(Arrays.asList(
                new CardAction() {{
                    setType(ActionTypes.POST_BACK);
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

        return card;
    }
}