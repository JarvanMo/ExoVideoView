# ExoPlayerView
ExoPlayerView 是一款简基于[ExoPlayer](https://github.com/google/ExoPlayer)的播放器控件.


在 `build.gradle` 文件中加入下面语句即可引用ExoPlayerView:

    compile 'com.jarvanmo:exoplayerview:0.1.0'
ExoPlayerView 可以直接播放一像常用视频, 比如说 mp4,m3u8等等，也可以用于直播.使用起来也很简单.
你需要在你的布局文件里面做如下声明:
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
ExoVideoView 提供了4种视频适应模式: fit ,  fit_width , fit_height
以及 none.


属性 orientationAuto 决定了controller的方向是否由传感器控制，当方向发生变化时，会回调:
```java
   videoView.setFullScreenListener(new ExoVideoPlaybackControlView.ExoClickListener() {
         @Override
         public void onClick(View view, boolean isPortrait) {
             videoView.changeOrientation();
          }
   });
```
此时,如果是传感器引起的方向变化，参数view则为null.

播放代码如下:
```java
   videoView.play(mediaSource);
```
当你调用play(mediaSource)方法播放时ExoPlayerView会自动为你创建一个SimpleExoPlayer;
当然你也可以构建你自己的ExoPlayer:
```java
    videoView.setPlayer(player);
```
注意:不要忘记释放ExoPlayer:
```java
videoView.releaseSelfPlayer();
```
可以提供一个显示名字:
```java
 mediaSource.setDisplayName("LuYu YouYue");
```
或者;
```java
 videoView.setDisplayName("LuYu YouYue");
```



也有一些监听器供你使用 :
```java

        videoView.setBackListener(new ExoVideoPlaybackControlView.ExoClickListener() {
            @Override
            public void onClick(View view, boolean isPortrait) {
                if(isPortrait){
                    finish();
                }else {
                    videoView.changeOrientation();
                }
            }
        });

```

```java
        videoView.setFullScreenListener(new ExoVideoPlaybackControlView.ExoClickListener() {
            @Override
            public void onClick(View view, boolean isPortrait) {
                videoView.changeOrientation();
            }
        });
```
也提供了横屏时在控制条添加自定义view:

```java
       videoView.addViewToControllerWhenLandscape(view);
```
其中，view会添加到FrameLayout中．

注意:`changeOrientation()` 只会影响控制控件的样式，不会做任何旋转操作.
ExoVideoView 也支持手势操作, 比如说左滑调亮度，右滑调音量,也可以快近或后退.
如果你的target SDK version 是在23或以上, 不要忘记申请权限：
```xml
<uses-permission android:name="android.permission.WRITE_SETTINGS"/>
```