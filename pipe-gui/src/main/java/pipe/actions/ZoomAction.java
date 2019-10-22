package pipe.actions;

import pipe.actions.gui.GuiAction;
import pipe.utilities.gui.GuiUtils;

import java.awt.event.ActionEvent;

/**
 * 缩放动作，用于放大和缩小画布
 * Zoom action for zooming in and out of the canvas
 */
@SuppressWarnings("serial")
public class ZoomAction extends GuiAction
{
    /**
     * Constructor
     * @param name of the action
     * @param tooltip for the action
     * @param keystroke shortcut
     */
    public ZoomAction(String name, String tooltip, String keystroke)
    {
        super(name, tooltip, keystroke);
    }

    /**
     * Performs the zoom
     *由于缩放功能中的错误，目前不支持此操作。 而是显示一条错误消息
     * This action is currently unsupported due to bugs in the zoom functionality. Instead an error
     * message is displayed
     * @param e event 
     */
    @Override
    public void actionPerformed(ActionEvent e)
    {
        GuiUtils.displayErrorMessage(null,
                "Zooming in/out is currently not supported in this version.\n Please file an issue if it is particularly important to you.");

    }


}
