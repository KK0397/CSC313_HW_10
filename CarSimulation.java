// javac -classpath ".;C:\Program Files\lwjgl-release-3.3.4-custom\*" CarSimulation.java
// java -classpath ".;C:\Program Files\lwjgl-release-3.3.4-custom\*" CarSimulation

import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;

import java.nio.FloatBuffer;
import org.lwjgl.BufferUtils;
public class CarSimulation {
    private long window;
    private int width = 800;
    private int height = 600;
    private Car car;
    private Terrain terrain;
}

public static void main(String[] args) {
    new CarSimulation().run();
}

public void run() {
    init();
    loop();
    GLFW.glfwDestroyWindow(window);
    GLFW.glfwTerminate();
}
