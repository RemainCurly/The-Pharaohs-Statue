package a4;

/*******************
 * Michael Burns
 * Assignment 4
 *******************/

import java.awt.event.*;
import java.nio.*;
import java.lang.Math;
import javax.swing.*;
import static com.jogamp.opengl.GL4.*;
import com.jogamp.opengl.*;
import com.jogamp.opengl.util.*;
import com.jogamp.opengl.awt.GLCanvas;
import com.jogamp.common.nio.Buffers;
import org.joml.*;

public class Starter extends JFrame implements GLEventListener, KeyListener, MouseMotionListener, MouseWheelListener, MouseListener
{	
   //Pointers, Vectors, Models, etc. ints
   private GLCanvas myCanvas;
   private int cubeMapProgram, basicRenderingProgram, axesProgram, lightingProgram, passOneProgram, enviroProgram, noiseProgram, shieldProgram;
   private ImportedModel cubeModel, sphereModel, palmTreesModel, carModel, anubisModel, pharaohModel, scarabModel;
   private int vao[] = new int[1];
   private int vbo[] = new int[25];
   private Vector2f mouseBegin, mouseDragEnd;
   private Vector3f shapeTranslate, shapeRotate, shapeScale;
   private int cubeMapTexture, sandTexture, palmTreeTexture, anubisTexture, marbleTexture;
   private boolean axesOn, wantLights;

   //Camera
   private Camera camera;
   private Noise marble = new Noise(300, 300, 300);
   private Vector3f cameraLoc = new Vector3f(0.0f, 0.0f, 28.0f);
   private float cameraScroll = 0.8f;

   //Matrices and Location Pointers
   private FloatBuffer vals = Buffers.newDirectFloatBuffer(16);
   private Matrix4f pMat = new Matrix4f();  // perspective matrix
   private Matrix4f vMat = new Matrix4f();  // view matrix
   private Matrix4f mMat = new Matrix4f();  // model matrix
   private Matrix4f mvMat = new Matrix4f(); // model-view matrix
   private Matrix4f invTrMat = new Matrix4f(); // inverse-transpose
   private int vLoc, mvLoc, projLoc, mvLocAxes, projLocAxes, nLoc, sLoc, alphaLoc, lightLoc, bumpLoc, shieldLoc;
   private float aspect;

   //Lighting
   private Vector3f initialLightLoc = new Vector3f(0.0f, 70.0f, 30.0f);
   private Vector3f currentLightLoc = new Vector3f();
   private Vector3f tempLightLoc = new Vector3f();
   private int globalAmbLoc, ambLoc, diffLoc, specLoc, posLoc, mAmbLoc, mDiffLoc, mSpecLoc, mShiLoc, lightChoiceLoc;
   private float[] lightPos = new float[3];
   float[] globalAmbient = new float[] { 0.6f, 0.6f, 0.6f, 0.6f};
   float[] lightAmbient = new float[] { 0.3f, 0.3f, 0.3f, 1.0f};
   float[] lightDiffuse = new float[] { 0.2f, 0.2f, 0.2f, 1.0f};
   float[] lightSpecular = new float[] {0.5f, 0.5f, 0.5f, 1.0f};
   float[][] goldADS = new float[][] {Utils.goldAmbient(), Utils.goldDiffuse(), Utils.goldSpecular(), {Utils.goldShininess(), 0, 0}};
   float[][] bronADS = new float[][] {Utils.bronzeAmbient(), Utils.bronzeDiffuse(), Utils.bronzeSpecular(), {Utils.bronzeShininess(), 0, 0}};
   float[] jadeAmb = new float[] {0.135f, 0.222f, 0.1575f, 1};
   float[] jadeDif = new float[] {0.54f, 0.89f, 0.63f, 1};
   float[] jadeSpe = new float[] {0.316f, 0.316f, 0.316f, 1};
   float jadeShi = 0.1f;
   float[][] jadeADS = new float[][] {jadeAmb, jadeDif, jadeSpe, {jadeShi, 0, 0}};
   private float lightsOn;

   //Shadows
   private int scSizeX, scSizeY;
   private int[] shadowTex = new int[1];
   private int[] shadowBuffer = new int[1];
   private Matrix4f lightVMat = new Matrix4f();
   private Matrix4f lightPMat = new Matrix4f();
   private Matrix4f shadowMVP1 = new Matrix4f();
   private Matrix4f shadowMVP2 = new Matrix4f();
   private Matrix4f b = new Matrix4f();
   private Vector3f origin = new Vector3f(0.0f, 0.0f, 0.0f);
   private Vector3f up = new Vector3f(0.0f, 1.0f, 0.0f);

   public Starter()
   {
      setTitle("Assignment 4 - Csc 155");
      setSize(800, 800);
      myCanvas = new GLCanvas();
      myCanvas.addGLEventListener(this);
      myCanvas.addKeyListener(this);
      myCanvas.addMouseMotionListener(this);
      myCanvas.addMouseWheelListener(this);
      myCanvas.addMouseListener(this);
      this.add(myCanvas);
      this.setVisible(true);
      this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
      this.requestFocusInWindow();
      Animator animator = new Animator(myCanvas);
      animator.start();
      axesOn = false;
      wantLights = true;
      lightsOn = 1.0f;
   }

   //Initializes programs, models, textures, and others
   public void init(GLAutoDrawable drawable)
   {
      GL4 gl = (GL4) GLContext.getCurrentGL();
      cubeMapProgram = Utils.createShaderProgram("a4/vertCube.glsl", "a4/fragCube.glsl");
      basicRenderingProgram = Utils.createShaderProgram("a4/vertBasic.glsl", "a4/fragBasic.glsl");
      axesProgram = Utils.createShaderProgram("a4/vertAxes.glsl", "a4/fragAxes.glsl");
      enviroProgram = Utils.createShaderProgram("a4/vertEnviro.glsl", "a4/fragEnviro.glsl");
      lightingProgram = Utils.createShaderProgram("a4/vertLights.glsl", "a4/fragLights.glsl");
      passOneProgram = Utils.createShaderProgram("a4/vertPassOne.glsl", "a4/fragPassOne.glsl");
      noiseProgram = Utils.createShaderProgram("a4/vertNoise.glsl", "a4/fragNoise.glsl");
      shieldProgram = Utils.createShaderProgram("a4/vertShield.glsl", "a4/geomShield.glsl", "a4/fragShield.glsl");
   
      cubeModel = new ImportedModel("Cube.obj");
      sphereModel = new ImportedModel("Sphere.obj");
      palmTreesModel = new ImportedModel("Tree.obj");
      carModel = new ImportedModel("Car.obj");
      anubisModel = new ImportedModel("Anubis.obj");
      pharaohModel = new ImportedModel("Pharaoh.obj");
      scarabModel = new ImportedModel("Scarab.obj");
   
      aspect = (float) myCanvas.getWidth() / (float) myCanvas.getHeight();
      pMat.identity().setPerspective((float) Math.toRadians(90.0f), aspect, 0.1f, 1000.0f);
   
      setupVertices();
      setupShadowBuffers();
   
      b.set(
             0.5f, 0.0f, 0.0f, 0.0f,
             0.0f, 0.5f, 0.0f, 0.0f,
             0.0f, 0.0f, 0.5f, 0.0f,
             0.5f, 0.5f, 0.5f, 1.0f);
   
      camera = new Camera(cameraLoc, cameraScroll, (float)Math.toRadians(10.0f));
      currentLightLoc.set(initialLightLoc);
   
      sandTexture = Utils.loadTexture("a4/Sand.jpg");
      palmTreeTexture = Utils.loadTexture("a4/PalmTreeTexture.png");
      anubisTexture = Utils.loadTexture("a4/AnubisTexture.png");
      marbleTexture = buildNoiseTexture();
      cubeMapTexture = Utils.loadCubeMap("a4/cubeMap");
      gl.glEnable(GL_TEXTURE_CUBE_MAP_SEAMLESS);
   }

