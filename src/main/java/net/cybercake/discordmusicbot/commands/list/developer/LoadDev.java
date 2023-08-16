package net.cybercake.discordmusicbot.commands.list.developer;

import net.cybercake.discordmusicbot.Main;
import net.cybercake.discordmusicbot.commands.Command;
import net.cybercake.discordmusicbot.commands.list.Play;
import net.cybercake.discordmusicbot.utilities.Embeds;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.unions.AudioChannelUnion;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;

public class LoadDev extends Command {

    public LoadDev() {
        super("load-dev", "Loads developer things. If you see this, please report it.");
        this.permission = DefaultMemberPermissions.enabledFor(Permission.ADMINISTRATOR);
        this.requireMaintenanceMode = true;
    }

    @Override
    public void command(SlashCommandInteractionEvent event) {
        try {
            Command.getCommandClass(Play.class).handlePlay(event, "https://www.youtube.com/watch?v=X-nwU7fvUp4&list=PLQFF4PlpFPWUOAUyHHYrcI6PgP_5LBU19&index=1&pp=gAQBiAQB8AUB", false, true);
        } catch (Exception exception) {
            Embeds.throwError(event, event.getUser(), "Error:" + exception.getMessage(), true, null);
        }
    }
}
