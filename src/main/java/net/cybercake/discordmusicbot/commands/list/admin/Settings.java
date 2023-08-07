package net.cybercake.discordmusicbot.commands.list.admin;

import net.cybercake.discordmusicbot.commands.Command;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

public class Settings extends Command {

    public Settings() {
        super("settings", "Modifies the settings for this server.");
        this.permission = DefaultMemberPermissions.enabledFor( // need these permissions to execute
                Permission.MANAGE_CHANNEL, Permission.MANAGE_SERVER, Permission.ADMINISTRATOR, Permission.VOICE_MOVE_OTHERS
        );
//        this.optionData = new OptionData[]{
//                new OptionData(OptionType.ROLE, "dj", "Sets the DJ role")
//        };
    }

    @Override
    public void command(SlashCommandInteractionEvent event) {
        event.reply("Command coming soon:tm:").setEphemeral(true).queue();
    }
}
