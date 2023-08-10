package net.cybercake.discordmusicbot.commands;

import net.cybercake.discordmusicbot.Embeds;
import net.cybercake.discordmusicbot.GuildSettings;
import net.cybercake.discordmusicbot.Main;
import net.cybercake.discordmusicbot.utilities.Log;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.ISnowflake;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.interactions.callbacks.IReplyCallback;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.build.*;
import org.reflections.Reflections;

import javax.annotation.Nullable;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.concurrent.TimeUnit;

public abstract class Command {

    private static final String COMMANDS_PACKAGE = "net.cybercake.discordmusicbot.commands";
    private static final List<Command> commands = new ArrayList<>();

    private static final List<CommandData> commandsData = new ArrayList<>();

    public static void register() {
        for(Class<? extends Command> clazz : new Reflections(COMMANDS_PACKAGE).getSubTypesOf(Command.class)) {
            try {
                @Nullable Constructor<?> constructor = Arrays.stream(clazz.getDeclaredConstructors()).filter(constructor0 -> constructor0.getParameterCount() == 0).findFirst().orElse(null);
                if(constructor == null)
                    throw new NullPointerException("constructor == null: " + clazz.getCanonicalName() + ": " + null);
                Command newInstance = (Command) constructor.newInstance();
                Log.info("Registering command: /" + newInstance.getName() + " (class: " + clazz.getCanonicalName() + ")");
                commands.add(newInstance);
                commandsData.add(addNewCommand(newInstance.getName(), newInstance));
                if(newInstance.getAliases() != null) {
                    for(String alias : newInstance.getAliases())
                        commandsData.add(addNewCommand(alias, newInstance));
                }
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException noSuchMethodException) {
                Log.error("An error occurred whilst registering commands: " + noSuchMethodException, noSuchMethodException);
            }
        }
        Main.JDA.updateCommands().addCommands(
                commandsData
        ).queueAfter(1, TimeUnit.SECONDS);
    }

    private static SlashCommandData addNewCommand(String name, Command command) { // require name because aliases exist
        SlashCommandData commandData = Commands.slash(name, command.getDescription());
        if(command.getSubCommands() != null)
            commandData = commandData.addSubcommands(command.getSubCommands());
        if(command.getOptionData() != null && Arrays.stream(command.getOptionData()).noneMatch(Objects::isNull))
            commandData = commandData.addOptions(command.getOptionData());
        if(command.getPermission() != null)
            commandData = commandData.setDefaultPermissions(command.getPermission());
        return commandData;
    }

    public static List<Command> getCommands() { return commands; }
    public static @Nullable Command getCommandClass(Class<? extends Command> clazz) {
        return getCommands()
                .stream()
                .filter(command -> command.getClass().getCanonicalName().equalsIgnoreCase(clazz.getCanonicalName()))
                .findFirst()
                .orElse(null);
    }
    public static @Nullable Command getCommandFromName(String name, boolean ignoreCase) {
        return getCommands()
                .stream()
                .filter(command -> {
                    if(ignoreCase)
                        return command.getName().equalsIgnoreCase(name) || (command.getAliases() != null && Arrays.stream(command.getAliases()).map(String::toLowerCase).toList().contains(name.toLowerCase(Locale.ROOT)));
                    else
                        return command.getName().equals(name) || (command.getAliases() != null && Arrays.stream(command.getAliases()).toList().contains(name));
                })
                .findFirst()
                .orElse(null);
    }

    public static boolean hasDjRole(Member member) {
        Guild guild = member.getGuild();
        GuildSettings settings = GuildSettings.get(guild.getIdLong(), false);
        if(settings == null) return true;
        long roleId = settings.settings.djRole;
//        if(guild.getRoleById(roleId).getName().equalsIgnoreCase("everyone")) {
//            Log.warn("is everyone");
//            return true;
//        }
        return member.getRoles().stream().map(ISnowflake::getIdLong).anyMatch(standardRole -> roleId == standardRole);
    }

    public static boolean requireDjRole(IReplyCallback event, Member member) {
        if(!hasDjRole(member)) {
            GuildSettings settings = GuildSettings.get(member.getGuild().getIdLong(), false);
            String additional = (settings == null ? "." : ": <@&" + settings.settings.djRole + ">");
            Embeds.throwError(event, member.getUser(), "You can't use this command without the DJ role" + additional, true, null);
            return true;
        }
        return false;
    }

    private final String name;
    private final String description;

    protected @Nullable String[] aliases;
    protected @Nullable OptionData[] optionData;
    protected @Nullable SubcommandData[] subCommands;
    protected @Nullable DefaultMemberPermissions permission;

    protected boolean registerButtonInteraction;
    protected boolean requireDjRole;

    public Command(@Nullable String name, @Nullable String description) {
        this.name = name;
        this.description = description;
        this.registerButtonInteraction = false;
        this.requireDjRole = false;
    }

    public String getName() { return this.name; }
    public String getDescription() { return this.description; }
    public @Nullable String[] getAliases() { return this.aliases; }
    public @Nullable OptionData[] getOptionData() { return this.optionData; }
    public @Nullable SubcommandData[] getSubCommands() { return this.subCommands; }
    public @Nullable DefaultMemberPermissions getPermission() { return this.permission; }
    public boolean sendButtonInteractionEvent() { return this.registerButtonInteraction; }
    public boolean requiresDjRole() { return this.requireDjRole; }

    public abstract void command(SlashCommandInteractionEvent event);
    public void tab(CommandAutoCompleteInteractionEvent event) { throw new UnsupportedOperationException("Not implemented"); }
    public void button(ButtonInteractionEvent event, String buttonId) { throw new UnsupportedOperationException("Not implemented"); }
}