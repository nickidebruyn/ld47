/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package za.co.bruynhuis.ld47.game;

import aurelienribon.tweenengine.BaseTween;
import aurelienribon.tweenengine.TweenCallback;
import com.bruynhuis.galago.control.RotationControl;
import com.bruynhuis.galago.control.effects.LineControl;
import com.bruynhuis.galago.control.effects.TrailControl;
import com.bruynhuis.galago.games.physics.PhysicsGame;
import com.bruynhuis.galago.games.physics.PhysicsPlayer;
import com.bruynhuis.galago.util.SpatialUtils;
import com.jme3.light.PointLight;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Vector3f;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.renderer.queue.RenderQueue;
import com.jme3.scene.Geometry;
import com.jme3.scene.LightNode;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.control.AbstractControl;
import com.jme3.scene.control.LightControl;
import java.util.ArrayList;
import java.util.Stack;

/**
 *
 * @author NideBruyn
 */
public class Player extends PhysicsPlayer {

    private Node model;
    private Node followNode;
    private boolean accelarate = false;
    private boolean left = false;
    private boolean right = false;
    private Stack<Vector3f> waypoints;
    private float waypointReachDistance = 0.55f;
    private Vector3f direction;
    private Vector3f nextPosition = new Vector3f(0, 0, 0);
    private float speed = 0;
    private float accel = 0.1f;
    private float targetSpeed = 0.25f;
    private float maxSpeed = 15;
    private float minSpeed = 5;
    private float turnSpeed = 0.085f;
    private Node tempNode;
    private int lapCount = 1;
    private boolean gameEnded = false;
    private float playerHeight = 0.2f;
    private float sideMoveSpeed = 2;
    private boolean crashed = false;
    private boolean turning = false;
    private float turnSpacing = 1.2f;
    private float turnTime = 0.6f;
    private boolean brake = false;
    private PointLight pointLight;
    private LightNode lightNode;
    private ArrayList<RotationControl> wheels = new ArrayList<>();
    private String vehicleType = "car1";

    public Player(PhysicsGame physicsGame, String vehicle) {
        super(physicsGame);
        vehicleType = vehicle;
    }

    @Override
    protected void init() {

        model = (Node) game.getBaseApplication().getAssetManager().loadModel("Models/player/" + vehicleType + ".j3o");
        playerNode.attachChild(model);
        model.move(0, playerHeight, 0);
        model.setShadowMode(RenderQueue.ShadowMode.Cast);

        for (int i = 0; i < model.getQuantity(); i++) {
            Spatial s = model.getChild(i);
            parseBodyParts(s);

        }

        pointLight = new PointLight(new Vector3f(0, 1, 0), ColorRGBA.White.mult(2f), 6);
        game.getRootNode().addLight(pointLight);

        lightNode = new LightNode("headlight", new LightControl(pointLight));
        lightNode.move(0, 2f, 2f);
//        SpatialUtils.rotateTo(lightNode, -85, 0, 0);
        model.attachChild(lightNode);

        followNode = new Node("follow-node");
        playerNode.attachChild(followNode);
        followNode.move(0, 3f, -3);
        followNode.rotate(20f * FastMath.DEG_TO_RAD, 0, 0);

        tempNode = new Node();

        waypoints = ((Game) game).getCopyOfWaypoints();
        playerNode.setLocalTranslation(waypoints.peek());
        playerNode.lookAt(waypoints.get(waypoints.size() - 2), Vector3f.UNIT_Y);

        playerNode.addControl(new AbstractControl() {
            @Override
            protected void controlUpdate(float tpf) {

                if ((game.isStarted() && !game.isPaused()) || gameEnded) {

                    //Adjust the speed
                    if (speed < targetSpeed) {
                        speed += accel;

                    } else if (speed > targetSpeed) {
                        speed -= accel;

                    }

                    if (gameEnded && waypoints.isEmpty()) {
                        waypoints = ((Game) game).getCopyOfWaypoints();
                    }

                    if (!crashed) {
                        if (left) {
                            log("Turn left clicked");
                            turnLeft();
//                            model.move(tpf*2, 0, 0);
                        } else if (right) {
                            log("Turn right clicked");
                            turnRight();
//                            model.move(-tpf*2, 0, 0);
                        }

                        if (brake) {
                            decreaseSpeed();
                        } else if (accelarate) {
                            increaseSpeed();
                        }

                        for (int i = 0; i < wheels.size(); i++) {
                            RotationControl control = wheels.get(i);
                            if (control.getRotator().x < 0) {
                                control.getRotator().setX(-100 * speed);
                            } else {
                                control.getRotator().setX(100 * speed);
                            }

                        }

                        game.getBaseApplication().getSoundManager().setMusicSpeed("car", 0.5f + (speed * 0.05f));
                    }

                    movePlayer(tpf);

                }

            }

            @Override
            protected void controlRender(RenderManager rm, ViewPort vp) {

            }
        });

    }

