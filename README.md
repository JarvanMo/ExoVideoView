![logo](/images/default_art.png)
# ExoVideoView
ExoVideoView is based on [ExoPlayer](https://github.com/google/ExoPlayer).

[中文移步至此](/README_CN.md).

![demo](/images/demo.gif)

**What's in ExoVideoView**

    1.Process AudioFocus automatically.
    2.Process its orientation by sensor automatically
    3.simple gesture action supported.
    4.multiple video quality supported
    5.you can add custom views to the default controller.
    6.multiple resize-mode supported
    7.custom controller supported.
## Using ExoVideoView
### 1.Dependency
The easiest way to get started using ExoVideoView is to add it as a gradle dependency. You need to make sure you have the JCenter and Google repositories included in the build.gradle file in the root of your project:
```
repositories {
    jcenter()
    google()
}
```
Next add a gradle compile dependency to the build.gradle file of your app module:
```
implementation 'com.jarvanmo:exoplayerview:2.0.9'
```
### 2.In Layout
Declare ExoVideoView in your layout file as :
```xml
<com.jarvanmo.exoplayerview.ui.ExoVideoView
     android:id="@+id/videoView"
     android:layout_width="match_parent"
     android:layout_height="300dp"/>
```
### 3.In Java
ExoVideoView provides built-in ```Player``` for convenience,so we can play a video as
```java
SimpleMediaSource mediaSource = new SimpleMediaSource(url);//uri also supported
videoView.play(mediaSource);
videoView.play(mediaSource,where);//play from a particular position
```
Passing a player outside to ExoVideoView:
```java
videoView.setPlayer(player);
```
Note:never forget to release ExoPlayer:
```java
videoView.releasePlayer();
```
see details in [demo]().

### 3.Orientation Management
The ExoVideoView can handle its orientation by sensor automatically only when ExoVideoVIew has a not-null OrientationListener  :
```java
    videoView.setOrientationListener(orientation -> {
            if (orientation == SENSOR_PORTRAIT) {
                //do something
            } else if (orientation == SENSOR_LANDSCAPE) {
                //do something
            }
        });
```
Note:When the ExoVideoView handle its orientation automatically,The ExoVideoView will call ```activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE)``` or ```activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT);``` if the ```context``` in controller is an Activity.
The fullscreen management is the same as orientation management.

### 4.Back Events
First,override onKeyDown:
```java
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        if (keyCode == KeyEvent.KEYCODE_BACK) {
            return videoView.onKeyDown(keyCode, event);
        }
        return super.onKeyDown(keyCode, event);
    }

```
Then passing a backListener to ExoVideoView:
```java
    videoView.setBackListener((view, isPortrait) -> {
            if (isPortrait) {
              //do something
            }
            return false;
        });
```
If return value is ```true```, operation will be interrupted.Otherwise,ExoVideoView handle its orientation by itself and ```OrientationLister.onOrientationChange()``` will be caled.
## Advance
### 1.Multi-Quality
ExoVideoView also provides a built-in multi-quality selector.The multi-quality selector
will be added to ```overlayFrameLayout``` if  multi-quality is enabled and  ```ExoMediaSource``` are given different qualities in current version.
```java
        List<ExoMediaSource.Quality> qualities = new ArrayList<>();
        ExoMediaSource.Quality quality =new SimpleQuality(quality,mediaSource.url());
        qualities.add(quality);
        mediaSource.setQualities(qualities);
```

### 2.Controller Display Mode
```ExoVideoPlaybackController``` are divided into four parts:
```
1.Top
2.Top Landscape
3.Bottom
4.Bottom Landscape
```
Each of them can be hidden or shown:
```xml
 app:controller_display_mode="all|none|top|top_landscape|bottom|bottom_landscape"
```
in java:
```java
  videoView.setControllerDisplayMode(mode);
```
### 3.Add Custom View To Controller
Views can be added to ```ExoVideoPlaybackController``` in java.
```java
  videoView.addCustomView(ExoVideoPlaybackControlView.CUSTOM_VIEW_TOP, view);
  videoView.addCustomView(ExoVideoPlaybackControlView.CUSTOM_VIEW_TOP_LANDSCAPE, view);
  videoView.addCustomView(ExoVideoPlaybackControlView.CUSTOM_VIEW_BOTTOM_LANDSCAPE, view);
```
### 4.Specifying A custom Layout File
Defining your own ```exo_video_playback_control_view.xml``` is useful to customize the layout of ```ExoVideoPlaybackControlView``` throughout your application. It's also possible to customize the layout for asingle instance in a layout file. This is achieved by setting the  controller_layout_id attribute on a ```ExoVideoPlaybackControlView```. This will cause the specified layout to be inflated instead of ```code exo_video_playback_control_view.xml``` for only the instance on which the attribute is set.
```xml
app:controller_layout_id="@layout/my_controller"
```
## Others

```
  app:controller_background="@android:color/holo_orange_dark"
  app:use_artwork="true"
  app:default_artwork="@drawable/default_art"
```