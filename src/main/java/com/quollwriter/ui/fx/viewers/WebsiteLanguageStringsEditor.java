package com.quollwriter.ui.fx.viewers;

import java.io.*;
import java.util.*;
import java.util.function.*;
import java.util.concurrent.*;
import java.text.*;
import java.net.*;
import java.nio.file.*;

//import com.gentlyweb.properties.*;

import org.josql.*;

import javafx.beans.binding.*;
import javafx.scene.layout.*;
import javafx.scene.control.*;
import javafx.beans.property.*;

import com.quollwriter.*;
import com.quollwriter.events.*;
import com.quollwriter.data.*;
import com.quollwriter.data.comparators.*;
import com.quollwriter.data.editors.*;
import com.quollwriter.editors.*;
import com.quollwriter.ui.fx.components.*;
import com.quollwriter.ui.fx.*;
import com.quollwriter.ui.fx.panels.*;
import com.quollwriter.text.*;
import com.quollwriter.db.*;
import com.quollwriter.ui.fx.sidebars.*;
import com.quollwriter.ui.fx.components.QSpellChecker;
import com.quollwriter.uistrings.*;

import static com.quollwriter.uistrings.UILanguageStringsManager.getUILanguageStringProperty;
import static com.quollwriter.LanguageStrings.*;

public class WebsiteLanguageStringsEditor extends AbstractLanguageStringsEditor<WebsiteLanguageStrings, WebsiteLanguageStrings>
{

    private WebsiteLanguageStrings prevEnglishStrings = null;
    private StringProperty titleProperty = null;

    public WebsiteLanguageStringsEditor (WebsiteLanguageStrings userStrings)
			                      throws Exception
    {

        super (userStrings);

        this.titleProperty = new SimpleStringProperty ();
        this.titleProperty.bind (Bindings.createStringBinding (() ->
        {

            return String.format ("Edit Website Language Strings for: %s",
                                  this.userStrings.getNativeName ());

        },
        this.userStrings.nativeNameProperty ()));

    }

    @Override
    public void onForwardLabelClicked ()
                                throws Exception
    {
/*
TODO?
        if ((this.nodeFilter != null)
            &&
            (this.prevEnglishStrings != null)
           )
        {

            this.showAllStrings ();

            this.showForwardLabel (new SimpleStringProperty (String.format ("Click to show what's changed/new between version <b>%1$s</b> and <b>%2$s</b>.",
                                                                            this.baseStrings.getStringsVersion (),
                                                                            this.prevEnglishStrings.getStringsVersion ())));

        } else {

            this.limitViewToPreviousVersionDiff ();

        }
*/
    }

