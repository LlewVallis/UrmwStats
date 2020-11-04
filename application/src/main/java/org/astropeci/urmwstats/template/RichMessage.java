package org.astropeci.urmwstats.template;

import lombok.RequiredArgsConstructor;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.MessageReaction;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@RequiredArgsConstructor
public class RichMessage {

    private final Message message;
    private final List<String> reactions;

    public Message send(MessageChannel channel) {
        Message postedMessage = channel.sendMessage(message).complete();

        for (String reaction : reactions) {
            postedMessage.addReaction(reaction).complete();
        }

        return postedMessage;
    }

    public void edit(Message target, JDA jda, boolean processReactions) {
        boolean suppressEmbeds = message.getEmbeds().isEmpty();

        CompletableFuture<Void> suppressFuture;
        if (target.isSuppressedEmbeds() != suppressEmbeds) {
            suppressFuture = target.suppressEmbeds(suppressEmbeds).submit();
        } else {
            suppressFuture = CompletableFuture.completedFuture(null);
        }

        CompletableFuture<Message> editFuture = target.editMessage(message).submit();

        if (processReactions) {
            List<CompletableFuture<Void>> removalFutures = new ArrayList<>();
            for (MessageReaction reaction : target.getReactions()) {
                if (reaction.isSelf()) {
                    CompletableFuture<Void> future = reaction.removeReaction(jda.getSelfUser()).submit();
                    removalFutures.add(future);
                }
            }

            CompletableFuture.allOf(removalFutures.toArray(new CompletableFuture[0])).join();

            List<CompletableFuture<Void>> addFutures = new ArrayList<>();
            for (String reaction : reactions) {
                CompletableFuture<Void> future = target.addReaction(reaction).submit();
                addFutures.add(future);
            }

            CompletableFuture.allOf(addFutures.toArray(new CompletableFuture[0])).join();
        }

        CompletableFuture.allOf(suppressFuture, editFuture).join();
    }

    public static RichMessage of(String contents) {
        return new RichMessage(
                new MessageBuilder(contents).build(),
                List.of()
        );
    }
}
