package com.quollwriter.db;

import java.sql.*;
import java.io.*;
import java.util.*;

import java.math.*;
import javax.crypto.*;
import java.security.*;

import org.bouncycastle.bcpg.*;
import org.bouncycastle.openpgp.*;
import org.bouncycastle.crypto.generators.*;
import org.bouncycastle.crypto.params.*;
import org.bouncycastle.openpgp.operator.bc.*;

import com.quollwriter.*;

import com.quollwriter.data.*;
import com.quollwriter.data.editors.*;
import com.quollwriter.editors.*;
import com.quollwriter.editors.messages.*;

public class EditorMessageDataHandler implements DataHandler<EditorMessage, EditorEditor>
{

    private static final String STD_SELECT_PREFIX = "SELECT dbkey, type, when, sentbyme, editordbkey, forprojectid, messageid, message, dealtwith FROM message_v";

    private ObjectManager objectManager = null;
    
    /**
     * A message cache, since the same message can be viewed in multiple places and updated in multiple places
     * we need a single object for the message so that we have one object -> multiple viewers.
     */
    private Map<Long, EditorMessage> messageCache = new HashMap ();
    
    public EditorMessageDataHandler (ObjectManager om)
    {

        this.objectManager = om;

    }
        
    @Override
    public void createObject (EditorMessage m,
                              Connection    conn)
                       throws GeneralException
    {
                
        if ((m.getOriginalMessage () == null)
            &&
            (m.isEncrypted ())
           )
        {
        
            throw new GeneralException ("Cannot save a message where the original, encrypted message is not available.");
            
        }
        
        if (m.getEditor () == null)
        {
            
            throw new GeneralException ("Cannot save a message where no editor is available.");
            
        }
        
        List params = new ArrayList ();
        params.add (m.getKey ());
        params.add (m.getWhen ());
        params.add (m.getMessageType ());
        params.add (m.isSentByMe ());
        params.add (m.getEditor ().getKey ());
        params.add (m.getForProjectId ());
        params.add (m.getMessageId ());
        params.add (m.getMessage ());
        params.add (m.getOriginalMessage ());
        params.add (m.isDealtWith ());
                
        this.objectManager.executeStatement ("INSERT INTO message (dbkey, when, type, sentbyme, editordbkey, forprojectid, messageid, message, origmessage, dealtwith) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                                             params,
                                             conn);        

        this.messageCache.put (m.getKey (),
                               m);
        
    }

    @Override
    public void deleteObject (EditorMessage d,
                              boolean       deleteChildObjects,
                              Connection    conn)
                       throws GeneralException
    {
    
        List params = new ArrayList ();
        params.add (d.getKey ());
    
        this.objectManager.executeStatement ("DELETE FROM message WHERE dbkey = ?",
                                             params,
                                             conn);

        this.messageCache.remove (d.getKey ());
        
    }

    @Override
    public void updateObject (EditorMessage m,
                              Connection    conn)
                       throws GeneralException
    {
        
        List params = new ArrayList ();

        params.add (m.getMessage ());
        params.add (m.isDealtWith ());
        params.add (m.getKey ());
        
        this.objectManager.executeStatement ("UPDATE message SET message = ?, dealtwith = ? WHERE dbkey = ?",
                                             params,
                                             conn);        
        
    }

    public boolean hasSentMessageOfTypeToEditor (EditorEditor ed,
                                                 String       messageType)
                                          throws GeneralException
    {

        // Ugh... so sick of this type of get connection, do something, release connection!
        Connection conn = null;
                
        try
        {

            conn = this.objectManager.getConnection ();

            List params = new ArrayList ();
            params.add (ed.getKey ());
            params.add (messageType);
        
            ResultSet rs = this.objectManager.executeQuery ("SELECT type FROM message_v WHERE editordbkey = ? AND sentbyme = TRUE AND type = ?",
                                                            params,
                                                            conn);

            if (rs.next ())
            {
                
                // Using the type here rather than the key because of stupid java Long/long ambiguities.
                if (rs.getString (1) != null)
                {
                    
                    return true;
                    
                }
                
            }
            
        } catch (Exception e) {

            this.objectManager.throwException (conn,
                                               "Unable to check if sent editor: " +
                                               ed +
                                               ", message of type: " +
                                               messageType,
                                               e);

        } finally {
            
            this.objectManager.releaseConnection (conn);
        
        }                    

        return false;
        
    }
    
