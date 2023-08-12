package net.cybercake.discordmusicbot;

import com.google.gson.Gson;
import com.jagrosh.jlyrics.LyricsClient;
import com.wrapper.spotify.SpotifyApi;
import com.wrapper.spotify.SpotifyHttpManager;
import com.wrapper.spotify.model_objects.credentials.AuthorizationCodeCredentials;
import net.cybercake.discordmusicbot.commands.Command;
import net.cybercake.discordmusicbot.commands.CommandManager;
import net.cybercake.discordmusicbot.utilities.Log;
import net.cybercake.discordmusicbot.listeners.BotDisconnectEvent;
import net.cybercake.discordmusicbot.listeners.ButtonInteraction;
import net.cybercake.discordmusicbot.queue.QueueManager;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.managers.AudioManager;
import net.dv8tion.jda.api.requests.GatewayIntent;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class Main {

    public static String TOKEN = null;
    public static String SPOTIFY_TOKEN = null;
    public static String SPOTIFY_CLIENT = "88b94b49a4af45b0bf80249d7f08479f";

    public static final float SKIP_VOTE_PERCENTAGE = 0.5F;

    public static JDA JDA;
    public static QueueManager queueManager;
    public static LyricsClient lyricsClient;
    public static SpotifyApi spotifyApi;
    public static boolean MAINTENANCE = false;

    public static void main(String[] args) throws InterruptedException {
        long mss = System.currentTimeMillis();

        try {
            if(TOKEN == null) TOKEN = getExternalBotInfo("TOKEN");
            if(SPOTIFY_TOKEN == null) SPOTIFY_TOKEN = getExternalBotInfo("SPOTIFY_SECRET");
        } catch (Exception exception) {
            if(TOKEN == null && SPOTIFY_TOKEN == null) Log.error("Failed to get the bot token and spotify token from environmental variables! ", exception);
        }

        Log.info("Checking token existence... ");
        if(TOKEN == null) throw new RuntimeException("Cannot find token value on computer as env variable 'TOKEN'");


        if(getExternalBotInfo("MAINTENANCE") != null && getExternalBotInfo("MAINTENANCE").equalsIgnoreCase("TRUE")) {
            Log.warn("------------------------------------------------------");
            Log.warn("Environmental variable 'MAINTENANCE' set to 'TRUE'...");
            Log.warn("------------------------------------------------------");
            MAINTENANCE = true;
        }

        Log.info("Building JDA...");
        JDA = JDABuilder.createDefault(TOKEN)
                .setActivity(Activity.watching("Cyberspace"))
                .setStatus(OnlineStatus.ONLINE)
                .setEnabledIntents(GatewayIntent.MESSAGE_CONTENT, GatewayIntent.values())
                .setEventPassthrough(true)
                .addEventListeners(new CommandManager(), new BotDisconnectEvent(), new ButtonInteraction())
                .build()
                .awaitReady();
        if(MAINTENANCE) {
            JDA.getPresence().setStatus(OnlineStatus.DO_NOT_DISTURB);
            JDA.getPresence().setActivity(Activity.playing("MAINTENANCE MODE"));
        }

        Log.info("Setting needed variables...");
        try {
            queueManager = new QueueManager();
            lyricsClient = new LyricsClient();
            spotifyApi = new SpotifyApi.Builder()
                    .setClientId(SPOTIFY_CLIENT)
                    .setClientSecret(SPOTIFY_TOKEN)
                    .setRedirectUri(SpotifyHttpManager.makeUri("http://localhost:8888/authorized"))
                    .build();
            AuthorizationCodeCredentials credentials = spotifyApi.authorizationCode("").redirect_uri(spotifyApi.getRedirectURI()).build().execute();
            spotifyApi.setAccessToken(credentials.getAccessToken());
            spotifyApi.setRefreshToken(credentials.getRefreshToken());
        } catch (Exception exception) {
            Log.error("Something went wrong when setting up needed variables: " + exception);
        }

        Log.info("Loading JSON files...");
        try {
            GuildSettings settings = GuildSettings.create(0, "Default", true);
            Log.warn("Found: " + settings.toString());
        } catch (Exception exception) {
            throw new IllegalStateException("Cannot load JSON", exception);
        }

        Log.info("Cleaning up from last boot...");
        JDA.getAudioManagers().forEach(AudioManager::closeAudioConnection);

        Log.info("Registering commands...");
        Command.register();

        Log.info("Loaded the bot in " + (System.currentTimeMillis()-mss) + "ms!");
    }

//    private static YouTube getYouTubeService() {
//        try {
//            JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
//            final NetHttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
//            return new YouTube.Builder(httpTransport, jsonFactory, null)
//                    .setApplicationName(Main.class.getPackageName())
//                    .build();
//        } catch (Exception exception) {
//            throw new IllegalStateException("Failed to initialize YouTube support.", exception);
//        }
//    }

    private static String getExternalBotInfo(String key) {
        return (System.getenv().getOrDefault(key, System.getProperty(key)));
    }


}