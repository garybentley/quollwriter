package com.quollwriter.ui.fx.popups;

import java.util.*;

import org.dom4j.*;

import javafx.scene.*;
import javafx.beans.property.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import com.quollwriter.*;
import com.quollwriter.ui.fx.*;
import com.quollwriter.ui.fx.components.*;
import com.quollwriter.ui.fx.viewers.*;

import static com.quollwriter.uistrings.UILanguageStringsManager.getUILanguageStringProperty;
import static com.quollwriter.LanguageStrings.*;

public class WhatsNewPopup extends PopupContent
{

    public static final String POPUP_ID = "whatsnew";

    private class XMLConstants
    {

        public static final String id = "id";
        public static final String version = "version";
        public static final String clazz = "class";
        public static final String beta = "beta";

    }

    private TreeMap<Version, List<WhatsNewItem>> items = new TreeMap<> ();

    public WhatsNewPopup (AbstractViewer viewer,
                          boolean        onlyShowCurrentVersion)
                   throws GeneralException
    {

        super (viewer);

        final WhatsNewPopup _this = this;

        Version lastWhatsNewVersion = Environment.getQuollWriterVersion ();

        String wn = UserProperties.get (Constants.WHATS_NEW_VERSION_VIEWED_PROPERTY_NAME);

        if (wn != null)
        {

            lastWhatsNewVersion = new Version (wn);

        }

        // Get the current whats new version (i.e. old).
        //Version lastWhatsNewVersion = new Version (wn);

        boolean betasAllowed = UserProperties.getAsBoolean (Constants.OPTIN_TO_BETA_VERSIONS_PROPERTY_NAME);

        try
        {

            String whatsNew = Utils.getResourceFileAsString (Constants.WHATS_NEW_FILE);

            // Load up all the whats new for greater versions.
            Element root = DOM4JUtils.stringAsElement (whatsNew);

            // Assume they are in the right order
            // TODO: Enforce the order and/or sort.
            for (Element vEl : root.elements (XMLConstants.version))
            {

                String id = DOM4JUtils.attributeValue (vEl,
                                                         XMLConstants.id,
                                                         true);

                Version v = new Version (id);
/*
                if ((v.isBeta ())
                    &&
                    (!betasAllowed)
                   )
                {

                    // Ignore, the user isn't interested in betas.
                    continue;

                }
  */

                if ((lastWhatsNewVersion.isNewer (v))
                    ||
                    ((onlyShowCurrentVersion)
                     &&
                     (v.isSame (Environment.getQuollWriterVersion ()))
                    )
                   )
                {

                    WhatsNewNodeProvider compProv = null;

                    String cl = DOM4JUtils.attributeValue (vEl,
                                                             XMLConstants.clazz,
                                                             false);

                    if (cl != null)
                    {

                        Class clz = null;

                        try
                        {

                            clz = Class.forName (cl);

                            if (WhatsNewNodeProvider.class.isAssignableFrom (clz))
                            {

                                compProv = (WhatsNewNodeProvider) clz.newInstance ();

                            }

                        } catch (Exception e) {

                        }

                    }

                    // This is a version we are interested in.
                    List<WhatsNewItem> its = new ArrayList<> ();

                    for (Element itEl : vEl.elements (WhatsNewItem.XMLConstants.root))
                    {

                        WhatsNewItem it = new WhatsNewItem (itEl,
                                                            compProv,
                                                            viewer);

                        if (it.onlyIfCurrentVersion)
                        {

                            if (!Environment.getQuollWriterVersion ().isSame (v))
                            {

                                continue;

                            }

                        }

                        if ((it.description == null)
                            &&
                            (it.id != null)
                           )
                        {

                            // Get the description from the ui string.
                            it.descriptionProp = getUILanguageStringProperty (whatsnew,versions,v.getVersion (), LanguageStrings.items,it.id,text);

                        }

                        if ((it.title == null)
                            &&
                            (it.id != null)
                           )
                        {

                            // Get the description from the ui string.
                            it.titleProp = getUILanguageStringProperty (whatsnew,versions,v.getVersion (), LanguageStrings.items,it.id,title);

                        }

                        if (it.titleProp == null)
                        {

                            Environment.logMessage ("Whats new item has no title, referenced by: " +
                                                    DOM4JUtils.getPath (itEl));

                            continue;

                        }

                        if ((it.descriptionProp == null)
                            &&
                            (it.component == null)
                           )
                        {

                            Environment.logMessage ("Whats new item has no description or component, referenced by: " +
                                                    DOM4JUtils.getPath (itEl));

                            continue;

                        }

                        its.add (it);

                    }

                    if (its.size () > 0)
                    {

                        this.items.put (v,
                                        its);

                    }

                }

            }

        } catch (Exception e) {

            throw new GeneralException ("Unable to init whats new",
                                        e);

        }

        QuollTextView mess = QuollTextView.builder ()
        //BasicHtmlTextFlow mess = BasicHtmlTextFlow.builder ()
            .styleClassName (StyleClassNames.DESCRIPTION)
            .text (getUILanguageStringProperty (Arrays.asList (whatsnew,text),
                                                Environment.getQuollWriterVersion ().getVersion (),
                                                Environment.getQuollWriterVersion ().getVersion ().replace ('.', '_')))
            .build ();

        // Maybe move to UIUtils?
        Wizard w = Wizard.builder ()
            .startStepId (this.getStartStepId ())
            .nextStepIdProvider (currId ->
            {

                return getNextStepId (currId);

            })
            .previousStepIdProvider (currId ->
            {

                return getPreviousStepId (currId);

            })
            .stepProvider (currId ->
            {

                return getStep (currId);

            })
            .build ();

        w.addEventHandler (Wizard.WizardEvent.CANCEL_EVENT,
                           ev ->
        {

            _this.close ();

        });

        w.addEventHandler (Wizard.WizardEvent.FINISH_EVENT,
                           ev ->
        {

            _this.close ();

        });

        VBox b = new VBox ();
        VBox.setVgrow (w, Priority.ALWAYS);
        b.getChildren ().addAll (mess, w);

        this.getChildren ().addAll (b);

    }

