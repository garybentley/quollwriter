package com.quollwriter.ui.panels;

import java.awt.Component;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Insets;
import java.awt.Container;
import java.awt.BasicStroke;
import java.awt.LayoutManager;
import java.awt.Point;
import java.awt.Font;
import java.awt.Cursor;

import java.awt.font.*;
import java.awt.geom.*;

import java.awt.event.*;

import javax.swing.*;
import javax.swing.border.*;

import java.util.Map;
import java.util.List;
import java.util.GregorianCalendar;
import java.util.Calendar;
import java.util.Date;
import java.util.Set;
import java.util.ArrayList;

import java.text.*;

import com.quollwriter.*;
import com.quollwriter.ui.*;
import com.quollwriter.data.*;
import com.quollwriter.ui.components.Header;
import com.quollwriter.ui.components.QPopup;
import com.quollwriter.ui.components.Dragger;
import com.quollwriter.ui.components.DragListener;
import com.quollwriter.ui.components.DragEvent;
import com.quollwriter.ui.components.ImagePanel;
import com.quollwriter.ui.components.ActionAdapter;

public class Timeline extends QuollPanel
{

    public static final String SCALE_WEEKLY = "weekly";
    public static final String SCALE_DAILY = "daily";
    public static final String SCALE_HOURLY = "hourly";

    public static final String PANEL_ID = "timeline";

    private String currentScale = SCALE_WEEKLY;//SCALE_WEEKLY;
    private Date viewStartDate = null;
    private Date viewEndDate = null;
    private Date threadsStartDate = null;
    private Date threadsEndDate = null;
    private SimpleDateFormat dateFormatter = null;
    private int colCount = -1;
    private int colWidth = -1;
    private int viewWidth = -1;
    private int lastViewWidth = -1;
    private int headerHeight = -1;
    private int padding = 5;

    private JPanel header = null;
    private TimelinePanel main = null;
    private JScrollPane scrollPane = null;

    private List<TimelineThread> threads = new ArrayList ();

    private String mainFormat = null;
    private String superFormat = null;
    private boolean dragInProgress = false;

    public Timeline (AbstractProjectViewer pv)
                     throws                GeneralException
    {
        
        super (pv,
               null);
        
        String df = "";
        
        if (this.currentScale.equals (SCALE_HOURLY))
        {
            
            df = "'<html>'HH:mm'<br>'dd MMM'</html>'";
            
        }

        if (this.currentScale.equals (SCALE_DAILY))
        {
            
            df = "EEE, dd MMM";
            
        }

        if (this.currentScale.equals (SCALE_WEEKLY))
        {
            
            df = "dd MMM yyyy";
            
        }
        
        this.dateFormatter = new SimpleDateFormat (df);
                
    }

    private void initViewDates ()
    {
     /*
        if (this.viewEndDate != null)
        {
            
            return;
            
        }
     */
        Date lastEndDate = this.viewEndDate;
        
        Date s = null;
        Date e = null;
        
        for (TimelineThread tt : this.threads)
        {

            Date start = tt.getStartDate ();
            Date end = tt.getEndDate ();
            
            if (s == null)
            {
                
                s = start;
                
            }
            
            if (e == null)
            {
                
                e = end;
                
            }
            
            if (start.before (s))
            {
                
                s = start;
                
            }
            
            if (end.after (e))
            {
                
                e = end;
                
            }
            
        }

        this.threadsStartDate = s;
        this.threadsEndDate = e;

        GregorianCalendar gc = new GregorianCalendar ();
        
        if (this.currentScale.equals (SCALE_HOURLY))
        {
            
            this.viewStartDate = s;
            this.viewEndDate = e;
            
        }
        
        if ((this.currentScale.equals (SCALE_WEEKLY))
            ||
            (this.currentScale.equals (SCALE_DAILY))
           )
        {
            
            gc.setFirstDayOfWeek (Calendar.MONDAY);
            
            // Put the start back to monday.
            gc.setTime (s);


            gc.add (Calendar.DATE,
                    -56);

            while (gc.get (Calendar.DAY_OF_WEEK) != gc.getFirstDayOfWeek ())
            {
                
                gc.add (Calendar.DATE,
                        -1);
                
            }
            
            s = gc.getTime ();
            
            gc.setTime (e);

            gc.add (Calendar.DATE,
                    56);

            while (gc.get (Calendar.DAY_OF_WEEK) != gc.getFirstDayOfWeek ())
            {
                
                gc.add (Calendar.DATE,
                        -1);
                
            }

            e = gc.getTime ();
            
        }

        this.viewStartDate = s;

        if (this.viewEndDate == null)
        {
            
            this.viewEndDate = e;
            
        } else {

            if ((this.dragInProgress)
                &&
                (e.after (lastEndDate))
               )
            {
                                            
                //e = lastEndDate;
            this.viewEndDate = e;
            
            } 
                
            //this.viewEndDate = e;

        }
        
    }

