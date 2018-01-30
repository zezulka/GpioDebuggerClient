package gui.deployer;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.layout.StackPane;
import util.SshWrapper;

import java.util.Objects;
import java.util.Stack;

/**
 * basic wizard infrastructure class
 */
public final class Wizard extends StackPane {
    private static final int UNDEFINED = -1;
    private ObservableList<AbstractWizardPage> pages
            = FXCollections.observableArrayList();
    private Stack<Integer> history = new Stack<>();
    private int curPageIdx = UNDEFINED;
    private SshWrapper sshWrapper;

    public Wizard() {
        pages.addAll(new IpUsernamePage(), new AuthPage(), new AgentJarPage());
        navTo(0);
        setStyle("-fx-padding: 10; -fx-background-color: cornsilk;");
    }

    public void nextPage() {
        if (hasNextPage()) {
            navTo(curPageIdx + 1);
        }
    }

    public SshWrapper getSshWrapper() {
        return sshWrapper;
    }

    public void setSshWrapper(SshWrapper sshWrapper) {
        Objects.requireNonNull(sshWrapper);
        this.sshWrapper = sshWrapper;
    }

    public void priorPage() {
        if (hasPriorPage()) {
            navTo(history.pop(), false);
        }
    }

    boolean hasNextPage() {
        return (curPageIdx < pages.size() - 1);
    }

    boolean hasPriorPage() {
        return !history.isEmpty();
    }

    private void navTo(int nextPageIdx, boolean pushHistory) {
        if (nextPageIdx < 0 || nextPageIdx >= pages.size()) {
            return;
        }
        if (curPageIdx != UNDEFINED) {
            if (pushHistory) {
                history.push(curPageIdx);
            }
        }

        AbstractWizardPage nextPage = pages.get(nextPageIdx);
        curPageIdx = nextPageIdx;
        getChildren().clear();
        getChildren().add(nextPage);
    }

    private void navTo(int nextPageIdx) {
        navTo(nextPageIdx, true);
    }

    void navTo(String id) {
        if (id == null) {
            return;
        }

        pages.stream()
                .filter(page -> id.equals(page.getId()))
                .findFirst()
                .ifPresent(page ->
                        navTo(pages.indexOf(page))
                );
    }

    public void reset() {
        navTo("IP address and username");
        if (sshWrapper != null) {
            sshWrapper.close();
        }
    }

    public void finish() {
        if (sshWrapper != null) {
            sshWrapper.close();
        }
    }
}

