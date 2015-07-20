package com.quollwriter.ui;

import java.awt.*;
import java.awt.event.*;

import java.io.*;

import java.net.*;

import java.security.*;

import java.util.*;
import java.util.zip.*;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;

import com.gentlyweb.properties.*;

import com.gentlyweb.utils.*;

import com.gentlyweb.xml.*;

import com.jgoodies.forms.builder.*;
import com.jgoodies.forms.factories.*;
import com.jgoodies.forms.layout.*;

import com.quollwriter.*;

import com.quollwriter.data.*;
import com.quollwriter.data.comparators.*;

import com.quollwriter.ui.components.*;

import org.jdom.*;


public class GetLatestVersion implements Runnable
{

    private AbstractProjectViewer projectViewer = null;
    private JProgressBar          progressBar = null;
    private boolean               stop = false;
    private Version               version = null;
    private long                  size = 0;
    private byte[]                digest = null;
    private boolean               beta = false;
    private Notification          notification = null;
    private JTextPane             help = null;
    private JButton               cancel = null;

    public GetLatestVersion(AbstractProjectViewer pv,
                            Version               version,
                            long                  size,
                            String                digest)
    {

        final GetLatestVersion _this = this;

        this.projectViewer = pv;

        this.progressBar = new JProgressBar ();

        this.progressBar.setAlignmentX (Component.LEFT_ALIGNMENT);
        
        int ind = 0;

        this.version = version;

        this.digest = com.quollwriter.Base64.decode (digest);

        this.size = size;
        
        Box c = new Box (BoxLayout.Y_AXIS);

        this.help = UIUtils.createHelpTextPane (String.format ("Downloading upgrade file for new version: <b>%s</b>",
                                                               version),
                                                  pv);
        this.help.setAlignmentX (Component.LEFT_ALIGNMENT);

        this.help.setMaximumSize (new Dimension (Short.MAX_VALUE,
                                                 Short.MAX_VALUE));
        this.help.setBorder (null);
        
        c.add (this.help);
        
        c.add (Box.createVerticalStrut (5));

        Box pb = new Box (BoxLayout.X_AXIS);
        
        pb.setAlignmentX (Component.LEFT_ALIGNMENT);
        pb.add (this.progressBar);
        
        pb.add (Box.createHorizontalStrut (5));
                
        this.progressBar.setPreferredSize (new Dimension (500,
                                                          20));
        this.progressBar.setMaximumSize (new Dimension (500,
                                                        20));
                
        this.cancel = new JButton ("Cancel");

        pb.add (this.cancel);
        
        pb.add (Box.createHorizontalGlue ());

        this.cancel.addActionListener (new ActionAdapter ()
        {

            public void actionPerformed (ActionEvent ev)
            {

                _this.stop = true;
            
                _this.notification.removeNotification ();

            }

        });
        
        c.add (pb);        
        
        this.notification = pv.addNotification (c,
                                                Constants.DOWNLOAD_ICON_NAME,
                                                -1);
        
        this.notification.setOnRemove (new ActionListener ()
        {
           
            public void actionPerformed (ActionEvent ev)
            {
                
                _this.stop = true;
                
            }
            
        });
                
    }

    public void start ()
    {
        
        Thread t = new Thread (this);

        t.setDaemon (true);
        t.start ();

    }

    private void showError (String m)
    {

        UIUtils.showErrorMessage (this.projectViewer,
                                  m);

        this.notification.removeNotification ();
                                  
    }

    private void showGeneralError ()
    {
        
        this.showError (String.format ("Unable to download/install the new version.  <a href='%s'>Click here to download the latest version from the Quoll Writer website</a>",
                                       Environment.getProperty (Constants.QUOLLWRITER_DOWNLOADS_URL_PROPERTY_NAME)));
                                       
    }
    
    private void showUnableToInstallError ()
    {
        
        this.showError ("Unable to install new version, please contact Quoll Writer support for assistance.");
        
    }
    
