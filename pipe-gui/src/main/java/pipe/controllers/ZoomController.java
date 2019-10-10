package pipe.controllers;

import pipe.constants.GUIConstants;

import java.awt.geom.AffineTransform;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.Serializable;

/**
 * Zoom controller repsonsible for zooming in and out of a Petri net tab
 * 可用于放大和缩小petri网选项卡的缩放控制器
 */
@SuppressWarnings("serial")
public class ZoomController implements Serializable {

    /**
     * Change support for firing events when percent is changed.
     * 当百分比被触发时，更改事件的支持。
     */
    protected PropertyChangeSupport changeSupport = new PropertyChangeSupport(this);

   /**PropertyChangeSupport这是一个可用于支持绑定属性的豆的实用程序类。它管理的列表 PropertyChangeEvents听众和派遣他们。
       您可以使用这个类的一个实例作为您的豆的成员字段，
       并将这些类型的工作委托给它的 PropertyChangeListener可以注册为所有属性或属性指定的名字。*/
    /**
     * Zoom percentage, 100% = unzoomed
     */
    private int percent;

    /**
     * Constructor
     * @param pct initial percentage to start with
     *            起始百分比pct
     */
    public ZoomController(int pct) {
        percent = pct;
    }

    /**
     *
     * Add a listener to be triggered when zooming in and out
     *添加放大和缩小时要触发的侦听器
     * @param listener to add
     */
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        changeSupport.addPropertyChangeListener(listener);
    }

    /**
     * Removes the listener from this controller
     * 移除监听器
     * @param listener to remove 
     */
    public void removePropertyChangeListener(PropertyChangeListener listener) {
        changeSupport.removePropertyChangeListener(listener);
    }

    /**
     *
     * @return the affine transform matrix for the zoom level
     *             缩放级的仿射变换矩阵
     */
    public AffineTransform getTransform() {
        return AffineTransform.getScaleInstance(percent * 0.01, percent * 0.01);
    }

    /**
     *
     * @return the scale factor e.g. 0.1, 0.5, 1.0 etc.
     * 比例因子
     */
    public double getScaleFactor() {
        return percent * 0.01;
    }

    /**
     * zooms out
     */
    public void zoomOut() {
        if (canZoomOut()) {
            int old = percent;
            percent -= GUIConstants.ZOOM_DELTA;
            changeSupport.firePropertyChange("zoomOut", old, percent);
        }
    }

    /**
     *
     * @return true if it can zoom out
     */
    public boolean canZoomOut() {
        int newPercent = percent - GUIConstants.ZOOM_DELTA;
        return newPercent >= GUIConstants.ZOOM_MIN;
    }

    /**
     * Zooms in
     */
    public void zoomIn() {
        if (canZoomIn()) {
            int old = percent;
            percent += GUIConstants.ZOOM_DELTA;
            changeSupport.firePropertyChange("zoomIn", old, percent);
        }
    }

    /**
     *
     * @return true if it can zoom in
     */
    public boolean canZoomIn() {
        int newPercent = percent + GUIConstants.ZOOM_DELTA;
        return newPercent <= GUIConstants.ZOOM_MAX;
    }

    /**
     *
     * @return zoom percentage e.g. 10%, 20%, 100%, 120%
     */
    public int getPercent() {
        return percent;
    }

    private void setPercent(int newPercent) {
        if (newPercent >= GUIConstants.ZOOM_MIN && newPercent <= GUIConstants.ZOOM_MAX) {
            percent = newPercent;
        }
    }

    /**
     *
     * @param newPercent the new zoom percentage
     */
    public void setZoom(int newPercent) {
        setPercent(newPercent);
    }
}
