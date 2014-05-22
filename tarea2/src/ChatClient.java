import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.PrintWriter;
 
import java.net.Socket;
import java.net.UnknownHostException;
 
public class ChatClient {
    private static int port = 8080; /* port to connect to */
    private static String host = "localhost"; /* host to connect to */
 
    private static BufferedReader stdIn;
 
    private static String nick = "ferind";
 
    /**
     * Read in a nickname from stdin and attempt to authenticate with the 
     * server by sending a NICK command to @out. If the response from @in
     * is not equal to "OK" go bacl and read a nickname again
     */
    private static String getNick(BufferedReader in, 
                                  PrintWriter out) throws IOException {
        System.out.print("Enter your nick: ");
        String msg = stdIn.readLine();
        out.println("NICK " + msg);
        String serverResponse = in.readLine();
        if ("SERVER: OK".equals(serverResponse)) return msg;
        System.out.println(serverResponse);
        return getNick(in, out);
    }
 
    public static void main (String[] args) throws IOException {
 
        Socket server = null;
 
        try {
            server = new Socket(host, port);
        } catch (UnknownHostException e) {
            System.err.println(e);
            System.exit(1);
        }
 
        stdIn = new BufferedReader(new InputStreamReader(System.in));
 
        /* obtain an output stream to the server... */
        PrintWriter out = new PrintWriter(server.getOutputStream(), true);
        /* ... and an input stream */
        BufferedReader in = new BufferedReader(new InputStreamReader(
                    server.getInputStream()));
 
        nick = getNick(in, out);
 
        /* create a thread to asyncronously read messages from the server */
        ServerConn sc = new ServerConn(server);
        Thread t = new Thread(sc);
        t.start();
 
        String msg;
        /* loop reading messages from stdin and sending them to the server */
        while ((msg = stdIn.readLine()) != null) {
            out.println(msg);
        }
    }
}
 
class ServerConn implements Runnable {
    private BufferedReader in = null;
 
    public ServerConn(Socket server) throws IOException {
        /* obtain an input stream from the server */
        in = new BufferedReader(new InputStreamReader(
                    server.getInputStream()));
    }
 
    public void run() {
        String msg;
        try {
            /* loop reading messages from the server and show them 
             * on stdout */
            while ((msg = in.readLine()) != null) {
                System.out.println(msg);
            }
        } catch (IOException e) {
            System.err.println(e);
        }
    }
}