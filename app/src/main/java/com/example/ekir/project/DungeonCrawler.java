package com.example.ekir.project;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.Shader;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;
import java.util.Random;

/**
 * This class describes the itself. It extends GameView which is my game engine
 */
class DungeonCrawler extends GameView {
        Bitmap buffer;
    public class Camera {
        int x;
        int y;
        int width=550;
        int height=450;
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
                points.add(new Point((int)event.getX(n),(int)event.getY(n)));
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

            for(int n=0;n<NumberOfPoints;n++) {
                int x=(int)event.getX(n);
                int y=(int)(int)event.getY(n);
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
        buffer=Bitmap.createBitmap(550,450, Bitmap.Config.RGB_565);
        gameObjects.add(new tree(200,400));
        gameObjects.add(player);
        gameObjects.add(MyTree);
        controller = new Controller();
        this.setOnTouchListener(controller);
        player.width=190;
        player.height=190;
        MyTree.x=100;
        MyTree.y=100;
        snd_sword = load_sound(R.raw.sword);
        grass_texture=load_bitmap("grass.jpg");
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        controller.moveRect=new Rect(0,0,w-200,h);
        btn_Attack = new virtButton() {
            public void onClick() {
                //player.x=10;
                if(player.attack==false) {
                    player.attack = true;
                    player.attackState = 0;
                }
            }
        };
        btn_Attack.position=new Rect(w-200,h-200,w,h-100);
        controller.add(btn_Attack);
    }

    public float vector_length(float x_diff,float y_diff) {
        return (float)Math.sqrt(x_diff*x_diff+y_diff*y_diff);
    }
    abstract class GameObject {
        public abstract void act();
        public int x;
        public int y;
        public int width;
        public int height;
        public abstract Bitmap getBitmap();
    }
    public class Player extends GameObject {
        float speed=10;
        GameView gameView;
        Bitmap attack_image[][] = new Bitmap[8][8];
        Bitmap running_image[][] = new Bitmap[8][8];
        boolean attack=false;
        int walkState=0;
        int attackState=0;
        public Player(GameView tgameView) {
            gameView=tgameView;
            for(int i=0;i<8;i++)
            {
                attack_image[0][i] = load_bitmap("attack/attack e000" + Integer.toString(i) + ".png");
                attack_image[1][i] = load_bitmap("attack/attack ne000" + Integer.toString(i) + ".png");
                attack_image[2][i] = load_bitmap("attack/attack n000" + Integer.toString(i) + ".png");
                attack_image[3][i] = load_bitmap("attack/attack nw000" + Integer.toString(i) + ".png");
                attack_image[4][i] = load_bitmap("attack/attack w000" + Integer.toString(i) + ".png");
                attack_image[5][i] = load_bitmap("attack/attack sw000" + Integer.toString(i) + ".png");
                attack_image[6][i] = load_bitmap("attack/attack s000" + Integer.toString(i) + ".png");
                attack_image[7][i] = load_bitmap("attack/attack se000" + Integer.toString(i) + ".png");

                running_image[0][i] = load_bitmap("running/running e000" + Integer.toString(i) + ".png");
                running_image[1][i] = load_bitmap("running/running ne000" + Integer.toString(i) + ".png");
                running_image[2][i] = load_bitmap("running/running n000" + Integer.toString(i) + ".png");
                running_image[3][i] = load_bitmap("running/running nw000" + Integer.toString(i) + ".png");
                running_image[4][i] = load_bitmap("running/running w000" + Integer.toString(i) + ".png");
                running_image[5][i] = load_bitmap("running/running sw000" + Integer.toString(i) + ".png");
                running_image[6][i] = load_bitmap("running/running s000" + Integer.toString(i) + ".png");
                running_image[7][i] = load_bitmap("running/running se000" + Integer.toString(i) + ".png");
            }
        }

        public int getIndexByAngle() {
            float angle = controller.dir_angle();
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
            if(attackState>0) {
               return attack_image[getIndexByAngle()][attackState];
            }
            return running_image[getIndexByAngle()][walkState];
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
        public void act() {
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
            }
        }

    }
    public class tree extends GameObject {
        int treeState=0;
        int treeDelay=0;
        Bitmap tree_image[] = new Bitmap[8];
        public void setPosition(int tx, int ty) {
            x=tx;
            y=ty;
        }
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
    public void drawGameObject(Canvas canvas,GameObject gameObject) {
        Bitmap image = gameObject.getBitmap();
        int radius_x = (image.getWidth()/2);
        int radius_y = (image.getHeight()/2);
        canvas.drawBitmap(image,null,camera.transformRect(new Rect((gameObject.x-radius_x),(gameObject.y-radius_y),(gameObject.x+radius_x),(gameObject.y+radius_y))),null);
    }

    tree MyTree = new tree();
    Player player = new Player(this);
    int i=0;
    Bitmap grass_texture=null;
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
        BitmapShader a = new BitmapShader(grass_texture,
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
        canvas.drawText(Float.toString(controller.dir_angle()),50,50,textpaint);

        for(int i=0;i<gameObjects.size();i++) {
            gameObjects.get(i).act();
            drawGameObject(canvas,gameObjects.get(i));
        }
        camera.focusOn(player);
        drawPanel(canvas);
        screenCanvas.drawBitmap(buffer,null,new Rect(0,0,1100,900),null);
        if(controller.move) {
            screenCanvas.drawLine(controller.touch_start_x, controller.touch_start_y, controller.touch_current_x, controller.touch_current_y, black);
        }
        for(int n=0;n<controller.points.size();n++) {
            Point tmp_point=controller.points.get(n);
            screenCanvas.drawCircle(tmp_point.x,tmp_point.y,50,black);
        }

    }

    public void drawPanel(Canvas canvas) {
        Paint paint = new Paint();
        paint.setColor(Color.WHITE);
        canvas.drawRect(new Rect(controller.moveRect.right,0,canvas.getWidth(),canvas.getHeight()),paint);
        paint.setColor(Color.RED);
        canvas.drawRect(btn_Attack.position,paint);
    }
}