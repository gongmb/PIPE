package pipe.views;

import pipe.actions.gui.PipeApplicationModel;
import pipe.actions.gui.ZoomManager;
import pipe.constants.GUIConstants;
import pipe.controllers.PetriNetController;
import pipe.controllers.SelectionManager;
import pipe.controllers.application.PipeApplicationController;
import pipe.gui.*;
import pipe.handlers.PetriNetMouseHandler;
import pipe.utilities.gui.GuiUtils;
import uk.ac.imperial.pipe.exceptions.PetriNetComponentNotFoundException;
import uk.ac.imperial.pipe.models.manager.PetriNetManagerImpl;
import uk.ac.imperial.pipe.models.petrinet.PetriNet;
import uk.ac.imperial.pipe.models.petrinet.Token;
import uk.ac.imperial.pipe.models.petrinet.name.PetriNetName;

import javax.swing.*;
import javax.swing.border.BevelBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.UndoableEditListener;
import javax.swing.text.BadLocationException;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.net.URL;
import java.util.*;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 * Main class application view
 * 主类应用程序视图
 */
@SuppressWarnings("serial")
public class PipeApplicationView extends JFrame implements ActionListener, Observer {


    /**
     * Class logger
     */
    private static final Logger LOGGER = Logger.getLogger(PipeApplicationView.class.getName());

    /**
     * Status bar for useful messages
     */
    public final StatusBar statusBar;

    /**
     * Zoom managager
     */
    private final ZoomManager zoomManager;

    private final JSplitPane moduleAndAnimationHistoryFrame;

    private final JTabbedPane frameForPetriNetTabs = new JTabbedPane();

    private final List<PetriNetTab> petriNetTabs = new ArrayList<>();

    private final PipeApplicationController applicationController;

    private final PipeApplicationModel applicationModel;

    public JComboBox<String> zoomComboBox;

    public JComboBox<String> tokenClassComboBox;

    private UndoableEditListener undoListener;

    private JScrollPane scroller;

    private List<JLayer<JComponent>> wrappedPetrinetTabs = new ArrayList<>();

    private Map<PetriNetTab, AnimationHistoryView> histories = new HashMap<>();

