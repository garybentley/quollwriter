package com.quollwriter;

import java.security.*;


public class QuollWriterSecurityManager extends SecurityManager
{

    public QuollWriterSecurityManager()
    {

        super ();

    }

    /*
    public void checkConnect (String host,
                              int    port)
    {

        System.out.println ("CALLED: " + host + ":" + port);

    }
     */
    /*
    public void checkSetFactory ()
    {

        System.out.println ("HERE 3");

    }

    public void checkPermission (Permission perm,
                                 Object     context)
    {
        System.out.println ("HERE 5: " + perm);
        if (perm.getName ().equals ("setSecurityManager"))
        {
            System.out.println ("HERE 4");
            new KillSwitch ();

        }

    }
     */
}
