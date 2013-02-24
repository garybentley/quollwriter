package com.quollwriter.tools;

import java.io.*;

import java.security.*;

import java.util.*;
import java.util.zip.*;

import javax.media.jai.*;
import javax.media.jai.operator.*;

import com.gentlyweb.utils.*;

import com.quollwriter.*;

import com.sun.media.jai.codec.*;


public class EncodeStrings
{

    public static void main (String[] argv)
    {

        try
        {

            StringBuilder outData = new StringBuilder ();

            File mappingFile = new File (System.getProperty ("user.dir") + "/" + argv[0]);

            BufferedReader in = new BufferedReader (new FileReader (mappingFile));

            String line = null;

            while ((line = in.readLine ()) != null)
            {

                String[] items = line.split (",");

                // 0 -> input file.
                // 1 -> input image file.
                // 2 -> output image file.

                File dataFile = new File (System.getProperty ("user.dir") + "/" + items[0]);

                if (outData.length () > 0)
                {

                    outData.append ("|");

                }

                outData.append (dataFile.getName ().substring (0,
                                                               dataFile.getName ().length () - "-strings.txt".length ()));
                outData.append (",");

                String file = IOUtils.getFile (dataFile);

                File inFile = new File (System.getProperty ("user.dir") + "/" + items[1]);
                File outFile = new File (System.getProperty ("user.dir") + "/" + items[2]);

                outData.append ("/" + items[2]);

                EncodeStrings.writeToFile (file.getBytes (),
                                           inFile,
                                           outFile);

                System.out.println ("Written: " + items[0] + " to: " + outFile);

            }

            in.close ();
            System.out.println ("OUTDATA: " + outData);
            EncodeStrings.writeToFile (outData.toString ().getBytes (),
                                       new File (System.getProperty ("user.dir") + "/" + argv[1]),
                                       new File (System.getProperty ("user.dir") + "/" + argv[2]));

            System.out.println ("Mapping file written: " + argv[0] + " to: " + argv[2]);

            EncodeStrings.writeToFile (IOUtils.getFileAsArray (new File (System.getProperty ("user.dir") + "/quollwriter-publickeys.store")),
                                       new File (System.getProperty ("user.dir") + "/imgs/logo.png"),
                                       new File (System.getProperty ("user.dir") + "/imgs/logo2.png"));

            System.out.println ("Written: quollwriter-publickeys.store to: /imgs/logo2.png");

        } catch (Exception e)
        {

            e.printStackTrace ();

        }

    }

    private static void writeToFile (byte[] file,
                                     File   inFile,
                                     File   outFile)
                              throws Exception
    {

        IOUtils.copyFile (inFile,
                          outFile,
                          4096);

        String fileStr = Base64.encodeBytes (file);

        // Get the digest.
        MessageDigest md = MessageDigest.getInstance ("SHA-256");

        md.update (fileStr.getBytes ());

        byte[] digest = md.digest ();

        // Get the input file.
        PNGDecodeParam param = new PNGDecodeParam ();
        param.setGenerateEncodeParam (true);

        RenderedOp inOp = PNGDescriptor.create (new FileSeekableStream (outFile),
                                                param,
                                                null);

        // Need to do this ourselves.
        PNGEncodeParam.getDefaultEncodeParam (inOp);

        PNGEncodeParam eparam = param.getEncodeParam ();

        eparam.addPrivateChunk ("mkBR",
                                EncodeStrings.zipData (fileStr.getBytes ()));
        eparam.addPrivateChunk ("mkBf",
                                digest);

        JAI.create ("filestore",
                    inOp,
                    outFile.getPath (),
                    "PNG",
                    eparam);

    }

    public static byte[] getPNGData (String resource)
                              throws Exception
    {

        return EncodeStrings.getPNGData (Environment.getResourceStream (resource));

    }

    public static byte[] getPNGData (File f)
                              throws Exception
    {

        return EncodeStrings.getPNGData (new FileInputStream (f));

    }

    public static byte[] getPNGData (InputStream in)
                              throws Exception
    {

        PNGDecodeParam param = new PNGDecodeParam ();
        param.setGenerateEncodeParam (true);

        RenderedOp op = PNGDescriptor.create (new ForwardSeekableStream (in),
                                              param,
                                              null);

        // Need to do this ourselves.
        PNGEncodeParam.getDefaultEncodeParam (op);

        PNGEncodeParam eparam = param.getEncodeParam ();

        boolean has = false;

        byte[] digest = null;
        byte[] ndigest = null;

        byte[] data = null;

        for (int i = 0; i < eparam.getNumPrivateChunks (); i++)
        {

            String t = eparam.getPrivateChunkType (i);

            if (t.equals ("mkBf"))
            {

                digest = eparam.getPrivateChunkData (i);

            }

            if (t.equals ("mkBR"))
            {

                has = true;

                data = EncodeStrings.getChunkData (eparam.getPrivateChunkData (i));

                MessageDigest md = MessageDigest.getInstance ("SHA-256");

                md.update (data);

                ndigest = md.digest ();

                String str = new String (data);

                str = StringUtils.replaceString (str,
                                                 String.valueOf ('\n'),
                                                 "");
                str = StringUtils.replaceString (str,
                                                 String.valueOf ('\r'),
                                                 "");

                data = Base64.decode (str);

            }

        }

        if ((data == null) ||
            (digest == null))
        {

            return null;

        }

        if (!MessageDigest.isEqual (digest,
                                    ndigest))
        {

            return null;

        }

        return data;

    }

    private static byte[] zipData (byte[] data)
                            throws Exception
    {

        ByteArrayOutputStream bout = new ByteArrayOutputStream ();
        GZIPOutputStream      gout = new GZIPOutputStream (bout);

        gout.write (data,
                    0,
                    data.length);

        gout.flush ();
        gout.close ();

        return bout.toByteArray ();

    }

    private static byte[] getChunkData (byte[] data)
                                 throws Exception
    {

        GZIPInputStream gin = new GZIPInputStream (new ByteArrayInputStream (data));

        ByteArrayOutputStream bout = new ByteArrayOutputStream ();

        IOUtils.streamTo (gin,
                          bout,
                          4096);

        return bout.toByteArray ();

    }

}
