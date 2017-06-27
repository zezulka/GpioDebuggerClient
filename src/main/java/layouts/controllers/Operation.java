/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package layouts.controllers;

/**
 * Operations available for communicating with interfaces 
 * available on the embedded device.
 * @author Miloslav
 */
public enum Operation {
    READ("read"),
    WRITE("write");
    
    private final String op;
    
    Operation(String op) {
        this.op = op;
    }
    
    public String getOp() {
        return this.op;
    }
    
    public boolean isReadOperation() {
        return this.equals(Operation.READ);
    }
    
    public boolean isWriteOperation() {
        return this.equals(Operation.WRITE);
    }
    
    @Override
    public String toString() {
        return this.getOp();
    }
}
