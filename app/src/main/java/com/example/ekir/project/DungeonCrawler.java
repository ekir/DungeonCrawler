package com.example.ekir.project;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.Shader;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Random;

/**
 * This class describes the itself. It extends GameView which is my game engine
 */
class DungeonCrawler extends GameView {
    int level=0;
    GameObject stairsUp;
    GameObject stairsDown;
    public enum Action {
        MOVE, LOOK, ATTACK
    }

    public class DepthComparator implements Comparator<GameObject> {
        @Override
        public int compare(GameObject o1, GameObject o2) {
            return o1.y-o2.y;
        }
    }
        Bitmap buffer;
    int panel_width=100;
    public class VirtScreen {
        public float widthScale=1;
        public float heightScale=1;
        public int width=640;
        public int height=360;
        public Point convertPoint(Point input) {
            return new Point((int)(input.x/widthScale),(int)(input.y/heightScale));
        }
    }
    VirtScreen virtScreen=new VirtScreen();
    public class Camera {
        int x;
        int y;
        int width=640; // Buffer width is 640, but only part of it is camera area
        int height=360;
        public void focusOn(GameObject gameObject) {
            this.x=gameObject.x-(int)(this.width/2);
            this.y=gameObject.y-(int)(this.height/2);
        }
        public Rect transformRect(Rect trect) {
            Rect rect=new Rect();
            rect.left=trect.left-x;
            rect.right=trect.right-x;
            rect.top=trect.top-y;
            rect.bottom=trect.bottom-y;
            return rect;
        }
    }
    public class World{
        public Rect border = new Rect();
        public World(int left,int top, int right, int bottom) {
            border.left=left;
            border.top=top;
            border.right=right;
            border.bottom=bottom;
        }
    }
    Camera camera = new Camera();
    abstract class virtButton
    {
        Rect position;
        public abstract void onClick();
    }
    public virtButton btn_Menu;
    public virtButton btn_Attack;
    public ArrayList<GameObject> gameObjects = new ArrayList<GameObject>();
    public class Controller implements View.OnTouchListener {
        public int touch_start_x;
        public int touch_start_y;
        public int touch_current_x;
        public int touch_current_y;
        public boolean move;
        public float dir_x;
        public float dir_y;
        public Rect moveRect;
        public ArrayList<Point> points = new ArrayList<Point>();
        public ArrayList<virtButton> virtButtonList = new ArrayList<virtButton>();
        public void add(virtButton button) {
            virtButtonList.add(button);
        }
        float dir_angle() {
            float math_dir_y=-dir_y;
            float result;
            if(dir_x > 0) {
                result= (float) Math.toDegrees(Math.asin(math_dir_y));
            } else {
                result= (float) 180-(float)Math.toDegrees(Math.asin(math_dir_y));
            }
            if(result<0) {
                result+=360;
            }
            return result;
        }
        public Controller() {
            moveRect = new Rect(0,0,200,200);
        }
        public boolean onTouch(View v, MotionEvent event) {
            // http://stackoverflow.com/questions/8356283/android-ontouch-listener-event
            int NumberOfPoints = event.getPointerCount();
            points.clear();
            for(int n=0;n<NumberOfPoints;n++) {
                points.add(virtScreen.convertPoint(new Point((int)event.getX(n),(int)event.getY(n))));
            }

            if(event.getAction()==event.ACTION_UP || event.getAction()==event.ACTION_POINTER_UP) {
                // ACTION_UP is only called when there are no more pointers
                points.remove(event.getActionIndex());
                //points.clear();
            }

            for(int n=0;n<points.size();n++) {
                Point tmp_point=points.get(n);
                for(i=0;i<virtButtonList.size();i++) {
                    if(virtButtonList.get(i).position.contains(tmp_point.x,tmp_point.y)) {
                        virtButtonList.get(i).onClick();
                    }
                }
            }

            move = false;
            for(int n=0;n<points.size();n++) {
                Point tmp_point=points.get(n);
                if(moveRect.contains(tmp_point.x,tmp_point.y)) {
                    move = true;
                }
            }

            for(int n=0;n<points.size();n++) {
                int x=points.get(n).x;
                int y=points.get(n).y;
                boolean button_pressed=false;

                for(i=0;i<virtButtonList.size();i++) {
                    if(virtButtonList.get(i).position.contains(x,y)) {
                        virtButtonList.get(i).onClick();
                        button_pressed = true;
                    }
                }
                /*if(button_pressed) {
                    continue;
                }*/



                /*if(!moveRect.contains(x,y)) {
                    continue;
                }*/
                if(moveRect.contains(x,y)) {
                    if (event.getAction() == event.ACTION_DOWN) {


                        touch_start_x = x;
                        touch_start_y = y;
                        touch_current_x = x;
                        touch_current_y = y;
                        //move = true;
                    }
                    if (event.getAction() == event.ACTION_MOVE) {
                        touch_current_x = x;
                        touch_current_y = y;
                        int x_diff = touch_current_x - touch_start_x;
                        int y_diff = touch_current_y - touch_start_y;
                        float length = vector_length(x_diff, y_diff);
                        dir_x = x_diff / length;
                        dir_y = y_diff / length;
                    }
                }
            }
            return true;
        }
    }
    int snd_sword;
    Controller controller;
    public DungeonCrawler(Context context) {
        super(context);
        buffer=Bitmap.createBitmap(640,360, Bitmap.Config.RGB_565);
        controller = new Controller();
        this.setOnTouchListener(controller);
        snd_sword = load_sound(R.raw.sword);
        stairsUp = new StaticObject("stairsup.png");;
        stairsDown = new StaticObject("stairsdown.png");;

        load_level();
    }

