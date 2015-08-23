# Purpose
Do Lesson 1, 2 Sunshine.

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

### Reference repository
Copied image resources from project reference
<https://github.com/udacity/Sunshine-Version-2>

## Gradle command line commands

Grant gradlew execute permission. Only need to do this once.

    chmod +x gradlew

Compile target

    ./gradlew assembleDebug

Use adb to install apk on device.

    adb install -r app/build/outputs/apk/app-debug-unaligned.apk

Start app running

    adb shell am start -n com.example.android.sunshine.app/com.example.android.sunshine.app.MainActivity

## json
To prettify format json in vim

    :%!python3 -m json.tool

## Settings
Android 10 Gingerbread used PreferenceActivity.
Android >= 11 can use PreferenceFragment.
Android Studio Settings template will generate code to support both, but may show deprecation warnings.
Instead, for this project use Udacity supplied gist.
<https://gist.github.com/udacityandroid/41aca2eb9ff6942e769b>

## Location city,us more reliable than zip code
Around 2015-07-03 1:00am Pacific, I set app to location 33710.
I thought this would give St Petersburg FL USA but got Samonac FR, very hot!

Instead in app enter location St Petersburg,us

US zip code 988053 returns Castroreale IT.
Instead enter location Seattle,us

## Phone and Tablet UI
### Phone
App uses a one pane layout.
MainActivity shows ForecastFragment.
If the user taps on a row, DetailActivity shows DetailFragment.
### Tablet
App uses two pane Master/Detail layout.
MainActivity shows ForecastFragment and DetailFragment.
DetailActivity isn't used.

## Passing information
### Callback
ForecastFragment declares a callback interface to send info to an implementing actiivty.
This decouples ForecastFragment from a particular activity (e.g. MainActivity)
This also decouples the fragment from other fragments (e.g. DetailFragment)

MainActivity implements the interface.

### Fragment arguments
Every fragment has a property named arguments of type Bundle.
MainActivity and DetailActivity may instantiate DetailFragment
These activities use detailFragment.setArguments to pass info to detailFragment.

#### Fragment arguments versus savedInstanceState
Fragment arguments Bundle is separate from savedInstanceState Bundle.
Fragment arguments is set when fragment is initialized and after that they are read only.
savedInstanceState Bundle is read/write.
savedInstanceState is passed in to onSaveInstanceState()
and app may put key/value pairs into it
to store and update information after the app has been running.
Can be used to restore fragment after device rotation or system kill.
For example can use it in onCreate or onCreateView.
