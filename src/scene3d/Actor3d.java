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

import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.scenes.scene2d.EventListener;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.DelayedRemovalArray;

public class Actor3d extends ModelInstance{
	private Stage3d stage3d;
	private Group3d parent;
	
	private final DelayedRemovalArray<EventListener> listeners = new DelayedRemovalArray<EventListener>(0);
	private final Array<Action3d> actions3d = new Array<Action3d>(0);
	
	private String name;
	private boolean visible = true;
	
	float x, y, z;
	float width, height, depth;
	float originX, originY, originZ;
	float scaleX = 1, scaleY = 1, scaleZ = 1;
	float rotation = 0;
	
	BoundingBox boundBox;
	
	public Actor3d(){
		super(new Model());
		setPosition(0,0,0);
		setOrigin(0,0,0);
	}
	
	public Actor3d(Model model){
		super(model);
		setPosition(0,0,0);
		boundBox = model.meshes.get(0).calculateBoundingBox();
		setOrigin(boundBox.max.x, boundBox.max.y, boundBox.max.z);
	}
	
	public Actor3d(Model model, float x, float y, float z){
		super(model);
		setPosition(x,y,z);
		boundBox = model.meshes.get(0).calculateBoundingBox();
		setOrigin(boundBox.max.x, boundBox.max.y, boundBox.max.z);
	}
	
	/** Updates the actor3d based on time. Typically this is called each frame by {@link Stage3d#act(float)}.
	 * <p>
	 * The default implementation calls {@link Action3d#act(float)} on each action and removes actions that are complete.
	 * @param delta Time in seconds since the last frame. */
	public void act (float delta) {
		for (int i = 0; i < actions3d.size; i++) {
			Action3d action3d = actions3d.get(i);
			if (action3d.act(delta) && i < actions3d.size) {
				actions3d.removeIndex(i);
				action3d.setActor3d(null);
				i--;
			}
		}
	}
	
	public Actor3d hit (float x, float y) {
		return null;
	}

	/** Removes this actor3d from its parent, if it has a parent.
	 * @see Group#removeActor(Actor) */
	public boolean remove () {
		if (parent != null) return parent.removeActor(this);
		return false;
	}
	
	/** Add a listener to receive events that {@link #hit(float, float, boolean) hit} this actor3d. See {@link #fire(Event)}.
	 * 
	 * @see InputListener
	 * @see ClickListener */
	public boolean addListener(EventListener listener) {
		if (!listeners.contains(listener, true)) {
			listeners.add(listener);
			return true;
		}
		return false;
	}

	public boolean removeListener (EventListener listener) {
		return listeners.removeValue(listener, true);
	}

	public Array<EventListener> getListeners () {
		return listeners;
	}
	
	public void addAction3d (Action3d action) {
		action.setActor3d(this);
		actions3d.add(action);
	}

	public void removeAction3d (Action3d action) {
		if (actions3d.removeValue(action, true)) action.setActor3d(null);
	}

	public Array<Action3d> getActions3d () {
		return actions3d;
	}

	/** Removes all actions on this actor3d. */
	public void clearActions3d () {
		for (int i = actions3d.size - 1; i >= 0; i--)
			actions3d.get(i).setActor3d(null);
		actions3d.clear();
	}

	/** Removes all listeners on this actor3d. */
	public void clearListeners () {
		listeners.clear();
	}

	/** Removes all actions and listeners on this actor3d. */
	public void clear () {
		clearActions3d();
		clearListeners();
	}
	
	/** Called by the framework when this actor3d or any parent is added to a group that is in the stage3d.
	 * @param stage3d May be null if the actor3d or any parent is no longer in a stage. */
	protected void setStage3d(Stage3d stage3d) {
		this.stage3d = stage3d;
	}

	/** Returns the stage3d that this actor3d is currently in, or null if not in a stage. */
	public Stage3d getStage3d() {
		return stage3d;
	}
	
	/** Returns true if this actor3d is the same as or is the descendant of the specified actor3d. */
	public boolean isDescendantOf (Actor3d actor3d) {
		if (actor3d == null) throw new IllegalArgumentException("actor3d cannot be null.");
		Actor3d parent = this;
		while (true) {
			if (parent == null) return false;
			if (parent == actor3d) return true;
			parent = parent.parent;
		}
	}

	/** Returns true if this actor3d is the same as or is the ascendant of the specified actor3d. */
	public boolean isAscendantOf (Actor3d actor3d) {
		if (actor3d == null) throw new IllegalArgumentException("actor3d cannot be null.");
		while (true) {
			if (actor3d == null) return false;
			if (actor3d == this) return true;
			actor3d = actor3d.parent;
		}
	}

	/** Returns true if the actor3d's parent is not null. */
	public boolean hasParent () {
		return parent != null;
	}
	
	/** Returns the parent actor3d, or null if not in a stage. */
	public Group3d getParent () {
		return parent;
	
	}
	