    private int getColumnCount ()
    {

        if (this.colCount < 0)
        {

            int incr = 1;
            int incrType = Calendar.DATE;
            
            if (this.currentScale.equals (SCALE_HOURLY))
            {
                
                incrType = Calendar.HOUR_OF_DAY;
                
            }
            
            if (this.currentScale.equals (SCALE_WEEKLY))
            {
                
                incr = 7;
                
            }
            
            Date vd = this.viewStartDate;
            
            GregorianCalendar gc = new GregorianCalendar ();
            gc.setTime (this.viewStartDate);
                        
            int c = 0;
            
            Date ed = new Date (this.viewEndDate.getTime () + 1);
            
            while (vd.before (ed))
            {                
                            
                c++;
    
                gc.add (incrType,
                        incr);
            
                vd = gc.getTime ();
            
            }

            this.colCount = c;

        }

        return this.colCount;
        
    }

    private int getColumnWidth ()
    {

        if (this.colWidth < 0)
        {

            int incr = 1;
            int incrType = Calendar.DATE;
            int minWidth = 24;

            if (this.currentScale.equals (SCALE_HOURLY))
            {
                
                incrType = Calendar.HOUR_OF_DAY;
                minWidth = 60;
                
            }

            if (this.currentScale.equals (SCALE_DAILY))
            {
                
                minWidth = 48;
                
            }

            if (this.currentScale.equals (SCALE_WEEKLY))
            {
                
                incr = 7;
                minWidth = 7;
                
            }
            
            Date vd = this.viewStartDate;
            
            GregorianCalendar gc = new GregorianCalendar ();
            gc.setTime (this.viewStartDate);
            
            double maxW = 0;
            double maxH = 0;
                                    
            while (vd.before (this.viewEndDate))
            {                

                JLabel l = new JLabel (this.dateFormatter.format (gc.getTime ()));

                double w = 0;
                
                w = (double) l.getPreferredSize ().width;
                
                maxH = Math.max ((double) l.getPreferredSize ().height,
                                 maxH);
                
                maxW = Math.max (w,
                                 maxW);
            
                gc.add (incrType,
                        incr);
                    
                vd = gc.getTime ();
            
            }

            this.headerHeight = UIUtils.scaleToScreenSize (maxH);
            this.colWidth = ((int) Math.round (maxW)) + (this.padding * 2);

            // Round to a multiple of the minWidth.
            if (this.colWidth < minWidth)
            {
                
                this.colWidth = minWidth;
                
            }
            
            int w = this.colWidth / minWidth;
            
            if (this.colWidth % minWidth != 0)
            {
                
                w++;
                
            }
            
            this.colWidth = w * minWidth;
            
        } 

        return this.colWidth;
        
    }

    public int getWidth (TimelineThread t)
    {
        
        return 0;
        
    }

    public int modelWidth (TimelineThread tt)
    {
        
        int incr = 1;
        int incrType = Calendar.DATE;
        
        if (this.currentScale.equals (SCALE_HOURLY))
        {
            
            incrType = Calendar.HOUR_OF_DAY;
            
        }
        
        if (this.currentScale.equals (SCALE_WEEKLY))
        {
            
            incr = 7;
            
        }
        
        double diff = (double) (tt.getEndDate ().getTime () - tt.getStartDate ().getTime ());
        
        double dur = (double) (incr * (24 * 60 * 60 * 1000));
        
        int x = (int) ((diff / dur) * (double) this.getColumnWidth ());

        x += (this.getColumnWidth () / 2);
        
        return x;        
        
    }

