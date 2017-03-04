/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package layouts;

/**
 *
 * @author Miloslav Zezulka, 2017
 */
public interface ClientPin {
    String getName();
    int getPort();
    boolean isGpio();
}
