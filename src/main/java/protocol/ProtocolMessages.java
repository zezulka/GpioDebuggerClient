/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package protocol;

/**
 *
 * @author miloslav
 */
public enum ProtocolMessages {
    C_CONNECTION_OK("Connection to server OK"),
    C_CONNECTION_NOK("I/O error while trying to communicate with server"),
    C_SERVER_READY("The system is ready to accept requests."),
    C_RESPONSE_WAIT("Waiting for server to response..."),
    
    C_ERR_NOT_CONNECTED("Proper connection has not been established with server."),
    C_ERR_NO_BOARD("No device is currently binded to this session!"),
    C_ERR_NOT_BUTTON("The clicked entity is not of Button type, ignoring..."),
    C_ERR_ALREADY_CLOSED("Cannot close connection to server: already closed"),
    C_ERR_GUI("GUI error"),
    C_ERR_GUI_NOT_BUTTON("error in MouseEvent: entity clicked is not of Button instance"),
    C_ERR_GUI_FXML("cannot load fxml file");
    
    private final String msg;
    
    ProtocolMessages(String msg) {
        this.msg = msg;
    }
    
    @Override
    public String toString() {
        return this.msg;
    }
}