    @Override
    public void tryOut ()
    {

        final WebsiteLanguageStringsEditor _this = this;

        try
        {

            this.save ();

        } catch (Exception e) {

             Environment.logError ("Unable to save: " + this.userStrings,
                                   e);

             ComponentUtils.showErrorMessage (this,
                                              new SimpleStringProperty ("Unable to save strings."));

             return;

        }

        int c = 0;

        for (Value uv : this.userStrings.getAllValues ())
        {

             Value bv = this.baseStrings.getValue (uv.getId ());

             if (bv == null)
             {

                 // The string is present in the user strings but not the base!
                 Environment.logError ("Found string: " + uv.getId () + " present in user strings but not base.");

                 continue;

             }

             c += uv.getErrors (this).size ();

        }

        if (c > 0)
        {

            this.showMessage ("Errors found in strings",
                              String.format ("Sorry, there are <b>%s</b> errors that must be corrected before you can submit the strings.",
                                             Environment.formatNumber (c)));

             return;

        }

        Path _outputFile = null;

        try
        {

            _outputFile = _this.createUploadFile ();

        } catch (Exception e)
        {

             Environment.logError ("Unable to create upload file for strings: " + _this.userStrings,
                                   e);

             ComponentUtils.showErrorMessage (_this,
                                              new SimpleStringProperty ("Unable to upload strings."));

             return;

        }

        final Path outputFile = _outputFile;

        Map<String, String> headers = new HashMap<> ();

        String submitterid = UserProperties.get (Constants.WEBSITE_LANGUAGE_STRINGS_SUBMITTER_ID_PROPERTY_NAME);

        if (submitterid != null)
        {

             headers.put (Constants.WEBSITE_LANGUAGE_STRINGS_SUBMITTER_ID_HEADER_NAME,
                          submitterid);

        }

        this.showMessage ("Uploading strings",
                          "Your strings/images will now be uploaded to the server.  Note: your strings/images will be deleted after 2 hours.");

        URL u = null;

        try
        {

             u = new URL (Environment.getQuollWriterWebsite () + UserProperties.get (Constants.TRYOUT_WEBSITE_LANGUAGE_STRINGS_URL_PROPERTY_NAME));

             QuollPopup pp = this.showProgressPopup ("Uploading",
                                                     StyleClassNames.SUBMIT,
                                                     "Uploading strings/images, please wait...");

             ProgressBar pb = (ProgressBar) pp.getProperties ().get ("progress-bar");

             Utils.postToURL (u,
                              headers,
                              outputFile,
                              // On success
                              (res, retCode) ->
                              {

                                  pp.close ();

                                  try
                                  {

                                      Files.deleteIfExists (outputFile);

                                  } catch (Exception e) {

                                      Environment.logError ("Unable to delete temp file: " + outputFile,
                                                            e);

                                  }

                                  Map m = (Map) JSONDecoder.decode (res);

                                  res = (String) m.get ("result");

                                  String sid = (String) m.get ("testid");

                                  try
                                  {

                                      this.saveToFile ();

                                  } catch (Exception e) {

                                      Environment.logError ("Unable to save strings file: " +
                                                            this.userStrings,
                                                            e);

                                  }

                                  String url = Environment.getQuollWriterWebsite () + "/" + sid + "/";

                                  this.showMessage ("Strings uploaded",
                                                    String.format ("Your strings have been uploaded and are ready to be tested.  A browser will now be opened for the {QW} website.<br /><br />You can also <a href='%s'>click here</a> to try out your translation.",
                                                                   url));

                                  UIUtils.openURL (_this,
                                                   url);

                             },
                             // On error
                             (res, retCode) ->
                             {

                                  pp.close ();

                                  try
                                  {

                                      Files.deleteIfExists (outputFile);

                                  } catch (Exception e) {

                                      Environment.logError ("Unable to delete temp file: " + outputFile,
                                                            e);

                                  }

                                  Map m = (Map) JSONDecoder.decode (res);

                                  res = (String) m.get ("reason");

                                  // Get the errors.
                                  ComponentUtils.showErrorMessage (_this,
                                                                   new SimpleStringProperty ("Unable to submit the strings, reason:<ul class='error'><li>" + res + "</li></ul>"));

                              },
                              // On failure
                              (exp) ->
                              {

                                  pp.close ();

                                  // Get the errors.
                                  ComponentUtils.showErrorMessage (_this,
                                                                   new SimpleStringProperty ("Unable to submit the strings, reason:<ul class='error'><li>" + exp.getMessage () + "</li></ul>"));

                              },
                              // Updater
                              (eev) ->
                              {

                                  pb.setProgress (eev.getPercent () / 100f);

                              });

        } catch (Exception e) {

            Environment.logError ("Unable to post to the url.",
                                e);

            ComponentUtils.showErrorMessage (this,
                                             new SimpleStringProperty ("Unable to upload strings."));

        }

    }