    private Wizard.Step getStep (String currId)
    {

        int ind = currId.indexOf (":");

        Version v = new Version (currId.substring (0,
                                                  ind));

        int lind = Integer.parseInt (currId.substring (ind + 1));

        List<WhatsNewItem> its = this.items.get (v);

        if (its == null)
        {

            return null;

        }

        WhatsNewItem item = its.get (lind);

        if (item == null)
        {

            return null;

        }

        Wizard.Step step = new Wizard.Step ();

        step.title = item.titleProp;

        if ((item.descriptionProp != null)
            ||
            (item.component != null)
           )
        {

            VBox b = new VBox ();

            if (item.descriptionProp != null)
            {

                //BasicHtmlTextFlow text = BasicHtmlTextFlow.builder ()
                QuollTextView text = QuollTextView.builder ()
                    .text (item.descriptionProp)
                    .styleClassName (StyleClassNames.DESCRIPTION)
                    //.withHandler (this.viewer)
                    .inViewer (this.viewer)
                    .build ();

                b.getChildren ().add (text);

            }

            if (item.component != null)
            {

                b.getChildren ().add (item.component);

            }

            step.content = b;

        }

        return step;

    }

    private String getPreviousStepId (String currId)
    {

        if (currId == null)
        {

            return null;

        }

        int ind = currId.indexOf (":");

        Version v = new Version (currId.substring (0,
                                                   ind));

        int lind = Integer.parseInt (currId.substring (ind + 1));

        List<WhatsNewItem> its = this.items.get (v);

        if (its == null)
        {

            return null;

        }

        lind--;

        if (lind > -1)
        {

            return v.getVersion () + ":" + lind;

        }

        Version p = this.items.lowerKey (v);

        if (p == null)
        {

            return null;

        }

        List<WhatsNewItem> pits = this.items.get (p);

        if (pits != null)
        {

            return p.getVersion () + ":" + (pits.size () - 1);

        }

        return null;

    }

