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

import java.nio.FloatBuffer;
import org.lwjgl.BufferUtils;

public class CarSimulation {
    private long window;
    private int width = 800;
    private int height = 600;
    private List<Car> cars = new ArrayList<>();
    private int currentCarIndex = 0;
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
        setPerspectiveProjection(45.0f, (float) width / height, 0.1f, 100.0f);
        GL11.glMatrixMode(GL11.GL_MODELVIEW);

        initLighting();

        GL11.glEnable(GL11.GL_COLOR_MATERIAL);
        GL11.glColorMaterial(GL11.GL_FRONT_AND_BACK, GL11.GL_AMBIENT_AND_DIFFUSE);

        FloatBuffer lightPosition = BufferUtils.createFloatBuffer(4).put(new float[]{0.0f, 10.0f, 10.0f, 1.0f});
        lightPosition.flip();
        GL11.glLightfv(GL11.GL_LIGHT0, GL11.GL_POSITION, lightPosition);

        GL11.glEnable(GL11.GL_DEPTH_TEST);
        GL11.glDepthFunc(GL11.GL_LEQUAL);

        GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);

        // Initialize two cars with different colors and positions
        cars.add(new Car(0, 0, 0, 1.0f, 0.2f, 0.2f)); // Red car
        cars.add(new Car(5, 0, 5, 0.2f, 0.2f, 1.0f)); // Blue car
        terrain = new Terrain("terrain2.obj");
    }

    private void loop() {
        while (!GLFW.glfwWindowShouldClose(window)) {
            GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
            GL11.glLoadIdentity();

            updateCarMovement();

            // Switch between cars
            if (GLFW.glfwGetKey(window, GLFW.GLFW_KEY_1) == GLFW.GLFW_PRESS) {
                currentCarIndex = 0;
            }
            if (GLFW.glfwGetKey(window, GLFW.GLFW_KEY_2) == GLFW.GLFW_PRESS) {
                currentCarIndex = 1;
            }

            updateCamera(cars.get(currentCarIndex));

            terrain.render();
            for (Car car : cars) {
                car.update();
                car.render(terrain);
            }

            GLFW.glfwSwapBuffers(window);
            GLFW.glfwPollEvents();
        }
    }

    private void updateCarMovement() {
        Car currentCar = cars.get(currentCarIndex);

        if (GLFW.glfwGetKey(window, GLFW.GLFW_KEY_UP) == GLFW.GLFW_PRESS) {
            currentCar.accelerate();
        }
        if (GLFW.glfwGetKey(window, GLFW.GLFW_KEY_DOWN) == GLFW.GLFW_PRESS) {
            currentCar.decelerate();
        }
        if (GLFW.glfwGetKey(window, GLFW.GLFW_KEY_LEFT) == GLFW.GLFW_PRESS) {
            currentCar.turnLeft();
        }
        if (GLFW.glfwGetKey(window, GLFW.GLFW_KEY_RIGHT) == GLFW.GLFW_PRESS) {
            currentCar.turnRight();
        }
    }

    public void initLighting() {
        GL11.glEnable(GL11.GL_LIGHTING);
        GL11.glEnable(GL11.GL_LIGHT0);
        GL11.glEnable(GL11.GL_DEPTH_TEST);
        GL11.glDepthFunc(GL11.GL_LEQUAL);

        FloatBuffer lightPosition = BufferUtils.createFloatBuffer(4).put(new float[]{0.0f, 10.0f, 10.0f, 1.0f});
        lightPosition.flip();
        GL11.glLightfv(GL11.GL_LIGHT0, GL11.GL_POSITION, lightPosition);

        FloatBuffer ambientLight = BufferUtils.createFloatBuffer(4).put(new float[]{0.4f, 0.4f, 0.4f, 1.0f});
        ambientLight.flip();
        GL11.glLightfv(GL11.GL_LIGHT0, GL11.GL_AMBIENT, ambientLight);

        FloatBuffer diffuseLight = BufferUtils.createFloatBuffer(4).put(new float[]{1.0f, 1.0f, 1.0f, 1.0f});
        diffuseLight.flip();
        GL11.glLightfv(GL11.GL_LIGHT0, GL11.GL_DIFFUSE, diffuseLight);

        FloatBuffer specularLight = BufferUtils.createFloatBuffer(4).put(new float[]{1.0f, 1.0f, 1.0f, 1.0f});
        specularLight.flip();
        GL11.glLightfv(GL11.GL_LIGHT0, GL11.GL_SPECULAR, specularLight);

        GL11.glEnable(GL11.GL_COLOR_MATERIAL);
        GL11.glColorMaterial(GL11.GL_FRONT_AND_BACK, GL11.GL_AMBIENT_AND_DIFFUSE);

        FloatBuffer materialAmbient = BufferUtils.createFloatBuffer(4).put(new float[]{0.6f, 0.6f, 0.6f, 1.0f});
        materialAmbient.flip();
        GL11.glMaterialfv(GL11.GL_FRONT, GL11.GL_AMBIENT, materialAmbient);

        FloatBuffer materialDiffuse = BufferUtils.createFloatBuffer(4).put(new float[]{0.8f, 0.8f, 0.8f, 1.0f});
        materialDiffuse.flip();
        GL11.glMaterialfv(GL11.GL_FRONT, GL11.GL_DIFFUSE, materialDiffuse);

        FloatBuffer materialSpecular = BufferUtils.createFloatBuffer(4).put(new float[]{1.0f, 1.0f, 1.0f, 1.0f});
        materialSpecular.flip();
        GL11.glMaterialfv(GL11.GL_FRONT, GL11.GL_SPECULAR, materialSpecular);

        GL11.glMaterialf(GL11.GL_FRONT, GL11.GL_SHININESS, 50.0f);

        FloatBuffer globalAmbient = BufferUtils.createFloatBuffer(4).put(new float[]{0.5f, 0.5f, 0.5f, 1.0f});
        globalAmbient.flip();
        GL11.glLightModelfv(GL11.GL_LIGHT_MODEL_AMBIENT, globalAmbient);
    }

    private void setPerspectiveProjection(float fov, float aspect, float zNear, float zFar) {
        float ymax = (float) (zNear * Math.tan(Math.toRadians(fov / 2.0)));
        float xmax = ymax * aspect;

        GL11.glMatrixMode(GL11.GL_PROJECTION);
        GL11.glLoadIdentity();
        GL11.glFrustum(-xmax, xmax, -ymax, ymax, zNear, zFar);
        GL11.glMatrixMode(GL11.GL_MODELVIEW);
    }

    private float lerp(float start, float end, float alpha) {
        return start + alpha * (end - start);
    }

    private float cameraX = 0;
    private float cameraY = 5;
    private float cameraZ = 10;
    private void updateCamera(Car car) {
        float cameraDistance = 10.0f;
        float cameraHeight = 5.0f;

        float targetCameraX = car.getX() - (float) (Math.sin(Math.toRadians(car.getAngle())) * cameraDistance);
        float targetCameraZ = car.getZ() - (float) (Math.cos(Math.toRadians(car.getAngle())) * cameraDistance);
        float targetCameraY = car.getY() + cameraHeight;

        float alpha = 0.1f;
        cameraX = lerp(cameraX, targetCameraX, alpha);
        cameraY = lerp(cameraY, targetCameraY, alpha);
        cameraZ = lerp(cameraZ, targetCameraZ, alpha);

        GL11.glLoadIdentity();
        gluLookAt(cameraX, cameraY, cameraZ, car.getX(), car.getY(), car.getZ(), 0.0f, 1.0f, 0.0f);
    }

    private void gluLookAt(float eyeX, float eyeY, float eyeZ, float centerX, float centerY, float centerZ, float upX, float upY, float upZ) {
        float[] forward = {centerX - eyeX, centerY - eyeY, centerZ - eyeZ};
        normalize(forward);

        float[] up = {upX, upY, upZ};
        float[] side = crossProduct(forward, up);
        normalize(side);

        up = crossProduct(side, forward);

        FloatBuffer viewMatrix = BufferUtils.createFloatBuffer(16);
        viewMatrix.put(new float[] {
                side[0], up[0], -forward[0], 0,
                side[1], up[1], -forward[1], 0,
                side[2], up[2], -forward[2], 0,
                -dotProduct(side, new float[]{eyeX, eyeY, eyeZ}),
                -dotProduct(up, new float[]{eyeX, eyeY, eyeZ}),
                dotProduct(forward, new float[]{eyeX, eyeY, eyeZ}),
                1
        });
        viewMatrix.flip();
        GL11.glMultMatrixf(viewMatrix);
    }

    private void normalize(float[] v) {
        float length = (float) Math.sqrt(v[0] * v[0] + v[1] * v[1] + v[2] * v[2]);
        if (length != 0) {
            v[0] /= length;
            v[1] /= length;
            v[2] /= length;
        }
    }

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
}

