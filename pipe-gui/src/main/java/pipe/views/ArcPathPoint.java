/*
 * Created on 28-Feb-2004
 * @author Michael Camacho (and whoever wrote the first bit!)
 * @author Edwin Chung 16 Mar 2007: modified the constructor and several other
 * functions so that DataLayer objects can be created outside the GUI
 */
package pipe.views;

import pipe.constants.GUIConstants;
import pipe.controllers.PetriNetController;
import uk.ac.imperial.pipe.models.petrinet.ArcPoint;

import java.awt.Container;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RectangularShape;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;


/**
 * 此类以图形方式表示弧形路径上的每个点。
 * This class represents each point on the arc path graphically.
 * 这是旧代码，因此需要解决Bezier数学等问题
 * It's old code so the Bezier(贝塞尔曲线(Bézier curve)，又称贝兹曲线或贝济埃曲线，是应用于二维图形应用程序的数学曲线。)
 * maths etc. needs to be addressed
 * 圆弧路径点
 * arc path point
 */
@SuppressWarnings("serial")
public class ArcPathPoint extends AbstractPetriNetViewComponent<ArcPoint> {

    /**
     * 布尔值，确定圆弧是直线还是曲线
     * Boolean value determining if the arc is straight or a curve
     */
    public static final boolean STRAIGHT = false;

    private static final int SIZE_OFFSET = 1;

    /**
     * 点的大小
     * Size of the point
     */
    private static final int SIZE = 3;

    /**
     * Underlying point model
     */
    private final ArcPoint model;

    /**
     * Control used for Bezier curve
     */
    private final Point2D.Double control1 = new Point2D.Double();

    /**
     * Control used for Bezier curve
     */
    private final Point2D.Double control = new Point2D.Double();

    /**
     * Path point belongs to
     */
    private ArcPath arcPath;

    /**
     * Sets copyPastable to false because we cant copy paste individual arc points
     */
    private void setup() {
        copyPasteable = false;
    }

    public void setPointLocation(double x, double y) {
        setBounds((int) x - SIZE, (int) y - SIZE, 2 * SIZE + SIZE_OFFSET, 2 * SIZE + SIZE_OFFSET);
    }

    /**
     * Constructor
     * @param point on the arc
     * @param arcPath path of the arc
     * @param petriNetController Petri net controller
     * @param parent container
     */
    public ArcPathPoint(ArcPoint point, ArcPath arcPath, PetriNetController petriNetController, Container parent) {
        super("", point, petriNetController, parent);
        setup();
        model = point;
        setPointLocation(model.getPoint());
        this.arcPath = arcPath;
        model.addPropertyChangeListener(new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent propertyChangeEvent) {
                if (propertyChangeEvent.getPropertyName().equals(ArcPoint.UPDATE_LOCATION_CHANGE_MESSAGE)) {
                    Point2D point = (Point2D) propertyChangeEvent.getNewValue();
                    setPointLocation(point.getX(), point.getY());
                }
            }
        });

    }

    public final void setPointLocation(Point2D point) {
        setPointLocation(point.getX(), point.getY());
    }

    public Point2D getPoint() {
        return model.getPoint();
    }

    public boolean isCurved() {
        return model.isCurved();
    }

    public void setVisibilityLock(boolean lock) {
        arcPath.setPointVisibilityLock(lock);
    }

    public double getAngle(Point2D.Double p2) {
        double angle;

        if (model.getPoint().getY() <= p2.y) {
            angle = Math.atan((model.getPoint().getX() - p2.x) / (p2.y - model.getPoint().getY()));
        } else {
            angle = Math.atan((model.getPoint().getX() - p2.x) / (p2.y - model.getPoint().getY())) + Math.PI;
        }

        // Needed to eliminate an exception on Windows
        if (model.getPoint().equals(p2)) {
            angle = 0;
        }
        return angle;
    }

    public Point2D.Double getMidPoint(ArcPathPoint target) {
        return new Point2D.Double((target.model.getPoint().getX() + model.getPoint().getX()) / 2,
                (target.model.getPoint().getY() + model.getPoint().getY()) / 2);
    }

    public Point2D.Double getControl1() {
        return control1;
    }

    public void setControl1(Point2D.Double p) {
        control1.x = p.x;
        control1.y = p.y;
    }

    public Point2D.Double getControl() {
        return control;
    }

    public void setControl(Point2D.Double p) {
        control.x = p.x;
        control.y = p.y;
    }

    public void setControl1(double _x, double _y) {
        control1.x = _x;
        control1.y = _y;
    }

    public void setControl2(double _x, double _y) {
        control.x = _x;
        control.y = _y;
    }

    @Override
    public void addToContainer(Container container) {
        // Nothing needed
    }

    @Override
    public String getName() {
        return this.getArcPath().getArc().getModel().getName() + " - Point " + this.getIndex();
    }

    public int getIndex() {
        for (int i = 0; i < arcPath.getNumPoints(); i++) {
            if (arcPath.getPathPoint(i) == this) {
                return i;
            }
        }
        return -1;
    }

    public ArcPath getArcPath() {
        return arcPath;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + model.hashCode();
        return result;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }

        ArcPathPoint pathPoint = (ArcPathPoint) o;

        if (!model.equals(pathPoint.model)) {
            return false;
        }

        return true;
    }

    /**
     * Performs a delete only if there are more than two points left after deleting this one
     */
    @Override
    public void componentSpecificDelete() {
        if (isDeleteable()) {
            if (getArcPath().getArc().isSelected()) {
                return;
            }
            kill();
        }
    }

    @Override
    public void paintComponent(Graphics g) {

        super.paintComponent(g);
        if (!ignoreSelection) {
            Graphics2D g2 = (Graphics2D) g;

            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_NORMALIZE);

            RectangularShape shape;
            if (model.isCurved()) {
                shape = new Ellipse2D.Double(0, 0, 2 * SIZE, 2 * SIZE);
            } else {
                shape = new Rectangle2D.Double(0, 0, 2 * SIZE, 2 * SIZE);
            }

            if (isSelected()) {
                g2.setPaint(GUIConstants.SELECTION_FILL_COLOUR);
                g2.fill(shape);
                g2.setPaint(GUIConstants.SELECTION_LINE_COLOUR);
                g2.draw(shape);
            } else {
                g2.setPaint(GUIConstants.ELEMENT_FILL_COLOUR);
                g2.fill(shape);
                g2.setPaint(GUIConstants.ELEMENT_LINE_COLOUR);
                g2.draw(shape);
            }
        }
    }

    public boolean isDeleteable() {
        int i = getIndex();
        return i > 0 && i != arcPath.getNumPoints() - 1;
    }

    public void kill() {
        // called internally by ArcPoint and parent ArcPath
        super.removeFromContainer();
    }
}
