import java.io.Serializable;

/**
 * A message that encapsulates a song request to the server.
 */
/**
 * SongReqest.java
 *
 * Blueprint for a song request
 *
 * @author Nicholas Rosato, L15, S100
 *
 * @version April 8, 2019
 *
 */
public class SongRequest implements Serializable {
    private boolean downloadRequest; // true only if the request is to download a song

    private String songName; // name of the song requested
    private String artistName; // name of the artist for the song requested

    public SongRequest(boolean downloadRequest) {
        this.downloadRequest = downloadRequest;
    }

    public SongRequest(boolean downloadRequest, String songName, String artistName) {
        this.downloadRequest = downloadRequest;
        this.songName = songName;
        this.artistName = artistName;
    }

    public String getSongName() {
        return songName;
    }

    public String getArtistName() {
        return artistName;
    }

    public boolean isDownloadRequest() {
        return downloadRequest;
    }
}
