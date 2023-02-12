package net.cybercake.discordmusicbot.commands;

import net.cybercake.discordmusicbot.Main;
import net.cybercake.discordmusicbot.generalutils.Log;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import org.reflections.Reflections;

import javax.annotation.Nullable;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public abstract class Command {

    private static final String COMMANDS_PACKAGE = "net.cybercake.discordmusicbot.commands.list";
    private static final List<Command> commands = new ArrayList<>();

    private static final List<CommandData> commandsData = new ArrayList<>();

    public static void registerAll() {
        for(Class<? extends Command> clazz : new Reflections(COMMANDS_PACKAGE).getSubTypesOf(Command.class)) {
            try {
                Command newInstance = clazz.getDeclaredConstructor().newInstance();
                commands.add(newInstance);
                commandsData.add(
                        Commands.slash(newInstance.getName(), newInstance.getDescription())
                                .addOptions(newInstance.getOptionData())
                );
                Log.info("Registered new command: /" + newInstance.getName() + " (class: " + clazz.getCanonicalName() + ")");

            } catch (NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException noSuchMethodException) {
                Log.error("An error occurred whilst registering commands: " + noSuchMethodException, noSuchMethodException);
            }
        }
        Main.JDA.updateCommands().addCommands(
                commandsData
        ).queue();
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
                        return command.getName().equalsIgnoreCase(name);
                    else
                        return command.getName().equals(name);
                })
                .findFirst()
                .orElse(null);
    }

    private final String name;
    private final String description;
    private final @Nullable String[] aliases;
    private final OptionData[] optionData;

    public Command(String name, String description, @Nullable String[] aliases, @Nullable OptionData... optionData) {
        this.name = name;
        this.description = description;
        this.aliases = aliases;
        this.optionData = (optionData == null ? new OptionData[]{} : optionData);
    }

    public Command(String name, String description, @Nullable String... aliases) {
        this(name, description, aliases, (OptionData) null);
    }

    public Command(String name, String description, @Nullable OptionData... optionData) {
        this(name, description, null, optionData);
    }

    public String getName() { return this.name; }
    public String getDescription() { return this.description; }
    public @Nullable String[] getAliases() { return this.aliases; }
    public OptionData[] getOptionData() { return this.optionData; }

    public abstract void command(SlashCommandInteractionEvent event);
}