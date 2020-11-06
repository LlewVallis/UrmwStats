package org.astropeci.urmwstats.command.commands.recording;

import lombok.RequiredArgsConstructor;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.requests.restaction.MessageAction;
import org.astropeci.urmwstats.command.Command;
import org.astropeci.urmwstats.command.CommandException;
import org.astropeci.urmwstats.command.CommandUtil;
import org.astropeci.urmwstats.command.HelpSection;
import org.astropeci.urmwstats.recording.NotRecordingException;
import org.astropeci.urmwstats.recording.RecordingController;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class SaveRecordingCommand implements Command {

    private final RecordingController recordingController;

    @Override
    public String label() {
        return "save-recording";
    }

    @Override
    public String usage() {
        return "save-recording";
    }

    @Override
    public String helpDescription() {
        return "Stop recording and upload the recorded audio file";
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
        if (arguments.size() != 0) {
            CommandUtil.throwWrongNumberOfArguments();
        }

        try {
            recordingController.save(event);
        } catch (NotRecordingException e) {
            throw new CommandException("❌ Not currently recording");
        }
}
}
