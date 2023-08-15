package net.cybercake.discordmusicbot.commands.settings;

import net.cybercake.discordmusicbot.commands.Command;
import net.cybercake.discordmusicbot.utilities.Embeds;
import net.cybercake.discordmusicbot.utilities.Log;
import net.cybercake.discordmusicbot.utilities.Pair;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.callbacks.IReplyCallback;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import org.reflections.Reflections;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class SettingsManager extends Command {

    private static final String SETTINGS_COMMAND_PATH = "net.cybercake.discordmusicbot.commands.settings.list";

    public Map<SubcommandData, SettingSubCommand> registerSettings() {
        Map<SubcommandData, SettingSubCommand> returned = new HashMap<>();
        try {
            for(Class<? extends SettingSubCommand> clazz : new Reflections(SETTINGS_COMMAND_PATH).getSubTypesOf(SettingSubCommand.class)) {
                SettingSubCommand subCommand = clazz.getDeclaredConstructor().newInstance();
                Log.info("Registering /settings sub-command: " + subCommand.getName() + " (class: " + clazz.getCanonicalName() + ")");

                SubcommandData data = new SubcommandData(subCommand.getName(), subCommand.getDescription());
                if(subCommand.optionData != null && Arrays.stream(subCommand.optionData).noneMatch(Objects::isNull))
                    data = data.addOptions(subCommand.optionData);

                returned.put(data, subCommand);
            }
        } catch (InvocationTargetException | InstantiationException | IllegalAccessException | NoSuchMethodException exception) {
            Log.error("An error occurred whilst registering subcommands: " + exception, exception);
        }
        return returned;
    }

    private final Map<SubcommandData, SettingSubCommand> subCommands;

    public SettingsManager() {
        super("settings", "Change settings relating to the music bot.");
        this.subCommands = new HashMap<>(registerSettings());
        this.permission = DefaultMemberPermissions.enabledFor( // need these permissions to execute
                Permission.MANAGE_SERVER, Permission.ADMINISTRATOR
        );
        super.subCommands = subCommands.keySet().toArray(SubcommandData[]::new);
    }

    private Pair<SubcommandData, SettingSubCommand> find(String subCommand, IReplyCallback event) {
        Pair<SubcommandData, SettingSubCommand> data = subCommands
                .entrySet()
                .stream()
                .filter(entry -> entry.getKey().getName().equalsIgnoreCase(subCommand))
                .map(entry -> new Pair<>(entry.getKey(), entry.getValue()))
                .findFirst()
                .orElse(null);
        if(data == null) { Embeds.throwError(event, event.getUser(), "Data null (no settings sub-command found)", true, null); return null; }
        if(data.getFirstItem() == null || data.getSecondItem() == null) { Embeds.throwError(event, event.getUser(), "First item is null || second item is null", true, null); return null; }
        return data;
    }

    @Override
    public void command(SlashCommandInteractionEvent event) {
        String subCommand = event.getSubcommandName();
        Pair<SubcommandData, SettingSubCommand> data = find(subCommand, event);
        if(data == null) return;
        data.getSecondItem().command(event);
    }

    @Override
    public void tab(CommandAutoCompleteInteractionEvent event) {
        String subCommand = event.getSubcommandName();
        Pair<SubcommandData, SettingSubCommand> data = find(subCommand, (IReplyCallback) event);
        if(data == null) return;
        try {
            data.getSecondItem().tab(event);
        } catch (UnsupportedOperationException ignored) { /* means there was no value set, that is okay */ }
    }
}
