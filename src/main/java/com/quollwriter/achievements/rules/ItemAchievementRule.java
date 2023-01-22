package com.quollwriter.achievements.rules;

import java.util.*;

import org.dom4j.*;


import com.quollwriter.*;
import com.quollwriter.data.*;
import com.quollwriter.ui.fx.*;
import com.quollwriter.ui.fx.viewers.*;

public class ItemAchievementRule extends AbstractAchievementRule
{

    public static final String RULE_TYPE = "item";

    public static Map<String, Class> objTypeToClassMapping = new HashMap<> ();

    static
    {

        Map<String, Class> m = ItemAchievementRule.objTypeToClassMapping;

        m.put (Note.OBJECT_TYPE,
               Note.class);
        m.put (Chapter.OBJECT_TYPE,
               Chapter.class);
        m.put (QCharacter.OBJECT_TYPE,
               QCharacter.class);
        m.put (QObject.OBJECT_TYPE,
               QObject.class);
        m.put (Location.OBJECT_TYPE,
               Location.class);
        m.put (IdeaType.OBJECT_TYPE,
               IdeaType.class);
        m.put (Idea.OBJECT_TYPE,
               Idea.class);
        m.put (OutlineItem.OBJECT_TYPE,
               OutlineItem.class);
        m.put (Scene.OBJECT_TYPE,
               Scene.class);
        m.put (ResearchItem.OBJECT_TYPE,
               ResearchItem.class);

    }

    public class XMLConstants
    {

        public static final String action = "action";
        public static final String actions = "actions";
        public static final String objectType = "objectType";
        public static final String count = "count";
        public static final String match = "match";
        public static final String asset = "asset";

    }

    private Set<String> actions = new HashSet<> ();
    private String objType = null;
    private ObjectMatch match = null;
    private int count = 0;
    private Class objClass = null;
    private boolean asset = false;

    public ItemAchievementRule (Element root)
                                throws  GeneralException
    {

        super (root);

        this.asset = DOM4JUtils.attributeValueAsBoolean (root,
                                                         XMLConstants.asset,
                                                         false);

        this.objType = DOM4JUtils.attributeValue (root,
                                                    XMLConstants.objectType,
                                                    true);

        this.objClass = ItemAchievementRule.objTypeToClassMapping.get (this.objType);

        if (this.objClass == null)
        {

            throw new GeneralException ("Object type: " +
                                     this.objType +
                                     ", referenced by: " +
                                     DOM4JUtils.getPath (root.attribute (XMLConstants.objectType)) +
                                     " is not supported.");

        }

        String act = DOM4JUtils.attributeValue (root,
                                                  XMLConstants.action,
                                                  false);

        if (act != null)
        {

            this.eventIds.add ((this.asset ? "asset" : this.objType) + "." + act.toLowerCase ());

        }

        String acts = DOM4JUtils.attributeValue (root,
                                                   XMLConstants.actions,
                                                   false);

        if (acts != null)
        {

            StringTokenizer t = new StringTokenizer (acts,
                                                     ",;");

            while (t.hasMoreTokens ())
            {

                this.eventIds.add ((this.asset ? "asset" : this.objType) + t.nextToken ().toLowerCase ());

            }

        }

        Element mEl = root.element (XMLConstants.match);

        if (mEl != null)
        {

            this.match = new ObjectMatch (mEl);

        }

        this.count = DOM4JUtils.attributeValueAsInt (root,
                                                     XMLConstants.count,
                                                     false);

    }

    public boolean shouldPersistState ()
    {

        return false;

    }

    @Override
    public boolean achieved (AbstractProjectViewer viewer,
                             ProjectEvent          ev)
                             throws                Exception
    {

        return this.achieved (viewer);

    }

    @Override
    public boolean achieved (ProjectEvent          ev)
    {

        return false;

    }

    @Override
    public boolean achieved (AbstractProjectViewer viewer)
                             throws                Exception
    {

        Set<NamedObject> objs = new HashSet<> ();

        if (this.asset)
        {

            Set<UserConfigurableObjectType> types = viewer.getProject ().getAssetTypes ();

            for (UserConfigurableObjectType t : types)
            {

                // This only applies to legacy types.
                if ((t.isAssetObjectType ())
                    &&
                    (t.isLegacyObjectType ())
                    &&
                    (t.getUserObjectType ().equals (this.objType))
                   )
                {

                    viewer.getProject ().getAssets (t).stream ()
                        .forEach (o ->
                        {

                            objs.add (o);

                        });
                    break;

                }
            }

        } else {

            // Get all the objects of the required type.
            viewer.getProject ().getAllNamedChildObjects (this.objClass).stream ()
                .forEach (o ->
                {

                    objs.add (o);

                });

        }

        int c = objs.size ();

        if (this.match != null)
        {

            c = 0;

            for (NamedObject n : objs)
            {

                if (this.match.match (n))
                {

                    c++;

                }

            }

        }

        return c >= this.count;

    }

    @Override
    public void init (Element root)
    {



    }

    @Override
    public void fillState (Element root)
    {

    }

}
