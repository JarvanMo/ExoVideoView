# ExoPlayerView
ExoPlayerView 是一款简基于[ExoPlayer](https://github.com/google/ExoPlayer)的播放器控件.

![brightness](/images/brightness.png)![controller_1](/images/conroller_1.png)
![fast_forward_rewind](/images/fastforward_rewind.png)![landscape](/images/landscap.png)
![portrait](/images/portrait.png)![volume](/images/volume.png)


将下面的语句加入到 `build.gradle` :
     compile 'com.jarvanmo:exoplayerview:0.0.1'
ExoPlayerView 可以直接播放一像常用视频, 比如说 mp4,m3u8等等，也可以用于直播.使用起来也很简单.
你需要在你的布局文件里面做如下声明:
```xml

    <com.jarvanmo.exoplayerview.ui.ExoVideoView
        android:id="@+id/videoView"
        android:layout_width="match_parent"
        android:layout_height="300dp"
        app:useController="true"
        app:resizeMode="fit"
        />
        
```
ExoPlayerView 提供了 3　 视频适应模式: fit ,  fit_width , fit_height
以及 none.

播放代码如下:
```java
   videoView.play(mediaSource);
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
ExoPlayerView 也支持手势操作, 比如说左滑调亮度，右滑调音量,也可以快近或后退.
如果你的target SDK version 是在23或以上, 不要忘记申请权限：
```xml
<uses-permission android:name="android.permission.WRITE_SETTINGS"/>
```