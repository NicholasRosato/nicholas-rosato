import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * MP3Server.java
 *
 * Blueprint for a MP3 server
 *
 * @author Nicholas Rosato, L15, S100
 * @author Mitchell Arndt, L15, S100
 *
 * @version April 8, 2019
 *
 */
public class MP3Server {
    private ServerSocket socket;

    public static void main(String[] args) {
        MP3Server server;

        try {
            server = new MP3Server(50000);
        } catch (IOException e) {
            System.out.println("Server could not be constructed");
            return;
        }

        server.serveClients();
    }

    //SERVE CLIENTS AFTER CONSTRUCTION
    public void serveClients() {
        Socket client;
        ClientHandler clientHandler;

        System.out.println("Server has been started");

        while (true) {
            try {
                client = this.socket.accept();
            } catch (IOException e) {
                System.out.println("Server could not accept");

                try {
                    this.socket.close();
                } catch (IOException i) {
                    System.out.println("Error when trying to close socket connection");
                }
                return;
            }

            System.out.println("Client has been connected");
            clientHandler = new ClientHandler(client);

            new Thread(clientHandler).start();
        }
    }


    //CONSTRUCTOR
    public MP3Server(int port) throws IllegalArgumentException, IOException {
        if (port < 0) {
            throw new IllegalArgumentException("Port argument is negative");
        } else {
            this.socket = new ServerSocket(port);
        }

    }
}


/**
 * Class - ClientHandler
 * <p>
 * This class implements Runnable, and will contain the logic for handling responses and requests to
 * and from a given client. The threads you create in MP3Server will be constructed using instances
 * of this class.
 */

/**
 * ClientHandler.java
 *i wanted
 * Blueprint for a client handler
 *
 * @author Nicholas Rosato, L15, S100
 *
 * @version April 8, 2019
 *
 */
final class ClientHandler implements Runnable {

    private ObjectInputStream inputStream;
    private ObjectOutputStream outputStream;

    //CONSTRUCTOR
    public ClientHandler(Socket clientSocket) throws IllegalArgumentException {
        if (clientSocket == null) {
            throw new IllegalArgumentException("Client socket is null in client handler");
        } else {
            try {
                this.inputStream = new ObjectInputStream(clientSocket.getInputStream());
                this.outputStream = new ObjectOutputStream(clientSocket.getOutputStream());
            } catch (IOException e) {
                System.out.println("Could not create streams in client handler");
            }
        }
    }

    /**
     * This method is the start of execution for the thread. See the handout for more details on what
     * to do here.
     */
    public void run() {
        SongRequest request;
        String mp3File;
        SongHeaderMessage responseToClient;

        try {
            while ((request = (SongRequest) inputStream.readObject()) != null) {
                if (request.isDownloadRequest()) {
                    mp3File = request.getArtistName() + " - " + request.getSongName() + ".mp3";
                    if (fileInRecord(mp3File)) {
                        File songFile = new File("songDatabase/" + mp3File);
                        responseToClient = new SongHeaderMessage(true, request.getSongName(),
                                request.getArtistName(), (int) songFile.length());
                        outputStream.writeObject(responseToClient);
                        outputStream.flush();
                        byte[] songArr = readSongData("songDatabase/" + songFile.getName());
                        sendByteArray(songArr);
                    } else {
                        responseToClient = new SongHeaderMessage(true, request.getSongName(),
                                request.getArtistName(), -1);
                        outputStream.writeObject(responseToClient);
                        outputStream.flush();
                    }
                } else {
                    responseToClient = new SongHeaderMessage(false, request.getSongName(),
                            request.getSongName(), -1);
                    outputStream.writeObject(responseToClient);
                    outputStream.flush();
                    String response = "Expect a record Strings rather than song data";
                    outputStream.writeObject(response);
                    outputStream.flush();
                    sendRecordData();
                }

            }
        } catch (IOException e) {
            System.out.println("Request in run method an issue: Client Connection Lost");
        } catch (ClassNotFoundException i) {
            System.out.println("Exception Occurred: Class Not Found");
        } finally {
            try {
                inputStream.close();
                outputStream.close();
            } catch (IOException e) {
                System.out.println("Error in trying to close input/output streams");
            }
        }
    }


