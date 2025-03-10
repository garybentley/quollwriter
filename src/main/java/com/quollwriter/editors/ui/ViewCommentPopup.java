package com.quollwriter.editors.ui;

import java.util.*;
import java.util.function.*;

import javafx.scene.Node;
import javafx.scene.layout.*;
import javafx.scene.control.IndexRange;

import javafx.beans.property.*;

import com.quollwriter.*;
import com.quollwriter.data.*;
import com.quollwriter.editors.*;
import com.quollwriter.ui.fx.*;
import com.quollwriter.ui.fx.components.*;
import com.quollwriter.ui.fx.viewers.*;
import com.quollwriter.editors.ui.panels.*;
import com.quollwriter.ui.fx.popups.*;
import com.quollwriter.ui.fx.panels.*;

import static com.quollwriter.uistrings.UILanguageStringsManager.getUILanguageStringProperty;
import static com.quollwriter.LanguageStrings.*;

public class ViewCommentPopup extends PopupContent<AbstractProjectViewer>
{

    private static final String POPUP_ID = "viewcomment";
    private Set<Note> items = null;
    private TextEditor.Highlight highlight = null;

    public ViewCommentPopup (AbstractProjectViewer viewer,
                             Set<Note>             items,
                             boolean               itemEditable)
    {

        super (viewer);

        if ((items == null)
            ||
            (items.size () == 0)
           )
        {

            throw new IllegalArgumentException ("At least one note must be specified.");

        }

        this.items = items;

        VBox b = new VBox ();

        Node last = null;
        Node first = null;

        Runnable r = () ->
        {

            this.close ();

        };

        for (Note i : items)
        {

            Supplier<Set<Node>> extraControls = null;

            if (!itemEditable)
            {

                extraControls = () ->
                {

                    Set<Node> cons = new LinkedHashSet<> ();

                    QuollButton d = QuollButton.builder ()
                        .iconName (i.isDealtWith () ? StyleClassNames.UNDEALTWITH : StyleClassNames.DEALTWITH)
                        .onAction (ev ->
                        {

                            if (i.isDealtWith ())
                            {

                                i.setDealtWith (null);

                            } else {

                                i.setDealtWith (new Date ());

                            }

                        })
                        .build ();

                    this.getBinder ().addChangeListener (i.dealtWithProperty (),
                                                         (pr, oldv, newv) ->
                    {

                        d.setIconName (i.isDealtWith () ? StyleClassNames.UNDEALTWITH : StyleClassNames.DEALTWITH);

                    });

                    cons.add (d);

                    return cons;

                };

            }

            CommentItemFormatter form = new CommentItemFormatter (this.viewer,
                                                                  this.getBinder (),
                                                                  i,
                                                                  r,
                                                                  itemEditable,
                                                                  extraControls);

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

        last.pseudoClassStateChanged (StyleClassNames.LAST_PSEUDO_CLASS, true);

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

        this.getChildren ().add (b);

    }

    @Override
    public QuollPopup createPopup ()
    {

        Note top = this.items.iterator ().next ();

        QuollPopup p = QuollPopup.builder ()
            .title (getUILanguageStringProperty (objectnames,singular,StyleClassNames.COMMENT))
            .styleClassName (StyleClassNames.COMMENT)
            .headerIconClassName (StyleClassNames.COMMENT)
            .styleSheet ("comments")
            .hideOnEscape (true)
            .withClose (true)
            .content (this)
            .popupId (ViewCommentPopup.getPopupIdForComment (top))
            .removeOnClose (true)
            .withViewer (this.viewer)
            .build ();

        p.getStyleClass ().add (StyleClassNames.CHAPTERITEM);
        p.getStyleClass ().add (StyleClassNames.VIEW);

        p.toFront ();

        p.addEventHandler (QuollPopup.PopupEvent.CLOSED_EVENT,
                           ev ->
        {

            this.getBinder ().dispose ();

            Note ci = this.items.iterator ().next ();

            ChapterEditorPanelContent ed = this.viewer.getEditorForChapter (ci.getChapter ());

            if (ed != null)
            {

                ed.getEditor ().removeHighlight (this.highlight);

            }

        });

        return p;

    }

    public static String getPopupIdForComment (Note ci)
    {

        return POPUP_ID + ci.getObjectReference ().asString ();

    }

}
