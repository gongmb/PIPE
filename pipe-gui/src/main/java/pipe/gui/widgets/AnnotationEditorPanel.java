package pipe.gui.widgets;

import pipe.controllers.AnnotationController;

import javax.swing.*;

/**
 *用于更改注释文本的注释面板
 * Annotation panel used to change text of the annotation
 */
@SuppressWarnings("serial")
public class AnnotationEditorPanel extends javax.swing.JPanel {

    /**
     * 注释控制器
     * Annotation controller
     */
    private final AnnotationController annotationController;

    /**
     * 新文字区
     * New text area
     */
    private javax.swing.JTextArea textArea;

    /**
     * 创建新表单参数面板
     * Creates new form ParameterPanel
     * @param annotationController controller for annotations 
     */
    public AnnotationEditorPanel(AnnotationController annotationController) {
        this.annotationController = annotationController;
        initComponents();
        textArea.setText(annotationController.getText());
    }

    /**
     * 从构造函数内部调用此方法以初始化表单。
     * This method is called from within the constructor to
     * initialize the form.
     */
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        JPanel panel = new JPanel();
        JScrollPane jScrollPane1 = new JScrollPane();
        textArea = new javax.swing.JTextArea();
        JPanel buttonPanel = new JPanel();
        JButton okButton = new JButton();
        JButton cancelButton = new JButton();

        setLayout(new java.awt.GridBagLayout());

        setMaximumSize(new java.awt.Dimension(239, 208));
        setMinimumSize(new java.awt.Dimension(239, 208));
        panel.setLayout(new java.awt.GridLayout(1, 0));

        panel.setBorder(javax.swing.BorderFactory.createTitledBorder("Edit Annotation编辑注释"));
        textArea.setColumns(20);
        textArea.setRows(5);
        jScrollPane1.setViewportView(textArea);

        panel.add(jScrollPane1);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
        add(panel, gridBagConstraints);

        buttonPanel.setLayout(new java.awt.GridBagLayout());

        okButton.setText("OK好嘞");
        okButton.setMaximumSize(new java.awt.Dimension(75, 25));
        okButton.setMinimumSize(new java.awt.Dimension(75, 25));
        okButton.setPreferredSize(new java.awt.Dimension(75, 25));
        okButton.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                okButtonActionPerformed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 6;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(6, 6, 6, 6);
        buttonPanel.add(okButton, gridBagConstraints);

        cancelButton.setText("Cancel取消");
        cancelButton.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cancelButtonActionPerformed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 6);
        buttonPanel.add(cancelButton, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        add(buttonPanel, gridBagConstraints);

    }

    /**
     * 设置注释的文本
     * Sets the text of the annotation
     * @param evt text event 
     */
    private void okButtonActionPerformed(java.awt.event.ActionEvent evt) {
        annotationController.setText(textArea.getText());
        exit();
    }

    /**
     *退出注释编辑器
     * Exits the annotation editor
     */
    private void exit() {
        getRootPane().getParent().setVisible(false);
    }

    /**
     * 取消注释编辑器，使文本保持不变
     * Cancels the annotation editor, leaving the text unchanged
     * @param evt cancel event 
     */
    private void cancelButtonActionPerformed(
            java.awt.event.ActionEvent evt) {
        exit();
    }
}
