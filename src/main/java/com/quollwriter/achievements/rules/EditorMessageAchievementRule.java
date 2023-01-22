package com.quollwriter.achievements.rules;

import java.util.*;

import org.dom4j.*;

import com.quollwriter.*;
import com.quollwriter.data.*;
import com.quollwriter.ui.fx.*;
import com.quollwriter.editors.*;
import com.quollwriter.editors.messages.*;

public class EditorMessageAchievementRule extends AbstractAchievementRule implements EditorMessageListener
{

    public static final String RULE_TYPE = "editor-message";

    public class XMLConstants
    {

        public static final String messageTypes = "messageTypes";
        public static final String count = "count";
        public static final String sentByMe = "sentByMe";
        public static final String savedCount = "savedCount";
        public static final String match = "match";

    }

    private Set<String> messageTypes = new HashSet<> ();
    private ObjectMatch match = null;
    private int count = 0;
    private boolean sentByMe = true;
    private int savedCount = 0;

    public EditorMessageAchievementRule (Element root)
                                 throws  GeneralException
    {

        super (root);

        Element mEl = root.element (XMLConstants.match);

        if (mEl != null)
        {

            this.match = new ObjectMatch (mEl);

        }

        this.count = DOM4JUtils.attributeValueAsInt (root,
                                                       XMLConstants.count,
                                                       false);

        if (root.attribute (XMLConstants.sentByMe) != null)
        {

            this.sentByMe = DOM4JUtils.attributeValueAsBoolean (root,
                                                                  XMLConstants.sentByMe,
                                                                  false);

        }

        String mt = DOM4JUtils.attributeValue (root,
                                                 XMLConstants.messageTypes);

        StringTokenizer t = new StringTokenizer (mt.toLowerCase (),
                                                 ",");

        while (t.hasMoreTokens ())
        {

            String tok = t.nextToken ().trim ();

            try
            {

                EditorMessage m = MessageFactory.getInstance (tok);

                if (this.match != null)
                {

                    // Just a check to see if the accessor will work.
                    this.match.match (m);

                }

                this.messageTypes.add (tok);

            } catch (Exception e) {

                throw new GeneralException ("Invalid message type: " +
                                         tok +
                                         ", referenced by: " +
                                         DOM4JUtils.getPath (root),
                                         e);

            }

        }

        EditorsEnvironment.addEditorMessageListener (this);

    }

    public boolean shouldPersistState ()
    {

        return (this.count > 0);

    }

    public void handleMessage (EditorMessageEvent ev)
    {

        if (ev.getType () != EditorMessageEvent.MESSAGE_ADDED)
        {

            return;

        }

        EditorMessage mess = ev.getMessage ();

        if (!this.messageTypes.contains (mess.getMessageType ()))
        {

            return;

        }

        if ((mess.isSentByMe () && !this.sentByMe)
            ||
            (!mess.isSentByMe () && this.sentByMe)
           )
        {

            return;

        }

        try
        {

            if ((this.match != null)
                &&
                (!this.match.match (mess))
               )
            {

                return;

            }

        } catch (Exception e) {

            Environment.logError ("Unable to check for message match: " +
                                  mess,
                                  e);

            return;

        }

        this.savedCount++;

        if (this.savedCount < this.count)
        {

            return;

        }

        Environment.getAchievementsManager ().userAchievementReached (this);

        EditorsEnvironment.removeEditorMessageListener (this);

    }

    public void init (Element root)
    {

        if (this.count > 0)
        {

            try
            {

                this.savedCount = DOM4JUtils.attributeValueAsInt (root,
                                                                    XMLConstants.savedCount,
                                                                    false);
                //this.savedCount = 0;
            } catch (Exception e) {

                Environment.logError ("Unable to set saved count for rule: " +
                                      this,
                                      e);

            }

        }

    }

    @Override
    public void fillState (Element root)
    {

        root.addAttribute (XMLConstants.savedCount,
                           String.valueOf (this.savedCount));

    }

}
