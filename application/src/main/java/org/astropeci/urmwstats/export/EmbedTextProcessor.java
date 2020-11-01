package org.astropeci.urmwstats.export;

import lombok.RequiredArgsConstructor;
import lombok.Value;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.utils.MiscUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@RequiredArgsConstructor
public class EmbedTextProcessor {

    private final JDA jda;
    private final UserCache userCache;
    private final MemberCache memberCache;

    @Value
    private static class ReducedEmote {
        long id;
        String name;
        boolean animated;

        public String getAsMention() {
            return (isAnimated() ? "<a:" : "<:") + getName() + ":" + getId() + ">";
        }
    }

    public String process(String content) {
        if (content == null) {
            return null;
        }

        for (User user : mentionedUsers(content, userCache)) {
            Member member = memberCache.retrieve(user.getIdLong());

            String name;
            if (member == null) {
                name = user.getName();
            } else {
                name = member.getEffectiveName();
            }

            content = content.replaceAll("<@!?" + Pattern.quote(user.getId()) + '>', '@' + Matcher.quoteReplacement(name));
        }

        for (ReducedEmote emote : usedEmotes(content)) {
            content = content.replace(emote.getAsMention(), ":" + emote.getName() + ":");
        }

        for (TextChannel mentionedChannel : mentionedChannels(content)) {
            content = content.replace(mentionedChannel.getAsMention(), '#' + mentionedChannel.getName());
        }

        for (Role mentionedRole : mentionedRoles(content)) {
            content = content.replace(mentionedRole.getAsMention(), '@' + mentionedRole.getName());
        }

        return content;
    }

    private List<User> mentionedUsers(String content, UserCache userCache) {
        return processMentions(
                content,
                Message.MentionType.USER,
                matcher -> matchUser(matcher, userCache)
        );
    }

    private List<ReducedEmote> usedEmotes(String content) {
        return processMentions(content, Message.MentionType.EMOTE, this::matchEmote);
    }

    private List<TextChannel> mentionedChannels(String content) {
        return processMentions(content, Message.MentionType.CHANNEL, this::matchChannel);
    }

    private List<Role> mentionedRoles(String content) {
        return processMentions(content, Message.MentionType.ROLE, this::matchRole);
    }

    private User matchUser(Matcher matcher, UserCache userCache) {
        long id = MiscUtil.parseSnowflake(matcher.group(1));
        return userCache.retrieve(id);
    }

    private ReducedEmote matchEmote(Matcher matcher) {
        long id = MiscUtil.parseSnowflake(matcher.group(2));
        String name = matcher.group(1);
        boolean animated = matcher.group(0).startsWith("<a:");
        return new ReducedEmote(id, name, animated);
    }

    private TextChannel matchChannel(Matcher matcher) {
        long id = MiscUtil.parseSnowflake(matcher.group(1));
        return jda.getTextChannelById(id);
    }

    private Role matchRole(Matcher matcher) {
        long id = MiscUtil.parseSnowflake(matcher.group(1));
        return jda.getRoleById(id);
    }

    private <T> List<T> processMentions(
            String content,
            Message.MentionType type,
            Function<Matcher, T> map
    ) {
        List<T> result = new ArrayList<>();
        Matcher matcher = type.getPattern().matcher(content);

        while (matcher.find()) {
            try {
                T elem = map.apply(matcher);
                if (elem == null || result.contains(elem)) {
                    continue;
                }

                result.add(elem);
            } catch (NumberFormatException ignored) { }
        }

        return result;
    }
}
