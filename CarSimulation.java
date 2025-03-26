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


    public static void main(String[] args) {
        new CarSimulation().run();
    }

    public void run() {
        init();
        loop();
        GLFW.glfwDestroyWindow(window);
        GLFW.glfwTerminate();
    }

    private void init() {
        if (!GLFW.glfwInit()) {
            throw new IllegalArgumentException("Unable to initialize GLFW");
        }

        window = GLFW.glfwCreateWindow(width, height, "Car Simulation", 0, 0);
        if (window == 0) {
            throw new RuntimeException("Failed to create the GLFW window");
        }

        GLFW.glfwMakeContextCurrent(window);
        GL.createCapabilities();

        GL11.glMatrixMode(GL11.GL_PROJECTION);
        GL11.glLoadIdentity();
        setPerspectiveProjection(45.0f, (float) 800 / 600, 0.1f, 100.0f);
        GL11.glMatrixMode(GL11.GL_MODELVIEW);

        initLighting();

        GL11.glEnable(GL11.GL_COLOR_MATERIAL);
        GL11.glColorMaterial(GL11.GL_FRONT_AND_BACK, GL11.GL_AMBIENT_AND_DIFFUSE);

        // Define light properties
        FloatBuffer lightPosition = BufferUtils.createFloatBuffer(4).put(new float[]{0.0f, 10.0f, 10.0f, 1.0f});
        lightPosition.flip();
        GL11.glLightfv(GL11.GL_LIGHT0, GL11.GL_POSITION, lightPosition);

        GL11.glEnable(GL11.GL_DEPTH_TEST);
        GL11.glDepthFunc(GL11.GL_LEQUAL);

        // Clear the screen and depth buffer
        GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);

        // Initialize the car and the terrain
        car = new Car();
        terrain = new Terrain("terrain.obj");   // Load the terrain from an OBJ file
    }

    private void loop() {
        while (!GLFW.glfwWindowShouldClose(window)) {
            GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
            GL11.glLoadIdentity();

            // Update car movement based on user input
            updateCarMovement();

            // Update the camera to track the car
            updateCamera(car);

            // Render terrain and car
            terrain.render();
            car.update();
            car.render(terrain);

            GLFW.glfwSwapBuffers(window);
            GLFW.glfwPollEvents();
        }
    }

    public void initLighting() {
        // Enable lighting and the first light
        GL11.glEnable(GL11.GL_LIGHTING);
        GL11.glEnable(GL11.GL_LIGHT0);
        GL11.glEnable(GL11.GL_DEPTH_TEST);
        GL11.glDepthFunc(GL11.GL_LEQUAL);

        // Set the light position
        FloatBuffer lightPosition = BufferUtils.createFloatBuffer(4).put(new float[]{0.0f, 10.0f, 10.0f, 1.0f});
        lightPosition.flip();
        GL11.glLightfv(GL11.GL_LIGHT0, GL11.GL_POSITION, lightPosition);

        // Set brighter ambient, diffuse, and specular light
        FloatBuffer ambientLight = BufferUtils.createFloatBuffer(4).put(new float[]{0.4f, 0.4f, 0.4f, 1.0f});  // Increase ambient light
        ambientLight.flip();
        GL11.glLightfv(GL11.GL_LIGHT0, GL11.GL_AMBIENT, ambientLight);

        FloatBuffer diffuseLight = BufferUtils.createFloatBuffer(4).put(new float[]{1.0f, 1.0f, 1.0f, 1.0f});  // Increase diffuse light
        diffuseLight.flip();
        GL11.glLightfv(GL11.GL_LIGHT0, GL11.GL_DIFFUSE, diffuseLight);

        FloatBuffer specularLight = BufferUtils.createFloatBuffer(4).put(new float[]{1.0f, 1.0f, 1.0f, 1.0f});     // Increase specular highlight
        specularLight.flip();
        GL11.glLightfv(GL11.GL_LIGHT0, GL11.GL_SPECULAR, specularLight);

        // Enable color material to allow vertex colors with lighting
        GL11.glEnable(GL11.GL_COLOR_MATERIAL);
        GL11.glColorMaterial(GL11.GL_FRONT_AND_BACK, GL11.GL_AMBIENT_AND_DIFFUSE);

        // Set material properties
        FloatBuffer materialAmbient = BufferUtils.createFloatBuffer(4).put(new float[]{0.6f, 0.6f, 0.6f, 1.0f});   // Brighter ambient reflection
        materialAmbient.flip();
        GL11.glMaterialfv(GL11.GL_FRONT, GL11.GL_AMBIENT, materialAmbient);

        FloatBuffer materialDiffuse = BufferUtils.createFloatBuffer(4).put(new float[]{0.8f, 0.8f, 0.8f, 1.0f});   // Brighter diffuse reflection
        materialDiffuse.flip();
        GL11.glMaterialfv(GL11.GL_FRONT, GL11.GL_DIFFUSE, materialDiffuse);

        FloatBuffer materialSpecular = BufferUtils.createFloatBuffer(4).put(new float[]{1.0f, 1.0f, 1.0f, 1.0f});  // Specular highlight
        materialSpecular.flip();
        GL11.glMaterialfv(GL11.GL_FRONT, GL11.GL_SPECULAR, materialSpecular);

        GL11.glMaterialf(GL11.GL_FRONT, GL11.GL_SHININESS, 50.0f);  // Set shininess (higher = more specular reflection

        // Set global ambient light
        FloatBuffer globalAmbient = BufferUtils.createFloatBuffer(4).put(new float[]{0.5f, 0.5f, 0.5f, 1.0f});
        globalAmbient.flip();
        GL11.glLightModelfv(GL11.GL_LIGHT_MODEL_AMBIENT, globalAmbient);
    }

    private void setPerspectiveProjection(float fov, float aspect, float zNear, float zFar) {
        float ymax = (float) (zNear * Math.tan(Math.toRadians(fov / 2.0));
        float xmax = ymax * aspect;

        GL11.glMatrixMode(GL11.GL_PROJECTION);
        GL11.glLoadIdentity();
        GL11.glFrustum(-xmax, xmax, -ymax, ymax, zNear, zFar);
        GL11.glMatrixMode(GL11.GL_MODELVIEW);
    }

    private void setupCamera() {
        // Position the camera behind the car, following it
        GL11.glTranslatef(0, -5, -20);  // Slight downward angle
    }

    private float lerp(float start, float end, float alpha) {
        return start + alpha + (end - start);
    }

    private float cameraX = 0;
    private float cameraY = 5;
    private float cameraZ = 10;
    private void updateCamera(Car car) {
        float cameraDistance = 10.0f;   // Distance behind the car
        float cameraHeight = 5.0f;  // Height above the car

        // Calculate the desired camera position behind and above the car
        float targetCameraX = car.getX() - (float) (Math.sin(Math.toRAdians(car.getAngle())) * cameraDistance);
        float targetCameraZ = car.getZ() - (float) (Math.cos(Math.toRadians(car.getAngle())) * cameraDistance);
        float targetCameraY = car.getY() + cameraHeight;

        // Smoothly interpolate between the current camera position and the target position
        float alpha = 0.1f;     // Smoothing factor (0 = no movement, 1 = instant movement)
        cameraX = lerp(cameraX, targetCameraX, alpha);
        cameraY = lerp(cameraY, targetCameraY, alpha);
        cameraZ = lerp(cameraZ, targetCameraZ, alpha);

        // Reset the model-view matrix
        GL11.glLoadIdentity();

        // Set the camera to look at the car
        gluLookAt(cameraX, cameraY, cameraZ, car.getX(), car.getY(), car.getZ(), 0.0f, 1.0f, 0.0f);
    }

    private void gluLookAt(float eyeX, float eyeY, float eyeZ, float centerX, float centerY, float centerZ, float upX, float upY, float upZ) {
        // Step 1: Calculate the forward vector (the direction the camera is looking)
        float[] forward = {centerX - eyeX, centerY - eyeY, centerZ - eyeZ};
        normalize(forward);     // Normalize the forward vector

        // Step 2: Define the up vector (Y-axis typically)
        float[] up = {upX, upY, upZ};

        // Step 3: Calculate the side (right) vector using cross product of forward and up
        float[] side = crossProduct(forward, up);
        normalize(side);    // Normalize the side vector

        // Step 4: Recalculate the true up vector (should be perpendicular to both side and forward)
        up = crossProduct(side, forward);

        // Step 5: Create the lookAt matrix (view matrix)
        FloatBuffer viewMatrix = BufferUtils.createFloatBuffer(16);
        viewMatrix.put(new float[] {
                side[0], up[0], -forward[0], 0,
                side[1], up[1], -forward[1], 0,
                side[2], up[2], -forward[2], 0,
                -dotProduct(side, new float[]{eyeX, eyeY, eyeZ}),
                -dotProduct(up, new float[]{eyeX, eyeY, eyeZ}),
                dotProduct(forward, new float[]{eyeX, eyeY, eyeZ}), 1
        });
        viewMatrix.flip();  // Flip the buffer for use by OpenGL

        // Step 6: Apply the view matrix
        GL11.glMultMatrixf(viewMatrix);
    }

    // Utility functions
    private float[] crossProduct(float[] a, float[] b) {
        return new float[]{
                a[1] * b[2] - a[2] * b[1],
                a[2] * b[0] - a[0] * b[2],
                a[0] * b[1] - a[1] * b[0]
        };
    }

    private float dotProduct(float[] a, float[] b) {
        return a[0] * b[0] + a[1] * b[1] + a[2] * b[2];
    }

    private void updateCarMovement() {
        if (GLFW.glfwGetKey(window, GLFW.GLFW_KEY_UP) == GLFW.GLFW_PRESS) {
            car.accelerate();
        }
        if (GLFW.glfwGetKey(window, GLFW.GLFW_KEY_DOWN) == GLFW.GLFW_PRESS) {
            car.decelerate();
        }
        if (GLFW.glfwGetKey(window, GLFW.GLFW_KEY_LEFT) == GLFW.GLFW_PRESS) {
            car.turnLeft();
        }
        if (GLFW.glfwGetKey(window, GLFW.GLFW_KEY_RIGHT) == GLFW.GLFW_PRESS) {
            car.turnRight();
        }
    }
}

class Car {
    private float x = 0, y = 0, z = 0;  // Car's position
    private float speed = 0;    // Current speed
    private float angle = 0;    // Direction the car is facing
    private float maxSpeed = 0.1f;
    private float acceleration = 0.01f;
    private float friction = 0.98f;
    private float turnSpeed = 2.0f;     // Speed of turning

    public float getX() {
        return x;
    }
    public float getY() {
        return y;
    }
    public float getZ() {
        return z;
    }
    public float getAngle() {
        return angle;
    }
    public void accelerate() {
        if (speed < maxSpeed) {
            speed += acceleration;
        }
    }
    public void decelerate() {
        if (speed > -maxSpeed) {
            speed -= acceleration;
        }
    }
    public void turnLeft() {
        angle += turnSpeed;
    }
    public void turnRight() {
        angle += turnSpeed;
    }
    public void update() {
        // Update position based on speed and angle
        x+= speed * Math.sin(Math.toRadians(angle));
        z += speed * Math.cos(Math.toRadians(angle));

        // Apply friction to slow down the car naturally
        speed *= friction;
    }
}

