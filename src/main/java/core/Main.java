package core;

import java.awt.EventQueue;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Application;
import layouts.controllers.Raspi;
import protocol.BoardType;

import protocol.ProtocolMessages;

/**
 *
 * @author Miloslav Zezulka, 2017
 */
public class Main {

    private static Socket sock;
    private static PrintWriter output;
    private static BufferedReader input;
    private static BoardType deviceName;
    
    private static final Scanner MOCK_INPUT = new Scanner(System.in);
    public static final int DEFAULT_SOCK_PORT = 1024;
    private static boolean hasFinished = false;

    public static void main(String[] args) {
        EventQueue.invokeLater(() -> {
                Application.launch(Raspi.class, args);
        });
        try {
            sock = new Socket("10.42.0.138", Main.DEFAULT_SOCK_PORT);
            initResources();
            Logger.getAnonymousLogger().log(Level.INFO, ProtocolMessages.C_CONNECTION_OK.getMessage());
            receiveInitResponse();
            EventQueue.invokeLater(() -> {
                Application.launch(Client.class, args);
            });
            while (!hasFinished) {
                Logger.getAnonymousLogger().log(Level.INFO, ProtocolMessages.C_SERVER_READY.getMessage());
                sendRequest(MOCK_INPUT.nextLine());
                receiveResponse();
            }
            sock.close();
        } catch (IOException ex) {
            System.err.println("Error has occured: \n" + ex);
            ex.printStackTrace(System.err);
        }
    }
    
    public static PrintWriter getOutput() {
        return Main.output;
    }
    
    public static BufferedReader getInput() {
        return Main.input;
    }
    
    public static BoardType getDeviceName() {
        return Main.deviceName;
    }
    
    private static void initResources() throws IOException {
        input = new BufferedReader(new InputStreamReader(sock.getInputStream()));
        output = new PrintWriter(sock.getOutputStream(), true);
        
    }
    
    private static void receiveInitResponse() throws IOException {
        String name = input.readLine();
        if(name == null){
            hasFinished = true;
        }
        Logger.getAnonymousLogger().log(Level.INFO, name);
        deviceName = BoardType.parse(name);
    }

    private static void receiveResponse() throws IOException {
        Logger.getAnonymousLogger().log(Level.INFO, ProtocolMessages.C_RESPONSE_WAIT.getMessage());
        String response = input.readLine();
        if(response == null) {
            Main.hasFinished = true;
        }
        Logger.getAnonymousLogger().log(Level.INFO, response);
    }

    private static void sendRequest(String mock_request) throws IOException {
        if ("exit".equals(mock_request)) {
            hasFinished = true;
            return;
        }
        output.println(mock_request);
    }
}
