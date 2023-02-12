package net.cybercake.discordmusicbot.commands;

import net.cybercake.discordmusicbot.Main;
import net.cybercake.discordmusicbot.generalutils.Log;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import org.reflections.Reflections;

import javax.annotation.Nullable;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public abstract class Command {

    private static final String COMMANDS_PACKAGE = "net.cybercake.discordmusicbot.commands.list";
    private static final List<Command> commands = new ArrayList<>();

    private static final List<CommandData> commandsData = new ArrayList<>();

    public static void registerAll() {
        for(Class<? extends Command> clazz : new Reflections(COMMANDS_PACKAGE).getSubTypesOf(Command.class)) {
            try {
                Command newInstance = clazz.getDeclaredConstructor().newInstance();
                Log.info("Registering command: /" + newInstance.getName() + " (class: " + clazz.getCanonicalName() + ")");
                commands.add(newInstance);
                commandsData.add(addNewCommand(newInstance.getName(), newInstance));
                if(newInstance.getAliases() != null) {
                    for(String alias : newInstance.getAliases())
                        commandsData.add(addNewCommand(alias, newInstance));
                }

            } catch (NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException noSuchMethodException) {
                Log.error("An error occurred whilst registering commands: " + noSuchMethodException, noSuchMethodException);
            }
        }
        Main.JDA.updateCommands().addCommands(
                commandsData
        ).queue();
    }

    private static SlashCommandData addNewCommand(String name, Command command) { // require name because aliases exist
        if(command.getOptionData() == null || Arrays.stream(command.getOptionData()).toList().contains(null))
            return Commands.slash(name, command.getDescription());
        return Commands.slash(name, command.getDescription())
                .addOptions(command.getOptionData());
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

    private final String name;
    private final String description;
    private final @Nullable String[] aliases;
    private final @Nullable OptionData[] optionData;

    public Command(String name, String description, @Nullable String[] aliases, @Nullable OptionData... optionData) {
        this.name = name;
        this.description = description;
        this.aliases = aliases;
        this.optionData = optionData;
    }

    public Command(String name, String description, @Nullable String... aliases) {
        this(name, description, aliases, (OptionData) null);
    }

    public Command(String name, String description, @Nullable OptionData... optionData) {
        this(name, description, null, optionData);
    }

    public Command(String name, String description) {
        this(name, description, null, (OptionData[]) null);
    }

    public String getName() { return this.name; }
    public String getDescription() { return this.description; }
    public @Nullable String[] getAliases() { return this.aliases; }
    public @Nullable OptionData[] getOptionData() { return this.optionData; }

    public abstract void command(SlashCommandInteractionEvent event);
}