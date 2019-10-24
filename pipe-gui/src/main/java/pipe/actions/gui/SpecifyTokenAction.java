package pipe.actions.gui;

import pipe.controllers.PetriNetController;
import pipe.controllers.application.PipeApplicationController;
import pipe.gui.AbstractDatum;
import pipe.gui.TokenEditorPanel;
import pipe.historyActions.MultipleEdit;
import pipe.historyActions.component.AddPetriNetObject;
import pipe.historyActions.component.ChangePetriNetComponentName;
import pipe.historyActions.component.DeletePetriNetObject;
import pipe.historyActions.token.ChangeTokenColor;
import pipe.utilities.gui.GuiUtils;
import pipe.views.PipeApplicationView;
import uk.ac.imperial.pipe.exceptions.PetriNetComponentException;
import uk.ac.imperial.pipe.exceptions.PetriNetComponentNotFoundException;
import uk.ac.imperial.pipe.models.petrinet.PetriNet;
import uk.ac.imperial.pipe.models.petrinet.PetriNetComponent;
import uk.ac.imperial.pipe.models.petrinet.Token;

import javax.swing.*;
import javax.swing.undo.UndoableEdit;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * 用于在Petri网中创建，删除和编辑令牌的操作
 * Action used to create, delete and edit tokens in the Petri net
 */
@SuppressWarnings("serial")
public class SpecifyTokenAction extends GuiAction {
    /**
     * 主PIPE应用控制器
     * Main PIPE application controller
     */
    private final PipeApplicationController pipeApplicationController;

    /**
     * PIPE主应用程序视图
     * Main PIPE application view
     */
    private final PipeApplicationView applicationView;

    /**
     * Pop up editor panel for the tokens
     */
    private TokenEditorPanel tokenEditorPanel;

    /**
     * Pop up dialog
     */
    private JDialog guiDialog;

    /**
     * 旧式动作，我不确定这是什么
     * Legacy action, I'm not sure what this is
     */
    private ActionEvent forcedAction;

    public SpecifyTokenAction(PipeApplicationController pipeApplicationController,
                              PipeApplicationView applicationView) {
        super("SpecifyTokenClasses", "Specify tokens (ctrl-shift-T)", KeyEvent.VK_T,
                Toolkit.getDefaultToolkit().getMenuShortcutKeyMask() | InputEvent.SHIFT_DOWN_MASK);
        this.pipeApplicationController = pipeApplicationController;
        this.applicationView = applicationView;
    }

    /**
     * 如果有活动的Petri网，则弹出以更改Petri网令牌
     * Pops up to change the petri net tokens if there is an active petri net
     *
     * @param e event 
     */
    @Override
    public void actionPerformed(ActionEvent e) {
        if (pipeApplicationController.getActivePetriNetController() != null) {
            buildTokenGuiClasses();
            finishBuildingGui();
        }
    }

    /**
     * 使用Petri网中的令牌创建一个新的令牌编辑器
     * Creates a new token editor with the tokens from the Petri net
     */
    public void buildTokenGuiClasses() {
        tokenEditorPanel = new TokenEditorPanel(pipeApplicationController.getActivePetriNetController());
        guiDialog = new TokenDialog("Tokens令牌", true, tokenEditorPanel);
    }