    public void run ()
    {
        
        // .zip
        String fileSuff = ".zip";

        File tf = null;

        try
        {

            tf = File.createTempFile (this.version.getVersion (),
                                      fileSuff);

            tf.deleteOnExit ();

        } catch (Exception e)
        {

            Environment.logError ("Unable to create temp file for: " +
                                  this.version + fileSuff,
                                  e);

            this.showGeneralError ();
                        
            return;

        }

        final File outFile = tf;

        try
        {

            URL u = Environment.getUpgradeURL (this.version);

            HttpURLConnection conn = (HttpURLConnection) u.openConnection ();

            conn.setDoInput (true);
            conn.setDoOutput (true);

            conn.connect ();

            BufferedOutputStream out = new BufferedOutputStream (new FileOutputStream (outFile));

            BufferedInputStream in = new BufferedInputStream (conn.getInputStream ());

            final GetLatestVersion _this = this;

            byte[] buf = new byte[8192];

            int bRead = -1;

            while ((bRead = in.read (buf)) != -1)
            {

                if (this.stop)
                {

                    in.close ();
                    out.flush ();
                    out.close ();

                    return;

                }

                out.write (buf,
                           0,
                           bRead);

                if (this.size > 0)
                {

                    // Update the progress bar.
                    UIUtils.doLater (new ActionListener ()
                    {
                        
                        @Override
                        public void actionPerformed (ActionEvent ev)
                        {
                        
                            int length = (int) outFile.length ();

                            _this.help.setText (String.format ("Downloading upgrade file for new version <b>%s</b> - %s of %s bytes",
                                                               version,
                                                               Environment.formatNumber (length),
                                                               Environment.formatNumber (size)));
                            
                            float v = (float) length / (float) _this.size;

                            float perc = v * 90f;

                            _this.setProgress ((int) perc);

                        }

                    });

                }

            }

            in.close ();
            out.flush ();
            out.close ();

            if (this.stop)
            {

                return;

            }

            // Calculate a hash code.
            MessageDigest md = MessageDigest.getInstance ("SHA-256");

            DigestInputStream dis = new DigestInputStream (new BufferedInputStream (new FileInputStream (outFile)),
                                                           md);

            buf = new byte[65536];

            bRead = -1;

            String ll = outFile.length () + "";

            ll = ll.substring (ll.length () - 1);

            int v = 0;

            try
            {

                v = Integer.parseInt (ll);

            } catch (Exception e)
            {
            }

            boolean odd = ((v % 2) == 0);

            while ((bRead = dis.read (buf)) != -1)
            {

                if (odd)
                {

                    md.update (ll.getBytes ());

                } else
                {

                    md.update (this.version.getVersion ().getBytes ());

                }

            }

            dis.close ();

            byte[] ddigest = md.digest ();

            if (this.stop)
            {

                return;

            }

            this.setProgress (95);

            // Digest calculated from download file is not the same as gained from the server.
            String digestNotEqualError = "Digest calculated from download file is not the same as gained from the server.";

            // Compare the digest with that gained from the server.
            if (!Arrays.equals (ddigest,
                                this.digest))
            {

                throw new GeneralException (digestNotEqualError);

            }

            this.help.setText ("Installing new version...");
            
            this.cancel.setVisible (false);
            
            ZipFile zf = new ZipFile (outFile);

            Enumeration<? extends ZipEntry> en = zf.entries ();

            File path = Environment.getQuollWriterJarsDir ();
            
            if (path == null)
            {
            
                this.showUnableToInstallError ();

                return;

            }

            // Create the .new directory.
            File newDir = new File (path + "/.new");
            
            if (!newDir.exists ())
            {
            
                newDir.mkdirs ();
                
            }
            
            if (!newDir.exists ())
            {
                
                this.showUnableToInstallError ();
                
                return;
             
            }
            
            ZipEntry ze = null;
            File     newFile = null;

            try
            {

                while (en.hasMoreElements ())
                {

                    ze = en.nextElement ();
                    
                    newFile = new File (newDir + "/" + ze.getName ());

                    BufferedOutputStream bout = new BufferedOutputStream (new FileOutputStream (newFile));

                    BufferedInputStream bin = new BufferedInputStream (zf.getInputStream (ze));

                    IOUtils.streamTo (bin,
                                      bout,
                                      8192);

                    bout.flush ();
                    bout.close ();
                    bin.close ();

                }

            } catch (Exception e)
            {

                Environment.logError ("Unable to copy new file: " +
                                      ze.getName () +
                                      " to: " +
                                      newFile,
                                      e);

                this.showUnableToInstallError ();

            }

            this.setProgress (100);
                                                
            this.notification.removeNotification ();
                                                 
            UIUtils.showMessage ((PopupsSupported) this.projectViewer,
                                 "Upgrade complete",
                                 String.format ("Quoll Writer has been upgraded to version: <b>%s</b>.\n\nThe changes will be available once you restart Quoll Writer.",
                                                this.version));

            Environment.setUpgradeRequired ();
                                 
        } catch (Exception e)
        {

            Environment.logError ("Unable to download the upgrade file",
                                  e);

            this.showGeneralError ();

        } 

    }

    public void setProgress (int p)
    {

        this.progressBar.setValue (p);

    }

}
