package scene3d;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.utils.AnimationController;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.math.collision.Ray;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.DelayedRemovalArray;
import com.badlogic.gdx.utils.Disposable;


public class Actor3d extends ModelInstance implements Disposable {
	private Stage3d stage3d;
	private Group3d parent;
	
	private final DelayedRemovalArray<Event3dListener> listeners = new DelayedRemovalArray<Event3dListener>(0);
	private final Array<Action3d> actions = new Array(0);
	
	public final Vector3 center = new Vector3();
    public final Vector3 dimensions = new Vector3();
    private BoundingBox boundBox = new BoundingBox();
    public final float radius;
	
	private String name;
	private boolean visible = true;
	
	float x, y, z;
	float originX, originY, originZ;
	float scaleX = 1, scaleY = 1, scaleZ = 1;
	float rotation = 0;
	float rotationX = 0, rotationY = 0, rotationZ = 0;
	private AnimationController animation;
	
	public Actor3d(){
		this(new Model());
		setOrigin(0,0,0);
		setScale(0,0,0);
	}
	
	public Actor3d(Model model){
		this(model, 0f, 0f ,0f);
	}
	
	public Actor3d(Model model, float x, float y, float z){
		super(model);
		setPosition(x,y,z);
		//boundBox = model.meshes.get(0).calculateBoundingBox();
		calculateBoundingBox(boundBox);
		setOrigin(boundBox.max.x, boundBox.max.y, boundBox.max.z);
        center.set(boundBox.getCenter());
        dimensions.set(boundBox.getDimensions());
		radius = dimensions.len() / 2f;
		animation = new AnimationController(this);
	}
	
	/** Updates the actor3d based on time. Typically this is called each frame by {@link Stage3d#act(float)}.
	 * <p>
	 * The default implementation calls {@link Action3d#act(float)} on each action and removes actions that are complete.
	 * @param delta Time in seconds since the last frame. */
	public void act (float delta) {
		for (int i = 0; i < actions.size; i++) {
			Action3d action3d = actions.get(i);
			if (action3d.act(delta) && i < actions.size) {
				actions.removeIndex(i);
				action3d.setActor3d(null);
				i--;
			}
		}
		if (animation.inAction)
			animation.update(delta);
	}
	
	public void draw(ModelBatch modelBatch, Environment environment){
		modelBatch.render(this, environment);
	}
	
	public Actor3d hit (float x, float y) {
		return null;
	}

	/** Removes this actor3d from its parent, if it has a parent.
	 * @see Group#removeActor3d(Actor3d) */
	public boolean remove () {
		if (parent != null) return parent.removeActor3d(this);
		return false;
	}
	
	/** Add a listener to receive events that {@link #hit(float, float, boolean) hit} this actor3d. See {@link #fire(Event)}.
	 * 
	 * @see InputListener
	 * @see ClickListener */
	public boolean addListener(Event3dListener listener) {
		if (!listeners.contains(listener, true)) {
			listeners.add(listener);
			return true;
		}
		return false;
	}

	public boolean removeListener (Event3dListener listener) {
		return listeners.removeValue(listener, true);
	}

	public Array<Event3dListener> getListeners () {
		return listeners;
	}
	
	public void addAction3d (Action3d action3d) {
		action3d.setActor3d(this);
		actions.add(action3d);
	}

	public void removeAction3d (Action3d action) {
		if (actions.removeValue(action, true)) action.setActor3d(null);
	}

	public Array<Action3d> getActions3d () {
		return actions;
	}

