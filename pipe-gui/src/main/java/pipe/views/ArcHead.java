package pipe.views;
import java.awt.*;

/**
 * 弧头的图形表示。
 * Graphical representation of the head of an arc.
 * E.g. 圆形用于禁止电弧或三角形用于正常电弧
 * circular for inhibitor arcs or triangular for normal arcs
 */
public interface ArcHead {
    /**
     * Draw using graphics g2
     * @param g2 graphics
     */
    void draw(Graphics2D g2);
}