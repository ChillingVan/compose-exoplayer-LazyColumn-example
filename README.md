# compose-exoplayer-LazyColumn-example
A video player of exoplayer on LazyColumn(list view) of Jetpack Compose. Codes are structured with MVVM and modularization. 

Related Blog 
- [Jetpack Compose with Exoplayer, Hilt, MVVM and Modularization](https://medium.com/p/4371a0a5a0d0)
- [Compose Pager And Video](https://medium.com/@chillingvan/compose-pager-and-video-84683d5f0faa)

This project shows a example of using Exoplayer with Jetpack Compose.

Here are the features included:
* Play videos in a LazyColumn(Similar to play videos on a RecyclerView)
* Play video with full screen.
* Custom play controller view with Jetpack Compose, especially the seek bar. Thanks to [Compose-sliders](https://github.com/krottv/compose-sliders)
* Listening to the scroll event of LazyColumn and play automatically.
* Play consecutively between List Page and Detail Page.
* Codes are organized and modularized.
* Use MVVM design pattern.
* Support vertical pager in the video detail page to allow scrolling up and down to switch videos

# Modules
* app: The app entry - contains something like the Application and MainActivity.
* core-ui: The common views - contains auxiliary UI components and specific dependencies that need to be shared between other modules. 
* core-navigation: This contains the codes about data sharing between pages. 
* core-video: Common library about Exoplayer control. It contains full screen control, video play event control. 
* feature-main: Feature specific module for main page. The UI of video list view and view model are included here.
* feature-videodetail: Feature specific module for video detail page.The UI of video detail view and view model are included here.

# Pages

The application has two screens(two pages).
1. Main page screen - List of Videos and the videos can play on the list directly. See the [MainPageScreen.kt](feature-main/src/main/java/com/chillingvan/samples/composevideo/main/MainPageScreen.kt) for detail.
2. Video detail screen - One Video in a detail page with detail description. See the [VideoDetailScreen.kt](feature-videodetail/src/main/java/com/chillingvan/samples/composevideo/videodetail/VideoDetailScreen.kt) for detail

![sample1](https://user-images.githubusercontent.com/7666419/190075315-64b0c3fc-0c09-4c9a-905a-f54e62898cf9.jpg)
![sample2](https://user-images.githubusercontent.com/7666419/188564548-8a66d2ee-867c-4a95-acd9-76ec46b07ea6.jpg)
![sample3](https://user-images.githubusercontent.com/7666419/188564607-3a36281f-2652-4f37-a54b-e41a713e4e06.jpg)

# About
* Thanks [Compose-sliders](https://github.com/krottv/compose-sliders) for the custom seek bar.
* Thanks [Now in Android](https://github.com/android/nowinandroid) for the modularization

# License

    Copyright 2021 compose-video Contributors

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

    https://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
