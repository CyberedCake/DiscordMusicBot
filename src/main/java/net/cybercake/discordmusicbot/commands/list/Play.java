package net.cybercake.discordmusicbot.commands.list;

import net.cybercake.discordmusicbot.Embeds;
import net.cybercake.discordmusicbot.Main;
import net.cybercake.discordmusicbot.commands.Command;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

import java.util.Objects;

public class Play extends Command {

    public Play() {
        super(
                "play",
                "Type a URL or the name of a video to play music.",
                new OptionData(OptionType.STRING, "song", "The song URL or name of the song.", true)
        );
    }

    @Override
    public void command(SlashCommandInteractionEvent event) {
        Member member = event.getMember();
        User user = event.getUser();
        event.deferReply().queue();
        if(member == null) { // member doesn't exist for some reason
            event.getHook().editOriginalEmbeds(Embeds.getTechnicalErrorEmbed(null, "member == null").build()).queue(); return;
        }
        if(member.getVoiceState() == null || member.getVoiceState().getChannel() == null) { // they are not in a voice chat
            Embeds.throwError(event, user, "You must be in a voice channel to continue.", null); return;
        }

        if(!Main.queueManager.checkQueueExists(member.getGuild()))
            Main.queueManager.getGuildQueue(member.getGuild(), member.getVoiceState().getChannel().asVoiceChannel(), event.getChannel().asTextChannel());

        try {
            Main.queueManager.getGuildQueue(member.getGuild()).loadAndPlay(
                    event.getChannel().asTextChannel(), user, Objects.requireNonNull(event.getOption("song")).getAsString(), event
            );
        } catch (Exception exception) {
            Embeds.throwError(event, user, "A general error occurred whilst trying to add the song! `" + exception + "`", exception);
        }
    }
}
