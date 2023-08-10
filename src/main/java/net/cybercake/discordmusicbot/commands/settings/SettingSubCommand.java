package net.cybercake.discordmusicbot.commands.settings;

import net.cybercake.discordmusicbot.Embeds;
import net.cybercake.discordmusicbot.GuildSettings;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.callbacks.IReplyCallback;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

import javax.annotation.Nullable;
import java.awt.*;
import java.util.Date;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public abstract class SettingSubCommand {

    private final String name;
    private final String description;

    @Nullable protected OptionData[] optionData;

    public SettingSubCommand(String name, String description) {
        this.name = name;
        this.description = description;
    }

    public GuildSettings doesExist_elseCreate(IReplyCallback event) {
        @Nullable Guild guild = event.getGuild();
        if(guild == null) {
            Embeds.throwError(event, event.getUser(), "You must be in a server to use this command", true, null); return null;
        }
        GuildSettings settings = GuildSettings.get(guild.getIdLong(), false);
        if(settings == null)
            settings = GuildSettings.create(guild.getIdLong(), guild.getName());
        return settings;
    }

    public void showSuccessEmbed(IReplyCallback event, ShowSetting setting, String oldValue, String newValue) {
        oldValue = oldValue.contains("null") ? setting.getValueIfNoneIsSet() : oldValue;
        newValue = newValue.contains("null") ? setting.getValueIfNoneIsSet() : newValue;
        MessageEmbed embed = new EmbedBuilder()
                .setColor(new Color(100, 223, 0))
                .setTitle("__Changed Value: `" + setting.getDisplayName() + "`__")
                .setTimestamp(new Date().toInstant())
                .addField("Old Value", oldValue, true)
                .addField("New Value", newValue, true)
                .build();
        if(event.isAcknowledged()) event.getHook().editOriginalEmbeds(embed).queue();
        else event.replyEmbeds(embed).queue();
    }


    public @Nullable OptionData[] getOptions() { return this.optionData; }

    public String getName() { return this.name; }
    public String getDescription() { return this.description; }

    public abstract void command(SlashCommandInteractionEvent event);
    public void tab(CommandAutoCompleteInteractionEvent event) { throw new UnsupportedOperationException("Not implemented"); }

}
