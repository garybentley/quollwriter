package com.quollwriter.ui.fx.tips;

import java.util.*;

import org.dom4j.*;

import javafx.beans.property.*;
import javafx.scene.*;

import com.quollwriter.*;
import com.quollwriter.data.*;
import com.quollwriter.ui.fx.*;
import com.quollwriter.ui.fx.components.*;
import com.quollwriter.ui.fx.viewers.*;
import com.quollwriter.ui.fx.panels.*;

import static com.quollwriter.LanguageStrings.*;
import static com.quollwriter.uistrings.UILanguageStringsManager.getUILanguageStringProperty;

public class Tips
{

    public class XMLConstants
    {

        public static final String tip = "tip";
        public static final String id = "id";

    }

    private List<Tip> baseTips = new ArrayList ();
    private List<Tip> tips = new ArrayList ();
    private Random ind = new Random ();
    private int lastInd = 0;
    private AbstractViewer viewer = null;

    public Tips (AbstractViewer viewer)
                 throws Exception
    {

        this.viewer = viewer;

        String tipsXML = Utils.getResourceFileAsString (Constants.TIPS_FILE);

        Element root = DOM4JUtils.stringAsElement (tipsXML);

        for (Element el : root.elements (XMLConstants.tip))
        {

            this.baseTips.add (new Tip (el));

        }

        this.tips = new ArrayList<> (this.baseTips);

    }

    public StringProperty getNextTip ()
    {

        if (this.tips.size () == 0)
        {

            this.tips = new ArrayList (this.baseTips);

        }

        int n = this.ind.nextInt (this.tips.size ());

        if ((n == this.lastInd)
            &&
            (this.tips.size () > 1)
           )
        {

            return this.getNextTip ();

        }

        this.lastInd = n;

        Tip t = this.tips.remove (n);

        // See if there is a condition.
        StringProperty text = t.getText (this.viewer);

        if (text == null)
        {

            return this.getNextTip ();

        }

        return text;

    }

    private class Tip
    {

        public class XMLConstants
        {

            public static final String id = "id";
            public static final String item = "item";

        }

        private List<Item> items = new ArrayList ();

        public Tip (Element root)
                    throws  GeneralException
        {

            String id = DOM4JUtils.attributeValue (root,
                                                     XMLConstants.id);

            for (Element el : root.elements (XMLConstants.item))
            {

                this.items.add (new Item (id,
                                          el));

            }

        }

        public StringProperty getText (AbstractViewer viewer)
        {

            for (Item it : this.items)
            {

                if (it.shouldShow (viewer))
                {

                    return it.getText ();

                }

            }

            return null;

        }

        private class Item
        {

            public class XMLConstants
            {

                public static final String id = "id";
                public static final String condition = "condition";

            }

            private List<String> conds = new ArrayList ();
            private StringProperty text = null;

            public Item (String  tipId,
                         Element root)
                         throws  GeneralException
            {

                String id = tipId;

                String iid = DOM4JUtils.attributeValue (root,
                                                          XMLConstants.id,
                                                          false);

                if (iid != null)
                {

                    id = tipId + "_" + iid;

                }

                String cs = DOM4JUtils.attributeValue (root,
                                                         XMLConstants.condition,
                                                         false);

                if (cs != null)
                {

                    StringTokenizer t = new StringTokenizer (cs,
                                                             ",;");

                    while (t.hasMoreTokens ())
                    {

                        this.conds.add (t.nextToken ().trim ().toLowerCase ());

                    }

                }

                this.text = getUILanguageStringProperty (LanguageStrings.tips,id);

                if (this.text == null)
                {

                    throw new GeneralException ("Unable to find language string for tip/item: " +
                                             id);

                }
                //JDOMUtils.getChildContent (root);

            }

            public StringProperty getText ()
            {

                return this.text;

            }

            public boolean shouldShow (AbstractViewer viewer)
            {

                for (String c : this.conds)
                {

                    boolean not = c.startsWith ("!");

                    if (not)
                    {

                        c = c.substring (1);

                    }

                    boolean v = false;

                    if (c.equals ("landingviewer"))
                    {

                        v = (viewer instanceof AllProjectsViewer);

                    }

                    if (c.equals ("allprojectsviewer"))
                    {

                        v = (viewer instanceof AllProjectsViewer);

                    }

                    if (c.equals ("projectviewer"))
                    {

                        v = (viewer instanceof ProjectViewer);

                    }

                    if (c.equals ("warmupsviewer"))
                    {

                        v = (viewer instanceof WarmupProjectViewer);

                    }

                    if (viewer instanceof AbstractProjectViewer)
                    {

                        AbstractProjectViewer pv = (AbstractProjectViewer) viewer;

                        Panel qp = pv.getCurrentlyVisibleTab ();

                        if (qp == null)
                        {

                            return false;

                        }

                        javafx.scene.Node qpc = qp.getContent ();

                        if (qp != null)
                        {

                            if ((c.equals ("chaptertab"))
                                &&
                                (qpc instanceof NamedObjectPanelContent)
                               )
                            {

                                v = (((NamedObjectPanelContent) qpc).getObject () instanceof Chapter);

                            }

                            if (c.equals ("ideaboard"))
                            {

                                v = qp.getPanelId ().equals (IdeaBoard.PANEL_ID);

                            }

                        }

                        if (c.equals ("spellcheckon"))
                        {

                            v = pv.spellCheckingEnabledProperty ().getValue ();

                        }

                        if (c.equals ("spellcheckoff"))
                        {

                            v = !pv.spellCheckingEnabledProperty ().getValue ();

                        }

                    }

                    if (v)
                    {

                        if (not)
                        {

                            return false;

                        }

                    } else {

                        if (!not)
                        {

                            return false;

                        }


                    }

                }

                return true;

            }

        }

    }

}
