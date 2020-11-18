package com.quollwriter.ui;

import java.awt.*;
import java.io.*;
import java.awt.datatransfer.*;
import java.awt.event.*;
import java.util.*;
import java.util.concurrent.*;
import java.text.*;
import java.net.*;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.text.*;
import javax.swing.event.*;
import javax.swing.filechooser.*;
import javax.swing.plaf.LayerUI;
import javax.swing.tree.*;

import com.gentlyweb.properties.*;
import com.gentlyweb.utils.*;

import org.josql.*;

import com.jgoodies.forms.builder.*;
import com.jgoodies.forms.factories.*;
import com.jgoodies.forms.layout.*;

import com.quollwriter.*;
import com.quollwriter.events.*;
import com.quollwriter.data.*;
import com.quollwriter.ui.events.*;
import com.quollwriter.data.comparators.*;
import com.quollwriter.data.editors.*;
import com.quollwriter.editors.*;
import com.quollwriter.ui.components.*;
import com.quollwriter.ui.panels.*;
import com.quollwriter.text.*;
import com.quollwriter.db.*;
import com.quollwriter.ui.sidebars.*;
import com.quollwriter.editors.*;
import com.quollwriter.editors.ui.*;
import com.quollwriter.editors.ui.sidebars.*;
import com.quollwriter.achievements.ui.*;
import com.quollwriter.ui.charts.*;
import com.quollwriter.ui.actionHandlers.*;
import com.quollwriter.ui.forms.*;
import com.quollwriter.ui.components.ScrollableBox;
import com.quollwriter.ui.components.SpellChecker;
import com.quollwriter.uistrings.*;

import static com.quollwriter.LanguageStrings.*;
import static com.quollwriter.Environment.getUIString;

public class LanguageStringsEditor extends AbstractLanguageStringsEditor<UILanguageStrings, UILanguageStrings>
{

	public static final String MAIN_CARD = "main";
	public static final String OPTIONS_CARD = "options";

    public static final String SUBMIT_HEADER_CONTROL_ID = "submit";
    public static final String USE_HEADER_CONTROL_ID = "use";
    public static final String HELP_HEADER_CONTROL_ID = "help";
    public static final String FIND_HEADER_CONTROL_ID = "find";

    public static int INTERNAL_SPLIT_PANE_DIVIDER_WIDTH = 2;

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

            UIUtils.showErrorMessage (this,
                                      "Unable to show strings.");

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

            UIUtils.showErrorMessage (this,
                                      "Unable to update view.");

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

        this.showForwardLabel (String.format ("Click to show all the strings for version <b>%s</b>.",
                                              this.userStrings.getQuollWriterVersion ().toString ()));

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

    private void updateTitle ()
    {

        this.setViewerTitle (String.format ("Edit Language Strings for: %s (%s)",
                                            this.userStrings.getNativeName (),
                                            this.baseStrings.getQuollWriterVersion ().toString ()));

    }

    @Override
    public void onForwardLabelClicked ()
                                throws Exception
    {

        if (this.nodeFilter != null)
        {

            this.showAllStrings ();

            this.showForwardLabel (String.format ("Click to show what's changed/new between version <b>%s</b> and <b>%s</b>.",
                                                  Environment.getQuollWriterVersion ().toString (),
                                                  this.userStrings.getQuollWriterVersion ().toString ()));

        } else {

            this.limitViewToPreviousVersionDiff ();

        }

    }