    public boolean hasEditorSentInfo (EditorEditor ed)
                               throws GeneralException
    {
        
        // Ugh... so sick of this type of get connection, do something, release connection!
        Connection conn = null;
        
        ResultSet rs = null;
        
        try
        {

            conn = this.objectManager.getConnection ();

            List params = new ArrayList ();
            params.add (ed.getKey ());
            params.add (EditorInfoMessage.MESSAGE_TYPE);
        
            rs = this.objectManager.executeQuery ("SELECT type FROM message_v WHERE editordbkey = ? AND sentbyme = FALSE AND type = ?",
                                                  params,
                                                  conn);

            if (rs.next ())
            {
                
                // Using the type here rather than the key because of stupid java Long/long ambiguities.
                if (rs.getString (1) != null)
                {
                    
                    return true;
                    
                }
                
            }
            
        } catch (Exception e) {

            this.objectManager.throwException (conn,
                                               "Unable to check if editor has sent info: " +
                                               ed,
                                               e);

        } finally {
            
            this.objectManager.releaseConnection (conn);
        
        }                    
        
        return false;
        
    }
    
    public boolean hasMyPublicKeyBeenSentToEditor (EditorEditor ed)
                                            throws GeneralException
    {
        
        // Ugh... so sick of this type of get connection, do something, release connection!
        Connection conn = null;
                
        try
        {

            conn = this.objectManager.getConnection ();

            List params = new ArrayList ();
            params.add (ed.getKey ());
            params.add (PublicKeyMessage.MESSAGE_TYPE);
        
            ResultSet rs = this.objectManager.executeQuery ("SELECT type FROM message_v WHERE editordbkey = ? AND sentbyme = TRUE AND type = ?",
                                                            params,
                                                            conn);

            if (rs.next ())
            {
                
                // Using the type here rather than the key because of stupid java Long/long ambiguities.
                if (rs.getString (1) != null)
                {
                    
                    return true;
                    
                }
                
            }
            
        } catch (Exception e) {

            this.objectManager.throwException (conn,
                                               "Unable to check if editor has received my public key: " +
                                               ed,
                                               e);

        } finally {
            
            this.objectManager.releaseConnection (conn);
        
        }                    

        return false;
        
    }

    @Override
    public List<EditorMessage> getObjects (EditorEditor ed,
                                           Connection   conn,
                                           boolean      loadChildObjects)
                                    throws GeneralException
    {
                
        List<EditorMessage> ret = new ArrayList ();

        ResultSet rs = null;
        
        try
        {

            List params = new ArrayList ();
            params.add (ed.getKey ());
        
            rs = this.objectManager.executeQuery (STD_SELECT_PREFIX + " WHERE editordbkey = ? ORDER BY when",
                                                  params,
                                                  conn);

            while (rs.next ())
            {

                ret.add (this.getMessage (rs));

            }
            
        } catch (Exception e) {

            throw new GeneralException ("Unable to load message for: " +
                                        ed,
                                        e);

        } finally {
            
            try
            {

                rs.close ();

            } catch (Exception e)
            {
                
            }
        
        }            

        return ret;
        
    }
    
    private EditorMessage getMessage (ResultSet rs)
                               throws GeneralException
    {

        try
        {

            int ind = 1;

            long key = rs.getLong (ind++);

            EditorMessage am = this.messageCache.get (key);
            
            if (am != null)
            {
                
                return am;
                
            }
            
            String type = rs.getString (ind++);
            
            am = MessageFactory.getInstance (type);
            am.setKey (key);
            am.setWhen (rs.getTimestamp (ind++));
            am.setSentByMe (rs.getBoolean (ind++));
            am.setEditor (EditorsEnvironment.getEditorByKey (rs.getLong (ind++)));
            am.setForProjectId (rs.getString (ind++));
            am.setMessageId (rs.getString (ind++));            
            am.setMessage (rs.getString (ind++));
            am.setDealtWith (rs.getBoolean (ind++));
            
            return am;

        } catch (Exception e)
        {

            throw new GeneralException ("Unable to load message",
                                        e);

        }
            
    }
    
    public byte[] getOriginalMessage (EditorMessage em)
                               throws GeneralException
    {
        
        // Ugh... so sick of this type of get connection, do something, release connection!
        Connection conn = null;
                    
        try
        {

            conn = this.objectManager.getConnection ();

            List params = new ArrayList ();
            params.add (em.getEditor ().getKey ());
            params.add (em.getKey ());
        
            ResultSet rs = this.objectManager.executeQuery ("SELECT origmessage FROM message WHERE editordbkey = ? AND dbkey = ?",
                                                            params,
                                                            conn);

            if (rs.next ())
            {

                return rs.getString (1).getBytes ();

            }
                        
        } catch (Exception e) {

            this.objectManager.throwException (conn,
                                               "Unable to get original message for message: " +
                                               em,
                                               e);

        } finally {
            
            this.objectManager.releaseConnection (conn);
        
        }                    

        return null;
        
    }
        
