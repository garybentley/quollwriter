package com.quollwriter.editors.ui.panels;

import java.io.*;

import java.text.*;

import java.util.*;
import java.util.stream.*;

import javafx.geometry.*;
import javafx.scene.control.*;
import javafx.scene.*;
import javafx.scene.input.*;

//import org.incava.util.diff.*;

import com.gentlyweb.properties.*;

import com.quollwriter.*;
import com.quollwriter.ui.fx.*;
import com.quollwriter.editors.ui.*;
import com.quollwriter.ui.fx.panels.*;
import com.quollwriter.ui.fx.components.*;
import com.quollwriter.data.*;
import com.quollwriter.data.comparators.*;

import com.quollwriter.text.*;
import com.quollwriter.text.rules.*;

import static com.quollwriter.LanguageStrings.*;
import static com.quollwriter.uistrings.UILanguageStringsManager.getUILanguageStringProperty;

public class ChapterCommentsPanel extends ChapterEditorWithMarginPanelContent<ProjectSentReceivedViewer> implements ToolBarSupported
{

     public ChapterCommentsPanel (ProjectSentReceivedViewer pv,
                                  Chapter                   c)
                           throws GeneralException
     {

        super (pv,
               c);

        //this.projectViewer = pv;

        this.editor.setEditable (false);

        UIUtils.addStyleSheet (this,
                               Constants.PANEL_STYLESHEET_TYPE,
                               "chapteredit");
        UIUtils.addStyleSheet (this,
                               Constants.PANEL_STYLESHEET_TYPE,
                               "editorchapteredit");

        final ChapterCommentsPanel _this = this;

    }

    @Override
    public Boolean canDrag (ChapterItem ci)
    {

        return false;

    }

    @Override
    public Map<KeyCombination, Runnable> getActionMappings ()
    {

        return new HashMap<> ();

    }

    @Override
    public Set<Node> getToolBarItems ()
    {

        Set<Node> its = new LinkedHashSet<> ();

        its.add (QuollButton.builder ()
            .tooltip (editors,projectsent,commentspanel,toolbar,textproperties,tooltip)
            .iconName (StyleClassNames.EDITPROPERTIES)
            .onAction (ev ->
            {

                this.viewer.runCommand (ProjectSentReceivedViewer.CommandId.textproperties);

            })
            .build ());

        return its;

    }

    /*
    public void showDifferences (Chapter c)
    {

         BlockPainter newp = new BlockPainter (Color.GREEN);
         BlockPainter modp = new BlockPainter (Color.YELLOW);
         BlockPainter oldp = new BlockPainter (Color.RED);

         String ot = TextUtilities.stripNonValidXMLCharacters (this.editor.getTextWithMarkup ().getText ());

         String nt = TextUtilities.stripNonValidXMLCharacters (c.getText ().getText ());

         this.editor.setTextWithMarkup (c.getText ());

         String[] oldText = ot.split ("\\n");
         String[] newText = nt.split ("\\n");
System.out.println ("OT: " + ot);
System.out.println ("NT: " + nt);
//LinkedList<Diff>
diff_match_patch dmp = new diff_match_patch ();
   System.out.println ("DIFF2: " + dmp.patch_make(ot, nt));

         List diffs = new Diff (oldText,
                                newText).diff ();
System.out.println ("DIFF: " + diffs);
         for (int i = 0; i < diffs.size (); i++)
         {

             Difference d = (Difference) diffs.get (i);

             if (d.getDeletedEnd () == Difference.NONE)
             {

                 // This is an addition.
                 for (int k = d.getAddedStart (); k < (d.getAddedEnd () + 1); k++)
                 {

                     this.editor.addHighlight (d.getAddedStart (),
                                               d.getAddedEnd (),
                                               newp,
                                               false);

                 }

                 continue;

             }

             if (d.getAddedEnd () == Difference.NONE)
             {

                 // This is a deletion.
                 for (int k = d.getDeletedStart (); k < (d.getDeletedEnd () + 1); k++)
                 {

                     this.editor.addHighlight (d.getDeletedStart (),
                                               d.getDeletedEnd (),
                                               oldp,
                                               false);

                 }

                 continue;

             }

             // This is a modification.
             for (int k = d.getAddedStart (); k < (d.getAddedEnd () + 1); k++)
             {

                  this.editor.addHighlight (d.getAddedStart (),
                                            d.getAddedEnd (),
                                            modp,
                                            false);

             }

         }

    }
    */

    @Override
    public Node getMarginNodeForChapterItem (ChapterItem ci)
    {

        if (ci instanceof Note)
        {

            Note n = (Note) ci;

            IconBox riv = IconBox.builder ()
                .iconName (StyleClassNames.COMMENT)
                .build ();

            riv.setOnMouseClicked (ev ->
            {

                if (ev.getButton () != MouseButton.PRIMARY)
                {

                    return;

                }

                this.showItem (n,
                               true);

                ev.consume ();

            });

            riv.setOnContextMenuRequested (ev ->
            {

                Set<MenuItem> items = new LinkedHashSet<> ();

                if (n.isDealtWith ())
                {

                    items.add (QuollMenuItem.builder ()
                        .label (getUILanguageStringProperty (Arrays.asList ("Set undealt with")))
                        .iconName (StyleClassNames.UNDEALTWITH)
                        .onAction (eev ->
                        {

                            n.setDealtWith (null);

                        })
                        .build ());

                } else {

                    items.add (QuollMenuItem.builder ()
                        .label (getUILanguageStringProperty (Arrays.asList ("Set dealt with")))
                        .iconName (StyleClassNames.DEALTWITH)
                        .onAction (eev ->
                        {

                            n.setDealtWith (new Date ());

                        })
                        .build ());

                }

                UIUtils.showContextMenu (riv,
                                         items,
                                         ev.getScreenX (),
                                         ev.getScreenY ());

            });

            return riv;

        }

        throw new UnsupportedOperationException ("Object not supported: " + ci);

    }

