/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package za.co.bruynhuis.ld47.screens;

import aurelienribon.tweenengine.BaseTween;
import aurelienribon.tweenengine.Tween;
import aurelienribon.tweenengine.TweenCallback;
import aurelienribon.tweenengine.equations.Bounce;
import aurelienribon.tweenengine.equations.Expo;
import aurelienribon.tweenengine.equations.Linear;
import com.bruynhuis.galago.control.camera.CameraShaker;
import com.bruynhuis.galago.control.tween.SpatialAccessor;
import com.bruynhuis.galago.filters.BarrelBlurFilter;
import com.bruynhuis.galago.filters.FXAAFilter;
import com.bruynhuis.galago.games.physics.PhysicsGameListener;
import com.bruynhuis.galago.listener.KeyboardControlEvent;
import com.bruynhuis.galago.listener.KeyboardControlInputListener;
import com.bruynhuis.galago.listener.KeyboardControlListener;
import com.bruynhuis.galago.messages.MessageListener;
import com.bruynhuis.galago.screen.AbstractScreen;
import com.bruynhuis.galago.ui.Image;
import com.bruynhuis.galago.ui.Label;
import com.bruynhuis.galago.ui.TextAlign;
import com.bruynhuis.galago.ui.button.TouchButton;
import com.bruynhuis.galago.ui.listener.TouchButtonAdapter;
import com.bruynhuis.galago.util.SpatialUtils;
import com.jayfella.jme.atmosphere.NewAtmosphereState;
import com.jayfella.jme.atmosphere.WeatherState;
import com.jme3.bullet.PhysicsSpace;
import com.jme3.bullet.PhysicsTickListener;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.font.BitmapFont;
import com.jme3.font.BitmapText;
import com.jme3.font.Rectangle;
import com.jme3.input.ChaseCamera;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.post.FilterPostProcessor;
import com.jme3.post.filters.BloomFilter;
import com.jme3.post.filters.TranslucentBucketFilter;
import com.jme3.renderer.queue.RenderQueue;
import com.jme3.scene.CameraNode;
import com.jme3.scene.Node;
import com.jme3.scene.SceneGraphVisitor;
import com.jme3.scene.Spatial;
import com.jme3.scene.control.BillboardControl;
import com.jme3.shadow.DirectionalLightShadowFilter;
import com.jme3.shadow.DirectionalLightShadowRenderer;
import com.jme3.shadow.EdgeFilteringMode;
import java.util.ArrayList;
import za.co.bruynhuis.ld47.MainApplication;
import za.co.bruynhuis.ld47.control.PickupControl;
import za.co.bruynhuis.ld47.game.Game;
import za.co.bruynhuis.ld47.game.Player;
import za.co.bruynhuis.ld47.game.PlayerPlayback;
import za.co.bruynhuis.ld47.game.PlayerRecorder;
import za.co.bruynhuis.ld47.game.RecordingSnapshot;
import za.co.bruynhuis.ld47.ui.Button;
import za.co.bruynhuis.ld47.ui.ButtonExit;
import za.co.bruynhuis.ld47.ui.VehicleButton;

/**
 *
 * @author NideBruyn
 */
public class PlayScreen extends AbstractScreen implements PhysicsGameListener, KeyboardControlListener, PhysicsTickListener, MessageListener {

    public static final String NAME = "PlayScreen";
    private Label messageLabel;
    private Label lapLabel;
    private Label lootLabel;
    private Image loot;
    private VehicleButton vehicleButton1;
    private VehicleButton vehicleButton2;
    private VehicleButton vehicleButton3;
    private VehicleButton vehicleButton4;

    private MainApplication mainApplication;
    private KeyboardControlInputListener keyboardControlInputListener;
    private ChaseCamera chaseCam;
    private CameraNode cameraNode;
    private CameraShaker cameraShaker;
    private FilterPostProcessor fpp;
    private BarrelBlurFilter barrelBlurFilter;
    private DirectionalLightShadowRenderer dlsr;
    private NewAtmosphereState atmosphereState;
    private WeatherState weatherState;

    private Game game;
    private Player player;
    private boolean gameover = false;

