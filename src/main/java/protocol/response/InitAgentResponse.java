package protocol.response;

import core.gui.App;
import core.net.ConnectionValueObject;
import java.time.LocalDateTime;
import protocol.BoardType;

public final class InitAgentResponse implements AgentResponse {

    private final ConnectionValueObject connection;
    private final BoardType boardType;

    public InitAgentResponse(ConnectionValueObject connection,
            BoardType boardType) {
        this.connection = connection;
        this.boardType = boardType;
    }

    @Override
    public void react() {
        connection.getDevice().setTimeConnected(LocalDateTime.now());
        connection.getDevice().setBoardType(boardType);

        App.getInstance().loadNewTab(connection.getDevice().getAddress(),
                connection.getDevice().getBoardType()
        );
    }

}
