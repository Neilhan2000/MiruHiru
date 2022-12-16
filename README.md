# **MiruHiru** <img align="right" src="https://img.shields.io/badge/version-1.0.7-green" /><img align="right" src="https://img.shields.io/badge/platform-Android-green" />
**MiruHiru** is a multiplayer travel challenge App, users can explore the story and features of attractions through challenges on the MiruHiru map or create their own challenges to share with everyone.
[<div style="text-align:center">
<img src="https://play.google.com/intl/en_us/badges/static/images/badges/en_badge_web_generic.png" width="280" /></div>](https://play.google.com/store/apps/details?id=com.neil.miruhiru)

## **Features**
---
+ ### **Exploere New Challenges**
    You can find different types of challenges on the MiruHiru map, find what you are interested in and play with friends.

    ### **> explore**
    <div>
    <img src="assets/explore_record.gif" height="280" />
    <img src="assets/screenshot_explore_card.png" height="280" />
    <img src="assets/screenshot_challenge_detail.png" height="280" />
    <img src="assets/screenshot_comment_report.png" height="280" />
    </div>

    ### **> start challenge**
    <img src="assets/start_challenge_record.gif" height="280" />
    <!-- <img src="assets/screenshot_multiplayer_challenge.png" height="280" /> -->
    <img src="assets/screenshot_task.png" height="280" />
    <img src="assets/screenshot_answer_question.png" height="280" />
    <!-- <img src="assets/screenshot_chat_room.png" height="280" /> -->
    <img src="assets/screenshot_challenge_log.png" height="280" />
    <!-- <img src="assets/screenshot_leave_comment.png" height="280" /> -->
+ ### **Custom Your Challenges**
    Customize your own challenges to share with friends and you can also re-edit those completed challenges.

    ### **> customize**
    <div>
    <img src="assets/my_custom_record2.gif" height="280" />
    <img src="assets/screenshot_custom_create.png" height="280" />
    <img src="assets/screenshot_custom_create2.png" height="280" />
    <img src="assets/screenshot_custom_challenge.png" height="280" />
    </div>

    ### **> re-edit**
    <div>
    <img src="assets/my_custom_re_edit_record.gif" height="280" />
    <img src="assets/screenshot_custom_move.png" height="280" />
    <img src="assets/screenshot_custom_detail.png" height="280" />
    <img src="assets/screenshot_custom_delete.png" height="280" />
    </div>

+ ### **Share to Community**
    Share your perfect challenges to the community for all MiruHiru users to participate

    <div>
    <img src="assets/custom_upload_record.gif" height="280" />
    <img src="assets/screenshot_custom_upload.png" height="280" />
    <img src="assets/screenshot_upload_to_community.png" height="280" />
    <img src="assets/screenshot_custom_in_community.png" height="280" />
    </div>
+ ### **Profile**
    See everything about your account here, including reviewing the records you left during the challenge, finding your favorite challenges, joining challenge through scanning QR Code and checking notifications.

    <div>
    <img src="assets/profile_feature_record.gif" height="280" />
    <img src="assets/screenshot_profile_like.png" height="280" />
    <img src="assets/screenshot_proflie_scan.png" height="280" />
    <img src="assets/screenshot_profile_notification.png" height="280" />
    </div>
## **Technical Highlights**
---
+ Implemented the **MVVM** architecture and **Repository Pattern** to improve the readability 
and maintainability
+ Applied **MapBox Maps SDK** and connected with **MapBox Geocoding API** to realize map presentation and location search
+ Tracked user location and calculated the distance between the user and their destination through **Google's Fused Location Provider**
+ **Customed MapView touch event** to solve the scrolling conflict between MapView and ScrollView
+ **Customed callback** to solve RecyclerView notifyItemRangeChange and moving item animation conflict and increased moving fluency
+ Combined **ViewModel** and **Fragment Result API** to pass data between fragments
+ Updated and retrieved real-time data through **Firestore SnapshotListener**, and successfully enabled the travel challenge to be played in a **multiplayer mode**
+ Stored challenge progress and editing progress in the **Cloud Firestore**, allowing users to temporarily exit or synchronize data between multiple devices
+ Applied ZXing Android Embedded and Code Scanner to accomplish scan **QR Code** to join travel challenge feature
+ Integrated **Google Sign-In** to achieve quick sign-in flow and increase user experience

## **Test Accounts**
---
General accounts will be limited by the distance of 30 meters to start the stage task when they are in the process of challenges. Using a test account can remove this restriction and has the ability to verify users' custom challenges as well.
Account | Password
--------|---------  
miru112822@gmail.com | miruhiru112822
hiru112822@gmail.com | miruhiru112822
## **API Keys**
---
To run this project, you will need to add the following tokens to your **local.properties** like below.  
You can get tokens from Mapbox website, checking [**Mapbox Documents**](https://docs.mapbox.com/android/maps/guides/install/) for more Details.
```
sdk.dir=C\:\\Users\\user\\AppData\\Local\\Android\\Sdk
mapbox.access.token="your mapbox public token"
mapbox.download.token="your mapbox secret token"
```
## **Requirment**
---
### Android SDK 26  
Gradle 7.3.3
### **Version History**
---
Version | Date
--------|------------
1.0.7 | 2022/12/14
1.0.6 | 2022/12/7
1.0.5 | 2022/11/28&ensp; (App Released)
1.0.3 | 2022/11/21
1.0.2 | 2022/11/14
1.0.1 | 2022/11/7

## **Contact**
---
Linkedin : https://www.linkedin.com/in/neil-tsai/  
Email : tsaichenghan999@gmail.com
