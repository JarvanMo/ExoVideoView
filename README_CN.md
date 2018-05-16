![logo](/images/default_art.png)
# ExoVideoView





ExoVideoView 是一款基于[ExoPlayer](https://github.com/google/ExoPlayer)开发的视频播放器.
![demo](/images/demo.gif)

**ExoVideoView可以做什么**

    1.自动处理音频焦点。
    2.根据传感器自动处理方向。
    3.手势支持。
    4.多清晰度选择支持。
    5.为控制器添加自定义布局.
    6.调整显示大小。
    7.自定义controller。
## 使用 ExoVideoView
### 1.依赖
最简单的方式是加入gradle依赖。请确认在工程的build.gradle中添加了JCenter和google()。
```
repositories {
    jcenter()
    google()
}
```
然后在你的项目中添加如下代码：
```
implementation 'com.jarvanmo:exoplayerview:2.0.3'
```
### 2.在xml中定义
在xml中使用 ExoVideoView:
```xml
<com.jarvanmo.exoplayerview.ui.ExoVideoView
     android:id="@+id/videoView"
     android:layout_width="match_parent"
     android:layout_height="300dp"/>
```
### 3.在java代码中
ExoVideoView 提供了内建```Player```：
```java
SimpleMediaSource mediaSource = new SimpleMediaSource(url);
videoView.play(mediaSource);
videoView.play(mediaSource,where);//play from a particular position
```
也可以使用自义的Player:
```java
videoView.setPlayer(player);
```
提示:不要忘记释放ExoPlayer:
```java
videoView.releasePlayer();
```
详情请移步[demo]().

### 3.方向管理
ExoVideoView 可以自动处理方向问题，前提是为ExoVideoView设置一个OrientationListener:
```java
    videoView.setOrientationListener(orientation -> {
            if (orientation == SENSOR_PORTRAIT) {
                //do something
            } else if (orientation == SENSOR_LANDSCAPE) {
                //do something
            }
        });
```
提示：当ExoVideoView自动处理方向问题时，如果在Controller中的context是Activity,那么系统会调用
```activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE)``` or ```activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT);```
全屏事件处理也是如此。
### 4返回管理
首先,重写onKeyDown:
```java
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        if (keyCode == KeyEvent.KEYCODE_BACK) {
            return videoView.onKeyDown(keyCode, event);
        }
        return super.onKeyDown(keyCode, event);
    }

```
为ExoVideoView设置监听:
```java
    videoView.setBackListener((view, isPortrait) -> {
            if (isPortrait) {
              //do something
            }
            return false;
        });
```
如果返回值是 ```true```, 系统后续动作会被中断.否则，ExoVideoView会自动处理方向，并且会回调```OrientationLister.onOrientationChange()``` .
## 高级
### 1.多清清晰度
ExoVideoView 内置清晰度选择器.如果开启发多清晰度并添加了多清晰度，内置清晰度选择器将被加入```overlayFrameLayout```.
```java
        List<ExoMediaSource.Quality> qualities = new ArrayList<>();
        ExoMediaSource.Quality quality =new SimpleQuality(quality,mediaSource.url());
        qualities.add(quality);
        mediaSource.setQualities(qualities);
```

### 2.Controller显示模式
```ExoVideoPlaybackController``` 被分为四个部分:
```
1.Top
2.Top Landscape
3.Bottom
4.Bottom Landscape
```
每一部分都可以被显示或隐藏:
```xml
 app:controller_display_mode="all|none|top|top_landscape|bottom|bottom_landscape"
```
在java中:
```java
  videoView.setControllerDisplayMode(mode);
```

### 3.为controller添加控件
```ExoVideoPlaybackController``` 允许在java代码中添加控件.
```java
  videoView.addCustomView(ExoVideoPlaybackControlView.CUSTOM_VIEW_TOP, view);
  videoView.addCustomView(ExoVideoPlaybackControlView.CUSTOM_VIEW_TOP_LANDSCAPE, view);
  videoView.addCustomView(ExoVideoPlaybackControlView.CUSTOM_VIEW_BOTTOM_LANDSCAPE, view);
```
### 4.使用自定义controller布局
```exo_video_playback_control_view.xml```是允许自定义的。其中一些属性在```ExoVideoPlaybackControlView```有定义。具体可看源码。
```xml
app:controller_layout_id="@layout/my_controller"
```
## 其他

```
  app:controller_background="@android:color/holo_orange_dark"
  app:use_artwork="true"
  app:default_artwork="@drawable/default_art"
```