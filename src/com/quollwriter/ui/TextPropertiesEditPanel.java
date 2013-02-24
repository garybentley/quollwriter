package com.quollwriter.ui;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.Color;

import java.awt.event.*;

import java.util.Map;
import java.util.HashMap;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.border.*;

import com.jgoodies.forms.builder.*;
import com.jgoodies.forms.factories.*;
import com.jgoodies.forms.layout.*;

import com.quollwriter.*;

import com.gentlyweb.properties.*;

import com.quollwriter.ui.components.ActionAdapter;
import com.quollwriter.ui.components.QPopup;
import com.quollwriter.ui.components.ChangeAdapter;
import com.quollwriter.ui.components.TextProperties;

public class TextPropertiesEditPanel extends Box
{
    
    private AbstractProjectViewer projectViewer = null;
    private TextProperties textProps = null;
    private String eventType = null;
    private boolean showColorSelectors = false;
    private PopupsSupported popupParent = null;

    private JComboBox            fonts = null;
    private JComboBox            sizes = null;
    private JComboBox            align = null;
    private JComboBox            line = null;
    private JCheckBox            indent = null;
    private JPanel               textcolorSwatch = null;
    private JPanel               bgcolorSwatch = null;

    private Map<String, QPopup>  popups = new HashMap ();
    
    public TextPropertiesEditPanel (AbstractProjectViewer pv,
                                    TextProperties        props,
                                    String                eventType,
                                    boolean               showColorSelectors,
                                    PopupsSupported       popupParent)
    {
        
        super (BoxLayout.Y_AXIS);
        
        this.textProps = props;
        this.eventType = eventType;
        this.projectViewer = pv;
        this.showColorSelectors = showColorSelectors;
        this.popupParent = popupParent;
        
    }
        
    public void setTextProperties (TextProperties props)
    {
        
        this.fonts.setSelectedItem (props.getFontFamily ());
        this.sizes.setSelectedItem (props.getFontSize ());
        this.align.setSelectedItem (props.getAlignment ());
        this.line.setSelectedItem (props.getLineSpacing ());
        this.textcolorSwatch.setBackground (props.getTextColor ());
        this.bgcolorSwatch.setBackground (props.getBackgroundColor ());        
            
        this.textProps = props;

        // Need to update the text background/text color.
        this.textProps.setTextColor (props.getTextColor ());
        this.textProps.setBackgroundColor (props.getBackgroundColor ());
        
    }
    
