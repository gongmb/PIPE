package pipe.gui.plugin;

import uk.ac.imperial.pipe.models.petrinet.PetriNet;

/**
 * GUI模块的API
 * API for GUI modules
 */
public interface GuiModule {

    /**
     * 使用当前的Petri网启动模块
     * Start a module using optionally the current Petri net
     * @param petriNet to start
     */
    void start(PetriNet petriNet);

    /**
     *模块的名称
     * @return the name of the module for
     */
    String getName();
}
