package core;

import java.awt.EventQueue;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
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

    public static final int DEFAULT_SOCK_PORT = 1024;

    public static void main(String[] args) {
        EventQueue.invokeLater(() -> {
                Application.launch(Raspi.class, args);
        });
        try {
            sock = new Socket("10.42.0.138", Main.DEFAULT_SOCK_PORT);
            initResources();
            Logger.getAnonymousLogger().log(Level.INFO, ProtocolMessages.C_CONNECTION_OK.toString());
            receiveInitResponse();
            while (!sock.isClosed()) {
                Logger.getAnonymousLogger().log(Level.INFO, ProtocolMessages.C_SERVER_READY.toString());
                receiveResponse();
            }
        } catch (IOException ex) {
            System.err.println("Error has occured: \n" + ex);
            ex.printStackTrace(System.err);
        }
    }
    
    /**
     * This method attempts to close connection to server 
     * ({@literal  i.e.} closes Socket instance binded to the server).
     * Should socket be already closed, request is ignored.
     */
    public static void closeConnection() {
        try {
            if(sock.isClosed()) {
                Logger.getAnonymousLogger().log(Level.SEVERE, "Cannot close connection to server: already closed");
            }
            sock.close();
        } catch (IOException ex) {
            Logger.getAnonymousLogger().log(Level.SEVERE, "Cannot close connection to server", ex);
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
            closeConnection();
        }
        Logger.getAnonymousLogger().log(Level.INFO, name);
        deviceName = BoardType.parse(name);
    }

    private static void receiveResponse() throws IOException {
        Logger.getAnonymousLogger().log(Level.INFO, ProtocolMessages.C_RESPONSE_WAIT.toString());
        String response = input.readLine();
        if(response == null) {
            closeConnection();
        }
        Logger.getAnonymousLogger().log(Level.INFO, response);
    }
}
