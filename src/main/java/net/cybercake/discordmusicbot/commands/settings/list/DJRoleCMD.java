package net.cybercake.discordmusicbot.commands.settings.list;

import net.cybercake.discordmusicbot.GuildSettings;
import net.cybercake.discordmusicbot.commands.settings.SettingSubCommand;
import net.cybercake.discordmusicbot.commands.settings.ShowSetting;
import net.cybercake.discordmusicbot.utilities.Log;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

import java.awt.*;
import java.util.Date;

public class DJRoleCMD extends SettingSubCommand {

    public DJRoleCMD() {
        super("djrole", "Sets the DJ role that only users under this role can access music commands.");
        this.optionData = new OptionData[]{
                new OptionData(OptionType.ROLE, "role", "Only users with this role can access music commands.", false)
        };
    }

    @Override
    public void command(SlashCommandInteractionEvent event) {
        event.deferReply(true).queue();
        GuildSettings settings = doesExist_elseCreate(event);
        if(settings == null) return;
        if(event.getGuild() == null) return;

        OptionMapping optionMapping = event.getOption("role");
        if(optionMapping == null) {
            event.getHook().editOriginalEmbeds(ShowSetting.getEmbed(event, settings, ShowSetting.DJ_ROLE)).queue(); return;
        }

        Role role = optionMapping.getAsRole();
        String newValue = role.getId();
        if(role.getName().equalsIgnoreCase("@everyone"))
            newValue = null;

        String oldValue = String.valueOf(settings.settings.djRole);
        settings.settings.djRole = newValue == null ? null : Long.parseLong(newValue);
        settings.expireAndReload(event.getGuild());
        showSuccessEmbed(event, ShowSetting.DJ_ROLE, "<@&" + oldValue + ">", "<@&" + newValue +">");
    }
}
