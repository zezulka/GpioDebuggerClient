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
import java.util.Collections;
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
        sshClient.authPassword(data.getUsername(),
                new String(data.getPassword()));
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
     * Wait for numCollect lines of the output stream and collect them into
     * the collection which is returned. Rest of the command output will then
     * be written to the output stream provided as the function argument.
     *
     * If less than numCollect lines are outputted before reaching EOF,
     * resulting collection will be smaller and nothing will be
     * written to the output stream.
     *
     * @param numCollect the num collect
     * @param command    the command
     * @param os         the os
     * @return the list
     */
    public List<String> scanAgentOutput(int numCollect, String command,
                                        OutputStream os) {
        Objects.requireNonNull(os, "output stream");
        Objects.requireNonNull(command, "command");
        try (Session session = sshClient.startSession()) {
            final Session.Command cmd = session.exec(command);
            BufferedReader br = new BufferedReader(
                    new InputStreamReader(cmd.getInputStream()));
            String curr;
            List<String> result = new ArrayList<>();
            while ((curr = br.readLine()) != null) {
                result.add(curr);
                if (--numCollect == 0) {
                    break;
                }
            }
            if (curr != null && numCollect == 0) {
                new Thread(() -> {
                    String innerCurr;
                    try {
                        while ((innerCurr = br.readLine()) != null) {
                            os.write(("[AGENT]" + innerCurr + '\n').getBytes());
                        }
                        cmd.join();
                    } catch (IOException ioe) {
                        LOGGER.debug(ioe.getMessage());
                    }
                }).start();
            }
            return result;
        } catch (IOException ex) {
            LOGGER.debug(ex.getMessage());
            return Collections.emptyList();
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
