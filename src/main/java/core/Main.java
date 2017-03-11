package core;

import java.awt.EventQueue;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import javafx.application.Application;
import layouts.controllers.Raspi;
import org.apache.log4j.BasicConfigurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
    private static final Logger MAIN_LOGGER = LoggerFactory.getLogger(Main.class);

    public static final int DEFAULT_SOCK_PORT = 1024;

    public static void main(String[] args) {
        EventQueue.invokeLater(() -> {
                Application.launch(Raspi.class, args);
        });
        try {
            sock = new Socket("10.42.0.138", Main.DEFAULT_SOCK_PORT);
            initResources();
            MAIN_LOGGER.info(ProtocolMessages.C_CONNECTION_OK.toString());
            receiveInitResponse();
            while (!sock.isClosed()) {
                receiveResponse();
            }
        } catch (IOException ex) {
            MAIN_LOGGER.error(ProtocolMessages.C_ERR_NOT_CONNECTED.toString(), ex);
            ex.printStackTrace(System.err);
            closeConnection();
        }
    }
    
    /**
     * This method attempts to close connection to server 
     * ({@literal  i.e.} closes Socket instance binded to the server).
     * Should socket be already closed, request is ignored.
     */
    public static void closeConnection() {
        try {
            if(sock == null || sock.isClosed()) {
                MAIN_LOGGER.error(ProtocolMessages.C_ERR_ALREADY_CLOSED.toString());
            } else {
                sock.close();
            }
        } catch (IOException ex) {
            MAIN_LOGGER.error(ProtocolMessages.C_CONNECTION_NOK.toString());
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
            return;
        }
        MAIN_LOGGER.info(name);
        deviceName = BoardType.parse(name);
    }

    private static void receiveResponse() throws IOException {
        MAIN_LOGGER.info(ProtocolMessages.C_RESPONSE_WAIT.toString());
        String response = input.readLine();
        if(response == null) {
            closeConnection();
        }
        MAIN_LOGGER.info(response);
    }
}