    public int getMessageCount (String  messageType,
                                boolean sentByMe)
                         throws GeneralException
    {
        
        // Ugh... so sick of this type of get connection, do something, release connection!
        Connection conn = null;
                
        try
        {

            conn = this.objectManager.getConnection ();

            List params = new ArrayList ();
            params.add (messageType);
            params.add (sentByMe);
        
            ResultSet rs = this.objectManager.executeQuery ("SELECT COUNT(*) FROM message_v WHERE type = ? AND sentbyme = ?",
                                                            params,
                                                            conn);

            if (rs.next ())
            {

                return rs.getInt (1);

            }
                        
        } catch (Exception e) {

            this.objectManager.throwException (conn,
                                               "Unable to get message count for type: " +
                                               messageType,
                                               e);

        } finally {
            
            this.objectManager.releaseConnection (conn);
        
        }                    
        
        return 0;
        
    }
    
    public Set<Project> getProjectsSentToEditor (EditorEditor ed)
                                          throws GeneralException
    {

        // Ugh... so sick of this type of get connection, do something, release connection!
        Connection conn = null;
                
        try
        {

            conn = this.objectManager.getConnection ();

            // Do this here to prevent an O^n lookup to occur.
            Set<Project> allProjs = Environment.getAllProjects ();
        
            Map<String, Project> allProjsM = new HashMap ();
            
            for (Project p : allProjs)
            {
                
                allProjsM.put (p.getId (),
                               p);
                
            }
        
            Set<Project> projs = new LinkedHashSet ();
        
            List params = new ArrayList ();
            params.add (ed.getKey ());
            params.add (NewProjectMessage.MESSAGE_TYPE);
        
            ResultSet rs = this.objectManager.executeQuery ("SELECT DISTINCT forprojectid FROM message_v WHERE editordbkey = ? AND type = ? AND sentbyme = TRUE",
                                                            params,
                                                            conn);

            while (rs.next ())
            {
                
                String pid = rs.getString (1);

                Project p = allProjsM.get (pid);
                
                if (p != null)
                {
                    
                    projs.add (p);
                    
                }
                
            }
            
            return projs;
            
        } catch (Exception e) {

            this.objectManager.throwException (conn,
                                               "Unable to get project ids sent to editor: " +
                                               ed,
                                               e);

        } finally {
            
            this.objectManager.releaseConnection (conn);

        }                    
        
        return null;
        
    }
    
    public NewProjectMessage getNewProjectMessage (EditorEditor ed,
                                                   String       projectId,
                                                   boolean      sentByMe)
                                            throws GeneralException
    {

        // Ugh... so sick of this type of get connection, do something, release connection!
        Connection conn = null;
                
        try
        {

            conn = this.objectManager.getConnection ();

            List params = new ArrayList ();
            params.add (ed.getKey ());
            params.add (projectId);
            params.add (NewProjectMessage.MESSAGE_TYPE);
            params.add (sentByMe);
        
            ResultSet rs = this.objectManager.executeQuery (STD_SELECT_PREFIX + " WHERE editordbkey = ? AND forprojectid = ? AND type = ? AND sentbyme = ? ORDER BY when DESC",
                                                            params,
                                                            conn); 

            if (rs.next ())
            {

                return (NewProjectMessage) this.getMessage (rs);

            }
                        
        } catch (Exception e) {

            this.objectManager.throwException (conn,
                                               "Unable to get new project message for editor: " +
                                               ed +
                                               " and project: " +
                                               projectId,
                                               e);

        } finally {
            
            this.objectManager.releaseConnection (conn);

        }                    
        
        return null;
        
    }
    
    public Set<EditorMessage> getAllUndealtWithMessages ()
                                                  throws GeneralException
    {

        // Ugh... so sick of this type of get connection, do something, release connection!
        Connection conn = null;
                
        try
        {
        
            conn = this.objectManager.getConnection ();

            // We only include messages from non-pending editors.        
            ResultSet rs = this.objectManager.executeQuery (STD_SELECT_PREFIX + " WHERE dealtwith = FALSE AND editordbkey IN (SELECT dbkey FROM editor WHERE status <> 'pending') ORDER BY when DESC",
                                                            null,
                                                            conn);

            Set<EditorMessage> messages = new LinkedHashSet ();
                                                  
            while (rs.next ())
            {

                messages.add (this.getMessage (rs));

            }
            
            return messages;
            
        } catch (Exception e) {

            this.objectManager.throwException (conn,
                                               "Unable to get undealt with messages.",
                                               e);

        } finally {
            
            this.objectManager.releaseConnection (conn);
        
        }                    
        
        return null;
        
    }

