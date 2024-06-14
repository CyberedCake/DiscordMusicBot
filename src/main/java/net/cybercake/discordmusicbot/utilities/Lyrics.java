package net.cybercake.discordmusicbot.utilities;

import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo;
import net.cybercake.discordmusicbot.Main;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.stream.Collectors;

public class Lyrics {

    private final String title;
    private final AudioTrackInfo info;

    private String content;
    private String source;
    private String url;

    public Lyrics(String title, AudioTrackInfo info) {
        this.title = title;
        this.info = info;

        try {
            com.jagrosh.jlyrics.Lyrics lyrics = Main.lyricsClient.getLyrics(title).get();

            this.content = lyrics.getContent();
            this.source = lyrics.getSource();
            this.url = lyrics.getURL();
        } catch (Exception exception) {
            Log.warn("JLyrics lyric method failed", exception);
        }

        try {
            InputStream inputStream = new URL("https://music.youtube.com/watch?v=" + info.identifier).openStream();
            Log.info(new BufferedReader(new InputStreamReader(inputStream)).lines().collect(Collectors.joining(", ")));
            Document htmlDocument = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(inputStream);
            XPath path = XPathFactory.newInstance().newXPath();

            String expression = "/html/body/ytmusic-app/ytmusic-app-layout/ytmusic-player-page/div/div[4]/ytmusic-tab-renderer/ytmusic-section-list-renderer/div[2]/ytmusic-description-shelf-renderer/yt-formatted-string[2]/span";
            NodeList nodes = (NodeList) path.compile(expression).evaluate(htmlDocument, XPathConstants.NODESET);

            Log.info(nodes.toString());
        } catch (Exception exception) {
            Log.warn("XPath lyric method failed", exception);
        }
    }

    public String getLyrics() { return this.content; }

    public String getTitle() { return this.info.title; }
    public String getAuthor() { return this.info.author; }
    public String getURL() { return this.url; }
    public String getSource() { return this.source; }

}