    public void init ()
    {

        final TextPropertiesEditPanel _this = this;
    
        PanelBuilder    builder = null;
        CellConstraints cc = null;
        FormLayout fl = null;
        JPanel p = null;
        
        fl = new FormLayout ("right:p, 6px, p:grow",
                             "p, 10px, p, 10px, p, 10px, p, 10px, p" + (this.showColorSelectors ? ", 10px, p, 10px, p, 10px, p" : ""));

        int r = 1;
    
        builder = new PanelBuilder (fl);
        cc = new CellConstraints ();

        builder.addLabel ("Font",
                          cc.xy (1,
                                 r));

        this.fonts = UIUtils.getFontsComboBox (this.textProps.getFontFamily (),
                                               this.textProps);

        this.fonts.addActionListener (new ActionAdapter ()
        {

            public void actionPerformed (ActionEvent ev)
            {            

                _this.projectViewer.fireProjectEvent (_this.eventType,
                                                      ProjectEvent.CHANGE_FONT);

            }

        });

        builder.add (UIUtils.getLimitWrapper (this.fonts),
                     cc.xy (3,
                            r));
        
        r += 2;

        builder.addLabel ("Size",
                          cc.xy (1,
                                 r));

        this.sizes = UIUtils.getFontSizesComboBox (this.textProps.getFontSize (),
                                                   this.textProps);

        this.sizes.addActionListener (new ActionAdapter ()
        {

            public void actionPerformed (ActionEvent ev)
            {

                _this.projectViewer.fireProjectEvent (_this.eventType,
                                                      ProjectEvent.CHANGE_FONT_SIZE);

            }

        });

        builder.add (UIUtils.getLimitWrapper (this.sizes),
                     cc.xy (3,
                            r));

        r += 2;
    
        builder.addLabel ("Alignment",
                          cc.xy (1,
                                 r));

        this.align = UIUtils.getAlignmentComboBox (this.textProps.getAlignment (),
                                                   this.textProps);

        this.align.addActionListener (new ActionAdapter ()
        {
    
            public void actionPerformed (ActionEvent ev)
            {

                _this.projectViewer.fireProjectEvent (_this.eventType,
                                                      ProjectEvent.CHANGE_ALIGNMENT);

            }

        });

        builder.add (UIUtils.getLimitWrapper (this.align),
                     cc.xy (3,
                            r));

        r += 2;

        builder.addLabel ("Line Spacing",
                          cc.xy (1,
                                 r));

        this.line = UIUtils.getLineSpacingComboBox (this.textProps.getLineSpacing (),
                                                    this.textProps);

        this.line.addActionListener (new ActionAdapter ()
        {
    
            public void actionPerformed (ActionEvent ev)
            {

                _this.projectViewer.fireProjectEvent (_this.eventType,
                                                      ProjectEvent.CHANGE_LINE_SPACING);

            }

        });

        builder.add (UIUtils.getLimitWrapper (this.line),
                     cc.xy (3,
                            r));

        r += 2;

        this.indent = new JCheckBox ("Indent the first line of each paragraph");
        this.indent.setOpaque (false);
        this.indent.setSelected (this.textProps.getFirstLineIndent ());

        this.indent.addActionListener (new ActionAdapter ()
        {

            public void actionPerformed (ActionEvent ev)
            {

                _this.textProps.setFirstLineIndent (_this.indent.isSelected ());
            
                _this.projectViewer.fireProjectEvent (_this.eventType,
                                                      ProjectEvent.CHANGE_LINE_INDENT);
                
                
            }

        });

        builder.add (this.indent,
                     cc.xy (3,
                            r));
                
        if (this.showColorSelectors)
        {

            r += 2;        
        
            builder.addLabel ("Color",
                              cc.xy (1,
                                     r));
        
            this.textcolorSwatch = QColorChooser.getSwatch (this.textProps.getTextColor ());
            this.bgcolorSwatch = QColorChooser.getSwatch (this.textProps.getBackgroundColor ());
    
            this.textcolorSwatch.addMouseListener (new MouseAdapter ()
            {
    
                public void mouseReleased (MouseEvent ev)
                {
    
                    String colors = _this.projectViewer.getProject ().getProperty (Constants.COLOR_SWATCHES_PROPERTY_NAME);
                
                    Color textcolor = _this.textProps.getTextColor ();
                
                    QPopup popup = _this.popups.get ("textcolor");
                    
                    if (popup == null)
                    {
                    
                        popup = QColorChooser.getColorChooserPopup (colors,
                                                                    textcolor,
                                                                    new ChangeAdapter ()
                                                                    {
                             
                                                                         public void stateChanged (ChangeEvent ev)
                                                                         {
                             
                                                                             Color c = (Color) ev.getSource ();
                             
                                                                             _this.textProps.setTextColor (c);
                             
                                                                             textcolorSwatch.setBackground (c);
                                                                             
                                                                             bgcolorSwatch.setBackground (_this.textProps.getBackgroundColor ());
    
                                                                             _this.projectViewer.fireProjectEvent (_this.eventType,
                                                                                                                   ProjectEvent.CHANGE_FONT_COLOR);
                             
                                                                         }
    
                                                                     },
                                                                    new ActionAdapter ()
                                                                    {
                                                                        
                                                                        public void actionPerformed (ActionEvent ev)
                                                                        {
                                                                            
                                                                            QPopup p = _this.popups.remove ("textcolor");
                                                                            
                                                                            p.removeFromParent ();
                                                                            
                                                                        }
                                                                        
                                                                    });                
                
                        _this.popups.put ("textcolor",
                                          popup);
    
                    }
                
                    int x = ev.getXOnScreen ();
                    int y = ev.getYOnScreen ();
    
                    Dimension d = Toolkit.getDefaultToolkit ().getScreenSize ();
    
                    if ((y + popup.getPreferredSize ().height) > d.height)
                    {
    
                        y -= popup.getPreferredSize ().height;
    
                    }
    
                    _this.popupParent.showPopupAt (popup,
                                                   new Point (x, y));
                
                }
    
            });
    
            builder.add (UIUtils.getLimitWrapper (textcolorSwatch),
                         cc.xy (3,
                                r));
    
            r += 2;
    
            builder.addLabel ("Background",
                              cc.xy (1,
                                     r));
                
            bgcolorSwatch.addMouseListener (new MouseAdapter ()
            {
    
                public void mouseReleased (MouseEvent ev)
                {
    
                    String colors = _this.projectViewer.getProject ().getProperty (Constants.COLOR_SWATCHES_PROPERTY_NAME);
    
                    Color bgcolor = _this.textProps.getBackgroundColor ();   
    
                    QPopup popup = _this.popups.get ("bgcolor");
                    
                    if (popup == null)
                    {
                        
                        popup = QColorChooser.getColorChooserPopup (colors,
                                                                    bgcolor,
                                                                    new ChangeAdapter ()
                                                                    {
     
                                                                        public void stateChanged (ChangeEvent ev)
                                                                        {
     
                                                                            Color c = (Color) ev.getSource ();
     
                                                                            _this.textProps.setBackgroundColor (c);
     
                                                                            //_this.fsf.setBackgroundColor (c);
                                                                            
                                                                            //_this.sizeBoxWrapper.setBackground (c);
                                                                            
                                                                            bgcolorSwatch.setBackground (c);
                                                                            textcolorSwatch.setBackground (_this.textProps.getTextColor ());
                                                                              
                                                                            _this.projectViewer.fireProjectEvent (_this.eventType,
                                                                                                                  ProjectEvent.CHANGE_BG_COLOR);
     
                                                                        }
     
                                                                    },
                                                                     new ActionAdapter ()
                                                                     {
                                                                      
                                                                        public void actionPerformed (ActionEvent ev)
                                                                        {
                                                                          
                                                                          QPopup p = _this.popups.remove ("bgcolor");
                                                                          
                                                                          p.removeFromParent ();
                                                                          
                                                                        }
                                                                      
                                                                    });    
    
                        _this.popups.put ("bgcolor",
                                          popup);
    
                    }
                                          
                    int x = ev.getXOnScreen ();
                    int y = ev.getYOnScreen ();
    
                    Dimension d = Toolkit.getDefaultToolkit ().getScreenSize ();
    
                    if ((y + popup.getPreferredSize ().height) > d.height)
                    {
    
                        y -= popup.getPreferredSize ().height;
    
                    }
    
                    _this.popupParent.showPopupAt (popup,
                                                   new Point (x, y));
                    
                }
    
            });
        
            //this.fsf.setFontColor (this.fullScreenTextProperties.getTextColor ());
    
            //this.fsf.setBackgroundColor (this.fullScreenTextProperties.getBackgroundColor ());
    
            builder.add (UIUtils.getLimitWrapper (bgcolorSwatch),
                         cc.xy (3,
                                r));

            r += 2;
    
            final JLabel reset = UIUtils.createClickableLabel ("Reset to defaults",
                                                               null);
        
            reset.setToolTipText ("Click to reset the text properties to their default values");
            
            reset.addMouseListener (new MouseAdapter ()
            {
        
                public void mouseReleased (MouseEvent ev)
                {
                    System.out.println ("PRESSED: " + _this.textProps);
                    _this.textProps.resetToDefaults ();
                                
                }
    
            });
        
            builder.add (reset,
                         cc.xywh (3,
                                  r,
                                  1,
                                  1));

        }
    
        p = builder.getPanel ();
        p.setOpaque (false);
        p.setAlignmentX (JComponent.LEFT_ALIGNMENT);
        p.setBorder (new EmptyBorder (10,
                                      10,
                                      10,
                                      10));

        this.add (p);
        
    }
    
}