    private TouchButton debugButton;
    private Button retryButton;
    private Button playButton;
    private ButtonExit exitButton;
    private Label readyLabel;
    private Label goLabel;
    private Image logo;
    private Node musicVolumeNode = new Node("music");
    private PlayerRecorder playerRecorder;
    private ArrayList<RecordingSnapshot> snapShotList = new ArrayList<>();
    private String currentVehicle = "truck1";
    private boolean played = false;

    @Override
    protected void init() {
        mainApplication = (MainApplication) baseApplication;
        
        logo = new Image(hudPanel, "Interface/logo.png", 600, 120, true);
        logo.centerTop(0, 80);

        lapLabel = new Label(hudPanel, "LAPS: 0", 26, 300, 50);
        lapLabel.centerAt(-460, 330);
        lapLabel.setAlignment(TextAlign.LEFT);

        lootLabel = new Label(hudPanel, "x 0", 26, 300, 50);
        lootLabel.centerAt(460, 330);
        lootLabel.setAlignment(TextAlign.RIGHT);

        loot = new Image(hudPanel, "Interface/loot.png", 48, 48, true);
        loot.centerAt(480, 330);

        vehicleButton1 = new VehicleButton(hudPanel, "Models/player/truck1.png", "truck1");
        vehicleButton1.setCost(0);
        vehicleButton1.centerAt(-192, -280);
        vehicleButton1.addButtonAdapter(new TouchButtonAdapter() {
            @Override
            public void doTouchUp(float touchX, float touchY, float tpf, String uid) {
                baseApplication.getSoundManager().playSound("button");
                buyVehicle(0);
                currentVehicle = "truck1";
                player.updateModel(currentVehicle);
                hideVehicleOptions();
                messageLabel.hide();
                doStartGameAction();
            }

        });

        vehicleButton2 = new VehicleButton(hudPanel, "Models/player/buggy1.png", "buggy1");
        vehicleButton2.setCost(2000);
        vehicleButton2.centerAt(-64, -280);
        vehicleButton2.addButtonAdapter(new TouchButtonAdapter() {
            @Override
            public void doTouchUp(float touchX, float touchY, float tpf, String uid) {
                baseApplication.getSoundManager().playSound("button");
                buyVehicle(2000);
                currentVehicle = "buggy1";
                player.updateModel(currentVehicle);
                hideVehicleOptions();
                messageLabel.hide();
                doStartGameAction();
            }

        });

        vehicleButton3 = new VehicleButton(hudPanel, "Models/player/car1.png", "car1");
        vehicleButton3.setCost(3000);
        vehicleButton3.centerAt(64, -280);
        vehicleButton3.addButtonAdapter(new TouchButtonAdapter() {
            @Override
            public void doTouchUp(float touchX, float touchY, float tpf, String uid) {
                baseApplication.getSoundManager().playSound("button");
                buyVehicle(3000);
                currentVehicle = "car1";
                player.updateModel(currentVehicle);
                hideVehicleOptions();
                messageLabel.hide();
                doStartGameAction();
            }

        });

        vehicleButton4 = new VehicleButton(hudPanel, "Models/player/ship1.png", "ship1");
        vehicleButton4.setCost(5000);
        vehicleButton4.centerAt(192, -280);
        vehicleButton4.addButtonAdapter(new TouchButtonAdapter() {
            @Override
            public void doTouchUp(float touchX, float touchY, float tpf, String uid) {
                baseApplication.getSoundManager().playSound("button");
                buyVehicle(5000);
                currentVehicle = "ship1";
                player.updateModel(currentVehicle);
                hideVehicleOptions();
                messageLabel.hide();
                doStartGameAction();
            }

        });

        messageLabel = new Label(hudPanel, "Choose a vehicle?", 38, 400, 50);
        messageLabel.setAlignment(TextAlign.CENTER);
        messageLabel.centerAt(0, 0);

        readyLabel = new Label(hudPanel, "READY", 58, 600, 50);
        readyLabel.centerAt(0, 200);

        goLabel = new Label(hudPanel, "GO!", 58, 600, 50);
        goLabel.centerAt(0, 200);

//        debugButton = new TouchButton(hudPanel, "debug", "Debug");
//        debugButton.rightBottom(5, 5);
//        debugButton.addTouchButtonListener(new TouchButtonAdapter() {
//            @Override
//            public void doTouchUp(float touchX, float touchY, float tpf, String uid) {
//                mainApplication.showDebuging();
//                mainApplication.showStats();
//            }
//
//        });

        retryButton = new Button(hudPanel, "retryButton", "RETRY");
        retryButton.centerAt(0, 80);
        retryButton.addTouchButtonListener(new TouchButtonAdapter() {
            @Override
            public void doTouchUp(float touchX, float touchY, float tpf, String uid) {
                showScreen(PlayScreen.NAME);
                game.getBaseApplication().getSoundManager().playSound("button");
            }

        });
        
        playButton = new Button(hudPanel, "play", "PLAY");
        playButton.centerAt(0, 80);
        playButton.addTouchButtonListener(new TouchButtonAdapter() {
            @Override
            public void doTouchUp(float touchX, float touchY, float tpf, String uid) {
                showStartAction();
                game.getBaseApplication().getSoundManager().playSound("button");
            }

        });        
        
        exitButton = new ButtonExit(hudPanel, "exit", "EXIT");
        exitButton.centerAt(0, -10);
        exitButton.addTouchButtonListener(new TouchButtonAdapter() {
            @Override
            public void doTouchUp(float touchX, float touchY, float tpf, String uid) {
                exitScreen();
                game.getBaseApplication().getSoundManager().playSound("button");
            }

        });        

        keyboardControlInputListener = new KeyboardControlInputListener();
        keyboardControlInputListener.addKeyboardControlListener(this);

    }

