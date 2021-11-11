package com.quollwriter.ui.fx.viewers;

import java.io.*;
import java.util.*;
import java.util.function.*;
import java.util.concurrent.*;
import java.text.*;
import java.net.*;
import java.nio.file.*;

import org.josql.*;

import javafx.beans.binding.*;
import javafx.scene.layout.*;
import javafx.beans.property.*;
import javafx.scene.control.*;

import com.quollwriter.*;
import com.quollwriter.data.*;
import com.quollwriter.data.comparators.*;
import com.quollwriter.ui.fx.components.*;
import com.quollwriter.ui.fx.panels.*;
import com.quollwriter.ui.fx.*;
import com.quollwriter.text.*;
import com.quollwriter.ui.fx.sidebars.*;
import com.quollwriter.uistrings.*;

import static com.quollwriter.uistrings.UILanguageStringsManager.getUILanguageStringProperty;
import static com.quollwriter.LanguageStrings.*;

public class LanguageStringsEditor extends AbstractLanguageStringsEditor<UILanguageStrings, UILanguageStrings>
{

	public static final String MAIN_CARD = "main";
	public static final String OPTIONS_CARD = "options";

    //public static final String SUBMIT_HEADER_CONTROL_ID = "submit";
    //public static final String USE_HEADER_CONTROL_ID = "use";
    //public static final String HELP_HEADER_CONTROL_ID = "help";
    //public static final String FIND_HEADER_CONTROL_ID = "find";

    //public static int INTERNAL_SPLIT_PANE_DIVIDER_WIDTH = 2;

    private StringProperty titleProp = null;

    public LanguageStringsEditor (UILanguageStrings userStrings)
			               throws Exception
    {

        this (userStrings,
              Environment.getQuollWriterVersion ());

    }

    public LanguageStringsEditor (UILanguageStrings userStrings,
                                  UILanguageStrings baseStrings)
			               throws Exception
    {

        super (userStrings,
               baseStrings);

        this.init ();

    }

    public LanguageStringsEditor (UILanguageStrings userStrings,
                                  Version         baseQWVersion)
			               throws Exception
    {

        super (userStrings);

        if (baseQWVersion == null)
        {

            baseQWVersion = Environment.getQuollWriterVersion ();

        }

        if (userStrings == null)
        {

            throw new IllegalArgumentException ("No strings provided.");

        }

        UILanguageStrings baseStrings = UILanguageStringsManager.getUserUIEnglishLanguageStrings (baseQWVersion);

        if (baseStrings == null)
        {

            throw new IllegalArgumentException ("Unable to find English strings for version: " + baseQWVersion);

        }

        this.setBaseStrings (baseStrings);

        this.init ();

    }

    private void init ()
    {

        this.titleProp = new SimpleStringProperty ();

        this.titleProp.bind (Bindings.createStringBinding (() ->
        {

          return String.format ("Edit Language Strings for: %s (%s)",
                                this.userStrings.getNativeName (),
                                this.baseStrings.getQuollWriterVersion ().toString ());

        },
        this.userStrings.nativeNameProperty ()));

    }

/*
    private int getErrorCount (LanguageStrings.Node n)
    {

        int c = 0;

        // Get the card.
        IdsPanel p = this.panels.get (n.getNodeId ());

        if (p != null)
        {

            return p.getErrorCount ();

        }

        for (LanguageStrings.Value nv : this.valuesCache.get (n))
        {

            LanguageStrings.Value uv = this.userStrings.getValue (nv.getId (),
                                                                  true);

            if (uv instanceof LanguageStrings.TextValue)
            {

                LanguageStrings.TextValue _nv = this.baseStrings.getTextValue (uv.getId ());

                if (nv == null)
                {

                  // The string is present in the user strings but not the base!
                  Environment.logError ("Found string: " + uv.getId () + " present in user strings but not base.");

                  continue;

                }

                if (LanguageStrings.getErrors (((LanguageStrings.TextValue) uv).getRawText (),
                                               LanguageStrings.toId (nv.getId ()),
                                               _nv.getSCount (),
                                               this).size () > 0)
                {

                    c++;

                };

            }

        }

        return c;

    }
*/

