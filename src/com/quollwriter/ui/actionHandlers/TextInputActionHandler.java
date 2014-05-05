package com.quollwriter.ui.actionHandlers;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;

import com.quollwriter.*;

import com.quollwriter.data.*;

import com.quollwriter.ui.*;
import com.quollwriter.ui.components.ActionAdapter;


public abstract class TextInputActionHandler extends ActionAdapter
{

    protected AbstractProjectViewer projectViewer = null;

    public TextInputActionHandler (AbstractProjectViewer pv)
    {

        this.projectViewer = pv;

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
    
    public void actionPerformed (ActionEvent ev)
    {

        final TextInputActionHandler _this = this;
    
        UIUtils.createTextInputPopup (this.projectViewer,
                                      this.getTitle (),
                                      this.getIcon (),
                                      this.getHelp (),
                                      this.getConfirmButtonLabel (),
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
                                                
                                                  UIUtils.showErrorMessage (_this.projectViewer,
                                                                            "Unable to perform command, please contact support for assistance.");
                                                  
                                                  Environment.logError ("Unable to perform command",
                                                                        e);
                                                
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
                                                
                                                  UIUtils.showErrorMessage (_this.projectViewer,
                                                                            "Unable to perform command, please contact support for assistance.");
                                                  
                                                  Environment.logError ("Unable to perform command",
                                                                        e);                                                
                                                
                                              }
                                            
                                          }
                                        
                                      },
                                      this.getShowAt ());
    
    }

}