    private void buyVehicle(int i) {
        int score = baseApplication.getGameSaves().getGameData().getScore();
        score = score - i;
        baseApplication.getGameSaves().getGameData().setScore(score);
        baseApplication.getGameSaves().save();
        player.setScore(score);
        lootLabel.setText("x " + score);
    }

    @Override
    protected void load() {

        gameover = false;

        game = new Game(mainApplication, rootNode);
        game.load();

        player = new Player(game, currentVehicle);
        player.load();
        player.addScore(baseApplication.getGameSaves().getGameData().getScore());
        lootLabel.setText("x " + player.getScore());

        playerRecorder = new PlayerRecorder();
        player.getModel().addControl(playerRecorder);

        log("SNAPSHOTS FOUND: " + snapShotList.size());
        for (int i = 0; i < snapShotList.size(); i++) {
            RecordingSnapshot snapshot = snapShotList.get(i);
            log("Snapshot entry count: " + snapshot.getEntries().size());
            log("Entry 0 of snapshot " + i + ": " + snapshot.getEntries().get(0).toString());
            Spatial spatial = snapshot.getVehicle();
            spatial.setLocalTranslation(snapshot.getEntries().get(0).getLocation());
            spatial.setLocalRotation(snapshot.getEntries().get(0).getRotation());
            if (spatial.getControl(PlayerPlayback.class) == null) {
                spatial.addControl(new PlayerPlayback(game, snapshot));

                //Add text
                BitmapText text = new BitmapText(baseApplication.getBitmapFont());
                text.setBox(new Rectangle(-2, -0.1f, 4, 0.2f));
                text.setAlignment(BitmapFont.Align.Center);
                text.setText("Play: " + snapshot.getRetryNumber());
                text.setSize(0.2f);
                text.setColor(new ColorRGBA(0.99f, 0.99f, 0.99f, 1));
                text.setLocalTranslation(0f, 0.8f, 0f);
                text.setQueueBucket(RenderQueue.Bucket.Translucent);
                text.setShadowMode(RenderQueue.ShadowMode.Off);

                BillboardControl billboardControl = new BillboardControl();
                billboardControl.setAlignment(BillboardControl.Alignment.Screen);
                text.addControl(billboardControl);
                ((Node) spatial).attachChild(text);

            }

            rootNode.attachChild(spatial);
        }

        game.addGameListener(this);

        camera.setLocation(player.getFollowNode().getWorldTranslation());
//        camera.lookAt(player.getPosition().clone(), Vector3f.UNIT_Y);

//        if (chaseCam == null) {
        chaseCam = new ChaseCamera(camera, player.getPlayerNode(), inputManager);
        chaseCam.setDefaultVerticalRotation(FastMath.DEG_TO_RAD * 10);
        chaseCam.setDefaultHorizontalRotation(FastMath.DEG_TO_RAD * 0);
        chaseCam.setMinDistance(5);
        chaseCam.setMaxDistance(5);
        chaseCam.setLookAtOffset(new Vector3f(0, 1f, 0));
        chaseCam.setDefaultDistance(5);
//        chaseCam.setInvertHorizontalAxis(true);
//        chaseCam.setInvertVerticalAxis(true);
//        chaseCam.setRotationSpeed(5);
        chaseCam.setTrailingEnabled(true);
        chaseCam.setSmoothMotion(true);
        chaseCam.setTrailingSensitivity(1f);
        chaseCam.setChasingSensitivity(8);
//    }
//        chaseCam.setSpatial(player.getPlayerNode());

//        cameraNode = new CameraNode("cam-node", camera);
//        rootNode.attachChild(cameraNode);
//        cameraNode.setLocalTranslation(player.getFollowNode().getWorldTranslation());
//        cameraNode.lookAt(player.getPlayerNode().getWorldTranslation(), Vector3f.UNIT_Y);
//        chaseCam.setDragToRotate(false);
        cameraShaker = new CameraShaker(camera, rootNode);

        initSky();
        initEffects();

        baseApplication.getMessageManager().addMessageListener(this);

    }

