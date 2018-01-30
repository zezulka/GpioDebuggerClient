package util;

public final class Unix {
    private Unix() {
        /* do not instantiate this class  */
    }

    private static final String OS_NAME = "os.name";
    private static final String USERNAME = System.getProperty("user.name");
    private static final String ROOT = "root";

    private static boolean checkOsNameProperty(String substring) {
        return System.getProperty(OS_NAME).toLowerCase().contains(substring);
    }

    private static boolean isUserRoot() {
        return USERNAME.equals(ROOT);
    }

    public static boolean isWindows() {
        return checkOsNameProperty("windows");
    }

    public static boolean isUnix() {
        return isLinux() || isMac();
    }

    private static boolean isLinux() {
        return checkOsNameProperty("linux");
    }

    private static boolean isMac() {
        return checkOsNameProperty("mac");
    }
}