	/** Called by the framework when an actor3d is added to or removed from a group.
	 * @param parent May be null if the actor3d has been removed from the parent. */
	protected void setParent (Group3d parent) {
		this.parent = parent;
	}
	
	public boolean isVisible () {
		return visible;
	
	}
	/** If false, the actor3d will not be drawn and will not receive touch events. Default is true. */
	public void setVisible (boolean visible) {
		this.visible = visible;
	}

	/* Sets the originx , originy and originz used for rotation */
	public void setOrigin (float originX, float originY, float originZ) {
		this.originX = originX;
		this.originY = originY;
		this.originZ = originZ;
	}
	
	public void setPosition(float x, float y, float z) {
		this.x = x;
		this.y = y;
		this.z = z;
		transform.setToTranslationAndScaling(this.x, this.y, this.z, scaleX, scaleY, scaleZ);
	}
	
	public void translate(float x, float y, float z) {
		this.x += x;
		this.y += y;
		this.z += z;
		transform.setToTranslationAndScaling(this.x, this.y, this.z, scaleX, scaleY, scaleZ);
	}
	
	public void setRotation(float degrees) {
		this.rotation = degrees;
		Matrix4 mat = new Matrix4();
		this.rotate(degrees);
		transform.setToRotation(originX, originY, originZ, rotation);
	}
	
	public void rotate (float degrees) {
		rotation += degrees;
		setPosition(x, y, z);
		transform.setToRotation(originX, originY, originZ, rotation);
	}
	
	public void setScale(float scaleX, float scaleY, float scaleZ) {
		this.scaleX = scaleX;
		this.scaleY = scaleY;
		this.scaleZ = scaleZ;
		transform.scale(scaleX, scaleY, scaleZ);
	}
	
	public void setScale(float scale) {
		this.scaleX = scale;
		this.scaleY = scale;
		this.scaleZ = scale;
		transform.scale(scaleX, scaleY, scaleZ);
	}
	
	/** Adds the specified scale to the current scale. */
	public void scale(float scale) {
		scaleX += scale;
		scaleY += scale;
		scaleZ += scale;
		transform.scl(scale);
	}
	
	public void scale(float scaleX, float scaleY, float scaleZ) {
		this.scaleX += scaleX;
		this.scaleY += scaleY;
		this.scaleZ += scaleZ;
		transform.scl(scaleX, scaleY, scaleZ);
	}
	
	
	public void setX (float x) {
		this.x = x;transform.setToTranslation(x, y, z);
	}
	
	public float getX () {
		return x;
	}
	
	public void setY (float y) {
		this.y = y;transform.setToTranslation(x, y, z);
	}
	
	public float getY () {
		return y;
	}
	
	public void setZ (float z) {
		this.z = z;transform.setToTranslation(x, y, z);
	}
	
	public float getZ (){
		return z;
	}
	
	public void setOriginX (float originX) {
		this.originX = originX;
	}
	
	public float getOriginX () {
		return originX;
	}
	
	public void setOriginY (float originY) {
		this.originY = originY;
	}
	
	public float getOriginY () {
		return originY;
	}
	
	public void setOriginZ (float originZ) {
		this.originZ = originZ;
	}
	
	public float getOriginZ () {
		return originZ;
	}
	
	public void setScaleX (float scaleX) {
		this.scaleX = scaleX;
		transform.scale(scaleX, scaleY, scaleZ);
	}
	
	public float getScaleX () {
		return scaleX;
	}
	
	public float getRotation () {
		return rotation;
	}
	
	public void setScaleY (float scaleY) {
		this.scaleY = scaleY;
		transform.scale(scaleX, scaleY, scaleZ);
	}
	
	public float getScaleY () {
		return scaleY;
	}
	
	public void setScaleZ (float scaleZ) {
		this.scaleY = scaleZ;
		transform.scale(scaleX, scaleY, scaleZ);
	}
	
	public float getScaleZ () {
		return scaleZ;
	}

	/** Sets a name for easier identification of the actor3d in application code.
	 * @see Group#findActor(String) */
	public void setName (String name) {
		this.name = name;
	
	}
	public String getName () {
		return name;
	}
	
	public void setWidth (float width) {
		//for(Mesh m: model.meshes)
		//	m.scale(width, arg1, arg2);
		this.width = width;
	}

	public void setHeight (float height) {
		this.height = height;
	}
	
	public float getWidth () {
		return width;
	}
	public float getHeight () {
		return height;
	}
	public float getDepth () {
		return depth;
	}


	/** Sets the width and height. */
	public void setSize (float width, float height) {
	}

	/** Adds the specified size to the current size. */
	public void size (float size) {
		width += size;
		height += size;
	}

	/** Adds the specified size to the current size. */
	public void size (float width, float height) {
		this.width += width;
		this.height += height;
	}
	
	public String toString () {
		String name = this.name;
		if (name == null) {
			name = getClass().getName();
			int dotIndex = name.lastIndexOf('.');
			if (dotIndex != -1) name = name.substring(dotIndex + 1);
		}
		return name;
	}
}