    @Override
    public Set<MenuItem> getMarginContextMenuItems (int cpos)
    {

        return new HashSet<> ();

    }

    @Override
    public Set<MenuItem> getContextMenuItems (boolean    compress)
    {

        Set<MenuItem> ret = new LinkedHashSet<> ();

        int pos = this.editor.getTextPositionForCurrentMousePosition ();

        if (compress)
        {

            List<Node> row1 = new ArrayList<> ();

            row1.add (QuollButton.builder ()
                .iconName (StyleClassNames.EDITPROPERTIES)
                .tooltip (getUILanguageStringProperty (editors,projectsent,commentspanel,popupmenu,Chapter.OBJECT_TYPE,items,textproperties,tooltip))
                .onAction (ev ->
                {

                    this.viewer.runCommand (ProjectSentReceivedViewer.CommandId.textproperties);

                })
                .build ());

            row1.add (QuollButton.builder ()
                .iconName (StyleClassNames.FIND)
                .tooltip (getUILanguageStringProperty (editors,projectsent,commentspanel,popupmenu,Chapter.OBJECT_TYPE,items,find,tooltip))
                .onAction (ev ->
                {

                    this.viewer.showFind ();

                })
                .build ());

            CustomMenuItem n = UIUtils.createCompressedMenuItem (getUILanguageStringProperty (editors,projectsent,commentspanel,popupmenu,Chapter.OBJECT_TYPE,compresstext),
                                                                 row1);

            ret.add (n);

        } else {

            ret.add (QuollMenuItem.builder ()
                .iconName (StyleClassNames.EDITPROPERTIES)
                .label (getUILanguageStringProperty (editors,projectsent,commentspanel,popupmenu,Chapter.OBJECT_TYPE,items,textproperties,text))
                .onAction (ev ->
                {

                    this.viewer.runCommand (ProjectSentReceivedViewer.CommandId.textproperties);

                })
                .build ());

            ret.add (QuollMenuItem.builder ()
                .iconName (StyleClassNames.FIND)
                .label (getUILanguageStringProperty (editors,projectsent,commentspanel,popupmenu,Chapter.OBJECT_TYPE,items,find,text))
                .onAction (ev ->
                {

                    this.viewer.showFind ();

                })
                .build ());

        }

        return ret;

    }

    @Override
    public void showItem (ChapterItem item,
                          boolean     showAllForLine)
    {

        Note n = (Note) item;

        Note top = n;
        Set<Note> items = null;

        if (showAllForLine)
        {

            items = this.getNotesForPosition (item.getPosition ());

            if (items.size () == 0)
            {

                return;

            }

            top = items.iterator ().next ();

        } else {

            items = new LinkedHashSet<> ();
            items.add (n);

        }

        QuollPopup qp = this.viewer.getPopupById (ViewCommentPopup.getPopupIdForComment (top));

        if (qp != null)
        {

            qp.toFront ();
            return;

        }

        qp = new ViewCommentPopup (this.viewer,
                                   items,
                                   false).getPopup ();

        this.showPopupForItem (top,
                               qp);

    }

    public Set<Note> getNotesForPosition (int p)
    {

        Bounds cb = this.editor.getBoundsForPosition (p);

        if (cb == null)
        {

            return new HashSet<> ();

        }

        int paraNo = this.editor.getParagraphForOffset (p);

        double y = cb.getMinY ();

        Set<Note> ret = new TreeSet<> (new ChapterItemSorter ());

        ret.addAll (this.object.getNotes ().stream ()
            // Only interested in those that have the same y value.  i.e. on the same line.
            .filter (i ->
            {

                // See if we are in the same paragraph.
                if (this.editor.getParagraphForOffset (i.getPosition ()) != paraNo)
                {

                    return false;

                }

                Bounds b = this.editor.getBoundsForPosition (i.getPosition ());

                return (b != null) && b.getMinY () == y;

            })
            .collect (Collectors.toSet ()));

        return ret;

    }

/*
    public void showNote (Note n)
                   throws GeneralException
    {

        this.scrollToNote (n);

        this.iconColumn.showItem (n);

    }
*/
/*
   public void removeItemHighlightTextFromEditor (ChapterItem it)
   {

      this.editor.removeAllHighlights (this.highlight);

   }

   public void highlightItemTextInEditor (ChapterItem it)
   {

      this.editor.removeAllHighlights (this.highlight);
      this.editor.addHighlight (it.getStartPosition (),
                                it.getEndPosition (),
                                this.highlight,
                                false);

   }
*/
}