    public Date viewToModel (int x)
    {
        
        int incr = 1;
        int incrType = Calendar.DATE;
        int mult = 24 * 60 * 60 * 1000;
        
        if (this.currentScale.equals (SCALE_HOURLY))
        {
            
            incrType = Calendar.HOUR_OF_DAY;
            mult = 60 * 60 * 1000;
            
        }
        
        if (this.currentScale.equals (SCALE_WEEKLY))
        {
            
            incr = 7;
            
        }
                
        double dur = (double) (incr * mult);
        
        long millis = (long) (((double) x / (double) this.getColumnWidth ()) * dur);
        
        return new Date (millis);
                
    }

    public int modelToView (Date d)
    {
 
        int incr = 1;
        int incrType = Calendar.DATE;
        int mult = 24 * 60 * 60 * 1000;
        
        if (this.currentScale.equals (SCALE_HOURLY))
        {
            
            incrType = Calendar.HOUR_OF_DAY;
            mult = 60 * 60 * 1000;
            
        }
        
        if (this.currentScale.equals (SCALE_WEEKLY))
        {
            
            incr = 7;
            
        }
        
        double diff = (double) (d.getTime () - this.viewStartDate.getTime ());
        
        double dur = (double) (incr * mult);
        
        int x = (int) ((diff / dur) * (double) this.getColumnWidth ());
        
        x += (this.getColumnWidth () / 2);
        
        return x;

    }

    public String getPanelId ()
    {
        
        return PANEL_ID;
        
    }
    
    public void close ()
    {
        
    }

    public int getViewWidth ()
    {
        
        if (this.viewStartDate == null)
        {
            
            this.initViewDates ();
            
        }
        
        if (this.viewWidth < 0)
        {
        
            this.viewWidth = (this.getColumnWidth () * this.getColumnCount ()) + 1;
            
        } 
        
        return this.viewWidth;
        
    }

    public Date getDate (String d)
                         throws GeneralException
    {
        
        SimpleDateFormat sdf = new SimpleDateFormat ("dd MMM yyyy");
        
        try
        {
            
            return sdf.parse (d);
            
        } catch (Exception e) {
            
            throw new GeneralException ("Unable to parse date: " +
                                        d,
                                        e);
            
        }
        
    }