    @Override
    public void init (State s)
               throws GeneralException
    {

        final WebsiteLanguageStringsEditor _this = this;

        super.init (s);

        try
        {

            this.prevEnglishStrings = WebsiteLanguageStringsManager.getPreviousEnglishWebsiteLanguageStrings ();

        } catch (Exception e) {

            throw new GeneralException ("Unable to get previous English website language strings",
                                        e);

        }

        if (this.prevEnglishStrings != null)
        {

            this.showForwardLabel (new SimpleStringProperty (String.format ("Click to show what's changed/new between version <b>%1$s</b> and <b>%2$s</b>.",
                                                                            this.baseStrings.getStringsVersion (),
                                                                            this.prevEnglishStrings.getStringsVersion ())));

        }

        // Check to see if a new version of the default strings is available.
        Environment.schedule (() ->
        {

            WebsiteLanguageStrings enStrs = null;

            try
            {

                enStrs = WebsiteLanguageStringsManager.getWebsiteLanguageStringsFromServer ();

            } catch (Exception e) {

                Environment.logError ("Unable to get English Website UI language strings",
                                      e);

                ComponentUtils.showErrorMessage (_this,
                                                 new SimpleStringProperty ("Unable to get English Website UI language strings."));

                return;

            }

             if (enStrs.getStringsVersion () == _this.userStrings.getDerivedFrom ().getStringsVersion ())
             {

                 // Server still has same version.
                 return;

             }

             try
             {

                 // Move the existing strings file to a previous veersion.
                 WebsiteLanguageStringsManager.moveWebsiteLanguageStringsToPrevious ();

             } catch (Exception e) {

                 Environment.logError ("Unable to move English Website UI language strings to previous file.",
                                       e);

                 ComponentUtils.showErrorMessage (_this,
                                                  new SimpleStringProperty ("Unable to get English Website UI language strings."));

                 return;

             }

             try
             {

                 // Save the new strings away.
                 WebsiteLanguageStringsManager.saveWebsiteLanguageStrings (enStrs);

             } catch (Exception e) {

                 Environment.logError ("Unable to save new English Website UI language strings",
                                       e);

                 ComponentUtils.showErrorMessage (_this,
                                                  new SimpleStringProperty ("Unable to save new English Website UI language strings."));

                 return;

             }

             final WebsiteLanguageStrings _enStrs = enStrs;

             UIUtils.runLater (() ->
             {

                 VBox content = new VBox ();

                 // Add a notification.
                 final Notification n = _this.addNotification (content,
                                                               StyleClassNames.INFORMATION,
                                                               -1);
                 content.getChildren ().add (QuollTextView.builder ()
                    .inViewer (this)
                    .text (new SimpleStringProperty (String.format ("A new version of the website language strings is available.<br />You can view the changes and submit an update to your strings.")))
                    .build ());
                 content.getChildren ().add (QuollHyperlink.builder ()
                    .styleClassName (StyleClassNames.VIEW)
                    .label (new SimpleStringProperty ("View the changes"))
                    .onAction (ev ->
                    {

                        WebsiteLanguageStrings uls = null;

                        try
                        {

                            uls = WebsiteLanguageStringsManager.getWebsiteLanguageStrings (_this.userStrings.getId ());

                        } catch (Exception e) {

                            Environment.logError ("Unable to get user strings: " + _this.userStrings.getId (),
                                                  e);

                            ComponentUtils.showErrorMessage (null,
                                                             getUILanguageStringProperty (uilanguage,edit,actionerror));

                            return;

                        }

                        if (uls != null)
                        {

                            // Open these instead.
                            WebsiteLanguageStringsEditor lse = WebsiteLanguageStringsManager.editWebsiteLanguageStrings (uls);

                            if (lse == _this)
                            {

                                _this.baseStrings = _enStrs;

                            }

                            try
                            {

                                lse.limitViewToPreviousVersionDiff ();

                            } catch (Exception e) {

                                Environment.logError ("Unable to update view",
                                                      e);

                                ComponentUtils.showErrorMessage (_this,
                                                                  new SimpleStringProperty ("Unable to update view"));

                                return;

                            }

                            _this.removeNotification (n);

                            return;

                        }

                        _this.showChanges (_enStrs);

                        _this.removeNotification (n);

                    })
                    .build ());

            });

        },
        5,
        -1);

    }

    public void limitViewToPreviousVersionDiff ()
                                         throws Exception
    {

        try
        {

            this.save ();

        } catch (Exception e) {

            Environment.logError ("Unable to save",
                                  e);

            ComponentUtils.showErrorMessage (this,
                                             new SimpleStringProperty ("Unable to update view."));

            return;

        }

        final WebsiteLanguageStrings basels = this.baseStrings;

        final WebsiteLanguageStrings prevbasels = WebsiteLanguageStringsManager.getPreviousEnglishWebsiteLanguageStrings ();

        if (prevbasels == null)
        {

            // No strings.
            return;

        }

        this.prevEnglishStrings = prevbasels;
/*
TODO?
        this.setNodeFilter (new Filter<Node> ()
        {

            @Override
            public boolean accept (Node n)
            {

                Node pn = prevbasels.getNode (n.getId ());

                // Does the node exist in the current base strings but not the previous?
                if (pn == null)
                {

                    // This is a new node.
                    return true;

                }

                // It exists, but has it changed?
                if ((n instanceof Value)
                    &&
                    (!(pn instanceof Value))
                   )
                {

                    // Node type changed.
                    return true;

                }

                if ((pn instanceof Value)
                    &&
                    (!(n instanceof Value))
                   )
                {

                    // Node type changed.
                    return true;

                }

                if ((pn instanceof TextValue)
                    &&
                    (n instanceof TextValue)
                   )
                {

                    TextValue pnv = (TextValue) pn;
                    TextValue nv = (TextValue) n;

                    // Value changed?
                    if (pnv.getRawText ().equals (nv.getRawText ()))
                    {

                        return false;

                    }

                }

                return true;

            }

        });
*/
        this.showForwardLabel (new SimpleStringProperty (String.format ("Click to show all the strings for version <b>%1$s</b>.",
                                                         this.baseStrings.getStringsVersion ())));
/*
      this.showForwardLabel (String.format ("Click to show what's changed/new between version <b>%s</b> and <b>%s</b>.",
                                            Environment.getQuollWriterVersion ().toString (),
                                            this.userStrings.getQuollWriterVersion ().toString ()));
*/

    }

