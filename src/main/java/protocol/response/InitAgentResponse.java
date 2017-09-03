package protocol.response;

import gui.TabAddressPair;
import gui.feature.Feature;
import java.time.LocalDateTime;
import javafx.scene.control.Tab;
import gui.layouts.controllers.ControllerUtils;
import gui.layouts.controllers.MasterWindowController;
import java.util.Set;
import protocol.BoardType;
import gui.userdata.DeviceValueObject;

public final class InitAgentResponse implements AgentResponse {

    private final DeviceValueObject device;
    private final BoardType boardType;
    private final Set<Feature> features;

    public InitAgentResponse(DeviceValueObject device,
            BoardType boardType, Set<Feature> features) {
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
