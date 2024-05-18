package net.cybercake.discordmusicbot.commands.list.user;

import net.cybercake.discordmusicbot.PresetExceptions;
import net.cybercake.discordmusicbot.commands.Command;
import net.cybercake.discordmusicbot.constant.Colors;
import net.cybercake.discordmusicbot.queue.MusicPlayer;
import net.cybercake.discordmusicbot.utilities.Embeds;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.interactions.callbacks.IReplyCallback;

import java.util.Date;

public class Stop extends Command {

    public Stop() {
        super(
                "stop", "Removes the bot from the voice chat."
        );
        this.aliases = new String[]{"disconnect", "leave", "goodbye"};
        this.requireDjRole = true;
        this.registerButtonInteraction = true;
    }

    @Override
    public void command(SlashCommandInteractionEvent event) {
        handleStopQueue(event);
    }

    @Override
    public void button(ButtonInteractionEvent event, String buttonId) {
        if(!buttonId.equalsIgnoreCase("stop-queue")) return;
        handleStopQueue(event);
    }

    public void handleStopQueue(IReplyCallback event) {
        if(PresetExceptions.memberNull(event)) return;
        Member member = event.getMember();
        assert member != null;

        MusicPlayer musicPlayer = PresetExceptions.trackIsNotPlaying(event, event.getMember(), true);
        if(musicPlayer == null) return;

        if(member.getVoiceState() == null || member.getVoiceState().getChannel() == null || !member.getVoiceState().getChannel().equals(musicPlayer.getVoiceChannel())) {
            Embeds.throwError(event, member.getUser(), "You must be in the voice chat to skip a song", true, null); return;
        }

        event.deferReply().queue();
        musicPlayer.destroy();

        EmbedBuilder builder = new EmbedBuilder();
        builder.setTitle("Bot disconnected.");
        builder.setDescription(event.getMember().getAsMention() + " cleared the queue and disconnected the bot from the voice chat.");
        builder.setColor(Colors.DISCONNECTED.get());
        builder.setTimestamp(new Date().toInstant());
        event.getHook().editOriginalEmbeds(builder.build()).queue();
    }
}
