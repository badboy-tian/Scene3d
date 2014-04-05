/*******************************************************************************
 * Copyright 2011 See AUTHORS file.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package scene3d;

import java.util.Iterator;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight;
import com.badlogic.gdx.graphics.g3d.model.MeshPart;
import com.badlogic.gdx.graphics.g3d.model.Node;
import com.badlogic.gdx.graphics.g3d.model.NodePart;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.math.collision.Ray;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.EventListener;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.SnapshotArray;

public class Stage3d extends InputAdapter implements Disposable {
	private float viewportX, viewportY, viewportWidth, viewportHeight;
	private float width, height;
	private float gutterWidth, gutterHeight;
	private final ModelBatch modelBatch;
	private Environment environment;

	private PerspectiveCamera camera;

	private final Group3d root;
	private Actor3d scrollFocus;
	private Actor3d keyboardFocus;
	
	public Touchable touchable = Touchable.disabled;
	private int selecting = -1;
	private Actor3d selectedActor;
	private Material selectionMaterial;
    private Material originalMaterial;
    
    private boolean canHit = false;


	/** Creates a stage with a {@link #setViewport(float, float, boolean) viewport} equal to the device screen resolution. The stage
	 * will use its own {@link SpriteBatch}. */
	public Stage3d () {
		this(Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), false);
	}

	/** Creates a stage with the specified {@link #setViewport(float, float, boolean) viewport} that doesn't keep the aspect ratio.
	 * The stage will use its own {@link SpriteBatch}, which will be disposed when the stage is disposed. */
	public Stage3d (float width, float height) {
		this(width, height, false);
	}

	public Stage3d (float width, float height, boolean keepAspectRatio) {
		this.width = width;
		this.height = height;

		root = new Group3d();
		root.setStage3d(this);

		modelBatch = new ModelBatch();

		camera =  new PerspectiveCamera(67, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		camera.position.set(10f, 10f, 10f);
		camera.lookAt(0,0,0);
		camera.near = 0.1f;
		camera.far = 300f;
		camera.update();
		environment = new Environment();
		environment.set(new ColorAttribute(ColorAttribute.AmbientLight, 0.9f, 0.9f, 0.9f, 1f));
		environment.add(new DirectionalLight().set(0.8f, 0f, 0f, -1f, -0.8f, -0.2f));

		setViewport(width, height, keepAspectRatio);
	    selectionMaterial = new Material();
	    selectionMaterial.set(ColorAttribute.createDiffuse(Color.ORANGE));
	    originalMaterial = new Material();
	}
	
	public Stage3d (float width, float height, PerspectiveCamera camera) {
		this.width = width;
		this.height = height;
		root = new Group3d();
		root.setStage3d(this);
		modelBatch = new ModelBatch();
		this.camera = camera;
	}
	
	public Stage3d (float width, float height, PerspectiveCamera camera, Environment environment) {
		this.width = width;
		this.height = height;
		root = new Group3d();
		root.setStage3d(this);
		modelBatch = new ModelBatch();
		this.camera = camera;
		this.environment = environment;
	}


	public void setViewport (float width, float height) {
		setViewport(width, height, false, 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
	}

	/** Sets up the stage size using a viewport that fills the entire screen.
	 * @see #setViewport(float, float, boolean, float, float, float, float) */
	public void setViewport (float width, float height, boolean keepAspectRatio) {
		setViewport(width, height, keepAspectRatio, 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
	}

	/** Sets up the stage size and viewport. The viewport is the glViewport position and size, which is the portion of the screen
	 * used by the stage. The stage size determines the units used within the stage, depending on keepAspectRatio:
	 * <p>
	 * If keepAspectRatio is false, the stage is stretched to fill the viewport, which may distort the aspect ratio.
	 * <p>
	 * If keepAspectRatio is true, the stage is first scaled to fit the viewport in the longest dimension. Next the shorter
	 * dimension is lengthened to fill the viewport, which keeps the aspect ratio from changing. The {@link #getGutterWidth()} and
	 * {@link #getGutterHeight()} provide access to the amount that was lengthened.
	 * @param viewportX The top left corner of the viewport in glViewport coordinates (the origin is bottom left).
	 * @param viewportY The top left corner of the viewport in glViewport coordinates (the origin is bottom left).
	 * @param viewportWidth The width of the viewport in pixels.
	 * @param viewportHeight The height of the viewport in pixels. */
	public void setViewport (float stageWidth, float stageHeight, boolean keepAspectRatio, float viewportX, float viewportY,
			float viewportWidth, float viewportHeight) {
		this.viewportX = viewportX;
		this.viewportY = viewportY;
		this.viewportWidth = viewportWidth;
		this.viewportHeight = viewportHeight;
		if (keepAspectRatio) {
			if (viewportHeight / viewportWidth < stageHeight / stageWidth) {
				float toViewportSpace = viewportHeight / stageHeight;
				float toStageSpace = stageHeight / viewportHeight;
				float deviceWidth = stageWidth * toViewportSpace;
				float lengthen = (viewportWidth - deviceWidth) * toStageSpace;
				this.width = stageWidth + lengthen;
				this.height = stageHeight;
				gutterWidth = lengthen / 2;
				gutterHeight = 0;
			} else {
				float toViewportSpace = viewportWidth / stageWidth;
				float toStageSpace = stageWidth / viewportWidth;
				float deviceHeight = stageHeight * toViewportSpace;
				float lengthen = (viewportHeight - deviceHeight) * toStageSpace;
				this.height = stageHeight + lengthen;
				this.width = stageWidth;
				gutterWidth = 0;
				gutterHeight = lengthen / 2;
			}
		} else {
			this.width = stageWidth;
			this.height = stageHeight;
			gutterWidth = 0;
			gutterHeight = 0;
		}

		camera.viewportWidth = this.width;
		camera.viewportHeight = this.height;
	}

	public void draw(){ 
		camera.update();
		if (!root.isVisible()) return;
		modelBatch.begin(camera);
		root.draw(modelBatch, environment);
		modelBatch.end();
	}

	/** Calls {@link #act(float)} with {@link Graphics#getDeltaTime()}. */
	public void act () {
		act(Math.min(Gdx.graphics.getDeltaTime(), 1 / 30f));
	}

	/** Calls the {@link Actor#act(float)} method on each actor in the stage. Typically called each frame. This method also fires
	 * enter and exit events.
	 * @param delta Time in seconds since the last frame. */
	public void act(float delta) {
		updateController(delta);
		root.act(delta);
	}

	/** Adds an actor to the root of the stage.
	 * @see Group#addActor(Actor)
	 * @see Actor#remove() */
	public void addActor3d(Actor3d actor) {
		root.addActor3d(actor);
	}

	/** Adds an action to the root of the stage.
	 * @see Group#addAction3d(Action) */
	public void addAction3d(Action3d action) {
		root.addAction3d(action);
	}

	/** Returns the root's child actors.
	 * @see Group#getChildren() */
	public Array<Actor3d> getActors3d() {
		return root.getChildren();
	}

	/** Adds a listener to the root.
	 * @see Actor#addListener(EventListener) */
	public boolean addListener (Event3dListener listener) {
		return root.addListener(listener);
	}

	/** Removes a listener from the root.
	 * @see Actor#removeListener(EventListener) */
	public boolean removeListener (Event3dListener listener) {
		return root.removeListener(listener);
	}

	/** Removes the root's children, actions, and listeners. */
	public void clear () {
		unfocusAll();
		root.dispose();
		root.clear();
	}

	/** Removes the touch, keyboard, and scroll focused actors. */
	public void unfocusAll () {
		scrollFocus = null;
		keyboardFocus = null;
		//cancelTouchFocus();
	}

	/** Removes the touch, keyboard, and scroll focus for the specified actor and any descendants. */
	public void unfocus(Actor3d actor) {
		if (scrollFocus != null && scrollFocus.isDescendantOf(actor)) scrollFocus = null;
		if (keyboardFocus != null && keyboardFocus.isDescendantOf(actor)) keyboardFocus = null;
	}

	/** Sets the actor that will receive key events.
	 * @param actor May be null. */
	public void setKeyboardFocus (Actor3d actor) {
		if (keyboardFocus == actor) return;
	}

	/** Gets the actor that will receive key events.
	 * @return May be null. */
	public Actor3d getKeyboardFocus () {
		return keyboardFocus;
	}

	/** Sets the actor that will receive scroll events.
	 * @param actor May be null. */
	public void setScrollFocus(Actor3d actor) {
		if (scrollFocus == actor) return;
	}

	/** Gets the actor that will receive scroll events.
	 * @return May be null. */
	public Actor3d getScrollFocus () {
		return scrollFocus;
	}

	public ModelBatch getModelBatch () {
		return modelBatch;
	}

	public PerspectiveCamera getCamera () {
		return camera;
	}

	/** Sets the stage's camera. The camera must be configured properly or {@link #setViewport(float, float, boolean)} can be called
	 * after the camera is set. {@link Stage#draw()} will call {@link Camera#update()} and use the {@link Camera#combined} matrix
	 * for the SpriteBatch {@link SpriteBatch#setProjectionMatrix(com.badlogic.gdx.math.Matrix4) projection matrix}. */
	public void setCamera (PerspectiveCamera camera) {
		this.camera = camera;
	}

	/** Returns the root group which holds all actors in the stage. */
	public Group3d getRoot () {
		return root;
	}
	
	public void setEnvironment(Environment environment){
		this.environment = environment;
	}

	public Environment getEnvironment(){
		return environment;
	}
	
	public void enableHit(){
		canHit = true;
	}
	
	public void disableHit(){
		canHit = false;
	}

	@Override
	public boolean touchDown(int screenX, int screenY, int pointer, int button) {
		if(canHit){
			Actor3d actor3d = getObject(screenX, screenY);
			selecting = actor3d != null?1:-1;
			if(actor3d != null && actor3d.getName() != null)
				Gdx.app.log("", ""+actor3d.getName());
		}
        return selecting > 0;
		//return false;
	}

	@Override
	public boolean touchUp(int screenX, int screenY, int pointer, int button) {
		if (selecting >= 0) {
	         //setSelected(getObject(screenX, screenY));
	         selecting = -1;
	         return true;
	    }
	    return false;
		//if(touchable == Touchable.enabled)
		//	hit(screenX, screenY);
		//return false;
	}
	
	@Override
    public boolean touchDragged (int screenX, int screenY, int pointer) {
        return selecting >= 0;
    }
 
	Vector3 position = new Vector3();
	int result = -1;
    float distance = -1;
    
    public Actor3d getObject(int screenX, int screenY) {
    	 Actor3d temp = null;
    	 SnapshotArray<Actor3d> children = root.getChildren();
    	 Actor3d[] actors = children.begin();
         for(int i = 0, n = children.size; i < n; i++){
        	 temp = hit3d(screenX, screenY, actors[i]);
        	 if(actors[i] instanceof Group3d)
        		 temp = hit3d(screenX, screenY, (Group3d)actors[i]);
         }
         children.end();
         return temp;
    }
    
    public Actor3d hit3d(int screenX, int screenY, Actor3d actor3d) {
        Ray ray = camera.getPickRay(screenX, screenY);
        float distance = -1;
        final float dist2 = actor3d.intersects(ray);
        if (dist2 >= 0f && (distance < 0f || dist2 <= distance)) { 
            distance = dist2;
            return actor3d;
        }
        return null;
    }
    
    public Actor3d hit3d(int screenX, int screenY, Group3d group3d) {
    	 Actor3d temp = null;
    	 SnapshotArray<Actor3d> children = group3d.getChildren();
    	 Actor3d[] actors = children.begin();
         for(int i = 0, n = children.size; i < n; i++){
        	 temp = hit3d(screenX, screenY, actors[i]);
        	 if(actors[i] instanceof Group3d)
        		 temp = hit3d(screenX, screenY, (Group3d)actors[i]);
         }
         children.end();
         return temp;
    }
    
    
 	private float offsetX = 10f, offsetY = 10f, offsetZ = 10f;
 	private float folllowSpeed = 0.5f;
 	private Actor3d followedActor3d;
 	private boolean lookAt;
 	
 	/*
 	 * The camera follows the actor3d as it moves along the scene
 	 * @param actor3d The actor3d the camera has to follow , if it is null the camera stops following
 	 * @param lookAt whether the camera should always be pointing to the actor3d
 	 */
 	public  void followActor3d(Actor3d actor3d, boolean lookAt){
 		followedActor3d = actor3d;
 		this.lookAt = lookAt;
 	}
 	
 	/*
 	 * This sets the distance between the camera and the actor
 	 * @param offX the x distance from actor
 	 * @param offY the y distance from actor
 	 * @param offZ the z distance from actor
 	 */
 	public void followOffset(float offX, float offY, float offZ){
 		offsetX = offX;
 		offsetY = offY;
 		offsetZ = offZ;
 	}
	
    private static float moveDuration;
    private static float moveTime;
    private static boolean moveCompleted;
    private static float moveLastPercent;
    private static float panSpeedX, panSpeedY, panSpeedZ;
    private static float movePercentDelta;
    
    private static float rotateTime;
    private static float rotateDuration;
    private static float rotateYaw, rotatePitch, rotateRoll;
    private static boolean rotateCompleted;
    private static float rotateLastPercent;
    private static float rotatePercentDelta;
    
    public void moveCameraTo(float x, float y, float z, float duration) {
        moveCameraBy(x-camera.position.x, y-camera.position.y, y-camera.position.y, duration);
    }

    public void moveCameraBy(float amountX, float amountY, float amountZ, float duration) {
    	moveDuration = duration;
     	panSpeedX = amountX;
     	panSpeedY = amountY;
     	panSpeedZ = amountZ;
     	moveLastPercent = 0;
     	moveTime = 0;
        moveCompleted = false;
    }
   
    /*
     * rotates the pitch of camera (rotates camera around it's local x-axis) for viewing up and down
     */
    public void rotateCameraBy(float yaw, float pitch, float roll, float duration){
    	rotateLastPercent = 0;
    	rotateTime = 0;
    	rotateYaw = yaw;
    	rotatePitch = pitch;
    	rotateRoll = roll;
    	rotateDuration = duration;
    	rotateCompleted = false;
    }
    
	private void updateController(float delta){
		if(!moveCompleted){
			moveTime += delta;
	        moveCompleted = moveTime >= moveDuration;
	        float percent;
	        if (moveCompleted)
	           percent = 1;
	        else {
	            percent = moveTime / moveDuration;
	        }
	        movePercentDelta = percent - moveLastPercent;
	    	camera.translate(panSpeedX * movePercentDelta, panSpeedY * movePercentDelta, panSpeedZ * movePercentDelta);
	        moveLastPercent = percent;
		}
		if(!rotateCompleted){
			rotateTime += delta;
	    	rotateCompleted = rotateTime >= rotateDuration;
	        float percent;
	        if (rotateCompleted)
	           percent = 1;
	        else 
	            percent = rotateTime / rotateDuration;
	        rotatePercentDelta = percent - rotateLastPercent;
	        camera.rotate(Vector3.Z, rotateYaw * rotatePercentDelta);
	        camera.rotate(Vector3.X, rotatePitch * rotatePercentDelta);
	        camera.rotate(Vector3.Y, rotateRoll * rotatePercentDelta);
	        rotateLastPercent = percent;
		}
		if (followedActor3d != null) { 
			moveCameraTo(followedActor3d.x+offsetX, followedActor3d.y+offsetY, followedActor3d.z+offsetZ, folllowSpeed);
			if(lookAt)
				camera.lookAt(followedActor3d.x, followedActor3d.y, followedActor3d.z);
			/*
			followedActor3d.getTransform().getTranslation(camera.direction);
			current.set(position).sub(camera.direction);
			desired.set(desiredLocation).rot(followedActor3d.getTransform()).add(desiredOffset);
			final float desiredDistance = desired.len();
			if (rotationSpeed < 0)
				current.set(desired).nor().mul(desiredDistance);
			else if (rotationSpeed == 0 || Vector3.tmp.set(current).dst2(desired) < rotationOffsetSq) 
				current.nor().mul(desiredDistance);
			else {
				current.nor();
				desired.nor();
				rotationAxis.set(current).crs(desired);
				float angle = (float)Math.acos(current.dot(desired)) * MathUtils.radiansToDegrees;
				final float maxAngle = rotationSpeed * delta;
				if (Math.abs(angle) > maxAngle) {
					angle = (angle < 0) ? -maxAngle : maxAngle;
				}
				current.rot(rotationMatrix.idt().rotate(rotationAxis, angle));
				current.mul(desiredDistance);
			}

			current.add(camera.direction);
			absoluteSpeed = Math.min(absoluteSpeed + acceleration, current.dst(position) / delta);
			position.add(speed.set(current).sub(position).nor().mul(absoluteSpeed * delta));
			if (bounds.isValid()) {
				if (position.x < bounds.min.x) position.x = bounds.min.x;
				if (position.x > bounds.max.x) position.x = bounds.max.x;
				if (position.y < bounds.min.y) position.y = bounds.min.y;
				if (position.y > bounds.max.y) position.y = bounds.max.y;
				if (position.z < bounds.min.z) position.z = bounds.min.z;
				if (position.z > bounds.max.z) position.z = bounds.max.z;
			}
			if (offsetBounds.isValid()) {
				Vector3.tmp.set(position).sub(camera.direction);
				if (Vector3.tmp.x < offsetBounds.min.x) position.x = offsetBounds.min.x + camera.direction.x;
				if (Vector3.tmp.x > offsetBounds.max.x) position.x = offsetBounds.max.x + camera.direction.x;
				if (Vector3.tmp.y < offsetBounds.min.y) position.y = offsetBounds.min.y + camera.direction.y;
				if (Vector3.tmp.y > offsetBounds.max.y) position.y = offsetBounds.max.y + camera.direction.y;
				if (Vector3.tmp.z < offsetBounds.min.z) position.z = offsetBounds.min.z + camera.direction.z;
				if (Vector3.tmp.z > offsetBounds.max.z) position.z = offsetBounds.max.z + camera.direction.z;
			}
			camera.direction.add(target.set(targetLocation)
			.rot(followedActor3d.getTransform()).add(targetOffset)).sub(position).nor();*/
		}
	}

	@Override
	public void dispose() {
		modelBatch.dispose();
		clear();
	}
}