	/** Removes all actions on this actor3d. */
	public void clearActions3d () {
		for (int i = actions.size - 1; i >= 0; i--)
			actions.get(i).setActor3d(null);
		actions.clear();
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
	
	private static final Vector3 position = new Vector3();
	public boolean isCullable(final Camera cam) {
	    return cam.frustum.sphereInFrustum(getTransform().getTranslation(position).add(center), radius);
    }
	
	public boolean isVisible () {
		return visible;
	
	}
	/** If false, the actor3d will not be drawn and will not receive touch events. Default is true. */
	public void setVisible (boolean visible) {
		this.visible = visible;
	}
	
	/** @return -1 on no intersection, or when there is an intersection: the squared distance between the center of this 
     * object and the point on the ray closest to this object when there is intersection. */
    public float intersects(Ray ray) {
        transform.getTranslation(position).add(center);
        final float len = ray.direction.dot(position.x-ray.origin.x, position.y-ray.origin.y, position.z-ray.origin.z);
        if (len < 0f)
            return -1f;
        float dist2 = position.dst2(ray.origin.x+ray.direction.x*len, ray.origin.y+ray.direction.y*len, ray.origin.z+ray.direction.z*len);
        return (dist2 <= radius * radius) ? dist2 : -1f;
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
		//this.rotate(degrees);
		transform.setToRotation(originX, originY, originZ, rotation);
	}
	
	public void setRotationX(float degrees){
		this.rotationX = degrees;
		transform.setToRotation(Vector3.X, degrees);
	}
	
	public void setRotationY(float degrees){
		this.rotationX = degrees;
		transform.setToRotation(Vector3.Y, degrees);
	}
	
	public void setRotationZ(float degrees){
		this.rotationZ = degrees;
		transform.setToRotation(Vector3.Z, degrees);
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
		transform.setToScaling(scaleX, scaleY, scaleZ);
	}
	
	public void setScale(float scale) {
		this.scaleX = scale;
		this.scaleY = scale;
		this.scaleZ = scale;
		transform.setToScaling(scaleX, scaleY, scaleZ);
	}
	
	/** Adds the specified scale to the current scale. */
	public void scale(float scale) {
		scaleX += scale;
		scaleY += scale;
		scaleZ += scale;
		transform.scl(scale); // re-implement this
	}
	
	public void scale(float scaleX, float scaleY, float scaleZ) {
		this.scaleX += scaleX;
		this.scaleY += scaleY;
		this.scaleZ += scaleZ;
		transform.scl(scaleX, scaleY, scaleZ); // re-implement this
	}
	
	
	public void setX (float x) {
		this.x = x;
		transform.setToTranslation(x, y, z);
	}
	
	public float getX () {
		return x;
	}
	
	public void setY (float y) {
		this.y = y;
		transform.setToTranslation(x, y, z);
	}
	
	public float getY () {
		return y;
	}
	
	public void setZ (float z) {
		this.z = z;
		transform.setToTranslation(x, y, z);
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
	
	public void updateTransform(){
		transform.setToTranslationAndScaling(x, y, z, scaleX, scaleY, scaleZ);
		transform.setToRotation(originX, originY, originZ, rotation);
	}

	/** Sets a name for easier identification of the actor3d in application code.
	 * @see Group#findActor(String) */
	public void setName (String name) {
		this.name = name;
	
	}
	public String getName () {
		return name;
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
	
	public Color getColor(){
		return ((ColorAttribute)getMaterial("Color").get(ColorAttribute.Diffuse)).color;
	}
	
	public void setColor(Color color){
		ColorAttribute ca = new ColorAttribute(ColorAttribute.Diffuse, color);
		if(getMaterial("Color") != null)
			getMaterial("Color").set(ca);
		else
			materials.add(new Material("Color", ca));
			model.materials.add(new Material("Color", ca));
	}
	
	public Matrix4 getTransform(){
		return transform;
	}
	
	public void setTransform(Matrix4 transform){
		this.transform = transform;
	}
	
	public BoundingBox getBoundingBox(){
		return boundBox;
	}
	
	public void setBoundingBox(BoundingBox box){
		boundBox = box;
	}
	
	public AnimationController getAnimation(){
		return animation;
	}

	@Override
	public void dispose() {
		//super.dispose(); FIXME: update gdx
	}
}