package protocol.response;

import util.StringConstants;
import java.net.InetAddress;
import javafx.scene.control.Tab;
import gui.layouts.controllers.MasterWindowController;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javafx.application.Platform;
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
        updateTextArea("#spiTableView");
    }

    private void updateTextArea(String idPrefix) {
        Tab t = MasterWindowController
                .getTabManager().findTabByAddress(address);
        TableView<ByteArrayResponse> ta
                = ((TableView<ByteArrayResponse>) t.getContent()
                        .lookup(idPrefix + ':' + address.getHostAddress()));
        Platform.runLater(() -> {
            List<String> viewItems;
            if (responseBody.equals(StringConstants.WRITE_OK.toString())) {
                viewItems = Arrays.asList(responseBody);
            } else {
                viewItems = new ArrayList<>(Arrays
                        .asList(responseBody.split(" ")));
            }
            ta.getItems().add(0,
                    new ByteArrayResponse(LocalTime.now(), viewItems));
            refreshTable(ta);
        });

    }

    private void refreshTable(TableView<ByteArrayResponse> ta) {
        final List<ByteArrayResponse> items = ta.getItems();
        if (items == null || items.isEmpty()) {
            return;
        }

        final ByteArrayResponse item = ta.getItems().get(0);
        items.remove(0);
        items.add(0, item);
    }

}
