package com.quollwriter.data.editors;

import java.util.*;

import org.bouncycastle.openpgp.*;

import com.quollwriter.*;
import com.quollwriter.editors.*;

public class Invite
{

    public enum Status
    {
        
        accepted ("accepted"),
        pending ("pending"),
        deleted ("deleted"),
        rejected ("rejected");
        
        private String type = null;
        
        Status (String type)
        {
            
            this.type = type;
            
        }
        
        public String getType ()
        {
            
            return this.type;
            
        }
        
    }
    
    private String from = null;
    private String to = null;
    private Date sent = null;
    private Status status = null;
    private PGPPublicKey fromPublicKey = null;
    private PGPPublicKey toPublicKey = null;
    private String toMessagingUsername = null;
    private String toServiceName = null;
    private String fromMessagingUsername = null;
    private String fromServiceName = null;
    
    public Invite ()
    {
        
        
    }
    
    public String toString ()
    {
        
        return "invite(to: " + this.to + ", from: " + this.from + ", status: " + this.status.getType () + ", sent: " + this.sent + ")";
        
    }
    
    public String getToMessagingUsername ()
    {
        
        return this.toMessagingUsername;
        
    }
    
    public void setToMessagingUsername (String u)
    {
        
        this.toMessagingUsername = u;
        
    }
    
    public String getToServiceName ()
    {
        
        return this.toServiceName;
        
    }
    
    public void setToServiceName (String n)
    {
        
        this.toServiceName = n;
        
    }
    
    public String getFromMessagingUsername ()
    {
        
        return this.fromMessagingUsername;
        
    }
    
    public void setFromMessagingUsername (String u)
    {
        
        this.fromMessagingUsername = u;
        
    }
    
    public String getFromServiceName ()
    {
        
        return this.fromServiceName;
        
    }
    
    public void setFromServiceName (String n)
    {
        
        this.fromServiceName = n;
        
    }
    
    public PGPPublicKey getFromPublicKey ()
    {
        
        return this.fromPublicKey;
        
    }
    
    public void setFromPublicKey (PGPPublicKey k)
    {
        
        this.fromPublicKey = k;
        
    }
    
    public PGPPublicKey getToPublicKey ()
    {
        
        return this.toPublicKey;
        
    }
    
    public void setToPublicKey (PGPPublicKey k)
    {
        
        this.toPublicKey = k;
        
    }

    public Status getStatus ()
    {
        
        return this.status;
        
    }
    
    public void setStatus (Status s)
    {
        
        this.status = s;
        
    }
    
    public Date getSent ()
    {
        
        return this.sent;
        
    }
    
    public void setSent (Date d)
    {
        
        this.sent = d;
        
    }
    
    public String getFromEmail ()
    {
        
        return this.from;
        
    }
    
    public void setFromEmail (String em)
    {
        
        this.from = em;
        
    }
    
    public String getToEmail ()
    {
        
        return this.to;
        
    }
    
    public void setToEmail (String em)
    {
        
        this.to = em;
        
    }
    
    public static Invite createFrom (Map m)
                              throws Exception
    {
        
        Invite inv = new Invite ();
        
        inv.setStatus (Status.valueOf ((String) m.get ("status")));
        inv.setFromEmail ((String) m.get ("from"));
        inv.setToEmail ((String) m.get ("to"));
        
        inv.setFromPublicKey (EditorsUtils.convertToPGPPublicKey (com.quollwriter.Base64.decode ((String) m.get ("fromPublicKey"))));
        inv.setToPublicKey (EditorsUtils.convertToPGPPublicKey (com.quollwriter.Base64.decode ((String) m.get ("toPublicKey"))));
        
        try
        {

            inv.setSent (new Date ((Integer) m.get ("sent")));
            
        } catch (Exception e) {
            
            
        }
        
        Map inf = (Map) m.get ("frommessaginginfo");
        
        if (inf != null)
        {
            
            inv.setFromMessagingUsername ((String) inf.get ("username"));
            inv.setFromServiceName ((String) inf.get ("servicename"));
            
        }
        
        inf = (Map) m.get ("tomessaginginfo");
        
        if (inf != null)
        {
            
            inv.setToMessagingUsername ((String) inf.get ("username"));
            inv.setToServiceName ((String) inf.get ("servicename"));
            
        }

        return inv;
        
    }
    
}