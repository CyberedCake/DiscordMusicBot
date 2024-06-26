package net.cybercake.discordmusicbot.commands.settings;

import net.cybercake.discordmusicbot.GuildSettings;
import net.cybercake.discordmusicbot.constant.Colors;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.interactions.callbacks.IReplyCallback;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.Date;
import java.util.function.BiFunction;

public enum ShowSetting {
    DJ_ROLE("DJ Role",
            ((event, settings) -> "<@&" + settings.settings.djRole + ">"),
            "Everyone");

//    CHANNELS_ALLOWED("Allowed Channels",
//            ((event, settings) -> String.join(", ",
//                    settings.settings.channels.allowed.stream()
//                            .map(channel -> "<#" + channel + ">")
//                            .toList()
//            )),
//            "All channels"),
//
//    CHANNELS_DISALLOWED("Disallowed Channels",
//            ((event, settings) -> String.join(", ",
//                    settings.settings.channels.disallowed.stream()
//                            .map(channel -> "<#" + channel + ">")
//                            .toList()
//            )),
//            "No channels"
//            );

    public static MessageEmbed getEmbed(IReplyCallback event, GuildSettings settings, @Nullable ShowSetting... filter) {
        EmbedBuilder builder = new EmbedBuilder()
                .setColor(Colors.LIST.get())
                .setTitle("__Settings for your server:__")
                .setTimestamp(new Date().toInstant());
        Guild guild = event.getGuild();
        if(guild != null)
            builder = builder.setFooter("Guild: " + guild.getName() + " • ID: " + guild.getId());

        boolean filtersExist = filter != null && filter.length > 0;
        if(filtersExist) builder = builder.setAuthor("Filtered for " + String.join(", ", Arrays.stream(filter)
                .map((setting) -> "'" + setting.getDisplayName() + "'").toList()
        ));
        for(ShowSetting setting : ShowSetting.values()) {
            if(filtersExist && Arrays.stream(filter).noneMatch(showSetting -> setting == showSetting))
                continue;
            String settingValue = setting.reply.apply(event, settings);
            settingValue = settingValue.contains("null") ? setting.getValueIfNoneIsSet() : settingValue;
            builder = builder.addField(setting.getDisplayName(), settingValue, true);
        }
        return builder.build();
    }

    private final String displayName;
    private final BiFunction<IReplyCallback, GuildSettings, String> reply;
    private final String ifNone;

    ShowSetting(String displayName, BiFunction<IReplyCallback, GuildSettings, String> reply, String ifNone) {
        this.displayName = displayName;
        this.reply = reply;
        this.ifNone = ifNone;
    }

    public String getDisplayName() { return this.displayName; }
    public String getValueIfNoneIsSet() { return this.ifNone; }

}
