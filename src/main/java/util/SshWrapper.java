package util;

import gui.deployer.SshData;
import net.schmizz.sshj.SSHClient;
import net.schmizz.sshj.connection.channel.direct.Session;
import net.schmizz.sshj.transport.verification.PromiscuousVerifier;
import net.schmizz.sshj.userauth.method.AuthPassword;
import net.schmizz.sshj.userauth.password.PasswordFinder;
import net.schmizz.sshj.userauth.password.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
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
        sshClient.authPassword(data.getUsername(), new String(data.getPassword()));
        this.data = data;
    }

    public List<String> getRemoteCommandOutput(String command) {
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
