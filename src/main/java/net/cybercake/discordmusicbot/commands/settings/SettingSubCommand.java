package net.cybercake.discordmusicbot.commands.settings;

import net.cybercake.discordmusicbot.GuildSettings;
import net.cybercake.discordmusicbot.constant.Colors;
import net.cybercake.discordmusicbot.utilities.Embeds;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.callbacks.IReplyCallback;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

import javax.annotation.Nullable;
import java.util.Date;

public abstract class SettingSubCommand {

    private final String name;
    private final String description;

    @Nullable protected OptionData[] optionData;

    public SettingSubCommand(String name, String description) {
        this.name = name;
        this.description = description;
    }

    public static GuildSettings doesExist_elseCreate(Guild guild) {
        GuildSettings settings = GuildSettings.get(guild.getIdLong(), false);
        if(settings == null)
            settings = GuildSettings.create(guild.getIdLong(), guild.getName());
        return settings;
    }

    public GuildSettings forEvent(IReplyCallback event) {
        if(!event.isAcknowledged())
            event.deferReply(true).queue();
        if(event.getGuild() == null) {
            Embeds.throwError(event, event.getUser(), "You must be in a server to use this command!", true, null); return null;
        }
        return doesExist_elseCreate(event.getGuild());
    }

    public void showSuccessEmbed(IReplyCallback event, ShowSetting setting, String oldValue, String newValue) {
        oldValue = oldValue.contains("null") ? setting.getValueIfNoneIsSet() : oldValue;
        newValue = newValue.contains("null") ? setting.getValueIfNoneIsSet() : newValue;
        MessageEmbed embed = new EmbedBuilder()
                .setColor(Colors.SUCCESS.get())
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
