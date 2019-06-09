package com.quollwriter.ui.fx.panels;

import javax.swing.*;
import javax.swing.event.*;

import javafx.scene.layout.*;
import javafx.embed.swing.*;

import com.quollwriter.synonyms.*;
import com.quollwriter.data.*;
import com.quollwriter.*;
import com.quollwriter.ui.fx.*;
import com.quollwriter.ui.fx.viewers.*;
import com.quollwriter.ui.fx.components.*;
import com.quollwriter.ui.fx.swing.*;

/**
 * A base class for content that is suitable for display within a panel for a specific named object.
 */
public abstract class ChapterEditorPanelContent<E extends AbstractProjectViewer, P extends AbstractEditorPanel> extends NamedObjectPanelContent<E, Chapter>
{

    private P chapterPanel = null;

    public ChapterEditorPanelContent (E       viewer,
                                      Chapter chapter)
    {

        super (viewer,
               chapter);

    }

    @Override
    public void init (State s)
               throws GeneralException
    {

        final ChapterEditorPanelContent _this = this;

        SwingUIUtils.doLater (() ->
        {

            try
            {

                _this.getChapterPanel ().init (s);

            } catch (Exception e) {

                Environment.logError ("Unable to init the panel for chapter: " +
                                      this.object,
                                      e);

            }

        });

        super.init (s);

    }

    public abstract P getChapterPanel ();

    @Override
    public Panel createPanel ()
    {

        final ChapterEditorPanelContent _this = this;

        //this.chapterPanel = this.getChapterPanel ();

        SwingNode n = new SwingNode ();

        _this.chapterPanel = _this.getChapterPanel ();

        n.setContent (_this.chapterPanel);

        //this.getChildren ().add (n);

        VBox.setVgrow (n, Priority.ALWAYS);

        this.getChildren ().add (n);

        Panel panel = Panel.builder ()
            // TODO .title (this.object.nameProperty ())
            .title (this.object.nameProperty ())
            .content (this)
            .styleClassName (StyleClassNames.CHAPTER)
            .panelId (getPanelIdForChapter (this.object))
            // TODO .headerControls ()
            .build ();

        return panel;

    }

    public static String getPanelIdForChapter (Chapter c)
    {

        return "chapter-" + c.getKey ();

    }

    public ReadabilityIndices getReadabilityIndices ()
    {

        return this.chapterPanel.getReadabilityIndices ();

    }

    public StringWithMarkup getText ()
    {

        return this.chapterPanel.getEditor ().getTextWithMarkup ();

    }

}
