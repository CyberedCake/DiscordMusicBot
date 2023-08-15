package net.cybercake.discordmusicbot.commands.settings.list;

import net.cybercake.discordmusicbot.GuildSettings;
import net.cybercake.discordmusicbot.commands.settings.SettingSubCommand;
import net.cybercake.discordmusicbot.commands.settings.ShowSetting;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

public class ListSettingsCMD extends SettingSubCommand {

    public ListSettingsCMD() {
        super("list", "Lists the settings currently set.");
    }

    @Override
    public void command(SlashCommandInteractionEvent event) {
        GuildSettings settings = forEvent(event);
        if(settings == null) return;
        if(event.getGuild() == null) return;

        event.getHook().editOriginalEmbeds(ShowSetting.getEmbed(event, settings, (ShowSetting[]) null)).queue();
    }
}