	@Override
    public void init ()
			   throws Exception
    {

		super.init ();

        final LanguageStringsEditor _this = this;

        this.updateTitle ();

        // Check to see if a new version of the default strings is available.
        Environment.schedule (() ->
        {

            String url = UserProperties.get (Constants.QUOLL_WRITER_GET_UI_LANGUAGE_STRINGS_URL_PROPERTY_NAME);

            url = StringUtils.replaceString (url,
                                             Constants.VERSION_TAG,
                                             _this.baseStrings.getQuollWriterVersion ().toString ());
            url = StringUtils.replaceString (url,
                                             Constants.ID_TAG,
                                             _this.baseStrings.getId ());

            url = StringUtils.replaceString (url,
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

                 Box content = new Box (BoxLayout.Y_AXIS);

                 content.add (UIUtils.createHelpTextPane (String.format ("A new version of the <b>%s</b> language strings is available.  This is for version <b>%s</b>, of {QW}.<br />You can view the changes and submit an update to your strings.",
                                                                         newls.getNativeName (),
                                                                         newls.getQuollWriterVersion ().toString ()),
                                                          _this));

                 content.add (Box.createVerticalStrut (5));

                 JButton b = UIUtils.createButton ("View the changes");

                 content.add (b);

                 // Add a notification.
                 final Notification n = _this.addNotification (content,
                                                               Constants.INFO_ICON_NAME,
                                                               -1);

                  b.addActionListener (new ActionListener ()
                  {

                       @Override
                       public void actionPerformed (ActionEvent ev)
                       {

                           UILanguageStrings uls = null;

                           try
                           {

                               uls = UILanguageStringsManager.getUserUILanguageStrings (newls.getQuollWriterVersion (),
                                                                           _this.userStrings.getId ());

                           } catch (Exception e) {

                               Environment.logError ("Unable to get user strings for version: " + newls.getQuollWriterVersion () + ", " + _this.userStrings.getId (),
                                                     e);

                               UIUtils.showErrorMessage (null,
                                                         getUIString (uilanguage,edit,actionerror));

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

                                   UIUtils.showErrorMessage (_this,
                                                             "Unable to update view");

                                   return;

                               }

                               _this.removeNotification (n);

                               return;

                           }

                           _this.showChanges (newls);

                           _this.removeNotification (n);

                       }

                  });

             } catch (Exception e) {

                 Environment.logError ("Unable to get new user interface strings",
                                       e);

            }

        },
        5,
        -1);