class Car {
    private float x, y, z;
    private float speed = 0;
    private float angle = 0;
    private float maxSpeed = 0.1f;
    private float acceleration = 0.01f;
    private float friction = 0.98f;
    private float turnSpeed = 2.0f;
    private float[] color;

    public Car(float x, float y, float z, float r, float g, float b) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.color = new float[]{r, g, b};
    }

    public float getX() { return x; }
    public float getY() { return y; }
    public float getZ() { return z; }
    public float getAngle() { return angle; }

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
        angle -= turnSpeed;
    }

    public void turnRight() {
        angle += turnSpeed;
    }

    public void update() {
        x += speed * Math.sin(Math.toRadians(angle));
        z += speed * Math.cos(Math.toRadians(angle));
        speed *= friction;
    }

    public void render(Terrain terrain) {
        float frontLeftWheelY = terrain.getTerrainHeightAt(x - 0.9f, z + 1.5f);
        float frontRightWheelY = terrain.getTerrainHeightAt(x + 0.9f, z + 1.5f);
        float rearLeftWheelY = terrain.getTerrainHeightAt(x - 0.9f, z - 1.5f);
        float rearRightWheelY = terrain.getTerrainHeightAt(x + 0.9f, z - 1.5f);

        float averageHeight = (frontLeftWheelY + frontRightWheelY + rearLeftWheelY + rearRightWheelY) / 4.0f;
        float carBodyHeight = 0.5f;
        float carBodyOffset = 4.0f * carBodyHeight + carBodyHeight / 2.0f;

        float pitch = (frontLeftWheelY + frontRightWheelY) / 2.0f - (rearLeftWheelY + rearRightWheelY) / 2.0f;
        float roll = (frontLeftWheelY + rearLeftWheelY) / 2.0f - (frontRightWheelY + rearRightWheelY) / 2.0f;

        GL11.glPushMatrix();
        GL11.glTranslatef(x, averageHeight + carBodyOffset, z);
        GL11.glRotatef(roll * 10.0f, 0, 0, 1);
        GL11.glRotatef(pitch * 10.0f, 1, 0, 0);
        GL11.glRotatef(angle, 0, 1, 0);

        GL11.glColor3f(color[0], color[1], color[2]);
        renderCarBody();
        renderWheels(terrain);
        GL11.glPopMatrix();
    }

    private void renderCarBody() {
        GL11.glShadeModel(GL11.GL_SMOOTH);
        FloatBuffer carBodySpecular = BufferUtils.createFloatBuffer(4).put(new float[] {0.9f, 0.9f, 0.9f, 1.0f});
        carBodySpecular.flip();
        GL11.glMaterialfv(GL11.GL_FRONT, GL11.GL_SPECULAR, carBodySpecular);
        GL11.glMaterialf(GL11.GL_FRONT, GL11.GL_SHININESS, 64.0f);

        float length = 4.0f;
        float width = 2.0f;
        float height = 0.5f;

        GL11.glBegin(GL11.GL_QUADS);
        // Front face
        GL11.glNormal3f(0, 0, 1);
        GL11.glVertex3f(-width/2, -height/2, length/2);
        GL11.glVertex3f(width/2, -height/2, length/2);
        GL11.glVertex3f(width/2, height/2, length/2);
        GL11.glVertex3f(-width/2, height/2, length/2);

        // Back face
        GL11.glVertex3f(-width/2, -height/2, -length/2);
        GL11.glVertex3f(width/2, -height/2, -length/2);
        GL11.glVertex3f(width/2, height/2, -length/2);
        GL11.glVertex3f(-width/2, height/2, -length/2);

        // Left face
        GL11.glVertex3f(-width/2, -height/2, -length/2);
        GL11.glVertex3f(-width/2, -height/2, length/2);
        GL11.glVertex3f(-width/2, height/2, length/2);
        GL11.glVertex3f(-width/2, height/2, -length/2);

        // Right face
        GL11.glVertex3f(width/2, -height/2, -length/2);
        GL11.glVertex3f(width/2, -height/2, length/2);
        GL11.glVertex3f(width/2, height/2, length/2);
        GL11.glVertex3f(width/2, height/2, -length/2);

        // Top face
        GL11.glVertex3f(-width/2, height/2, -length/2);
        GL11.glVertex3f(width/2, height/2, -length/2);
        GL11.glVertex3f(width/2, height/2, length/2);
        GL11.glVertex3f(-width/2, height/2, length/2);

        // Bottom face
        GL11.glVertex3f(-width/2, -height/2, -length/2);
        GL11.glVertex3f(width/2, -height/2, -length/2);
        GL11.glVertex3f(width/2, -height/2, length/2);
        GL11.glVertex3f(-width/2, -height/2, length/2);
        GL11.glEnd();
    }

    private void renderWheel() {
        float radius = 0.4f;
        float width = 0.2f;
        int numSegments = 36;

        GL11.glColor3f(0.2f, 0.2f, 0.2f);
        GL11.glShadeModel(GL11.GL_SMOOTH);
        FloatBuffer wheelSpecular = BufferUtils.createFloatBuffer(4).put(new float[] {0.5f, 0.5f, 0.5f, 1.0f});
        wheelSpecular.flip();
        GL11.glMaterialfv(GL11.GL_FRONT, GL11.GL_SPECULAR, wheelSpecular);
        GL11.glMaterialf(GL11.GL_FRONT, GL11.GL_SHININESS, 16.0f);

        GL11.glPushMatrix();
        GL11.glRotatef(90, 0, 1, 0);

        // Front face
        GL11.glBegin(GL11.GL_TRIANGLE_FAN);
        GL11.glVertex3f(0.0f, 0.0f, -width/2);
        for (int i = 0; i <= numSegments; i++) {
            double angle = 2 * Math.PI * i / numSegments;
            GL11.glVertex3f((float)Math.cos(angle)*radius, (float)Math.sin(angle)*radius, -width/2);
        }
        GL11.glEnd();

        // Rear face
        GL11.glBegin(GL11.GL_TRIANGLE_FAN);
        GL11.glVertex3f(0.0f, 0.0f, width/2);
        for (int i = 0; i <= numSegments; i++) {
            double angle = 2 * Math.PI * i / numSegments;
            GL11.glVertex3f((float)Math.cos(angle)*radius, (float)Math.sin(angle)*radius, width/2);
        }
        GL11.glEnd();

        GL11.glBegin(GL11.GL_QUAD_STRIP);
        for (int i = 0; i <= numSegments; i++) {
            double angle = 2 * Math.PI * i / numSegments;
            float x = (float)Math.cos(angle)*radius;
            float y = (float)Math.sin(angle)*radius;
            GL11.glNormal3f(x, y, 0);
            GL11.glVertex3f(x, y, -width/2);
            GL11.glVertex3f(x, y, width/2);
        }
        GL11.glEnd();
        GL11.glPopMatrix();
    }

    private void renderWheels(Terrain terrain) {
        float wheelHeightOffset = 0.8f;

        // Front-left wheel
        GL11.glPushMatrix();
        float frontLeftWheelY = terrain.getTerrainHeightAt(x-0.9f, z+1.5f);
        GL11.glTranslatef(-0.9f, frontLeftWheelY+0.5f-wheelHeightOffset, 1.5f);
        renderWheel();
        GL11.glPopMatrix();

        // Front-right wheel
        GL11.glPushMatrix();
        float frontRightWheelY = terrain.getTerrainHeightAt(x+0.9f, z+1.5f);
        GL11.glTranslatef(0.9f, frontRightWheelY+0.5f-wheelHeightOffset, 1.5f);
        renderWheel();
        GL11.glPopMatrix();

        // Rear-left wheel
        GL11.glPushMatrix();
        float rearLeftWheelY = terrain.getTerrainHeightAt(x-0.9f, z-1.5f);
        GL11.glTranslatef(-0.9f, rearLeftWheelY+0.5f-wheelHeightOffset, -1.5f);
        renderWheel();
        GL11.glPopMatrix();

        // Rear-right wheel
        GL11.glPushMatrix();
        float rearRightWheelY = terrain.getTerrainHeightAt(x+0.9f, z-1.5f);
        GL11.glTranslatef(0.9f, rearRightWheelY+0.5f-wheelHeightOffset, -1.5f);
        renderWheel();
        GL11.glPopMatrix();
    }
}