    public void showChanges (WebsiteLanguageStrings newls)
    {

        try
        {

            WebsiteLanguageStringsManager.saveWebsiteLanguageStrings (newls);

            WebsiteLanguageStringsManager.saveWebsiteLanguageStrings (this.userStrings);

            WebsiteLanguageStrings uls = WebsiteLanguageStringsManager.getWebsiteLanguageStrings (this.userStrings.getId ());

            // Get a diff of the default to this new.
            WebsiteLanguageStringsEditor lse = WebsiteLanguageStringsManager.editWebsiteLanguageStrings (uls);

            lse.limitViewToPreviousVersionDiff ();

        } catch (Exception e) {

            Environment.logError ("Unable to show strings editor for: " +
                                  newls,
                                  e);

            ComponentUtils.showErrorMessage (this,
                                             new SimpleStringProperty ("Unable to show strings."));

        }

    }

    @Override
    public javafx.scene.Node getTitleHeaderControl (HeaderControl control)
    {

        if (control == HeaderControl.help)
        {

            return QuollButton.builder ()
                .tooltip ("Click to view the help about editing your strings")
                .iconName (StyleClassNames.HELP)
                .onAction (ev ->
                {

                    UIUtils.openURL (this,
                                     "help:websitelanguage");

                    this.submit (null,
                                 null);

                })
                .build ();

        }

        return super.getTitleHeaderControl (control);

    }

    private Path createUploadFile ()
                            throws Exception
    {

        // Get the file, then send.
        String t = JSONEncoder.encode (this.userStrings.getAsJSON ());

        Map<String, Object> entries = new HashMap<> ();
        entries.put ("/strings", t);

        for (ImageValue iv : this.userStrings.getAllImageValues ())
        {

            ImageValue bv = this.baseStrings.getImageValue (iv.getId ());

            entries.put (BaseStrings.toId (bv.getId ()),
                         iv.getImageFile ());

        }

        Path outputFile = Files.createTempFile ("qw-upload",
                                                ".zip");

        outputFile.toFile ().deleteOnExit ();

        Utils.createZipFile (outputFile,
                             entries);

        return outputFile;

    }

    @Override
    public StringProperty titleProperty ()
    {

        return this.titleProperty;

    }

