package com.quollwriter.editors;

import java.io.*;
import java.util.*;
import java.sql.*;
import javax.imageio.*;
import javafx.embed.swing.*;

import org.bouncycastle.bcpg.*;
import org.bouncycastle.openpgp.*;
import org.bouncycastle.openpgp.bc.*;
import org.bouncycastle.crypto.generators.*;
import org.bouncycastle.crypto.params.*;
import org.bouncycastle.openpgp.operator.bc.*;

import com.quollwriter.*;
import com.quollwriter.ui.*;
import com.quollwriter.db.*;
import com.quollwriter.data.*;
import com.quollwriter.data.editors.*;
import com.quollwriter.editors.messages.*;

public class EditorsObjectManager extends ObjectManager
{

    public EditorsObjectManager ()
    {

        this.handlers.put (EditorProject.class,
                           new EditorProjectDataHandler (this));
        //this.handlers.put (EditorProject.OBJECT_TYPE,
        //                   this.handlers.get (EditorProject.OBJECT_TYPE));
        this.handlers.put (EditorEditor.class,
                           new EditorEditorDataHandler (this));
        //this.handlers.put (EditorEditor.OBJECT_TYPE,
        //                   this.handlers.get (EditorEditor.OBJECT_TYPE));
        this.handlers.put (EditorMessage.class,
                           new EditorMessageDataHandler (this));
        //this.handlers.put (EditorMessage.OBJECT_TYPE,
        //                   this.handlers.get (EditorMessage.OBJECT_TYPE));
        this.handlers.put (ProjectEditor.class,
                           new ProjectEditorDataHandler (this));
        //this.handlers.put (ProjectEditor.OBJECT_TYPE,
        //                   this.handlers.get (ProjectEditor.OBJECT_TYPE));

    }

    public void init (File   dir,
                      String username,
                      String password,
                      String filePassword,
                      int    newSchemaVersion)
               throws GeneralException
    {

        super.init (dir,
                    username,
                    password,
                    filePassword,
                    newSchemaVersion);

    }

    @Override
    public boolean supportsLinks ()
    {

        return false;

    }

    @Override
    public void updateLinks (NamedObject d,
                             Connection  c)
    {

    }

    @Override
    public void deleteLinks (NamedObject      n,
                             Set<NamedObject> remove,
                             Connection       conn)
    {

    }

    @Override
    public List<Link> getLinks (NamedObject d,
                                Connection  conn)
    {

        return new ArrayList<> ();

    }

    public byte[] getOriginalMessage (EditorMessage em)
                               throws Exception
    {

        EditorMessageDataHandler handler = (EditorMessageDataHandler) this.getHandler (EditorMessage.class);

        return handler.getOriginalMessage (em);

    }

    public int getUndealtWithMessageCount ()
                                    throws Exception
    {

        EditorMessageDataHandler handler = (EditorMessageDataHandler) this.getHandler (EditorMessage.class);

        return handler.getUndealtWithMessageCount ();

    }

    // TODO: Maybe move to a builder pattern for more elegant searching/building up criteria?
    public Set<EditorMessage> getProjectMessages (String    projId,
                                                  String... messageTypes)
                                           throws Exception
    {

        EditorMessageDataHandler handler = (EditorMessageDataHandler) this.getHandler (EditorMessage.class);

        return handler.getProjectMessages (projId,
                                           messageTypes);

    }

    public boolean hasUserSentAProjectBefore ()
                                       throws Exception
    {

        EditorMessageDataHandler handler = (EditorMessageDataHandler) this.getHandler (EditorMessage.class);

        return handler.getMessageCount (NewProjectMessage.MESSAGE_TYPE,
                                        true) > 0;

    }

    public Set<EditorMessage> getAllUndealtWithMessages ()
                                                  throws Exception
    {

        EditorMessageDataHandler handler = (EditorMessageDataHandler) this.getHandler (EditorMessage.class);

        return handler.getAllUndealtWithMessages ();

    }

