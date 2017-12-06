# ExoPlayerView
ExoPlayerView 是一个基于[ExoPlayer](https://github.com/google/ExoPlayer)的视频播放器，
并且做了很多封装.


![brightness](/images/brightness_new.png)
![controller_1](/images/controller_1_new.png)
![fast_forward_rewind](/images/fastforward_rewind_new.png)
![landscape](/images/landscape_new.png)
![portrait](/images/portrait_new.png)
![volume](/images/volume_new.png)


**特性**

    1.提供了4种视频适应模式: 
      fit ,  fit_width , fit_height and none。
    2.自动处理音频焦点问题。
    3.可以根据传感器自动处理视频方向问题。
    4.支持简单的手势操作
**用法**

***导入***

在 `build.gradle` 中加入

    compile 'com.jarvanmo:exoplayerview:1.1.3'

ExoPlayerView 可以直接播放如mp4,m3u8 等简单视频，可以用于直播.
在布局文件中引入 ExoVideoView:
```xml

    <com.jarvanmo.exoplayerview.ui.ExoVideoView
        android:id="@+id/videoView"
        android:layout_width="match_parent"
        android:layout_height="300dp"
        app:useController="true"
        app:resizeMode="none"
        app:orientationAuto="true"
        />
        
```
***Play***    
播放一个视频:
```java
   videoView.play(mediaSource);
```
如果你直接调用了上面的方法，ExoVideoView可以自动创建ExoPlayer.
当然了, 你也可以自己创建ExoPlayer;
```java
    videoView.setPlayer(player);
```

也可以从指定位置播放:
```java
   videoView.play(mediaSource,where);
```
注意:不要忘记释放ExoPlayer:
```java
videoView.releaseSelfPlayer();
```
可以通过如下方式为视频设置一个显示名称:
```java
 mediaSource.setDisplayName("LuYu YouYue");
```
或者
```java
 videoView.setDisplayName("LuYu YouYue");
```

***管理ExoVideoView方向***

如果你为ExoVideoView设置了一个非空```OrientationListener```,ExoVideoView可以通过感器自动
变换方向。
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
只有当在controller中的context是Activity的时候，ExoVideoView才会调用：
```activity.setRequestedOrientation()```
全屏按钮也是如此。
也可以通过如下方式更改ExoVideoView方向:
```java
videoView.toggleControllerOrientation();
```
或者
```java
videoView.setPortrait(true);
```
***处理返回事件***

在activity:
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
当 controller 中的返回键钮被点击了:
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
如果 ```onClick()``` 返回了true,它会拦截controller中的事件.如果返回的是false 并且你设置了一个非空的OrientationListener，


***Others***

你也可以在横屏的时候加入一个自定义布局：

```java
       videoView.addViewToControllerWhenLandscape(view);
```
你添加的布局将被加入FrameLayout中．

***提示***

永远不要忘记去释放ExoPlayer.
```
 videoView.releaseSelfPlayer();
```
or
```
player.release();
```

ExoVideoView 也支持手势操作, 比如说左滑调亮度，右滑调音量,也可以快近或后退.