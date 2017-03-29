/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package layouts.controllers;

/**
 *
 * @author Miloslav
 */
public interface DeviceController {
    /**
     * This method provides feedback to client via status bar. The argument
     * being passed is set as the feedback.
     * @param msg 
     * @throws IllegalArgumentException if msg null
     */
    void setStatus(String msg);
}