   //Self explanatory, for shadow mapping
   private void setupShadowBuffers()
   {
      GL4 gl = (GL4) GLContext.getCurrentGL();
      scSizeX = myCanvas.getWidth();
      scSizeY = myCanvas.getHeight();
   
      gl.glGenFramebuffers(1, shadowBuffer, 0);
   
      gl.glGenTextures(1, shadowTex, 0);
      gl.glBindTexture(GL_TEXTURE_2D, shadowTex[0]);
      gl.glTexImage2D(GL_TEXTURE_2D, 0, GL_DEPTH_COMPONENT32,
             scSizeX, scSizeY, 0, GL_DEPTH_COMPONENT, GL_FLOAT, null);
      gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
      gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
      gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_COMPARE_MODE, GL_COMPARE_REF_TO_TEXTURE);
      gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_COMPARE_FUNC, GL_LEQUAL);
   
      gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
      gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
   }

   //Calls Noise.java for the Pharaoh's statue noise
   private int buildNoiseTexture()
   {
      GL4 gl = (GL4) GLContext.getCurrentGL();
   
      byte[] data = new byte[marble.getNoiseHeight() * marble.getNoiseWidth() * marble.getNoiseDepth() * 4];
   
      marble.fillDataArray(data);
   
      ByteBuffer bb = Buffers.newDirectByteBuffer(data);
   
      int[] textureIDs = new int[1];
      gl.glGenTextures(1, textureIDs, 0);
      int textureID = textureIDs[0];
   
      gl.glBindTexture(GL_TEXTURE_3D, textureID);
   
      gl.glTexStorage3D(GL_TEXTURE_3D, 1, GL_RGBA8, marble.getNoiseWidth(), marble.getNoiseHeight(), marble.getNoiseDepth());
      gl.glTexSubImage3D(GL_TEXTURE_3D, 0, 0, 0, 0, marble.getNoiseWidth(), marble.getNoiseHeight(), marble.getNoiseDepth(),
                         GL_RGBA, GL_UNSIGNED_INT_8_8_8_8_REV, bb);
      gl.glTexParameteri(GL_TEXTURE_3D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
   
      return textureID;
   }

   //Collects Models into Objects, and binds all the buffers
   private void setupVertices()
   {	
      GL4 gl = (GL4) GLContext.getCurrentGL();
   
      Object cube = new Object(cubeModel.getNumVertices(), cubeModel.getVertices(),
                              cubeModel.getTexCoords(), cubeModel.getNormals());
      Object sphere = new Object(sphereModel.getNumVertices(), sphereModel.getVertices(),
                                sphereModel.getTexCoords(), sphereModel.getNormals());
      Object palmTrees = new Object(palmTreesModel.getNumVertices(), palmTreesModel.getVertices(),
                                   palmTreesModel.getTexCoords(), palmTreesModel.getNormals());
      Object car = new Object(carModel.getNumVertices(), carModel.getVertices(), carModel.getTexCoords(), carModel.getNormals());
      Object anubis = new Object(anubisModel.getNumVertices(), anubisModel.getVertices(), anubisModel.getTexCoords(), anubisModel.getNormals());
      Object pharaoh = new Object(pharaohModel.getNumVertices(), pharaohModel.getVertices(), pharaohModel.getTexCoords(), pharaohModel.getNormals());
      Object scarab = new Object(scarabModel.getNumVertices(), scarabModel.getVertices(),
                                 scarabModel.getTexCoords(), scarabModel.getNormals());
      BasicShape cubeMap = new BasicShape(BasicShape.Shape.CUBE);
   
      float[] axesLocations =
         {
             0.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f,
             0.0f, 0.0f, 0.0f, 0.0f, 1.0f, 0.0f,
             0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f
         };
   
      gl.glGenVertexArrays(vao.length, vao, 0);
      gl.glBindVertexArray(vao[0]);
      gl.glGenBuffers(vbo.length, vbo, 0);
   
      //Cube Map
      gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[0]);
      FloatBuffer cMapBuf = Buffers.newDirectFloatBuffer(cubeMap.getVertices());
      gl.glBufferData(GL_ARRAY_BUFFER, cMapBuf.limit()*4, cMapBuf, GL_STATIC_DRAW);
   
      //Test Cube (p, t, & n)
      gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[1]);
      FloatBuffer cubePBuf = Buffers.newDirectFloatBuffer(cube.getpValues());
      gl.glBufferData(GL_ARRAY_BUFFER, cubePBuf.limit()*4, cubePBuf, GL_STATIC_DRAW);
      gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[2]);
      FloatBuffer cubeTBuf = Buffers.newDirectFloatBuffer(cube.gettValues());
      gl.glBufferData(GL_ARRAY_BUFFER, cubeTBuf.limit()*4, cubeTBuf, GL_STATIC_DRAW);
      gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[3]);
      FloatBuffer cubeNBuf = Buffers.newDirectFloatBuffer(cube.getnValues());
      gl.glBufferData(GL_ARRAY_BUFFER, cubeNBuf.limit()*4, cubeNBuf, GL_STATIC_DRAW);
   
      //Axes
      gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[4]);
      FloatBuffer axesBuf = Buffers.newDirectFloatBuffer(axesLocations);
      gl.glBufferData(GL_ARRAY_BUFFER, axesBuf.limit()*4, axesBuf, GL_STATIC_DRAW);
   
      //Sphere Model (p, t, & n)
      gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[5]);
      FloatBuffer spherePBuf = Buffers.newDirectFloatBuffer(sphere.getpValues());
      gl.glBufferData(GL_ARRAY_BUFFER, spherePBuf.limit()*4, spherePBuf, GL_STATIC_DRAW);
      gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[6]);
      FloatBuffer sphereTBuf = Buffers.newDirectFloatBuffer(sphere.gettValues());
      gl.glBufferData(GL_ARRAY_BUFFER, sphereTBuf.limit()*4, sphereTBuf, GL_STATIC_DRAW);
      gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[7]);
      FloatBuffer sphereNBuf = Buffers.newDirectFloatBuffer(sphere.getnValues());
      gl.glBufferData(GL_ARRAY_BUFFER, sphereNBuf.limit()*4, sphereNBuf, GL_STATIC_DRAW);
   
      //Palm Trees Model (p, t, & n)
      gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[8]);
      FloatBuffer palmPBuf = Buffers.newDirectFloatBuffer(palmTrees.getpValues());
      gl.glBufferData(GL_ARRAY_BUFFER, palmPBuf.limit()*4, palmPBuf, GL_STATIC_DRAW);
      gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[9]);
      FloatBuffer palmTBuf = Buffers.newDirectFloatBuffer(palmTrees.gettValues());
      gl.glBufferData(GL_ARRAY_BUFFER, palmTBuf.limit()*4, palmTBuf, GL_STATIC_DRAW);
      gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[10]);
      FloatBuffer palmNBuf = Buffers.newDirectFloatBuffer(palmTrees.getnValues());
      gl.glBufferData(GL_ARRAY_BUFFER, palmNBuf.limit()*4, palmNBuf, GL_STATIC_DRAW);
   
      //Car Model (p, t, & n)
      gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[11]);
      FloatBuffer carPBuf = Buffers.newDirectFloatBuffer(car.getpValues());
      gl.glBufferData(GL_ARRAY_BUFFER, carPBuf.limit()*4, carPBuf, GL_STATIC_DRAW);
      gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[12]);
      FloatBuffer carTBuf = Buffers.newDirectFloatBuffer(car.gettValues());
      gl.glBufferData(GL_ARRAY_BUFFER, carTBuf.limit()*4, carTBuf, GL_STATIC_DRAW);
      gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[13]);
      FloatBuffer carNBuf = Buffers.newDirectFloatBuffer(car.getnValues());
      gl.glBufferData(GL_ARRAY_BUFFER, carNBuf.limit()*4, carNBuf, GL_STATIC_DRAW);
   
      //Anubis Model (p, t, & n)
      gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[14]);
      FloatBuffer anubisPBuf = Buffers.newDirectFloatBuffer(anubis.getpValues());
      gl.glBufferData(GL_ARRAY_BUFFER, anubisPBuf.limit()*4, anubisPBuf, GL_STATIC_DRAW);
      gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[15]);
      FloatBuffer anubisTBuf = Buffers.newDirectFloatBuffer(anubis.gettValues());
      gl.glBufferData(GL_ARRAY_BUFFER, anubisTBuf.limit()*4, anubisTBuf, GL_STATIC_DRAW);
      gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[16]);
      FloatBuffer anubisNBuf = Buffers.newDirectFloatBuffer(anubis.getnValues());
      gl.glBufferData(GL_ARRAY_BUFFER, anubisNBuf.limit()*4, anubisNBuf, GL_STATIC_DRAW);
   
      //Pharaoh Model (p, t, & n)
      gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[17]);
      FloatBuffer pharaohPBuf = Buffers.newDirectFloatBuffer(pharaoh.getpValues());
      gl.glBufferData(GL_ARRAY_BUFFER, pharaohPBuf.limit()*4, pharaohPBuf, GL_STATIC_DRAW);
      gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[18]);
      FloatBuffer pharaohTBuf = Buffers.newDirectFloatBuffer(pharaoh.gettValues());
      gl.glBufferData(GL_ARRAY_BUFFER, pharaohTBuf.limit()*4, pharaohTBuf, GL_STATIC_DRAW);
      gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[19]);
      FloatBuffer pharaohNBuf = Buffers.newDirectFloatBuffer(pharaoh.getnValues());
      gl.glBufferData(GL_ARRAY_BUFFER, pharaohNBuf.limit()*4, pharaohNBuf, GL_STATIC_DRAW);
   
      //Scarab Model (p, t, & n)
      gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[20]);
      FloatBuffer scarabPBuf = Buffers.newDirectFloatBuffer(scarab.getpValues());
      gl.glBufferData(GL_ARRAY_BUFFER, scarabPBuf.limit()*4, scarabPBuf, GL_STATIC_DRAW);
      gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[21]);
      FloatBuffer scarabTBuf = Buffers.newDirectFloatBuffer(scarab.gettValues());
      gl.glBufferData(GL_ARRAY_BUFFER, scarabTBuf.limit()*4, scarabTBuf, GL_STATIC_DRAW);
      gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[22]);
      FloatBuffer scarabNBuf = Buffers.newDirectFloatBuffer(scarab.getnValues());
      gl.glBufferData(GL_ARRAY_BUFFER, scarabNBuf.limit()*4, scarabNBuf, GL_STATIC_DRAW);
   }
   
   //Set up for shadow mapping, using PassOne and PassTwo
   public void display(GLAutoDrawable drawable)
   {	
      GL4 gl = (GL4) GLContext.getCurrentGL();
      gl.glClear(GL_COLOR_BUFFER_BIT);
      gl.glClearColor(0.15f, 0.16f, 0.2f, 1.0f);
      gl.glClear(GL_DEPTH_BUFFER_BIT);
   
      lightVMat.identity().setLookAt(currentLightLoc, origin, up);
      lightPMat.identity().setPerspective((float)Math.toRadians(60.0f), aspect, 0.1f, 1000.0f);
   
      gl.glBindFramebuffer(GL_FRAMEBUFFER, shadowBuffer[0]);
      gl.glFramebufferTexture(GL_FRAMEBUFFER, GL_DEPTH_ATTACHMENT, shadowTex[0], 0);
   
      gl.glDrawBuffer(GL_NONE);
      gl.glEnable(GL_DEPTH_TEST);
      gl.glEnable(GL_POLYGON_OFFSET_FILL);
      gl.glPolygonOffset(3.0f, 5.0f);
   
      passOne();
   
      gl.glDisable(GL_POLYGON_OFFSET_FILL);
   
      gl.glBindFramebuffer(GL_FRAMEBUFFER, 0);
      gl.glActiveTexture(GL_TEXTURE1);
      gl.glBindTexture(GL_TEXTURE_2D, shadowTex[0]);
   
      gl.glDrawBuffer(GL_FRONT);
   
      passTwo();
   }

   //Renders all object vertices from the PoV of the light source
   private void passOne()
   {
      GL4 gl = (GL4) GLContext.getCurrentGL();
      gl.glUseProgram(passOneProgram);
   
      //Sand Ground===================================================================================================
      mMat.identity().translate(0.0f, -6.0f, -20.0f).scale(50.0f, .5f, 50.0f);
      shadowMVP1.identity().mul(lightPMat).mul(lightVMat).mul(mMat);
      sLoc = gl.glGetUniformLocation(passOneProgram, "shadowMVP");
      gl.glUniformMatrix4fv(sLoc, 1, false, shadowMVP1.get(vals));
   
      gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[1]);
      gl.glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);
      gl.glEnableVertexAttribArray(0);
   
      gl.glDisable(GL_CULL_FACE);
      gl.glClear(GL_DEPTH_BUFFER_BIT);
      gl.glEnable(GL_DEPTH_TEST);
      gl.glDepthFunc(GL_LEQUAL);
      gl.glDrawArrays(GL_TRIANGLES, 0, cubeModel.getNumVertices());
   
      //Scarab Beetle 1 (right)=======================================================================================
      shapeTranslate = new Vector3f(16.0f, -5.5f, 10.0f);
      shapeRotate = new Vector3f((float) Math.toRadians(-90.0f), 0.0f, (float) Math.toRadians(120.0f));
      shapeScale = new Vector3f(.1f, .1f, .1f);
      drawScarab(shapeTranslate, shapeRotate, shapeScale, true);
   
      //Scarab Beetle 2 (left)========================================================================================
      shapeTranslate = new Vector3f(-22.0f, -5.5f, 10.0f);
      shapeRotate = new Vector3f((float) Math.toRadians(-90.0f), 0.0f, (float) Math.toRadians(-120.0f));
      shapeScale = new Vector3f(.1f, .1f, .1f);
      drawScarab(shapeTranslate, shapeRotate, shapeScale, true);
   
      //Pharaoh Statue================================================================================================
      mMat.identity().translate(0.0f, -6.0f, -23.0f).rotateXYZ((float)Math.toRadians(-90.0f), 0.0f, (float)Math.toRadians(180.0f)).scale(.5f, .5f, .7f);
      shadowMVP1.identity().mul(lightPMat).mul(lightVMat).mul(mMat);
      sLoc = gl.glGetUniformLocation(passOneProgram, "shadowMVP");
      gl.glUniformMatrix4fv(sLoc, 1, false, shadowMVP1.get(vals));
   
      gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[17]);
      gl.glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);
      gl.glEnableVertexAttribArray(0);
   
      gl.glDrawArrays(GL_TRIANGLES, 0, pharaohModel.getNumVertices());
   
      //Chrome Car====================================================================================================
      mMat.identity().translate(-20.0f, -5.5f, -20.0f).rotateY((float)Math.toRadians(30.0f)).scale(3.0f, 3.0f, 3.0f);
      shadowMVP1.identity().mul(lightPMat).mul(lightVMat).mul(mMat);
      sLoc = gl.glGetUniformLocation(passOneProgram, "shadowMVP");
      gl.glUniformMatrix4fv(sLoc, 1, false, shadowMVP1.get(vals));
   
      gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[11]);
      gl.glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);
      gl.glEnableVertexAttribArray(0);
   
      gl.glDrawArrays(GL_TRIANGLES, 0, carModel.getNumVertices());
   
      //Palm Tree (far left)==========================================================================================
      shapeTranslate = new Vector3f(-40.0f, -6.0f, -8.0f);
      shapeRotate = new Vector3f(0.0f, (float)Math.toRadians(20.0f), 0.0f);
      shapeScale = new Vector3f(2.0f, 2.0f ,2.0f);
      drawPalmTree(shapeTranslate, shapeRotate, shapeScale, true);
   
      //Palm Tree (mid left)==========================================================================================
      shapeTranslate = new Vector3f(-30.0f, -6.0f, -60.0f);
      shapeRotate = new Vector3f(0.0f, (float)Math.toRadians(90.0f), 0.0f);
      drawPalmTree(shapeTranslate, shapeRotate, shapeScale, true);
   
      //Palm Tree (mid right)=========================================================================================
      shapeTranslate = new Vector3f(20.0f, -6.0f, -60.0f);
      shapeRotate = new Vector3f(0.0f, (float)Math.toRadians(170.0f), 0.0f);
      drawPalmTree(shapeTranslate, shapeRotate, shapeScale, true);
   
      //Palm Tree (far right)=========================================================================================
      shapeTranslate = new Vector3f(40.0f, -6.0f, -20.0f);
      shapeRotate = new Vector3f(0.0f, (float)Math.toRadians(130.0f), 0.0f);
      drawPalmTree(shapeTranslate, shapeRotate, shapeScale, true);
   }

   //Renders all objects normally (comments show each object)
   private void passTwo()
   {
      GL4 gl = (GL4) GLContext.getCurrentGL();
      gl.glClear(GL_COLOR_BUFFER_BIT);
      gl.glClear(GL_DEPTH_BUFFER_BIT);
      gl.glUseProgram(cubeMapProgram);
   
      camera.refreshMatrices();
      vMat.identity().mul(camera.getrMat()).mul(camera.gettMat());
   
      //Cube Map======================================================================================================
      if(wantLights)
      {
         vLoc = gl.glGetUniformLocation(cubeMapProgram, "v_matrix");
         gl.glUniformMatrix4fv(vLoc, 1, false, vMat.get(vals));
      
         projLoc = gl.glGetUniformLocation(cubeMapProgram, "proj_matrix");
         gl.glUniformMatrix4fv(projLoc, 1, false, pMat.get(vals));
      
         gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[0]);
         gl.glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);
         gl.glEnableVertexAttribArray(0);
      
         gl.glActiveTexture(GL_TEXTURE0);
         gl.glBindTexture(GL_TEXTURE_CUBE_MAP, cubeMapTexture);
      
         gl.glEnable(GL_CULL_FACE);
         gl.glFrontFace(GL_CCW);
         gl.glDisable(GL_DEPTH_TEST);
         gl.glDrawArrays(GL_TRIANGLES, 0, 36);
         gl.glEnable(GL_DEPTH_TEST);
         gl.glDisable(GL_CULL_FACE);
      }
   
      //Sphere sun====================================================================================================
      if(wantLights)
      {
         gl.glUseProgram(basicRenderingProgram);
         mMat.identity().translate(currentLightLoc).scale(0.5f, 0.5f, 0.5f);
         mvMat.identity().mul(vMat).mul(mMat);
         mvLoc = gl.glGetUniformLocation(basicRenderingProgram, "mv_matrix");
         projLoc = gl.glGetUniformLocation(basicRenderingProgram, "proj_matrix");
         alphaLoc = gl.glGetUniformLocation(basicRenderingProgram, "alpha");
         lightLoc = gl.glGetUniformLocation(basicRenderingProgram, "light");
         gl.glUniformMatrix4fv(mvLoc, 1, false, mvMat.get(vals));
         gl.glUniformMatrix4fv(projLoc, 1, false, pMat.get(vals));
         gl.glProgramUniform1f(basicRenderingProgram, alphaLoc, 1.0f);
         gl.glProgramUniform1f(basicRenderingProgram, lightLoc, lightsOn);
         gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[5]);
         gl.glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);
         gl.glEnableVertexAttribArray(0);
         gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[6]);
         gl.glVertexAttribPointer(1, 2, GL_FLOAT, false, 0, 0);
         gl.glEnableVertexAttribArray(1);
         gl.glActiveTexture(GL_TEXTURE0);
         gl.glBindTexture(GL_TEXTURE_2D, anubisTexture);
         gl.glEnable(GL_DEPTH_TEST);
         gl.glFrontFace(GL_LEQUAL);
         gl.glDrawArrays(GL_TRIANGLES, 0, sphereModel.getNumVertices());
      }
   
      //Shield========================================================================================================
      if(!wantLights)
      {
         gl.glUseProgram(shieldProgram);
      
         mvLoc = gl.glGetUniformLocation(shieldProgram, "mv_matrix");
         projLoc = gl.glGetUniformLocation(shieldProgram, "proj_matrix");
         nLoc = gl.glGetUniformLocation(shieldProgram, "norm_matrix");
         shieldLoc = gl.glGetUniformLocation(shieldProgram, "shield");
      
         mMat.identity().translate(-3.0f, 2.0f, -21.0f).scale(10.0f, 11.0f, 10.0f);
         mvMat.identity().mul(vMat).mul(mMat);
         mvMat.invert(invTrMat);
         invTrMat.transpose(invTrMat);
      
         gl.glUniformMatrix4fv(mvLoc, 1, false, mvMat.get(vals));
         gl.glUniformMatrix4fv(projLoc, 1, false, pMat.get(vals));
         gl.glUniformMatrix4fv(nLoc, 1, false, invTrMat.get(vals));
         gl.glProgramUniform1f(shieldProgram, shieldLoc, 1.0f);
      
         gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[5]);
         gl.glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);
         gl.glEnableVertexAttribArray(0);
         gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[7]);
         gl.glVertexAttribPointer(1, 3, GL_FLOAT, false, 0, 0);
         gl.glEnableVertexAttribArray(1);
      
         gl.glDrawArrays(GL_TRIANGLES, 0, sphereModel.getNumVertices());
      
      //Protector Beetle==============================================================================================
         mvLoc = gl.glGetUniformLocation(shieldProgram, "mv_matrix");
         projLoc = gl.glGetUniformLocation(shieldProgram, "proj_matrix");
         nLoc = gl.glGetUniformLocation(shieldProgram, "norm_matrix");
         shieldLoc = gl.glGetUniformLocation(shieldProgram, "shield");
      
         mMat.identity().translate(-3.0f, 27.0f, -24.0f);
         mvMat.identity().mul(vMat).mul(mMat);
         mvMat.invert(invTrMat);
         invTrMat.transpose(invTrMat);
      
         gl.glUniformMatrix4fv(mvLoc, 1, false, mvMat.get(vals));
         gl.glUniformMatrix4fv(projLoc, 1, false, pMat.get(vals));
         gl.glUniformMatrix4fv(nLoc, 1, false, invTrMat.get(vals));
         gl.glProgramUniform1f(shieldProgram, shieldLoc, 0.0f);
      
         gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[20]);
         gl.glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);
         gl.glEnableVertexAttribArray(0);
         gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[22]);
         gl.glVertexAttribPointer(1, 3, GL_FLOAT, false, 0, 0);
         gl.glEnableVertexAttribArray(1);
      
         gl.glDrawArrays(GL_TRIANGLES, 0, scarabModel.getNumVertices());
      }
   
      //Sand Ground===================================================================================================
      gl.glUseProgram(lightingProgram);
   
      mvLoc = gl.glGetUniformLocation(lightingProgram, "mv_matrix");
      projLoc = gl.glGetUniformLocation(lightingProgram, "proj_matrix");
      nLoc = gl.glGetUniformLocation(lightingProgram, "norm_matrix");
      sLoc = gl.glGetUniformLocation(lightingProgram, "shadowMVP");
      bumpLoc = gl.glGetUniformLocation(lightingProgram, "bump");
   
      installLights(vMat, lightingProgram, jadeADS);
   
      mMat.identity().translate(0.0f, -6.0f, -20.0f).scale(50.0f, .5f, 50.0f);
      mvMat.identity().mul(vMat).mul(mMat);
      mvMat.invert(invTrMat);
      invTrMat.transpose(invTrMat);
      shadowMVP2.identity().mul(b).mul(lightPMat).mul(lightVMat).mul(mMat);
   
      gl.glUniformMatrix4fv(mvLoc, 1, false, mvMat.get(vals));
      gl.glUniformMatrix4fv(projLoc, 1, false, pMat.get(vals));
      gl.glUniformMatrix4fv(nLoc, 1, false, invTrMat.get(vals));
      gl.glUniformMatrix4fv(sLoc, 1, false, shadowMVP2.get(vals));
      gl.glProgramUniform1f(lightingProgram, bumpLoc, 0.0f);
   
      gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[1]);
      gl.glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);
      gl.glEnableVertexAttribArray(0);
      gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[2]);
      gl.glVertexAttribPointer(1, 2, GL_FLOAT, false, 0, 0);
      gl.glEnableVertexAttribArray(1);
      gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[3]);
      gl.glVertexAttribPointer(2, 3, GL_FLOAT, false, 0, 0);
      gl.glEnableVertexAttribArray(2);
   
      gl.glActiveTexture(GL_TEXTURE0);
      gl.glBindTexture(GL_TEXTURE_2D, sandTexture);
      gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR_MIPMAP_LINEAR);
      gl.glGenerateMipmap(GL_TEXTURE_2D);
      if (gl.isExtensionAvailable("GL_EXT_texture_filter_anisotropic")) {
         float anisoSetting[] = new float[1];
         gl.glGetFloatv(GL_MAX_TEXTURE_MAX_ANISOTROPY_EXT, anisoSetting, 0);
         gl.glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_MAX_ANISOTROPY_EXT, anisoSetting[0]);
      }
   
      gl.glDrawArrays(GL_TRIANGLES, 0, cubeModel.getNumVertices());

      //Scarab Beetle 1 (right)=======================================================================================
      if(wantLights)
      {
         shapeTranslate = new Vector3f(16.0f, -5.5f, 10.0f);
         shapeRotate = new Vector3f((float) Math.toRadians(-90.0f), 0.0f, (float) Math.toRadians(120.0f));
         shapeScale = new Vector3f(.1f, .1f, .1f);
         drawScarab(shapeTranslate, shapeRotate, shapeScale, false);
      
      //Scarab Beetle 2 (left)========================================================================================
         shapeTranslate = new Vector3f(-22.0f, -5.5f, 10.0f);
         shapeRotate = new Vector3f((float) Math.toRadians(-90.0f), 0.0f, (float) Math.toRadians(-120.0f));
         shapeScale = new Vector3f(.1f, .1f, .1f);
         drawScarab(shapeTranslate, shapeRotate, shapeScale, false);
      }
   
      //Pharaoh Statue================================================================================================
      gl.glUseProgram(noiseProgram);
   
      mvLoc = gl.glGetUniformLocation(noiseProgram, "mv_matrix");
      projLoc = gl.glGetUniformLocation(noiseProgram, "proj_matrix");
      nLoc = gl.glGetUniformLocation(noiseProgram, "norm_matrix");
      sLoc = gl.glGetUniformLocation(noiseProgram, "shadowMVP");
   
      mMat.identity().translate(0.0f, -6.0f, -23.0f).rotateXYZ((float)Math.toRadians(-90.0f), 0.0f, (float)Math.toRadians(180.0f)).scale(.5f, .5f, .7f);
      mvMat.identity().mul(vMat).mul(mMat);
   
      installLights(vMat, noiseProgram, bronADS);
   
      mvMat.invert(invTrMat);
      invTrMat.transpose(invTrMat);
      shadowMVP2.identity().mul(b).mul(lightPMat).mul(lightVMat).mul(mMat);
   
      gl.glUniformMatrix4fv(mvLoc, 1, false, mvMat.get(vals));
      gl.glUniformMatrix4fv(projLoc, 1, false, pMat.get(vals));
      gl.glUniformMatrix4fv(nLoc, 1, false, invTrMat.get(vals));
      gl.glUniformMatrix4fv(sLoc, 1, false, shadowMVP2.get(vals));
   
      gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[17]);
      gl.glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);
      gl.glEnableVertexAttribArray(0);
      gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[19]);
      gl.glVertexAttribPointer(1, 3, GL_FLOAT, false, 0, 0);
      gl.glEnableVertexAttribArray(1);
   
      gl.glActiveTexture(GL_TEXTURE0);
      gl.glBindTexture(GL_TEXTURE_3D, marbleTexture);
      gl.glEnable(GL_CULL_FACE);
      gl.glFrontFace(GL_CCW);
      gl.glEnable(GL_DEPTH_TEST);
      gl.glDepthFunc(GL_LEQUAL);
   
      gl.glDrawArrays(GL_TRIANGLES, 0, pharaohModel.getNumVertices());
      gl.glDisable(GL_CULL_FACE);
   
      //Chrome Car====================================================================================================
      if(wantLights)
      {
         gl.glUseProgram(enviroProgram);
      
         mvLoc = gl.glGetUniformLocation(enviroProgram, "mv_matrix");
         projLoc = gl.glGetUniformLocation(enviroProgram, "proj_matrix");
         nLoc = gl.glGetUniformLocation(enviroProgram, "norm_matrix");
         sLoc = gl.glGetUniformLocation(enviroProgram, "shadowMVP");
      
         mMat.identity().translate(-20.0f, -5.5f, -20.0f).rotateY((float) Math.toRadians(30.0f)).scale(3.0f, 3.0f, 3.0f);
         mvMat.identity().mul(vMat).mul(mMat);
      
         mvMat.invert(invTrMat);
         invTrMat.transpose(invTrMat);
         shadowMVP2.identity().mul(b).mul(lightPMat).mul(lightVMat).mul(mMat);
      
         installLights(vMat, enviroProgram, goldADS);
      
         gl.glUniformMatrix4fv(mvLoc, 1, false, mvMat.get(vals));
         gl.glUniformMatrix4fv(projLoc, 1, false, pMat.get(vals));
         gl.glUniformMatrix4fv(nLoc, 1, false, invTrMat.get(vals));
         gl.glUniformMatrix4fv(sLoc, 1, false, shadowMVP2.get(vals));
      
         gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[11]);
         gl.glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);
         gl.glEnableVertexAttribArray(0);
      
         gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[13]);
         gl.glVertexAttribPointer(1, 3, GL_FLOAT, false, 0, 0);
         gl.glEnableVertexAttribArray(1);
      
         gl.glActiveTexture(GL_TEXTURE0);
         gl.glBindTexture(GL_TEXTURE_CUBE_MAP, cubeMapTexture);
         gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR_MIPMAP_LINEAR);
         gl.glGenerateMipmap(GL_TEXTURE_2D);
         if (gl.isExtensionAvailable("GL_EXT_texture_filter_anisotropic")) {
            float anisoSetting[] = new float[1];
            gl.glGetFloatv(GL_MAX_TEXTURE_MAX_ANISOTROPY_EXT, anisoSetting, 0);
            gl.glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_MAX_ANISOTROPY_EXT, anisoSetting[0]);
         }
      
         gl.glDrawArrays(GL_TRIANGLES, 0, carModel.getNumVertices());
      }
   
      //Palm Tree (far left)==========================================================================================
      gl.glUseProgram(lightingProgram);
      shapeTranslate = new Vector3f(-40.0f, -6.0f, -8.0f);
      shapeRotate = new Vector3f(0.0f, (float)Math.toRadians(20.0f), 0.0f);
      shapeScale = new Vector3f(2.0f, 2.0f ,2.0f);
      drawPalmTree(shapeTranslate, shapeRotate, shapeScale, false);
   
      //Palm Tree (mid left)==========================================================================================
      shapeTranslate = new Vector3f(-30.0f, -6.0f, -60.0f);
      shapeRotate = new Vector3f(0.0f, (float)Math.toRadians(90.0f), 0.0f);
      drawPalmTree(shapeTranslate, shapeRotate, shapeScale, false);
   
      //Palm Tree (mid right)=========================================================================================
      shapeTranslate = new Vector3f(20.0f, -6.0f, -60.0f);
      shapeRotate = new Vector3f(0.0f, (float)Math.toRadians(170.0f), 0.0f);
      drawPalmTree(shapeTranslate, shapeRotate, shapeScale, false);
   
      //Palm Tree (far right)=========================================================================================
      shapeTranslate = new Vector3f(40.0f, -6.0f, -20.0f);
      shapeRotate = new Vector3f(0.0f, (float)Math.toRadians(130.0f), 0.0f);
      drawPalmTree(shapeTranslate, shapeRotate, shapeScale, false);
   
      //Anubis (left guard)===========================================================================================
      if(!wantLights)
      {
         gl.glUseProgram(basicRenderingProgram);
      
         mvLoc = gl.glGetUniformLocation(basicRenderingProgram, "mv_matrix");
         projLoc = gl.glGetUniformLocation(basicRenderingProgram, "proj_matrix");
         alphaLoc = gl.glGetUniformLocation(basicRenderingProgram, "alpha");
         lightLoc = gl.glGetUniformLocation(basicRenderingProgram, "light");
      
         mMat.identity().translate(-20.0f, -9.0f, 10.0f).rotateXYZ((float)Math.toRadians(270.0f), 0.0f, (float)Math.toRadians(90.0f)).scale(2.0f, 2.0f, 2.0f);
         mvMat.identity().mul(vMat).mul(mMat);
      
         gl.glUniformMatrix4fv(mvLoc, 1, false, mvMat.get(vals));
         gl.glUniformMatrix4fv(projLoc, 1, false, pMat.get(vals));
         gl.glProgramUniform1f(basicRenderingProgram, lightLoc, lightsOn);
      
         gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[14]);
         gl.glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);
         gl.glEnableVertexAttribArray(0);
         gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[15]);
         gl.glVertexAttribPointer(1, 2, GL_FLOAT, false, 0, 0);
         gl.glEnableVertexAttribArray(1);
      
         gl.glActiveTexture(GL_TEXTURE0);
         gl.glBindTexture(GL_TEXTURE_2D, anubisTexture);
      
         gl.glEnable(GL_BLEND);
         gl.glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
         gl.glBlendEquation(GL_FUNC_ADD);
      
         gl.glEnable(GL_CULL_FACE);
      
         gl.glCullFace(GL_FRONT);
         gl.glProgramUniform1f(basicRenderingProgram, alphaLoc, 0.1f);
         gl.glDrawArrays(GL_TRIANGLES, 0, anubisModel.getNumVertices());
      
         gl.glCullFace(GL_BACK);
         gl.glProgramUniform1f(basicRenderingProgram, alphaLoc, 0.5f);
         gl.glDrawArrays(GL_TRIANGLES, 0, anubisModel.getNumVertices());
      
      //Anubis (right guard)==========================================================================================
         mvLoc = gl.glGetUniformLocation(basicRenderingProgram, "mv_matrix");
         projLoc = gl.glGetUniformLocation(basicRenderingProgram, "proj_matrix");
         alphaLoc = gl.glGetUniformLocation(basicRenderingProgram, "alpha");
      
         mMat.identity().translate(15.0f, -9.0f, 10.0f).rotateXYZ((float)Math.toRadians(270.0f), 0.0f, (float)Math.toRadians(-90.0f)).scale(2.0f, 2.0f, 2.0f);
         mvMat.identity().mul(vMat).mul(mMat);
      
         gl.glUniformMatrix4fv(mvLoc, 1, false, mvMat.get(vals));
         gl.glUniformMatrix4fv(projLoc, 1, false, pMat.get(vals));
      
         gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[14]);
         gl.glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);
         gl.glEnableVertexAttribArray(0);
         gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[15]);
         gl.glVertexAttribPointer(1, 2, GL_FLOAT, false, 0, 0);
         gl.glEnableVertexAttribArray(1);
      
         gl.glActiveTexture(GL_TEXTURE0);
         gl.glBindTexture(GL_TEXTURE_2D, anubisTexture);
      
         gl.glEnable(GL_BLEND);
         gl.glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
         gl.glBlendEquation(GL_FUNC_ADD);
      
         gl.glEnable(GL_CULL_FACE);
      
         gl.glCullFace(GL_FRONT);
         gl.glProgramUniform1f(basicRenderingProgram, alphaLoc, 0.1f);
         gl.glDrawArrays(GL_TRIANGLES, 0, anubisModel.getNumVertices());
      
         gl.glCullFace(GL_BACK);
         gl.glProgramUniform1f(basicRenderingProgram, alphaLoc, 0.5f);
         gl.glDrawArrays(GL_TRIANGLES, 0, anubisModel.getNumVertices());
      }
   
      //Axes==========================================================================================================
      if(axesOn)
      {
         gl.glUseProgram(axesProgram);
         mMat.identity().translate(0.0f, 0.0f, 0.0f).scale(10.0f, 10.0f, 10.0f);
         mvMat.identity().mul(vMat).mul(mMat);
         mvLocAxes = gl.glGetUniformLocation(axesProgram, "mv_matrix");
         projLocAxes = gl.glGetUniformLocation(axesProgram, "proj_matrix");
         gl.glUniformMatrix4fv(mvLocAxes, 1, false, mvMat.get(vals));
         gl.glUniformMatrix4fv(projLocAxes, 1, false, pMat.get(vals));
         gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[4]);
         gl.glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);
         gl.glEnableVertexAttribArray(0);
         gl.glDrawArrays(GL_LINES, 0, 6);
      }
   }

   //Called for all objects that use uniform lighting. "program" and "material" help determine
   //    which object and material ADS to use in the lighting process
   private void installLights(Matrix4f vMatrix, int program, float[][] material)
   {
      GL4 gl = (GL4) GLContext.getCurrentGL();
   
      tempLightLoc.set(currentLightLoc);
      tempLightLoc.mulPosition(vMatrix);
      lightPos[0] = tempLightLoc.x(); lightPos[1] = tempLightLoc.y(); lightPos[2] = tempLightLoc.z();
   
      globalAmbLoc = gl.glGetUniformLocation(program, "globalAmbient");
      ambLoc = gl.glGetUniformLocation(program, "light.ambient");
      diffLoc = gl.glGetUniformLocation(program, "light.diffuse");
      specLoc = gl.glGetUniformLocation(program, "light.specular");
      posLoc = gl.glGetUniformLocation(program, "light.position");
      mAmbLoc = gl.glGetUniformLocation(program, "material.ambient");
      mDiffLoc = gl.glGetUniformLocation(program, "material.diffuse");
      mSpecLoc = gl.glGetUniformLocation(program, "material.specular");
      mShiLoc = gl.glGetUniformLocation(program, "material.shininess");
      lightChoiceLoc = gl.glGetUniformLocation(program, "choice");
      gl.glProgramUniform4fv(program, globalAmbLoc, 1, globalAmbient, 0);
      gl.glProgramUniform4fv(program, ambLoc, 1, lightAmbient,0);
      gl.glProgramUniform4fv(program, diffLoc, 1, lightDiffuse, 0);
      gl.glProgramUniform4fv(program, specLoc, 1, lightSpecular, 0);
      gl.glProgramUniform3fv(program, posLoc, 1, lightPos, 0);
      gl.glProgramUniform4fv(program, mAmbLoc, 1, material[0], 0);
      gl.glProgramUniform4fv(program, mDiffLoc, 1, material[1], 0);
      gl.glProgramUniform4fv(program, mSpecLoc, 1, material[2], 0);
      gl.glProgramUniform1f(program, mShiLoc, material[3][0]);
      gl.glProgramUniform1f(program, lightChoiceLoc, lightsOn);
   }

   //Used for multiple draws of the Scarab Beetle, and if it's shadow passed or not
   private void drawScarab(Vector3f translate, Vector3f rotate, Vector3f scale, boolean isShadowPass)
   {
      GL4 gl = (GL4) GLContext.getCurrentGL();
   
      if(!isShadowPass)
      {
         mvLoc = gl.glGetUniformLocation(lightingProgram, "mv_matrix");
         projLoc = gl.glGetUniformLocation(lightingProgram, "proj_matrix");
         nLoc = gl.glGetUniformLocation(lightingProgram, "norm_matrix");
         sLoc = gl.glGetUniformLocation(lightingProgram, "shadowMVP");
         bumpLoc = gl.glGetUniformLocation(lightingProgram, "bump");
      }
   
      mMat.identity().translate(translate).rotateXYZ(rotate).scale(scale);
   
      if(!isShadowPass)
      {
         installLights(vMat, lightingProgram, jadeADS);
         mvMat.identity().mul(vMat).mul(mMat);
         mvMat.invert(invTrMat);
         invTrMat.transpose(invTrMat);
         shadowMVP2.identity().mul(b).mul(lightPMat).mul(lightVMat).mul(mMat);
         gl.glUniformMatrix4fv(mvLoc, 1, false, mvMat.get(vals));
         gl.glUniformMatrix4fv(projLoc, 1, false, pMat.get(vals));
         gl.glUniformMatrix4fv(nLoc, 1, false, invTrMat.get(vals));
         gl.glUniformMatrix4fv(sLoc, 1, false, shadowMVP2.get(vals));
         gl.glProgramUniform1f(lightingProgram, bumpLoc, 1.0f);
      }
      else
      {
         shadowMVP1.identity().mul(lightPMat).mul(lightVMat).mul(mMat);
         sLoc = gl.glGetUniformLocation(passOneProgram, "shadowMVP");
         gl.glUniformMatrix4fv(sLoc, 1, false, shadowMVP1.get(vals));
      }
   
      gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[20]);
      gl.glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);
      gl.glEnableVertexAttribArray(0);
   
      if(!isShadowPass)
      {
         gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[21]);
         gl.glVertexAttribPointer(1, 2, GL_FLOAT, false, 0, 0);
         gl.glEnableVertexAttribArray(1);
         gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[22]);
         gl.glVertexAttribPointer(2, 3, GL_FLOAT, false, 0, 0);
         gl.glEnableVertexAttribArray(2);
         gl.glActiveTexture(GL_TEXTURE0);
         gl.glBindTexture(GL_TEXTURE_2D, anubisTexture);
      }
   
      gl.glDrawArrays(GL_TRIANGLES, 0, scarabModel.getNumVertices());
   }

   //Used for multiple draws of the Palm Trees, and if it's shadow passed or not
   private void drawPalmTree(Vector3f translate, Vector3f rotate, Vector3f scale, boolean isShadowPass)
   {
      GL4 gl = (GL4) GLContext.getCurrentGL();
   
      if(!isShadowPass)
      {
         mvLoc = gl.glGetUniformLocation(lightingProgram, "mv_matrix");
         projLoc = gl.glGetUniformLocation(lightingProgram, "proj_matrix");
         nLoc = gl.glGetUniformLocation(lightingProgram, "norm_matrix");
         sLoc = gl.glGetUniformLocation(lightingProgram, "shadowMVP");
         bumpLoc = gl.glGetUniformLocation(lightingProgram, "bump");
      }
   
      mMat.identity().translate(translate).rotateXYZ(rotate).scale(scale);
   
      if(!isShadowPass)
      {
         installLights(vMat, lightingProgram, jadeADS);
         mvMat.identity().mul(vMat).mul(mMat);
         mvMat.invert(invTrMat);
         invTrMat.transpose(invTrMat);
         shadowMVP2.identity().mul(b).mul(lightPMat).mul(lightVMat).mul(mMat);
         gl.glUniformMatrix4fv(mvLoc, 1, false, mvMat.get(vals));
         gl.glUniformMatrix4fv(projLoc, 1, false, pMat.get(vals));
         gl.glUniformMatrix4fv(nLoc, 1, false, invTrMat.get(vals));
         gl.glUniformMatrix4fv(sLoc, 1, false, shadowMVP2.get(vals));
         gl.glProgramUniform1f(lightingProgram, bumpLoc, 0.0f);
      }
      else
      {
         shadowMVP1.identity().mul(lightPMat).mul(lightVMat).mul(mMat);
         sLoc = gl.glGetUniformLocation(passOneProgram, "shadowMVP");
         gl.glUniformMatrix4fv(sLoc, 1, false, shadowMVP1.get(vals));
      }
   
      gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[8]);
      gl.glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);
      gl.glEnableVertexAttribArray(0);
   
      if(!isShadowPass)
      {
         gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[9]);
         gl.glVertexAttribPointer(1, 2, GL_FLOAT, false, 0, 0);
         gl.glEnableVertexAttribArray(1);
         gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[10]);
         gl.glVertexAttribPointer(2, 3, GL_FLOAT, false, 0, 0);
         gl.glEnableVertexAttribArray(2);
         gl.glActiveTexture(GL_TEXTURE0);
         gl.glBindTexture(GL_TEXTURE_2D, palmTreeTexture);
      }
   
      gl.glDrawArrays(GL_TRIANGLES, 0, palmTreesModel.getNumVertices());
   }

   //KeyListener (commands in ReadMe)
   public void keyPressed(KeyEvent e)
   {
      switch(e.getKeyCode())
      {
         case KeyEvent.VK_W:
            camera.moveCamera(Camera.Direction.FORWARD);
            break;
         case KeyEvent.VK_S:
            camera.moveCamera(Camera.Direction.BACKWARD);
            break;
         case KeyEvent.VK_A:
            camera.moveCamera(Camera.Direction.LEFT);
            break;
         case KeyEvent.VK_D:
            camera.moveCamera(Camera.Direction.RIGHT);
            break;
         case KeyEvent.VK_E:
            camera.moveCamera(Camera.Direction.DOWN);
            break;
         case KeyEvent.VK_Q:
            camera.moveCamera(Camera.Direction.UP);
            break;
         case KeyEvent.VK_L:
            lightsOn = (lightsOn == 1.0f)? 0.0f : 1.0f;
            wantLights = !wantLights;
            System.out.println("wantLights = " + wantLights);
            break;
         case KeyEvent.VK_R:
            camera = new Camera(cameraLoc, cameraScroll, (float)Math.toRadians(10.0f));
            break;
         case KeyEvent.VK_LEFT:
            camera.moveCamera(Camera.Direction.PAN_LEFT);
            break;
         case KeyEvent.VK_RIGHT:
            camera.moveCamera(Camera.Direction.PAN_RIGHT);
            break;
         case KeyEvent.VK_UP:
            camera.moveCamera(Camera.Direction.PITCH_UP);
            break;
         case KeyEvent.VK_DOWN:
            camera.moveCamera(Camera.Direction.PITCH_DOWN);
            break;
         case KeyEvent.VK_SPACE:
            axesOn = !axesOn;
            System.out.println("axesOn = " + axesOn);
            break;
         default:
      }
      //System.out.println(camera);
   }

   //Mouse listeners (determine X & Y movement of positional light)=============================
   public void mousePressed(MouseEvent e)
   {
      mouseBegin = new Vector2f(e.getX(), e.getY());
   }

   public void mouseDragged(MouseEvent e)
   {
      mouseDragEnd = new Vector2f(e.getX() - mouseBegin.x(), e.getY() - mouseBegin.y());
      currentLightLoc.add(mouseDragEnd.x()*0.006f, -mouseDragEnd.y()*0.006f, 0.0f);
   
      if(currentLightLoc.x() > 90.0f)
         currentLightLoc.set(90.0f, currentLightLoc.y(), currentLightLoc.z());
      else if(currentLightLoc.x() < -90.0f)
         currentLightLoc.set(-90.0f, currentLightLoc.y(), currentLightLoc.z());
      if(currentLightLoc.y() > 70.0f)
         currentLightLoc.set(currentLightLoc.x(), 70.0f, currentLightLoc.z());
      else if(currentLightLoc.y() < 0.0f)
         currentLightLoc.set(currentLightLoc.x(), 0.0f, currentLightLoc.z());
   }

   //MouseWheel listener (determines Z movement of positional light)=============================
   public void mouseWheelMoved(MouseWheelEvent e)
   {
      currentLightLoc.add(0.0f, 0.0f, e.getWheelRotation());
   
      if(currentLightLoc.z() > 30.0f)
         currentLightLoc.set(currentLightLoc.x(), currentLightLoc.y(), 30.0f);
      else if(currentLightLoc.z() < -130.0f)
         currentLightLoc.set(currentLightLoc.x(), currentLightLoc.y(), -130.0f);
   }

   public static void main(String[] args)
   {
      new Starter();
   }
   public void reshape(GLAutoDrawable glAutoDrawable, int i, int i1, int i2, int i3)
   {
      aspect = (float) myCanvas.getWidth() / (float) myCanvas.getHeight();
      pMat.identity().setPerspective((float) Math.toRadians(90.0f), aspect, 0.1f, 1000.0f);
      setupShadowBuffers();
   }
   public void dispose(GLAutoDrawable glAutoDrawable) {
   
   }
   public void keyReleased(KeyEvent e) {
   
   }
   public void keyTyped(KeyEvent e) {
   
   }
   public void mouseClicked(MouseEvent e) {
   
   }
   public void mouseReleased(MouseEvent e) {
   
   }
   public void mouseEntered(MouseEvent e) {
   
   }
   public void mouseExited(MouseEvent e) {
   
   }
   public void mouseMoved(MouseEvent e) {
   
   }
}
