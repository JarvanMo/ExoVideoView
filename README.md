# ExoPlayerView
ExoPlayerView is a simple video view based on [ExoPlayer](https://github.com/google/ExoPlayer).

The new ExoPlayerView based on ExoPlayer-2.6.0 is under development.

[中文](/README_CN.md).

![brightness](/images/brightness_new.png)
![controller_1](/images/controller_1_new.png)
![fast_forward_rewind](/images/fastforward_rewind_new.png)
![landscape](/images/landscape_new.png)
![portrait](/images/portrait_new.png)
![volume](/images/volume_new.png)



**Features**

    1.There are 4 modes to resize the video: 
      fit ,  fit_width , fit_height and none.
    2.Process AudioFocus automatically.
    3.Change its orientation by sensor automatically
    4.simple gesture action supported.
**Usage**

***Import***

Add the following to your `build.gradle` file

    compile 'com.jarvanmo:exoplayerview:1.1.4'

ExoPlayerView can play simple video directly, such as mp4,m3u8 and so on.
It's easy to use.
Declare ExoVideoView in your layout files:
```xml

    <com.jarvanmo.exoplayerview.ui.ExoVideoView
        android:id="@+id/videoView"
        android:layout_width="match_parent"
        android:layout_height="300dp"
        app:useController="true"
        app:resizeMode="fit"
        app:orientationAuto="true"
        />
        
```

***Play***  
  
play a video :
```java
   videoView.play(mediaSource);
```
The ExoVideoView will create SimpleExoPlayer by itself if we play mediaSource.
Actually, you can set a player by yourself;
```java
    videoView.setPlayer(player);
```

We can play from a particular position too:
```java
   videoView.play(mediaSource,where);
```
Note:don't forget to release ExoPlayer:
```java
videoView.releaseSelfPlayer();
```
also we can give a display name:
```java
 mediaSource.setDisplayName("LuYu YouYue");
```
or
```java
 videoView.setDisplayName("LuYu YouYue");
```

***Manage Orientation***

The ExoVideoView can change its orientation by sensor automatically only when you set
a not-null orientation listener:
```java
      videoView.setOrientationListener(new ExoVideoPlaybackControlView.OrientationListener() {
            @Override
            public void onOrientationChange(@ExoVideoPlaybackControlView.SensorOrientationType int orientation) {
                if(orientation == SENSOR_PORTRAIT){
                    changeToPortrait();
                }else if(orientation == SENSOR_LANDSCAPE){
                    changeToLandscape();
                }
            }
        });
```
When the ExoVideoView change its orientation by itself,The ExoVideoView will call ```activity.setRequestedOrientation()``` if
the context in controller is an Activity.
The fullscreen button is the same.

You can change the orientation of ExoVideoView by:
```java
videoView.toggleControllerOrientation();
```
Or
```java
videoView.setPortrait(true);
```
***Handle Back Events***

In activity :
```java

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(event.getKeyCode() == KeyEvent.KEYCODE_BACK){

            if(videoView.isPortrait()){
               finish();
                return false;
            }else {
                videoView.toggleControllerOrientation();
                return true;
            }
        }
        return super.onKeyDown(keyCode, event);
    }

```
When button in controller clicked:
```java
        videoView.setBackListener(new ExoVideoPlaybackControlView.ExoClickListener() {
            @Override
            public boolean onClick(View view, boolean isPortrait) {
                if(isPortrait){
                    finish();
                }
              return false;
            }
        });

```
if ```onClick()``` return true,it'll  interrupt  controller's operation.If it return 
false and you set a not-null OrientationListener,The ExoVideoView will request to 
change its orientation automatically.If the ExoVideoView's orientation is landscape,
it'll be changed to portrait and ```OrientationLister.onOrientationChange()``` will 
be called.

***Others***

Also you can add you view to the controller view when landscape:

```java
  videoView.addViewToControllerWhenLandscape(view);
```
the view you want to add will add into FrameLayout．

***NOTE***

Never forget to release the ExoPlayer.
```
 videoView.releaseSelfPlayer();
```
or
```
player.release();
```

The ExoPlayer also support simple gesture action, such as change-volume,change-brightness and so on.