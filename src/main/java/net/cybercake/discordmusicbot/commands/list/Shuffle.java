package net.cybercake.discordmusicbot.commands.list;

import net.cybercake.discordmusicbot.commands.Command;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

public class Shuffle extends Command {

    public Shuffle() {
        super("shuffle", "Mixes up the current playlist into a random order.");
        this.aliases = new String[]{"randomize"};
    }

    @Override
    public void command(SlashCommandInteractionEvent event) {

    }
}
