package pipe.gui.plugin.concrete;

import pipe.gui.reachability.ReachabilityGraph;
import pipe.gui.plugin.GuiModule;
import uk.ac.imperial.pipe.models.petrinet.PetriNet;

import javax.swing.*;
import java.awt.FileDialog;

/**
 * 动态加载到GUI中的状态空间模块
 * State Space module that is dynamically loaded into the GUI
 */
public class StateSpaceModule implements GuiModule {
    @Override
    public void start(PetriNet petriNet) {
        JFrame frame = new JFrame("State Space Explorer");
        //  FileDialog 显示一个对话框，用户可以选择一个文件。因为它是一个模态对话框，
        // 当应用程序调用它的show方法显示的对话框，它会阻止其他应用程序在用户选择一个文件。
        FileDialog selector = new FileDialog(frame, "Select petri net", FileDialog.LOAD);
        FileDialog saver = new FileDialog(frame, "Save binary transition data", FileDialog.SAVE);
        frame.setContentPane(new ReachabilityGraph(selector, petriNet).getMainPanel());
        frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
    }

    @Override
    public String getName() {
        return "State space exploration";
    }
}