    @Override
    public void submit (Runnable onSuccess,
                        Runnable onFailure)
    {

        WebsiteLanguageStringsEditor _this = this;

        try
        {

            this.save ();

        } catch (Exception e) {

            Environment.logError ("Unable to save: " + this.userStrings,
                                  e);

            ComponentUtils.showErrorMessage (this,
                                             new SimpleStringProperty ("Unable to save strings."));

            UIUtils.runLater (onFailure);

            return;

        }

        Set<Value> vals = this.userStrings.getAllValues ();

        if (vals.size () != this.baseStrings.getAllValues ().size ())
        {

            this.showMessage ("Strings/images required",
                              "Sorry, you must provide values for all strings and images.");

            UIUtils.runLater (onFailure);

            return;

        }

        int c = 0;

        for (Value uv : vals)
        {

            Value bv = this.baseStrings.getValue (uv.getId ());

            if (bv == null)
            {

                // The string is present in the user strings but not the base!
                //Environment.logError ("Found string: " + uv.getId () + " present in user strings but not base.");

                continue;

            }

            c += uv.getErrors (this).size ();

        }

        if (c > 0)
        {

            this.showMessage ("Errors found in strings",
                              String.format ("Sorry, there are <b>%s</b> errors that must be corrected before you can submit the strings.",
                                                    Environment.formatNumber (c)));

            UIUtils.runLater (onFailure);

            return;

        }

        String popupId = "submit";

        QuollPopup qp = this.getPopupById (popupId);

        if (qp != null)
        {

            qp.show ();
            qp.toFront ();
            return;

        }

        QuollTextField nativelang = QuollTextField.builder ()
            .text (this.userStrings.getNativeName ())
            .build ();
        QuollTextField langcode = QuollTextField.builder ()
            .text (this.userStrings.getLanguageCode ())
            .build ();
        QuollTextField email = QuollTextField.builder ()
            .text (this.userStrings.getEmail ())
            .build ();

        QuollCheckBox tandc = QuollCheckBox.builder ()
            .label (new SimpleStringProperty ("I have read and agree to the Terms and Conditions"))
            .build ();

        Form.Builder fb = Form.builder ()
            .layoutType (Form.LayoutType.stacked)
            .confirmButton ()
            .cancelButton ()
            .description (new SimpleStringProperty ("Complete the form below to submit your strings.  All the values are required.<br /><br /><a href='https://en.wikipedia.org/wiki/List_of_ISO_639-1_codes'>Click here to view the language codes</a>"))
            .item (new SimpleStringProperty ("Native Name (i.e. Espa\u00F1ol, Fran\u00E7ais, Deutsch)"),
                   nativelang)
            .item (new SimpleStringProperty ("Language Code (ISO 639-1 2 letter code, i.e es)"),
                   langcode)
            .item (new SimpleStringProperty ("Contact Email"),
                   email);

        if (this.userStrings.getStringsVersion () == 0)
        {

            fb.item (QuollHyperlink.builder ()
                .styleClassName (StyleClassNames.INFORMATION)
                .label (new SimpleStringProperty ("View the Terms and Conditions for creating a translation"))
                .onAction (ev ->
                {

                    UIUtils.openURL (this,
                                     Environment.getQuollWriterHelpLink ("websitelanguage-terms-and-conditions",
                                                                         null));

                })
                .build ());

            fb.item (tandc);

        }

        Form f = fb.build ();

        qp = QuollPopup.builder ()
            .headerIconClassName (StyleClassNames.SUBMIT)
            .title (new SimpleStringProperty ("Submit your strings"))
            .popupId (popupId)
            .withClose (true)
            .hideOnEscape (true)
            .removeOnClose (true)
            .content (f)
            .build ();

        QuollPopup _qp = qp;

        f.setOnConfirm (ev ->
        {

            f.hideError ();

            String em = email.getText ();

            if (em == null)
            {

                f.showError (new SimpleStringProperty ("Please enter a valid email."));

                UIUtils.runLater (onFailure);

                return;

            }

            String nl = nativelang.getText ();

            if (nl == null)
            {

                f.showError (new SimpleStringProperty ("Please enter the Name Language name."));

                UIUtils.runLater (onFailure);

                return;

            }

            String lc = langcode.getText ();

            boolean show = false;

            if (lc == null)
            {

                show = true;

            } else {

                lc = lc.trim ().toLowerCase ();

                if (lc.length () != 2)
                {

                    show = true;

                } else {

                    if (!Character.isLetter (lc.charAt (0)))
                    {

                        show = true;

                    }

                    if (!Character.isLetter (lc.charAt (1)))
                    {

                        show = true;

                    }

                }

            }

            if (show)
            {

                f.showError (new SimpleStringProperty ("Please enter a valid 2 letter ISO 639-1 language code."));

                UIUtils.runLater (onFailure);

                return;

            }

            if (WebsiteLanguageStrings.isEnglish (lc))
            {

                f.showError (new SimpleStringProperty ("The {QW} already has an English translation!"));

                UIUtils.runLater (onFailure);

                return;

            }

            if (_this.userStrings.getAllValues ().size () == 0)
            {

                f.showError (new SimpleStringProperty ("No strings provided.  Please provide at least 1 string for your translation."));

                UIUtils.runLater (onFailure);

                return;

            }

            if ((_this.userStrings.getStringsVersion () == 0)
                &&
                (!tandc.isSelected ())
               )
            {

                f.showError (new SimpleStringProperty ("To submit your strings you must agree to the Terms & Conditions, and please give them a quick read ;)"));

                UIUtils.runLater (onFailure);

                return;

            }

            this.userStrings.setEmail (em);
            this.userStrings.setNativeName (nl);
            this.userStrings.setLanguageCode (lc);

            try
            {

                this.save ();

            } catch (Exception e) {

                Environment.logError ("Unable to save strings: " + this.userStrings,
                                      e);

                ComponentUtils.showErrorMessage (this,
                                                 new SimpleStringProperty ("Unable to save strings."));

                UIUtils.runLater (onFailure);

                return;

            }

            Path _outputFile = null;

            try
            {

                _outputFile = this.createUploadFile ();

            } catch (Exception e)
            {

                Environment.logError ("Unable to create upload file for strings: " + this.userStrings,
                                      e);

                ComponentUtils.showErrorMessage (this,
                                                 new SimpleStringProperty ("Unable to upload strings."));

                UIUtils.runLater (onFailure);

                return;

            }

            final Path outputFile = _outputFile;

            Map<String, String> headers = new HashMap<> ();

            String submitterid = UserProperties.get (Constants.WEBSITE_LANGUAGE_STRINGS_SUBMITTER_ID_PROPERTY_NAME);

            if (submitterid != null)
            {

                headers.put (Constants.WEBSITE_LANGUAGE_STRINGS_SUBMITTER_ID_HEADER_NAME,
                             submitterid);

            }

            URL u = null;

            try
            {

                _qp.close ();

                QuollPopup pp = this.showProgressPopup ("Uploading",
                                                        StyleClassNames.SUBMIT,
                                                        "Uploading strings/images, please wait...");

                ProgressBar pb = (ProgressBar) pp.getProperties ().get ("progress-bar");

                u = new URL (Environment.getQuollWriterWebsite () + UserProperties.getProperty (Constants.SUBMIT_WEBSITE_LANGUAGE_STRINGS_URL_PROPERTY_NAME));

                Utils.postToURL (u,
                                 headers,
                                 outputFile,
                                 // On success
                                 (res, retCode) ->
                                 {

                                     pp.close ();

                                     try
                                     {

                                         Files.deleteIfExists (outputFile);

                                     } catch (Exception e) {

                                         Environment.logError ("Unable to delete temp file: " + outputFile,
                                                               e);

                                     }

                                     Map m = (Map) JSONDecoder.decode (res);

                                     res = (String) m.get ("result");

                                     String sid = (String) m.get ("submitterid");

                                     UserProperties.set (Constants.WEBSITE_LANGUAGE_STRINGS_SUBMITTER_ID_PROPERTY_NAME,
                                                         sid);

                                     //_this.userStrings.setSubmitterId (sid);
                                     _this.userStrings.setStringsVersion (((Number) m.get ("version")).intValue ());

                                     try
                                     {

                                         _this.saveToFile ();

                                     } catch (Exception e) {

                                         Environment.logError ("Unable to save strings file: " +
                                                               _this.userStrings,
                                                               e);

                                         ComponentUtils.showErrorMessage (_this,
                                                                          new SimpleStringProperty ("Your strings have been submitted to Quoll Writer support for review.  However the associated local file, where the strings are kept on your machine, could not be updated."));

                                         UIUtils.runLater (onFailure);

                                         return;

                                     }

                                     if (_this.userStrings.getStringsVersion () == 1)
                                     {

                                         _this.showMessage ("Strings submitted",
                                                            String.format ("Your strings have been submitted to Quoll Writer support for review.<br /><br />A confirmation email has been sent to <b>%s</b>.  Please click on the link in that email to confirm your email address.<br /><br />Thank you for taking the time and the effort to create the strings, it is much appreciated!",
                                                                             _this.userStrings.getEmail ()));

                                     } else {

                                         _this.showMessage ("Strings submitted",
                                                            String.format ("Thank you!  Your strings have been updated to version <b>%s</b> and will be made available to visitors of the Quoll Writer website.<br /><br />Thank you for taking the time and effort to update the strings, it is much appreciated!",
                                                                             Environment.formatNumber (_this.userStrings.getStringsVersion ())));

                                     }

                                     UIUtils.runLater (onSuccess);

                                 },
                                 // On error
                                 (res, retCode) ->
                                 {

                                     pp.close ();

                                     try
                                     {

                                         Files.deleteIfExists (outputFile);

                                     } catch (Exception e) {

                                         Environment.logError ("Unable to delete temp file: " + outputFile,
                                                               e);

                                     }

                                     Map m = (Map) JSONDecoder.decode (res);

                                     res = (String) m.get ("reason");

                                     // Get the errors.
                                     ComponentUtils.showErrorMessage (_this,
                                                                      new SimpleStringProperty ("Unable to submit the strings, reason:<ul class='error'><li>" + res + "</li></ul>"));

                                     UIUtils.runLater (onFailure);

                                 },
                                 // On failure
                                 (exp) ->
                                 {

                                     pp.close ();

                                     // Get the errors.
                                     ComponentUtils.showErrorMessage (_this,
                                                                      new SimpleStringProperty ("Unable to submit the strings, reason:<ul class='error'><li>" + exp.getMessage () + "</li></ul>"));

                                     UIUtils.runLater (onFailure);

                                 },
                                 // Updater
                                 (eev) ->
                                 {

                                     pb.setProgress (eev.getPercent () / 100f);

                                 });

             } catch (Exception e) {

                 Environment.logError ("Unable to post to the url.",
                                       e);

                 ComponentUtils.showErrorMessage (_this,
                                                  new SimpleStringProperty ("Unable to upload strings."));

                 UIUtils.runLater (onFailure);

                 return;

             }

        });

        qp.show ();

    }

