package protocol.response;

import gui.AgentUserPrivileges;
import gui.TabAddressPair;
import core.net.ConnectionValueObject;
import java.time.LocalDateTime;
import javafx.scene.control.Tab;
import gui.layouts.controllers.ControllerUtils;
import gui.layouts.controllers.MasterWindowController;
import protocol.BoardType;

public final class InitAgentResponse implements AgentResponse {

    private final ConnectionValueObject connection;
    private final BoardType boardType;
    private final AgentUserPrivileges privileges;

    public InitAgentResponse(ConnectionValueObject connection,
            BoardType boardType, AgentUserPrivileges privileges) {
        this.connection = connection;
        this.boardType = boardType;
        this.privileges = privileges;
    }

    @Override
    public void react() {
        connection.getDevice().setTimeConnected(LocalDateTime.now());
        connection.getDevice().setBoardType(boardType);

        Tab loadedTab = ControllerUtils.getLoader(privileges).loadNewTab(
                connection.getDevice().getAddress(),
                connection.getDevice().getBoardType()
        );
        MasterWindowController.getTabManager()
                .addTab(new TabAddressPair(loadedTab,
                        connection.getDevice().getAddress()));
    }

}
