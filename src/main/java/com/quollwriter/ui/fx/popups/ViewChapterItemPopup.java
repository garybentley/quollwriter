package com.quollwriter.ui.fx.popups;

import java.util.*;

import javafx.scene.Node;
import javafx.scene.layout.*;
import javafx.scene.control.IndexRange;

import javafx.beans.property.*;

import com.gentlyweb.utils.*;

import com.quollwriter.*;
import com.quollwriter.data.*;
import com.quollwriter.editors.*;
import com.quollwriter.ui.fx.*;
import com.quollwriter.ui.fx.components.*;
import com.quollwriter.ui.fx.viewers.*;
import com.quollwriter.ui.fx.panels.*;

import static com.quollwriter.uistrings.UILanguageStringsManager.getUILanguageStringProperty;
import static com.quollwriter.LanguageStrings.*;

public class ViewChapterItemPopup extends PopupContent<ProjectViewer>
{

    private static final String POPUP_ID = "viewchapteritem";
    private Set<ChapterItem> items = null;
    private PropertyBinder binder = new PropertyBinder ();
    private TextEditor.Highlight highlight = null;

    public ViewChapterItemPopup (ProjectViewer    viewer,
                                 Set<ChapterItem> items)
    {

        super (viewer);

        if ((items == null)
            ||
            (items.size () == 0)
           )
        {

            throw new IllegalArgumentException ("At least one item must be specified.");

        }

        this.items = items;

        VBox b = new VBox ();

        Node last = null;
        Node first = null;

        for (ChapterItem i : items)
        {

            ChapterItemFormatter form = this.getChapterItemFormatter (i);

            Node n = form.format ();
            n.getStyleClass ().add (StyleClassNames.CHAPTERITEM);
            n.getStyleClass ().add (form.getStyleClassName ());

            if (first == null)
            {

                first = n;
                n.pseudoClassStateChanged (StyleClassNames.FIRST_PSEUDO_CLASS, true);

            }

            last = n;

            b.getChildren ().add (n);

        }


        if (items.size () == 1)
        {

            ChapterItem ci = items.iterator ().next ();

            if (ci instanceof Note)
            {

                Note n = (Note) ci;

                if (n.isEditNeeded ())
                {

                    if (n.getEndPosition () > n.getStartPosition ())
                    {

                        this.highlight = this.viewer.getEditorForChapter (n.getChapter ()).getEditor ().addHighlight (new IndexRange (n.getStartPosition (),
                                                                                                                                      n.getEndPosition ()),
                                                                                                                      UserProperties.getEditNeededNoteChapterHighlightColor ());

                    }

                }

            }

        }

        last.pseudoClassStateChanged (StyleClassNames.LAST_PSEUDO_CLASS, true);

        this.getChildren ().add (b);

    }

    @Override
    public QuollPopup createPopup ()
    {

        ChapterItem top = this.items.iterator ().next ();

        ChapterItemFormatter form = this.getChapterItemFormatter (top);

        QuollPopup p = QuollPopup.builder ()
            .title (form.getPopupTitle ())
            .styleClassName (form.getStyleClassName ())
            .hideOnEscape (true)
            .withClose (true)
            .content (this)
            .popupId (ViewChapterItemPopup.getPopupIdForChapterItem (top))
            .removeOnClose (true)
            .withViewer (this.viewer)
            .build ();

        p.getStyleClass ().add (StyleClassNames.CHAPTERITEM);
        p.getStyleClass ().add (StyleClassNames.VIEW);

        p.toFront ();

        p.addEventHandler (QuollPopup.PopupEvent.CLOSED_EVENT,
                           ev ->
        {

            this.binder.dispose ();

            ChapterItem ci = this.items.iterator ().next ();

            ProjectChapterEditorPanelContent ed = this.viewer.getEditorForChapter (ci.getChapter ());

            if (ed != null)
            {

                ed.getEditor ().removeHighlight (this.highlight);

            }

        });

        return p;

    }

    public static String getPopupIdForChapterItem (ChapterItem ci)
    {

        return POPUP_ID + ci.getObjectReference ().asString ();

    }

    public ChapterItemFormatter getChapterItemFormatter (ChapterItem     ci)
    {

        Runnable r = () ->
        {

            this.close ();

        };

        // TODO Make nicer.
        if (ci instanceof com.quollwriter.data.Scene)
        {

            return new SceneItemFormatter (this.viewer,
                                           this.binder,
                                           (com.quollwriter.data.Scene) ci,
                                           r);

        }

        if (ci instanceof com.quollwriter.data.OutlineItem)
        {

            return new OutlineItemFormatter (this.viewer,
                                             this.binder,
                                             (com.quollwriter.data.OutlineItem) ci,
                                             r);

        }

        if (ci instanceof com.quollwriter.data.Note)
        {

            return new NoteItemFormatter (this.viewer,
                                          this.binder,
                                          (com.quollwriter.data.Note) ci,
                                          r);

        }

        return null;

    }

}
