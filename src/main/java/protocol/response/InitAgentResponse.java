package protocol.response;

import core.gui.App;
import core.net.AgentConnectionValueObject;
import java.time.LocalDateTime;
import protocol.BoardType;

public class InitAgentResponse implements AgentResponse {

    private final AgentConnectionValueObject connection;
    private final BoardType boardType;

    public InitAgentResponse(AgentConnectionValueObject connection, BoardType boardType) {
        this.connection = connection;
        this.boardType = boardType;
    }

    @Override
    public void react() {
        connection.getDevice().setTimeConnected(LocalDateTime.now());
        connection.getDevice().setBoardType(boardType);
        
        App.loadNewTab(connection.getDevice().getAddress(), connection.getDevice().getBoardType());
    }
    
}
