package gpiodebuggerui;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

import protocol.ProtocolMessages;

/**
 *
 * @author Miloslav Zezulka, 2017
 */
public class Main {

    private static Socket sock;
    private static final Scanner MOCK_INPUT = new Scanner(System.in);
    public static final int DEFAULT_SOCK_PORT = 1024;
    private static PrintWriter output;
    private static BufferedReader input;
    private static boolean hasFinished = false;

    public static void main(String[] args) {
        try {
            sock = new Socket("10.42.0.138", Main.DEFAULT_SOCK_PORT);
            initResources();
            System.out.println(ProtocolMessages.C_CONNECTION_OK.getMessage());
            GUI gui = new GUI();
            while (!hasFinished) {
                System.out.println(ProtocolMessages.C_SERVER_READY.getMessage());
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
        //input = new BufferedReader(new InputStreamReader(sock.getInputStream()));
        input = new BufferedReader(new InputStreamReader(sock.getInputStream()));
        output = new PrintWriter(sock.getOutputStream(), true);
    }

    private static void receiveResponse() throws IOException {
        System.out.println(ProtocolMessages.C_RESPONSE_WAIT.getMessage());
        String response = input.readLine();
        System.out.println(response);
        Main.hasFinished = response == null;
    }

    private static void sendRequest(String mock_request) throws IOException {
        if ("exit".equals(mock_request)) {
            hasFinished = true;
            return;
        }
        output.println(mock_request);
    }
}
