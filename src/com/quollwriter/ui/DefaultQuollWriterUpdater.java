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
import com.quollwriter.ui.*;
import com.quollwriter.ui.components.*;

import org.jdom.*;

import static com.quollwriter.LanguageStrings.*;
import static com.quollwriter.uistrings.UILanguageStringsManager.getUIString;

public class DefaultQuollWriterUpdater implements QuollWriterUpdater
{

    private AbstractViewer viewer = null;
    private JProgressBar   progressBar = null;
    private boolean        stop = false;
    private Version        version = null;
    private long           size = 0;
    private byte[]         digest = null;
    private boolean        beta = false;
    private Notification   downloadNotification = null;
    private JTextPane      help = null;
    private JButton        cancel = null;

    public DefaultQuollWriterUpdater ()
    {

    }

    public URL getUpgradeURL (Version version)
                              throws Exception
    {

        String parms = "?version=" + version.getVersion ();

        return Environment.getSupportUrl (Constants.GET_UPGRADE_FILE_PAGE_PROPERTY_NAME,
                                          parms);

    }

    private URL getNewsAndVersionCheckURL ()
                                           throws Exception
    {

        String parms = "?";

        if (UserProperties.getAsBoolean (Constants.OPTIN_TO_BETA_VERSIONS_PROPERTY_NAME))
        {

            parms += "beta=true&";

        }

        String lastVersionCheckTime = UserProperties.get (Constants.LAST_VERSION_CHECK_TIME_PROPERTY_NAME);

        if (lastVersionCheckTime != null)
        {

            parms += "since=" + lastVersionCheckTime;

        }

        return Environment.getSupportUrl (Constants.GET_LATEST_VERSION_PAGE_PROPERTY_NAME,
                                          parms);

    }

