package com.quollwriter.editors.messages;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)

/**
 * An annotation to allow the system to determine what message type a class is for.
 */
public @interface MessageType
{
    
    public String type ();
    
}