    public void init ()
                      throws GeneralException
    {
                
        final Timeline _this = this;
                                              /*                                    
        this.header = new JPanel ()
        {
            
            public Dimension getPreferredSize ()
            {
          
                return new Dimension (_this.getViewWidth (), _this.headerHeight);
                
            }
            
            public void paintComponent (Graphics g)
            {

                this.setOpaque (false);

                Graphics2D g2 = (Graphics2D) g;

                g2.setPaint (Color.WHITE);
                g2.fill (new Rectangle (this.getSize ()));
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, // Anti-alias!
                                               RenderingHints.VALUE_ANTIALIAS_ON);

                g2.setPaint (Color.BLACK);

                GregorianCalendar gc = new GregorianCalendar ();
                gc.setTime (_this.viewStartDate);
                
                int incr = 1;
                int incrType = Calendar.DATE;

                if (_this.currentScale.equals (SCALE_HOURLY))
                {
                    
                    incrType = Calendar.HOUR_OF_DAY;
                    
                }

                if (_this.currentScale.equals (SCALE_WEEKLY))
                {
                    
                    incr = 7;
                    
                }
                                                
                int colW = _this.getColumnWidth ();
                int colC = _this.getColumnCount ();
                
                for (int i = 0; i < colC; i++)
                {
                
                    String d = _this.dateFormatter.format (gc.getTime ());

                    Rectangle2D r = g2.getFont ().getStringBounds (d,
                                                                    g2.getFontRenderContext ());

                    double h = r.getHeight ();
                                   
                    double x = (i * colW) + ((colW - r.getWidth ()) / 2);
                                    
                    g2.drawString (d,
                                   (int) x,
                                   (int) h);

                    gc.add (incrType,
                            incr);
                
                }
                                                
            }
                                
        };
                                              */

        MouseAdapter mouseA = new MouseAdapter ()
        {

            private TimelineThread thread = null;
            private Point last = null;
            private boolean left = true;
            private Timer eventTimer = null;
            
            public void mouseReleased (MouseEvent ev)
            {
                
                this.thread = null;
                this.last = null;

                if (this.eventTimer != null)
                {

                    this.eventTimer.stop ();
                    this.eventTimer = null;

                }

                _this.dragInProgress = false;
                _this._repaint ();
                _this.main.setCursor (Cursor.getPredefinedCursor (Cursor.DEFAULT_CURSOR));
                
            }
            
            public void mousePressed (MouseEvent ev)
            {
                
                TimelineThread tt = _this.getClosestThread (10,
                                                            ev.getPoint ());

                this.thread = tt;
                
            }
            
            public void mouseMoved (MouseEvent ev)
            {

                if (this.eventTimer != null)
                {

                    this.eventTimer.stop ();
                    this.eventTimer = null;

                }

                TimelineThread tt = _this.getClosestThread (10,
                                                            ev.getPoint ());

                if (tt != null)
                {
                    
                    _this.main.setCursor (Cursor.getPredefinedCursor (Cursor.W_RESIZE_CURSOR));
                                                            
                } else {
                    
                    _this.main.setCursor (Cursor.getPredefinedCursor (Cursor.DEFAULT_CURSOR));
                    
                }
                
            }
            
            public void mouseDragged (MouseEvent ev)
            {

                if (this.thread != null)
                {
                  
                    _this.dragInProgress = true;

                    JViewport vport = _this.scrollPane.getViewport ();

                    Point cp = SwingUtilities.convertPoint(_this.main, ev.getPoint(),vport);
                    
                    if (this.last != null)
                    {

                        int dx = cp.x - this.last.x;
                        int dy = cp.y - this.last.y;

                        if ((vport.getMousePosition ().x >= vport.getExtentSize ().width - _this.getColumnWidth ())
                            ||
                            (vport.getMousePosition ().x <= _this.getColumnWidth ())
                           )
                        {

                            Point vp = vport.getViewPosition ();
                  
                            vp.translate(dx, dy);
                  
                            _this.main.scrollRectToVisible(new Rectangle(vp, vport.getSize()));

                        }

                        if (!left)
                        {

                            int x = _this.modelToView (this.thread.getStartDate ());
                                                        
                            Date diff = _this.viewToModel (dx);
                            Date sd = new Date (diff.getTime () + this.thread.getStartDate ().getTime ());
                            
                            this.thread.setStartDate (sd);
            
                        } else {
                        
                            int x = _this.modelToView (this.thread.getEndDate ());
                                                        
                            Date diff = _this.viewToModel (dx);
                            Date ed = new Date (diff.getTime () + this.thread.getEndDate ().getTime ());
                            
                            this.thread.setEndDate (ed);                            
                                                      
                        }
                        
                        _this._repaint ();

                        this.last = cp;
                        
                        //this.last = ep;

                    } else {
                        
                        // Check which side we're on.                        
                        this.last = cp;
                                                                        
                    }
                    
                } 
                                
            }
            
        };

        this.main = new TimelinePanel (this);
        
        this.main.setLayout (new TimelineLayout (this));
                                 
        this.main.addMouseMotionListener (mouseA);
        this.main.addMouseListener (mouseA);        
                                                        
        this.scrollPane = new JScrollPane (this.main);
        this.scrollPane.setBorder (null);
        this.scrollPane.getVerticalScrollBar ().setUnitIncrement (20);
        //this.scrollPane.setHorizontalScrollBarPolicy (JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
                
        this.add (this.scrollPane);
/*
        JViewport vp = this.scrollPane.getViewport ();
        
        ViewportDragScrollListener vl = new ViewportDragScrollListener (this.main);

        vp.addMouseMotionListener (vl);
        vp.addMouseListener (vl);
        vp.addHierarchyListener (vl);
*/
/*
        ComponentDragScrollListener cl = new ComponentDragScrollListener (this.main);
        main.addMouseMotionListener (cl);
        main.addMouseListener (cl);
        main.addHierarchyListener (cl);
*/
        TimelineThread tt1 = new TimelineThread ("Ben and Sam to out", this.getDate ("03 Apr 2012"), this.getDate ("08 Apr 2012"), this);
        TimelineThread tt2 = new TimelineThread ("Sam gets beaten up", this.getDate ("10 Apr 2012"), this.getDate ("12 Apr 2012"), this);
        
        this.addThread (tt1);
        this.addThread (tt2);
        this.addThread (new TimelineThread ("They leave the planet", this.getDate ("05 Apr 2012"), this.getDate ("27 May 2012"), this));
        
        this.initViewDates ();
        
        this.setScrollPaneHeader ();
        
    }