    public void setTargetSpeed(float targetSpeed) {
        this.targetSpeed = targetSpeed;
    }

    private void movePlayer(float tpf) {
        if (getPosition().distance(waypoints.peek()) > waypointReachDistance) {

            tempNode.setLocalTranslation(playerNode.getLocalTranslation().clone());
            tempNode.setLocalRotation(playerNode.getLocalRotation().clone());
            tempNode.lookAt(waypoints.peek(), Vector3f.UNIT_Y);

            playerNode.getLocalRotation().slerp(tempNode.getLocalRotation(), turnSpeed);

            //Move in the direction the player is facing
            direction = playerNode.getLocalRotation().getRotationColumn(2).normalize();
            playerNode.move(direction.mult(tpf * speed));

        } else {
//            Debug.log("Waypoint reached");
            waypoints.pop();

            if (waypoints.isEmpty()) {
                waypoints = ((Game) game).getCopyOfWaypoints();
                lapCount++;
                addScore(1000);
                game.getBaseApplication().getMessageManager().sendMessage("lap", null);

            }

        }
    }

    public void increaseSpeed() {
        this.targetSpeed += 0.1f;
        if (this.targetSpeed > this.maxSpeed) {
            this.targetSpeed = this.maxSpeed;
        }
    }

    public void decreaseSpeed() {
        this.targetSpeed -= 0.25f;
        if (this.targetSpeed < this.minSpeed) {
            this.targetSpeed = this.minSpeed;
        }
    }

    @Override
    public Vector3f getPosition() {
        return playerNode.getWorldTranslation();
    }

    @Override
    public void doDie() {
        game.getBaseApplication().getSoundManager().stopMusic("car");
        game.getBaseApplication().getSoundManager().playSound("crash");
        game.getBaseApplication().getEffectManager().doEffect(vehicleType, model.getWorldTranslation());
        model.removeFromParent();

    }

    @Override
    public void close() {
        game.getRootNode().removeLight(pointLight);
        super.close(); //To change body of generated methods, choose Tools | Templates.

    }

    private void loadTrail(float offsetX) {
        Material trailMat = game.getBaseApplication().getAssetManager().loadMaterial("Materials/trailline.j3m");

        Geometry trailGeometry = new Geometry();
//        trailMat.getAdditionalRenderState().setAlphaTest(true);
//        trailMat.getAdditionalRenderState().setAlphaFallOff(0.5f);
        trailGeometry.setMaterial(trailMat);
        //rootNode.attachChild(trail);  // either attach the trail geometry node to the root…
        trailGeometry.setIgnoreTransform(true); // or set ignore transform to true. this should be most useful when attaching nodes in the editor
        //trailGeometry.setQueueBucket(RenderQueue.Bucket.Translucent);

        Node trailNode = new Node("trailnode");
        trailNode.move(offsetX, -0.12f, -0.3f);
        model.attachChild(trailNode);

        LineControl line = new LineControl(new LineControl.Algo2CamPosBBNormalized(), true);
        trailGeometry.addControl(line);
        TrailControl trailControl = new TrailControl(line);
        trailNode.addControl(trailControl);
        trailControl.setStartWidth(0.15f);
        trailControl.setEndWidth(0.15f);
        trailControl.setLifeSpan(0.1f);
//        trailControl.setSegmentLength(0.1f);

        trailNode.attachChild(trailGeometry);
        trailGeometry.setQueueBucket(RenderQueue.Bucket.Transparent);
    }

    @Override
    public void start() {

        game.getBaseApplication().getSoundManager().setMusicVolume("car", 1f);
        game.getBaseApplication().getSoundManager().playMusic("car");

        game.getBaseApplication().getSoundManager().setMusicSpeed("car", 0.75f);

    }

    public boolean isAccelarate() {
        return accelarate;
    }

    public void setAccelarate(boolean accelarate) {
        this.accelarate = accelarate;
    }

    public void setBrake(boolean brake) {
        this.brake = brake;
    }

    public boolean isLeft() {
        return left;
    }

    public void setLeft(boolean left) {
        this.left = left;
    }

    public boolean isRight() {
        return right;
    }