    /**
     * 主程序视图类 构造方法
     *
     * @param zoomManager           ZoomUI 缩放管理
     * @param applicationController 程序控制器
     * @param applicationModel      程序基本信息对象
     */
    public PipeApplicationView(ZoomManager zoomManager, final PipeApplicationController applicationController,
                               PipeApplicationModel applicationModel) {
        this.zoomManager = zoomManager;
        this.applicationModel = applicationModel;
        this.applicationController = applicationController;
        //PropertyChangeListener 是一个接口    该处使用了匿名内部类
        //PropertyChangeListener 每当bean更改“ bound”属性时，都会触发“ PropertyChange”事件。
        //您可以向源bean注册PropertyChangeListener，以便在任何绑定的属性更新时得到通知。
        applicationController.registerToManager(new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                if (evt.getPropertyName().equals(PetriNetManagerImpl.NEW_PETRI_NET_MESSAGE)) {
                    PetriNet petriNet = (PetriNet) evt.getNewValue();
                    registerNewPetriNet(petriNet);
                } else if (evt.getPropertyName().equals(PetriNetManagerImpl.REMOVE_PETRI_NET_MESSAGE)) {
                    removeCurrentTab();
                }

            }
        });
        applicationModel.addPropertyChangeListener(new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                if (evt.getPropertyName().equals(PipeApplicationModel.TOGGLE_ANIMATION_MODE)) {
                    boolean oldMode = (boolean) evt.getOldValue();
                    boolean newMode = (boolean) evt.getNewValue();
                    if (oldMode != newMode) {
                        setAnimationMode(newMode);
                    }
                } else if (evt.getPropertyName().equals(PipeApplicationModel.TYPE_ACTION_CHANGE_MESSAGE)) {
                    PetriNetTab petriNetTab = getCurrentTab();
                    if (petriNetTab != null) {
                        petriNetTab.setCursorType("crosshair");
                        SelectionManager selectionManager =
                                applicationController.getActivePetriNetController().getSelectionManager();
                        selectionManager.disableSelection();
                    }
                }
            }
        });
        //设置应用程序展示名称,从applicationModel中获取name属性
        setTitle(null);
        try {
            //返回实现本机系统外观的<code> LookAndFeel </ code>类的名称，否则，返回默认跨平台<code> LookAndFeel </ code>的名称。
            //类。 可以通过设置<code> swing.systemlaf </ code>系统属性来覆盖此值。
            String systemLookAndFeelClassName = UIManager.getSystemLookAndFeelClassName();
            //加载给定类指定的{@code LookAndFeel}名称，使用当前线程的上下文类加载器，以及将其传递给{@code setLookAndFeel（LookAndFeel）}。
            UIManager.setLookAndFeel(systemLookAndFeelClassName);
        } catch (IllegalAccessException | ClassNotFoundException | InstantiationException | UnsupportedLookAndFeelException e) {
            LOGGER.log(Level.SEVERE, e.getMessage());
        }
        //获取并设置icon图标
        this.setIconImage(new ImageIcon(getImageURL("icon")).getImage());
        //Java AWT的Toolkit是对系统低层实现图形控件的最基本功能的一些接口。
        //Window.getToolkit获得当前控件的底层控件的基本功能
        //Toolkit.getDefaultToolkit获得默认的底层控件的基本功能
        //WToolkit 对象
        Toolkit defaultToolkit = Toolkit.getDefaultToolkit();
        //获取系统窗口大小
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        //设置软件窗口大小
        this.setSize(screenSize.width * 80 / 100, screenSize.height * 80 / 100);
        //设置其他相关信息  左边距 右边距 上边距 下边距
        this.setLocationRelativeTo(null);

        // Status bar... 状态栏
        statusBar = new StatusBar();
        //BorderLayout.PAGE_END 该组件位于布局内容的最后一行之后。 对于Western，从左到右和从上到下的方向，这等效于SOUTH。
        //添加状态栏
        getContentPane().add(statusBar, BorderLayout.PAGE_END);
        //设置前景颜色
        this.setForeground(java.awt.Color.BLACK);
        //设置背景颜色
        this.setBackground(java.awt.Color.WHITE);
        //模型管理器
        ModuleManager moduleManager = new ModuleManager(this, applicationController);
        //获取模型树
        JTree moduleTree = moduleManager.getModuleTree();
        moduleAndAnimationHistoryFrame = new JSplitPane(JSplitPane.VERTICAL_SPLIT, moduleTree, null);
        moduleAndAnimationHistoryFrame.setContinuousLayout(true);
        moduleAndAnimationHistoryFrame.setDividerSize(0);
        JSplitPane pane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, moduleAndAnimationHistoryFrame, frameForPetriNetTabs);
        pane.setContinuousLayout(true);
        pane.setOneTouchExpandable(true);
        // avoid multiple borders
        pane.setBorder(null);
        pane.setDividerSize(8);
        getContentPane().add(pane);

        setVisible(true);
        applicationModel.setMode(GUIConstants.SELECT);
        //TODO: DO YOU NEED TO DO THIS?
        //        selectAction.actionPerformed(null);

        setTabChangeListener();

        setZoomChangeListener();
    }

    public void setUndoListener(UndoableEditListener listener) {
        undoListener = listener;
    }

    @Override
    public final void setTitle(String title) {
        //获取程序名称
        String name = applicationModel.getName();
        //三元运算符
        super.setTitle((title == null) ? name : name + ": " + title);
    }

    // set tabbed pane properties and add change listener that updates tab with
    // linked model and view
    private void setTabChangeListener() {
        frameForPetriNetTabs.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                PetriNetTab petriNetTab = getCurrentTab();
                applicationController.setActiveTab(petriNetTab);

                if (areAnyTabsDisplayed()) {
                    PetriNetController controller = applicationController.getActivePetriNetController();
                    if (controller.isCopyInProgress()) {
                        controller.cancelPaste();
                    }
                    petriNetTab.setVisible(true);
                    petriNetTab.repaint();
                    updateZoomCombo();
                    setTitle(petriNetTab.getName());
                    applicationModel.setInAnimationMode(controller.isInAnimationMode());
                }
                refreshTokenClassChoices();
            }
        });
    }

    public void setTabChangeListener(ChangeListener listener) {
        frameForPetriNetTabs.addChangeListener(listener);
    }

    public PetriNetTab getCurrentTab() {
        int index = frameForPetriNetTabs.getSelectedIndex();
        return getTab(index);
    }

    PetriNetTab getTab(int index) {
        if (index < 0 || index >= petriNetTabs.size()) {
            return null;
        }
        return petriNetTabs.get(index);
    }

    /**
     * Refreshes the combo box that presents the Tokens available for use.
     * If there are no Petri nets being displayed this clears it
     */
    public void refreshTokenClassChoices() {
        if (areAnyTabsDisplayed()) {
            String[] tokenClassChoices = buildTokenClassChoices();
            ComboBoxModel<String> model = new DefaultComboBoxModel<>(tokenClassChoices);
            tokenClassComboBox.setModel(model);

            if (tokenClassChoices.length > 0) {
                try {
                    PetriNetController controller = applicationController.getActivePetriNetController();
                    controller.selectToken(getSelectedTokenName());
                } catch (PetriNetComponentNotFoundException petriNetComponentNotFoundException) {
                    GuiUtils.displayErrorMessage(this, petriNetComponentNotFoundException.getMessage());
                }
            }
        } else {
            tokenClassComboBox.setModel(new DefaultComboBoxModel<String>());
        }
    }

    public String getSelectedTokenName() {
        ComboBoxModel<String> model = tokenClassComboBox.getModel();
        Object selected = model.getSelectedItem();
        return selected.toString();
    }

    /**
     * @return names of Tokens for the combo box
     */
    protected String[] buildTokenClassChoices() {
        if (areAnyTabsDisplayed()) {
            PetriNetController petriNetController = applicationController.getActivePetriNetController();
            Collection<Token> tokens = petriNetController.getNetTokens();
            String[] tokenClassChoices = new String[tokens.size()];
            int index = 0;
            for (Token token : tokens) {
                tokenClassChoices[index] = token.getId();
                index++;
            }
            return tokenClassChoices;
        }
        return new String[0];
    }

    /**
     * @return true if any tabs are displayed
     */
    public boolean areAnyTabsDisplayed() {
        return applicationController.getActivePetriNetController() != null;
    }

    /**
     * Remove the listener from the zoomComboBox, so that when
     * the box's selected item is updated to keep track of ZoomActions
     * called from other sources, a duplicate ZoomAction is not called
     */
    public void updateZoomCombo() {
        ActionListener zoomComboListener = zoomComboBox.getActionListeners()[0];
        zoomComboBox.removeActionListener(zoomComboListener);

        String zoomPercentage = zoomManager.getPercentageZoom() + "%";
        zoomComboBox.setSelectedItem(zoomPercentage);
        zoomComboBox.addActionListener(zoomComboListener);
    }

    public void setAnimationMode(boolean animateMode) {
        if (animateMode) {
            statusBar.changeText(statusBar.TEXT_FOR_ANIMATION);
            createAnimationViewPane();

        } else {
            statusBar.changeText(statusBar.TEXT_FOR_DRAWING);
            removeAnimationViewPlane();
        }
    }

    void removeAnimationViewPlane() {
        if (scroller != null) {
            moduleAndAnimationHistoryFrame.remove(scroller);
            moduleAndAnimationHistoryFrame.setDividerLocation(0);
            moduleAndAnimationHistoryFrame.setDividerSize(0);
        }
    }

    /**
     * Creates a new currentAnimationView text area, and returns a reference to it
     */
    private void createAnimationViewPane() {
        AnimationHistoryView animationHistoryView = histories.get(getCurrentTab());
        scroller = new JScrollPane(animationHistoryView);
        scroller.setBorder(new EmptyBorder(0, 0, 0, 0));

        moduleAndAnimationHistoryFrame.setBottomComponent(scroller);

        moduleAndAnimationHistoryFrame.setDividerLocation(0.5);
        moduleAndAnimationHistoryFrame.setDividerSize(8);
    }

    public void setToolBar(JToolBar toolBar) {
        getContentPane().add(toolBar, BorderLayout.PAGE_START);
    }

    /**
     * Creates and adds the token view combo box to the view
     *
     * @param toolBar the JToolBar to add the combo box to
     * @param action  the action that the tokenClassComboBox performs when selected
     */
    protected void addTokenClassComboBox(JToolBar toolBar, Action action) {
        String[] tokenClassChoices = new String[]{"Default"};
        DefaultComboBoxModel<String> model = new DefaultComboBoxModel<>(tokenClassChoices);
        tokenClassComboBox = new JComboBox<>(model);
        tokenClassComboBox.setEditable(true);
        tokenClassComboBox.setSelectedItem(tokenClassChoices[0]);
        tokenClassComboBox.setMaximumRowCount(100);
        //        tokenClassComboBox.setMaximumSize(new Dimension(125, 100));
        tokenClassComboBox.setEditable(false);
        tokenClassComboBox.setAction(action);
        toolBar.add(tokenClassComboBox);
    }

    /**
     * Sets pipes menu
     *
     * @param menu for PIPE
     */
    public void setMenu(JMenuBar menu) {
        setJMenuBar(menu);
    }

    private void setZoomChangeListener() {
        zoomManager.addPropertyChangeListener(new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                getTabComponent().repaint();
                updateZoomCombo();
            }
        });
    }

    private JComponent getTabComponent() {
        return wrappedPetrinetTabs.get(frameForPetriNetTabs.getSelectedIndex());
    }

    /**
     * Sets the default behaviour for exit for both Windows/Linux/Mac OS X
     *
     * @param adapter for exit action
     */
    public void setExitAction(WindowListener adapter) {
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(adapter);
    }

    /**
     * Displays contributors
     */
    @Override
    public void actionPerformed(ActionEvent e) {

        JOptionPane.showMessageDialog(this, "PIPE: Platform Independent Petri Net Ediror\n\n" + "Authors:\n" +
                        "2003: Jamie Bloom, Clare Clark, Camilla Clifford, Alex Duncan, Haroun Khan and Manos Papantoniou\n"
                        +
                        "2004: Tom Barnwell, Michael Camacho, Matthew Cook, Maxim Gready, Peter Kyme and Michail Tsouchlaris\n"
                        +
                        "2005: Nadeem Akharware\n" + "????: Tim Kimber, Ben Kirby, Thomas Master, Matthew Worthington\n"
                        +
                        "????: Pere Bonet Bonet (Universitat de les Illes Balears)\n" +
                        "????: Marc Meli\u00E0 Aguil\u00F3 (Universitat de les Illes Balears)\n" +
                        "2010: Alex Charalambous (Imperial College London)\n" +
                        "2011: Jan Vlasak (Imperial College London)\n\n" + "http://pipe2.sourceforge.net/",
                "About PIPE", JOptionPane.INFORMATION_MESSAGE
        );
    }

    //TODO: Find out if this actually ever gets called
    @Override
    public void update(Observable o, Object obj) {
    }

    /**
     * Adds the tab to the main application view in the tabbed view frame
     *
     * @param name name of tab
     * @param tab  tab to add
     */
    //TODO: ADD ZOOMING
    public void addNewTab(String name, PetriNetTab tab) {

        JScrollPane tabScroller = new JScrollPane(tab, ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
        tabScroller.setBorder(new BevelBorder(BevelBorder.LOWERED));

        //        JLayer<JComponent> jLayer = new JLayer<>(tab, zoomUI);
        //        wrappedPetrinetTabs.add(jLayer);

        petriNetTabs.add(tab);
        frameForPetriNetTabs.addTab(name, tabScroller);
        frameForPetriNetTabs.setSelectedIndex(petriNetTabs.size() - 1);
    }

    public File getFile() {
        PetriNetTab petriNetTab = petriNetTabs.get(frameForPetriNetTabs.getSelectedIndex());
        return petriNetTab.appFile;
    }

    public void removeCurrentTab() {
        removeTab(frameForPetriNetTabs.getSelectedIndex());
    }

    public void removeTab(int index) {
        if (frameForPetriNetTabs.getTabCount() > 0) {
            petriNetTabs.remove(index);
            if (index > 0) {
                applicationController.setActiveTab(petriNetTabs.get(index - 1));
            } else {
                applicationController.setActiveTab(null);
            }
            frameForPetriNetTabs.remove(index);
        }
    }

    public void updateSelectedTabName(String title) {
        int index = frameForPetriNetTabs.getSelectedIndex();
        frameForPetriNetTabs.setTitleAt(index, title);
    }

    public void registerNewPetriNet(PetriNet petriNet) {


        petriNet.addPropertyChangeListener(new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                String msg = evt.getPropertyName();
                if (msg.equals(PetriNet.PETRI_NET_NAME_CHANGE_MESSAGE)) {
                    PetriNetName name = (PetriNetName) evt.getNewValue();
                    updateSelectedTabName(name.getName());
                } else if (msg.equals(PetriNet.NEW_TOKEN_CHANGE_MESSAGE) || msg.equals(
                        PetriNet.DELETE_TOKEN_CHANGE_MESSAGE)) {
                    refreshTokenClassChoices();
                }
            }
        });

        AnimationHistoryView animationHistoryView;
        try {
            animationHistoryView = new AnimationHistoryView("Animation History");
        } catch (BadLocationException e) {
            throw new RuntimeException(e);
        }
        PetriNetTab petriNetTab = new PetriNetTab();
        histories.put(petriNetTab, animationHistoryView);

        PropertyChangeListener zoomListener = new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent propertyChangeEvent) {
                updateZoomCombo();
            }
        };
        applicationController.registerTab(petriNet, petriNetTab, animationHistoryView, undoListener, zoomListener);
        PetriNetController petriNetController = applicationController.getActivePetriNetController();
        petriNetTab.setMouseHandler(
                new PetriNetMouseHandler(applicationModel, petriNetController, petriNetTab));
        petriNetTab.updatePreferredSize();

        addNewTab(petriNet.getNameValue(), petriNetTab);
    }

    /**
     * 根据图片名称 获取图片本地地址
     *
     * @param name
     * @return
     */
    private URL getImageURL(String name) {
        PipeResourceLocator locator = new PipeResourceLocator();
        return locator.getImage(name);
    }

    public void register(JComboBox<String> tokenClassComboBox) {
        this.tokenClassComboBox = tokenClassComboBox;
    }

    public void registerZoom(JComboBox<String> zoomComboBox) {
        this.zoomComboBox = zoomComboBox;
    }
}

