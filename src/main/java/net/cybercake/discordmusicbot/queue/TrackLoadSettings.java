package net.cybercake.discordmusicbot.queue;

import net.cybercake.discordmusicbot.commands.Command;
import net.cybercake.discordmusicbot.utilities.Log;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

import javax.annotation.Nullable;
import java.net.URL;
import java.util.function.Function;

public class TrackLoadSettings {

    public static Builder config(final User requestedBy, String trackUrl, final SlashCommandInteractionEvent event, Command command, boolean startNow, boolean shuffle) {
        return config().requestedBy(requestedBy).trackUrl(trackUrl).parentEvent(event).parentCommand(command).startNow(startNow).shuffle(shuffle);
    }

    public static Builder config(final User requestedBy, Message.Attachment file, final SlashCommandInteractionEvent event, Command command, boolean startNow, boolean shuffle) {
        return config().requestedBy(requestedBy).file(file).parentEvent(event).parentCommand(command).startNow(startNow).shuffle(shuffle);
    }

    public static Builder config() {
        return new Builder();
    }

    public static class Builder {

        User requestedBy;
        String trackUrl;
        @Nullable Message.Attachment file;
        SlashCommandInteractionEvent parentEvent;
        Command parentCommand;
        boolean startNow;
        boolean shuffle;

        Builder() { }

        public Builder requestedBy(User requestedBy) { this.requestedBy = requestedBy; return this; }
        public Builder trackUrl(String trackUrl) { this.trackUrl = trackUrl; return this; }
        public Builder file(@Nullable Message.Attachment file) { this.file = file; return this; }
        public Builder parentEvent(SlashCommandInteractionEvent parentEvent) { this.parentEvent = parentEvent; return this; }
        public Builder parentCommand(Command parentCommand) { this.parentCommand = parentCommand; return this; }
        public Builder startNow(boolean startNow) { this.startNow = startNow; return this; }
        public Builder shuffle(boolean shuffle) { this.shuffle = shuffle; return this; }

        public TrackLoadSettings build() {
            return new TrackLoadSettings(this);
        }
    }

    private Builder builder;

    TrackLoadSettings(Builder builder) {
        this.builder = builder;
    }

    public User getRequestedBy() { return this.builder.requestedBy; }
    public String getTrackUrl() { return this.builder.trackUrl; }
    public @Nullable Message.Attachment getFile() { return this.builder.file; }
    public SlashCommandInteractionEvent getParentEvent() { return this.builder.parentEvent; }
    public Command getParentCommand() { return this.builder.parentCommand; }
    public boolean shouldStartNow() { return this.builder.startNow; }
    public boolean shouldShuffle() { return this.builder.shuffle; }

    public boolean isSpotify() {
        try {
            return !(new URL(this.builder.trackUrl).getHost().contains("spotify.com"));
        } catch (Exception exception) {
            Log.warn("Found error when parsing URL (" + this.builder.trackUrl + "): " + exception);
            return false;
        }
    }

    void change(Function<Builder, Builder> builder) {
        this.builder = builder.apply(this.builder);
    }


}
