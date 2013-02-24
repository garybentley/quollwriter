package com.quollwriter.tools;

import de.schlichtherle.util.*;


public class CreateObfuscatedString
{

    public static void main (String[] argv)
    {

        try
        {

            System.out.println (ObfuscatedString.obfuscate (argv[0]));

        } catch (Exception e)
        {

            e.printStackTrace ();

        }

    }

}
