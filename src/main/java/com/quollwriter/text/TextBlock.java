package com.quollwriter.text;

import java.util.*;

public interface TextBlock<P extends TextBlock,
                           // T should be same type as defining class.
                           T extends TextBlock,
                           // C is child class.
                           C extends TextBlock
                          >
{
    
    public int getAllTextStartOffset ();
    
    public int getAllTextEndOffset ();

    public int getEnd ();
    
    public int getStart ();
    
    public String getText ();
    
    public P getParent ();
    
    public T getPrevious ();    
    
    public T getNext ();
    
    public List<C> getChildren ();
    
}