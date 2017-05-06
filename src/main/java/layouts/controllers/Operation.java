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
public enum Operation {
    READRANGE("read from range of registers"), READ("read from register"),
    WRITERANGE("write to range of registers"), WRITE("write to register");
    
    private String op;
    
    Operation(String op) {
        this.op = op;
    }
    
    public String getOp() {
        return this.op;
    }
    
    public static boolean isReadOperation(Operation op) {
        return op.equals(Operation.READ) || op.equals(Operation.READRANGE);
    }
    
    public static boolean isWriteOperation(Operation op) {
        return !isReadOperation(op);
    }
}
