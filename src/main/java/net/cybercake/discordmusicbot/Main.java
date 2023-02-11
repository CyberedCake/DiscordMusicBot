package net.cybercake.discordmusicbot;

import net.cybercake.discordmusicbot.commands.Command;
import net.cybercake.discordmusicbot.commands.CommandManager;
import net.cybercake.discordmusicbot.generalutils.Log;
import net.cybercake.discordmusicbot.queue.QueueManager;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.requests.GatewayIntent;

public class Main {

    public static final String TOKEN = System.getenv("TOKEN");

    public static final long MUSIC_CHANNEL_ID = 1061402397378887761L;
    public static final long GUILD_ID = 1060022039311818752L;

    public static JDA JDA;
    public static Guild guild;
    public static QueueManager queueManager;

    public static void main(String[] args) throws InterruptedException {
        long mss = System.currentTimeMillis();

        Log.info("Checking token existence...");
        if(TOKEN == null) throw new RuntimeException("Cannot find token value on computer as env variable 'TOKEN'");

        Log.info("Building JDA...");
        JDA = JDABuilder.createDefault(TOKEN)
                .setActivity(Activity.watching("Cyberspace"))
                .setStatus(OnlineStatus.ONLINE)
                .setEnabledIntents(GatewayIntent.MESSAGE_CONTENT, GatewayIntent.values())
                .addEventListeners(new CommandManager())
                .build()
                .awaitReady();

        Log.info("Setting needed variables...");
        guild = JDA.getGuildById(GUILD_ID);
        queueManager = new QueueManager();

        Log.info("Registering commands...");
        Command.registerAll();

        Log.info("Loaded the bot in " + (System.currentTimeMillis()-mss) + "ms!");
    }


}