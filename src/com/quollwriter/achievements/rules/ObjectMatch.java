package com.quollwriter.achievements.rules;

import org.jdom.*;

import com.gentlyweb.utils.*;
import com.gentlyweb.xml.*;

import com.quollwriter.data.*;

public class ObjectMatch
{
    
    public class XMLConstants
    {

        public static final String accessor = "accessor";
        public static final String value = "value";

    }

    private Getter accessor = null;
    private String value = null;
    private String acc = null;
    private boolean not = false;

    public ObjectMatch (Element root)
                        throws  JDOMException
    {
                        
        this.value = JDOMUtils.getAttributeValue (root,
                                                  XMLConstants.value,
                                                  true);

        if (this.value.startsWith ("!"))
        {
            
            this.not = true;
            this.value = this.value.substring (1);
            
        }

        if (this.value.equals ("null"))
        {
            
            this.value = null;
            
        }

        this.acc = JDOMUtils.getAttributeValue (root,
                                                XMLConstants.accessor,
                                                true);
        
    }
    
    public boolean match (Object d)
                          throws Exception
    {
        
        if ((d == null)
            &&
            (this.acc.equals (""))
           )
        {
            
            if (this.value == null)
            {
                
                if (this.not)
                {
                    
                    return false;
                    
                }
                
                return true;
                
            }
            
        }
        
        if (this.accessor == null)
        {
            
            this.accessor = new Getter (this.acc,
                                        d.getClass ());
            
        }
        
        Object obj = this.accessor.getValue (d);
        
        if (obj == null)
        {
            
            if (this.value == null)
            {
                
                if (this.not)
                {
                    
                    return false;
                    
                }
                
                return true;
                
            }
            
        } else {
            
            if (this.value == null)
            {
                
                if (this.not)
                {
                    
                    return true;
                    
                }
                
                return false;
                
            }
            
        }
        
        String v = obj.toString ();
        
        if (v.equals (this.value))
        {
            
            if (this.not)
            {
                
                return false;
                
            }
            
            return true;
            
        } else {
            
            if (this.not)
            {
                
                return true;
                
            }
            
            return false;
            
        }
        
    }
            
}