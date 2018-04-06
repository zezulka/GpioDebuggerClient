package properties;

public final class SystemPropertiesExtractor {

    /**
     * User home directory. This is used as a directory in which another
     * .gpiodebugger hidden directory is created and then used to store user
     * defined data and preferences.
     */
    public static final String USER_HOME = System.getProperty("user.home");

    private SystemPropertiesExtractor() {
    }
}