    private void initSky() {

        // SkyState skyState = new SkyState();
        atmosphereState = new NewAtmosphereState(rootNode);
        baseApplication.getStateManager().attach(atmosphereState);

        weatherState = new WeatherState(atmosphereState);
        weatherState.setAutoWeather(true);
        baseApplication.getStateManager().attach(weatherState);

        atmosphereState.getCalendar().setHour(10);

    }

    private void initEffects() {
//        dlsr = new DirectionalLightShadowRenderer(assetManager, 2048, 2);
//        dlsr.setLight(atmosphereState.getDirectionalLight());
//        dlsr.setShadowIntensity(0.65f);
//        dlsr.setEdgesThickness(5);
//        dlsr.setShadowCompareMode(CompareMode.Hardware);
//        dlsr.setEdgeFilteringMode(EdgeFilteringMode.PCF8);
////        dlsr.setEnabledStabilization(false);
//        baseApplication.getViewPort().addProcessor(dlsr);

        fpp = new FilterPostProcessor(assetManager);
        baseApplication.getViewPort().addProcessor(fpp);

        FXAAFilter fxaaf = new FXAAFilter();
        fpp.addFilter(fxaaf);

        BloomFilter bf = new BloomFilter(BloomFilter.GlowMode.Objects);
        bf.setBloomIntensity(1.25f);
        bf.setBlurScale(2f);
        fpp.addFilter(bf);

//        DepthOfFieldFilter doff = new DepthOfFieldFilter();
//        doff.setFocusDistance(0);
//        doff.setFocusRange(60);
//        doff.setBlurScale(1.4f);
//        fpp.addFilter(doff);
        fpp.addFilter(new TranslucentBucketFilter());

        DirectionalLightShadowFilter dlsf = new DirectionalLightShadowFilter(assetManager, 4096, 1);
        dlsf.setLight(atmosphereState.getDirectionalLight());
        dlsf.setEdgeFilteringMode(EdgeFilteringMode.PCF8);
//        dlsf.setShadowCompareMode(CompareMode.Software);
        dlsf.setShadowIntensity(0.55f);
//        dlsf.setEdgesThickness(1);
        fpp.addFilter(dlsf);

//        SSAOFilter basicSSAOFilter = new SSAOFilter();
//        basicSSAOFilter.setBias(0.2f);
//        basicSSAOFilter.setIntensity(2.5f);
//        basicSSAOFilter.setSampleRadius(0.15f);
//        basicSSAOFilter.setScale(0.12f);
//        fpp.addFilter(basicSSAOFilter);
        barrelBlurFilter = new BarrelBlurFilter();
        barrelBlurFilter.setAmount(1);
        barrelBlurFilter.setEnabled(false);
        fpp.addFilter(barrelBlurFilter);

    }

