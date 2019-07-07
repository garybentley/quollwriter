package com.quollwriter.ui.fx.panels;

import javafx.scene.input.*;
import javafx.beans.property.*;
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
public abstract class ChapterEditorPanelContent<E extends AbstractProjectViewer, P extends javax.swing.JComponent> extends NamedObjectPanelContent<E, Chapter>
{

    private P chapterPanel = null;
    protected javafx.beans.property.StringProperty selectedTextProp = null;

    public ChapterEditorPanelContent (E       viewer,
                                      Chapter chapter)
    {

        super (viewer,
               chapter);

        this.selectedTextProp = new SimpleStringProperty ();

    }

    public StringProperty selectedTextProperty ()
    {

        return this.selectedTextProp;

    }

    @Override
    public void init (State s)
               throws GeneralException
    {

        final ChapterEditorPanelContent _this = this;

/*
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
*/
        super.init (s);

    }

    public abstract P getChapterPanel ()
                                throws Exception;

    @Override
    public Panel createPanel ()
    {

        SwingNode n = new SwingNode ();

        SwingUIUtils.doLater (() ->
        {

            try
            {

                this.chapterPanel = this.getChapterPanel ();
                //this.chapterPanel.init (null);
                n.setContent (this.chapterPanel);

            } catch (Exception e) {

                // TODO Improve.
                Environment.logError ("HERE",
                                      e);

            }

            try{
    this.init (null);
    }catch(Exception e) {e.printStackTrace ();}

        });

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

/*
TODO ? Remove?
    @Override
    public void saveObject ()
                     throws Exception
    {

        this.object.setText (this.getChapterPanel ().getEditor ().getTextWithMarkup ());

        super.saveObject ();

    }
*/
    public static String getPanelIdForChapter (Chapter c)
    {

        return "chapter-" + c.getKey ();

    }

    public ReadabilityIndices getReadabilityIndices ()
    {

        // TODO return this.chapterPanel.getReadabilityIndices ();
        return null;

    }

    public StringWithMarkup getText ()
    {

        return null;
        // TODO return this.chapterPanel.getEditor ().getTextWithMarkup ();

    }

}
