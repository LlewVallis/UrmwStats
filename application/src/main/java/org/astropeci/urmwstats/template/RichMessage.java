package org.astropeci.urmwstats.template;

import lombok.RequiredArgsConstructor;
import net.dv8tion.jda.api.JDA;
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

    public void send(MessageChannel channel) {
        Message postedMessage = channel.sendMessage(message).complete();

        for (String reaction : reactions) {
            postedMessage.addReaction(reaction).complete();
        }
    }

    public void edit(Message target, JDA jda) {
        boolean suppressEmbeds = message.getEmbeds().isEmpty();

        target.suppressEmbeds(suppressEmbeds).queue();
        target.editMessage(message).queue();

        List<CompletableFuture<Void>> reactionRemovals = new ArrayList<>();

        for (MessageReaction reaction : target.getReactions()) {
            if (reaction.isSelf()) {
                CompletableFuture<Void> future = reaction.removeReaction(jda.getSelfUser()).submit();
                reactionRemovals.add(future);
            }
        }

        CompletableFuture.allOf(reactionRemovals.toArray(new CompletableFuture[0])).join();

        for (String reaction : reactions) {
            target.addReaction(reaction).queue();
        }
    }
}