class OBJLoader {
    public static Model loadModel(String fileName) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(fileName));
        String line;
        List<float[]> vertices = new ArrayList<>();
        List<float[]> normals = new ArrayList<>();
        List<int[]> faces = new ArrayList<>();

        while ((line = reader.readLine()) != null) {
            String[] tokens = line.split("\\s+");
            if (tokens[0].equals("v")) {
                float[] vertex = {
                        Float.parseFloat(tokens[1]),
                        Float.parseFloat(tokens[2]),
                        Float.parseFloat(tokens[3])
                };
                vertices.add(vertex);
            }
            else if (tokens[0].equals("vn")) {
                float[] normal = {
                        Float.parseFloat(tokens[1]),
                        Float.parseFloat(tokens[2]),
                        Float.parseFloat(tokens[3])
                };
                normals.add(normal);
            }
            else if (tokens[0].equals("f")) {
                String[] v1 = tokens[1].split("/");
                String[] v2 = tokens[2].split("/");
                String[] v3 = tokens[3].split("/");

                int[] face = {
                        Integer.parseInt(v1[0])-1,
                        v1.length>2 ? Integer.parseInt(v1[2])-1 : 0,
                        Integer.parseInt(v2[0])-1,
                        v2.length>2 ? Integer.parseInt(v2[2])-1 : 0,
                        Integer.parseInt(v3[0])-1,
                        v3.length>2 ? Integer.parseInt(v3[2])-1 : 0
                };
                faces.add(face);
            }
        }

        float[] verticesArray = new float[vertices.size()*3];
        float[] normalsArray = new float[normals.size()*3];
        int[] indicesArray = new int[faces.size()*3];
        float[] normalIndicesArray = new float[faces.size()*3];

        int vertexIndex = 0;
        for (float[] vertex : vertices) {
            verticesArray[vertexIndex++] = vertex[0];
            verticesArray[vertexIndex++] = vertex[1];
            verticesArray[vertexIndex++] = vertex[2];
        }

        int normalIndex = 0;
        for (float[] normal : normals) {
            normalsArray[normalIndex++] = normal[0];
            normalsArray[normalIndex++] = normal[1];
            normalsArray[normalIndex++] = normal[2];
        }

        int faceIndex = 0;
        for (int[] face : faces) {
            indicesArray[faceIndex] = face[0];
            normalIndicesArray[faceIndex++] = face[1];
            indicesArray[faceIndex] = face[2];
            normalIndicesArray[faceIndex++] = face[3];
            indicesArray[faceIndex] = face[4];
            normalIndicesArray[faceIndex++] = face[5];
        }

        reader.close();
        return new Model(verticesArray, normalsArray, indicesArray, normalIndicesArray);
    }
}