    public void load_level() {
        gameObjects.clear();
        gameObjects.add(player);
        switch(level) {
            case 0:
                load_level0();
                break;
            case 1:
                load_level1();
                break;
            case 2:
                load_level1();
                break;
        }
    }

    public void load_level0() {
        gameObjects.add(new Ogre());
        gameObjects.add(stairsDown);
        ground_texture=load_bitmap("grass.jpg");
        gameObjects.add(new tree(200,400));
        gameObjects.add(MyTree);
        player.x=-120;
        player.y=-100;
        player.width=190;
        player.height=190;
        MyTree.x=100;
        MyTree.y=100;
        stairsUp.setPosition(-800,0);
        stairsDown.setPosition(-300,-400);
    }

    public void load_level1() {
        gameObjects.add(stairsUp);
        gameObjects.add(stairsDown);
        stairsDown.setPosition(-900,100);
        stairsUp.setPosition(-300,-400);
        ground_texture=load_bitmap("stone.png");
    }

    public abstract class Level {
        public abstract void Load();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        controller.moveRect=new Rect(0,0,virtScreen.width-panel_width,virtScreen.height);
        btn_Menu = new virtButton() {
            public void onClick() {
                if(game_paused==false) {
                    game_paused=true;
                    ((PlayActivity) getContext()).LaunchMenu();
                }
            }
        };
        btn_Attack = new virtButton() {
            public void onClick() {
                //player.x=10;
                if(player.attack==false) {
                    player.attack = true;
                    player.attackState = 0;
                }
            }
        };
        virtScreen.widthScale=w/virtScreen.width;
        virtScreen.heightScale=h/virtScreen.height;
        btn_Menu.position=new Rect(virtScreen.width-100,0,virtScreen.width,50);
        btn_Attack.position=new Rect(virtScreen.width-100,virtScreen.height-100,virtScreen.width,virtScreen.height-50);
        controller.add(btn_Menu);
        controller.add(btn_Attack);
    }

    public float vector_length(float x_diff,float y_diff) {
        return (float)Math.sqrt(x_diff*x_diff+y_diff*y_diff);
    }
    abstract class GameObject {
        public void setPosition(int tx, int ty) {
            x=tx;
            y=ty;
        }
        public abstract void act();
        public int x;
        public int y;
        public int width;
        public int height;
        public abstract Bitmap getBitmap();
        public float distance(GameObject target) {
            float x_diff=target.x-x;
            float y_diff=target.y-y;
            return (float)Math.pow((double)(x_diff*x_diff+y_diff*y_diff),(float)0.5);
        }
    }
    public class Character extends GameObject {
        float dir_angle() {
            float math_dir_y=-dir_y;
            float result;
            if(dir_x > 0) {
                result= (float) Math.toDegrees(Math.asin(math_dir_y));
            } else {
                result= (float) 180-(float)Math.toDegrees(Math.asin(math_dir_y));
            }
            if(result<0) {
                result+=360;
            }
            return result;
        }
        Action action=Action.LOOK;
        public void move() {
            action=Action.MOVE;
            walkState = walkState + 1;
            walkState = walkState % 8;
            x=(int)(x+dir_x*speed);
            y=(int)(y+dir_y*speed);
        }
        float dir_x;
        float dir_y;
        public void directTo(GameObject target) {
            int x_diff=target.x-x;
            int y_diff=target.y-y;
            float length=vector_length(x_diff,y_diff);
            dir_x=x_diff/length;
            dir_y=y_diff/length;
        }
        float speed=10;
        Bitmap looking_image[][] = new Bitmap[8][8];
        Bitmap attack_image[][] = new Bitmap[8][8];
        Bitmap running_image[][] = new Bitmap[8][8];
        boolean attack=false;
        float lookState=0;
        int walkState=0;
        int attackState=0;
        public Bitmap[][] load_bitmap_360(String basename) {
            Bitmap return_image[][]=new Bitmap[8][8];
            for(int i=0;i<8;i++) {
                return_image[0][i] = load_bitmap(basename+"e000" + Integer.toString(i) + ".png");
                return_image[1][i] = load_bitmap(basename+"ne000" + Integer.toString(i) + ".png");
                return_image[2][i] = load_bitmap(basename+"n000" + Integer.toString(i) + ".png");
                return_image[3][i] = load_bitmap(basename+"nw000" + Integer.toString(i) + ".png");
                return_image[4][i] = load_bitmap(basename+"w000" + Integer.toString(i) + ".png");
                return_image[5][i] = load_bitmap(basename+"sw000" + Integer.toString(i) + ".png");
                return_image[6][i] = load_bitmap(basename+"s000" + Integer.toString(i) + ".png");
                return_image[7][i] = load_bitmap(basename+"se000" + Integer.toString(i) + ".png");
            }
            return return_image;
        }

