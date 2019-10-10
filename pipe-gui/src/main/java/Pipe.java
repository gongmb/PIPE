import pipe.actions.gui.PipeApplicationModel;
import pipe.controllers.application.PipeApplicationController;
import pipe.views.PipeApplicationBuilder;
import pipe.views.PipeApplicationView;

import javax.swing.*;
import java.lang.reflect.InvocationTargetException;

public final class Pipe {

    protected static PipeApplicationView applicationView;

    private Pipe(String version) {
        //应用程序基本信息对象 applicationModel  应用名称等
        PipeApplicationModel applicationModel = new PipeApplicationModel(version);
        //应用程序控制器
        PipeApplicationController applicationController = new PipeApplicationController(applicationModel);
        //主程序建造者
        PipeApplicationBuilder builder = new PipeApplicationBuilder();
        applicationView = builder.build(applicationController, applicationModel);
        applicationController.createEmptyPetriNet();
        System.out.println("该输出为了测试invokeAndWait和invokeLater两个方法" + 222);
    }

    public static void main(String[] args) throws Exception {
        Runnable runnable = pipeRunnable();
        //Swing 是一个用于 Java GUI 编程（图形界面设计）的工具包（类库）；换句话说，Java 可以用来开发带界面的 PC 软件，使用到的工具就是 Swing。
        //invokeAndWait：后面的程序必须等这个线程（参数中的线程）的东西执行完才能执行
        //invokeLater：后面的程序和这个参数的线程对象可以并行，异步地执行
        SwingUtilities.invokeLater(runnable);
        System.out.println("该输出为了测试invokeAndWait和invokeLater两个方法" + 111);
        //测试invokeAndWait和invokeLater  111 先输出说明SwingUtilities.invokeLater(runnable) 异步执行
        //222 先输出说明 需要等待 SwingUtilities.invokeLater(runnable) 执行完 才往下执行
    }

    /**
     * 创建一个线程
     *
     * @return
     */
    protected static Runnable pipeRunnable() {
        //基于runnable接口的方式实现多线程
        return new Runnable() {
            @Override
            public void run() {
                new Pipe("v5.0.2");
            }
        };
    }

    protected static void runPipeForTesting() throws InterruptedException, InvocationTargetException {
        SwingUtilities.invokeAndWait(pipeRunnable());
    }
}
