package com.quollwriter.editors.messages;

import java.util.*;

import com.quollwriter.*;
import com.quollwriter.data.*;
import com.quollwriter.data.editors.*;
import com.quollwriter.editors.*;

public class MessageFactory
{
    
    private static final Map<String, Class> messageTypes = new HashMap ();
        
    static
    {
        
        Map m = MessageFactory.messageTypes;
        
        // It "may" be better to scan the classpath here and find everything that extends
        // "EditorMessage", we could also use an annotation (which is just another way of saying it)
        // however an annotation duplicates the message type value which isn't desired.
        
        // However this "registration" method isn't the best either.  All have their pro/cons.
        // This method prevents tedious classpath scanning (or having to use yet another library like
        // "reflections" which is 100KB+) and inadvertent use of a class, for example we don't want
        // two classes trying to represent themselves as a single message and since we don't have complete
        // control of the classpath this method prevents that.  In theory a user may want to use a different
        // class for the message, but it would be better for them to recompile from the source rather than
        // adding in another jar (that may not guarantee the correct class anyway).
        
        // TODO: Move this setup to a config file that dynamically loads the classes.
        m.put (EditorChatMessage.MESSAGE_TYPE,
               EditorChatMessage.class);
        m.put (EditorInfoMessage.MESSAGE_TYPE,
               EditorInfoMessage.class);
        m.put (NewProjectMessage.MESSAGE_TYPE,
               NewProjectMessage.class);
        m.put (NewProjectResponseMessage.MESSAGE_TYPE,
               NewProjectResponseMessage.class);
        m.put (ProjectCommentsMessage.MESSAGE_TYPE,
               ProjectCommentsMessage.class);
        m.put (ErrorMessage.MESSAGE_TYPE,
               ErrorMessage.class);
        m.put (InviteResponseMessage.MESSAGE_TYPE,
               InviteResponseMessage.class);
        m.put (InviteMessage.MESSAGE_TYPE,
               InviteMessage.class);
        m.put (ProjectEditStopMessage.MESSAGE_TYPE,
               ProjectEditStopMessage.class);
        m.put (UpdateProjectMessage.MESSAGE_TYPE,
               UpdateProjectMessage.class);
        m.put (EditorRemovedMessage.MESSAGE_TYPE,
               EditorRemovedMessage.class);
        
    }
         
    public static EditorMessage getInstance (String messageType)
                                      throws GeneralException
    {

        // Create the relevant message.
        Class cl = MessageFactory.messageTypes.get (messageType);
        
        if (cl == null)
        {
            
            throw new GeneralException ("Unknown messagetype: " + messageType);
            
        }
        
        EditorMessage mess = null;
        
        try
        {
            
            mess = (EditorMessage) cl.newInstance ();
            
        } catch (Exception e) {
            
            throw new GeneralException ("Unable to create new instance of: " +
                                        cl.getName () +
                                        " for message type: " +
                                        messageType,
                                        e);
            
        }

        return mess;
        
    }
    
    public static EditorMessage getMessage (String       message,
                                            EditorEditor from,
                                            boolean      encrypted)
                                     throws Exception
    {
        
        String messageData = message;
        
        if (encrypted)
        {
            
            byte[] bytes = null;

            // Decrypt first.
            try
            {

                bytes = EditorsUtils.decrypt (message.getBytes (),
                                              EditorsEnvironment.getUserAccount ().getPrivateKey (),
                                              from.getTheirPublicKey ());            

            } catch (Exception e) {
                
                throw new GeneralException ("Unable to decrypt message from editor: " +
                                            from,
                                            e);
                
            }
            
            try
            {
            
                messageData = new String (bytes,
                                          "utf-8");

            } catch (Exception e) {
                
                throw new GeneralException ("Unable to convert decrypted message to a string from editor: " +
                                            from,
                                            e);
                
            }
            
        }
                
        // JSON decode
        Map data = (Map) JSONDecoder.decode (messageData);
        
        String messageType = (String) data.get ("messagetype");
        
        if (messageType == null)
        {
            
            throw new GeneralException ("No messagetype field found.");
            
        }
        
        EditorMessage mess = (EditorMessage) MessageFactory.getInstance (messageType);
        
        mess.init (data,
                   from);
        mess.setOriginalMessage (message);

        return mess;
        
    }
    
}