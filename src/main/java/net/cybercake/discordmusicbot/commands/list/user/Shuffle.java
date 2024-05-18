package net.cybercake.discordmusicbot.commands.list.user;

import net.cybercake.discordmusicbot.PresetExceptions;
import net.cybercake.discordmusicbot.commands.Command;
import net.cybercake.discordmusicbot.constant.Colors;
import net.cybercake.discordmusicbot.queue.MusicPlayer;
import net.cybercake.discordmusicbot.queue.Queue;
import net.cybercake.discordmusicbot.utilities.Embeds;
import net.cybercake.discordmusicbot.utilities.Reply;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.interactions.callbacks.IReplyCallback;

public class Shuffle extends Command {

    public Shuffle() {
        super("shuffle", "Mixes up the current playlist into a random order.");
        this.aliases = new String[]{"randomize"};
        this.requireDjRole = true;
        this.registerButtonInteraction = true;
    }

    @Override
    public void command(SlashCommandInteractionEvent event) {
        handleShuffle(event);
    }

    @Override
    public void button(ButtonInteractionEvent event, String buttonId) {
        if(!buttonId.equalsIgnoreCase("shuffle-queue")) return;

        handleShuffle(event);
    }

    public void handleShuffle(IReplyCallback event) {
        if(PresetExceptions.memberNull(event)) return;
        assert event.getMember() != null;

        MusicPlayer musicPlayer = PresetExceptions.trackIsNotPlaying(event, event.getMember(), true);
        if(musicPlayer == null) return;

        if(PresetExceptions.isNotInVoiceChat(event, event.getMember(), musicPlayer.getVoiceChannel())) return;

        if(musicPlayer.getTrackScheduler().getQueue().getLiteralQueue().isEmpty()) {
            Embeds.throwError(event, event.getUser(), "There aren't any items to shuffle.", true, null); return;
        }

        Queue queue = musicPlayer.getTrackScheduler().getQueue();

        musicPlayer.getTrackScheduler().shuffle();
        Reply.standardEmbed(event, Colors.OTF_SETTINGS,
                event.getUser().getAsMention() + " shuffled the queue. " + Emoji.fromFormatted("✅").getFormatted() + "\nThe next song is now `" + queue.getTrackAt(queue.getCurrentIndex()).getInfo().title + "`");
    }
}
