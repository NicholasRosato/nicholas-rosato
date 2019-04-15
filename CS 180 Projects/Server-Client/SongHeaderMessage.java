import java.io.Serializable;

/**
 * A message that encapsulates the critical information about a song. This message is sent only
 * once for each song, before the byte data starts being sent.
 */
/**
 * SongHeaderMessage.java
 *
 * Blueprint for a song header
 *
 * @author Nicholas Rosato, L15, S100
 *
 * @version April 8, 2019
 *
 */
public class SongHeaderMessage implements Serializable {
    private boolean songHeader;  // true only if the header indicates the start of a download request

    private String songName; // the name of the song being sent
    private String artistName; // the name of the artist who made the song being sent
    private int fileSize; // the size of the file, in bytes (should not be more than INTEGER.MAX_VALUE

    public SongHeaderMessage(boolean songHeader) {
        this.songHeader = songHeader;
    }

    public SongHeaderMessage(boolean songHeader, String songName, String artistName, int fileSize) {
        this.songHeader = songHeader;
        this.songName = songName;
        this.artistName = artistName;
        this.fileSize = fileSize;
    }

    public String getSongName() {
        return songName;
    }

    public String getArtistName() {
        return artistName;
    }

    public int getFileSize() {
        return fileSize;
    }

    public boolean isSongHeader() {
        return songHeader;
    }
}