    public void showChanges (UILanguageStrings newls)
    {

        Version v = this.userStrings.getQuollWriterVersion ();

        try
        {

            UILanguageStringsManager.saveUserUILanguageStrings (newls);

            this.userStrings.setQuollWriterVersion (newls.getQuollWriterVersion ());

            UILanguageStringsManager.saveUserUILanguageStrings (this.userStrings);

            this.userStrings.setQuollWriterVersion (v);

            UILanguageStrings uls = UILanguageStringsManager.getUserUILanguageStrings (newls.getQuollWriterVersion (),
                                                                          this.userStrings.getId ());

            // Get a diff of the default to this new.
            LanguageStringsEditor lse = UILanguageStringsManager.editUILanguageStrings (uls,
                                                                           newls.getQuollWriterVersion ());
            lse.limitViewToPreviousVersionDiff ();

        } catch (Exception e) {

            Environment.logError ("Unable to show strings editor for: " +
                                  newls,
                                  e);

            ComponentUtils.showErrorMessage (this,
                                             new SimpleStringProperty ("Unable to show strings."));

        }

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

        final UILanguageStrings basels = this.baseStrings;

        // Get the previous version (which will be the current QW version).
        final UILanguageStrings prevbasels = UILanguageStringsManager.getUserUIEnglishLanguageStrings (Environment.getQuollWriterVersion ());

        if (prevbasels == null)
        {

            // No strings.
            return;

        }

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

        this.showForwardLabel (new SimpleStringProperty (String.format ("Click to show all the strings for version <b>%s</b>.",
                                                                        this.userStrings.getQuollWriterVersion ().toString ())));

    }

    private void saveToFile ()
                      throws Exception
    {

        UILanguageStringsManager.saveUserUILanguageStrings (this.userStrings);

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

        this.userStrings.setQuollWriterVersion (this.baseStrings.getQuollWriterVersion ());

        this.saveToFile ();

    }

    @Override
    public void onForwardLabelClicked ()
                                throws Exception
    {

        if (this.nodeFilter != null)
        {

            this.showAllStrings ();

            this.showForwardLabel (new SimpleStringProperty (String.format ("Click to show what's changed/new between version <b>%s</b> and <b>%s</b>.",
                                                                            Environment.getQuollWriterVersion ().toString (),
                                                                            this.userStrings.getQuollWriterVersion ().toString ())));

        } else {

            this.limitViewToPreviousVersionDiff ();

        }

    }

