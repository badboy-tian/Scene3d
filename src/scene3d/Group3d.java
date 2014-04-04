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

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.SnapshotArray;


public class Group3d extends Actor3d{
	private final SnapshotArray<Actor3d> children = new SnapshotArray<Actor3d>(true, 4, Actor3d.class);
	private final Matrix4 localTransform = new Matrix4();
	private final Matrix4 batchTransform = new Matrix4();
	private final Matrix4 oldBatchTransform = new Matrix4();
	private boolean transform = true;
	public int visibleCount;
	
	public Group3d(){
		super();
	}
	
	public Group3d(Model model){
		super(model);
	}
	
	public void act (float delta) {
        super.act(delta);
        Actor3d[] actors = children.begin();
        for(int i = 0, n = children.size; i < n; i++){
        	actors[i].act(delta);
        }
        children.end();
	}
	
	/** Draws the group and its children. The default implementation calls {@link #applyTransform(Batch, Matrix4)} if needed, then
	 * {@link #drawChildren(Batch, float)}, then {@link #resetTransform(Batch)} if needed. */
	@Override
	public void draw(ModelBatch modelBatch, Environment environment) {
		super.draw(modelBatch, environment);
		//if (transform) applyTransform(batch, computeTransform());
		drawChildren(modelBatch, environment);
		//if (transform) resetTransform(batch);
	}

	
	public void drawChildren(ModelBatch modelBatch, Environment environment){
	     //modelBatch.render(children, environment); maybe faster 
	     SnapshotArray<Actor3d> children = this.children;
		 Actor3d[] actors = children.begin();
		 visibleCount = 0;
		 for (int i = 0, n = children.size; i < n; i++){
			 if(actors[i] instanceof Group3d){
	    		 ((Group3d) actors[i]).drawChildren(modelBatch, environment);
	    	 }
			 else{
					float offsetX = x, offsetY = y, offsetZ = z;
					float offsetScaleX = scaleX, offsetScaleY = scaleY, offsetScaleZ = scaleZ;
					x = 0;
					y = 0;
					z = 0;
					scaleX = 0;
					scaleY = 0;
					scaleZ = 0;
					Actor3d child = actors[i];
					if (!child.isVisible()) continue;
					/*Matrix4 diff = sub(child.getTransform(), getTransform());
					Matrix4 childMatrix = child.getTransform().cpy();
					child.getTransform().set(add(diff, childMatrix));
					child.draw(modelBatch, environment);*/
					float cx = child.x, cy = child.y, cz = child.z;
					float sx = child.scaleX, sy = child.scaleY, sz = child.scaleZ;
					//child.x = cx + offsetX;
					//child.y = cy + offsetY;
					//child.z = cz + offsetZ;
					child.setPosition(cx + offsetX, cy + offsetY, cz + offsetZ);
					child.scale(sx + offsetScaleX, sy + offsetScaleY, sz + offsetScaleZ);
			        if (child.isCullable(getStage3d().getCamera())) {
			        	child.draw(modelBatch, environment);
			            visibleCount++;
			        }
					child.x = cx;
					child.y = cy;
					child.z = cz;
					x = offsetX;
					y = offsetY;
					z = offsetZ;
					child.scaleX = sx;
					child.scaleY = sy;
					child.scaleZ = sz;
					scaleX = offsetScaleX;
					scaleY = offsetScaleY;
					scaleZ = offsetScaleZ;
			 }
		 }
		 children.end();
	}
	
	/** Set the Batch's transformation matrix, often with the result of {@link #computeTransform()}. Note this causes the batch to
	 * be flushed. {@link #resetTransform(Batch)} will restore the transform to what it was before this call. */
	protected void applyTransform (ModelBatch batch, Matrix4 transform) {
		oldBatchTransform.set(getTransform());
		setTransform(transform);
	}

