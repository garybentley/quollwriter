package com.quollwriter.ui.fx.sidebars;

import java.util.*;

import javafx.beans.property.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import com.quollwriter.*;
import com.quollwriter.data.*;
import com.quollwriter.ui.fx.components.*;
import com.quollwriter.ui.fx.*;
import com.quollwriter.ui.fx.viewers.*;

import static com.quollwriter.uistrings.UILanguageStringsManager.getUILanguageStringProperty;
import static com.quollwriter.LanguageStrings.*;

public class WarmupsSideBar extends BaseSideBar<WarmupProjectViewer>
{

    public static final String SIDEBAR_ID = "warmups";

    private WarmupsSidebarItem   warmupsItem = null;
    private QuollTextView prompt = null;
    private VBox contentBox = null;
    private ScrollPane sp = null;

    public WarmupsSideBar (WarmupProjectViewer v)
    {

        super (v);

        this.contentBox = new VBox ();

        this.sp = new ScrollPane (this.contentBox);
        VBox.setVgrow (this.sp,
                       Priority.ALWAYS);
        this.setContent (this.sp);

    }

    @Override
    public void init (State s)
    {

        super.init (s);

        VBox promptWrapper = new VBox ();
        promptWrapper.getStyleClass ().add (StyleClassNames.PROMPT);
        promptWrapper.getChildren ().add (Header.builder ()
            .title (warmups,LanguageStrings.sidebar,warmups,LanguageStrings.prompt)
            .build ());
        promptWrapper.managedProperty ().bind (promptWrapper.visibleProperty ());

        QuollTextView prompt = QuollTextView.builder ()
            .inViewer (this.viewer)
            .build ();
        prompt.managedProperty ().bind (prompt.visibleProperty ());

        promptWrapper.getChildren ().add (prompt);

        this.contentBox.getChildren ().add (promptWrapper);

        if (this.viewer.currentWarmupProperty ().getValue () != null)
        {

            Warmup w = this.viewer.currentWarmupProperty ().getValue ();

            prompt.setText (UIUtils.formatPrompt (w.getPrompt ()));

        }

        promptWrapper.setVisible (this.viewer.currentWarmupProperty ().getValue () != null);

        this.addChangeListener (this.viewer.currentWarmupProperty (),
                                (pr, oldv, newv) ->
        {

            if (newv != null)
            {

                prompt.setText (UIUtils.formatPrompt (newv.getPrompt ()));
                promptWrapper.setVisible (true);

            } else {

                promptWrapper.setVisible (false);

            }

        });

        this.warmupsItem = new WarmupsSidebarItem (this.viewer,
                                                   this.getBinder ());

        AccordionItem ai = this.warmupsItem.getAccordionItem ();
        VBox.setVgrow (ai,
                       Priority.ALWAYS);

        this.contentBox.getChildren ().add (ai);

    }

    @Override
    public State getState ()
    {

        State s = super.getState ();

        return s;

    }

    @Override
    public SideBar createSideBar ()
    {

        StringProperty title = getUILanguageStringProperty (warmups,LanguageStrings.sidebar,warmups,LanguageStrings.title);

        return SideBar.builder ()
            .title (title)
            .activeTitle (title)
            //.contextMenu ()?
            .styleClassName (StyleClassNames.WARMUP)
            .styleSheet (StyleClassNames.PROJECT, StyleClassNames.WARMUP)
            .withScrollPane (false)
            .canClose (false)
            //.headerControls ()?
            .withViewer (this.viewer)
            .content (this)
            .sideBarId (SIDEBAR_ID)
            .build ();

    }

}
