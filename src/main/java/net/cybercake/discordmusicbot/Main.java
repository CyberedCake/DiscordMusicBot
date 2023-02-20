package net.cybercake.discordmusicbot;

import com.jagrosh.jlyrics.LyricsClient;
import net.cybercake.discordmusicbot.commands.Command;
import net.cybercake.discordmusicbot.commands.CommandManager;
import net.cybercake.discordmusicbot.generalutils.Log;
import net.cybercake.discordmusicbot.listeners.BotDisconnectEvent;
import net.cybercake.discordmusicbot.listeners.ButtonInteraction;
import net.cybercake.discordmusicbot.queue.QueueManager;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.managers.AudioManager;
import net.dv8tion.jda.api.requests.GatewayIntent;

public class Main {

    //public static String TOKEN = System.getenv("TOKEN");
    public static String TOKEN = null;
    //public static String SPOTIFY_TOKEN = System.getenv("SPOTIFY_SECRET");
    public static String SPOTIFY_TOKEN = null;
    public static String SPOTIFY_CLIENT = "88b94b49a4af45b0bf80249d7f08479f";

    public static final float SKIP_VOTE_PERCENTAGE = 0.5F;

    public static JDA JDA;
    public static QueueManager queueManager;
    public static LyricsClient lyricsClient;

    public static void main(String[] args) throws InterruptedException {
        boolean nextToken = false;
        boolean nextSpotify = true;
        for(String str : args) {
            if(nextToken) { TOKEN = str; nextToken = false;}
            if(nextSpotify) { SPOTIFY_TOKEN = str; nextSpotify = false; }

            if(str.equals("--token")) {
                nextToken = true;
            }else if(str.equals("--spotify")) {
                nextSpotify = true;
            }
        }

        long mss = System.currentTimeMillis();

        Log.info("Checking token existence... ");
        if(TOKEN == null) throw new RuntimeException("Cannot find token value on computer as env variable 'TOKEN'");

        Log.info("Building JDA...");
        JDA = JDABuilder.createDefault(TOKEN)
                .setActivity(Activity.watching("Cyberspace"))
                .setStatus(OnlineStatus.ONLINE)
                .setEnabledIntents(GatewayIntent.MESSAGE_CONTENT, GatewayIntent.values())
                .setEventPassthrough(true)
                .addEventListeners(new CommandManager(), new BotDisconnectEvent(), new ButtonInteraction())
                .build()
                .awaitReady();

        Log.info("Setting needed variables...");
        queueManager = new QueueManager();
        lyricsClient = new LyricsClient();

        Log.info("Cleaning up from last boot...");
        JDA.getAudioManagers().forEach(AudioManager::closeAudioConnection);

        Log.info("Registering commands...");
        Command.registerAll();

        Log.info("Loaded the bot in " + (System.currentTimeMillis()-mss) + "ms!");
    }


}