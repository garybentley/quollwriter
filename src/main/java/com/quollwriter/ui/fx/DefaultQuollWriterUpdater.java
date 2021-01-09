package com.quollwriter.ui.fx;

import java.io.*;

import java.net.*;

import java.security.*;

import java.util.*;
import java.util.concurrent.*;
import java.util.zip.*;

import javafx.beans.property.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;

//import com.gentlyweb.properties.*;

import com.quollwriter.*;

import com.quollwriter.data.*;
import com.quollwriter.data.comparators.*;
import com.quollwriter.ui.fx.*;
import com.quollwriter.ui.fx.components.*;
import com.quollwriter.ui.fx.viewers.*;

import static com.quollwriter.LanguageStrings.*;
import static com.quollwriter.uistrings.UILanguageStringsManager.getUILanguageStringProperty;

public class DefaultQuollWriterUpdater implements QuollWriterUpdater
{

    private AbstractViewer viewer = null;
    private ControllableProgressBar   progressBar = null;
    private boolean        stop = false;
    private Version        version = null;
    private long           size = 0;
    private String         url = null;
    private byte[]         digest = null;
    private boolean        beta = false;
    private Notification   downloadNotification = null;
    private QuollTextView  help = null;
    private Button         cancel = null;
    private ScheduledFuture watch = null;
    private String          fileExt = null;

    public DefaultQuollWriterUpdater ()
    {

    }

    public URL getUpgradeURL (Version version)
                              throws Exception
    {

        String parms = "?version=" + version.getVersion () + "&platform=" + Environment.getOSPlatform ();

        return Environment.getSupportUrl (Constants.GET_UPGRADE_FILE_PAGE_PROPERTY_NAME,
                                          parms);

    }

    private URL getNewsAndVersionCheckURL ()
                                           throws Exception
    {

        Map<String, String> parms = new HashMap<> ();

        if (UserProperties.getAsBoolean (Constants.OPTIN_TO_BETA_VERSIONS_PROPERTY_NAME))
        {

            parms.put ("beta",
                       "true");

        }

        String lastVersionCheckTime = UserProperties.get (Constants.LAST_VERSION_CHECK_TIME_PROPERTY_NAME);

        if (lastVersionCheckTime != null)
        {

            parms.put ("since",
                       lastVersionCheckTime);

        }

        parms.put ("platform",
                   Environment.getOSPlatform ());
        parms.put ("currVersion",
                   Environment.getQuollWriterVersion ().getVersion ());

        return Environment.getSupportUrl (Constants.GET_LATEST_VERSION_PAGE_PROPERTY_NAME,
                                          Utils.encodeParms (parms));

    }