        if (!UserProperties.getAsBoolean (Constants.LANGUAGE_STRINGS_FIRST_USE_SEEN_PROPERTY_NAME))
        {

            UIUtils.showMessage ((PopupsSupported) this,
                                 "Welcome to the Editor!",
                                 "Welcome to the strings Editor.  This window is where you'll create your own strings for a language for the {QW} User Interface.<br /><br />It is recommended that you read the <a href='help://uilanguages/overview'>strings editor guide</a> before starting out.  It describes how the editor works and the concepts used.<br /><br /><a href='action:contact'>All feedback and ideas are welcome</a>.");

            UserProperties.set (Constants.LANGUAGE_STRINGS_FIRST_USE_SEEN_PROPERTY_NAME,
                                true);

        }

    }

	@Override
	public void doSaveState ()
	{

        super.doSaveState ();

	}

	@Override
    public Set<String> getTitleHeaderControlIds ()
	{

		Set<String> ids = new LinkedHashSet ();

        ids.add (SUBMIT_HEADER_CONTROL_ID);
        ids.add (FIND_HEADER_CONTROL_ID);
        ids.add (USE_HEADER_CONTROL_ID);
        //ids.add (REPORT_BUG_HEADER_CONTROL_ID);
        ids.add (HELP_HEADER_CONTROL_ID);
        ids.add (SETTINGS_HEADER_CONTROL_ID);

		return ids;

	}

	@Override
    public JComponent getTitleHeaderControl (String id)
	{

		if (id == null)
		{

			return null;

		}

        java.util.List<String> prefix = Arrays.asList (allprojects,headercontrols,items);

		final LanguageStringsEditor _this = this;

		JComponent c = null;

        if (id.equals (HELP_HEADER_CONTROL_ID))
        {

            c = UIUtils.createButton (Constants.HELP_ICON_NAME,
                                      Constants.ICON_TITLE_ACTION,
                                      "Click to view the help about editing your strings",
                                      new ActionListener ()
                                      {

                                        @Override
                                        public void actionPerformed (ActionEvent ev)
                                        {

                                            UIUtils.openURL (_this,
                                                             "help:uilanguage");

                                        }

                                      });

        }

		if (c != null)
		{

			return c;

		}

		return super.getTitleHeaderControl (id);

	}

    @Override
    public void submit (ActionListener onSuccess,
                        ActionListener onError)
    {

        final LanguageStringsEditor _this = this;

        try
        {

            this.save ();

        } catch (Exception e) {

            Environment.logError ("Unable to save: " + this.userStrings,
                                  e);

            UIUtils.showErrorMessage (this,
                                      "Unable to save strings.");

            return;

        }

        Set<Value> vals = this.userStrings.getAllValues ();

        if (vals.size () == 0)
        {

            UIUtils.showMessage ((PopupsSupported) this,
                                 "No strings provided",
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

            UIUtils.showMessage ((PopupsSupported) this,
                                 "Errors found in strings",
                                 String.format ("Sorry, there are <b>%s</b> errors that must be corrected before you can submit the strings.",
                                                Environment.formatNumber (c)));

            return;

        }

        final String popupName = "submit";
        QPopup popup = this.getNamedPopup (popupName);

        if (popup == null)
        {

            popup = UIUtils.createClosablePopup ("Submit your strings",
                                                 Environment.getIcon (Constants.UP_ICON_NAME,
                                                                      Constants.ICON_POPUP),
                                                 null);

            final QPopup qp = popup;

            Box content = new Box (BoxLayout.Y_AXIS);

            JTextPane help = UIUtils.createHelpTextPane ("Complete the form below to submit your strings.  All the values are required.",
                                                         this);

            help.setBorder (null);

            content.add (help);
            content.add (Box.createVerticalStrut (10));

            final JLabel error = UIUtils.createErrorLabel ("");
            error.setVisible (false);
            error.setBorder (UIUtils.createPadding (0, 5, 5, 5));

            content.add (error);

            Set<FormItem> items = new LinkedHashSet ();

            final TextFormItem nativelang = new TextFormItem ("Native Name (i.e. Espa\u00F1ol, Fran\u00E7ais, Deutsch)",
                                                             this.userStrings.getNativeName ());

            items.add (nativelang);

            final TextFormItem lang = new TextFormItem ("English Name (i.e. Spanish, French, German)",
                                                         this.userStrings.getLanguageName ());

            items.add (lang);

            final TextFormItem email = new TextFormItem ("Contact Email",
                                                         this.userStrings.getEmail ());

            items.add (email);

            final JLabel tc = UIUtils.createClickableLabel ("View the Terms and Conditions for creating a translation",
                                                            Environment.getIcon (Constants.INFO_ICON_NAME,
                                                                                 Constants.ICON_CLICKABLE_LABEL),
                                                            Environment.getQuollWriterHelpLink ("uilanguages/terms-and-conditions",
                                                                                                null));

            final CheckboxFormItem tandc = new CheckboxFormItem (null, "I have read and agree to the Terms and Conditions");

            if (this.userStrings.getStringsVersion () == 0)
            {

                items.add (new AnyFormItem (null, tc));
                items.add (tandc);

            }

            ActionListener saveAction = new ActionListener ()
            {

                @Override
                public void actionPerformed (ActionEvent ev)
                {

                    String em = email.getValue ();

                    if (em == null)
                    {

                        error.setText ("Please enter a valid email.");
                        error.setVisible (true);
                        qp.resize ();
                        qp.resize ();

                        return;

                    }

                    String l = lang.getValue ();

                    if (l == null)
                    {

                        error.setText ("Please enter the English Language name.");
                        error.setVisible (true);
                        qp.resize ();

                        return;

                    }

                    String nl = nativelang.getValue ();

                    if (nl == null)
                    {

                        error.setText ("Please enter the Name Language name.");
                        error.setVisible (true);
                        qp.resize ();

                        return;

                    }

                    if (_this.userStrings.getAllValues ().size () == 0)
                    {

                        error.setText ("No strings provided.  Please provide at least 1 string for your translation.");
                        error.setVisible (true);
                        qp.resize ();

                        return;

                    }

                    if ((_this.userStrings.getStringsVersion () == 0)
                        &&
                        (!tandc.isSelected ())
                       )
                    {

                        error.setText ("To submit your strings you must agree to the Terms & Conditions, and please give them a quick read ;)");
                        error.setVisible (true);
                        qp.resize ();

                        return;

                    }

                    _this.userStrings.setEmail (em);
                    _this.userStrings.setLanguageName (l);
                    _this.userStrings.setNativeName (nl);

                    _this.updateTitle ();

                    try
                    {

                        _this.save ();

                    } catch (Exception e) {

                        Environment.logError ("Unable to save strings: " + _this.userStrings,
                                              e);

                        UIUtils.showErrorMessage (_this,
                                                  "Unable to save strings.");

                        return;

                    }

                    // Get the file, then send.
                    String t = null;

                    try
                    {

                        t = JSONEncoder.encode (_this.userStrings.getAsJSON ());

                    } catch (Exception e)
                    {

                        Environment.logError ("Unable to upload strings: " + _this.userStrings,
                                              e);

                        UIUtils.showErrorMessage (_this,
                                                  "Unable to upload strings.");

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

                        UIUtils.showErrorMessage (_this,
                                                  "Unable to upload strings.");

                        return;

                    }

                    qp.setVisible (false);

                    final ProgressPopup pp = new ProgressPopup (_this,
                                                                "Uploading",
                                                                "up",
                                                                "Uploading strings, please wait...");

                    _this.showPopupAt (pp,
                                       UIUtils.getCenterShowPosition (_this,
                                                                      pp),
                                       false);

                    pp.setDraggable (_this);

                    Utils.postToURL (u,
                                     headers,
                                     t,
                                     // On success
                                     (content, resCode) ->
                                     {

                                         pp.removeFromParent ();

                                         Map m = (Map) JSONDecoder.decode (content);

                                         String res = (String) m.get ("result");

                                         String sid = (String) m.get ("submitterid");

                                         UserProperties.set (Constants.UI_LANGUAGE_STRINGS_SUBMITTER_ID_PROPERTY_NAME,
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

                                             UIUtils.showErrorMessage (_this,
                                                                       "Your strings have been submitted to Quoll Writer support for review.  However the associated local file, where the strings are kept on your machine, could not be updated.");

                                             return;

                                         }

                                         if (_this.userStrings.getStringsVersion () == 1)
                                         {

                                             UIUtils.showMessage ((PopupsSupported) _this,
                                                                  "Strings submitted",
                                                                  String.format ("Your strings have been submitted to Quoll Writer support for review.<br /><br />A confirmation email has been sent to <b>%s</b>.  Please click on the link in that email to confirm your email address.<br /><br />Thank you for taking the time and the effort to create the strings, it is much appreciated!",
                                                                                 _this.userStrings.getEmail ()));

                                         } else {

                                             UIUtils.showMessage ((PopupsSupported) _this,
                                                                  "Strings submitted",
                                                                  String.format ("Thank you!  Your strings have been updated to version <b>%s</b> and will be made available to Quoll Writer users.<br /><br />Thank you for taking the time and effort to update the strings, it is much appreciated!",
                                                                                 Environment.formatNumber (_this.userStrings.getStringsVersion ())));

                                         }

                                         qp.resize ();
                                         qp.removeFromParent ();

                                     },
                                     // On error
                                     (errContent, resCode) ->
                                     {

                                         pp.removeFromParent ();

                                         Map m = (Map) JSONDecoder.decode (errContent);

                                         String res = (String) m.get ("reason");

                                         // Get the errors.
                                         UIUtils.showErrorMessage (_this,
                                                                   "Unable to submit the strings, reason:<ul class='error'><li>" + res + "</li></ul>");

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

                                         pp.update (eev.getPercent ());

                                     });

                }

            };

            UIUtils.addDoActionOnReturnPressed (lang.getTextField (),
                                                saveAction);
            UIUtils.addDoActionOnReturnPressed (nativelang.getTextField (),
                                                saveAction);
            UIUtils.addDoActionOnReturnPressed (email.getTextField (),
                                                saveAction);

            JButton save = UIUtils.createButton ("Submit",
                                                 saveAction);
            JButton cancel = UIUtils.createButton ("Cancel",
                                                   new ActionListener ()
            {

                @Override
                public void actionPerformed (ActionEvent ev)
                {

                    qp.removeFromParent ();
                    _this.removeNamedPopup (popupName);

                }

            });

            Set<JButton> buttons = new LinkedHashSet ();
            buttons.add (save);
            buttons.add (cancel);

            Form f = new Form (Form.Layout.stacked,
                               items,
                               buttons);

            content.add (f);

            content.setSize (new Dimension (UIUtils.getPopupWidth () - 20,
                             content.getPreferredSize ().height));
            content.setBorder (UIUtils.createPadding (10, 10, 10, 10));

            popup.setContent (content);

            popup.setDraggable (this);

            popup.setPopupName (popupName);

            this.addNamedPopup (popupName,
                                popup);

            //popup.resize ();
            this.showPopupAt (popup,
                              UIUtils.getCenterShowPosition (this,
                                                             popup),
                              false);

            UIUtils.doLater (new ActionListener ()
            {

                @Override
                public void actionPerformed (ActionEvent ev)
                {

                    //desc.grabFocus ();
                    qp.resize ();

                }

            });

        } else {

            popup.setVisible (true);
            popup.resize ();

        }

    }

    @Override
    public void tryOut ()
    {

        if (!this.userStrings.getQuollWriterVersion ().equals (Environment.getQuollWriterVersion ()))
        {

            UIUtils.showMessage ((PopupsSupported) this,
                                 "Unable to try out",
                                 "Sorry, you can only try out a set of strings if the version matches the currently installed {QW} version.");

            return;

        }

        try
        {

            this.save ();

            Environment.setUILanguage ("user-" + this.userStrings.getId ());

        } catch (Exception e) {

            Environment.logError ("Unable to update ui language for: " + this.userStrings.getId (),
                                  e);

            UIUtils.showErrorMessage (this,
                                      "Unable to set strings.");

            return;

        }

        UIUtils.showMessage ((PopupsSupported) this,
                             "Restart recommended",
                             "{QW} has been updated to make use of your strings.  To make full use of them it is recommended that you restart {QW}.");

    }

	@Override
    public void fillSettingsPopup (JPopupMenu popup)
	{

        //java.util.List<String> prefix = Arrays.asList (allprojects,settingsmenu,items);

		final LanguageStringsEditor _this = this;

        popup.add (this.createMenuItem ("Submit your strings",
                                        Constants.UP_ICON_NAME,
                                        new ActionAdapter ()
                                        {

                                                public void actionPerformed (ActionEvent ev)
                                                {

                                                    _this.submit (null,
                                                                  null);

                                                }

                                            }));

        popup.add (this.createMenuItem ("Create a new translation",
                                        Constants.NEW_ICON_NAME,
										new ActionListener ()
											 {

												@Override
												public void actionPerformed (ActionEvent ev)
												{

                                                    UIUtils.showAddNewUILanguageStringsPopup (_this);

												}

											 }));

        popup.add (this.createMenuItem ("Edit a translation",
                                        Constants.EDIT_ICON_NAME,
 			    						new ActionListener ()
 										{

											@Override
											public void actionPerformed (ActionEvent ev)
											{

                                                UIUtils.showEditUILanguageStringsSelectorPopup (_this);

											}

										}));

        if (this.baseStrings.getQuollWriterVersion ().equals (Environment.getQuollWriterVersion ()))
        {

            popup.add (this.createMenuItem ("Try out your strings",
                                            Constants.PLAY_ICON_NAME,
    										new ActionListener ()
    											 {

    												@Override
    												public void actionPerformed (ActionEvent ev)
    												{

                                                        _this.tryOut ();

    												}

    											 }));

        }

        popup.add (this.createMenuItem ("Delete",
                                     Constants.DELETE_ICON_NAME,
										new ActionListener ()
											 {

												@Override
												public void actionPerformed (ActionEvent ev)
												{

                                                    try
                                                    {

                                                        _this.delete ();

                                                    } catch (Exception e) {

                                                        Environment.logError ("Unable to delete strings.",
                                                                              e);

                                                        UIUtils.showErrorMessage (_this,
                                                                                  "Unable to delete the strings.");

                                                    }

												}

											 }));

         popup.add (this.createMenuItem ("Close",
                                      Constants.CLOSE_ICON_NAME,
 										new ActionListener ()
 											 {

 												@Override
 												public void actionPerformed (ActionEvent ev)
 												{

                                                  _this.close (true,
                                                               null);

 												}

 											 }));

        popup.addSeparator ();

        popup.add (this.createMenuItem ("Open Project",
                                        Constants.UP_ICON_NAME,
                                        new ActionAdapter ()
                                        {

                                                public void actionPerformed (ActionEvent ev)
                                                {

                                                    Environment.showAllProjectsViewer ();

                                                }

                                            }));


	}

    @Override
    public void delete ()
                 throws Exception
    {

        final LanguageStringsEditor _this = this;

        QPopup qp = UIUtils.createPopup ("Delete the strings",
                                         Constants.DELETE_ICON_NAME,
                                         null,
                                         true,
                                         null);

        final Box content = new Box (BoxLayout.Y_AXIS);

        JComponent mess = UIUtils.createHelpTextPane ("Please confirm you wish to delete your strings.  Enter <b>Yes</b> in the box below to confirm deletion.<br /><br />To delete <b>all</b> versions of the strings please check the box below.<br /><br /><span class='warning'>Warning!  This is an irreverisble operation and cannot be undone.  This will make your strings unavailable to {QW} users but will not remove it from anyone who has already downloaded the strings.",
                                                      this);
        mess.setBorder (null);
        mess.setSize (new Dimension (UIUtils.getPopupWidth () - 20,
                                     mess.getPreferredSize ().height));

        content.add (mess);

        content.add (Box.createVerticalStrut (10));

        final JCheckBox delAll = UIUtils.createCheckBox ("Delete all versions");

        content.add (delAll);

        content.add (Box.createVerticalStrut (10));

        final JLabel error = UIUtils.createErrorLabel ("Please enter the word Yes.");
        //"Please enter a value.");

        error.setVisible (false);
        error.setBorder (UIUtils.createPadding (0, 0, 5, 0));

        final JTextField text = UIUtils.createTextField ();

        text.setMinimumSize (new Dimension (300,
                                            text.getPreferredSize ().height));
        text.setPreferredSize (new Dimension (300,
                                              text.getPreferredSize ().height));
        text.setMaximumSize (new Dimension (Short.MAX_VALUE,
                                            text.getPreferredSize ().height));
        text.setAlignmentX (Component.LEFT_ALIGNMENT);

        error.setAlignmentX (Component.LEFT_ALIGNMENT);

        content.add (error);
        content.add (text);

        content.add (Box.createVerticalStrut (10));

        JButton confirm = null;
        JButton cancel = UIUtils.createButton ("No, keep them",
                                               new ActionListener ()
        {

            @Override
            public void actionPerformed (ActionEvent ev)
            {

                qp.removeFromParent ();

            }

        });

        ActionListener confirmAction = new ActionListener ()
        {

            @Override
            public void actionPerformed (ActionEvent ev)
            {

                String mess = UIUtils.getYesValueValidator ().isValid (text.getText ().trim ());

                if (mess != null)
                {

                    // Should probably wrap this in a
                    error.setText (mess);

                    error.setVisible (true);

                    // Got to be an easier way of doing this.
                    content.setPreferredSize (null);

                    content.setPreferredSize (new Dimension (UIUtils.getPopupWidth (),
                                                             content.getPreferredSize ().height));

                    _this.showPopupAt (qp,
                                       qp.getLocation (),
                                       false);

                    return;

                }

                String submitterid = UserProperties.get (Constants.UI_LANGUAGE_STRINGS_SUBMITTER_ID_PROPERTY_NAME);

                if ((submitterid != null)
                    &&
                    // Has they been submitted?
                    (_this.userStrings.getStringsVersion () > 0)
                   )
                {

                    URL u = null;

                    try
                    {

                        String p = UserProperties.get (Constants.DELETE_UI_LANGUAGE_STRINGS_URL_PROPERTY_NAME);

                        p = StringUtils.replaceString (p,
                                                       Constants.ID_TAG,
                                                       _this.userStrings.getId ());

                        p = StringUtils.replaceString (p,
                                                       Constants.VERSION_TAG,
                                                       _this.userStrings.getQuollWriterVersion ().toString ());

                        p = StringUtils.replaceString (p,
                                                       Constants.ALL_TAG,
                                                       (delAll.isSelected () ? "true" : ""));

                        u = new URL (Environment.getQuollWriterWebsite () + p);

                    } catch (Exception e) {

                        Environment.logError ("Unable to construct the url for submitting the ui language strings.",
                                              e);

                        UIUtils.showErrorMessage (_this,
                                                  "Unable to upload strings.");

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

                                             UILanguageStringsManager.deleteUserUILanguageStrings (_this.userStrings,
                                                                                      delAll.isSelected ());

                                         } catch (Exception e) {

                                             Environment.logError ("Unable to delete user strings: " + _this.userStrings,
                                                                   e);

                                             UIUtils.showErrorMessage (_this,
                                                                       "Unable to delete the strings.");

                                              qp.removeFromParent ();

                                              return;

                                         }

                                         LanguageStringsEditor.super.close (true,
                                                                            new ActionListener ()
                                         {

                                             @Override
                                             public void actionPerformed (ActionEvent ev)
                                             {

                                                 UIUtils.showMessage ((Component) null,
                                                                      "Strings deleted",
                                                                      "Your strings have been deleted.<br /><br />Thank you for the time and effort you put in to create the strings, it is much appreciated!");

                                             }

                                         });

                                     },
                                     // On error
                                     (errContent, resCode) ->
                                     {

                                         Map m = (Map) JSONDecoder.decode (errContent);

                                         String res = (String) m.get ("reason");

                                         // Get the errors.
                                         UIUtils.showErrorMessage (_this,
                                                                   "Unable to submit the strings, reason:<ul class='error'><li>" + res + "</li></ul>");

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

                        UILanguageStringsManager.deleteUserUILanguageStrings (_this.userStrings,
                                                                              delAll.isSelected ());

                    } catch (Exception e) {

                        Environment.logError ("Unable to delete user strings: " + _this.userStrings,
                                              e);

                        UIUtils.showErrorMessage (_this,
                                                  "Unable to delete the strings.");

                         qp.removeFromParent ();

                         return;

                    }

                    // Close without saving.
                    LanguageStringsEditor.super.close (true,
                                                       new ActionListener ()
                    {

                        @Override
                        public void actionPerformed (ActionEvent ev)
                        {

                            UIUtils.showMessage ((Component) null,
                                                 "Strings deleted",
                                                 "Your strings have been deleted.");

                        }

                    });

                }

            }

        };

        confirm = UIUtils.createButton ("Yes, delete them",
                                        confirmAction);

        UIUtils.addDoActionOnReturnPressed (text,
                                            confirmAction);

        JButton[] buts = null;

        if (confirm != null)
        {

            buts = new JButton[] { confirm, cancel };

        } else {

            buts = new JButton[] { cancel };

        }

        JComponent buttons = UIUtils.createButtonBar2 (buts,
                                                       Component.LEFT_ALIGNMENT); //ButtonBarFactory.buildLeftAlignedBar (buts);
        buttons.setAlignmentX (Component.LEFT_ALIGNMENT);
        content.add (buttons);
        content.setBorder (UIUtils.createPadding (10, 10, 10, 10));

        content.setPreferredSize (new Dimension (UIUtils.getPopupWidth (),
                                                 content.getPreferredSize ().height));

        qp.setContent (content);
        qp.setDraggable (this);

        this.showPopupAt (qp,
                          UIUtils.getCenterShowPosition (this,
                                                         qp),
                          false);

    }

	@Override
    public boolean close (boolean              noConfirm,
                          final ActionListener afterClose)
	{

        super.close (noConfirm,
                     afterClose);

		return true;

	}

    @Override
    public void showReportProblemForId (String id)
    {

        this.showReportProblem ("Language Strings Id: " + id + "\n\n");

    }

}
