package pipe.controllers;

import pipe.historyActions.MultipleEdit;
import pipe.historyActions.component.MovePetriNetObject;
import pipe.utilities.gui.GuiUtils;
import uk.ac.imperial.pipe.exceptions.PetriNetComponentException;
import uk.ac.imperial.pipe.models.petrinet.*;

import javax.swing.event.UndoableEditEvent;
import javax.swing.undo.UndoableEdit;
import java.awt.Point;
import java.awt.geom.Point2D;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Handles dragging of objects around when selected
 * 选中时对处理对象进行拖动
 */
public class DragManager {

    /**
     * Petri net controller for which components will be dragged
     * 将为其拖动组件的petri网控制器
     */
    private PetriNetController petriNetController;

    /**
     * Starting coordinate of the drag
     * 拖动的起始坐标 并命名为dragStart
     */
    private Point2D.Double dragStart = new Point2D.Double(0, 0);//Point2D类定义一个点代表 (x,y)坐标空间中的位置。

    /**
     * All selected items locations at the start of a drag
     * 拖动开始时的所有选定项位置
     * Mapping of id -> location
     */
    private Map<String, Point2D> startingCoordinates = new HashMap<>();

    /**
     * Constructor
     * @param petriNetController controller for which components will be dragged
     *                           将为其拖动组件的控制器
     */
    public DragManager(PetriNetController petriNetController) {
        this.petriNetController = petriNetController;
    }

    /**
     *
     * @param dragStart the start location from which components are dragged
     */
    public void setDragStart(Point2D.Double dragStart) {
        this.dragStart = dragStart;
    }

    /**
     * Drag items to location
     *
     * @param location location of mouse to drag items to
     *                 鼠标拖动项目的位置
     */
    public void drag(Point location) {
        int x = (int) (location.getX() - dragStart.getX());
        int y = (int) (location.getY() - dragStart.getY());
        dragStart = new Point2D.Double(location.x, location.y);
        try {
            petriNetController.translateSelected(new Point(x, y));
        } catch (PetriNetComponentException e) {
            GuiUtils.displayErrorMessage(null, e.getMessage());
        }
    }

    /**
     * Saves the starting coordinates of all the selected items.
     * 保存所有选定项的起始坐标
     * This means that their undo drag item can reference their starting location
     * 这意味着它们的撤消拖动项可以涉及到它们的起始位置
     */
    public void saveStartingDragCoordinates() {
        startingCoordinates.clear();
        Map<PlaceablePetriNetComponent, Point2D> selectedPoints = getSelectedCoordinates();
        for (Map.Entry<PlaceablePetriNetComponent, Point2D> entry : selectedPoints.entrySet()) {
            startingCoordinates.put(entry.getKey().getId(), entry.getValue());
        }
    }

    /**
     * Method to call after finishing a drag,
     * ensures undoable edit is created
     * 方法在完成拖动后调用，以确保创建可撤消的编辑。
     */
    public void finishDrag() {
        Map<PlaceablePetriNetComponent, Point2D> translatedCoordinates = getSelectedCoordinates();
        createMovedUndoItem(startingCoordinates, translatedCoordinates);
    }

    /**
     * Loops through each Placeable PetriNet Components start and ending coordinates (i.e. before and after translation)
     * 循环遍历每个可放置的petrinet组件的开始和结束坐标（即转换前后）
     * and creates a {@link pipe.historyActions.component.MovePetriNetObject} undoEdit for each event
     *并为每个事件创建撤消编辑
     * It then creates an {@link pipe.historyActions.MultipleEdit} with all these undoEdits in and
     * registers this with the undoListener.
     * 然后，它创建一个{@link pipe.historyactions.multipleedit}，其中包含所有这些撤消编辑，并将其注册到撤消侦听器。
     * @param startingCoordinates of selected items before translation
     * @param translatedCoordinates of selected items after translation
     */
    private void createMovedUndoItem(Map<String, Point2D> startingCoordinates,
                                     Map<PlaceablePetriNetComponent, Point2D> translatedCoordinates) {
        List<UndoableEdit> undoableEdits = new LinkedList<>();
        for (Map.Entry<PlaceablePetriNetComponent, Point2D> entry : translatedCoordinates.entrySet()) {
            PlaceablePetriNetComponent component = entry.getKey();
            Point2D starting = startingCoordinates.get(component.getId());
            Point2D translated = entry.getValue();
            if (!starting.equals(translated)) {
                undoableEdits.add(new MovePetriNetObject(component, starting, translated));
            }
        }
        if (!undoableEdits.isEmpty()) {
            petriNetController.getUndoListener().undoableEditHappened(new UndoableEditEvent(this, new MultipleEdit(undoableEdits)));
        }
    }

    /**
     *
     * @return the coordinates of all selected items
     * 所有选定项的坐标
     */
    private Map<PlaceablePetriNetComponent, Point2D> getSelectedCoordinates() {
        CoordinateSaver saver = new CoordinateSaver();
        for (PetriNetComponent component : petriNetController.getSelectedComponents()) {
            if (component.isDraggable()) {
                try {
                    component.accept(saver);
                } catch (PetriNetComponentException e) {
                    GuiUtils.displayErrorMessage(null, e.toString());
                }
            }
        }
        return saver.savedCoordinates;
    }

    /**
     * Saves the coordinates of selectable items
     * 保存可选择项的坐标
     */
    private static class CoordinateSaver
            implements ArcVisitor, ArcPointVisitor, PlaceVisitor, TransitionVisitor, AnnotationVisitor {

        /**
         * Map containing a component to its coordinates
         * 包含组件到其坐标的映射
         */
        private Map<PlaceablePetriNetComponent, Point2D> savedCoordinates = new HashMap<>();

        /**
         *
         * Saves the annotaiton coordinates
         *保存注释坐标
         * @param annotation
         */
        @Override
        public void visit(Annotation annotation) {
            savedCoordinates.put(annotation, new Point2D.Double(annotation.getX(), annotation.getY()));
        }

        /**
         * Arc point coordinates
         * @param arcPoint
         * 弧点坐标
         */
        @Override
        public void visit(ArcPoint arcPoint) {
            savedCoordinates.put(arcPoint, arcPoint.getPoint());
        }

        /**
         * Saves the place coordinates
         * 保存库所坐标
         * @param place
         */
        @Override
        public void visit(Place place) {
            savedCoordinates.put(place, new Point2D.Double(place.getX(), place.getY()));
        }

        /**
         *
         * Saves the transition coordinates
         * 保存变迁的坐标
         * @param transition
         */
        @Override
        public void visit(Transition transition) {
            savedCoordinates.put(transition, new Point2D.Double(transition.getX(), transition.getY()));

        }

        /**
         * Noop action
         * @param inboundArc
         */
        @Override
        public void visit(InboundArc inboundArc) {
            //TODO: Arc arc points covered by the above?
        }

        /**
         * Noop action
         * @param outboundArc
         */
        @Override
        public void visit(OutboundArc outboundArc) {
            //TODO: Arc arc points covered by the above?
        }
    }
}