    private TimelineThread getClosestThread (int   padding,
                                             Point p)
    {
        
        int n = this.main.getComponentCount ();

        for (int i = 0; i < n; i++)
        {

            Component c = this.main.getComponent (i);

            if (!c.isVisible ())
            {

                continue;

            }

            // Get a box with padding around the component.
            Rectangle r = c.getBounds ();
            
            r.x -= padding;
            r.y -= padding;
            r.width += (padding * 2);
            r.height += (padding * 2);
            
            if (r.contains (p))
            {
                
                return (TimelineThread) c;
                
            }

        }
        
        return null;
        
    }

    private void setScrollPaneHeader ()
    {

        Box headerBox = new Box (BoxLayout.Y_AXIS);

        Box mainBox = new Box (BoxLayout.X_AXIS);
        
        Box subBox = new Box (BoxLayout.X_AXIS);

        headerBox.add (mainBox);
        headerBox.add (subBox);
       
        headerBox.setOpaque (true);
        headerBox.setBackground (Color.WHITE);

        // Handle sub dates.
        GregorianCalendar gc = new GregorianCalendar ();
        gc.setTime (this.viewStartDate);
        
        int incr = 1;
        int incrType = Calendar.DATE;

        if (this.currentScale.equals (SCALE_HOURLY))
        {
            
            incrType = Calendar.HOUR_OF_DAY;
            
        }

        if (this.currentScale.equals (SCALE_WEEKLY))
        {
            
            incr = 7;
            
        }
        
        int colW = this.getColumnWidth ();
        int colC = this.getColumnCount ();
        
        for (int i = 0; i < colC; i++)
        {
                
            String d = this.dateFormatter.format (gc.getTime ());

            Box b = new Box (BoxLayout.X_AXIS);

            JLabel l = new JLabel (d);
            l.setHorizontalAlignment (SwingConstants.CENTER);
            
            b.add (l);

            b.setPreferredSize (new Dimension (colW,
                                               b.getPreferredSize ().height));
            b.setMaximumSize (new Dimension (colW,
                                               b.getPreferredSize ().height));

            subBox.add (b);

            gc.add (incrType,
                    incr);
                
        }

        subBox.add (Box.createHorizontalGlue ());
               
        this.scrollPane.setColumnHeaderView (headerBox);
       
    }

    private void addThread (final TimelineThread t)
    {
        
        final Timeline _this = this;
        
        this.threads.add (t);
        
        this.main.add (t);

        final Dragger d = t.setDraggable (this.main);
        
        d.constrainToX (true);

        d.addDragListener (new DragListener ()
        {
           
            public void dragStarted (DragEvent ev)
            {
                
                _this.dragInProgress = true;
                
            }

            public void dragFinished (DragEvent ev)
            {
                
                _this.dragInProgress = false;
                _this._repaint ();
            }
            
            public void dragInProgress (DragEvent ev)
            {
                
                int x = _this.modelToView (t.getStartDate ());
                
                long td = t.getEndDate ().getTime () - t.getStartDate ().getTime ();
                
                int dx = ev.getDifference ().width;

                Date diff = _this.viewToModel (dx);
                Date sd = new Date (diff.getTime () + t.getStartDate ().getTime ());
                
                t.setStartDate (sd);
System.out.println ("DX: " + sd);
                Date ed = new Date (td + t.getStartDate ().getTime ());
                t.setEndDate (ed);

                JViewport vport = _this.scrollPane.getViewport ();
                
                _this._repaint ();

                int lw = _this.getViewWidth ();
                
                if (lw != _this.lastViewWidth)
                {
                    
                    if (_this.lastViewWidth > 0)
                    {
                        
                        System.out.println ("VW: " + lw + ", " + _this.lastViewWidth);
                        Point vp = vport.getViewPosition ();
                        
                        vp.x += (lw - _this.lastViewWidth);
                        
                        //vport.setViewPosition (vp);
                        
                        //_this.main.scrollRectToVisible(new Rectangle(vp, vport.getSize()));
                        
                        System.out.println ("DIFF: " + (lw - _this.lastViewWidth));

                    }

                    _this.lastViewWidth = lw;
                    
                }

                Point tp = t.getLocation ();
                tp = SwingUtilities.convertPoint (_this.main,
                                                  tp,
                                                  vport);

                x = tp.x;
                                
                boolean scroll = false;
                        
                if ((x <= _this.getColumnWidth ())
                    &&
                    (vport.getViewPosition ().x > 0)
                    &&
                    (dx < 0)
                   )
                {

                    scroll = true;
                    
                }
                                
                if (((x + t.getSize ().width) >= vport.getExtentSize ().width - _this.getColumnWidth ())
                    &&
                    (dx > 0)
                   )
                {

                    scroll = true;

                }

                if (scroll)
                {
                    
                    Point vp = vport.getViewPosition ();

                    vp.translate(dx, 0);
              
                    _this.main.scrollRectToVisible(new Rectangle(vp, vport.getSize()));

                }
                           
                //_this._repaint ();
                                                              
            }
            
        });
        
    }

