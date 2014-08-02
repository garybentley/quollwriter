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


public class GetLatestVersion extends PopupWindow implements Runnable
{

    private AbstractProjectViewer projectViewer = null;
    private JProgressBar          progressBar = null;
    private boolean               stop = false;
    private String                version = null;
    private int                   size = 0;
    private byte[]                digest = null;
    private JButton               cancelButton = null;
    private String                windowTitle = null;
    private String                headerText = null;
    private String                headerTitle = null;
    private String                headerIcon = null;
    private String                helpText = null;
    private String                cancelButtonText = null;
    private String                fileText = null;

    public GetLatestVersion(AbstractProjectViewer pv,
                            String                version,
                            String                size,
                            String                digest)
    {

        super (pv);

        final GetLatestVersion _this = this;

        this.addWindowListener (new WindowAdapter ()
            {

                public void windowClosing (WindowEvent ev)
                {

                    _this.stop = true;

                }

            });

        this.progressBar = new JProgressBar ();

        int ind = 0;

        this.version = version;
        this.windowTitle = "Download Latest Version";
        this.headerText = "Upgrading to New Version";
        this.headerIcon = "install";
        this.helpText = "Version" + version + " of Quoll Writer is now being downloaded and installed.";
        this.fileText = "Downloading file";
        this.cancelButtonText = "Cancel";

        this.digest = com.quollwriter.Base64.decode (digest);

        try
        {

            this.size = Integer.parseInt (size);

        } catch (Exception e)
        {

            // Version is wrong?
            Environment.logError ("Size: " +
                                  size +
                                  " is invalid, setting indeterminate",
                                  e);

            this.progressBar.setIndeterminate (true);

        }

    }

    public String getWindowTitle ()
    {

        return this.windowTitle;

    }

    public String getHeaderTitle ()
    {

        return this.headerTitle;

    }

    public String getHeaderIconType ()
    {

        return this.headerIcon;

    }

    public String getHelpText ()
    {

        return this.helpText;

    }

    public JComponent getContentPanel ()
    {

        Box c = new Box (BoxLayout.PAGE_AXIS);
        c.setBorder (new EmptyBorder (0,
                                      10,
                                      0,
                                      10));

        c.add (new JLabel (this.fileText));

        c.add (Box.createVerticalStrut (10));

        /*
        this.progressBar.setPreferredSize (new Dimension (image.getWidth (null) - 29,
                                                          this.progressBar.getPreferredSize ().height));
         */
        c.add (this.progressBar);

        return c;

    }

    public JButton[] getButtons ()
    {

        final GetLatestVersion _this = this;

        this.cancelButton = new JButton ();
        this.cancelButton.setText (this.cancelButtonText);

        this.cancelButton.addActionListener (new ActionAdapter ()
            {

                public void actionPerformed (ActionEvent ev)
                {

                    _this.stop = true;

                    _this.close ();

                }

            });

        JButton[] buts = { this.cancelButton };

        return buts;

    }

    public void init ()
    {

        super.init ();

        Thread t = new Thread (this);

        t.setDaemon (true);
        t.start ();

    }

    private void showError (String m)
    {

        UIUtils.showErrorMessage (this,
                                  m);

        this.close ();

    }

    public void run ()
    {
        
        String errM = "Unable to download/install the new version, please visit the Quoll Writer website\n   " + Environment.getProperty (Constants.QUOLLWRITER_DOWNLOADS_URL_PROPERTY_NAME) + "\nto manually download/install the latest version.";
        String versionTag = Constants.VERSION_TAG;
        String cannotDownloadError = "Unable to download the upgrade file from: ";

        // .zip
        String fileSuff = ".zip";

        // Unable to create temp file for:
        String tempFileCreateError = "Unable to create temp file for: "; 

        File tf = null;

        try
        {

            tf = File.createTempFile (this.version,
                                      fileSuff);

            tf.deleteOnExit ();

        } catch (Exception e)
        {

            Environment.logError (tempFileCreateError +
                                  this.version + fileSuff,
                                  e);

            this.showError (errM);

            UIUtils.openURL (this,
                             Environment.getProperty (Constants.QUOLLWRITER_DOWNLOADS_URL_PROPERTY_NAME));
                        
            return;

        }

        final File outFile = tf;

        try
        {

            URL u = Environment.getSupportUrl (Constants.GET_UPGRADE_FILE_PAGE_PROPERTY_NAME);

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
                    SwingUtilities.invokeLater (new Runner ()
                        {

                            public void run ()
                            {

                                int length = (int) outFile.length ();

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

                this.close ();

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

                    md.update (this.version.getBytes ());

                }

            }

            dis.close ();

            byte[] ddigest = md.digest ();

            if (this.stop)
            {

                this.close ();

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

            this.cancelButton.setEnabled (false);

            ZipFile zf = new ZipFile (outFile);

            Enumeration<? extends ZipEntry> en = zf.entries ();

            String unableToInstallError = "Unable to install, please contact support for assistance.";

            // Unable to copy new file:
            String cantCopyFilePref = "Unable to copy new file: ";

            // to:
            String cantCopyFileSuff = " to: ";

            File path = Environment.getQuollWriterJarsDir ();
            
            if (path == null)
            {
            
                this.showError (unableToInstallError);

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
                
                this.showError (unableToInstallError);
                
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

                Environment.logError (cantCopyFilePref +
                                      ze.getName () +
                                      cantCopyFileSuff +
                                      newFile,
                                      e);

                this.showError (unableToInstallError);

            }

            this.setProgress (100);

            this.cancelButton.setEnabled (true);

            String closeButtonText = "Close";
            this.cancelButton.setText (closeButtonText);

            String message = "Quoll Writer has been successfully upgraded to version: [[VERSION]].\n\nThe changes will be available once you restart Quoll Writer.";

            message = StringUtils.replaceString (message,
                                                 Constants.VERSION_TAG,
                                                 this.version);

            UIUtils.showMessage (this,
                                 message);

            Environment.setUpgradeRequired ();
                                 
            this.close ();

        } catch (Exception e)
        {

            Environment.logError (cannotDownloadError,
                                  e);

            this.showError (errM);

        } finally
        {
            /*
            if (outFile != null)
            {

            outFile.delete ();

            }
             */
        }

    }

    public void setProgress (int p)
    {

        this.progressBar.setValue (p);

    }

}