	@Override
    public void init (State s)
			   throws GeneralException
    {

		super.init (s);

        final LanguageStringsEditor _this = this;

        // Check to see if a new version of the default strings is available.
        Environment.schedule (() ->
        {

            String url = UserProperties.get (Constants.QUOLL_WRITER_GET_UI_LANGUAGE_STRINGS_URL_PROPERTY_NAME);

            url = Utils.replaceString (url,
                                             Constants.VERSION_TAG,
                                             _this.baseStrings.getQuollWriterVersion ().toString ());
            url = Utils.replaceString (url,
                                             Constants.ID_TAG,
                                             _this.baseStrings.getId ());

            url = Utils.replaceString (url,
                                             Constants.LAST_MOD_TAG,
                                             "0");

            url += "&newer=true";

            try
            {

                 String data = Utils.getUrlFileAsString (new URL (Environment.getQuollWriterWebsite () + "/" + url));

                 if (data.startsWith (Constants.JSON_RETURN_PREFIX))
                 {

                     data = data.substring (Constants.JSON_RETURN_PREFIX.length ());

                 }

                 Object obj = JSONDecoder.decode (data);

                 if (obj == null)
                 {

                     return;

                 }

                 final UILanguageStrings newls = new UILanguageStrings (data);

                 VBox content = new VBox ();

                 // Add a notification.
                 final Notification n = _this.addNotification (content,
                                                               StyleClassNames.INFORMATION,
                                                               -1);

                 content.getChildren ().add (QuollTextView.builder ()
                    .inViewer (this)
                    .text (new SimpleStringProperty (String.format ("A new version of the <b>%s</b> language strings is available.  This is for version <b>%s</b>, of {QW}.<br />You can view the changes and submit an update to your strings.",
                                                                    newls.getNativeName (),
                                                                    newls.getQuollWriterVersion ().toString ())))
                    .build ());
                content.getChildren ().add (QuollHyperlink.builder ()
                    .label (new SimpleStringProperty ("View the changes"))
                    .styleClassName (StyleClassNames.VIEW)
                    .onAction (ev ->
                    {

                        UILanguageStrings uls = null;

                        try
                        {

                            uls = UILanguageStringsManager.getUserUILanguageStrings (newls.getQuollWriterVersion (),
                                                                                     _this.userStrings.getId ());

                        } catch (Exception e) {

                            Environment.logError ("Unable to get user strings for version: " + newls.getQuollWriterVersion () + ", " + _this.userStrings.getId (),
                                                  e);

                            ComponentUtils.showErrorMessage (this,
                                                             getUILanguageStringProperty (uilanguage,edit,actionerror));

                            return;

                        }

                        if (uls != null)
                        {

                            // Open these instead.
                            LanguageStringsEditor lse = UILanguageStringsManager.editUILanguageStrings (uls,
                                                                                                        uls.getQuollWriterVersion ());

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

                        _this.showChanges (newls);

                        _this.removeNotification (n);

                    })
                    .build ());

             } catch (Exception e) {

                 Environment.logError ("Unable to get new user interface strings",
                                       e);

            }

        },
        5,
        -1);

        if (!UserProperties.getAsBoolean (Constants.LANGUAGE_STRINGS_FIRST_USE_SEEN_PROPERTY_NAME))
        {

            this.showMessage ("Welcome to the Editor!",
                              "Welcome to the strings Editor.  This window is where you'll create your own strings for a language for the {QW} User Interface.<br /><br />It is recommended that you read the <a href='help://uilanguages/overview'>strings editor guide</a> before starting out.  It describes how the editor works and the concepts used.<br /><br /><a href='action:contact'>All feedback and ideas are welcome</a>.");

            UserProperties.set (Constants.LANGUAGE_STRINGS_FIRST_USE_SEEN_PROPERTY_NAME,
                                true);

        }

    }

    @Override
    public javafx.scene.Node getTitleHeaderControl (HeaderControl control)
	{

		if (control == null)
		{

			return null;

		}

        if (control == HeaderControl.help)
        {

            return QuollButton.builder ()
                .tooltip ("Click to view the help about editing your strings")
                .iconName (StyleClassNames.HELP)
                .buttonId (HeaderControlButtonIds.help)
                .onAction (ev ->
                {

                    UIUtils.openURL (this,
                                     "help:uilanguage");

                })
                .build ();

        }

		return super.getTitleHeaderControl (control);

	}

    @Override
    public void submit (Runnable onSuccess,
                        Runnable onError)
    {

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

        Set<Value> vals = this.userStrings.getAllValues ();

        if (vals.size () == 0)
        {

            this.showMessage ("No strings provided",
                              "Sorry, you must provide at least 1 string.");

            return;

        }

        int c = 0;

        for (Value uv : vals)
        {

            if (uv instanceof TextValue)
            {

                TextValue nv = this.baseStrings.getTextValue (uv.getId ());

                if (nv == null)
                {

                    // The string is present in the user strings but not the base!
                    Environment.logError ("Found string: " + uv.getId () + " present in user strings but not base.");

                    continue;

                }

                c += BaseStrings.getErrors (((TextValue) uv).getRawText (),
                                            BaseStrings.toId (nv.getId ()),
                                            nv.getSCount (),
                                            this).size ();

            }

        }

        if (c > 0)
        {

            this.showMessage ("Errors found in strings",
                              String.format ("Sorry, there are <b>%s</b> errors that must be corrected before you can submit the strings.",
                                             Environment.formatNumber (c)));

            return;

        }

        final String popupId = "submit";

        if (this.getPopupById (popupId) != null)
        {

            this.getPopupById (popupId).toFront ();
            this.getPopupById (popupId).setVisible (true);
            return;

        }

        Form.Builder fb = Form.builder ()
            .confirmButton (new SimpleStringProperty ("Submit"))
            .cancelButton (new SimpleStringProperty ("Cancel"))
            .inViewer (this)
            .description (new SimpleStringProperty ("Complete the form below to submit your strings.  All the values are required."));

        QuollTextField nativelang = QuollTextField.builder ()
            .text (this.userStrings.getNativeName ())
            .build ();
        QuollTextField lang = QuollTextField.builder ()
            .text (this.userStrings.getLanguageName ())
            .build ();
        QuollTextField email = QuollTextField.builder ()
            .text (this.userStrings.getEmail ())
            .build ();
        QuollCheckBox tandc = QuollCheckBox.builder ()
            .label (new SimpleStringProperty ("I have read and agree to the Terms and Conditions"))
            .build ();

        fb.item (new SimpleStringProperty ("Native Name (i.e. Espa\u00F1ol, Fran\u00E7ais, Deutsch)"),
                 nativelang);
        fb.item (new SimpleStringProperty ("English Name (i.e. Spanish, French, German)"),
                 lang);
        fb.item (new SimpleStringProperty ("Contact Email"),
                 email);

        if (this.userStrings.getStringsVersion () == 0)
        {

            fb.item (QuollHyperlink.builder ()
                .label (new SimpleStringProperty ("View the Terms and Conditions for creating a translation"))
                .styleClassName (StyleClassNames.VIEW)
                .onAction (ev ->
                {

                    UIUtils.openURL (this,
                                     Environment.getQuollWriterHelpLink ("uilanguages/terms-and-conditions",
                                                                         null));

                })
                .build ());
            fb.item (tandc);

        }

        Form f = fb.build ();

        QuollPopup qp = QuollPopup.builder ()
            .title (new SimpleStringProperty ("Submit your strings"))
            .styleClassName (StyleClassNames.SUBMIT)
            .popupId (popupId)
            .content (f)
            .inViewer (this)
            .build ();

        f.setOnConfirm (ev ->
        {

            f.hideError ();

            Set<StringProperty> errs = new LinkedHashSet<> ();

            String em = email.getText ().trim ();

            if ("".equals (em))
            {

                errs.add (new SimpleStringProperty ("Please provide a Contact Email address."));

            } else {

                StringProperty emErr = Utils.checkEmail (em);

                if (emErr != null)
                {

                    errs.add (emErr);

                }

            }

            String l = lang.getText ().trim ();

            if ("".equals (l))
            {

                errs.add (new SimpleStringProperty ("Please enter the English Language name."));

            }

            String nl = nativelang.getText ().trim ();

            if ("".equals (nl))
            {

                errs.add (new SimpleStringProperty ("Please enter the Name Language name."));

            }

            if (this.userStrings.getAllValues ().size () == 0)
            {

                errs.add (new SimpleStringProperty ("No strings provided.  Please provide at least 1 string for your translation."));

            }

            if ((this.userStrings.getStringsVersion () == 0)
                &&
                (!tandc.isSelected ())
               )
            {

                errs.add (new SimpleStringProperty ("To submit your strings you must agree to the Terms & Conditions, and please give them a quick read ;)"));

            }

            if (errs.size () > 0)
            {

                f.showErrors (errs);
                return;

            }

            this.userStrings.setEmail (em);
            this.userStrings.setLanguageName (l);
            this.userStrings.setNativeName (nl);

            try
            {

                this.save ();

            } catch (Exception e) {

                Environment.logError ("Unable to save strings: " + this.userStrings,
                                      e);

                ComponentUtils.showErrorMessage (this,
                                                 new SimpleStringProperty ("Unable to save strings."));

                return;

            }

            // Get the file, then send.
            String t = null;

            try
            {

                t = JSONEncoder.encode (this.userStrings.getAsJSON ());

            } catch (Exception e)
            {

                Environment.logError ("Unable to upload strings: " + this.userStrings,
                                      e);

                ComponentUtils.showErrorMessage (this,
                                                 new SimpleStringProperty ("Unable to upload strings."));

                return;

            }

            Map<String, String> headers = new HashMap<> ();

            String submitterid = UserProperties.get (Constants.UI_LANGUAGE_STRINGS_SUBMITTER_ID_PROPERTY_NAME);

            if (submitterid != null)
            {

                headers.put (Constants.UI_LANGUAGE_STRINGS_SUBMITTER_ID_HEADER_NAME,
                             submitterid);

            }

            URL u = null;

            try
            {

                u = new URL (Environment.getQuollWriterWebsite () + UserProperties.get (Constants.SUBMIT_UI_LANGUAGE_STRINGS_URL_PROPERTY_NAME));

            } catch (Exception e) {

                Environment.logError ("Unable to construct the url for submitting the ui language strings.",
                                      e);

                ComponentUtils.showErrorMessage (this,
                                                 new SimpleStringProperty ("Unable to upload strings."));

                return;

            }

            qp.close ();

            QuollPopup pp = this.showProgressPopup ("Uploading",
                                                    StyleClassNames.SUBMIT,
                                                    "Uploading strings, please wait...");

            ProgressBar pb = (ProgressBar) pp.getProperties ().get ("progress-bar");

            Utils.postToURL (u,
                             headers,
                             t,
                             // On success
                             (content, resCode) ->
                             {

                                 UIUtils.runLater (() ->
                                 {

                                     pp.close ();

                                     Map m = (Map) JSONDecoder.decode (content);

                                     String res = (String) m.get ("result");

                                     String sid = (String) m.get ("submitterid");

                                     UserProperties.set (Constants.UI_LANGUAGE_STRINGS_SUBMITTER_ID_PROPERTY_NAME,
                                                         sid);

                                     //_this.userStrings.setSubmitterId (sid);
                                     this.userStrings.setStringsVersion (((Number) m.get ("version")).intValue ());

                                     try
                                     {

                                         this.saveToFile ();

                                     } catch (Exception e) {

                                         Environment.logError ("Unable to save strings file: " +
                                                               this.userStrings,
                                                               e);

                                         ComponentUtils.showErrorMessage (this,
                                                                          new SimpleStringProperty ("Your strings have been submitted to Quoll Writer support for review.  However the associated local file, where the strings are kept on your machine, could not be updated."));

                                         return;

                                     }

                                     if (this.userStrings.getStringsVersion () == 1)
                                     {

                                         this.showMessage ("Strings submitted",
                                                           String.format ("Your strings have been submitted to Quoll Writer support for review.<br /><br />A confirmation email has been sent to <b>%s</b>.  Please click on the link in that email to confirm your email address.<br /><br />Thank you for taking the time and the effort to create the strings, it is much appreciated!",
                                                                          this.userStrings.getEmail ()));

                                     } else {

                                         this.showMessage ("Strings submitted",
                                                           String.format ("Thank you!  Your strings have been updated to version <b>%s</b> and will be made available to Quoll Writer users.<br /><br />Thank you for taking the time and effort to update the strings, it is much appreciated!",
                                                                          Environment.formatNumber (this.userStrings.getStringsVersion ())));

                                     }

                                 });

                             },
                             // On error
                             (errContent, resCode) ->
                             {

                                 UIUtils.runLater (() ->
                                 {

                                     pp.close ();
                                     Map m = (Map) JSONDecoder.decode (errContent);

                                     String res = (String) m.get ("reason");

                                     // Get the errors.
                                     ComponentUtils.showErrorMessage (this,
                                                                      new SimpleStringProperty ("Unable to submit the strings, reason:<ul class='error'><li>" + res + "</li></ul>"));

                                });

                             },
                             exp ->
                             {

/*
TODO Improve
                                 pp.removeFromParent ();

                                 Map m = (Map) JSONDecoder.decode ((String) ev.getSource ());

                                 String res = (String) m.get ("reason");

                                 // Get the errors.
                                 UIUtils.showErrorMessage (_this,
                                                           "Unable to submit the strings, reason:<ul class='error'><li>" + res + "</li></ul>");
*/
                             },
                             // Updater
                             eev ->
                             {

                                 pb.setProgress (eev.getPercent () / 100f);

                             });

        });

        f.setOnCancel (ev ->
        {

            qp.close ();

        });

        UIUtils.forceRunLater (() ->
        {

            nativelang.requestFocus ();

        });

        qp.show ();

    }

    @Override
    public void tryOut ()
    {

        if (!this.userStrings.getQuollWriterVersion ().equalsIgnoreBeta (Environment.getQuollWriterVersion ()))
        {

            this.showMessage ("Unable to try out",
                              BaseStrings.replaceSpecialValues ("Sorry, you can only try out a set of strings if the version matches the currently installed {QW} version."));

            return;

        }

        try
        {

            this.save ();

            Environment.setUILanguage ("user-" + this.userStrings.getId ());

        } catch (Exception e) {

            Environment.logError ("Unable to update ui language for: " + this.userStrings.getId (),
                                  e);

            ComponentUtils.showErrorMessage (this,
                                             new SimpleStringProperty ("Unable to set strings."));

            return;

        }

        this.showMessage ("Restart recommended",
                          "{QW} has been updated to make use of your strings.  To make full use of them it is recommended that you restart {QW}.");

    }

    @Override
    public Supplier<Set<MenuItem>> getSettingsMenuSupplier ()
    {

		final LanguageStringsEditor _this = this;

        return () ->
        {

            Set<MenuItem> items = new LinkedHashSet<> ();

            items.add (QuollMenuItem.builder ()
                .label ("Submit your strings")
                .iconName (StyleClassNames.SUBMIT)
                .onAction (ev ->
                {

                    _this.submit (null,
                                  null);

                })
                .build ());

            items.add (QuollMenuItem.builder ()
                .label ("Create a new translation")
                .iconName (StyleClassNames.NEW)
                .onAction (ev ->
                {

                    UIUtils.showAddNewUILanguageStringsPopup (_this);

                })
                .build ());

            items.add (QuollMenuItem.builder ()
                .label ("Edit a translation")
                .iconName (StyleClassNames.NEW)
                .onAction (ev ->
                {

                    UIUtils.showEditUILanguageStringsSelectorPopup (_this);

                })
                .build ());

            if (this.baseStrings.getQuollWriterVersion ().equals (Environment.getQuollWriterVersion ()))
            {

                items.add (QuollMenuItem.builder ()
                    .label ("Try out your strings")
                    .iconName (StyleClassNames.TRYOUT)
                    .onAction (ev ->
                    {

                        _this.tryOut ();

                    })
                    .build ());

            }

            items.add (QuollMenuItem.builder ()
                .label ("Delete")
                .iconName (StyleClassNames.DELETE)
                .onAction (ev ->
                {

                    try
                    {

                        _this.delete ();

                    } catch (Exception e) {

                        Environment.logError ("Unable to delete strings.",
                                              e);

                        ComponentUtils.showErrorMessage (_this,
                                                         new SimpleStringProperty ("Unable to delete the strings."));

                    }

                })
                .build ());

            items.add (QuollMenuItem.builder ()
                .label ("Close")
                .iconName (StyleClassNames.CLOSE)
                .onAction (ev ->
                {

                    _this.close (null);

                })
                .build ());

            items.add (new SeparatorMenuItem ());

            items.add (QuollMenuItem.builder ()
                .label ("Open Project")
                .iconName (StyleClassNames.OPEN)
                .onAction (ev ->
                {

                    Environment.showAllProjectsViewer ();

                })
                .build ());

            return items;


        };

	}

    @Override
    public void delete ()
                 throws Exception
    {

        final LanguageStringsEditor _this = this;

        QuollCheckBox delAll = QuollCheckBox.builder ()
            .label (new SimpleStringProperty ("Delete all versions"))
            .build ();

        QuollPopup qp = QuollPopup.yesConfirmTextEntryBuilder ()
            .title (new SimpleStringProperty ("Delete the strings"))
            .description (new SimpleStringProperty ("Please confirm you wish to delete your strings.  Enter <b>Yes</b> in the box below to confirm deletion.<br /><br />To delete <b>all</b> versions of the strings please check the box below.<br /><br /><span class='warning'>Warning!  This is an irreverisble operation and cannot be undone.  This will make your strings unavailable to {QW} users but will not remove it from anyone who has already downloaded the strings."))
            .item (new Form.Item (delAll))
            .inViewer (this)
            .headerIconClassName (StyleClassNames.DELETE)
            .confirmButtonLabel (new SimpleStringProperty ("Yes, delete them"))
            .cancelButtonLabel (new SimpleStringProperty ("No, keep them"))
            .onConfirm (ev ->
            {

                String submitterid = UserProperties.get (Constants.UI_LANGUAGE_STRINGS_SUBMITTER_ID_PROPERTY_NAME);

                if ((submitterid != null)
                    &&
                    // Has they been submitted?
                    (this.userStrings.getStringsVersion () > 0)
                   )
                {

                    URL u = null;

                    try
                    {

                        String p = UserProperties.get (Constants.DELETE_UI_LANGUAGE_STRINGS_URL_PROPERTY_NAME);

                        p = Utils.replaceString (p,
                                                 Constants.ID_TAG,
                                                 this.userStrings.getId ());

                        p = Utils.replaceString (p,
                                                 Constants.VERSION_TAG,
                                                 this.userStrings.getQuollWriterVersion ().toString ());

                        p = Utils.replaceString (p,
                                                 Constants.ALL_TAG,
                                                 (delAll.isSelected () ? "true" : ""));

                        u = new URL (Environment.getQuollWriterWebsite () + p);

                    } catch (Exception e) {

                        Environment.logError ("Unable to construct the url for submitting the ui language strings.",
                                              e);

                        ev.getForm ().showError (new SimpleStringProperty ("Unable to delete strings."));

                        ev.consume ();

                        return;

                    }

                    Map<String, String> headers = new HashMap<> ();

                    headers.put (Constants.UI_LANGUAGE_STRINGS_SUBMITTER_ID_HEADER_NAME,
                                 submitterid);

                    Utils.postToURL (u,
                                     headers,
                                     "bogus",
                                     // On success
                                     (content, resCode) ->
                                     {

                                         String r = (String) JSONDecoder.decode (content);

                                         // Delete our local versions.
                                         try
                                         {

                                             _this.deleteStrings (delAll.isSelected ());

                                         } catch (Exception e) {

                                             Environment.logError ("Unable to delete user strings: " + _this.userStrings,
                                                                   e);

                                             ev.getForm ().showError (new SimpleStringProperty ("Unable to delete the strings."));

                                             ev.consume ();

                                              return;

                                         }

                                         UIUtils.runLater (() ->
                                         {

                                             this.close (false,
                                                         () ->
                                             {

                                                 this.showMessage ("Strings deleted",
                                                                   "Your strings have been deleted.<br /><br />Thank you for the time and effort you put in to create the strings, it is much appreciated!");

                                            });

                                        });

                                     },
                                     // On error
                                     (errContent, resCode) ->
                                     {

                                         Map m = (Map) JSONDecoder.decode (errContent);

                                         String res = (String) m.get ("reason");

                                         // Get the errors.
                                         ev.getForm ().showError (new SimpleStringProperty ("Unable to delete the strings, reason:<ul class='error'><li>" + res + "</li></ul>"));
                                         ev.consume ();
                                         return;

                                     },
                                     eex ->
                                     {

    /*
    TODO Improve
                                             Map m = (Map) JSONDecoder.decode ((String) ev.getSource ());

                                             String res = (String) m.get ("reason");

                                             // Get the errors.
                                             UIUtils.showErrorMessage (_this,
                                                                       "Unable to delete the strings, reason:<ul class='error'><li>" + res + "</li></ul>");
    */

                                     },
                                     null);

                } else {

                    // Not been submitted.
                    // Delete our local versions.
                    try
                    {

                        this.deleteStrings (delAll.isSelected ());

                    } catch (Exception e) {

                        Environment.logError ("Unable to delete user strings: " + this.userStrings,
                                              e);

                        ev.getForm ().showError (new SimpleStringProperty ("Unable to delete the strings."));

                        ev.consume ();

                         return;

                    }

                    // Close without saving.
                    this.close (false,
                                () ->
                    {

                        this.showMessage ("Strings deleted",
                                          "Your strings have been deleted.");

                    });

                }

            })
            .build ();

        qp.show ();

    }

    private void deleteStrings (boolean allVersions)
                         throws Exception
    {

        if (!this.userStrings.isUser ())
        {

            throw new IllegalArgumentException ("Can only delete user language strings.");

        }

        if (allVersions)
        {

            Set<UILanguageStrings> allLs = UILanguageStringsManager.getAllUserUILanguageStrings ();

            for (UILanguageStrings _ls : allLs)
            {

                if (_ls.getId ().equals (this.userStrings.getId ()))
                {

                    this.deleteStrings (_ls);

                }

            }

        } else {

            this.deleteStrings (this.userStrings);

        }

    }

    private void deleteStrings (final UILanguageStrings ls)
    {

        Runnable remFile = () ->
        {

            try
            {

                Path f = UILanguageStringsManager.getUserUILanguageStringsFilePath (ls.getQuollWriterVersion (),
                                                                                    ls.getId ());

                Files.deleteIfExists (f);

            } catch (Exception e) {

                Environment.logError (String.format ("Unable to delete user ui language strings: %s/%s",
                                                     ls.getQuollWriterVersion (),
                                                     ls.getId ()),
                                      e);

                ComponentUtils.showErrorMessage (new SimpleStringProperty ("Unable to delete language strings."));

            }

        };
/*
        for (AbstractViewer v : Environment.getOpenViewers ())
        {

            if (v instanceof LanguageStringsEditor)
            {

                LanguageStringsEditor lse = (LanguageStringsEditor) v;

                if ((lse.getUserLanguageStrings ().getId ().equals (ls.getId ()))
                    &&
                    (lse.getUserLanguageStrings ().getQuollWriterVersion ().equals (ls.getQuollWriterVersion ()))
                   )
                {

                    lse.close (remFile);

                    return;

                }

            }

        }
*/
        remFile.run ();

        if (UserProperties.get (Constants.USER_UI_LANGUAGE_PROPERTY_NAME).equals ("user-" + ls.getId ()))
        {

            try
            {

                // Need to set the language back to English.
                UILanguageStringsManager.setUILanguage (UILanguageStrings.ENGLISH_ID);

            } catch (Exception e) {

                Environment.logError ("Unable to set UI strings.",
                                      e);

                // TODO Check this...
                ComponentUtils.showErrorMessage (new SimpleStringProperty ("Unable to reset user interface language to " + Constants.ENGLISH));

                return;

            }

            // TODO Check this...
            QuollPopup.messageBuilder ()
                .withViewer (Environment.getFocusedViewer ())
                .title (new SimpleStringProperty ("Restart recommended"))
                .message (new SimpleStringProperty ("The user interface language has been reset to " + Constants.ENGLISH + ", a restart is recommended."))
                .build ();

        }

    }

    @Override
    public void showReportProblemForId (String id)
    {

        this.showReportProblem ("Language Strings Id: " + id + "\n\n");

    }

    @Override
    public StringProperty titleProperty ()
    {

        return this.titleProp;

    }

    @Override
    public String getStyleClassName ()
    {

        return "uilanguagestrings";

    }

}
