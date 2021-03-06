package protocol.response;

import util.StringConstants;
import java.net.InetAddress;
import java.time.LocalTime;
import javafx.scene.control.Tab;
import gui.controllers.MasterWindow;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javafx.application.Platform;
import javafx.scene.control.TableView;

public final class I2cAgentResponse implements AgentResponse {

    private final String responseBody;
    private final InetAddress address;

    public I2cAgentResponse(String responseBody, InetAddress address) {
        this.responseBody = responseBody;
        this.address = address;
    }

    @Override
    public void react() {
        updateTextArea();
    }

    private void updateTextArea() {
        Tab t = MasterWindow
                .getTabManager().findTabByAddress(address);
        TableView<ByteArrayResponse> ta =
                ((TableView<ByteArrayResponse>) t.getContent()
                        .lookup("#i2cTableView" + ':'
                                + address.getHostAddress()));
        Platform.runLater(() -> {
            List<String> viewItems;
            if (responseBody.equals(StringConstants.WRITE_OK)) {
                viewItems = Arrays.asList(responseBody);
            } else {
                viewItems = new ArrayList<>();
                for (int i = 0; i < responseBody.length(); i += 2) {
                    viewItems.add(responseBody.substring(i, i + 2));
                }
            }
            ta.getItems()
                    .add(0, new ByteArrayResponse(LocalTime.now(), viewItems));
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