    /**
     * Searches the record file for the given filename.
     *
     * @param fileName the fileName to search for in the record file
     * @return true if the fileName is present in the record file, false if the fileName is not
     */
    private static boolean fileInRecord(String fileName) {
        File file = new File("record.txt");
        try {
            BufferedReader br = new BufferedReader(new FileReader(file));
            String mp3Name;

            try {
                while ((mp3Name = br.readLine()) != null) {
                    if (mp3Name.equals(fileName)) {
                        return true;
                    }
                }
            } catch (IOException e) {
                System.out.println("Error reading file in fileInRecord");
            }
        } catch (FileNotFoundException e) {
            System.out.println("record.txt not found in fileInRecord");
        }

        return false;
    }


    /**
     * Read the bytes of a file with the given name into a byte array.
     *
     * @param fileName the name of the file to read
     * @return the byte array containing all bytes of the file, or null if an error occurred
     */
    private static byte[] readSongData(String fileName) {
        File file = new File(fileName);
        byte[] fileArr = new byte[(int) file.length()];

        try {
            FileInputStream fis = new FileInputStream(file);
            fis.read(fileArr);
            fis.close();
        } catch (FileNotFoundException e) {
            System.out.println("Could not find file in readSongData");
        } catch (IOException i) {
            System.out.println("IO exception in readSongData");
        }

        return fileArr;
    }


    /**
     * Split the given byte array into smaller arrays of size 1000, and send the smaller arrays
     * to the client using SongDataMessages.
     *
     * @param songData the byte array to send to the client
     */
    private void sendByteArray(byte[] songData) {
        byte[] arrayOfBytes;
        SongDataMessage message;
        boolean oneMoreIteration = false;
        int lastIndex = 0;
        int offset = 0;

        if (songData.length % 1000 != 0) {
            oneMoreIteration = true;
        }

        for (int i = 0; i < songData.length / 1000; i++) {
            arrayOfBytes = new byte[1000];
            offset = 1000 * i;
            for (int j = 0; j < 1000; j++) {
                arrayOfBytes[j] = songData[offset + j];
                lastIndex = offset + j + 1;
            }
            message = new SongDataMessage(arrayOfBytes);

            try {
                outputStream.writeObject(message);
                outputStream.flush();
            } catch (IOException e) {
                System.out.println("Error sending byte array message in sendByteArray");
            }
        }

        if (oneMoreIteration) {
            byte[] lastArray = new byte[(songData.length % 1000)];
            for (int i = 0; i < songData.length % 1000; i++) {
                lastArray[i] = songData[lastIndex + i];
            }
            message = new SongDataMessage(lastArray);
            try {
                outputStream.writeObject(message);
                outputStream.flush();
            } catch (IOException e) {
                System.out.println("Error sending last byte array in sendByteArray");
            }
        }
        try {
            outputStream.writeObject(null);
            outputStream.flush();
        } catch (IOException e) {
            System.out.println("Error in writing to the output stream");
        }

    }


    /**
     * Read ''record.txt'' line by line again, this time formatting each line in a readable
     * format, and sending it to the client. Send a ''null'' value to the client when done, to
     * signal to the client that you've finished sending the record data.
     */
    private void sendRecordData() {
        File file = new File("record.txt");
        try {
            BufferedReader br = new BufferedReader(new FileReader(file));
            String mp3Name;

            try {
                while ((mp3Name = br.readLine()) != null) {
                    outputStream.writeObject(mp3Name);
                    outputStream.flush();
                }

                outputStream.writeObject(null);
                outputStream.flush();
            } catch (IOException e) {
                System.out.println("Error reading file in fileInRecord");
            }
        } catch (FileNotFoundException e) {
            System.out.println("record.txt not found in fileInRecord");
        }
    }
}
