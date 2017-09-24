package protocol.response;

import java.net.InetAddress;
import javafx.scene.control.Tab;
import gui.layouts.controllers.MasterWindowController;
import gui.layouts.controllers.SpiResponse;
import java.time.LocalTime;
import javafx.collections.FXCollections;
import javafx.scene.control.ListView;
import javafx.scene.control.TableView;

public final class SpiAgentResponse implements AgentResponse {

    private final String responseBody;
    private final InetAddress address;

    public SpiAgentResponse(String responseBody, InetAddress address) {
        this.responseBody = responseBody;
        this.address = address;
    }

    @Override
    public void react() {
        updateTextArea("#tableView");
    }

    private void updateTextArea(String idPrefix) {
        Tab t = MasterWindowController
                .getTabManager().findTabByAddress(address);
        TableView<SpiResponse> ta = ((TableView<SpiResponse>) t.getContent()
                .lookup(idPrefix + ':' + address.getHostAddress()));
        ta.getItems().add(new SpiResponse(LocalTime.now(),
                new ListView<>(FXCollections
                        .observableArrayList(responseBody.split(" ")))));
    }

}
