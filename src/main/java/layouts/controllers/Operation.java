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
    READ("read"),
    WRITE("write");
    
    private final String op;
    
    Operation(String op) {
        this.op = op;
    }
    
    public String getOp() {
        return this.op;
    }
    
    public static boolean isReadOperation(Operation op) {
        return op.equals(Operation.READ);
    }
    
    public static boolean isWriteOperation(Operation op) {
        return op.equals(Operation.WRITE);
    }
}
