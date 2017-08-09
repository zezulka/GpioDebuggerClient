package protocol;

/**
 *
 * @author Miloslav Zezulka, 2017
 */
public interface ClientPin {
    /**
     * This identifier is used for client protocol messages and should be used
     * for internal identification of all pins. When the pin is GPIO, this id
     * should not be directly used as a label in user interface as more common
     * way of describing GPIO pins is via their GPIO address.
     * @return
     */
    String getPinId();
    /**
     * Name which makes it easy for user to recognise GPIO pin. It is advised
     * IsGpio() should be called before calling this method as
     * UnsupportedOperationException is thrown when invoked on nonGPIO pins.
     * NonGPIO pins may use pin identifier instead as their reasonable name.
     * @throws UnsupportedOperationException pin does not belong to GPIO group
     * @return
     */
    String getGpioName();
    /**
     * @return physical location of the pin
     */
    int getPort();
    /**
     * Tells whether this pin belongs to GPIO pin group.
     * @return
     */
    boolean isGpio();
}