    public void getState (Map<String, Object> s)
    {
        
    }

    public void setState (Map<String, String> s,
                          boolean             hasFocus)
    {
        
        
    }

    public boolean saveUnsavedChanges ()
                                       throws Exception
    {
        
        return false;
        
    }

    public String getTitle ()
    {
        
        return "Timeline";
        
    }
    
    public String getIconType ()
    {
        
        return "timeline";
        
    }

    public void fillToolBar (JToolBar toolBar,
                             boolean  fullScreen)
    {
        
    }

    public void fillPopupMenu (MouseEvent ev,
                               JPopupMenu popup)
    {
        
    }

    public List<Component> getTopLevelComponents ()
    {
        
        return null;
        
    }

    public <T extends NamedObject> void refresh (T n)
    {
                
    }
    
    public void _repaint ()
    {

        this.lastViewWidth = this.viewWidth;

        this.viewWidth = -1;
        this.colCount = -1;
        this.colWidth = -1;

        this.initViewDates ();

        this.setScrollPaneHeader ();

        this.scrollPane.getColumnHeader ().setViewPosition (new Point (this.scrollPane.getViewport ().getViewPosition ().x,
                                                                       0));
        
        this.main.revalidate ();
        this.main.repaint ();
        
        this.revalidate ();
        this.repaint ();
        
    }
    
    public class TimelineThread extends QPopup
    {
        
        private Date startDate = null;
        private Date endDate = null;
        
        public TimelineThread (String title,
                               Date   startDate,
                               Date   endDate,
                               final Timeline timeline)
        {
            
            super (title,
                   null,
                   null);
            
            this.setStartDate (startDate);
            this.setEndDate (endDate);
            //this.setBorder (null);
                                                
            final TimelineThread _this = this;
                                    
        }
        
        public Date getStartDate ()
        {
            
            return this.startDate;
            
        }
        
        public Date getEndDate ()
        {
            
            return this.endDate;
            
        }
 
        public void setStartDate (Date d)
        {
            
            this.startDate = d;
            
            this.setContent ();
            
        }

        public void setEndDate (Date d)
        {
            
            this.endDate = d;
            
            this.setContent ();
            
        }
        
        private void setContent ()
        {
            
            SimpleDateFormat sdf = new SimpleDateFormat ("HH:mm:ss dd MMM yyyy");
                                    
            this.setContent (UIUtils.createHelpTextPane ((this.startDate != null ? sdf.format (this.startDate) : "") + " : " + (this.endDate != null ? sdf.format (this.endDate) : ""),
                                                         Timeline.this.projectViewer));
            
        }
        
        public String toString ()
        {
            
            return this.getHeader ().getTitle ();
            
        }
        
    }
  
    public class TimelineLayout implements LayoutManager
    {
        
        private Timeline timeline = null;
        
        public TimelineLayout (Timeline t)
        {
            
            this.timeline = t;
            
        }

