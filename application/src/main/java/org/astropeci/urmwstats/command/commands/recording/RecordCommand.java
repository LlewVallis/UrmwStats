package org.astropeci.urmwstats.command.commands.recording;

import lombok.RequiredArgsConstructor;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.VoiceChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.astropeci.urmwstats.command.Command;
import org.astropeci.urmwstats.command.CommandException;
import org.astropeci.urmwstats.command.CommandUtil;
import org.astropeci.urmwstats.command.HelpSection;
import org.astropeci.urmwstats.recording.AlreadyRecordingException;
import org.astropeci.urmwstats.recording.RecordingController;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class RecordCommand implements Command {

    private final RecordingController recordingController;

    @Override
    public String label() {
        return "record";
    }

    @Override
    public String usage() {
        return "record";
    }

    @Override
    public String helpDescription() {
        return "Join the channel you are in and create an audio recording";
    }

    @Override
    public HelpSection section() {
        return HelpSection.RECORDING;
    }

    @Override
    public boolean isStaffOnly() {
        return true;
    }

    @Override
    public void execute(List<String> arguments, MessageReceivedEvent event) {
        if (arguments.size() > 0) {
            CommandUtil.throwWrongNumberOfArguments();
        }

        User author = event.getAuthor();
        Member member = event.getGuild().retrieveMember(author).complete();
        VoiceChannel channel = member.getVoiceState().getChannel();
        if (channel == null) {
            throw new CommandException("❌ You are not in a voice channel");
        }

        try {
            recordingController.start(channel, event);
        } catch (AlreadyRecordingException e) {
            throw new CommandException("❌ Already recording");
        }
    }
}
