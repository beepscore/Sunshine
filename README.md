# Purpose
Do Lesson 1 Sunshine.

# References

## Udacity Sunshine repository
<https://github.com/udacity/Sunshine-Version-2>

Online course "Android nanodegree" from Udacity/Google  
<https://www.udacity.com/course/android-developer-nanodegree--nd801>  
Developing Android Apps: Fundamentals
Lesson 1: Create project sunshine
<https://www.udacity.com/course/viewer#!/c-ud853-nd/l-1395568821/e-3603098832/m-3640878702>

Discussion Forum
<http://discussions.udacity.com/c/nd801-2015-05-28/developing-android-apps-android-fundamentals>

## Android device
Nexus 4 running Android 5.1.1

# Results

## Replace launcher icon images ic_launcher.png
Default app contains default ic_launcher.png
res/mipmap-mdpi 48x48
res/mipmap-hdpi 72x72
res/mipmap-xhdpi 96x96
res/mipmap-xxhdpi 144x144

udacity image is 144x144.

Online icon generator, requests creative commons attribution. (didn't use it)
http://romannurik.github.io/AndroidAssetStudio/index.html

### Preview
Used Preview app to scale images.
Manually replaced images.

### Android Studio
This option is simpler than using Preview.app.
Right click on app.
Select new / image asset. This launches Asset Studio.
Use Asset Studio to generate images.

### Gradle command line commands

Grant gradlew execute permission. Only need to do this once.

    chmod +x gradlew

Compile target

    ./gradlew assembleDebug

Use adb to install apk on device.

    adb install -r app/build/outputs/apk/app-debug-unaligned.apk

Start app running

    adb shell am start -n com.example.android.sunshine.app/com.example.android.sunshine.app.MainActivity
