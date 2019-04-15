import java.io.*;
import java.net.Socket;
import java.util.Scanner;

/**
 * MP3Client.java
 *
 * Blueprint for a MP3 client
 *
 * @author Nicholas Rosato, L15, S100
 * @author Mitchell Arndt, L15, S100
 *
 * @version April 8, 2019
 *
 */
public class MP3Client {

    public static void main(String[] args) {
        Socket connection = null; //Connection to the server
        Scanner userInput = new Scanner(System.in);
        ObjectOutputStream outToTheServer = null;

        String request;
        SongRequest song;

        try {
            connection = new Socket("localhost", 50000);
            outToTheServer = new ObjectOutputStream(connection.getOutputStream());
        } catch (IOException e) {
            System.out.println("Client could not connect to server from MP3Client Main method");

            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (IOException i) {
                System.out.println("Could not close connection stream in MP3Client");
            }

            return;
        }

        System.out.println("Connected to Server");


        ResponseListener listener = new ResponseListener(connection);

        do {
            System.out.print("Do you want to see a [list] of available songs, " +
                    "[download] a song, or [exit]? Enter choice:  ");
            request = userInput.nextLine();

            if (request.toLowerCase().equals("download") || request.toLowerCase().equals("list")) {

                if (request.toLowerCase().equals("download")) {
                    String artist;
                    String songName;
                    System.out.print("Enter song name: ");
                    songName = userInput.nextLine();

                    System.out.print("Enter artist name: ");
                    artist = userInput.nextLine();
                    song = new SongRequest(true, songName, artist);
                } else {
                    song = new SongRequest(false);
                }

                try {
                    outToTheServer.writeObject(song);
                    outToTheServer.flush();

                    Thread t = new Thread(listener);
                    t.start();
                    t.join();

                } catch (IOException e) {
                    System.out.println("There was an IO exception in client main");
                } catch (InterruptedException e) {
                    System.out.println("There was an Interrupted exception in client main");
                }
            } else if (!request.toLowerCase().equals("exit")) {
                System.out.println("Invalid input: please enter [exit], [list], or [download]");
            }
        } while (!request.toLowerCase().equals("exit"));

        System.out.println("Client shutting down");
        userInput.close();

        try {
            outToTheServer.close();
            connection.close();
        } catch (IOException e) {
            System.out.println("Error closing in client main");
        }
    }
}

/**
 * This class implements Runnable, and will contain the logic for listening for
 * server responses. The threads you create in MP3Server will be constructed using
 * instances of this class.
 */
/**
 * ResponseListener.java
 *
 * Blueprint for a ResponseListener
 *
 * @author Nicholas Rosato, L15, S100
 *
 * @version April 8, 2019
 *
 */
final class ResponseListener implements Runnable {

    private ObjectInputStream ois;

    public ResponseListener(Socket clientSocket) throws IllegalArgumentException {
        if (clientSocket == null) {
            throw new IllegalArgumentException("Client socket is null in client response listener");
        } else {
            try {
                this.ois = new ObjectInputStream(clientSocket.getInputStream());
            } catch (IOException e) {
                System.out.println("Error making ois in response listener");
            }
        }

    }

    /**
     * Listens for a response from the server.
     * <p>
     * Continuously tries to read a SongHeaderMessage. Gets the artist name, song name, and file size from that header,
     * and if the file size is not -1, that means the file exists. If the file does exist, the method then subsequently
     * waits for a series of SongDataMessages, takes the byte data from those data messages and writes it into a
     * properly named file.
     */
    public void run() {
        try {
            Object response = ois.readObject();
            if (response instanceof SongHeaderMessage) {
                if (((SongHeaderMessage) response).isSongHeader() &&
                        ((SongHeaderMessage) response).getFileSize() > 0) {
                    byte[] songBytes = new byte[((SongHeaderMessage) response).getFileSize()];
                    String filename = "savedSongs/" + ((SongHeaderMessage) response).getArtistName() + " - " +
                            ((SongHeaderMessage) response).getSongName() + ".mp3";
                    SongDataMessage data;
                    int songIndex = 0;
                    data = (SongDataMessage) ois.readObject();
                    System.out.println("Downloading...... Please wait.");

                    while (data != null &&
                            songIndex < ((SongHeaderMessage) response).getFileSize()) {
                        for (int i = 0; i < data.getData().length; i++) {
                            songBytes[songIndex] = data.getData()[i];
                            songIndex++;
                        }
                        if (songIndex == ((SongHeaderMessage) response).getFileSize()) {
                            break;
                        }
                        data = (SongDataMessage) ois.readObject();
                    }

                    writeByteArrayToFile(songBytes, filename);
                    System.out.println("Finished Downloading.");
                    ois.readObject(); //Account for null message

                } else if (((SongHeaderMessage) response).isSongHeader() &&
                        ((SongHeaderMessage) response).getFileSize() < 0) {
                    System.out.println("Song: " + ((SongHeaderMessage) response).getSongName() + " by: " +
                            ((SongHeaderMessage) response).getArtistName() + " does not exist.");
                } else {
                    while (true) {
                        String line = (String) ois.readObject();
                        if (line == null) {
                            break;
                        } else {
                            System.out.println(line);
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("Connection with server had an error in client run method");
        }
    }


    /**
     * Writes the given array of bytes to a file whose name is given by the fileName argument.
     *
     * @param songBytes the byte array to be written
     * @param fileName  the name of the file to which the bytes will be written
     */
    private void writeByteArrayToFile(byte[] songBytes, String fileName) {
        File song = new File(fileName);
        try {
            FileOutputStream os = new FileOutputStream(song);
            os.write(songBytes);
            os.flush();
            os.close();
        } catch (IOException e) {
            System.out.println("Error writing to file in client");
        }
    }
}