    public Set<ProjectInfo> getProjectsSentToEditor (EditorEditor ed)
                                              throws Exception
    {

        EditorMessageDataHandler handler = (EditorMessageDataHandler) this.getHandler (EditorMessage.class);

        return handler.getProjectsSentToEditor (ed);

    }

    public NewProjectMessage getNewProjectMessage (EditorEditor ed,
                                                   String       projectId,
                                                   boolean      sentByMe)
                                            throws Exception
    {

        EditorMessageDataHandler handler = (EditorMessageDataHandler) this.getHandler (EditorMessage.class);

        return handler.getNewProjectMessage (ed,
                                             projectId,
                                             sentByMe);

    }

    public boolean hasMyPublicKeyBeenSentToEditor (EditorEditor ed)
                                            throws Exception
    {

        EditorMessageDataHandler handler = (EditorMessageDataHandler) this.getHandler (EditorMessage.class);

        return handler.hasMyPublicKeyBeenSentToEditor (ed);

    }

    public boolean hasSentMessageOfTypeToEditor (EditorEditor ed,
                                                 String       messageType)
                                          throws Exception
    {

        EditorMessageDataHandler handler = (EditorMessageDataHandler) this.getHandler (EditorMessage.class);

        return handler.hasSentMessageOfTypeToEditor (ed,
                                                     messageType);

    }

    public boolean hasEditorSentInfo (EditorEditor ed)
                               throws Exception
    {

        EditorMessageDataHandler handler = (EditorMessageDataHandler) this.getHandler (EditorMessage.class);

        return handler.hasEditorSentInfo (ed);

    }

    public void updateSchemaVersion (int        newVersion,
                                     Connection conn)
                              throws Exception
    {

        List params = new ArrayList ();
        params.add (newVersion);

        this.executeStatement ("UPDATE info SET schema_version = ?",
                               params,
                               conn);

    }

    /**
     * Get the current/latest version of the schema that is available.  This is in contrast
     * to getSchemaVersion which should return the current version of the actual schema being
     * used.
     *
     * @returns The version.
     */
    public int getLatestSchemaVersion ()
    {

        return EditorsEnvironment.schemaVersion;

    }

    public int getSchemaVersion ()
                          throws GeneralException
    {

        Connection c = null;

        try
        {

            c = this.getConnection ();

            PreparedStatement ps = c.prepareStatement ("SELECT schema_version FROM info");

            ResultSet rs = ps.executeQuery ();

            if (rs.next ())
            {

                return rs.getInt (1);

            }

        } catch (Exception e)
        {

            this.throwException (c,
                                 "Unable to get schema version",
                                 e);

        } finally
        {

            this.releaseConnection (c);

        }

        return -1;


    }

