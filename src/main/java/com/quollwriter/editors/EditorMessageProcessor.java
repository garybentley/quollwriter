package com.quollwriter.editors;

import com.quollwriter.editors.messages.*;

public interface EditorMessageProcessor
{
    
    /**
     * Process the message, the return value indicates whether the message should be saved (sometimes it shouldn't,
     * for example if an invalid message is received or at the wrong time).
     *
     * @param mess The message to process.
     * @returns true if the message should be saved.
     */
    public boolean processMessage (EditorMessage mess)
                            throws Exception;
    
}