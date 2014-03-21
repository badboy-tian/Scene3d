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

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.utils.CameraInputController;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;

public class Scene3dDemo implements ApplicationListener {
	Stage3d stage3d;
	Stage stage2d;
	Skin skin;
	ModelBuilder modelBuilder;
	CameraInputController camController;
	Model model;
	Actor3d actor1, actor2;
	Group3d group3d;
	Label fpsText;
	
	public static void main(String[] argc) {
		LwjglApplicationConfiguration cfg = new LwjglApplicationConfiguration();
		cfg.audioDeviceBufferCount = 20;
		cfg.title = "Stage3d Test";
		cfg.useGL20 = false;
		cfg.width = 852;
		cfg.height = 480;
		new LwjglApplication(new Scene3dDemo(), cfg);
	}
	
	// Must implement a better test
     
    @Override
    public void create () {
    	//2d stuff
    	stage2d = new Stage();
    	skin = new Skin(Gdx.files.internal("skin/uiskin.json"));
    	fpsText = new Label("ff", skin);
    	fpsText.setPosition(Gdx.graphics.getWidth() - 80, Gdx.graphics.getHeight()-40);
    	stage2d.addActor(fpsText);
    	
    	//3dstuff
    	stage3d = new Stage3d();
    	modelBuilder = new ModelBuilder();
    	model = modelBuilder.createBox(5f, 5f, 5f, new Material("Color", ColorAttribute.createDiffuse(Color.WHITE)),
                Usage.Position | Usage.Normal);
    	actor1 = new Actor3d(model, 0f, 0f, 0f);
    	model = modelBuilder.createBox(2f, 2f, 2f, new Material("Color", ColorAttribute.createDiffuse(Color.WHITE)),
                Usage.Position | Usage.Normal);
    	actor2 = new Actor3d(model, 10f, 0f, 0f);
    	camController = new CameraInputController(stage3d.getCamera());
        Gdx.input.setInputProcessor(camController);
        stage3d.addActor3d(actor1);
        stage3d.addActor3d(actor2);
    	testActor3d();
    	actor1.setColor(Color.BLUE);
    	actor2.setColor(Color.RED);
    	//testGroup3d();
    	//testStage3d();
    }
    
    void testActor3d(){
    	//actor1.addAction3d(Actions3d.rotateTo(60f, 5f));
    	//actor1.addAction3d(Actions3d.scaleBy(0.5f, 0.5f, 0.5f, 5f, Interpolation.linear));
        //actor1.addAction3d(Actions3d.forever(Actions3d.sequence(Actions3d.moveBy(7f, 0f, 0f, 2f),
        //		Actions3d.moveBy(-7f, 0f, 0f, 2f))));
        actor1.addAction3d(Actions3d.sequence(
        		Actions3d.moveBy(7f, 0f, 0f, 2f),
        		Actions3d.scaleTo(0.5f, 0.5f, 0.5f, 5f),
        		Actions3d.moveBy(-7f, 0f, 0f, 2f)));
    	
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
    	group3d.addActor(actor1);
    	group3d.addActor(new Actor3d(model, 7f, 0f, 0f));
    	stage3d.addActor3d(group3d);
    	group3d.addAction3d(Actions3d.moveTo(-7f, 0f, 0f, 2f));
    }
    
    void testStage3d(){
    	stage3d.addAction3d(Actions3d.moveTo(-7f, 0f, 0f, 1f));
    }
 
    @Override
    public void render () {
    	//Gdx.gl.glViewport(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        Gdx.gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);
        stage2d.act();
    	stage2d.draw();
        stage3d.act();
    	stage3d.draw();
    	camController.update();
    	fpsText.setText("Fps: " + Gdx.graphics.getFramesPerSecond());
    	Gdx.app.log("", ""+actor1.getScaleX());
    }
     
    @Override
    public void dispose () {
    	stage3d.dispose();
    }

	@Override
	public void pause() {
	}

	@Override
	public void resize(int arg0, int arg1) {
	}

	@Override
	public void resume() {
	}
}