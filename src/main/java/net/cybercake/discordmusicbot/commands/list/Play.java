package net.cybercake.discordmusicbot.commands.list;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.wrapper.spotify.exceptions.SpotifyWebApiException;
import com.wrapper.spotify.model_objects.specification.*;
import com.wrapper.spotify.requests.data.search.simplified.SearchAlbumsRequest;
import com.wrapper.spotify.requests.data.search.simplified.SearchPlaylistsRequest;
import com.wrapper.spotify.requests.data.search.simplified.SearchTracksRequest;
import net.cybercake.discordmusicbot.Embeds;
import net.cybercake.discordmusicbot.Main;
import net.cybercake.discordmusicbot.PresetExceptions;
import net.cybercake.discordmusicbot.commands.Command;
import net.cybercake.discordmusicbot.utilities.Asserts;
import net.cybercake.discordmusicbot.utilities.Log;
import net.cybercake.discordmusicbot.queue.Queue;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import org.apache.hc.core5.http.ParseException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class Play extends Command {

    public Play() {
        super(
                "play",
                "Type a URL or the name of a video to play music."
        );
        this.aliases = new String[]{"p"};
        this.requireDjRole = true;
        this.optionData = new OptionData[]{
                new OptionData(OptionType.STRING, "query", "The song URL or name.", true, true),
                new OptionData(OptionType.BOOLEAN, "priority", "Plays this song after the current song.", false)
        };
    }

    @Override
    public void command(SlashCommandInteractionEvent event) {
        Member member = event.getMember();
        User user = event.getUser();
        event.deferReply().queue();
        if(PresetExceptions.memberNull(event)) return;
        assert member != null;

        if(member.getVoiceState() == null || member.getVoiceState().getChannel() == null) { // they are not in a voice chat
            Embeds.throwError(event, user, "You must be in a voice channel to continue.", true, null); return;
        }

        Queue queue = Main.queueManager.getGuildQueue(member.getGuild(), member.getVoiceState().getChannel(), event.getChannel().asTextChannel());
        if(queue != null && !member.getVoiceState().getChannel().equals(queue.getVoiceChannel())) {
            Embeds.throwError(event, user, "You must be in the voice channel to continue.", true, null); return;
        }

        try {
            OptionMapping priority = event.getOptions().stream().filter(option -> option.getName().equalsIgnoreCase("priority")).findFirst().orElse(null);
            Main.queueManager.getGuildQueue(member.getGuild()).loadAndPlay(
                    event.getChannel().asTextChannel(),
                    user,
                    Objects.requireNonNull(event.getOption("query")).getAsString(),
                    event,
                    priority != null && priority.getAsBoolean()
            );
        } catch (Exception exception) {
            Embeds.throwError(event, user, "A general error occurred whilst trying to add the song! `" + exception + "`", exception);
        }
    }

    private record QueryDataCached(List<String> autocompletions, long expiration) {
        public boolean isExpired() {
            return expiration < System.currentTimeMillis();
        }
    }

    // first item: query
    // second item: autocompletions data, see QueryDataCached (record)
    private static final Map<String, QueryDataCached> queryCache = new HashMap<>();

    @Override
    @SuppressWarnings({"all"})
    public void tab(CommandAutoCompleteInteractionEvent event) {
        String option = event.getOption("query").getAsString();
        if(option == null || option.strip().isBlank()) { event.replyChoiceStrings().queue(); return; }
        if(!Asserts.throwsError(() -> new URL(option), MalformedURLException.class)) { event.replyChoiceStrings().queue(); return; }
        try {
            if(queryCache.containsKey(option)) {
                QueryDataCached data = queryCache.get(option);
                event.replyChoiceStrings(data.autocompletions).queue();
                if(data.isExpired()) queryCache.remove(option);
                return;
            }
            List<String> returnedSearchResults = new ArrayList<>();
            returnedSearchResults.addAll(youTubeSearchResults(option));
            // returnedSearchResults.addAll(spotifySearchResults(option)); // temp disabled

            event.replyChoiceStrings(returnedSearchResults).queue();
            queryCache.put(
                    option,
                    new QueryDataCached(
                            returnedSearchResults,
                            System.currentTimeMillis() + (TimeUnit.MILLISECONDS.convert(1, TimeUnit.HOURS))
                    )
            );
        } catch (Exception exception) {
            exception.printStackTrace();
            Log.error("Failed to autocomplete query of '" + option + "': " + exception);
            event.replyChoiceStrings().queue();
        }
    }

    // 🎵
    public List<String> spotifySearchResults(String query) throws IOException, ParseException, SpotifyWebApiException {
        List<String> spotifyReturnedSearchResults = new ArrayList<>();

        // search tracks
        SearchTracksRequest tracksRequest = Main.spotifyApi.searchTracks(query).limit(1).build();
        Paging<Track> trackPaging = tracksRequest.execute();
        spotifyReturnedSearchResults.addAll(
                Arrays.stream(trackPaging.getItems())
                        .map(Track::getName)
                        .map(this::shorten)
                        .map(item -> "Spotify: \uD83C\uDFB5 " + item)
                        .toList()
        );

        // search playlists
        SearchPlaylistsRequest playlistsRequest = Main.spotifyApi.searchPlaylists(query).limit(1).build();
        Paging<PlaylistSimplified> playlistSimplifiedPaging = playlistsRequest.execute();
        spotifyReturnedSearchResults.addAll(
                Arrays.stream(playlistSimplifiedPaging.getItems())
                        .map(PlaylistSimplified::getName)
                        .map(this::shorten)
                        .map(item -> "Spotify: \uD83D\uDCDA " + item)
                        .toList()
        );

        // search albums
        SearchAlbumsRequest albumsRequest = Main.spotifyApi.searchAlbums(query).limit(1).build();
        Paging<AlbumSimplified> albumPaging = albumsRequest.execute();
        spotifyReturnedSearchResults.addAll(
                Arrays.stream(albumPaging.getItems())
                        .map(AlbumSimplified::getName)
                        .map(this::shorten)
                        .map(item -> "Spotify: \uD83D\uDCBF " + item)
                        .toList()
        );

        return spotifyReturnedSearchResults;

//        URL suggestQueries = new URL("https://api.spotify.com/v1/search?type=track&locale=en-US%2Cen%3Bq%3D0.5&offset=0&limit=5&query=" + URLEncoder.encode(query, StandardCharsets.UTF_8));
//        HttpURLConnection connection = (HttpURLConnection) suggestQueries.openConnection();
//        connection.setRequestProperty("Authorization", "Bearer " + new String(Base64.getEncoder().encode(Main.SPOTIFY_TOKEN.getBytes(StandardCharsets.UTF_8))));
//        BufferedReader readerIn = new BufferedReader(new InputStreamReader(connection.getInputStream()));
//        String line;
//        while ((line = readerIn.readLine()) != null) {
//            Log.warn(line);
//        }
//        return List.of("no", "spotify", "suggestions", "yet");
    }
    public List<String> youTubeSearchResults(String query) throws IOException {
        URL suggestQueries = new URL("https://suggestqueries.google.com/complete/search?client=firefox&ds=yt&q=" + URLEncoder.encode(query, StandardCharsets.UTF_8));
        URLConnection connection = suggestQueries.openConnection();
        BufferedReader readerIn = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        JsonArray jsonArray = JsonParser.parseReader(readerIn).getAsJsonArray();
        List<JsonElement> search = jsonArray.get(1).getAsJsonArray().asList();
        return search.subList(0, (Math.min(search.size(), 10))).stream().map(JsonElement::getAsString).map(this::shorten).toList();
    }

    public String shorten(String string) {
        int MAX_LENGTH = 50;
        return (string.length() > MAX_LENGTH ? string.substring(0, MAX_LENGTH) + "..." : string);
    }
}