        public Character() {
            //running_image=load_bitmap_360("running/running ");
            //attack_image=load_bitmap_360("attack/attack ");
            looking_image=load_bitmap_360("ogre/looking ");
            running_image=load_bitmap_360("ogre/running ");
            attack_image=load_bitmap_360("ogre/attack ");
        }

        public int getIndexByAngle() {
            float angle = dir_angle();
            if(angle <= 25) {
                return 0;
            } else if(angle <=70) {
                return 1;
            } else if(angle <=115) {
                return 2;
            } else if(angle <=160) {
                return 3;
            } else if(angle <= 205) {
                return 4;
            } else if(angle <= 250) {
                return 5;
            } else if(angle <= 295) {
                return 6;
            } else if(angle <= 340) {
                return 7;
            } else if(angle <= 385) {
                return 0;
            } else {
                return 0;
            }
        }

        public Bitmap getBitmap() {
            switch (action) {
                case LOOK:
                    return looking_image[getIndexByAngle()][(int) lookState];
                case MOVE:
                    return running_image[getIndexByAngle()][walkState];
                case ATTACK:
                    return attack_image[getIndexByAngle()][attackState];
            }
            return looking_image[getIndexByAngle()][(int) lookState];
        }

        /*
        public Bitmap getBitmapOld() {
            if(controller.dir_x>=0) {
                if(controller.dir_y > Math.sin(45)) {
                    return player_image_south[walkState];
                } else if (controller.dir_y < -Math.sin(45)) {
                    return player_image_north[walkState];
                } else {
                    return player_image_east[walkState];
                }
            } else {
                if(controller.dir_y > Math.sin(45)) {
                    return player_image_south[walkState];
                } else if (controller.dir_y < -Math.sin(45)) {
                    return player_image_north[walkState];
                } else {
                    return player_image_west[walkState];
                }
            }
        }
        */
        public void proceed() {
            switch(action) {
                case LOOK:
                    lookState = lookState + 0.4f;
                    lookState = lookState % 8;
                    break;
            }
        }
        public void act() {
            action=Action.LOOK;
        }
    }
    public class Ogre extends Character {
        public void act() {
            super.act();
            float distance_to_player=distance(player);
            if(distance_to_player > 150) {
                // To far away to notice
                proceed(); // Continue with current action
                return;
            }
            // Notices
            directTo(player);
            // Moves to player unless close enough
            if(distance_to_player > 50) {
                move();
            }
            proceed(); // Continue with current action
        }
        public Ogre() {
            super();
            speed=7;
            looking_image=load_bitmap_360("ogre/looking ");
            running_image=load_bitmap_360("ogre/running ");
            attack_image=load_bitmap_360("ogre/attack ");
        }
    }
    public class Player extends Character {
        public Bitmap getBitmap() {
            if(attackState>0) {
                return attack_image[getIndexByAngle()][attackState];
            } else if(controller.move) {
                return running_image[getIndexByAngle()][walkState];
            } else {
                return looking_image[getIndexByAngle()][(int)lookState];
            }
        }
        public Player() {
            super();
            looking_image=load_bitmap_360("player/looking ");
            running_image=load_bitmap_360("player/running ");
            attack_image=load_bitmap_360("player/attack ");
        }
        public void act() {
            super.act();
            dir_x=controller.dir_x;
            dir_y=controller.dir_y;
            if(attack) {
                if(attackState<7) {
                    attackState = attackState + 1;
                } else {
                    attackState=0;
                    attack=false;
                }
                if(attackState==4) {
                    play_sound(snd_sword);
                }
                return;
            }
            if(controller.move) {
                walkState = walkState + 1;
                walkState = walkState % 8;
                int new_x=(int)(x+controller.dir_x*speed);
                int new_y=(int)(y+controller.dir_y*speed);
                if(world.border.contains(new_x,new_y)){
                    x=new_x;
                    y=new_y;
                }
            } else {
                lookState = lookState + 0.4f;
                lookState = lookState % 8;
            }
        }
    }
    public class tree extends GameObject {
        int treeState=0;
        int treeDelay=0;
        Bitmap tree_image[] = new Bitmap[8];
        public tree() {
            for(int i=0;i<7;i++) {
                tree_image[i] = load_bitmap("background/fir A ani000"+Integer.toString(i)+".png");
            }
        }
        public tree(int tx,int ty) {
            this();
            this.x=tx;
            this.y=ty;
        }