	@Override
    public Supplier<Set<MenuItem>> getSettingsMenuSupplier ()
	{

        return () ->
        {

            Set<MenuItem> items = new LinkedHashSet<> ();

            items.add (QuollMenuItem.builder ()
                .label (new SimpleStringProperty ("Submit your strings"))
                .iconName (StyleClassNames.SUBMIT)
                .onAction (ev ->
                {

                    this.submit (null,
                                 null);

                })
                .build ());

            items.add (QuollMenuItem.builder ()
                .label (new SimpleStringProperty ("Try out your strings"))
                .iconName (StyleClassNames.TRYOUT)
                .onAction (ev ->
                {

                    this.tryOut ();

                })
                .build ());

            items.add (QuollMenuItem.builder ()
                .label (new SimpleStringProperty ("Create a new translation"))
                .iconName (StyleClassNames.NEW)
                .onAction (ev ->
                {

                    UIUtils.showAddNewWebsiteLanguageStringsPopup (this);

                })
                .build ());

            items.add (QuollMenuItem.builder ()
                .label (new SimpleStringProperty ("Edit a translation"))
                .iconName (StyleClassNames.EDIT)
                .onAction (ev ->
                {

                    UIUtils.showEditWebsiteLanguageStringsSelectorPopup (this);

                })
                .build ());

            items.add (QuollMenuItem.builder ()
                .label (new SimpleStringProperty ("Delete"))
                .iconName (StyleClassNames.DELETE)
                .onAction (ev ->
                {

                    try
                    {

                        this.delete ();

                    } catch (Exception e) {

                        Environment.logError ("Unable to delete strings: " + this.userStrings.getId (),
                                              e);

                        ComponentUtils.showErrorMessage (this,
                                                         "Unable to delete strings.");

                    }

                })
                .build ());

            items.add (QuollMenuItem.builder ()
                .label (new SimpleStringProperty ("Close"))
                .iconName (StyleClassNames.CLOSE)
                .onAction (ev ->
                {

                    this.close (null);

                })
                .build ());

            items.add (new SeparatorMenuItem ());

            items.add (QuollMenuItem.builder ()
                .label (new SimpleStringProperty ("Open Project"))
                .iconName (StyleClassNames.OPEN)
                .onAction (ev ->
                {

                    Environment.showAllProjectsViewer ();

                })
                .build ());

            return items;

        };

	}