    private String getNextStepId (String currId)
    {

        if (currId == null)
        {

            return this.getStartStepId ();

        }

        int ind = currId.indexOf (":");

        Version v = new Version (currId.substring (0,
                                                   ind));

        int lind = Integer.parseInt (currId.substring (ind + 1));

        List<WhatsNewItem> its = this.items.get (v);

        if (its == null)
        {

            return null;

        }

        lind++;

        if (lind <= (its.size () - 1))
        {

            return v.getVersion () + ":" + lind;

        }

        Version n = this.items.higherKey (v);

        if (n == null)
        {

            return null;

        }

        List<WhatsNewItem> nits = this.items.get (n);

        if (nits != null)
        {
System.out.println ("HERE4");
            return n.getVersion () + ":0";

        }
System.out.println ("HERE5");
        return null;

    }

    private String getStartStepId ()
    {

        try
        {

            return this.items.firstKey ().getVersion () + ":0";

        } catch (Exception e) {

            return null;

        }

    }

    @Override
    public QuollPopup createPopup ()
    {

        QuollPopup p = QuollPopup.builder ()
            .title (getUILanguageStringProperty (Arrays.asList (whatsnew,LanguageStrings.popup,title),
                                                 Environment.getQuollWriterVersion ()))
            .styleClassName (StyleClassNames.WHATSNEW)
            .styleSheet (StyleClassNames.WHATSNEW)
            .hideOnEscape (true)
            .withClose (true)
            .content (this)
            .popupId (POPUP_ID)
            .withViewer (this.viewer)
            .removeOnClose (true)
            .build ();
        p.requestFocus ();

        Environment.fireUserProjectEvent (this.viewer,
                                          ProjectEvent.Type.whatsnew,
                                          ProjectEvent.Action.show);

        UserProperties.set (Constants.WHATS_NEW_VERSION_VIEWED_PROPERTY_NAME,
                            Environment.getQuollWriterVersion ().getVersion ());

        return p;

    }

    private class WhatsNewItem
    {

        public class XMLConstants
        {

            public static final String root = "item";
            public static final String title = "title";
            public static final String description = "description";
            public static final String onlyIfCurrentVersion = "onlyIfCurrentVersion";
            public static final String id = "id";

        }

        public String title = null;
        public String description = null;
        public StringProperty titleProp = null;
        public StringProperty descriptionProp = null;
        public String id = null;
        public boolean onlyIfCurrentVersion = false;
        public javafx.scene.Node component = null;

        public WhatsNewItem (Element              root,
                             WhatsNewNodeProvider prov,
                             AbstractViewer       pv)
                             throws               Exception
        {

            this.id = DOM4JUtils.attributeValue (root,
                                                   XMLConstants.id,
                                                   false);

            if ((!this.id.equals (""))
                &&
                (prov != null)
               )
            {

                this.component = prov.getNode (pv,
                                               this.id);

            } else {

                //this.id = null;

            }

            this.onlyIfCurrentVersion = DOM4JUtils.attributeValueAsBoolean (root,
                                                                              XMLConstants.onlyIfCurrentVersion,
                                                                              false);
            this.title = DOM4JUtils.childElementContent (root,
                                                           XMLConstants.title,
                                                           false,
                                                           null);

            this.titleProp = new SimpleStringProperty (this.title);

            String desc = DOM4JUtils.childElementContent (root,
                                                          XMLConstants.description,
                                                          false,
                                                          null);

            if (desc != null)
            {

                this.descriptionProp = new SimpleStringProperty (desc);

            }

        }

    }

}