        private Dimension layoutSize (Container parent,
                                      boolean   minimum)
        {

            int n = parent.getComponentCount ();

            int h = 0;
            int w = this.timeline.getViewWidth ();
    
            for (int i = 0; i < n; i++)
            {
    
                Component c = parent.getComponent (i);
    
                if (!c.isVisible ())
                {
    
                    continue;
    
                }

                if (c instanceof TimelineThread)
                {

                    TimelineThread tt = (TimelineThread) c;
                
                    Dimension d = minimum ? tt.getMinimumSize () : tt.getPreferredSize ();
        
                    h += d.height + 10;

                }
    
            }
            
            return new Dimension (w,
                                  h);
            
        }
        
        /**
         * Lays out the container.
         */
        public void layoutContainer (Container parent)
        {
    
            Insets insets = parent.getInsets ();
    
            synchronized (parent.getTreeLock ())
            {
    
                java.util.List<Component> visComps = new ArrayList ();
    
                int n = parent.getComponentCount ();
    
                int y = 0;
    
                for (int i = 0; i < n; i++)
                {
    
                    Component c = parent.getComponent (i);
    
                    if (!c.isVisible ())
                    {
    
                        continue;
    
                    }

                    if (c instanceof TimelineThread)
                    {
                        
                        TimelineThread tt = (TimelineThread) c;
                                          
                        Insets ttIns = tt.getInsets ();
                                                
                        int h = tt.getPreferredSize ().height;

                        //public int getWidth (TimelineThread tt)
                        Point p = null;
                        
                        if (p == null)
                        {
                            
                            p = new Point (insets.left + this.timeline.modelToView (tt.getStartDate ()),
                                           insets.top + y);
                            
                        }
                        
                        tt.setBounds (p.x,
                                      p.y,
                                      ttIns.right + this.timeline.modelToView (tt.getEndDate ()) - this.timeline.modelToView (tt.getStartDate ()),
                                      h);
        
                        y += h + 10;
        
                    }
        
                }
                
            }
            
        }

        public Dimension minimumLayoutSize (Container parent)
        {
            return layoutSize (parent,
                               true);
        }

        public Dimension preferredLayoutSize (Container parent)
        {
            return layoutSize (parent,
                               false);
        }
    
        /**
         * Not used by this class
         */
        public void addLayoutComponent (String    name,
                                        Component comp)
        {
        }
    //-----------------------------------------------------------------------------
    
        /**
         * Not used by this class
         */
        public void removeLayoutComponent (Component comp)
        {
        }
        
        
    }
 
    public class ComponentDragScrollListener extends MouseAdapter
                                      implements HierarchyListener {
      private static final int SPEED = 4;
      private static final int DELAY = 10;
      private final Cursor dc;
      private final Cursor hc = Cursor.getPredefinedCursor(Cursor.HAND_CURSOR);
      private final javax.swing.Timer scroller;
      private final JComponent label;
      private Point startPt = new Point();
      private Point move    = new Point();
    
      public ComponentDragScrollListener(JComponent comp) {
        this.label = comp;
        this.dc = comp.getCursor();
        this.scroller = new javax.swing.Timer(DELAY, new ActionListener() {
          @Override public void actionPerformed(ActionEvent e) {
            Container c = label.getParent();
            if(c instanceof JViewport) {
              JViewport vport = (JViewport)c;
              Point vp = vport.getViewPosition();
              vp.translate(move.x, move.y);
              label.scrollRectToVisible(new Rectangle(vp, vport.getSize()));
            }
          }
        });
      }
      @Override public void hierarchyChanged(HierarchyEvent e) {
        JComponent jc = (JComponent)e.getSource();
        if((e.getChangeFlags() & HierarchyEvent.DISPLAYABILITY_CHANGED)!=0
           && !jc.isDisplayable()) {
          scroller.stop();
        }
      }
      @Override public void mouseDragged(MouseEvent e) {
        scroller.stop();
        JComponent jc = (JComponent)e.getSource();
        Container c = jc.getParent();
        if(c instanceof JViewport) {
          JViewport vport = (JViewport)jc.getParent();
          Point cp = SwingUtilities.convertPoint(jc,e.getPoint(),vport);
          int dx = startPt.x - cp.x;
          int dy = startPt.y - cp.y;
          Point vp = vport.getViewPosition();

          vp.translate(dx, dy);

          jc.scrollRectToVisible(new Rectangle(vp, vport.getSize()));
          move.setLocation(SPEED*dx, SPEED*dy);
          startPt.setLocation(cp);
        }
      }
      @Override public void mousePressed(MouseEvent e) {
        scroller.stop();
        move.setLocation(0, 0);
        JComponent jc = (JComponent)e.getSource();
        jc.setCursor(hc);
        Container c = jc.getParent();
        if(c instanceof JViewport) {
          JViewport vport = (JViewport)c;
          Point cp = SwingUtilities.convertPoint(jc,e.getPoint(),vport);
          startPt.setLocation(cp);
        }
      }
      @Override public void mouseReleased(MouseEvent e) {
        ((JComponent)e.getSource()).setCursor(dc);
        scroller.start();
      }
      @Override public void mouseExited(MouseEvent e) {
        ((JComponent)e.getSource()).setCursor(dc);
        move.setLocation(0, 0);
        scroller.stop();
      }
    }