        @Override
        public void act() {
            if(treeDelay<=2) {
                treeDelay=treeDelay+1;
            } else {
                if(treeState<6) {
                    treeState++;
                } else {
                    treeState=0;
                }
                treeDelay=0;
            }
        }

        @Override
        public Bitmap getBitmap() {
            return tree_image[treeState];
        }
    }
    public class StaticObject extends GameObject {
        Bitmap image;
        public void setPosition(int tx, int ty) {
            x=tx;
            y=ty;
        }
        public StaticObject(String filename) {
           image = load_bitmap(filename);
        }
        public StaticObject(String filename,int tx,int ty) {
            this(filename);
            this.x=tx;
            this.y=ty;
        }

        @Override
        public void act() {
        }

        @Override
        public Bitmap getBitmap() {
            return image;
        }
    }
    public void drawGameObject(Canvas canvas,GameObject gameObject) {
        Bitmap image = gameObject.getBitmap();
        int radius_x = (image.getWidth()/2);
        int radius_y = (image.getHeight()/2);
        canvas.drawBitmap(image,null,camera.transformRect(new Rect((gameObject.x-radius_x),(gameObject.y-radius_y),(gameObject.x+radius_x),(gameObject.y+radius_y))),null);
    }

    tree MyTree = new tree();
    Player player = new Player();
    int i=0;
    Bitmap ground_texture;
    World world = new World(-1000,-1000,1000,1000);
    @Override
    public void gameLoop(Canvas screenCanvas) {
        Canvas canvas = new Canvas(buffer);
        canvas.drawColor(Color.BLACK);

        Paint paint=new Paint();
        //paint.setColor(Color.BLACK);
        Paint black = new Paint();
        black.setColor(Color.BLACK);

        //http://code.tutsplus.com/tutorials/android-sdk-drawing-with-pattern-fills--mobile-19527
        Paint background = new Paint();
        background.setColor(Color.YELLOW);
        BitmapShader a = new BitmapShader(ground_texture,
                Shader.TileMode.REPEAT, Shader.TileMode.REPEAT);

        //http://stackoverflow.com/questions/3719736/moving-a-path-with-a-repeating-bitmap-image-in-android
        Matrix matrix = new Matrix();
        matrix.reset();
        matrix.setScale(1,1);
        matrix.preTranslate(-camera.x, -camera.y);
        a.setLocalMatrix(matrix);

        background.setShader(a);
        canvas.drawRect(camera.transformRect(world.border),background);
        // Show move area
        //canvas.drawRect(controller.moveRect, yellow);

        // Init the paint
        Paint textpaint = new Paint();
        textpaint.setTextSize(50);
        textpaint.setFakeBoldText(true);
        textpaint.setColor(Color.BLACK);
        canvas.drawText(Float.toString(level),50,50,textpaint);
        if(player.distance(stairsUp)<70) {
            Log.d("hoj","Up");
            level--;
            load_level();
            player.setPosition(stairsDown.x-100,stairsDown.y-100);
        }

        if(player.distance(stairsDown)<70) {
            Log.d("hoj","Down");
            level++;
            load_level();
            player.setPosition(stairsUp.x+100,stairsUp.y+100);
        }

        Collections.sort(gameObjects, new DepthComparator());
        for(int i=0;i<gameObjects.size();i++) {
            gameObjects.get(i).act();
            drawGameObject(canvas,gameObjects.get(i));
        }
        camera.focusOn(player);
        drawPanel(canvas);
        for(int n=0;n<controller.points.size();n++) {
            Point tmp_point=controller.points.get(n);
            canvas.drawCircle(tmp_point.x,tmp_point.y,50,black);
        }
        if(controller.move) {
            canvas.drawLine(controller.touch_start_x, controller.touch_start_y, controller.touch_current_x, controller.touch_current_y, black);
        }
        screenCanvas.drawBitmap(buffer,null,new Rect(0,0,screenCanvas.getWidth(),screenCanvas.getHeight()),null);



    }

    public void drawPanel(Canvas canvas) {
        Paint paint = new Paint();
        paint.setColor(Color.WHITE);
        canvas.drawRect(new Rect(canvas.getWidth()-panel_width,0,canvas.getWidth(),canvas.getHeight()),paint);
        paint.setColor(Color.RED);
        canvas.drawRect(btn_Menu.position,paint);
        canvas.drawRect(btn_Attack.position,paint);
    }
}