package com.quollwriter.ui.actionHandlers;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;

import com.quollwriter.*;

import com.quollwriter.data.*;

import com.quollwriter.ui.*;
import com.quollwriter.ui.components.ActionAdapter;

import static com.quollwriter.LanguageStrings.*;
import static com.quollwriter.Environment.getUIString;

public abstract class TextInputActionHandler<E extends AbstractViewer> extends ActionAdapter
{

    protected E viewer = null;

    public TextInputActionHandler (E pv)
    {

        this.viewer = pv;

    }

    public abstract String getIcon ();
    public abstract String getTitle ();
    public abstract String getHelp ();
    public abstract String getConfirmButtonLabel ();
    public abstract String getInitialValue ();
    public abstract String isValid (String v);
    public abstract boolean onConfirm (String v) throws Exception;
    public abstract boolean onCancel () throws Exception;
    public abstract Point getShowAt ();

    public String getCancelButtonLabel ()
    {

        // Use the default.
        return null;

    }

    public void actionPerformed (ActionEvent ev)
    {

        final TextInputActionHandler _this = this;

        UIUtils.createTextInputPopup (this.viewer,
                                      this.getTitle (),
                                      this.getIcon (),
                                      this.getHelp (),
                                      this.getConfirmButtonLabel (),
                                      this.getCancelButtonLabel (),
                                      this.getInitialValue (),
                                      new ValueValidator<String> ()
                                      {

                                        public String isValid (String v)
                                        {

                                            return _this.isValid (v);

                                        }

                                      },
                                      new ActionAdapter ()
                                      {

                                          public void actionPerformed (ActionEvent ev)
                                          {

                                              try
                                              {

                                                  _this.onConfirm (ev.getActionCommand ());

                                              } catch (Exception e) {

                                                  Environment.logError ("Unable to perform command",
                                                                        e);

                                                  UIUtils.showErrorMessage (_this.viewer,
                                                                            getUIString (general,unabletoperformaction));
                                                                            //"Unable to perform command, please contact support for assistance.");

                                              }

                                          }

                                      },
                                      new ActionAdapter ()
                                      {

                                          public void actionPerformed (ActionEvent ev)
                                          {

                                              try
                                              {

                                                  _this.onCancel ();

                                              } catch (Exception e) {

                                                  Environment.logError ("Unable to perform command",
                                                                        e);

                                                  UIUtils.showErrorMessage (_this.viewer,
                                                                            getUIString (unabletoperformaction));
                                                                            //"Unable to perform command, please contact support for assistance.");

                                              }

                                          }

                                      },
                                      this.getShowAt ());

    }

}
