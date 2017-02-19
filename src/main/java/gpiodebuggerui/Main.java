package gpiodebuggerui;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.Socket;
import java.util.Scanner;

import net.ConnectionManager;

/**
 *
 * @author Miloslav Zezulka, 2017
 */
public class Main {

    private static Socket sock;
    private static final Scanner MOCK_INPUT = new Scanner(System.in);
    private static PrintStream output;
    private static BufferedReader input;
    private static boolean hasFinished = false;

    public static void main(String[] args) {
        try {
            sock = new Socket("10.42.0.138", ConnectionManager.DEFAULT_SOCK_PORT);
            System.out.println("Connection to server OK");
            while (!hasFinished) {
                initResources();
                sendRequest(MOCK_INPUT.nextLine());
                receiveResponse();
            }
            sock.close();
        } catch (IOException ex) {
            System.err.println("Error has occured: \n" + ex);
            ex.printStackTrace(System.err);
        }
    }
    
    private static void initResources() throws IOException {
        input = new BufferedReader(new InputStreamReader(sock.getInputStream()));
        output = new PrintStream(sock.getOutputStream());
    }

    private static void receiveResponse() throws IOException {
        String line;
        System.out.println("Waiting for server response...");
        while ((line = input.readLine()) != null) {
            System.out.println(line);
        }
    }

    private static void sendRequest(String mock_request) throws IOException {
        if ("exit".equals(mock_request)) {
            hasFinished = true;
            return;
        }
        output.println(mock_request);
    }
}
