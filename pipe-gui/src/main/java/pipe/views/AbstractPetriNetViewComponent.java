package pipe.views;

import pipe.controllers.PetriNetController;
import uk.ac.imperial.pipe.models.petrinet.PetriNetComponent;

import javax.swing.*;
import java.awt.Container;
import java.awt.Rectangle;


/**
 * 抽象视图组件
 * Abstract view component
 * 基本Petri网组件模型
 * @param <T> underlying Petri net component model
 */
public abstract class AbstractPetriNetViewComponent<T extends PetriNetComponent> extends JComponent implements PetriNetViewComponent {

    /**
     * x，y与模型位置的偏移
     * x, y offset from the model position
     */
    public static final int COMPONENT_DRAW_OFFSET = 5;

    /**
     * 如果忽略选择为true，则不应选择组件
     * If ignore selection is true then the component should not be selected
     */
    protected static boolean ignoreSelection = false;

    /**
     *
     70/5000
     Petri网组件所在的Petri网的控制器
     * Controller for the Petri net that the Petri net component is housed in
     */
    protected final PetriNetController petriNetController;

    /**
     * Petri网组件ID
     * Petri net component id
     */
    protected String id;

    /**
     * 包含此组件的父级。
     * Parent that this component is contained in.
     */
    protected final Container parent;

    /**
     * 如果可以复制和粘贴项目，则为true
     * true if the item can be copy and pasted
     */
    protected boolean copyPasteable;

    /**
     * 查看项目范围
     * View item bounds
     *因为画布没有布局管理器而使用
     * Used because the canvas has no layout manager
     *
     */
    protected Rectangle bounds;

    /**
     * 如果此视图已被删除，则为True
     * True if this view has been deleted
     */
    protected boolean deleted;

    /**
     * 旧版标记为已删除的代码
     * Legacy mark as deleted code
     */
    @Deprecated
    protected boolean markedAsDeleted;

    /**
     *
     基础模型
     * Underlying model
     */
    protected T model;

    /**
     * 如果组件是可选的，则为真
     * True if the component is selectable
     */
    @Deprecated
    protected boolean selectable;

    /**
     * Constructor
     * @param id component id
     * @param model model
     * @param controller Petri net controller that the model belongs to
     * @param parent Parent of the view
     */
    public AbstractPetriNetViewComponent(String id, T model, PetriNetController controller, Container parent) {
        this.id = id;
        this.parent = parent;
        selectable = true;
        copyPasteable = true;
        bounds = new Rectangle();
        deleted = false;
        markedAsDeleted = false;
        this.model = model;
        this.petriNetController = controller;
    }

    /**
     * 为了执行组件而必须执行删除的任何代码
     * Any code that must be executed in order to perform a component delete
     */
    public abstract void componentSpecificDelete();

    @Override
    public int hashCode() {
        return model.hashCode();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        AbstractPetriNetViewComponent that = (AbstractPetriNetViewComponent) o;

        if (!model.equals(that.model)) {
            return false;
        }

        return true;
    }

    /**
     * @return model id
     */
    public final String getId() {
        return id;
    }

    /**
     *
     * @return underlying model
     */
    public final T getModel() {
        return model;
    }

    /**
     * Delete the view
     */
    @Override
    public final void delete() {
        componentSpecificDelete();
        deleted = true;
        removeFromContainer();
        removeAll();
    }

    /**
     * Remove the view from its container
     */
    protected final void removeFromContainer() {
        Container c = getParent();

        if (c != null) {
            c.remove(this);
        }
    }


    /**
     *
     * @return the x, y draw offset
     */
    protected static int getComponentDrawOffset() {
        return COMPONENT_DRAW_OFFSET;
    }

    /**
     * @return true if model selected
     */
    public final boolean isSelected() {
        return petriNetController.isSelected(model);
    }
}