    private void activatePlayer() {
        if (atmosphereState != null) {
//            atmosphereState.getCalendar().setHour(10);
//            atmosphereState.getCalendar().setTimeMult(1000);
        }
        playerRecorder.start();
        player.activate(currentVehicle);
        chaseCam.setTrailingSensitivity(5);
        fadeMusicIn();
        barrelBlurFilter.setEnabled(true);

    }

    private void startAllSnapshots() {
        for (int i = 0; i < snapShotList.size(); i++) {
            RecordingSnapshot snapshot = snapShotList.get(i);
            if (snapshot.getVehicle().getControl(PlayerPlayback.class) != null) {
                snapshot.getVehicle().getControl(PlayerPlayback.class).start();
            }
        }
    }

    @Override
    protected void show() {
        setPreviousScreen(null);
//        baseApplication.showDebuging();

        musicVolumeNode.setLocalScale(0.2f, 0, 0);
        baseApplication.getSoundManager().playMusic("loop");
        baseApplication.getSoundManager().setMusicVolume("loop", 0.2f);

        chaseCam.cleanupWithInput(inputManager);
        mainApplication.getBulletAppState().getPhysicsSpace().addTickListener(this);
        game.start(player);
        keyboardControlInputListener.registerWithInput(inputManager);

        if (played) {
            showStartAction();
            
            
        } else {
            played = true;
            showMenuAction();
        }
        

//        doStartGameAction();
    }

    private void showMenuAction() {
        logo.show();
        retryButton.hide();
        playButton.hide();
        exitButton.hide();
        playButton.show();
        exitButton.show();
        playButton.scaleFromTo(0, 0, 1, 1, 1, 0.1f, Bounce.OUT);
        exitButton.scaleFromTo(0, 0, 1, 1, 1, 0.5f, Bounce.OUT);
        readyLabel.hide();
        hideVehicleOptions();
        goLabel.hide();
        messageLabel.hide();

    }

    private void showStartAction() {
        logo.hide();
        retryButton.hide();
        playButton.hide();
        exitButton.hide();

        showVehicleOptions();
        readyLabel.hide();
        goLabel.hide();
        messageLabel.hide();
        showMessage("Choose a vehicle?", false);

    }
    
    private void showRetryAction() {
        logo.hide();
        retryButton.hide();
        playButton.hide();
        exitButton.hide();
        retryButton.show();
        exitButton.show();
        retryButton.scaleFromTo(0, 0, 1, 1, 1, 0.1f, Bounce.OUT);
        exitButton.scaleFromTo(0, 0, 1, 1, 1, 0.5f, Bounce.OUT);
        readyLabel.hide();
        goLabel.hide();
        messageLabel.hide();

    }

    private void showMessage(String text, boolean autohide) {
        messageLabel.show();
        messageLabel.setText(text);
        messageLabel.moveFromToCenter(0, 600, 0, 10, 1, 0.1f, Expo.OUT);

        if (autohide) {
            messageLabel.moveFromToCenter(0, 10, 0, -600, 0.6f, 2f, Linear.INOUT);
        }
    }

    private void doStartGameAction() {
        readyLabel.show();
        readyLabel.setScale(0);
        goLabel.hide();

        readyLabel.scaleFromTo(0, 0, 1, 1, 0.2f, 2, Bounce.OUT, new TweenCallback() {
            @Override
            public void onEvent(int i, BaseTween<?> bt) {
                game.getBaseApplication().getSoundManager().playSound("timer");

                readyLabel.fadeFromTo(1, 0, 0.25f, 0.2f, new TweenCallback() {
                    @Override
                    public void onEvent(int i, BaseTween<?> bt) {
                        readyLabel.scaleFromTo(0, 0, 1, 1, 0.2f, 0.5f, Bounce.OUT, new TweenCallback() {
                            @Override
                            public void onEvent(int i, BaseTween<?> bt) {
                                game.getBaseApplication().getSoundManager().playSound("timer");
                                goLabel.show();
                                goLabel.setScale(0);
                                readyLabel.fadeFromTo(1, 0, 0.5f, 0);
                                startAllSnapshots();
                            }
                        });
                    }
                });

            }
        });

        goLabel.scaleFromTo(0, 0, 1, 1, 0.2f, 4.5f, Bounce.OUT, new TweenCallback() {
            @Override
            public void onEvent(int i, BaseTween<?> bt) {
                game.getBaseApplication().getSoundManager().playSound("go");
                activatePlayer();

                goLabel.fadeFromTo(1, 0, 0.5f, 0.2f, new TweenCallback() {
                    @Override
                    public void onEvent(int i, BaseTween<?> bt) {

                    }
                });
            }
        });
    }

