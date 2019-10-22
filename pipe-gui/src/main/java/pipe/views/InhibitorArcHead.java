package pipe.views;

import java.awt.*;

/*
 * 画一个圆形代表抑制弧头
 * Draws a round circle to represent inhibitor arc heads
 */
public class InhibitorArcHead implements ArcHead {
    /**
     * 椭圆的宽度
     * Width of the oval
     */
    private static final int OVAL_WIDTH = 8;

    /**
     * 椭圆的高度
     * Height of the oval
     */
    private static final int OVAL_HEIGHT = 8;

    /**
     * x相对于椭圆0的位置
     * x location relative to 0 of the oval
     */
    private static final int OVAL_X = -OVAL_WIDTH;

    /**
     * y相对于椭圆0的位置
     * y location relative to 0 of the oval
     */
    private static final int OVAL_Y = -OVAL_HEIGHT/2;

    /**
     *在图形所在的地方绘制圆圈
     * Will draw the circle where the graphics is located
     * @param g2 graphics
     */
    @Override
    public void draw(Graphics2D g2) {
        Graphics2D graphics2D = (Graphics2D) g2.create();
        graphics2D.setStroke(new BasicStroke(0.6f));//0.8f

        graphics2D.setColor(Color.WHITE);
        graphics2D.fillOval(OVAL_X, OVAL_Y, OVAL_WIDTH, OVAL_HEIGHT);

        graphics2D.setColor(Color.BLACK);
        graphics2D.drawOval(OVAL_X, OVAL_Y, OVAL_WIDTH, OVAL_HEIGHT);
        graphics2D.dispose();
    }
}
