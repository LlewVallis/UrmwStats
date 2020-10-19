package org.astropeci.urmwstats.ingestion;

import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Message;
import org.astropeci.urmwstats.data.Match;
import org.astropeci.urmwstats.data.MatchParticipant;
import org.astropeci.urmwstats.data.Player;
import org.astropeci.urmwstats.data.Skill;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Slf4j
public class MatchParser {

    private static final Pattern MATCH_HEADER_REGEX = Pattern.compile("(?<team1>.+) vs (?<team2>.+)");
    private static final Pattern MATCH_PARTICIPANT_REGEX =
            Pattern.compile("(?<name>\\S+) (\\*\\*[+-]\\d+\\*\\*,\\*\\*[+-]\\d+\\*\\* )?\\((?<trueskill>\\d+)\\D+(?<deviation>\\d+)\\)");

    private int id = 0;

    public Match parseMatch(Message message, Map<String, Player> playersByName) {
        String content = message.getContentRaw();

        if (!content.startsWith("-")) {
            throw new HistoryParser.ParseException();
        }

        List<String> lines = content.lines().collect(Collectors.toList());
        if (lines.size() < 7) {
            log.warn("Match message did not have 7 lines, ignoring\n" + content);
            throw new HistoryParser.ParseException();
        }

        Matcher headerMatcher = MATCH_HEADER_REGEX.matcher(lines.get(1));
        if (!headerMatcher.find()) {
            log.warn("Match message did not have valid header, ignoring\n" + content);
            throw new HistoryParser.ParseException();
        }

        String winnersBeforeString = headerMatcher.group("team1");
        String losersBeforeString = headerMatcher.group("team2");
        String winnersAfterString = lines.get(4);
        String losersAfterString = lines.get(6);

        if (lines.get(2).contains("2")) {
            String beforeSwap = winnersBeforeString;
            winnersBeforeString = losersBeforeString;
            losersBeforeString = beforeSwap;

            String afterSwap = winnersAfterString;
            winnersAfterString = losersAfterString;
            losersAfterString = afterSwap;
        } else if (!lines.get(2).contains("1")) {
            log.warn("Match message did not have valid winner, ignoring\n" + content);
            throw new HistoryParser.ParseException();
        }

        Set<MatchParticipant> winners = parseParticipants(content, winnersBeforeString, winnersAfterString);
        Set<MatchParticipant> losers = parseParticipants(content, losersBeforeString, losersAfterString);

        Match match = new Match(id++, winners, losers, message.getTimeCreated().toInstant());

        for (MatchParticipant participant : match.getParticipants()) {
            Player player = playersByName.get(participant.getName());
            if (player == null) {
                log.warn("Match participant " + participant.getName() + " missing");
                continue;
            }

            if (winners.contains(participant)) {
                player.setWins(player.getWins() + 1);
                player.setStreak(Math.max(player.getStreak(), 0) + 1);

                for (MatchParticipant opponent : losers) {
                    player.getWinsAgainst().merge(opponent.getName(), 1, Integer::sum);
                }
            }

            if (losers.contains(participant)) {
                player.setLosses(player.getLosses() + 1);
                player.setStreak(Math.min(player.getStreak(), 0) - 1);

                for (MatchParticipant opponent : winners) {
                    player.getLossesAgainst().merge(opponent.getName(), 1, Integer::sum);
                }
            }

            Comparator<Skill> skillComparator = Comparator.comparingDouble(Skill::getTrueskill).thenComparing(Skill::getMean);
            if (skillComparator.compare(player.getPeakSkill(), participant.getSkillAfter()) < 0) {
                player.setPeakSkill(participant.getSkillAfter());
            }
        }

        return match;
    }

    private Set<MatchParticipant> parseParticipants(String content, String beforeString, String afterString) {
        Map<String, Skill> before = parseTeamListing(content, beforeString);
        Map<String, Skill> after = parseTeamListing(content, afterString);

        return before.keySet().stream()
                .map(name -> {
                    Skill skillBefore = before.get(name);
                    Skill skillAfter = after.get(name);

                    if (skillAfter == null) {
                        log.warn("Match message did not contain updated trueskill value for " + name + "\n" + content);
                    }

                    return new MatchParticipant(name, skillBefore, skillAfter);
                }).collect(Collectors.toSet());
    }

    private Map<String, Skill> parseTeamListing(String content, String teamString) {
        Map<String, Skill> participants = new HashMap<>();

        Matcher matcher = MATCH_PARTICIPANT_REGEX.matcher(teamString);
        while (matcher.find()) {
            String name = matcher.group("name");
            String trueskillString = matcher.group("trueskill");
            String deviationString = matcher.group("deviation");

            Skill skill = parseSkill(content, trueskillString, deviationString);
            participants.put(name, skill);
        }

        return participants;
    }

    private Skill parseSkill(String content, String trueskillString, String deviationString) {
        try {
            double trueskill = Double.parseDouble(trueskillString);
            double deviation = Double.parseDouble(deviationString);
            return Skill.fromTrueskill(trueskill, deviation);
        } catch (NumberFormatException e) {
            log.warn("Match message had invalid trueskill values, ignoring\n" + content);
            throw new HistoryParser.ParseException();
        }
    }
}