    @Override
    protected void exit() {
        chaseCam.cleanupWithInput(inputManager);
        camera.setLocation(new Vector3f(0, 0, 0));
        camera.setRotation(new Quaternion());
        baseApplication.getStateManager().detach(atmosphereState);
        baseApplication.getStateManager().detach(weatherState);
//        baseApplication.getViewPort().removeProcessor(dlsr);
        baseApplication.getViewPort().removeProcessor(fpp);
        mainApplication.getBulletAppState().getPhysicsSpace().removeTickListener(this);
        game.close();
        baseApplication.getMessageManager().removeMessageListener(this);

    }

    @Override
    protected void pause() {
    }

    @Override
    public void doGameOver() {
        playerRecorder.stop();
        RecordingSnapshot snapshot = new RecordingSnapshot();
        snapshot.setRetryNumber(snapShotList.size() + 1);
        snapshot.setEntries(playerRecorder.getEntries());
        snapshot.setVehicle(player.getModel().clone(true));
        SceneGraphVisitor sgv = new SceneGraphVisitor() {
            @Override
            public void visit(Spatial spatial) {
                if (spatial.getName() != null && spatial.getName().equals("trailnode")) {
                    log("Remove trail effect");
                    spatial.removeFromParent();
                }
            }
        };
        snapshot.getVehicle().depthFirstTraversal(sgv);
        SpatialUtils.updateSpatialTransparency(snapshot.getVehicle(), true, 0.25f);

        snapShotList.add(snapshot);
        showRetryAction();
        fadeMusicOut();

        int score = baseApplication.getGameSaves().getGameData().getScore();
        score = player.getScore() + score;
        baseApplication.getGameSaves().getGameData().setScore(score);
        baseApplication.getGameSaves().save();

    }

    @Override
    public void doGameCompleted() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void doScoreChanged(int score) {
        lapLabel.setText("LAPS: " + player.getLapCount());
        lootLabel.setText("x " + player.getScore());
//        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void doCollisionPlayerWithStatic(Spatial collided, Spatial collider) {
        log("Collision with static");
//        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void doCollisionPlayerWithPickup(Spatial collided, Spatial collider) {
        if (collided.getControl(PickupControl.class
        ) != null) {
            collided.getControl(PickupControl.class
            ).doPickup();

        }
    }

    @Override
    public void doCollisionPlayerWithEnemy(Spatial collided, Spatial collider) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void doCollisionPlayerWithBullet(Spatial collided, Spatial collider) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void doCollisionObstacleWithBullet(Spatial collided, Spatial collider) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void doCollisionEnemyWithBullet(Spatial collided, Spatial collider) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void doCollisionEnemyWithEnemy(Spatial collided, Spatial collider) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void doCollisionPlayerWithObstacle(Spatial collided, Spatial collider) {
        player.setCrashed(true);
        gameover = true;
//        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void doCollisionEnemyWithObstacle(Spatial collided, Spatial collider) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void onKey(KeyboardControlEvent keyboardControlEvent, float fps) {

//        if (keyboardControlEvent.isUp()) {
//            player.setAccelarate(keyboardControlEvent.isKeyDown());
//        }
        if (keyboardControlEvent.isDown()) {
            player.setBrake(keyboardControlEvent.isKeyDown());
            if (!keyboardControlEvent.isKeyDown()) {
                player.setTargetSpeed(player.getVehicleSpeed(currentVehicle));
            }
        }

        if (keyboardControlEvent.isLeft()) {
            player.setLeft(keyboardControlEvent.isKeyDown());
        }

        if (keyboardControlEvent.isRight()) {
            player.setRight(keyboardControlEvent.isKeyDown());
        }
    }

