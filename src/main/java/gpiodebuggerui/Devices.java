package gpiodebuggerui;

import io.silverspoon.bulldog.beagleboneblack.BeagleBoneBlackBoardFactory;
import io.silverspoon.bulldog.core.platform.Board;
import io.silverspoon.bulldog.cubieboard.CubieboardBoardFactory;
import io.silverspoon.bulldog.raspberrypi.RaspberryPiBoardFactory;

/**
 *
 * @author Miloslav Zezulka, 2017
 */
public final class Devices {
    public static final Board RASPBERRY_PI = new RaspberryPiBoardFactory().createBoard();
    public static final Board BEAGLEBONE_BLACK = new BeagleBoneBlackBoardFactory().createBoard();
    public static final Board CUBIEBOARD = new CubieboardBoardFactory().createBoard();
}