    private void doDownload ()
    {

        final java.util.List<String> prefix = new ArrayList<> ();
        prefix.add (LanguageStrings.upgrade);
        prefix.add (LanguageStrings.download);

        final DefaultQuollWriterUpdater _this = this;

        this.progressBar = new JProgressBar ();

        this.progressBar.setAlignmentX (Component.LEFT_ALIGNMENT);

        int ind = 0;

        Box c = new Box (BoxLayout.Y_AXIS);

        this.help = UIUtils.createHelpTextPane (String.format (Environment.getUIString (prefix,
                                                                                        LanguageStrings.start),
                                                                //"Downloading upgrade file for new version: <b>%s</b>",
                                                               version),
                                                viewer);
        this.help.setAlignmentX (Component.LEFT_ALIGNMENT);
        this.help.setSize (new Dimension (500, 50));

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

        this.cancel = UIUtils.createButton (Environment.getUIString (LanguageStrings.buttons,
                                                                     LanguageStrings.cancel));
                                                            //"Cancel");

        pb.add (this.cancel);

        pb.add (Box.createHorizontalGlue ());

        this.cancel.addActionListener (new ActionAdapter ()
        {

            public void actionPerformed (ActionEvent ev)
            {

                _this.stop = true;

                _this.downloadNotification.removeNotification ();

            }

        });

        c.add (pb);

        this.downloadNotification = viewer.addNotification (c,
                                                    Constants.DOWNLOAD_ICON_NAME,
                                                    -1);

        this.downloadNotification.setOnRemove (new ActionListener ()
        {

            public void actionPerformed (ActionEvent ev)
            {

                _this.stop = true;

            }

        });

        File tf = null;

        try
        {

            tf = File.createTempFile ("QuollWriter-install-" + _this.version.getVersion () + ".",
                                      ".exe");

//            tf.deleteOnExit ();

        } catch (Exception e)
        {

            Environment.logError ("Unable to create temp file for: " +
                                  _this.version + ".exe",
                                  e);

            _this.showError (Environment.getUIString (prefix,
                                                      LanguageStrings.errors,
                                                      LanguageStrings.cantcreatetemporaryfile));
                                                      //"Unable to create the temporary file for the download.");

            return;

        }

        final File outFile = tf;

        final javax.swing.Timer watch = new javax.swing.Timer (750,
                                       new ActionListener ()
        {

            @Override
            public void actionPerformed (ActionEvent ev)
            {

                int length = (int) outFile.length ();

                float v = (float) length / (float) _this.size;

                float perc = v * 90f;

                _this.help.setText (String.format (Environment.getUIString (prefix,
                                                                            LanguageStrings.inprogress),
                                                                            //"Downloading upgrade file for new version <b>%s</b> - %s of %s bytes",
                                                   _this.version.getVersion (),
                                                   Environment.formatNumber (length),
                                                   Environment.formatNumber (size)));

                _this.setProgress ((int) perc);

                _this.help.setSize (new Dimension (500, _this.help.getPreferredSize ().height));
                _this.downloadNotification.validate ();
                _this.downloadNotification.repaint ();

              }

        });

        Runnable r = new Runnable ()
        {

            @Override
            public void run ()
            {

                try
                {

                    URL u = _this.getUpgradeURL (_this.version);

                    HttpURLConnection conn = (HttpURLConnection) u.openConnection ();

                    conn.setDoInput (true);
                    conn.setDoOutput (true);

                    conn.connect ();

                    int p = 0;

                    BufferedOutputStream out = new BufferedOutputStream (new FileOutputStream (outFile));

                    BufferedInputStream in = new BufferedInputStream (conn.getInputStream ());

                    byte[] buf = new byte[65536];

                    int bRead = -1;

                    while ((bRead = in.read (buf)) != -1)
                    {

                        if (_this.stop)
                        {

                            in.close ();
                            out.flush ();
                            out.close ();

                            return;

                        }

                        out.write (buf,
                                   0,
                                   bRead);

                    }

                    in.close ();
                    out.flush ();
                    out.close ();

                    _this.setProgress (90);

                    watch.stop ();

                    if (_this.stop)
                    {

                        return;

                    }

                    UIUtils.doLater (new ActionListener ()
                    {

                        @Override
                        public void actionPerformed (ActionEvent ev)
                        {

                            _this.help.setText (Environment.getUIString (LanguageStrings.upgrade,
                                                                         LanguageStrings.checkingfile));
                                                                                        //)"Checking the file..."));

                        }

                    });

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

                            md.update (_this.version.getVersion ().getBytes ());

                        }

                    }

                    dis.close ();

                    byte[] ddigest = md.digest ();

                    if (_this.stop)
                    {

                        return;

                    }

                    _this.setProgress (95);

                    // Digest calculated from download file is not the same as gained from the server.
                    String digestNotEqualError = Environment.getUIString (prefix,
                                                                          LanguageStrings.errors,
                                                                          LanguageStrings.digestinvalid);
                                                                          //"Digest calculated from download file is not the same as gained from the server.";

                    // Compare the digest with that gained from the server.
                    if (!Arrays.equals (ddigest,
                                        _this.digest))
                    {

                        throw new GeneralException (digestNotEqualError);

                    }

                    UIUtils.doLater (new ActionListener ()
                    {

                        @Override
                        public void actionPerformed (ActionEvent ev)
                        {

                            _this.help.setText (Environment.getUIString (prefix,
                                                                         LanguageStrings.complete));
                                                                         //"Download of upgrade file complete...");

                            _this.cancel.setVisible (false);

                        }

                    });

                    _this.setProgress (100);

                    _this.downloadNotification.removeNotification ();

                    Map<String, ActionListener> buts = new LinkedHashMap ();

                    buts.put (Environment.getUIString (LanguageStrings.upgrade,
                                                       LanguageStrings.restart,
                                                       LanguageStrings.buttons,
                                                       LanguageStrings.exitnow),
                                                       //"Yes, exit now",
                              new ActionListener ()
                    {

                        @Override
                        public void actionPerformed (ActionEvent ev)
                        {

                            _this.closeDown ();

                        }

                    });

                    buts.put (Environment.getUIString (LanguageStrings.upgrade,
                                                       LanguageStrings.restart,
                                                       LanguageStrings.buttons,
                                                       LanguageStrings.exitlater),
                                                       //"No, I'll exit later",
                              new ActionListener ()
                    {

                        @Override
                        public void actionPerformed (ActionEvent ev)
                        {

                            // Do nothing.

                        }

                    });

                    UIUtils.createQuestionPopup (_this.viewer,
                                                 Environment.getUIString (LanguageStrings.upgrade,
                                                                          LanguageStrings.restart,
                                                                          LanguageStrings.title),
                                                 //"Restart Quoll Writer?",
                                                 null,
                                                 String.format (getUIString (upgrade,restart,text),
                                                                //"Version <b>%s</b> of Quoll Writer has been downloaded.  To complete the upgrade you must exit Quoll Writer, the installer will then guide you through the rest of the upgrade process.<br /><br />Would you like to exit now?",
                                                                _this.version),
                                                 buts,
                                                 null,
                                                 null);

                    Environment.addDoOnShutdown (new Runnable ()
                    {

                        @Override
                        public void run ()
                        {

                            try
                            {

                                java.util.List args = new ArrayList ();
                                args.add (outFile.getPath ());

                                ProcessBuilder pb = new ProcessBuilder (args);
                                pb.start ();

                                // TODO Check that the JVM is exiting...

                            } catch (Exception e) {

                                Environment.logError ("Unable to run upgrade file: " +
                                                      outFile.getPath (),
                                                      e);

                            }

                        }

                    });

                } catch (Exception e)
                {

                    Environment.logError ("Unable to download the upgrade file",
                                          e);

                    _this.showError (Environment.getUIString (prefix,
                                                              LanguageStrings.errors,
                                                              LanguageStrings.unabletodownload));
                                                              //"Unable to download the upgrade file.");

                }

            }

        };

        Thread t = new Thread (r);

        t.setDaemon (true);
        t.setPriority (Thread.MIN_PRIORITY);

        t.start ();

        watch.start ();

    }

    public void doUpdate (AbstractViewer viewer)
    {

        final DefaultQuollWriterUpdater _this = this;

        this.viewer = viewer;

        try
        {

            URL u = this.getNewsAndVersionCheckURL ();

            HttpURLConnection conn = (HttpURLConnection) u.openConnection ();

            conn.setDoInput (true);
            conn.setDoOutput (true);

            conn.connect ();

            BufferedInputStream bin = new BufferedInputStream (conn.getInputStream ());

            ByteArrayOutputStream bout = new ByteArrayOutputStream ();

            IOUtils.streamTo (bin,
                              bout,
                              8192);

            UserProperties.set (Constants.LAST_VERSION_CHECK_TIME_PROPERTY_NAME,
                                String.valueOf (System.currentTimeMillis ()));

            String info = new String (bout.toByteArray (),
                                      "utf-8");

            // Should be json.
            Map data = (Map) JSONDecoder.decode (info);

            Map version = (Map) data.get ("version");

            if (version != null)
            {

                this.version = new Version ((String) version.get ("version"));

                this.size = ((Number) version.get ("size")).longValue ();

                this.digest = com.quollwriter.Base64.decode ((String) version.get ("digest"));

                if (Environment.getQuollWriterVersion ().isNewer (this.version))
                {

                    UIUtils.doLater (new ActionListener ()
                    {

                        public void actionPerformed (ActionEvent ev)
                        {

                            Box ib = new Box (BoxLayout.Y_AXIS);
                            ib.setMaximumSize (new Dimension (Short.MAX_VALUE,
                                                              Short.MAX_VALUE));

                            JTextPane p = UIUtils.createHelpTextPane (String.format (Environment.getUIString (LanguageStrings.upgrade,
                                                                                                              LanguageStrings.newversionavailable,
                                                                                                              LanguageStrings.text),
                                                                                    //"A new version of %s is available.  <a href='help:version-changes/%s'>View the changes.</a>",
                                                                                    // Constants.QUOLL_WRITER_NAME,
                                                                                     _this.version.getVersion ().replace (".",
                                                                                                                          "_")),
                                                                      viewer);

                            p.setAlignmentX (Component.LEFT_ALIGNMENT);

                            p.setMaximumSize (new Dimension (Short.MAX_VALUE,
                                                             Short.MAX_VALUE));
                            p.setBorder (null);

                            ib.add (p);
                            ib.add (Box.createVerticalStrut (5));

                            JButton installNow = UIUtils.createButton (Environment.getUIString (LanguageStrings.upgrade,
                                                                                                LanguageStrings.newversionavailable,
                                                                                                LanguageStrings.buttons,
                                                                                                LanguageStrings.download));
                                                                                       //"Download now");
                            JButton installLater = UIUtils.createButton (Environment.getUIString (LanguageStrings.upgrade,
                                                                                                  LanguageStrings.newversionavailable,
                                                                                                  LanguageStrings.buttons,
                                                                                                  LanguageStrings.later));
                                                                                       //"Later");

                            Box bb = new Box (BoxLayout.X_AXIS);
                            bb.add (installNow);
                            bb.add (Box.createHorizontalStrut (5));
                            bb.add (installLater);
                            bb.setAlignmentX (Component.LEFT_ALIGNMENT);

                            ib.add (bb);
                            ib.add (Box.createVerticalStrut (5));

                            final ActionListener hasVersionNotification = _this.viewer.addNotification (ib,
                                                                                                        "notify",
                                                                                                        600);

                            installNow.addActionListener (new ActionAdapter ()
                            {

                                public void actionPerformed (ActionEvent ev)
                                {

                                    hasVersionNotification.actionPerformed (ev);

                                    _this.doDownload ();

                                }

                            });

                            installLater.addActionListener (hasVersionNotification);

                        }

                    });

                }

            }

        } catch (Exception e)
        {

            Environment.logError ("Unable to perform update check",
                                  e);

        }

    }

    private void showError (String m)
    {

        UIUtils.showErrorMessage (this.viewer,
                                  m);

        this.downloadNotification.removeNotification ();

    }

    private void showGeneralError ()
    {

        this.showError (String.format (getUIString (LanguageStrings.upgrade,
                                                                LanguageStrings.download,
                                                                LanguageStrings.errors,
                                                                LanguageStrings.general),
                                            //"Unable to download/install the new version.  <a href='%s'>Click here to download the latest version from the Quoll Writer website</a>",
                                       UserProperties.get (Constants.QUOLLWRITER_DOWNLOADS_URL_PROPERTY_NAME)));

       this.downloadNotification.removeNotification ();

    }

    public void setProgress (int p)
    {

        final DefaultQuollWriterUpdater _this = this;

        UIUtils.doLater (new ActionListener ()
        {

            @Override
            public void actionPerformed (ActionEvent ev)
            {

                _this.progressBar.setValue (p);
                _this.downloadNotification.validate ();
                _this.downloadNotification.repaint ();

            }

        });

    }

    private void closeDown ()
    {

        final DefaultQuollWriterUpdater _this = this;

        // TODO Environment.doForOpenViewers -> close ()
/*
TODO
        Map<ProjectInfo, AbstractProjectViewer> open = Environment.getOpenProjects ();

        // Get the first, close it.
        if (open.size () > 0)
        {

            AbstractProjectViewer pv = open.values ().iterator ().next ();

            pv.setState (java.awt.Frame.NORMAL);
            pv.toFront ();

            pv.close (false,
                      new ActionListener ()
                      {

                          @Override
                          public void actionPerformed (ActionEvent ev)
                          {

                              _this.closeDown ();

                          }

                      });

            return;

        } else {

            if (Environment.getAllProjectsViewer () != null)
            {

                Environment.getAllProjectsViewer ().close (null);

            } else {

                Environment.closeDown ();

            }

        }
*/

    }

}
