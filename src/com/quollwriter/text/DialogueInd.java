package com.quollwriter.text;

import java.util.*;

public class DialogueInd
{
    
    private Stack<Character> openChars = new Stack ();
    public boolean isInDialogue = false;
    
    public DialogueInd ()
    {
                    
    }
    
    public boolean update (char c)
    {
        
        boolean isOpen = TextUtilities.isOpenQ (c);
        
        if ((!this.isInDialogue)
            &&
            (isOpen)
           )
        {
            
            this.isInDialogue = true;
            this.openChars.push (c);
            
            return this.isInDialogue;
            
        }
                    
        if (this.isInDialogue)
        {
            
            // Check the top of the stack.
            if (TextUtilities.isCloseQForOpenQ (c,
                                                this.openChars.peek ()))
            {
                
                // Got matching pair.
                this.openChars.pop ();
                
            } else {
                
                // Don't match.
                if (isOpen)
                {
                    
                    // Is another open, doesn't match the top of the stack, so check down the stack to see
                    // if we have a match.
                    for (Character _c : this.openChars)
                    {
                        
                        if (_c.equals (c))
                        {
                            
                            this.openChars = new Stack ();
                            
                            this.isInDialogue = false;
                            
                            return this.isInDialogue;
                            
                        }
                        
                    }
                    
                    // Is open but the last char doesn't match, so push it onto stack.                        
                    this.openChars.push (c);
                    
                }
                
            }
                                
        }

        this.isInDialogue = this.openChars.size () != 0;
           
        return this.isInDialogue;
                    
    }
    
}
