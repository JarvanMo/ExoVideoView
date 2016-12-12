# ExoPlayerView
ExoPlayerView is a simple video view based on [ExoPlayer](https://github.com/google/ExoPlayer).

[中文](/README_CN.md).



Just add the following to your `build.gradle` file

    compile 'com.jarvanmo:exoplayerview:0.0.2'

ExoPlayerView can play simple video directly, such as mp4,m3u8 and so on.
It's easy to use.
Just declare ExoVideoView in your layout files:
```xml

    <com.jarvanmo.exoplayerview.ui.ExoVideoView
        android:id="@+id/videoView"
        android:layout_width="match_parent"
        android:layout_height="300dp"
        app:useController="true"
        app:resizeMode="fit"
        />
        
```
The ExoVideoView provide 3 modes to resize your video: fit ,  fit_width , fit_height
and none.

We can play a video just like:
```java
   videoView.play(mediaSource);
```
The ExoVideoView will create SimpleExoPlayer by itself if we play mediaSource.
Actually, you can set a player by yourself;
```java
    videoView.setPlayer(player);
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


There are also some listeners for you :
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
Note:The method `changeOrientation()` only determine the style of the 
playback controller view.


Also you can add you view to the controller view when landscape:

```java
       videoView.addViewToControllerWhenLandscape(view);
```
the view you want to add will add into FrameLayout．

The ExoPlayer also support simple gesture action, such as change-volume,
change-brightness and so on.If your target SDK version is 23
or higher, don't forget to request the following permission:
```xml
<uses-permission android:name="android.permission.WRITE_SETTINGS"/>
```