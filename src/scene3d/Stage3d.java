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
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight;
import com.badlogic.gdx.graphics.g3d.model.MeshPart;
import com.badlogic.gdx.graphics.g3d.model.Node;
import com.badlogic.gdx.graphics.g3d.model.NodePart;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.Ray;
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
		environment.set(new ColorAttribute(ColorAttribute.AmbientLight, 0.4f, 0.4f, 0.4f, 1f));
		environment.add(new DirectionalLight().set(0.8f, 0.8f, 0.8f, -1f, -0.8f, -0.2f));

		setViewport(width, height, keepAspectRatio);
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

	@Override
	public boolean touchDown(int screenX, int screenY, int pointer, int button) {
		return false;
	}

	@Override
	public boolean touchUp(int screenX, int screenY, int pointer, int button) {
		if(touchable == Touchable.enabled)
			hit(screenX, screenY);
		return false;
	}
	
	private Ray pickRay;
	private Vector3 returnIntersection = new Vector3();
	private Vector3[] returnTris = new Vector3[3];
	private Array<Vector3[]> miTriangs = new Array<Vector3[]>();
	private Actor3d hitActor;

	public Actor3d hit(float x, float y) {
		hitActor = null;
		SnapshotArray<Actor3d> children = root.getChildren();
		Actor3d[] actors = children.begin();
		for (int i = 0, n = children.size; i < n; i++){
			miTriangs.clear();
			pickRay = camera.getPickRay(x, y);
			getModelInstanceTriangles(actors[i].model, miTriangs);
			if((getIntersectionTriangles(actors[i], pickRay, 
					miTriangs, returnIntersection, returnTris)) != -1)
				//Gdx.app.log("", actors[i].getName());
				hitActor = actors[i];
		}
		children.end();
		return hitActor;
	}
	
	public Actor3d getHitActor(){
		return hitActor;
	}


	/*
	 * Written by Scott Griffy
	 * The code in this class has only been tested on meshes (with applied rot+scale+pos) exported from blender to FBX then converted to G3Dj with fbx-conv
	 * Works with multiple mesh parts (e.g. different objects in one scene in blender)
	 * fbx-conv binaries can be found for windows on the page here:
	 * http://libgdx.badlogicgames.com/fbx-conv/
	 * 
	 * TODO accept animations to change the mesh and raycast on the changed mesh
	 * TODO return the texture UV coordinates.
	 * 
	 * Don't forget to add a triangulate modifier (you don't necessarily have to apply it)
	 * I used the built-in FBX exporter in Blender 2.66
	 * Also Y-Forward and Z-up (both positive) seems to work for me in the FBX exporter
	 * 
	 * Throughout these methods the variable "miARRtriangs" is used
	 * It's just an ArrayMap that links models to arrays of vector3[3]s (triangles)
	 * When the raycast checks a ModelInstance it looks up in this variable the triangle array corresponding to the ModelInstance's model
	 * You can pre-fetch a "miARRtriangs" with "getModelInstanceArrTriangles" and an Array of ModelInstances.
	 * This cuts down on CPU time during raycasts (very important on devices)
	 * This works because models don't change (they will when I implement animations, but that's later)
	 * 
	 * You'll want to start by using "getIntersectionTriangles" on your array of ModelInstances
	 * you'll have to feed "getIntersectionTriangles" an initialized Vector3 for the "intersection" parameter,
	 * and a Vector3[3] array for the "triags" parameter
	 * and a ModelInstance[1] array for the "mi" parameter (hacky return)
	 * also of course your ModelInstance array and PickRay (cam.getPickRay(x, y)?)
//			the method will set the first element of "mi" to
//			the ModelInstance that the ray intersected, sets "intersection" to the intersection with that ModelInstance
//			and sets the first 3 elements of "triags" to the vertices in the triangle that was intersected
//			the "triags" can be used to calculate the normal with another method in this file
	 * 
	 * Also I don't think this works with ModelInstances with multiple models (don't know if this is possible)
	 */

	// used for the "miARRtriangs" object when iterating though an array of modelInstances,
	private void getModelInstanceTriangles(Model mi, Array<Vector3[]> triangs) {
		Iterator<Node> nodeIter = mi.nodes.iterator();
		while (nodeIter.hasNext())
		{
			Node n = nodeIter.next();
			n.calculateLocalTransform();
			Iterator<NodePart> nodePartIter = n.parts.iterator();
			while (nodePartIter.hasNext())
			{
				NodePart np = nodePartIter.next();
				MeshPart meshPart = np.meshPart;
				float[] triangsF = new float[this.getNumFloatTriangles(meshPart)];
				this.getTriangles(meshPart, triangsF);
				//float[] normals = new float[this.getNumFloatTriangles(meshPart)];
				//this.getNormals(meshPart, normals);
				for (int i = 0; i < meshPart.numVertices/3; ++i)
				{
					Vector3[] triVectors = new Vector3[3];
					for (int c = 0; c < 3; ++c)
					{
						triVectors[c] = new Vector3(
								triangsF[i*9+c*3],// the 9 is the triangle size (3 vectors = 9 floats)
								triangsF[i*9+c*3+1],
								triangsF[i*9+c*3+2]);
					}
					triangs.add(triVectors);
				}
			}
		}
	}

	// the meatiest method
	// this can be used by itself actually
	private Ray tempPickRay = new Ray(new Vector3(), new Vector3());
	private float getIntersectionTriangles(ModelInstance mi, Ray pickRay, 
			Array<Vector3[]> miARRtriangs, Vector3 intersection, Vector3[] triags){
		Vector3 intersect = new Vector3();
		float closestIntersectionDistance = -1;
		Iterator<Node> nodeIter = mi.nodes.iterator();
		Iterator<Vector3[]> triangArr = miARRtriangs.iterator();
		while (nodeIter.hasNext())
		{
			Node n = nodeIter.next();
			Iterator<NodePart> nodePartIter = n.parts.iterator();
			while (nodePartIter.hasNext())
			{
				NodePart np = nodePartIter.next();
				MeshPart meshPart = np.meshPart;
				//float[] triangs = triangArr.next();//new float[this.getNumFloatTriangles(meshPart)];
				//this.getTriangles(meshPart, triangs);
				//float[] normals = new float[this.getNumFloatTriangles(meshPart)];
				//this.getNormals(meshPart, normals);
				for (int i = 0; i < meshPart.numVertices/3; ++i)
				{
					Vector3[] triVectors = triangArr.next();//new Vector3[3];
					/*
						for (int c = 0; c < 3; ++c)
						{
							triVectors[c] = new Vector3(
									triangs[i*9+c*3],// the 9 is the triangle size (3 vectors = 9 floats)
									triangs[i*9+c*3+1],
									triangs[i*9+c*3+2]);
						}*/
					// normals may not be used
					/*
						Vector3[] triNormals = new Vector3[3];
						for (int c = 0; c < 3; ++c)
						{
							triNormals[c] = new Vector3(
									triangs[i*9+c*3],
									triangs[i*9+c*3+1],
									triangs[i*9+c*3+2]);
						}*/
					tempPickRay.set(pickRay);
					//n.calculateLocalTransform();// shouldnt be called on the render loop
					tempPickRay.mul(mi.transform.cpy().inv());
					tempPickRay.mul(n.globalTransform.cpy().inv());
					if (tempPickRay != null && 
							Intersector.intersectRayTriangle(
									tempPickRay, 
									triVectors[0], triVectors[1], triVectors[2], intersect))
					{
						/* the following code has been commented out for future debugging
							for (int c = 0; c < 3; ++c)
							{
								Gdx.app.log("normals", "normals"+triNormals[c].x+"y"+triNormals[c].y+"z"+triNormals[c].z);
							}
						 */
						float dist = intersect.dst(tempPickRay.origin);
						//Gdx.app.log("distBot", ""+dist);
						if (dist != -1 && (closestIntersectionDistance == -1 || dist < closestIntersectionDistance))
						{
							closestIntersectionDistance = dist;
							intersection.set(intersect.cpy());
							for (int c = 0; c < 3; ++c)
							{
								triags[c] = triVectors[c];
							}
						}

					}
				}
			}
		}
		return closestIntersectionDistance;
	}
	// used to fetch triangles for the model (each meshpart)
	private void getTriangles(MeshPart meshPart, float[] triags) {
		// get the mesh of the mesh part (this holds the vertices+normals+UV+otherstuff)
		Mesh mesh = meshPart.mesh;
		// this changes based on the what stuff the mesh has (vertices+normals+UV+idk)
		int floatsInAVertex = mesh.getVertexSize()/4;
		//Gdx.app.log("floatsInAVertex", ""+floatsInAVertex);
		// each vertices will need enough floats for all the info including vertices+normals+UV+otherstuff
		float[] verts = new float[mesh.getNumVertices()*floatsInAVertex];
		mesh.getVertices(verts);
		// this is a list of all the indices in the mesh. Every 3 is a triangle also holds a lot of extra space for buffers
		short[] indicesFull = new short[mesh.getNumIndices()];
		mesh.getIndices(indicesFull);
		// need to get rid of the extra indices not used by this MeshPart
		short[] indices = new short[meshPart.numVertices];
		int currIndex = 0;
		for (int i = 0 ; i < indicesFull.length ; ++i)
		{
			// use only the indices in the mesh part's range
			if (i >= meshPart.indexOffset && i < meshPart.indexOffset+meshPart.numVertices)
			{
				indices[currIndex] = indicesFull[i];
				++currIndex;
			}
		}
		// now make the triangle array
		int indNum = 0;
		while (indNum < meshPart.numVertices)
		{
			triags[indNum*3] = verts[indices[indNum]*floatsInAVertex];
			triags[indNum*3+1] = verts[indices[indNum]*floatsInAVertex+1];
			triags[indNum*3+2] = verts[indices[indNum]*floatsInAVertex+2];
			indNum++;
		}
	}

	// gets the normals for a meshPart (not really used)
	private void getNormals(MeshPart meshPart, float[] normals){
		// get the mesh of the mesh part (this holds the vertices+normals+UV+otherstuff)
		Mesh mesh = meshPart.mesh;
		// this changes based on the what stuff the mesh has (vertices+normals+UV+idk)
		int floatsInAVertex = mesh.getVertexSize()/4;// sizeof(float)
		// each vertices will need enough floats for all the info including vertices+normals+UV+otherstuff
		float[] verts = new float[mesh.getNumVertices()*floatsInAVertex];
		mesh.getVertices(verts);
		// this is a list of all the indices in the mesh. Every 3 is a triangle also holds a lot of extra space for buffers
		short[] indicesFull = new short[mesh.getNumIndices()];
		mesh.getIndices(indicesFull);
		// need to get rid of the extra indices not used by this MeshPart
		short[] indices = new short[meshPart.numVertices];
		int currIndex = 0;
		for (int i = 0 ; i < indicesFull.length ; ++i)
		{
			// use only the indices in the mesh part's range
			if (i >= meshPart.indexOffset && i < meshPart.indexOffset+meshPart.numVertices)
			{
				indices[currIndex] = indicesFull[i];
				++currIndex;
			}
		}
		// now make the normal array
		int indNum = 0;
		while (indNum < meshPart.numVertices)
		{
			/* this is found slightly differently than the triangles array because normals are USUALLY just after position in the vertex information
			 * usually is capitalized because this only holds true for certain meshes and this code will break if the vertex information order is changed
			 * TODO this means this code needs to be reworked to account for vertex information order
			 */
			normals[indNum*3] = verts[indices[indNum]*floatsInAVertex+3];
			normals[indNum*3+1] = verts[indices[indNum]*floatsInAVertex+4];
			normals[indNum*3+2] = verts[indices[indNum]*floatsInAVertex+5];
			indNum++;
		}
	}
	// this calculates the normal, (might be the wrong direction, idk about how the FBX/FBX-conv handle twisting)
	private Vector3 calcNormU = new Vector3();
	private Vector3 calcNormV = new Vector3();
	private Vector3 calcNormTemp = new Vector3();
	private Vector3 calcNormV2 = new Vector3(0, 1, 0).nor();

	private void calcNormal(Vector3[] closestIntersectionTriang, 
			boolean flipZ, Vector3 returnV, Quaternion quat){
		// get the normal vector from the triangle hit
		calcNormU = closestIntersectionTriang[1].cpy().sub(closestIntersectionTriang[0]);
		calcNormV = closestIntersectionTriang[2].cpy().sub(closestIntersectionTriang[0]);
		calcNormTemp.set(calcNormU.crs(calcNormV).nor());

		if (flipZ)
		{
			// it's probably inverted (depends on exporter)
			float tempNormal = calcNormTemp.y;
			calcNormTemp.y = calcNormTemp.z;
			calcNormTemp.z = -tempNormal;
		}

		// find quaternion from normal
		quat.setFromCross(calcNormV2, calcNormTemp);

		returnV.set(calcNormTemp);
	}
	private int getNumFloatTriangles(MeshPart meshPart){
		return meshPart.numVertices*3;
	}

	private void reorientIntersection(Vector3 intersection)  {
		// the intersection point needs to be flipped as well
		float temp = intersection.y;
		intersection.y = intersection.z;
		intersection.z = -temp;
	}

	private void setForBillBoard(Vector3 intersection, Vector3 nor) {
		// this pushes the bullet out a bit from the wall
		intersection.add(nor.cpy().scl(.1f, .1f, .1f));
	}

	@Override
	public void dispose() {
		clear();
		modelBatch.dispose();
	}
}
