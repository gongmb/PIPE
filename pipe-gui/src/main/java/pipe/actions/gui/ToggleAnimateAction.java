package pipe.actions.gui;

import pipe.controllers.GUIAnimator;
import pipe.controllers.PetriNetController;
import pipe.controllers.application.PipeApplicationController;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * This action is responsible for toggling the petri net in and out of animation mode
 */
public class ToggleAnimateAction extends AnimateAction {
    private final PipeApplicationModel applicationModel;

    private final PipeApplicationController applicationController;

    /**
     * Noop动作用于切换动画
     * Noop action to be used for toggling in and out of animation
     *删除动画模式下发生的任何虚假创建
     * Removes any spurious creates happening in animation mode
     */
    private final ActionListener noopAction;

    public ToggleAnimateAction(String name, String tooltip, String keystroke, PipeApplicationModel applicationModel,
                               PipeApplicationController applicationController) {
        super(name, tooltip, keystroke);
        this.applicationModel = applicationModel;
        this.applicationController = applicationController;
        noopAction = new NoopAction(applicationModel);
    }

    @Override
    public void actionPerformed(ActionEvent event) {
        PetriNetController petriNetController = applicationController.getActivePetriNetController();
        boolean animated = petriNetController.toggleAnimation();

        applicationModel.setInAnimationMode(animated);

        noopAction.actionPerformed(null);

        GUIAnimator animator = petriNetController.getAnimator();
        if (animated) {
            animator.startAnimation();
        } else {
            animator.finish();
        }
    }
}
