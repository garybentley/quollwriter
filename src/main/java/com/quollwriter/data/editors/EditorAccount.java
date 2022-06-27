package com.quollwriter.data.editors;

import java.io.*;
import javax.imageio.*;

import javafx.beans.property.*;
import javafx.scene.image.*;
import javafx.embed.swing.*;

import java.util.Date;
import java.util.Map;
import java.util.LinkedHashMap;

import org.bouncycastle.openpgp.*;

import com.quollwriter.*;
import com.quollwriter.ui.fx.UIUtils;

public class EditorAccount
{

    private String email = null;
    private String pwd = null;
    private String name = null;
    private Date lastLogin = null;

    private EditorAuthor author = null;
    private EditorEditor editor = null;

    private String webServiceSessionId = null;

    private PGPPublicKey publicKey = null;
    private PGPPrivateKey privateKey = null;

    private String messagingUsername = null;
    private String serviceName = null;

    private ObjectProperty<Image> avatarProp = new SimpleObjectProperty ();

    public EditorAccount ()
    {


    }

    @Override
    public String toString ()
    {

        Map<String, Object> props = new LinkedHashMap ();

        props.put ("email",
                   this.email);
        props.put ("name",
                   this.name);
        props.put ("messagingUsername",
                   this.messagingUsername);
        props.put ("serviceName",
                   this.serviceName);

        props.put ("hasPassword",
                   this.pwd != null);

        props.put ("lastLogin",
                   this.lastLogin);

        return Environment.formatObjectToStringProperties (props);

    }

    public String getServiceName ()
    {

        return this.serviceName;

    }

    public void setServiceName (String n)
    {

        this.serviceName = n;

    }

    public String getMessagingUsername ()
    {

        return this.messagingUsername;

    }

    public void setMessagingUsername (String u)
    {

        this.messagingUsername = u;

    }

    public void setPublicKey (PGPPublicKey k)
    {

        this.publicKey = k;

    }

    public void setPrivateKey (PGPPrivateKey k)
    {

        this.privateKey = k;

    }

    public PGPPublicKey getPublicKey ()
    {

        return this.publicKey;

    }

    public PGPPrivateKey getPrivateKey ()
    {

        return this.privateKey;

    }

    public Date getLastLogin ()
    {

        return this.lastLogin;

    }

    public void setLastLogin (Date d)
    {

        this.lastLogin = d;

    }

    public void setWebServiceSessionId (String id)
    {

        this.webServiceSessionId = id;

    }

    public String getWebServiceSessionId ()
    {

        return this.webServiceSessionId;

    }

    public String getAvatarBase64Encoded ()
                                   throws Exception
    {

        if (this.avatarProp.getValue () == null)
        {

            return null;

        }

        ByteArrayOutputStream bout = new ByteArrayOutputStream ();
        ImageIO.write (SwingFXUtils.fromFXImage (this.avatarProp.getValue (), null),
                       "",
                       bout);

        return Base64.encodeBytes (bout.toByteArray ());

    }

    public ObjectProperty<Image> avatarProperty ()
    {

        return this.avatarProp;

    }

    public void setAvatar (Image im)
    {

        if (im != null)
        {

            if (im.getWidth () > Constants.EDITORS_SERVICE_AVATAR_MAX_WIDTH)
            {

                im = UIUtils.getScaledImage (im,
                                             Constants.EDITORS_SERVICE_AVATAR_MAX_WIDTH);

            }

        }

        this.avatarProp.setValue (im);

    }

    public Image getAvatar ()
    {

        return this.avatarProp.getValue ();

    }

    public void setName (String n)
    {

        this.name = n;

    }

    public String getName ()
    {

        return this.name;

    }

    public void setEditor (EditorEditor e)
    {

        this.editor = e;

    }

    public EditorEditor getEditor ()
    {

        return this.editor;

    }

    public EditorAuthor getAuthor ()
    {

        return this.author;

    }

    public void setAuthor (EditorAuthor a)
    {

        this.author = a;

    }

    public void setPassword (String p)
    {

        this.pwd = p;

    }

    public String getPassword ()
    {

        return this.pwd;

    }

    public String getEmail ()
    {

        return this.email;

    }

    public void setEmail (String em)
    {

        this.email = em;

    }

}
