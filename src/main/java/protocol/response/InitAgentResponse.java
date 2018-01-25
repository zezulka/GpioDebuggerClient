package protocol.response;

import gui.tab.loader.TabAddressPair;
import gui.feature.Feature;
import java.time.LocalDateTime;
import javafx.scene.control.Tab;
import gui.controllers.ControllerUtils;
import gui.controllers.MasterWindowController;
import protocol.BoardType;
import gui.userdata.DeviceValueObject;
import java.util.List;

public final class InitAgentResponse implements AgentResponse {

    private final DeviceValueObject device;
    private final BoardType boardType;
    private final List<Feature> features;

    public InitAgentResponse(DeviceValueObject device,
            BoardType boardType, List<Feature> features) {
        this.device = device;
        this.boardType = boardType;
        this.features = features;
    }

    @Override
    public void react() {
        device.setTimeConnected(LocalDateTime.now());
        device.setBoardType(boardType);

        Tab loadedTab = ControllerUtils.getLoader().loadNewTab(
                device.getAddress(),
                device.getBoardType(),
                features
        );
        MasterWindowController.getTabManager()
                .addTab(new TabAddressPair(loadedTab, device.getAddress()));
    }

}
