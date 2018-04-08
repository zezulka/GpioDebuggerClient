package gui.misc;

import java.net.InetAddress;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Objects;

public final class SshData {
    private final String username;
    private final InetAddress inetAddress;
    private byte[] password;

    public static class Builder {
        private String username = null;
        private InetAddress inetAddress = null;
        private byte[] password = null;

        public final SshData build() {
            Objects.requireNonNull(username, "Username was not set!");
            Objects.requireNonNull(inetAddress, "IP address was not set!");
            Objects.requireNonNull(password, "Password was not set!");
            return new SshData(this);
        }

        public final Builder username(String value) {
            this.username = value;
            return this;
        }

        public final Builder inetAddress(InetAddress value) {
            this.inetAddress = value;
            return this;
        }

        public final Builder password(byte[] value) {
            this.password = value;
            return this;
        }
    }

    private SshData(Builder builder) {
        this.username = builder.username;
        this.inetAddress = builder.inetAddress;
        this.password = builder.password;
    }

    public String getUsername() {
        return username;
    }

    public InetAddress getInetAddress() {
        return inetAddress;
    }

    public byte[] getPassword() {
        return password;
    }

    public void erasePassword() {
        Arrays.fill(password, (byte) 0);
    }

    @Override
    public String toString() {
        String passwd = "N/A";
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            passwd = Arrays.toString(md.digest(password));
        } catch (NoSuchAlgorithmException e) {
            // swallow the exception and leave the passwd in the default state
        }
        return "SshData{" + "username='" + username + '\''
                + ", inetAddress=" + inetAddress
                + ", password(SHA256)=" + passwd + '}';
    }
}
