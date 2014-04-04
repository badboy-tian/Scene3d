/*******************************************************************************
 * Copyright 2013 pyros2097
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
package scene3d.demo;

import scene3d.Actor3d;
import scene3d.Group3d;
import scene3d.Stage3d;
import scene3d.actions.Actions3d;

import com.badlogic.gdx.graphics.g3d.loader.G3dModelLoader;
import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.TextureAttribute;
import com.badlogic.gdx.graphics.g3d.utils.CameraInputController;
import com.badlogic.gdx.graphics.g3d.utils.MeshPartBuilder;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.utils.UBJsonReader;

public class Scene3dDemo implements ApplicationListener {
	Stage3d stage3d;
	Stage stage2d;
	Skin skin;
	ModelBuilder modelBuilder;
	CameraInputController camController;
	Model model, model2, model3;
	Actor3d knight, actor2, actor3, floor, skydome;
	Group3d group3d;
	Label fpsText;
	Label visibleText;
	
	public static void main(String[] argc) {
		LwjglApplicationConfiguration cfg = new LwjglApplicationConfiguration();
		cfg.audioDeviceBufferCount = 20;
		cfg.title = "Stage3d Test";
		cfg.useGL20 = false;
		cfg.width = 852;
		cfg.height = 480;
		new LwjglApplication(new Scene3dDemo(), cfg);
	}
	boolean rightKey, leftKey, upKey, downKey, spaceKey;
    @Override
    public void create () {
    	//2d stuff
    	stage2d = new Stage();
    	skin = new Skin(Gdx.files.internal("skin/uiskin.json"));
    	fpsText = new Label("ff", skin);
    	fpsText.setPosition(Gdx.graphics.getWidth() - 80, Gdx.graphics.getHeight()-40);
    	visibleText = new Label("ff", skin);
    	visibleText.setPosition(Gdx.graphics.getWidth() - 80, Gdx.graphics.getHeight()- 60);
    	stage2d.addActor(fpsText);
    	stage2d.addActor(visibleText);
    	stage2d.addListener(new InputListener(){
    		@Override
    		public boolean keyUp(InputEvent event, int keycode) {
    			if (keycode == Keys.LEFT) leftKey = false;
    			if (keycode == Keys.RIGHT) rightKey = false;
    			if (keycode == Keys.UP) upKey = false;
    			if (keycode == Keys.DOWN) downKey = false;
    			if (keycode == Keys.SPACE) spaceKey = false;
    			return super.keyUp(event, keycode);
    		}
    		@Override
    		public boolean keyDown(InputEvent event, int keycode) {
    			if (keycode == Keys.LEFT) leftKey = true;
    			if (keycode == Keys.RIGHT) rightKey = true;
    			if (keycode == Keys.UP) upKey = true;
    			if (keycode == Keys.DOWN) downKey = true;
    			if (keycode == Keys.SPACE) spaceKey = true;
    			return super.keyDown(event, keycode);
    		}
    	});
    	
    	//3dstuff
    	stage3d = new Stage3d();
    	modelBuilder = new ModelBuilder();
    	model = modelBuilder.createBox(5f, 5f, 5f, new Material("Color", ColorAttribute.createDiffuse(Color.WHITE)),
                Usage.Position | Usage.Normal);
		
    	model2 = modelBuilder.createBox(2f, 2f, 2f, new Material("Color", ColorAttribute.createDiffuse(Color.WHITE)),
                Usage.Position | Usage.Normal);
    	actor2 = new Actor3d(model2, 10f, 0f, 0f);
    	model3 = modelBuilder.createBox(2f, 2f, 2f, new Material("Color", ColorAttribute.createDiffuse(Color.ORANGE)),
                Usage.Position | Usage.Normal);
    	actor3 = new Actor3d(model3, -10f, 0f, 0f);
    	actor2.setColor(Color.RED);
    	actor2.setName("actor2");
    	actor3.setName("actor3");
    	camController = new CameraInputController(stage3d.getCamera());
    	InputMultiplexer im = new InputMultiplexer();
    	im.addProcessor(stage2d);// 2d should get click events first
		//im.addProcessor(stage3d);
		im.addProcessor(camController);
		Gdx.input.setInputProcessor(im);
    	stage3d.touchable = Touchable.enabled; // only then will it detect hit actor3d
    	//createAxes();
    	
    	ModelBuilder builder = new ModelBuilder();
		builder.begin();
		MeshPartBuilder part = builder.part("floor", GL20.GL_TRIANGLES, Usage.Position | Usage.TextureCoordinates | Usage.Normal,
			new Material());
		for (float x = -200f; x < 200f; x += 10f) {
			for (float z = -200f; z < 200f; z += 10f) {
				part.rect(x, 0, z + 10f, x + 10f, 0, z + 10f, x + 10f, 0, z, x, 0, z, 0, 1, 0);
			}
		}
		floor = new Actor3d(builder.end());
		AssetManager am = new AssetManager();
    	am.load("data/g3d/knight.g3db", Model.class);
    	am.load("data/g3d/skydome.g3db", Model.class);
    	am.load("data/g3d/concrete.png", Texture.class);
    	am.finishLoading();
    	knight = new Actor3d(am.get("data/g3d/knight.g3db", Model.class), -20f, 10f, 0f);
    	knight.getAnimation().inAction = true;
		knight.getAnimation().animate("Walk", -1, 1f, null, 0.2f);
    	skydome = new Actor3d(am.get("data/g3d/skydome.g3db", Model.class));
    	floor.materials.get(0).set(TextureAttribute.createDiffuse(am.get("data/g3d/concrete.png", Texture.class)));
    	stage3d.addActor3d(skydome);
		stage3d.addActor3d(floor);
		knight.rotate(0f, -90f, 0f);
		testActor3d();
		//stage3d.addAction3d(Actions3d.rotateBy(0f, 90f, 0f, 2f));
    	//testGroup3d();
    	//testStage3d();
    }
    
    //
    @Override
    public void render () {
        Gdx.gl.glClearColor(1, 1, 1, 1);
        Gdx.gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);
        stage3d.act();
    	stage3d.draw();
    	stage2d.act();
     	stage2d.draw();
    	camController.update();
    	fpsText.setText("Fps: " + Gdx.graphics.getFramesPerSecond());
    	visibleText.setText("Visible: " + stage3d.getRoot().visibleCount);
		if (upKey) {
			knight.addAction3d(Actions3d.moveBy(1f, 0f, 0f, 1f));
		} 
		else if (downKey) {
			knight.addAction3d(Actions3d.moveBy(-1f, 0f, 0f, 1f));
		}
		else if (rightKey) {
			knight.rotateYaw(1f);
		} 
		else if (leftKey) {
			knight.rotateYaw(-1f);
		} 
    }

	@Override
	public void resize(int width, int height) {
		stage2d.setViewport(width, height);
		stage3d.setViewport(width, height);
	}
	
	@Override
	public void pause() {}

	@Override
	public void resume() {}
	
    @Override
	public void dispose () {
	   stage3d.dispose();
	}
    
    final float GRID_MIN = -10f;
	final float GRID_MAX = 10f;
	final float GRID_STEP = 1f;
	
	// Must implement a better test
	public Actor3d axesActor;
	
	private void createAxes() {
		ModelBuilder modelBuilder = new ModelBuilder();
		modelBuilder.begin();
		MeshPartBuilder builder = modelBuilder.part("grid", GL10.GL_LINES, Usage.Position | Usage.Color, new Material());
		builder.setColor(Color.LIGHT_GRAY);
		for (float t = GRID_MIN; t <= GRID_MAX; t+=GRID_STEP) {
			builder.line(t, 0, GRID_MIN, t, 0, GRID_MAX);
			builder.line(GRID_MIN, 0, t, GRID_MAX, 0, t);
		}
		builder = modelBuilder.part("axes", GL10.GL_LINES, Usage.Position | Usage.Color, new Material());
		builder.setColor(Color.RED);
		builder.line(0, 0, 0, 100, 0, 0);
		builder.setColor(Color.GREEN);
		builder.line(0, 0, 0, 0, 100, 0);
		builder.setColor(Color.BLUE);
		builder.line(0, 0, 0, 0, 0, 100);
		axesActor = new Actor3d(modelBuilder.end());
		stage3d.addActor3d(axesActor);
	}
	
	public void createLine(Vector3 p1, Vector3 p2, Color color) {		// by xiaozc
		ModelBuilder modelBuilder = new ModelBuilder();
		modelBuilder.begin();
		MeshPartBuilder builder = modelBuilder.part("axes", GL10.GL_LINES, Usage.Position | Usage.Color, new Material());
		builder.setColor(color);
		builder.line(p1, p2);
		Actor3d lineActor = new Actor3d(modelBuilder.end());
		stage3d.addActor3d(lineActor);
	}
	
	void testActor3d(){
    	stage3d.addActor3d(knight);
    	stage3d.followActor3d(knight, true);
    	//stage3d.moveBy(-50f, 0f, 0f, 2f);
        //stage3d.addActor3d(actor2);
    	//actor1.addAction3d(Actions3d.rotateTo(60f, 5f));
    	//actor1.addAction3d(Actions3d.scaleBy(0.5f, 0.5f, 0.5f, 5f, Interpolation.linear));
       // actor1.addAction3d(Actions3d.forever(Actions3d.sequence(Actions3d.moveBy(50f, 0f, 0f, 10f),
       // 		Actions3d.moveBy(-50f, 0f, 0f, 10f))));
     //   actor1.addAction3d(Actions3d.sequence(
     //   		Actions3d.moveBy(7f, 0f, 0f, 2f),
      //  		Actions3d.scaleTo(0.5f, 0.5f, 0.5f, 5f),
      //  		Actions3d.moveBy(-7f, 0f, 0f, 2f)));
    	
    	//actor1.addAction3d(Actions3d.scaleTo(1.2f, 1.2f, 1.2f, 1f));
    	//actor2.addAction3d(Actions3d.scaleBy(0.3f, 0.3f, 0.3f, 5f));
       //actor1.addAction3d(Actions3d.sequence(Actions3d.moveBy(7f, 0f, 0f, 2f), Actions3d.moveBy(-7f, 0f, 0f, 2f)));
        //actor1.addAction3d(Actions3d.moveTo(7f, 0f, 0f, 3f));
       // actor1.addAction3d(Actions3d.moveTo(-10f, 0f, 0f, 2f));
        /*
         *  Since both actions are running at same time we get the difference in x
         *  ans : actor.getX() = -3.0000014
         */
        /* if you see this the setRotation method called alone works but after this moveTo is called and the actor1
         * rotation is reset to original .. this is the problem because of transformation matrix
         * and the Sequence action problem is i think because i'm using the pools from gdx.utils.pools
         */
        //actor1.setRotation(59);
        //actor1.setPosition(5f, 0f, 0f);
       // actor2.addAction3d(Actions3d.moveBy(-7f, 0f, 0f, 2f));
       
        // r.setRotation(59);
        //r.addAction3d(Actions.rotateTo(59, 1f));
        //r.addAction3d(Actions.rotateBy(59, 1f));
    }
    
    void testGroup3d(){
    	group3d = new Group3d();
    	group3d.setPosition(0f, 0f, 0f);
    	group3d.setName("group1");
    	stage3d.addActor3d(group3d);
    	group3d.addActor3d(knight);
    	group3d.addActor3d(actor2);
    	group3d.addActor3d(actor3);
    	group3d.addAction3d(Actions3d.sequence(Actions3d.moveTo(0f, 5f, 0f, 2f), 
    			Actions3d.moveTo(5f, 0f, 0f, 2f), Actions3d.scaleTo(0f, 5f, 0f, 2f)));
    }
    
    void testStage3d(){
    	stage3d.addActor3d(knight);
        stage3d.addActor3d(actor2);
    	stage3d.addAction3d(Actions3d.moveTo(-7f, 0f, 0f, 3f));
    }
}