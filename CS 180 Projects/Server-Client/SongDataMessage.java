import java.io.Serializable;

/**
 * A message encapsulating the byte data of a song. Multiple of these messages should be sent for each song
 * because songs should be broken up into segments of 1000 bytes or less.
 */
/**
 * SongDataMessage.java
 *
 * Blueprint for a song header
 *
 * @author Nicholas Rosato, L15, S100
 *
 * @version April 8, 2019
 *
 */
public class SongDataMessage implements Serializable {
    private byte[] data;

    public SongDataMessage(byte[] data) {
        this.data = data;
    }

    public byte[] getData() {
        return data;
    }
}