    private void doDownload ()
    {

        final List<String> prefix = Arrays.asList (upgrade,download);

        final DefaultQuollWriterUpdater _this = this;

        this.progressBar = ControllableProgressBar.builder ()
            //.allowStop (true)
            //.stopTooltip (getUILanguageStringProperty (buttons,cancel))
            .build ();

        int ind = 0;

        VBox b = new VBox ();
        this.help = QuollTextView.builder ()
            .text (getUILanguageStringProperty (Utils.newList (prefix,start),
                                                version))
            .inViewer (this.viewer)
            .build ();
        b.getChildren ().addAll (this.help, this.progressBar);

        this.progressBar.stateProperty ().addListener ((pr, oldv, newv) ->
        {

            this.stop = true;
            this.downloadNotification.removeNotification ();

        });

        this.downloadNotification = this.viewer.addNotification (b,
                                                                 StyleClassNames.DOWNLOAD,
                                                                 -1);

        this.downloadNotification.setOnRemove (() ->
        {

            this.stop = true;

        });

        File tf = null;

        try
        {

            tf = File.createTempFile ("QuollWriter-install-" + _this.version.getVersion () + ".",
                                      this.fileExt);

            //tf.deleteOnExit ();

        } catch (Exception e)
        {

            Environment.logError ("Unable to create temp file for: " +
                                  this.version,
                                  e);

            this.showError (getUILanguageStringProperty (Utils.newList (prefix,errors,cantcreatetemporaryfile)));
                                                      //"Unable to create the temporary file for the download.");

            return;

        }

        final File outFile = tf;

        this.watch = Environment.schedule (() ->
        {

            if (this.stop)
            {

                this.watch.cancel (true);

            }

            int length = (int) outFile.length ();

            double v = (double) length / (double) _this.size;

            //double perc = v * 90f;

            UIUtils.runLater (() ->
            {

                this.help.setText (getUILanguageStringProperty (Utils.newList (prefix,inprogress),
                                   this.version.getVersion (),
                                   Environment.formatNumber (length),
                                   Environment.formatNumber (size)));

                this.setProgress (v);

            });

        },
        750,
        750);

        Environment.scheduleImmediately (() ->
        {

            try
            {

                URL u = new URL (this.url);
                //this.getUpgradeURL (this.version);

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

                }

                in.close ();
                out.flush ();
                out.close ();

                this.setProgress (90);

                watch.cancel (true);

                if (this.stop)
                {

                    return;

                }

                UIUtils.runLater (() ->
                {

                    this.help.setText (getUILanguageStringProperty (upgrade,checkingfile));

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

                this.setProgress (95);

                // Digest calculated from download file is not the same as gained from the server.
                StringProperty digestNotEqualError = getUILanguageStringProperty (Utils.newList (prefix,errors,digestinvalid));
                                                                      //"Digest calculated from download file is not the same as gained from the server.";

                // Compare the digest with that gained from the server.
                if (!Arrays.equals (ddigest,
                                    this.digest))
                {

                    throw new GeneralException (digestNotEqualError.getValue ());

                }

                UIUtils.runLater (() ->
                {

                    this.help.setText (getUILanguageStringProperty (Utils.newList (prefix,complete)));
                                                                 //"Download of upgrade file complete...");

                    //this.cancel.setVisible (false);
                    this.downloadNotification.removeNotification ();

                    this.setProgress (100);

                    QuollButton nb = QuollButton.builder ()
                        .label (upgrade,restart,buttons,exitnow)
                        .build ();

                    QuollButton lb = QuollButton.builder ()
                        .label (upgrade,restart,buttons,exitlater)
                        .build ();

                    QuollPopup qp = QuollPopup.messageBuilder ()
                        .title (getUILanguageStringProperty (upgrade,restart,title))
                        .message (getUILanguageStringProperty (Arrays.asList (upgrade,restart,text),
                                                               this.version))
                        .button (nb)
                        .button (lb)
                        .build ();

                    nb.setOnAction (ev ->
                    {

                        qp.close ();

                        this.closeDown ();

                    });

                    lb.setOnAction (ev ->
                    {

                        qp.close ();

                    });

                });

                Environment.addDoOnShutdown (() ->
                {

                    try
                    {

                        List<String> args = new ArrayList<> ();

                        if (Environment.OsCheck.getOperatingSystemType () == Environment.OsCheck.OSType.MacOS)
                        {

                            args.add ("open");

                        }

                        if (Environment.OsCheck.getOperatingSystemType () == Environment.OsCheck.OSType.Linux)
                        {

                            args.add ("xdg-open");

                        }

                        args.add (outFile.getPath ());

                        ProcessBuilder pb = new ProcessBuilder (args);
                        pb.start ();

                        // TODO Check that the JVM is exiting...

                    } catch (Exception e) {

                        Environment.logError ("Unable to run upgrade file: " +
                                              outFile.getPath (),
                                              e);

                    }

                });

            } catch (Exception e)
            {

                Environment.logError ("Unable to download the upgrade file",
                                      e);

                this.showError (getUILanguageStringProperty (Utils.newList (prefix,errors,unabletodownload)));
                                                          //"Unable to download the upgrade file.");

            }

        });

    }

    public void doUpdate (AbstractViewer viewer)
    {

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

            Utils.streamTo (bin,
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

                this.url = (String) version.get ("url");

                this.fileExt = this.url.substring (this.url.lastIndexOf ("."));

                this.size = ((Number) version.get ("size")).longValue ();

                this.digest = com.quollwriter.Base64.decode ((String) version.get ("digest"));

                if (Environment.getQuollWriterVersion ().isNewer (this.version))
                {

                    UIUtils.runLater (() ->
                    {

                        QuollButton dl = QuollButton.builder ()
                            .label (getUILanguageStringProperty (upgrade,newversionavailable,buttons,download))
                            .buttonType (ButtonBar.ButtonData.APPLY)
                            .build ();

                        QuollButton later = QuollButton.builder ()
                            .label (getUILanguageStringProperty (upgrade,newversionavailable,buttons,LanguageStrings.later))
                            .buttonType (ButtonBar.ButtonData.CANCEL_CLOSE)
                            .build ();

                        QuollButtonBar bb = QuollButtonBar.builder ()
                            .button (dl)
                            .button (later)
                            .build ();

                        VBox v = new VBox ();
                        v.getChildren ().add (QuollTextView.builder ()
                            .text (getUILanguageStringProperty (Arrays.asList (upgrade,newversionavailable,text),
                                                                this.version.getVersion ().replace (".",
                                                                                                    "-")))
                            .build ());

                        v.getChildren ().add (bb);

                        Notification n = this.viewer.addNotification (v,
                                                                      StyleClassNames.DOWNLOAD,
                                                                      90);

                        later.setOnAction (ev ->
                        {

                            n.removeNotification ();

                        });

                        dl.setOnAction (ev ->
                        {

                            this.doDownload ();
                            n.removeNotification ();

                        });

                    });

                }

            }

        } catch (Exception e)
        {

            Environment.logError ("Unable to perform update check",
                                  e);

        }

    }

    private void showError (StringProperty m)
    {

        UIUtils.runLater (() ->
        {

            ComponentUtils.showErrorMessage (this.viewer,
                                             m);

            this.downloadNotification.removeNotification ();

        });

    }

    private void showGeneralError ()
    {

        this.showError (getUILanguageStringProperty (Arrays.asList (upgrade,download,errors,general),
                                            //"Unable to download/install the new version.  <a href='%s'>Click here to download the latest version from the Quoll Writer website</a>",
                                                     UserProperties.get (Constants.QUOLLWRITER_DOWNLOADS_URL_PROPERTY_NAME)));

       this.downloadNotification.removeNotification ();

    }

    public void setProgress (double p)
    {

        UIUtils.runLater (() ->
        {

            this.progressBar.setProgress (p);

        });

    }

    private void closeDown ()
    {

        final DefaultQuollWriterUpdater _this = this;

        Environment.getOpenViewers ().stream ()
            .forEach (pv -> pv.close (null));

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
