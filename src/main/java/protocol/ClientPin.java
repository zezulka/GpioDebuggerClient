package protocol;

/**
 *
 * @author Miloslav Zezulka, 2017
 */
public interface ClientPin {
    String getName();
    int getPort();
    boolean isGpio();
}
