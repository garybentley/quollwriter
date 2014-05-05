package com.quollwriter.text;

import java.util.*;

/**
 * A null implementation that paragraph and word uses to fill in the blanks.
 *
 * This is probably some major generics abuse but I don't care bwahahahaah!!!!
 */
public class NoTextBlock implements TextBlock<NoTextBlock, NoTextBlock, NoTextBlock>
{
    
    public int getAllTextStartOffset ()
    {
        
        return -1;
        
    }
    
    public int getAllTextEndOffset ()
    {
        
        return -1;
        
    }

    public int getEnd ()
    {
        
        return -1;
        
    }
    
    public int getStart ()
    {
        
        return -1;
        
    }
    
    public String getText ()
    {
        
        return null;
        
    }
    
    public NoTextBlock getParent ()
    {
        
        return null;
        
    }
    
    public NoTextBlock getPrevious ()
    {
        
        return null;
        
    }
    
    public NoTextBlock getNext ()
    {
        
        return null;
        
    }
    
    public List<NoTextBlock> getChildren ()
    {
        
        return null;
        
    }
    
}