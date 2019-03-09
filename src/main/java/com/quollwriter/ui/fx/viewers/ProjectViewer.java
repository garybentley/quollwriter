package com.quollwriter.ui.fx.viewers;

import java.util.*;
import java.util.function.*;

import javafx.beans.property.*;
import javafx.scene.*;
import javafx.scene.control.*;

import com.quollwriter.data.*;
import com.quollwriter.ui.fx.*;
import com.quollwriter.*;
import com.quollwriter.ui.fx.panels.*;
import com.quollwriter.ui.fx.sidebars.*;
import com.quollwriter.ui.fx.components.*;

import static com.quollwriter.LanguageStrings.*;
import static com.quollwriter.uistrings.UILanguageStringsManager.getUILanguageStringProperty;

public class ProjectViewer extends AbstractProjectViewer
{

    public ProjectViewer ()
    {

    }

    @Override
    public void init (State s)
               throws GeneralException
    {

        Chapter c = this.project.getBooks ().get (0).getChapters ().iterator ().next ();

        this.editChapter (c);

        super.init (s);

    }

    @Override
    public Panel getPanelForId (String id)
    {

        return null;

    }

    @Override
    public void handleNewProject ()
                           throws Exception
    {

        Book b = this.project.getBooks ().get (0);

        Chapter c = b.getFirstChapter ();

        // Create a new chapter for the book.
        if (c == null)
        {

            c = new Chapter (b,
                             Environment.getDefaultChapterName ());

            b.addChapter (c);

        }

        this.saveObject (c,
                         true);

        // Refresh the chapter tree.
        // TODO Needed? this.reloadTreeForObjectType (c.getObjectType ());

        this.handleOpenProject ();

        this.editChapter (c);

    }

    @Override
    public void handleOpenProject ()
    {

        //this.initProjectItemBoxes ();

		final ProjectViewer _this = this;

		// Called whenever a note type is changed.
        /*
         TODO
		this.noteTypePropChangedListener = new PropertyChangedListener ()
		{

			@Override
			public void propertyChanged (PropertyChangedEvent ev)
			{

				if (ev.getChangeType ().equals (UserPropertyHandler.VALUE_CHANGED))
				{

					java.util.List<Note> toSave = new ArrayList ();

					Set<Note> objs = _this.getAllNotes ();

					for (Note o : objs)
					{

						if (o.getType ().equals ((String) ev.getOldValue ()))
						{

							o.setType ((String) ev.getNewValue ());

							toSave.add (o);

						}

						if (toSave.size () > 0)
						{

							try
							{

								_this.saveObjects (toSave,
												   true);

							} catch (Exception e)
							{

								Environment.logError ("Unable to save notes: " +
													  toSave +
													  " with new type: " +
													  ev.getNewValue (),
													  e);
// TODO: Language string
								UIUtils.showErrorMessage (_this,
														  "Unable to change type");

							}

						}

					}

					_this.reloadTreeForObjectType (Note.OBJECT_TYPE);

				}

			}

		};
*/
		// TODO Environment.getUserPropertyHandler (Constants.NOTE_TYPES_PROPERTY_NAME).addPropertyChangedListener (this.noteTypePropChangedListener);

		// TODO this.scheduleUpdateAppearsInChaptersTree ();

    }

    @Override
    public Supplier<Set<MenuItem>> getSettingsMenuSupplier ()
    {

        // TODO
        return null;

    }

    @Override
    public Supplier<Set<Node>> getTitleHeaderControlsSupplier ()
    {

        // TODO
        return null;

    }

    public void viewObject (DataObject d)
                     throws GeneralException
    {

        if (d == null)
        {

            return;

        }

        this.viewObject (d,
                         null);

    }

    @Override
    public ProjectChapterEditorPanelContent getEditorForChapter (Chapter c)
    {

        NamedObjectPanelContent p = this.getPanelForObject (c);

        if (p instanceof ProjectChapterEditorPanelContent)
        {

            return (ProjectChapterEditorPanelContent) p;

        }

        return null;

    }

    public void viewObject (final DataObject d,
                            final Runnable   doAfterView)
                     throws GeneralException
    {

        final ProjectViewer _this = this;

        if (d instanceof ChapterItem)
        {

            final ChapterItem ci = (ChapterItem) d;

            this.viewObject (ci.getChapter (),
                             () -> _this.getEditorForChapter (ci.getChapter ()).showItem (ci));

            return;

        }

        if (d instanceof Chapter)
        {

            Chapter c = (Chapter) d;

            if (d.getObjectType ().equals (Chapter.INFORMATION_OBJECT_TYPE))
            {

                try
                {

                    this.viewChapterInformation (c,
                                                 doAfterView);

                } catch (Exception e) {

                    Environment.logError ("Unable to view chapter information for chapter: " +
                                          c,
                                          e);

                    ComponentUtils.showErrorMessage (_this,
                                                     getUILanguageStringProperty (LanguageStrings.project,actions,viewchapterinformation,actionerror));
                                              //"Unable to show chapter information.");

                }

            } else
            {

                this.editChapter (c,
                                  doAfterView);

            }

            return;

        }

        if (d instanceof Asset)
        {

            this.viewAsset ((Asset) d,
                            doAfterView);

        }
/*
        if (d instanceof Note)
        {

            this.viewNote ((Note) d);

            return true;

        }
        */
/*
        if (d instanceof OutlineItem)
        {

            this.viewOutlineItem ((OutlineItem) d);

            return true;

        }
*/
        // Record the error, then ignore.
        // TODO throw new GeneralException ("Unable to open object");

    }