    public void setRight(boolean right) {
        this.right = right;
    }

    public Spatial getModel() {
        return model;
    }

    public void turnLeft() {
        log("Car X Pos: " + model.getLocalTranslation().x);

        //Help to block player from driving into the walls.
        if (model.getLocalTranslation().x > 0.1f) {
            if (!turning) {
                game.getBaseApplication().getSoundManager().playSound("blockturn");
            }
            return;
        }

        if (!turning) {
            turning = true;
            SpatialUtils.moveFromToCenter(model, model.getLocalTranslation().x, model.getLocalTranslation().y, model.getLocalTranslation().z,
                    model.getLocalTranslation().x + turnSpacing, model.getLocalTranslation().y, model.getLocalTranslation().z, turnTime, 0, new TweenCallback() {
                @Override
                public void onEvent(int i, BaseTween<?> bt) {
                    log("left turning done");
                    turning = false;
                }
            }).start(game.getBaseApplication().getTweenManager());
            SpatialUtils.rotateFromTo(model, new Vector3f(0, 0, 0), new Vector3f(0, 20, 0), turnTime * 0.5f, 0)
                    .repeatYoyo(1, 0)
                    .start(game.getBaseApplication().getTweenManager());
        }

    }

    public void turnRight() {
        log("Car X Pos: " + model.getLocalTranslation().x);

        //Help to block player from driving into the walls.
        if (model.getLocalTranslation().x < -0.1f) {
            if (!turning) {
                game.getBaseApplication().getSoundManager().playSound("blockturn");
            }
            return;
        }

        if (!turning) {
            turning = true;
            SpatialUtils.moveFromToCenter(model, model.getLocalTranslation().x, model.getLocalTranslation().y, model.getLocalTranslation().z,
                    model.getLocalTranslation().x - turnSpacing, model.getLocalTranslation().y, model.getLocalTranslation().z, turnTime, 0, new TweenCallback() {
                @Override
                public void onEvent(int i, BaseTween<?> bt) {
                    log("right turning done");
                    turning = false;
                }
            }).start(game.getBaseApplication().getTweenManager());
            SpatialUtils.rotateFromTo(model, new Vector3f(0, 0, 0), new Vector3f(0, -20, 0), turnTime * 0.5f, 0)
                    .repeatYoyo(1, 0)
                    .start(game.getBaseApplication().getTweenManager());
        }
    }

    public void setCrashed(boolean crashed) {
        this.crashed = crashed;
    }

    public Node getFollowNode() {
        return followNode;
    }

    public float getVehicleSpeed(String name) {
        float speed = 8;
        if (name.equals("buggy1")) {
            speed = 9;
        } else if (name.equals("car1")) {
            speed = 10;
        } else if (name.equals("ship1")) {
            speed = 11;
        }

        return speed;

    }

    private float getVehicleTurnTime(String name) {
        float speed = 0.7f;
        if (name.equals("buggy1")) {
            speed = 0.6f;
        } else if (name.equals("car1")) {
            speed = 0.55f;
        } else if (name.equals("ship1")) {
            speed = 0.5f;
        }

        return speed;

    }

    public float getSpeed() {
        return speed;
    }

    public int getLapCount() {
        return lapCount;
    }

    public void updateModel(String name) {
        Node n = (Node) game.getBaseApplication().getAssetManager().loadModel("Models/player/" + name + ".j3o");
//        log(name + "Parts = " + n.getQuantity());
        model.detachAllChildren();
        model.attachChild(n);
        model.attachChild(lightNode);

        for (int i = 0; i < n.getQuantity(); i++) {
            Spatial s = n.getChild(i);
            parseBodyParts(s);

        }

        loadTrail(0.25f);
        loadTrail(-0.25f);
    }

    public void activate(String name) {
        this.vehicleType = name;
        this.targetSpeed = getVehicleSpeed(name);
        this.turnTime = getVehicleTurnTime(name);
        updateModel(name);

    }

    private void parseBodyParts(Spatial s) {
        if (s.getUserData("type") != null && s.getUserData("type").equals("wheel")) {
            RotationControl control = new RotationControl(new Vector3f(100, 0, 0));
            s.addControl(control);
            wheels.add(control);
        } else if (s.getUserData("type") != null && s.getUserData("type").equals("wheel-left")) {
            RotationControl control = new RotationControl(new Vector3f(-100, 0, 0));
            s.addControl(control);
            wheels.add(control);
        }
    }

    public void setScore(int score) {
        this.score = score;
    }

}
