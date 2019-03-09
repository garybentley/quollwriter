package com.quollwriter.ui.actionHandlers;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;

import com.quollwriter.*;

import com.quollwriter.data.*;

import com.quollwriter.ui.*;
import com.quollwriter.ui.components.ActionAdapter;
import com.quollwriter.ui.panels.*;

import static com.quollwriter.LanguageStrings.*;
import static com.quollwriter.Environment.getUIString;

public class DeleteChapterItemActionHandler extends ActionAdapter
{

    private AbstractProjectViewer projectViewer = null;
    private ChapterItem           item = null;
    private boolean               showAtItem = false;

    public DeleteChapterItemActionHandler (ChapterItem           item,
                                           AbstractProjectViewer projectViewer,
                                           boolean               showAtItem)
    {

        this.item = item;
        this.projectViewer = projectViewer;
        this.showAtItem = showAtItem;

    }

    public void actionPerformed (ActionEvent ev)
    {

        final DeleteChapterItemActionHandler _this = this;

        Point showAt = null;

        // Sigh...
        if (this.showAtItem)
        {

            // Get the editor panel.
            AbstractEditorPanel p = this.projectViewer.getEditorForChapter (this.item.getChapter ());

            if ((p != null)
                &&
                (p instanceof QuollEditorPanel)
               )
            {

                QuollEditorPanel qep = (QuollEditorPanel) p;

                showAt = qep.getIconColumn ().getLocation (this.item);

                showAt = SwingUtilities.convertPoint (qep,
                                                      showAt,
                                                      (this.projectViewer.getFullScreenFrame () != null ? this.projectViewer.getFullScreenFrame () : this.projectViewer));

                showAt.y -= qep.getScrollOffset ();

            }

        }

        UIUtils.createQuestionPopup (this.projectViewer,
                                     String.format (getUIString (chapteritems,delete,title),
                                                    Environment.getObjectTypeName (this.item)),
                                     Constants.DELETE_ICON_NAME,                                                    
                                     String.format (getUIString (chapteritems,delete,text),
                                                   Environment.getObjectTypeName (this.item),
                                                   this.item.getSummary ()),
                                     //"Delete " + Environment.getObjectTypeName (this.item),
                                     //Constants.DELETE_ICON_NAME,
                                     //"Please confirm you wish to delete " +
                                     //Environment.getObjectTypeName (this.item) +
                                     //" <b>" +
                                     //this.item.getSummary () +
                                     //"</b>?",
                                     getUIString (chapteritems,delete,buttons,confirm),
                                     //"Yes, delete it",
                                     getUIString (chapteritems,delete,buttons,cancel),
                                     //null,
                                     new ActionListener ()
                                     {

                                        public void actionPerformed (ActionEvent ev)
                                        {

                                            _this.doDelete ();

                                        }

                                     },
                                     null,
                                     null,
                                     showAt);

    }

    private void deleteItem (boolean deleteChildren)
    {

        try
        {

            this.projectViewer.deleteObject (this.item,
                                             deleteChildren);

        } catch (Exception e)
        {

            Environment.logError ("Unable to delete item: " +
                                  this.item,
                                  e);

            UIUtils.showErrorMessage (this.projectViewer,
                                      getUIString (chapteritems,delete,actionerror));
                                    //"Unable to delete.");

        }

    }

    private void doDelete ()
    {

        final DeleteChapterItemActionHandler _this = this;

        if (this.item instanceof Scene)
        {

            Scene s = (Scene) this.item;

            int c = s.getOutlineItems ().size ();

            if (c > 0)
            {

                String it = Environment.getObjectTypeName (OutlineItem.OBJECT_TYPE) + (c > 1 ? "s" : "");

                UIUtils.createQuestionPopup (this.projectViewer,
                                             String.format (getUIString (chapteritems,deletesceneoutlineitems,title),
                                                            //"Also delete %s %s",
                                                            Environment.formatNumber (c)),
                                             Constants.DELETE_ICON_NAME,
                                             String.format (getUIString (chapteritems,deletesceneoutlineitems,text),
                                                            //"The %s has %s associated %s.  Do you want to also delete them?",
                                                            //Environment.getObjectTypeName (this.item),
                                                            Environment.formatNumber (c)),
                                             getUIString (chapteritems,deletesceneoutlineitems,buttons,confirm),
                                             //"Yes, delete them",
                                             getUIString (chapteritems,deletesceneoutlineitems,buttons,cancel),
                                             //"No, keep them",
                                             new ActionListener ()
                                             {

                                                public void actionPerformed (ActionEvent ev)
                                                {

                                                    _this.deleteItem (true);

                                                }

                                             },
                                             new ActionListener ()
                                             {

                                                public void actionPerformed (ActionEvent ev)
                                                {

                                                    _this.deleteItem (false);

                                                }

                                             },
                                             null,
                                             null);

                return;

            }

        }

        this.deleteItem (false);

    }

}
