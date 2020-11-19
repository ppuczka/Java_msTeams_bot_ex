//package com.ppuczka.teamsbot;
//
//import com.codepoetics.protonpack.collectors.CompletableFutures;
//import com.microsoft.bot.builder.ActivityHandler;
//import com.microsoft.bot.builder.MessageFactory;
//import com.microsoft.bot.builder.TurnContext;
//import com.microsoft.bot.schema.*;
//import org.apache.commons.lang3.StringUtils;
//import org.springframework.stereotype.Component;
//
//import java.util.Arrays;
//import java.util.List;
//import java.util.concurrent.CompletableFuture;
//
//@Component
//public class SuggestedActionsBotController extends ActivityHandler {
//
//    public static final String WELCOMETEXT =
//            "This bot will introduce you to suggestedActions." + " Please answer the question:";
//
//    @Override
//    protected CompletableFuture<Void> onMembersAdded(List<ChannelAccount> membersAdded, TurnContext turnContext) {
//        return sendWelcomeMessage(turnContext);
//
//    }
//
//    @Override
//    protected CompletableFuture<Void> onMessageActivity(TurnContext turnContext) {
//        String text = turnContext.getActivity().getText().toLowerCase();
//        String responseText = processInput(text);
//
//        return turnContext.sendActivities(MessageFactory.text(responseText), createSuggestedActions())
//                .thenApply(resourceResponses -> null);
//    }
//
//    private CompletableFuture<Void> sendWelcomeMessage(TurnContext turnContext) {
//        return turnContext.getActivity().getMembersAdded().stream().filter(
//                member -> !StringUtils.equals(member.getId(), turnContext.getActivity().getRecipient().getId())
//        ).map(
//                channelAccount -> turnContext.sendActivities(MessageFactory.text("Welcome to SugesionsBot" +
//                        channelAccount.getName() + ", " + WELCOMETEXT), createSuggestedActions())
//        )
//                .collect(CompletableFutures.toFutureList())
//                .thenApply(resourceResponses -> null);
//    }
//
//    private String processInput(String text) {
//        String colorText = "is the best color. I agree.";
//        switch (text) {
//            case "red":
//                return "Red " + colorText;
//            case "yellow":
//                return "Yellow " + colorText;
//            case "blue":
//                return "Blue " + colorText;
//
//            default:
//                return "Please select a color from the suggested action choices";
//        }
//    }
//
//    private Activity createSuggestedActions() {
//        Activity reply = MessageFactory.text("What is your favorite color?");
//
//        reply.setSuggestedActions(new SuggestedActions() {
//            {
//                setActions(Arrays.asList(new CardAction() {
//                    {
//                        setTitle("Red");
//                        setType(ActionTypes.IM_BACK);
//                        setValue("Red");
//                        setImage("https://via.placeholder.com/20/FF0000?text=R");
//                        setImageAltText("R");
//                    }
//                }, new CardAction() {
//                    {
//                        setTitle("Yellow");
//                        setType(ActionTypes.IM_BACK);
//                        setValue("Yellow");
//                        setImage("https://via.placeholder.com/20/FFFF00?text=Y");
//                        setImageAltText("Y");
//                    }
//                }, new CardAction() {
//                    {
//                        setTitle("Blue");
//                        setType(ActionTypes.IM_BACK);
//                        setValue("Blue");
//                        setImage("https://via.placeholder.com/20/0000FF?text=B");
//                        setImageAltText("B");
//                    }
//                }));
//            }
//        });
//
//        return reply;
//    }
//}