	/** Returns the transform for this group's coordinate system. */
	protected Matrix4 computeTransform () {
		Matrix4 temp = getTransform();

		float originX = this.originX;
		float originY = this.originY;
		float originZ = this.originZ;
		float rotation = this.rotation;
		float scaleX = this.scaleX;
		float scaleY = this.scaleY;
		float scaleZ = this.scaleZ;

		if (originX != 0 || originY != 0)
			localTransform.setToTranslation(originX, originY, originZ);
		else
			localTransform.idt();
		if (rotation != 0) localTransform.rotate(originX, originY, originZ, rotation);
		if (scaleX != 1 || scaleY != 1) localTransform.scale(scaleX, scaleY, scaleZ);
		if (originX != 0 || originY != 0) localTransform.translate(-originX, -originY, -originZ);
		localTransform.trn(x, y, z);

		// Find the first parent that transforms.
		Group3d parentGroup = getParent();
		while (parentGroup != null) {
			if (parentGroup.transform) break;
			parentGroup = parentGroup.getParent();
		}

		if (parentGroup != null) {
			setTransform(parentGroup.getTransform());
			getTransform().mul(localTransform);
		} else {
			getTransform().set(localTransform);
		}

		batchTransform.set(getTransform());
		return batchTransform;
	}

	/** Restores the Batch transform to what it was before {@link #applyTransform(Batch, Matrix4)}. Note this causes the batch to be
	 * flushed. */
	protected void resetTransform (ModelBatch batch) {
		setTransform(oldBatchTransform);
	}
	 
    /** Adds an actor as a child of this group. The actor is first removed from its parent group, if any.
     * @see #remove() */
    public void addActor3d(Actor3d actor3d) {
         actor3d.remove();
         children.add(actor3d);
         actor3d.setParent(this);
         actor3d.setStage3d(getStage3d());
         childrenChanged();
    }
    
    /** Removes an actor from this group. If the actor will not be used again and has actions, they should be
     * {@link Actor#clearActions3d() cleared} so the actions will be returned to their
     * {@link Action#setPool(com.badlogic.gdx.utils.Pool) pool}, if any. This is not done automatically. */
    public boolean removeActor3d(Actor3d actor3d) {
            if (!children.removeValue(actor3d, true)) return false;
            Stage3d stage = getStage3d();
            if (stage != null) stage.unfocus(actor3d);
            actor3d.setParent(null);
            actor3d.setStage3d(null);
            childrenChanged();
            return true;
    }
    
    /** Called when actors are added to or removed from the group. */
    protected void childrenChanged () {
    }
    
    /** Removes all actors from this group. */
    public void clearChildren () {
            Actor3d[] actors = children.begin();
            for (int i = 0, n = children.size; i < n; i++) {
                    Actor3d child = actors[i];
                    child.setStage3d(null);
                    child.setParent(null);
            }
            children.end();
            children.clear();
            childrenChanged();
    }

    /** Removes all children, actions, and listeners from this group. */
    public void clear () {
            super.clear();
            clearChildren();
    }

    /** Returns the first actor found with the specified name. Note this recursively compares the name of every actor in the group. */
    public Actor3d findActor (String name) {
            Array<Actor3d> children = this.children;
            for (int i = 0, n = children.size; i < n; i++)
                    if (name.equals(children.get(i).getName())) return children.get(i);
            for (int i = 0, n = children.size; i < n; i++) {
                    Actor3d child = children.get(i);
                    if (child instanceof Group3d) {
                            Actor3d actor = ((Group3d)child).findActor(name);
                            if (actor != null) return actor;
                    }
            }
            return null;
    }
    
    @Override
    protected void setStage3d (Stage3d stage3d) {
        super.setStage3d(stage3d);
        Array<Actor3d> children = this.children;
        for (int i = 0, n = children.size; i < n; i++)
            children.get(i).setStage3d(stage3d);
    }
    
    /** Returns an ordered list of child actors in this group. */
    public SnapshotArray<Actor3d> getChildren () {
    	return children;
    }
    
    public boolean hasChildren () {
    	return children.size > 0;
    }
    
    /** Prints the actor hierarchy recursively for debugging purposes. */
    public void print () {
            print("");
    }

    private void print (String indent) {
            Actor3d[] actors = children.begin();
            for (int i = 0, n = children.size; i < n; i++) {
                    System.out.println(indent + actors[i]);
                    if (actors[i] instanceof Group3d) ((Group3d)actors[i]).print(indent + "|  ");
            }
            children.end();
    }

	@Override
	public void dispose() {
		super.dispose();
		for(Actor3d actor3d: children)
			actor3d.dispose();
	}
}
