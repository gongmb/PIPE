package pipe.historyActions;


import uk.ac.imperial.pipe.models.petrinet.Transition;

import java.util.ArrayList;
import java.util.List;
import java.util.Observable;

/**
 * AnimationHistory for an individual PetriNet
 * 单个Petri网的动画记录
 */
public final class AnimationHistoryImpl extends Observable implements AnimationHistory {
    /**
     * List to hold transitions fired in their order
     * Used for going back/forward in time
     * 保存按顺序触发的变迁的列表，用于及时返回/前进
     */
    private List<Transition> firingSequence = new ArrayList<>();

    /**
     * Current index of the firingSequence;
     * 触发序列的当前索引
     * Initialised to -1 so when the first item is added it points to zero
     * 初始化为-1，因此当第一个项被添加时，它指向零
     */
    private int currentPosition = -1;


    /**
     * Cannot step forward if head of the list
     *
     * @return true if stepping forward within the animation is allowed, that is if there are transition firings to redo
     * 如果列表头返回true，则无法前进。如果允许在动画中前进，即如果有要重做的转换触发
     */
    @Override
    public boolean isStepForwardAllowed() {
        return currentPosition < firingSequence.size() - 1;
    }


    /**
     * Can step back if currentPosition points to any transitions
     * 如果当前位置指向任何变迁，则可以后退
     * @return true if stepping backward within the animation is allowed, that is if there are transition firings to undo
     */
    @Override
    public boolean isStepBackAllowed() {
        return currentPosition >= 0;
    }

    /**
     * Steps forward firing the transition associated with the latest action
     * 前进一步启动与最新操作关联的变迁
     */
    @Override
    public void stepForward() {
        if (isStepForwardAllowed()) {
            currentPosition++;
            flagChanged();
        }
    }


    /**
     * Steps backwards updating the current transition highlighted in the list
     * 逐步向后更新列表中突出显示的当前变迁
     */
    @Override
    public void stepBackwards() {
        if (isStepBackAllowed()) {
            currentPosition--;
            flagChanged();
        }
    }

    /**
     * Remove all steps past the current step
     * 删除当前步骤之后的所有步骤
     */
    @Override
    public void clearStepsForward() {
        if (currentPosition >= -1 && currentPosition + 1 < firingSequence.size()) {
            while (firingSequence.size() > currentPosition + 1) {
                firingSequence.remove(firingSequence.size() - 1);
            }
        }
    }

    /**
     *
     * @return list of transitions in the firing sequence
     */
    @Override
    public List<Transition> getFiringSequence() {
        return firingSequence;
    }

    /**
     *
     * @return current position in the firing sequence
     */
    @Override
    public int getCurrentPosition() {
        return currentPosition;
    }

    /**
     * Add a transition to the head of the firing sequence
     * @param transition to be added 
     */
    @Override
    public void addHistoryItem(Transition transition) {
        firingSequence.add(transition);
        currentPosition++;
        flagChanged();
    }

    /**
     *
     * @return transition at current position in the firing sequence
     */
    @Override
    public Transition getCurrentTransition() {
        if (currentPosition >= 0) {
            return firingSequence.get(currentPosition);
        }
        throw new RuntimeException("No transitions in history");
    }

    /**
     *
     * @param index of the transition 
     * @return transition at the given index in the firing sequence
     */
    @Override
    public Transition getTransition(int index) {
        if (index <= firingSequence.size()) {
            return firingSequence.get(index);
        }
        throw new RuntimeException("Index is greater than number of transitions stored");
    }

    /**
     * Clears the firing sequence
     */
    @Override
    public void clear() {
        currentPosition = -1;
        firingSequence.clear();
        flagChanged();
    }

    /**
     * Rolls the setting changed and notifying observers into one method call.
     * It tells Observers that it has changed
     */
    private void flagChanged() {
        setChanged();
        notifyObservers();
    }
}
