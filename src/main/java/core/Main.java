package core;

import java.awt.EventQueue;
import javafx.application.Application;

/**
 *
 * @author Miloslav Zezulka, 2017
 */
public class Main {

    /**
     * @param args
     */
    public static void main(String[] args) {
        EventQueue.invokeLater(() -> Application.launch(GuiEntryPoint.class, args));
        new Thread(ConnectionManager.getInstance()).start();
    }
    
}
    /*
    public static void main(String[] args) {
        EventQueue.invokeLater(() -> Application.launch(GuiEntryPoint.class, args));
     */
 /*try {
            while (!isReady()) {
                Thread.sleep(2500);
            }
            while (!sock.isClosed()) {
                receiveResponse();
            }
        } catch (IOException ex) {
            MAIN_LOGGER.error(ProtocolMessages.C_ERR_NOT_CONNECTED.toString(), ex);
            ex.printStackTrace(System.err);
            closeConnection();
        } catch (InterruptedException ex) {
            java.util.logging.Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        }*/

/*
    public static boolean isReady() {
        return !(deviceName == null || sock == null || sock.isClosed());
    }*/
/**
 * This method attempts to close connection to server ({@literal  i.e.} closes
 * Socket instance binded to the server). Should socket be already closed,
 * request is ignored.
 */
/*
    public static void closeConnection() {
        try {
            if (sock == null || sock.isClosed()) {
                MAIN_LOGGER.error(ProtocolMessages.C_ERR_ALREADY_CLOSED.toString());
            } else {
                sock.close();
            }
        } catch (IOException ex) {
            MAIN_LOGGER.error(ProtocolMessages.C_CONNECTION_NOK.toString(), ex);
        }
    }

    public static void initRoutine() {
        try {
            initResources();
            MAIN_LOGGER.info(ProtocolMessages.C_CONNECTION_OK.toString());
            receiveInitResponse();
        } catch (IOException ex) {
            MAIN_LOGGER.error("I/O exception:", ex);
        }
    }*/
/**
 * Sets IP address, which also invokes initRoutine method.
 *
 * @param ipAddress
 
    
    
    
    public static void setIpAddress(String ipAddress) {
        Main.ipAddress = ipAddress;
        initRoutine();
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
        sock = new Socket(ipAddress, Main.DEFAULT_SOCK_PORT);
        input = new BufferedReader(new InputStreamReader(sock.getInputStream()));
        output = new PrintWriter(sock.getOutputStream(), true);
    }

    private static void receiveInitResponse() throws IOException {
        String name = input.readLine();
        if (name == null) {
            closeConnection();
            return;
        }
        MAIN_LOGGER.info(name);
        deviceName = BoardType.parse(name);
    }

    private static void receiveResponse() throws IOException {
        MAIN_LOGGER.info(ProtocolMessages.C_RESPONSE_WAIT.toString());
        String response = input.readLine();
        if (response == null) {
            closeConnection();
        }
        MAIN_LOGGER.info(response);
    }
}*/