    public void editChapter (final Chapter  c)
    {

        this.editChapter (c,
                          null);

    }

    public void editChapter (final Chapter  c,
                             final Runnable doAfterView)
    {

        // TODO
        try
        {

        ProjectChapterEditorPanelContent p = new ProjectChapterEditorPanelContent (this,
                                                                                   c);

        p.init (null);

        this.addPanel (p);

    } catch (Exception e) {

        e.printStackTrace ();

    }

/*
        // Check our tabs to see if we are already editing this chapter, if so then just switch to it instead.
        QuollEditorPanel qep = (QuollEditorPanel) this.getQuollPanelForObject (c);

        if (qep != null)
        {

            this.setPanelVisible (qep);

            this.getEditorForChapter (c).getEditor ().grabFocus ();

            this.getEditorForChapter (c).getEditor ().getDocument ().addDocumentListener (this);

            if (doAfterView != null)
            {

                UIUtils.doActionWhenPanelIsReady (qep,
                                                  doAfterView,
                                                  c,
                                                  "afterview");

            }

            return true;

        }

        final ProjectViewer _this = this;

        try
        {

            qep = new QuollEditorPanel (this,
                                        c);

            qep.init ();

        } catch (Exception e)
        {

            Environment.logError ("Unable to edit chapter: " +
                                  c,
                                  e);

            UIUtils.showErrorMessage (_this,
                                      String.format (Environment.getUIString (LanguageStrings.project,
                                                                              LanguageStrings.actions,
                                                                              LanguageStrings.editchapter,
                                                                              LanguageStrings.actionerror),
                                                     c.getName ()));
                                      //"Unable to edit {chapter}: " +
                                      //c.getName ());

            return false;

        }

        final TabHeader th = this.addPanel (qep);

        qep.addActionListener (new ActionAdapter ()
        {

            public void actionPerformed (ActionEvent ev)
            {

                if (ev.getID () == QuollPanel.UNSAVED_CHANGES_ACTION_EVENT)
                {

                    th.setComponentChanged (true);

                }

            }

        });

        this.addNameChangeListener (c,
                                    qep);

        // Open the tab :)
        return this.editChapter (c,
                                 doAfterView);
*/
    }

    /**
     * This is a top-level action so it can handle showing the user a message, it returns a boolean to indicate
     * whether the chapter information is viewed.
     */
    public void viewChapterInformation (final Chapter c,
                                        final Runnable doAfterView)
                                 throws GeneralException
    {

        ChapterInformationSideBar cb = new ChapterInformationSideBar (this,
                                                                      c);

        SideBar sb = cb.createSideBar ();

        // TODO Merge these calls and put them in AbstractProjectViewer
        this.initSideBar (sb);

        this.addSideBar (sb);

        this.showSideBar (sb,
                          doAfterView);

    }

    public void viewAsset (final Asset    a,
                           final Runnable doAfterView)
    {

        NamedObjectPanelContent p = this.getPanelForObject (a);

        if (p != null)
        {

            this.setPanelVisible (p);

            if (doAfterView != null)
            {

                UIUtils.runLater (doAfterView);

            }

            return;

        }

        final ProjectViewer _this = this;

        AssetViewPanel avp = null;

        try
        {

            avp = new AssetViewPanel (this,
                                      a);

            if (doAfterView != null)
            {

                avp.readyForUseProperty ().addListener ((pv, oldv, newv) -> UIUtils.runLater (doAfterView));

            }

            // TODO Add state handling...
            avp.init (new State ());

            this.addPanel (avp.getPanel ());

        } catch (Exception e)
        {

            Environment.logError ("Unable to view asset: " +
                                  a,
                                  e);

            ComponentUtils.showErrorMessage (this,
                                             getUILanguageStringProperty (Arrays.asList (assets,view,actionerror),
                                                                          a.getObjectTypeName (),
                                                                          a.getName ()));

            return;

        }

        // Open the tab :)
        this.viewAsset (a,
                        null);

    }

    @Override
    public SideBar getMainSideBar ()
    {

        // TODO
        return null;

    }

    @Override
    public String getStyleClassName ()
    {

        return StyleClassNames.PROJECT;

    }

}
