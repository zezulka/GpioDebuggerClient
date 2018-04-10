package util;

import gui.misc.SshData;
import net.schmizz.sshj.SSHClient;
import net.schmizz.sshj.connection.channel.direct.Session;
import net.schmizz.sshj.transport.verification.PromiscuousVerifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

public final class SshWrapper implements AutoCloseable {
    private final SSHClient sshClient;
    private final SshData data;
    private static final Logger LOGGER = LoggerFactory
            .getLogger(SshWrapper.class);

    public SshWrapper(SshData data) throws IOException {
        sshClient = new SSHClient();
        sshClient.addHostKeyVerifier(new PromiscuousVerifier());
        sshClient.connect(data.getInetAddress().getHostAddress());
        if (data.getPassword() == null || data.getPassword().length == 0) {
            sshClient.authPublickey(data.getUsername());
        } else {
            sshClient.authPassword(data.getUsername(),
                    new String(data.getPassword()));
        }
        this.data = data;
    }

    public List<String> scanRemoteOutput(String command) {
        List<String> result = new ArrayList<>();
        try (Session session = sshClient.startSession()) {
            final Session.Command cmd = session.exec(command);
            BufferedReader br = new BufferedReader(
                    new InputStreamReader(cmd.getInputStream()));
            String curr;
            while ((curr = br.readLine()) != null) {
                result.add(curr);
            }
            cmd.join(5, TimeUnit.SECONDS);
        } catch (IOException ex) {
            return new ArrayList<>();
        }
        return result;
    }

    /*
     * Launch command and write its output to output stream provided as the
     * function argument. This method is blocking.
     */
    public void launchRemoteCommand(String command, OutputStream os) {
        Objects.requireNonNull(os, "output stream");
        Objects.requireNonNull(command, "command");
        try (Session session = sshClient.startSession()) {
            final Session.Command cmd = session.exec(command);
            BufferedReader br = new BufferedReader(
                    new InputStreamReader(cmd.getInputStream()));
            String curr;
            while ((curr = br.readLine()) != null) {
                os.write(("[AGENT]" + curr + '\n').getBytes());
            }
            cmd.join();
        } catch (IOException ex) {
            LOGGER.debug(ex.getMessage());
        }
    }

    @Override
    public void close() {
        data.erasePassword();
        try {
            sshClient.close();
        } catch (IOException e) {
            LOGGER.debug(e.getMessage());
        }
    }
}
