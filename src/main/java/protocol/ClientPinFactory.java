package protocol;

public final class ClientPinFactory {

    private ClientPinFactory() {
    }

    public static ClientPin getPin(String pinName) {

        BoardType type = pinName.startsWith("T_")
                ? BoardType.TESTING : BoardType.RASPBERRY_PI;
        switch (type) {
            case TESTING:
                return TestingClientPin.getPin(pinName);
            case RASPBERRY_PI:
                return RaspiClientPin.getPin(pinName);
            default:
                throw new IllegalArgumentException();
        }
    }
}
