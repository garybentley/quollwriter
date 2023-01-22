package com.quollwriter.editors.messages;

import java.util.*;

import org.bouncycastle.openpgp.*;
import org.bouncycastle.bcpg.*;

import com.quollwriter.*;
import com.quollwriter.data.editors.*;
import com.quollwriter.editors.*;

public class PublicKeyMessage extends EditorMessage
{
    
    public static final String MESSAGE_TYPE = "publickey";
    
    private PGPPublicKey publicKey = null;
    
    public PublicKeyMessage ()
    {
                
    }
    
    public PublicKeyMessage (PGPPublicKey k)
    {
                
        this.publicKey = k;
        
    }
    
    public String getMessage ()
    {
        
        return null;
        
    }
    
    public void setMessage (String m)
    {
        
        // Just ignore.
        
    }

    public boolean isEncrypted ()
    {
        
        return false;
        
    }
    
    public String getMessageType ()
    {
        
        return MESSAGE_TYPE;
        
    }

    protected void fillMap (Map data)
                     throws GeneralException
    {
        
        if (this.publicKey == null)
        {
            
            throw new GeneralException ("No public key set");
            
        }
        
        RSAPublicBCPGKey pubKey = (RSAPublicBCPGKey) this.publicKey.getPublicKeyPacket ().getKey ();
                        
        data.put ("publickey",
                  com.quollwriter.Base64.encodeBytes (pubKey.getEncoded ()));
        
    }
    
    protected void doInit (Map          data,
                           EditorEditor from)
                    throws GeneralException
    {
        
        // Just look for a public key field.
        String pubkey = (String) data.get ("publickey");
        
        if (pubkey == null)
        {
            
            throw new GeneralException ("Expected to find a public key field");
            
        }
        
        byte[] pubkeybytes = com.quollwriter.Base64.decode (pubkey);
                                                    
        try
        {
            
            this.publicKey = EditorsUtils.convertToPGPPublicKey (pubkeybytes);
            
        } catch (Exception e) {
            
            throw new GeneralException ("Unable to convert public key",
                                        e);
            
        }
        
    }
    
    public PGPPublicKey getPublicKey ()
    {
        
        return this.publicKey;
        
    }
    
}