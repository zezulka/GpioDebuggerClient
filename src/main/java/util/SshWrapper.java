package util;

import core.SshData;
import net.schmizz.sshj.SSHClient;
import net.schmizz.sshj.connection.channel.direct.Session;
import net.schmizz.sshj.userauth.method.AuthPassword;
import net.schmizz.sshj.userauth.password.PasswordFinder;
import net.schmizz.sshj.userauth.password.Resource;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/* Wrapper to a SSH library which is capable of executing commands remotely */
public class SshWrapper {
    private final SSHClient sshClient;

    public SshWrapper(SshData data) throws IOException {
        this.sshClient = new SSHClient();
        sshClient.loadKnownHosts();
        sshClient.connect(data.ipAddress.get());
        sshClient.auth(data.username.get(), new AuthPassword(new PasswordFinder() {

            @Override
            public char[] reqPassword(Resource<?> resource) {
                return data.password.get().toCharArray();
            }

            @Override
            public boolean shouldRetry(Resource<?> resource) {
                return false;
            }
        }));
    }

    public List<String> getRemoteCommandOutput(String command) {
            List<String> result = new ArrayList<>();
            try (Session session = sshClient.startSession()) {
                final Session.Command cmd = session.exec(command);
                BufferedReader br = new BufferedReader(new InputStreamReader(cmd.getInputStream()));
                String curr;
                while((curr = br.readLine()) != null) {
                    result.add(curr);
                }
                cmd.join(5, TimeUnit.SECONDS);
            } catch(IOException ex) {
                return new ArrayList<>();
            }
            return result;
    }

    public void close() {
        try {
            sshClient.close();
        } catch (IOException e) {
            // nothing to do about it
        }
    }
}
