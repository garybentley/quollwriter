package com.quollwriter.db;

import java.sql.*;
import java.io.*;
import java.util.*;

import java.math.*;
import javax.crypto.*;
import java.security.*;

//import org.bouncycastle.bcpg.*;
import org.bouncycastle.openpgp.*;
//import org.bouncycastle.crypto.generators.*;
//import org.bouncycastle.crypto.params.*;
//import org.bouncycastle.openpgp.operator.bc.*;

import com.quollwriter.*;

import com.quollwriter.ui.*;
import com.quollwriter.data.*;
import com.quollwriter.data.editors.*;
import com.quollwriter.editors.*;

public class EditorEditorDataHandler implements DataHandler<EditorEditor, NamedObject>
{

    private static final String STD_SELECT_PREFIX = "SELECT dbkey, email, name, mynameforeditor, avatarimage, myavatarimageforeditor, status, invitedbyme, theirpublickey, id, messagingusername, servicename FROM editor_v";

    private ObjectManager objectManager = null;

    /**
     * A cache, since the same editor can be viewed in multiple places and updated in multiple places
     * we need a single object for the editor so that we have one object -> multiple viewers.
     */
    private Map<Long, EditorEditor> cache = new HashMap ();

    public EditorEditorDataHandler (ObjectManager om)
    {

        this.objectManager = om;

    }

    @Override
    public void createObject (EditorEditor ed,
                              Connection   conn)
                       throws GeneralException
    {

/*
        try
        {

            PGPKeyPair pair = this.generateKeyPair ();

            ed.setMyPublicKey (pair.getPublicKey ());
            ed.setMyPrivateKey (pair.getPrivateKey ());

        } catch (Exception e) {

            throw new GeneralException ("Unable to generate key pair for editor: " +
                                        ed,
                                        e);

        }
  */
        List<Object> params = new ArrayList<> ();
        params.add (ed.getKey ());
        params.add (ed.getEmail ());
        params.add (ed.getEditorStatus ().getType ());
        params.add (ed.isInvitedByMe ());

        try
        {

            params.add (UIUtils.getImageBytes (ed.getAvatar ()));

        } catch (Exception e) {

            params.add (null);

            Environment.logError ("Unable to get image bytes for avatar for editor: " +
                                  ed);

        }

        if (ed.getTheirPublicKey () != null)
        {

            try
            {

                params.add (EditorsUtils.getPGPPublicKeyByteEncoded (ed.getTheirPublicKey ()));

            } catch (Exception e) {

                throw new GeneralException ("Unable to encode public key",
                                            e);

            }

        } else {

            params.add (null);

        }

        params.add (ed.getMessagingUsername ());
        params.add (ed.getServiceName ());

        this.objectManager.executeStatement ("INSERT INTO editor (dbkey, email, status, invitedbyme, avatarimage, theirpublickey, messagingusername, servicename) VALUES (?, ?, ?, ?, ?, ?, ?, ?)",
                                             params,
                                             conn);

        this.cache.put (ed.getKey (),
                        ed);

    }

    @Override
    public void deleteObject (EditorEditor d,
                              boolean      deleteChildObjects,
                              Connection   conn)
                       throws GeneralException
    {

        List<Object> params = new ArrayList<> ();
        params.add (d.getKey ());

        this.objectManager.executeStatement ("DELETE FROM editor WHERE dbkey = ?",
                                             params,
                                             conn);

        this.cache.remove (d.getKey ());

    }

    @Override
    public void updateObject (EditorEditor ed,
                              Connection   conn)
                       throws GeneralException
    {

        List<Object> params = new ArrayList<> ();
        params.add (ed.getEditorStatus ().getType ());

        // May not exist if the editor is rejected.
        if (ed.getTheirPublicKey () != null)
        {

            //params.add (((RSAPublicBCPGKey) ed.getTheirPublicKey ().getPublicKeyPacket ().getKey ()).getEncoded ());
            try
            {

                params.add (EditorsUtils.getPGPPublicKeyByteEncoded (ed.getTheirPublicKey ()));

            } catch (Exception e) {

                throw new GeneralException ("Unable to encode public key",
                                            e);

            }

        } else {

            params.add (null);

        }

        params.add (ed.getMyNameForEditor ());

        try
        {

            params.add (UIUtils.getImageBytes (ed.getAvatar ()));

        } catch (Exception e) {

            params.add (null);

            Environment.logError ("Unable to get image bytes for avatar for editor: " +
                                  ed);

        }

        try
        {

            params.add (UIUtils.getImageBytes (ed.getMyAvatarForEditor ()));

        } catch (Exception e) {

            params.add (null);

            Environment.logError ("Unable to get image bytes for my avatar for editor: " +
                                  ed);

        }

        params.add (ed.getMessagingUsername ());
        params.add (ed.getServiceName ());
        params.add (ed.getKey ());

        this.objectManager.executeStatement ("UPDATE editor SET status = ?, theirpublickey = ?, mynameforeditor = ?, avatarimage = ?, myavatarimageforeditor = ?, messagingusername = ?, servicename = ? WHERE dbkey = ?",
                                             params,
                                             conn);

    }