    public WebsiteLanguageStrings getUserLanguageStrings ()
    {

        return this.userStrings;

    }

    @Override
    public void delete ()
                 throws Exception
    {

        final WebsiteLanguageStringsEditor _this = this;

        QuollPopup qp = QuollPopup.yesConfirmTextEntryBuilder ()
            .headerIconClassName (StyleClassNames.DELETE)
            .inViewer (this)
            .title (new SimpleStringProperty ("Delete the strings?"))
            .description (new SimpleStringProperty ("Please confirm you wish to delete your strings.  Enter <b>Yes</b> in the box below to confirm deletion.<br /><br /><span class='warning'>Warning!  This is an irreverisble operation and cannot be undone.  This will make your strings unavailable to {QW} users but will not remove it from anyone who has already downloaded the strings."))
            .confirmButtonLabel (new SimpleStringProperty ("Yes, delete"))
            .cancelButtonLabel (new SimpleStringProperty ("No, keep them"))
            .onConfirm (ev ->
            {

                String submitterid = UserProperties.get (Constants.WEBSITE_LANGUAGE_STRINGS_SUBMITTER_ID_PROPERTY_NAME);

                if ((submitterid != null)
                    &&
                    // Has they been submitted?
                    (_this.userStrings.getStringsVersion () > 0)
                   )
                {

                    URL u = null;

                    try
                    {

                        String p = UserProperties.get (Constants.DELETE_WEBSITE_LANGUAGE_STRINGS_URL_PROPERTY_NAME);

                        p = Utils.replaceString (p,
                                                       Constants.ID_TAG,
                                                       this.userStrings.getId ());

                        u = new URL (Environment.getQuollWriterWebsite () + p);

                    } catch (Exception e) {

                        Environment.logError ("Unable to construct the url for delete the website ui language strings.",
                                              e);

                        ComponentUtils.showErrorMessage (this,
                                                         new SimpleStringProperty ("Unable to delete strings."));

                        return;

                    }

                    Map<String, String> headers = new HashMap<> ();

                    headers.put (Constants.WEBSITE_LANGUAGE_STRINGS_SUBMITTER_ID_HEADER_NAME,
                                 submitterid);

                    Utils.postToURL (u,
                                     headers,
                                     "bogus",
                                     // On success
                                     (res, retCode) ->
                                     {

                                         String r = (String) JSONDecoder.decode (res);

                                         // Delete our local versions.
                                         try
                                         {

                                             WebsiteLanguageStringsManager.deleteWebsiteLanguageStrings (_this.userStrings);

                                         } catch (Exception e) {

                                             Environment.logError ("Unable to delete user strings: " + _this.userStrings,
                                                                   e);

                                             ComponentUtils.showErrorMessage (_this,
                                                                              new SimpleStringProperty ("Unable to delete the strings."));

                                              return;

                                         }

                                         _this.close (() ->
                                         {

                                             this.showMessage ("Strings deleted",
                                                               "Your strings have been deleted.  Please note: your images have <b>not</b> been deleted.<br /><br />Thank you for the time and effort you put in to create the strings, it is much appreciated!");

                                         });

                                     },
                                     // On error
                                     (res, retCode) ->
                                     {

                                         Map m = (Map) JSONDecoder.decode (res);

                                         res = (String) m.get ("reason");

                                         // Get the errors.
                                         ComponentUtils.showErrorMessage (_this,
                                                                          new SimpleStringProperty ("Unable to submit the strings, reason:<ul class='error'><li>" + res + "</li></ul>"));

                                     },
                                     // On failure
                                     (exp) ->
                                     {

                                         //Map m = (Map) JSONDecoder.decode ((String) ev.getSource ());

                                         //String res = (String) m.get ("reason");

                                         // Get the errors.
                                         ComponentUtils.showErrorMessage (this,
                                                                          new SimpleStringProperty ("Unable to delete the strings, reason:<ul class='error'><li>" + exp.getMessage () + "</li></ul>"));

                                     },
                                     null);

                } else {

                    // Not been submitted.
                    // Delete our local versions.
                    try
                    {

                        WebsiteLanguageStringsManager.deleteWebsiteLanguageStrings (this.userStrings);

                    } catch (Exception e) {

                        Environment.logError ("Unable to delete website strings: " + this.userStrings,
                                              e);

                        ComponentUtils.showErrorMessage (this,
                                                         new SimpleStringProperty ("Unable to delete the strings."));

                         return;

                    }

                    // Close without saving.
                    this.close (() ->
                    {

                        this.showMessage ("Strings deleted",
                                          "Your strings have been deleted.");

                    });

                }

            })
            .build ();

    }

    private void saveToFile ()
                      throws Exception
    {

        WebsiteLanguageStringsManager.saveWebsiteLanguageStrings (this.userStrings);

    }

    @Override
    public void save ()
                throws Exception
    {

        // Cycle over all the id boxes and set the values if present.
        for (LanguageStringsIdsPanel p : this.panels.values ())
        {

            p.saveValues ();

        }

        this.userStrings.setBaseVersion (this.userStrings.getDerivedFrom ().getStringsVersion ());

        this.saveToFile ();

    }

    @Override
    public void showReportProblemForId (String id)
    {

        this.showReportProblem ("Website Language Strings Id: " + id + "\n\n");

    }

    @Override
    public String getStyleClassName ()
    {

        return "websitelanguagestrings";

    }

}