    public Set<EditorMessage> getProjectMessages (String    projId,
                                                  String... messageTypes)
                                           throws GeneralException
    {

        // Ugh... so sick of this type of get connection, do something, release connection!
        Connection conn = null;
                
        try
        {
        
            conn = this.objectManager.getConnection ();

            List params = new ArrayList ();
            params.add (projId);
            
            String other = "";
            
            if ((messageTypes != null)
                &&
                // Oh java...
                (messageTypes.length > 0)
               )
            {
                
                StringBuilder b = new StringBuilder ("AND type IN (");
                
                for (int i = 0; i < messageTypes.length; i++)
                {
                
                    params.add (messageTypes[i]);
                    
                    if (i < messageTypes.length - 1)
                    {
                        
                        b.append (",");
                        
                    }
                    
                    b.append ("?");
                
                }
                
                b.append (")");
                
                other = b.toString ();
                
            }
                        
            // We only include messages from non-pending editors.        
            ResultSet rs = this.objectManager.executeQuery (String.format ("%s WHERE forprojectid = ? %s ORDER BY when DESC",
                                                                           STD_SELECT_PREFIX,
                                                                           other),
                                                            params,
                                                            conn);

            Set<EditorMessage> messages = new LinkedHashSet ();
                                                  
            while (rs.next ())
            {

                messages.add (this.getMessage (rs));

            }
            
            return messages;
            
        } catch (Exception e) {

            this.objectManager.throwException (conn,
                                               "Unable to get messages for project: " +
                                               projId +
                                               " and types: " +
                                               messageTypes,
                                               e);

        } finally {
            
            this.objectManager.releaseConnection (conn);
        
        }                    
        
        return null;
        
    }

    public int getUndealtWithMessageCount ()
                                    throws GeneralException
    {

        // Ugh... so sick of this type of get connection, do something, release connection!
        Connection conn = null;
                
        try
        {
        
            conn = this.objectManager.getConnection ();

            // We only include messages from non-pending editors.
            ResultSet rs = this.objectManager.executeQuery ("SELECT COUNT(*) FROM message_v mv WHERE dealtwith = FALSE AND editordbkey IN (SELECT dbkey FROM editor WHERE status <> 'pending')",
                                                            null,
                                                            conn);        

            if (rs.next ())
            {

                return rs.getInt (1);

            }
                        
        } catch (Exception e) {

            this.objectManager.throwException (conn,
                                               "Unable to get undealt with message count",
                                               e);

        } finally {
            
            this.objectManager.releaseConnection (conn);
        
        }                    
        
        return 0;
        
    }

    public EditorMessage getMessageByEditorAndId (EditorEditor ed,
                                                  String       messId,
                                                  Connection   conn)
                                           throws GeneralException
    {
        
        ResultSet rs = null;
        
        try
        {

            List params = new ArrayList ();
            params.add (ed.getKey ());
            params.add (messId);
        
            rs = this.objectManager.executeQuery (STD_SELECT_PREFIX + " WHERE editordbkey = ? AND messageid = ?",
                                                  params,
                                                  conn);

            if (rs.next ())
            {

                return this.getMessage (rs);

            }
            
            return null;
            
        } catch (Exception e) {

            throw new GeneralException ("Unable to get message: " +
                                        ed +
                                        ", message id: " +
                                        messId,
                                        e);

        } finally {
            
            try
            {

                rs.close ();

            } catch (Exception e)
            {
                
            }
        
        }            
        
    }
    
    @Override
    public EditorMessage getObjectByKey (int          key,
                                         EditorEditor ed,
                                         Connection   conn,
                                         boolean      loadChildObjects)
                                  throws GeneralException
    {
        
        ResultSet rs = null;
        
        try
        {

            List params = new ArrayList ();
            params.add (key);
            params.add (ed.getKey ());
        
            rs = this.objectManager.executeQuery (STD_SELECT_PREFIX + " WHERE dbkey = ? AND editordbkey = ?",
                                                  params,
                                                  conn);

            if (rs.next ())
            {

                return this.getMessage (rs);

            }
            
            return null;
            
        } catch (Exception e) {

            throw new GeneralException ("Unable to get message with key: " +
                                        key,
                                        e);

        } finally {
            
            try
            {

                rs.close ();

            } catch (Exception e)
            {
                
            }
        
        }            
                
    }

}
