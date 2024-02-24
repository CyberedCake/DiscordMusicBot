package net.cybercake.discordmusicbot.commands.list;

import net.cybercake.discordmusicbot.commands.Command;
import net.cybercake.discordmusicbot.constant.Colors;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

public class Help extends Command {

    public Help() {
        super(
                "help", "Contact information for the bot developer."
        );
        this.aliases = new String[]{"contact"};
    }

    @Override
    public void command(SlashCommandInteractionEvent event) {
        event.replyEmbeds(
                new EmbedBuilder()
                        .setTitle("Need help?")
                        .setDescription("Contact the bot developer, CyberedCake, with the following information:")
                        .addField("Discord", "cyberedcake (<@351410272256262145>)", true)
                        .addField("X (Twitter)", "[@CyberedCake](https://x.com/CyberedCake)", true)
                        .setColor(Colors.CONTACT.get())
                        .build()
        ).queue();
    }

}