    @Override
    public List<EditorEditor> getObjects (NamedObject parent,
                                          Connection  conn,
                                          boolean     loadChildObjects)
                                   throws GeneralException
    {

        List<EditorEditor> ret = new ArrayList<> ();

        try
        {

            //List params = new ArrayList ();
            //params.add (EditorEditor.EditorStatus.rejected.getType ());

            ResultSet rs = this.objectManager.executeQuery (STD_SELECT_PREFIX,// + " WHERE status != ?",
                                                            null,
                                                            conn);

            while (rs.next ())
            {

                ret.add (this.getEditor (rs));

            }

            try
            {

                rs.close ();

            } catch (Exception e)
            {
            }

        } catch (Exception e)
        {

            throw new GeneralException ("Unable to load ideas for: " +
                                        parent,
                                        e);

        }

        return ret;

    }


    private EditorEditor getEditor (ResultSet rs)
                             throws GeneralException
    {

        try
        {

            int ind = 1;

            long key = rs.getLong (ind++);

            EditorEditor ed = new EditorEditor ();

            ed.setKey (key);
            ed.setEmail (rs.getString (ind++));
            ed.setName (rs.getString (ind++));
            ed.setMyNameForEditor (rs.getString (ind++));

            ed.setAvatar (UIUtils.getImage (rs.getBytes (ind++)));

            // My avatar image for the editor.
            ed.setMyAvatarForEditor (UIUtils.getImage (rs.getBytes (ind++)));

            ed.setEditorStatus (EditorEditor.EditorStatus.valueOf (rs.getString (ind++)));
            ed.setInvitedByMe (rs.getBoolean (ind++));
            /*
            // My private key.
            byte[] bytes = rs.getBytes (ind++);

            // My public key.
            ed.setMyPublicKey (EditorsUtils.convertToPGPPublicKey (rs.getBytes (ind++)));

            if (bytes != null)
            {

                ByteArrayInputStream bin = new ByteArrayInputStream (bytes);

                RSASecretBCPGKey nprivKey = new RSASecretBCPGKey (new BCPGInputStream (bin));

                ed.setMyPrivateKey (new PGPPrivateKey (1,
                                                       ed.getMyPublicKey ().getPublicKeyPacket (),
                                                       nprivKey));

            }
              */
            // Their public key
            ed.setTheirPublicKey (EditorsUtils.convertToPGPPublicKey (rs.getBytes (ind++)));
            ed.setId (rs.getString (ind++));
            ed.setMessagingUsername (rs.getString (ind++));
            ed.setServiceName (rs.getString (ind++));

            this.cache.put (ed.getKey (),
                            ed);

            return ed;

        } catch (Exception e)
        {

            throw new GeneralException ("Unable to load editor",
                                        e);

        }

    }

    @Override
    public EditorEditor getObjectByKey (long        key,
                                        NamedObject parent,
                                        Connection  conn,
                                        boolean     loadChildObjects)
                                 throws GeneralException
    {

        ResultSet rs = null;

        try
        {

            List<Object> params = new ArrayList<> ();
            params.add (key);

            rs = this.objectManager.executeQuery (STD_SELECT_PREFIX + " WHERE dbkey = ?",
                                                  params,
                                                  conn);

            if (rs.next ())
            {

                EditorEditor ed = this.getEditor (rs);

                return ed;

            }

            return null;

        } catch (Exception e)
        {

            throw new GeneralException ("Unable to load editor",
                                        e);

        } finally
        {

            try
            {

                if (rs != null)
                {

                    rs.close ();

                }

            } catch (Exception e)
            {

            }

        }

    }

}
