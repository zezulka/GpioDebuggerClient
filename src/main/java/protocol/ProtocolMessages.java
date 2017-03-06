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
    C_SERVER_READY("The system is ready to accept requests."),
    C_RESPONSE_WAIT("Waiting for server to response..."),
    
    C_ERR_NOT_CONNECTED("Proper connection has not been established with server."),
    C_ERR_NOT_BUTTON("The clicked entity is not of Button type, ignoring...");
    
    private final String msg;
    
    ProtocolMessages(String msg) {
        this.msg = msg;
    }
    
    @Override
    public String toString() {
        return this.msg;
    }
}