class Model {
    private float[] vertices;
    private float[] normals;
    private int[] indices;
    private float[] normalIndices;

    public Model(float[] vertices, float[] normals, int[] indices, float[] normalIndices) {
        this.vertices = vertices;
        this.normals = normals;
        this.indices = indices;
        this.normalIndices = normalIndices;
    }

    public float[] getVertices() { return vertices; }
    public float[] getNormals() { return normals; }
    public int[] getIndices() { return indices; }
    public float[] getNormalIndices() { return normalIndices; }
}

class Terrain {
    private Model model;

    public Terrain(String objFilePath) {
        try {
            this.model = OBJLoader.loadModel(objFilePath);
        } catch (IOException e) {
            e.printStackTrace();
            // Create simple fallback terrain
            float[] vertices = {-10,0,-10, -10,0,10, 10,0,10, 10,0,-10};
            float[] normals = {0,1,0, 0,1,0, 0,1,0, 0,1,0};
            int[] indices = {0,1,2, 0,2,3};
            float[] normalIndices = {0,0,0, 0,0,0};
            this.model = new Model(vertices, normals, indices, normalIndices);
        }
    }

    public void render() {
        GL11.glColor3f(0.3f, 0.8f, 0.3f);
        GL11.glShadeModel(GL11.GL_SMOOTH);

        FloatBuffer terrainAmbient = BufferUtils.createFloatBuffer(4).put(new float[]{0.6f,0.8f,0.6f,1.0f});
        FloatBuffer terrainDiffuse = BufferUtils.createFloatBuffer(4).put(new float[]{0.7f,0.9f,0.7f,1.0f});
        FloatBuffer terrainSpecular = BufferUtils.createFloatBuffer(4).put(new float[]{0.2f,0.2f,0.2f,1.0f});
        terrainAmbient.flip(); terrainDiffuse.flip(); terrainSpecular.flip();

        GL11.glMaterialfv(GL11.GL_FRONT_AND_BACK, GL11.GL_AMBIENT, terrainAmbient);
        GL11.glMaterialfv(GL11.GL_FRONT_AND_BACK, GL11.GL_DIFFUSE, terrainDiffuse);
        GL11.glMaterialfv(GL11.GL_FRONT_AND_BACK, GL11.GL_SPECULAR, terrainSpecular);
        GL11.glMaterialf(GL11.GL_FRONT_AND_BACK, GL11.GL_SHININESS, 10.0f);

        float[] vertices = model.getVertices();
        float[] normals = model.getNormals();
        int[] indices = model.getIndices();
        float[] normalIndices = model.getNormalIndices();

        GL11.glBegin(GL11.GL_TRIANGLES);
        for (int i = 0; i < indices.length; i++) {
            int vertexIndex = indices[i]*3;
            int normalIndex = (int)normalIndices[i]*3;

            if (vertexIndex+2 < vertices.length && normalIndex+2 < normals.length) {
                GL11.glNormal3f(normals[normalIndex], normals[normalIndex+1], normals[normalIndex+2]);
                GL11.glVertex3f(vertices[vertexIndex], vertices[vertexIndex+1], vertices[vertexIndex+2]);
            }
        }
        GL11.glEnd();
    }

