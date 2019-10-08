package pipe.actions.gui;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.Serializable;

/**
 * This class contains information about the underlying state of the application
 * For example whether it is in animation mode and what action is currently selected
 *
 * 此类包含有关应用程序基础状态的信息，例如，它是否处于动画模式以及当前选择了什么操作
 */
@SuppressWarnings("serial")
public class PipeApplicationModel implements Serializable {
    //实现serializabel接口的作用是就是可以把对象存到字节流，然后可以恢复，所以你想如果你的对象没实现序列化怎么才能进行
    // 持久化和网络传输呢，要持久化和网络传输就得转为字节流，所以在分布式应用中及设计数据持久化的场景中，你就得实现序列化。
    /**
     * Message fired when animation mode is toggled
     * 切换动画模式时触发消息
     */
    public static final String TOGGLE_ANIMATION_MODE = "Toggle animation";

    /**
     * Message fired when the action type is changed on the tool bar
     * 在工具栏上更改操作类型时触发消息
     */
    public static final String TYPE_ACTION_CHANGE_MESSAGE = "Type action change";

    /**
     * Property change support for publish-subscribe architecture
     * 对发布-订阅体系结构的属性更改支持
     */
    protected final PropertyChangeSupport changeSupport = new PropertyChangeSupport(this);

    /**
     * Zoom percentages
     * 缩放百分比
     */
    private final String[] zoomExamples =
            new String[]{"40%", "60%", "80%", "100%", "120%", "140%", "160%", "180%", "200%", "300%"};

    /**
     * Application name
     * 应用名称
     */
    private final String name;

    /**
     * True if edition to the Petri net is allowed
     * 如果允许使用Petrinet版本，则为true
     */
    private boolean editionAllowed = true;

    /**
     * 传统模式已选择
     */
    private int mode;

    /**
     * Legacy old mode
     * 旧版旧模式
     */
    private int oldMode;

    /**
     * Determines if PIPE is viewing in animation mode or not
     * 确定PIPE是否以动画模式查看
     */
    private boolean inAnimationMode;

    /**
     * Type that is currently selected on the petrinet
     * 当前在petrinet上选择的类型
     */
    private CreateAction selectedType;

    /**
     * PipeApplicationModel 类的有参构造
     * Constructor
     * @param version e.g. 5
     */
    public PipeApplicationModel(String version) {
        //设置应用程序名称
        name = "PIPE: Platform Independent Petri Net Editor " + version+",auth:巩孟博";
    }

    /**
     * 添加一个监听器 监听当前对象属性
     * Adds a listener for changes in this model.
     * @param listener to add
     */
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        changeSupport.addPropertyChangeListener(listener);
    }

    /**
     * Remove a listener from this model
     * @param listener to remove 
     */
    public void removePropertyChangeListener(PropertyChangeListener listener) {
        changeSupport.removePropertyChangeListener(listener);
    }

    /**
     *
     * @return true if the net is in animation mode
     */
    public boolean isInAnimationMode() {
        return inAnimationMode;
    }

    /**
     * Set whether the Petri net should be in animation mode or not
     * @param inAnimationMode true for animation mode, false for edit mode
     */
    public void setInAnimationMode(boolean inAnimationMode) {
        boolean old = this.inAnimationMode;
        this.inAnimationMode = inAnimationMode;
        if (old != inAnimationMode) {
            changeSupport.firePropertyChange(TOGGLE_ANIMATION_MODE, old, inAnimationMode);
        }
    }

    /**
     *
     * @return zoom percentages to be displayed
     */
    public String[] getZoomExamples() {
        return zoomExamples;
    }

    /**
     *
     * @return name of the application
     */
    public String getName() {
        return name;
    }

    /**
     *
     * @return true if edition is allowed
     */
    public boolean isEditionAllowed() {
        return editionAllowed;
    }

    /**
     *
     * Set whether changes are allowed to be made to the Petri net at the given time
     *
     * @param flag true if edition is allowed, false otherwise
     */
    public void setEditionAllowed(boolean flag) {
        editionAllowed = flag;
    }


    /**
     *
     * @return current mode
     */
    public int getMode() {
        return mode;
    }

    /**
     *
     * Set the current GUIAction mode
     * @param mode for GUI actions 
     */
    public void setMode(int mode) {
        this.mode = mode; 
    }

    /**
     *
     * @param action set the currently selected action on the tool bar
     */
    public void selectTypeAction(CreateAction action) {
        CreateAction old = this.selectedType;
        selectedType = action;
        changeSupport.firePropertyChange(TYPE_ACTION_CHANGE_MESSAGE, old, selectedType);
    }

    /**
     *
     * @return the currently selected action on the tool bar
     */
    public CreateAction getSelectedAction() {
        return selectedType;
    }
}