    public EditorAccount getUserAccount ()
                                  throws Exception
    {

        Connection c = null;

        try
        {

            c = this.getConnection ();

            PreparedStatement ps = c.prepareStatement ("SELECT email, name, avatarimage, lastlogin, mypublickey, myprivatekey, messagingusername, servicename FROM info");

            ResultSet rs = ps.executeQuery ();

            if (rs.next ())
            {

                int ind = 1;

                String em = rs.getString (ind++);

                if (em == null)
                {

                    return null;

                }

                EditorAccount acc = new EditorAccount ();

                acc.setEmail (em);
                acc.setName (rs.getString (ind++));

                InputStream av = rs.getBinaryStream (ind++);

                if (av != null)
                {

                    acc.setAvatar (SwingFXUtils.toFXImage (ImageIO.read (av), null));

                }

                acc.setLastLogin (rs.getTimestamp (ind++));
                acc.setPublicKey (EditorsUtils.convertToPGPPublicKey (rs.getBytes (ind++)));

                ByteArrayInputStream bin = new ByteArrayInputStream (rs.getBytes (ind++));

                RSASecretBCPGKey nprivKey = new RSASecretBCPGKey (new BCPGInputStream (bin));

                acc.setPrivateKey (new PGPPrivateKey (1,
                                                      acc.getPublicKey ().getPublicKeyPacket (),
                                                      nprivKey));

                acc.setMessagingUsername (rs.getString (ind++));
                acc.setServiceName (rs.getString (ind++));

                return acc;

            }

        } catch (Exception e)
        {

            this.throwException (c,
                                 "Unable to get user account",
                                 e);

        } finally
        {

            this.releaseConnection (c);

        }

        return null;

    }
    /*
    public void setUserEmail (String em)
                       throws GeneralException
    {

        Connection c = null;

        try
        {

            c = this.getConnection ();

            List params = new ArrayList ();
            params.add (em);

            this.executeStatement ("UPDATE info SET email = ?",
                                   params,
                                   c);

        } finally
        {

            if (c != null)
            {

                try
                {

                    this.releaseConnection (c);

                } catch (Exception e)
                {
                }

            }

        }

    }
*/
    public String getNewMessageId (EditorEditor ed,
                                   String       messageType)
                            throws Exception
    {

        Connection c = null;

        try
        {

            c = this.getConnection ();

            EditorMessageDataHandler handler = (EditorMessageDataHandler) this.getHandler (EditorMessage.class);

            int count = 0;

            while (true)
            {

                if (count > 20)
                {

                    Environment.logError ("Unable to find new message id for editor: " + ed + " and message type: " + messageType);

                    return null;

                }

                String messId = ed.getId () + ":" + messageType + ":" + System.nanoTime ();

                EditorMessage mess = handler.getMessageByEditorAndId (ed,
                                                                      messId,
                                                                      c);

                if (mess == null)
                {

                    return messId;

                }

                count++;

            }

        } catch (Exception e)
        {

            this.throwException (c,
                                 "Unable to get new message id",
                                 e);

        } finally
        {

            this.releaseConnection (c);

        }

        return null;

    }

    public void setLastLogin (java.util.Date d)
                       throws GeneralException
    {

        Connection c = null;

        try
        {

            c = this.getConnection ();

            List params = new ArrayList ();

            params.add (d);

            this.executeStatement ("UPDATE info SET lastlogin = ?",
                                   params,
                                   c);

        } catch (Exception e)
        {

            this.throwException (c,
                                 "Unable to set last login",
                                 e);

        } finally
        {

            this.releaseConnection (c);

        }

    }

    public void setUserInformation (EditorAccount acc)
                             throws GeneralException
    {

        Connection c = null;

        try
        {

            c = this.getConnection ();

            List params = new ArrayList ();
            params.add (acc.getEmail ());
            params.add (acc.getName ());

            if (acc.getAvatar () != null)
            {

                try
                {

                    params.add (UIUtils.getImageBytes (SwingFXUtils.fromFXImage (acc.getAvatar (), null)));

                } catch (Exception e) {

                    Environment.logError ("Unable to get avatar bytes",
                                          e);

                    params.add (null);

                }

            } else {

                params.add (null);

            }

            try
            {

                params.add (EditorsUtils.getPGPPublicKeyByteEncoded (acc.getPublicKey ()));

            } catch (Exception e) {

                throw new GeneralException ("Unable to encode public key",
                                            e);

            }

            params.add (((RSASecretBCPGKey) acc.getPrivateKey ().getPrivateKeyDataPacket ()).getEncoded ());

            params.add (acc.getMessagingUsername ());
            params.add (acc.getServiceName ());

            this.executeStatement ("UPDATE info SET email = ?, name = ?, avatarimage = ?, mypublickey = ?, myprivatekey = ?, messagingusername = ?, servicename = ?",
                                   params,
                                   c);

        } catch (Exception e)
        {

            this.throwException (c,
                                 "Unable to update user info",
                                 e);

        } finally
        {

            this.releaseConnection (c);

        }

    }

    public String getSchemaFile (String file)
    {

        return Constants.EDITOR_SCHEMA_DIR + file;

    }

    public String getCreateViewsFile ()
    {

        return Constants.EDITOR_UPDATE_SCRIPTS_DIR + "/create-views.xml";

    }

    public String getUpgradeScriptFile (int oldVersion,
                                        int newVersion)
    {

        return Constants.EDITOR_UPDATE_SCRIPTS_DIR + "/" + oldVersion + "-" + newVersion + ".xml";

    }

}