    public float getTerrainHeightAt(float x, float z) {
        float[] vertices = model.getVertices();
        int[] indices = model.getIndices();

        for (int i = 0; i < indices.length; i += 3) {
            int v1 = indices[i]*3;
            int v2 = indices[i+1]*3;
            int v3 = indices[i+2]*3;

            if (v1+2 >= vertices.length || v2+2 >= vertices.length || v3+2 >= vertices.length) {
                continue;
            }

            float v1X = vertices[v1], v1Y = vertices[v1+1], v1Z = vertices[v1+2];
            float v2X = vertices[v2], v2Y = vertices[v2+1], v2Z = vertices[v2+2];
            float v3X = vertices[v3], v3Y = vertices[v3+1], v3Z = vertices[v3+2];

            if (isPointInTriangle(x, z, v1X, v1Z, v2X, v2Z, v3X, v3Z)) {
                return interpolateHeight(x, z, v1X, v1Y, v1Z, v2X, v2Y, v2Z, v3X, v3Y, v3Z);
            }
        }
        return 0.0f;
    }

    private boolean isPointInTriangle(float px, float pz, float v1X, float v1Z, float v2X, float v2Z, float v3X, float v3Z) {
        float d1 = sign(px, pz, v1X, v1Z, v2X, v2Z);
        float d2 = sign(px, pz, v2X, v2Z, v3X, v3Z);
        float d3 = sign(px, pz, v3X, v3Z, v1X, v1Z);

        boolean hasNeg = (d1 < 0) || (d2 < 0) || (d3 < 0);
        boolean hasPos = (d1 > 0) || (d2 > 0) || (d3 > 0);
        return !(hasNeg && hasPos);
    }

    private float sign(float px, float pz, float v1X, float v1Z, float v2X, float v2Z) {
        return (px-v2X)*(v1Z-v2Z)-(v1X-v2X)*(pz-v2Z);
    }

    private float interpolateHeight(float x, float z, float v1X, float v1Y, float v1Z,
                                    float v2X, float v2Y, float v2Z, float v3X, float v3Y, float v3Z) {
        float areaTotal = triangleArea(v1X, v1Z, v2X, v2Z, v3X, v3Z);
        float area1 = triangleArea(x, z, v2X, v2Z, v3X, v3Z);
        float area2 = triangleArea(x, z, v3X, v3Z, v1X, v1Z);
        float area3 = triangleArea(x, z, v1X, v1Z, v2X, v2Z);

        float weight1 = area1/areaTotal;
        float weight2 = area2/areaTotal;
        float weight3 = area3/areaTotal;

        return weight1*v1Y + weight2*v2Y + weight3*v3Y;
    }

    private float triangleArea(float x1, float z1, float x2, float z2, float x3, float z3) {
        return Math.abs((x1*(z2-z3) + x2*(z3-z1) + x3*(z1-z2))/2.0f);
    }
}