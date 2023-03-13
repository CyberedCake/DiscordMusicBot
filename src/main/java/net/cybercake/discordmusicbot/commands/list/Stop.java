package net.cybercake.discordmusicbot.commands.list;

import net.cybercake.discordmusicbot.Embeds;
import net.cybercake.discordmusicbot.PresetExceptions;
import net.cybercake.discordmusicbot.commands.Command;
import net.cybercake.discordmusicbot.queue.Queue;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

import java.awt.*;
import java.util.Date;

public class Stop extends Command {

    public Stop() {
        super(
                "stop", "Removes the bot from the voice chat."
        );
        this.aliases = new String[]{"disconnect", "leave", "remove", "goodbye"};
    }

    @Override
    public void command(SlashCommandInteractionEvent event) {
        if(PresetExceptions.memberNull(event)) return;
        Member member = event.getMember();
        assert member != null;

        Queue queue = PresetExceptions.trackIsNotPlaying(event, event.getMember(), true);
        if(queue == null) return;

        if(member.getVoiceState() == null || member.getVoiceState().getChannel() == null || !member.getVoiceState().getChannel().asVoiceChannel().equals(queue.getVoiceChannel())) {
            Embeds.throwError(event, member.getUser(), "You must be in the voice chat to skip a song", true, null); return;
        }

        event.deferReply().queue();
        queue.destroy();

        EmbedBuilder builder = new EmbedBuilder();
        builder.setTitle("Bot disconnected.");
        builder.setDescription(event.getMember().getAsMention() + " cleared the queue and disconnected the bot from the voice chat.");
        builder.setColor(new Color(255, 152, 68));
        builder.setTimestamp(new Date().toInstant());
        event.getHook().editOriginalEmbeds(builder.build()).queue();
    }
}