    public class TimelinePanel extends JPanel //implements Scrollable
    {
                        
        private Timeline timeline = null;
                        
        public TimelinePanel (Timeline timeline)
        {
                                        
            this.timeline = timeline;
                                        
        }
        
        public Dimension getPreferredScrollableViewportSize ()
        {
            
            return new Dimension (this.timeline.getViewWidth (),
                                  500);
            
        }
        
        public int getScrollableBlockIncrement (Rectangle vr,
                                                int orien,
                                                int dir)
        {
            
            return 200;
            
        }

        public int getScrollableUnitIncrement (Rectangle vr,
                                                int orien,
                                                int dir)
        {
            
            return 20;
            
        }
        
        public boolean getScrollableTracksViewportHeight ()
        {
            
            return false;
            
        }

        public boolean getScrollableTracksViewportWidth ()
        {
            
            return false;
            
        }
        
        public void paintComponent (Graphics g)
        {

            this.setOpaque (false);

            Graphics2D g2 = (Graphics2D) g;

            g2.setPaint (Color.WHITE);
            g2.fill (new Rectangle (this.getSize ()));

            g2.setPaint (UIUtils.getColor ("#dfdfdf"));
            GregorianCalendar gc = new GregorianCalendar ();
            gc.setTime (this.timeline.viewStartDate);
            
            int incr = 1;
            int incrType = Calendar.DATE;

            if (this.timeline.currentScale.equals (SCALE_HOURLY))
            {
                
                incrType = Calendar.HOUR_OF_DAY;
                
            }

            if (this.timeline.currentScale.equals (SCALE_WEEKLY))
            {
                
                incr = 7;
                
            }
System.out.println ("VXW: " + this.timeline.getViewWidth ());                            
            int colW = this.timeline.getColumnWidth ();
            int colC = this.timeline.getColumnCount () + 1;
            int h = this.getSize ().height;
            
            for (int i = 0; i < colC - 1; i++)
            {
            
                int x = (i * colW)  + (colW / 2);

                Line2D line = new Line2D.Float (x,
                                                0,
                                                x,
                                                h);
                
                g2.draw (line);
                               
                gc.add (incrType,
                        incr);
            
            }

            float[] dash = {5f};

            BasicStroke st = new BasicStroke (1f,
                                              BasicStroke.CAP_BUTT,
                                              BasicStroke.JOIN_MITER,
                                              5f,
                                              dash,
                                              0f);

            g2.setStroke (st);

            float y = 0;

            for (int i = 0; i < this.getComponentCount (); i++)
            {
                
                Component c = this.getComponent (i);
                
                if (c instanceof TimelineThread)
                {
                    
                    TimelineThread tt = (TimelineThread) c;

                    if (i > 0)
                    {

                        Line2D line = new Line2D.Float (0,
                                                        y,
                                                        this.timeline.getViewWidth (),
                                                        y);

                        g2.draw (line);
                    
                    }
                    
                    y += (tt.getPreferredSize ().height + 5);
                    
                }
                
            }
                        
        }
                                
    }

}