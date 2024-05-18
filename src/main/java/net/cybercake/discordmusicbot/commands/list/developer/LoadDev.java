package net.cybercake.discordmusicbot.commands.list.developer;

import net.cybercake.discordmusicbot.commands.Command;
import net.cybercake.discordmusicbot.commands.list.user.Play;
import net.cybercake.discordmusicbot.utilities.Embeds;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;

public class LoadDev extends Command {

    public LoadDev() {
        super("load-dev", "Loads developer things. If you see this, please report it.");
        this.permission = DefaultMemberPermissions.enabledFor(Permission.ADMINISTRATOR);
        this.requireMaintenanceMode = true;
    }

    @Override
    public void command(SlashCommandInteractionEvent event) {
        try {
            Command.getCommandClass(Play.class).handlePlay(event, "https://music.youtube.com/playlist?list=PLQFF4PlpFPWVkThOxHDoyyrsuzTuzwwW9", false, true);
        } catch (Exception exception) {
            Embeds.throwError(event, event.getUser(), "Error:" + exception.getMessage(), true, null);
        }
    }
}
