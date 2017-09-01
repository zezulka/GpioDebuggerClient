package protocol.response;

import java.net.InetAddress;
import java.time.LocalTime;
import javafx.scene.control.Tab;
import javafx.scene.control.TextArea;
import gui.layouts.controllers.MasterWindowController;

public final class I2cAgentResponse implements AgentResponse {

    private final String responseBody;
    private final InetAddress address;

    public I2cAgentResponse(String responseBody, InetAddress address) {
        this.responseBody = responseBody;
        this.address = address;
    }

    @Override
    public void react() {
        updateTextArea("#i2cTextArea");
    }

    private void updateTextArea(String idPrefix) {
        Tab t = MasterWindowController
                .getTabManager().findTabByAddress(address);
        TextArea ta = ((TextArea) t.getContent().lookup(idPrefix
                + ':' + address.getHostAddress()));
        ta.setText(LocalTime.now().toString()
                + '\n' + responseBody
                + '\n' + ta.getText());
    }
}
