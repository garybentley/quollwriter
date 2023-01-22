package com.quollwriter.editors.ui;

import java.util.*;
import javafx.scene.*;
import javafx.beans.property.*;
import javafx.scene.layout.*;

import com.quollwriter.data.*;
import com.quollwriter.data.editors.*;
import com.quollwriter.*;
import com.quollwriter.editors.messages.*;
import com.quollwriter.ui.fx.components.*;
import com.quollwriter.ui.fx.*;
import com.quollwriter.ui.fx.viewers.*;
import com.quollwriter.uistrings.UILanguageStringsManager;

import static com.quollwriter.LanguageStrings.*;
import static com.quollwriter.uistrings.UILanguageStringsManager.getUILanguageStringProperty;

public class MessageAccordionItem<E extends EditorMessage> extends VBox
{

    protected AbstractViewer viewer = null;
    protected Date date = null;
    protected AccordionItem accItem = null;
    private StringProperty titleProp = new SimpleStringProperty ();

    public MessageAccordionItem (AbstractViewer viewer,
                                 Date           d,
                                 Set<E>         messages)
    {

        if (messages == null)
        {

            throw new IllegalArgumentException ("Messages is null.");

        }

        this.managedProperty ().bind (this.visibleProperty ());
        this.date = d;

        //this.getStyleClass ().add (StyleClassNames.CHATMESSAGES);

        this.viewer = viewer;

        for (E m : messages)
        {

            this.addMessage (m);

        }

        this.updateHeaderTitle ();

    }

    public void updateHeaderTitle ()
    {

        StringProperty dateName = null;

        if (Utils.isToday (this.date))
        {

            dateName = getUILanguageStringProperty (times,today);
            //"Today";

        }

        if (Utils.isYesterday (this.date))
        {

            dateName = getUILanguageStringProperty (times,yesterday);
            //"Yesterday";

        }

        if (dateName == null)
        {

            dateName = UILanguageStringsManager.createStringPropertyWithBinding (() ->
            {

                return Environment.formatDate (this.date);

            });

        }

        int c = this.getChildren ().size ();

        this.titleProp.unbind ();
        this.titleProp.bind (getUILanguageStringProperty (Arrays.asList (editors,editor,view,chatmessages,title),
                                                          dateName,
                                                          c));

    }

    public AccordionItem getAccordionItem ()
    {

        if (this.accItem == null)
        {

            this.accItem = this.createAccordionItem ();

        }

        return this.accItem;

    }

    public AccordionItem createAccordionItem ()
    {

        AccordionItem acc = AccordionItem.builder ()
            .openContent (this)
            .title (this.titleProp)
            .styleClassName (StyleClassNames.CHATMESSAGES)
            .build ();

        return acc;

    }

    public void addMessage (E m)
    {

        Node mb = this.getMessageBox (m);

        if (mb == null)
        {

            return;

        }

        this.getChildren ().add (mb);

        this.updateHeaderTitle ();

    }

    public Node getMessageBox (E m)
    {

        MessageBox mb = null;

        try
        {

            mb = MessageBoxFactory.getMessageBoxInstance (m,
                                                          this.viewer);

        } catch (Exception e) {

            Environment.logError ("Unable to get message box for message: " +
                                  m,
                                  e);

            return null;

        }
/*
        Box b = new Box (BoxLayout.Y_AXIS);
        b.setAlignmentX (Component.LEFT_ALIGNMENT);

        Box details = new Box (BoxLayout.X_AXIS);
        details.setAlignmentX (Component.LEFT_ALIGNMENT);

        String name = m.getEditor ().getMainName ();

        if (m.isSentByMe ())
        {

            name = "Me";

        }

        details.add (this.createLabel (name));
        details.add (Box.createHorizontalGlue ());
        details.add (this.createLabel (Environment.formatTime (m.getWhen ())));

        b.add (details);

        b.add (Box.createVerticalStrut (5));

        mb.setAlignmentX (Component.LEFT_ALIGNMENT);

        b.add (mb);
*/
        return mb;

    }

}
