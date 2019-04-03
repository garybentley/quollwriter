package com.quollwriter.ui.fx.sidebars;

import java.util.*;

import javafx.beans.property.*;
import javafx.scene.*;
import javafx.scene.control.*;

import com.quollwriter.*;
import com.quollwriter.data.editors.*;
import com.quollwriter.ui.fx.*;
import com.quollwriter.ui.fx.viewers.*;
import com.quollwriter.ui.fx.components.*;

import static com.quollwriter.uistrings.UILanguageStringsManager.getUILanguageStringProperty;
import static com.quollwriter.LanguageStrings.*;

public class EditorsSideBar<E extends AbstractViewer> extends SideBarContent
{

    public static final String SIDEBAR_ID = "editors";

    public EditorsSideBar (E viewer)
    {

        super (viewer);

        //this.getChildren ().add (new TextField ("test"));

    }

    @Override
    public SideBar createSideBar ()
    {

        StringProperty title = getUILanguageStringProperty (editors,LanguageStrings.sidebar,LanguageStrings.title);

        return SideBar.builder ()
            .title (title)
            .activeTitle (title)
            //.contextMenu ()?
            .styleClassName (StyleClassNames.CONTACTS)
            .withScrollPane (true)
            .canClose (true)
            //.headerControls ()?
            .withViewer (this.viewer)
            .content (this)
            .sideBarId (SIDEBAR_ID)
            .build ();

    }

    @Override
    public void init (State s)
    {

        super.init (s);

    }

    @Override
    public State getState ()
    {

        return super.getState ();

    }

    public void showChatBox (final EditorEditor ed)
                      throws GeneralException
    {

        this.showEditor (ed);

        // TODO
/*
        final EditorsSideBar _this = this;

        UIUtils.doLater (new ActionListener ()
        {

            @Override
            public void actionPerformed (ActionEvent ev)
            {

                EditorPanel edPanel = _this.getEditorPanel (ed);

                if (edPanel != null)
                {

                    edPanel.showChatBox ();

                }

            }

        });
*/
    }

    public void showEditor (EditorEditor ed)
                     throws GeneralException
    {

        // TODO
/*
        final EditorsSideBar _this = this;

        EditorPanel edPanel = this.getEditorPanel (ed);

        if (edPanel != null)
        {

            this.editorChanged (new EditorChangedEvent (ed,
                                                        EditorChangedEvent.EDITOR_CHANGED));

            this.tabs.setSelectedComponent (edPanel);
            this.tabs.revalidate ();
            this.tabs.repaint ();

            return;

        }

        if (!ed.messagesLoaded ())
        {

            try
            {

                EditorsEnvironment.loadMessagesForEditor (ed);

            } catch (Exception e) {

                throw new GeneralException ("Unable to load messages for editor: " +
                                            ed,
                                            e);

            }

        }

        EditorPanel ep = new EditorPanel (this,
                                          ed);
        ep.init ();

        ep.setOpaque (true);
        ep.setBackground (UIUtils.getComponentColor ());
        ep.setAlignmentX (Component.LEFT_ALIGNMENT);
        ep.setBorder (UIUtils.createPadding (5, 5, 5, 0));

        int ind = this.tabs.getTabCount ();

        this.tabs.add (ep);

        final JLabel th = new JLabel ();
        th.setBorder (new CompoundBorder (UIUtils.createPadding (2, 0, 0, 0),
                                          UIUtils.createLineBorder ()));

        th.setMaximumSize (new Dimension (100, 100));

        this.tabs.setTabComponentAt (ind,
                                     th);

        this.addTabHeaderMouseHandler (th,
                                       ep);

        this.showEditor (ed);
*/
    }

}