    /**
     * 提供令牌编辑器面板外观的设置
     * Provides the set up for what the token editor panel will look like
     */
    public void finishBuildingGui() {
        guiDialog.setSize(600, 200);
        guiDialog.setLocationRelativeTo(null);
        tokenEditorPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        tokenEditorPanel.setOpaque(true);

        JPanel buttonPane = new JPanel();
        buttonPane.setLayout(new BoxLayout(buttonPane, BoxLayout.LINE_AXIS));
        buttonPane.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));
        buttonPane.add(Box.createHorizontalGlue());
        JButton ok = new JButton("OK");
        ok.addActionListener((ActionListener) guiDialog);
        buttonPane.add(ok);
        buttonPane.add(Box.createRigidArea(new Dimension(10, 0)));
        JButton cancel = new JButton("Cancel");
        cancel.addActionListener((ActionListener) guiDialog);
        buttonPane.add(cancel);

        guiDialog.add(tokenEditorPanel, BorderLayout.CENTER);
        guiDialog.add(buttonPane, BorderLayout.PAGE_END);
        tokenEditorPanel.setVisible(true);

        if (forcedAction != null) {
            forceContinue();
        } else {
            guiDialog.setVisible(true);
        }
    }

    /**
     * 旧版代码
     * Legacy code
     */
    private void forceContinue() {
        ((TokenDialog) guiDialog).actionPerformed(forcedAction);
        forcedAction = null;
    }

    /**
     * Alex Charalambous，2010年6月：ColorDrawer，ColorPicker，
     * TokenPanel和TokenDialog是用于显示“标记类”对话框的四个类（可通过butto工具栏访问）。
     * @author Alex Charalambous, June 2010: ColorDrawer, ColorPicker,
     *         TokenPanel and TokenDialog are four classes used
     *         to display the Token Classes dialog (accessible through the button
     *         toolbar).
     */

    public class TokenDialog<T extends PetriNetComponent> extends JDialog implements ActionListener {

        /**
         *类记录器
         * Class logger
         */
        private final Logger logger = Logger.getLogger(TokenDialog.class.getName());

        /**
         * 编辑器面板
         * Editor panel
         */
        private TokenEditorPanel tokenEditorPanel;

        /**
         *令牌对话框标题
         * @param title token dialog title
         *              对话
         * @param modal dialog
         *              面板
         * @param tokenEditorPanel panel 
         */
        public TokenDialog(String title, boolean modal, TokenEditorPanel tokenEditorPanel) {
            super(applicationView, title, modal);
            this.tokenEditorPanel = tokenEditorPanel;
        }

        /**
         * 处理Petri网令牌的更改
         * Processes the changing of Petri net tokens
         * 事件
         * @param e event 
         */
        @Override
        public void actionPerformed(ActionEvent e) {
            if (e.getActionCommand().equals("OK好嘞")) {
                if (tokenEditorPanel.isDataValid()) {
                    updateFromTable(tokenEditorPanel.getTableData());
                    removeDeletedData(tokenEditorPanel.getDeletedData());
                    setVisible(false);
                }
            } else if (e.getActionCommand().equals("Cancel取消")) {
                setVisible(false);
            }
        }

        //TODO: ONCE PETRINET CAN GET COMPONENT BY ID YOU CAN MAKE THIS WHOLE CLASS ABSTRACT
        //      AND SHARE IT WITH RATE EDITOR

        /**
         * 从Petri网中删除令牌。
         * Removes tokens from the Petri net.
         *
         * 如果由于某种原因无法删除令牌（例如，场所仍包含其类型的令牌），则会创建错误消息。 它将应用其他所有的更改。
         * Creates error message if a token cannot be removed for some reason, for
         * example if places still contain tokens of its type. It will apply all other
         * changes.
         *包含从表中删除的基准项目
         * @param deletedData contains Datum items that were deleted from the table
         */
        private void removeDeletedData(Iterable<TokenEditorPanel.Datum> deletedData) {
            PetriNetController petriNetController = pipeApplicationController.getActivePetriNetController();
            PetriNet petriNet = petriNetController.getPetriNet();
            List<UndoableEdit> undoableEdits = new LinkedList<>();
            for (TokenEditorPanel.Datum datum : deletedData) {
                if (tokenEditorPanel.isExistingDatum(datum)) {
                    try {
                        Token token = petriNet.getComponent(datum.id, Token.class);
                        petriNet.removeToken(token);
                        UndoableEdit historyItem = new DeletePetriNetObject(token, petriNet);
                        undoableEdits.add(historyItem);
                    } catch (PetriNetComponentNotFoundException e) {
                    	logger.log(Level.SEVERE, e.getMessage());
                    } catch (PetriNetComponentException e) {
                        StringBuilder messageBuilder = new StringBuilder();
                        messageBuilder.append(e.getMessage());
                        messageBuilder.append("\n");
                        messageBuilder.append("All other changes will be applied but this token will not be deleted!");
                        GuiUtils.displayErrorMessage(null, messageBuilder.toString());
                    }
                }
            }
            if (undoableEdits.size() > 0) {
                registerUndoEvent(new MultipleEdit(undoableEdits));
            }
        }

        /**
         *从表数据项更新Petri net组件
         * Update the Petri net component from the table data item
         * @param data for update 
         */
        private void updateFromTable(Iterable<TokenEditorPanel.Datum> data) {
            PetriNetController petriNetController = pipeApplicationController.getActivePetriNetController();
            List<UndoableEdit> undoableEdits = new LinkedList<>();
            for (TokenEditorPanel.Datum modified : data) {
                if (tokenEditorPanel.isExistingDatum(modified)) {
                    AbstractDatum initial = modified.initial;
                    if (!modified.equals(initial) && modified.hasBeenSet()) {
                        try {
                            Token token = petriNetController.getToken(initial.id);
                            undoableEdits.add(new ChangePetriNetComponentName(token, initial.id, modified.id));
                            undoableEdits.add(new ChangeTokenColor(token, token.getColor(), modified.color));
                            petriNetController.updateToken(initial.id, modified.id, modified.color);
                        } catch (PetriNetComponentNotFoundException petriNetComponentNotFoundException) {
                            GuiUtils.displayErrorMessage(null, petriNetComponentNotFoundException.getMessage());
                        }
                    }
                } else if (modified.hasBeenSet()) {
                    petriNetController.createNewToken(modified.id, modified.color);
                    try {
                        Token token = petriNetController.getToken(modified.id);
                        undoableEdits.add(new AddPetriNetObject(token, petriNetController.getPetriNet()));
                    } catch (PetriNetComponentNotFoundException e) {
                        logger.log(Level.SEVERE, e.getMessage());
                    }

                }
            }
            if (undoableEdits.size() > 0) {
                registerUndoEvent(new MultipleEdit(undoableEdits));
            }
        }


    }
}
