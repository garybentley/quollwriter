package com.quollwriter.ui;

import java.awt.Component;
import java.awt.Point;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.*;
import java.awt.dnd.*;

import java.util.List;
import java.util.ArrayList;
import java.util.StringTokenizer;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.tree.*;

import com.jgoodies.forms.builder.*;
import com.jgoodies.forms.factories.*;
import com.jgoodies.forms.layout.*;

import com.quollwriter.data.*;
import com.quollwriter.*;
import com.quollwriter.events.*;
import com.quollwriter.ui.actionHandlers.*;
import com.quollwriter.ui.components.ActionAdapter;
import com.quollwriter.ui.components.Header;
import com.quollwriter.ui.renderers.*;

public class ChatAccordionItem extends AccordionItem
{
        
    private ProjectViewer projectViewer = null;
    private Box content = null;
    private JTextArea message = null;
    private boolean typed = false;
        
    public ChatAccordionItem (ProjectViewer pv)
    {
        
        super ("",
               null);
        
        this.setTitle ("Messages");
        this.setIconType ("chat");

        this.projectViewer = pv;

        final ChatAccordionItem _this = this;
        
        this.content = new Box (BoxLayout.Y_AXIS);
                
        this.message = UIUtils.createTextArea (5);

        JScrollPane sp = new JScrollPane (this.message);

        sp.setAlignmentX (Component.LEFT_ALIGNMENT);
        sp.setOpaque (false);
        sp.getViewport ().setOpaque (false);
        sp.setBorder (null);
        
        Box messageBox = new Box (BoxLayout.Y_AXIS);
        messageBox.add (this.message);
                
        JButton save = UIUtils.createButton ("send",
                                             Constants.ICON_MENU,
                                             "Click to send the message",
                                             new ActionAdapter ()
                                             {
                                                
                                                public void actionPerformed (ActionEvent ev)
                                                {
                                                    
                                                }
                                                
                                             });

        JButton cancel = UIUtils.createButton (Constants.CANCEL_ICON_NAME,
                                               Constants.ICON_MENU,
                                               "Click to cancel",
                                               new ActionAdapter ()
                                               {
                                                
                                                  public void actionPerformed (ActionEvent ev)
                                                  {

                                                  }
                                                  
                                               });

        List<JButton> buts = new ArrayList ();

        buts.add (save);
        buts.add (cancel);

        JToolBar tb = UIUtils.createButtonBar (buts);

        tb.setAlignmentX (Component.LEFT_ALIGNMENT);        
        
        Box buttons = new Box (BoxLayout.X_AXIS);
        buttons.setAlignmentX (Component.LEFT_ALIGNMENT);
        buttons.add (Box.createHorizontalGlue ());
        buttons.add (tb);

        buttons.setBorder (new CompoundBorder (new MatteBorder (1,
                                                                0,
                                                                0,
                                                                0,
                                                                UIUtils.getColor ("#dddddd")),
                                               new EmptyBorder (3,
                                                                3,
                                                                3,
                                                                3)));
                                                                
                                                                
        messageBox.add (buttons);
        
        this.message.addMouseListener (new MouseAdapter ()
        {

            public void mouseEntered (MouseEvent ev)
            {

                _this.message.grabFocus ();

            }
        });

        this.message.addKeyListener (new KeyAdapter ()
        {

            private boolean typed = false;
        
            public void keyPressed (KeyEvent ev)
            {
                        
                if (!_this.typed)
                {                        
 
                    _this.typed = true;
                
                    _this.message.setText ("");
                    _this.message.setForeground (Color.BLACK);
                            
                }
                
                if ((ev.getKeyCode () == KeyEvent.VK_ENTER) &&
                    ((ev.getModifiersEx () & InputEvent.CTRL_DOWN_MASK) == InputEvent.CTRL_DOWN_MASK))
                {

                }

            }

        });        
                
        messageBox.setBorder (new CompoundBorder (new EmptyBorder (3, 5, 5, 0),
                                                  UIUtils.createLineBorder ()));
        message.setBorder (new EmptyBorder (3,
                                            3,
                                            3,
                                            3));

        Header h = this.getHeader ();
                                        
        h.setBorder (new CompoundBorder (new CompoundBorder (new MatteBorder (0, 0, 1, 0, UIUtils.getBorderColor ()),
                                                             new EmptyBorder (0, 0, 3, 0)),
                                         h.getBorder ()));

        this.content.add (messageBox);
        this.content.setBorder (new EmptyBorder (5, 0, 10, 0));
        
    }
    
    private void initEditText ()
    {
        
        final Color lightGrey = UIUtils.getColor ("#aaaaaa");

        this.message.setForeground (lightGrey);
        
        String help = "Enter message...\n\nTo save press Ctrl+Enter or use the buttons below.";
                    
        this.message.setText (help);

        this.message.getCaret ().setDot (0);

        this.message.grabFocus ();
        
    }
            /*        
    public JComponent getWrappedText (String t,
                                      int    initialWidth)
    {
        
        FormLayout fl = new FormLayout ("fill:" + initialWidth + "px:grow",
                                        "p");

        PanelBuilder pb = new PanelBuilder (fl);

        CellConstraints cc = new CellConstraints ();
    
        JTextPane tp = UIUtils.createObjectDescriptionViewPane (t,
                                                                this.chapter,
                                                                this.projectViewer,
                                                                this.projectViewer.getEditorForChapter (this.chapter)); 

        pb.add (tp,
                    cc.xy (1,
                           1));

        JPanel p = pb.getPanel ();

        p.setOpaque (false);
        
        return p;
        
    }
    */
    public JComponent getContent ()
    {
        
        return this.content;
                
    }
        
    public void init ()
    {
        
        super.init ();

        this.initEditText ();
                
    }
    
}