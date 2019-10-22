/*
 * Created on 07-Mar-2004
 */
package pipe.actions.gui;

import java.net.URL;

import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import javax.swing.KeyStroke;
import javax.swing.event.UndoableEditEvent;
import javax.swing.event.UndoableEditListener;
import javax.swing.undo.UndoableEdit;

import pipe.gui.PipeResourceLocator;


/**
 * 所有PIPE GUI动作都应该子类化的抽象动作。 这些
   操作应该是PIPE工具栏上的按钮
 * Abstract action which all PIPE GUI actions should subclass. These
 * actions should be buttons on the PIPE tool bar
 *此类负责加载按钮的图像
 * This class is responsible for loading the images of the button
 */
@SuppressWarnings("serial")
public abstract class GuiAction extends AbstractAction {

    public static final String SELECTED = "selected";

    protected UndoableEditListener listener;


    /**
     *构造函数加载图像并设置工具提示消息。
     * Constructor loading the image and setting the tool tip mssage.
     * 将操作的键盘快捷键设置为指定的快捷键
     * It sets the keyboard shortcut of the action to that specified
     *
     * @param name      image name
     * @param tooltip   tooltip message
     * @param key       {@link java.awt.event.KeyEvent} key
     * @param modifiers e.g. ctrl/shift obtained from {@link java.awt.event.InputEvent}
     */
    protected GuiAction(String name, String tooltip, int key, int modifiers) {
        this(name, tooltip);
        putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(key, modifiers));
    }

    /**
     *
     * Constructor loading the image and setting the tooltip message.
     * It does not provide a keyboard shortcut
     * @param name image name
     * @param tooltip tooltip message
     */
    protected GuiAction(String name, String tooltip) {
        super(name);
		PipeResourceLocator locator = new PipeResourceLocator(); 
		try {
			URL iconURL = locator.getImage(name);
			putValue(SMALL_ICON, new ImageIcon(iconURL));
		} catch (RuntimeException e) {
			// some actions don't have icons; ignore
		}

        if (tooltip != null) {
            putValue(SHORT_DESCRIPTION, tooltip);
        }
    }

    /**
     *
     * Constructor loading the image and setting the tooltip message.
     * It sets the short cut to the specified keystrol and the accelerator key
     * @param name image name
     * @param tooltip tooltip message
     * @param keystroke shortcut for action
     */
    protected GuiAction(String name, String tooltip, String keystroke) {

        this(name, tooltip);
        if (keystroke != null) {
            putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(keystroke));
        }
    }

    /**
     * Adds a listener to this action that is interested in undoable actions
     * @param l listener
     */
    public void addUndoableEditListener(UndoableEditListener l) {
        //TODO: Should ideally throw an exception if listener != null
        listener = l;
    }

    /**
     * Removes the listener from this action
     * @param l listener
     */
    public void removeUndoableEditListener(UndoableEditListener l) {
        listener = null;
    }

    /**
     *
     * @return true if the action is currently selected
     */
    public boolean isSelected() {
        Boolean b = (Boolean) getValue(SELECTED);

        return b != null && b;
    }


    /**
     *
     * @param selected true or false for setting the action as selected and not selected accordingly
     */
    public void setSelected(boolean selected) {
        Boolean b = (Boolean) getValue(SELECTED);

        if (b != null) {
            putValue(SELECTED, Boolean.valueOf(selected));
        }
    }

    /**
     *
     * Notifies the lister that the following undo event has been created.
     *
     * @param edit event 
     */
    protected void registerUndoEvent(UndoableEdit edit) {
        if (listener != null) {
            listener.undoableEditHappened(new UndoableEditEvent(this, edit));
        }
    }

}