    @Override
    public void update(float tpf) {
        if (isActive()) {

//            if (cameraNode != null) {
//                cameraNode.setLocalTranslation(cameraNode.getLocalTranslation().interpolateLocal(player.getFollowNode().getWorldTranslation(), 0.08f));
//                cameraNode.getLocalRotation().slerp(player.getFollowNode().getWorldRotation(), 0.08f);
//            }
            if (game.isStarted() && !game.isGameover() && !game.isPaused()) {

                if (gameover) {
                    player.doDamage(10);
                    gameover = false;
                }

            }

            if (barrelBlurFilter != null) {
                if (player.getSpeed() > 8f) {
                    barrelBlurFilter.setAmount((player.getSpeed() - 8) * 0.2f);

                } else {
                    barrelBlurFilter.setAmount(0f);
                }
            }

            if (atmosphereState != null) {
                atmosphereState.setLocation(camera.getLocation());
            }

//            log("musicVolumeNode.getLocalScale().x = " + musicVolumeNode.getLocalScale().x);
            baseApplication.getSoundManager().setMusicVolume("loop", musicVolumeNode.getLocalScale().x);
        }
    }

    private void showVehicleOptions() {
        vehicleButton1.moveFromToCenter(-192, -500, -192, -280, 0.5f, 0, Bounce.OUT);
        vehicleButton2.moveFromToCenter(-64, -500, -64, -280, 1f, 0, Bounce.OUT);
        vehicleButton3.moveFromToCenter(64, -500, 64, -280, 1.5f, 0, Bounce.OUT);
        vehicleButton4.moveFromToCenter(192, -500, 192, -280, 2f, 0, Bounce.OUT);

        vehicleButton2.setEnabled(false);
        vehicleButton3.setEnabled(false);
        vehicleButton4.setEnabled(false);

        if (player.getScore() >= 2000) {
            vehicleButton2.setEnabled(true);
        }

        if (player.getScore() >= 3000) {
            vehicleButton3.setEnabled(true);
        }

        if (player.getScore() >= 5000) {
            vehicleButton4.setEnabled(true);
        }
    }

    private void hideVehicleOptions() {
        vehicleButton1.moveFromToCenter(-192, -280, -192, -580, 0.5f, 0.02f, Linear.INOUT);
        vehicleButton2.moveFromToCenter(-64, -280, -64, -580, 0.5f, 0.02f, Linear.INOUT);
        vehicleButton3.moveFromToCenter(64, -280, 64, -580, 0.5f, 0.02f, Linear.INOUT);
        vehicleButton4.moveFromToCenter(192, -280, 192, -580, 0.5f, 0.02f, Linear.INOUT);
    }

    private void fadeMusicIn() {
        Tween.to(musicVolumeNode, SpatialAccessor.SCALE_XYZ, 3)
                .target(0.5f, 0, 0)
                .delay(0)
                .start(baseApplication.getTweenManager());

    }

    private void fadeMusicOut() {
        Tween.to(musicVolumeNode, SpatialAccessor.SCALE_XYZ, 2)
                .target(0.2f, 0, 0)
                .delay(0)
                .start(baseApplication.getTweenManager());

    }

    @Override
    public void prePhysicsTick(PhysicsSpace space, float tpf) {

    }

    @Override
    public void physicsTick(PhysicsSpace space, float tpf) {
        if (game.isStarted() && !game.isGameover() && !game.isPaused()) {
            game.getPlayerTracker().getControl(RigidBodyControl.class
            ).setPhysicsLocation(player.getModel().getWorldTranslation().add(0, 0.2f, 0));
            game
                    .getPlayerTracker().getControl(RigidBodyControl.class
                    ).setPhysicsRotation(player.getModel().getWorldRotation().clone());

        }
    }

    @Override
    public void messageReceived(String message, Object object) {
        if (message.equals("lap")) {
//            if (currentVehicle.equals("truck1")) {
//                currentVehicle = "buggy1";
//                player.activate(currentVehicle);
//
//            } else if (currentVehicle.equals("buggy1")) {
//                currentVehicle = "car1";
//                player.activate(currentVehicle);
//
//            } else if (currentVehicle.equals("car1")) {
//                currentVehicle = "ship1";
//                player.activate(currentVehicle);
//
//            }

            showMessage("Lap: " + player.getLapCount(), true);

            baseApplication.getSoundManager().playSound("coin");
            baseApplication.getSoundManager().playSound("upgrade");

        } else if (message.equals("score")) {
            log("Score");

        }
    }

}
