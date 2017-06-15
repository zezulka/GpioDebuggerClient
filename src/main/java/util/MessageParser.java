package util;


import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import protocol.InterruptListenerStatus;


/**
 *
 * @author Miloslav Zezulka
 */
public class MessageParser {
    
    private static final Set<InterruptListenerStatus> SPECIAL_PREFIXES = 
            new HashSet<>(Arrays.asList(InterruptListenerStatus.values()));
    
    
    public static boolean isInterruptMessage(String messagePrefix) {
        if(messagePrefix == null) {
            return false;
        }
        try{
            return SPECIAL_PREFIXES.contains(InterruptListenerStatus.valueOf(messagePrefix));    
        } catch(IllegalArgumentException ex) {
            return false;
        }
        
    }
}
