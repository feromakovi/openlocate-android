# CHANGELOG

`OpenLocate-Android` adheres to [Semantic Versioning](http://semver.org/).

## [1.3.0](https://github.com/OpenLocate/openlocate-ios/tag/1.3.0)

#### Added

- Upgrade to API 26 as compile version. (#90)
- Ability to force send current data collected. (#90)
- Data now includes vertical accuracy (altitude accuracy)


## [1.2.0](https://github.com/OpenLocate/openlocate-ios/tag/1.2.0)

#### Added

- Ability to include OpenLocate in apps targeting Android API Level 15 or higher. Data collection will only work when running on device with API Level 19 or higher.

## [1.1.11](https://github.com/OpenLocate/openlocate-ios/tag/1.1.11)

#### Fixed

- Add fix SQL lite crash when device storage is full.
- Add fix for crash when accessing DB from multiple processes.
- Add fix for crash when shutting down Google Place Services Network Manager on some Android devices.

## [1.1.10](https://github.com/OpenLocate/openlocate-ios/tag/1.1.10)

#### Fixed

- Fix crash related to Context.startService() throwing a security exception from some specific Android OEMs doing battery management

## [1.1.9](https://github.com/OpenLocate/openlocate-ios/tag/1.1.9)

#### Fixed

- Fix crash related to Google Play Services not being up to date.

## [1.1.8](https://github.com/OpenLocate/openlocate-ios/tag/1.1.8)

#### Fixed

- Fix possible crash related to Google Play Services calling location listener delegate after it has been removed.

## [1.1.7](https://github.com/OpenLocate/openlocate-ios/tag/1.1.7)

#### Fixed

- Fix crash related to null OpenLocate.Configuration if service was killed by user.

## [1.1.6](https://github.com/OpenLocate/openlocate-ios/tag/1.1.6)

#### Fixed

- Fix crash that may occour on the location service if SDK is not initialized correctly.

## [1.1.5](https://github.com/OpenLocate/openlocate-ios/tag/1.1.5)

#### Fixed

- Fix crash on Android O devices when the service was restarted

## [1.1.4](https://github.com/OpenLocate/openlocate-ios/tag/1.1.4)

#### Fixed

- Fix crash that occours if advertising identifier fetch throws an exception (#72)

## [1.1.3](https://github.com/OpenLocate/openlocate-ios/tag/1.1.3)

#### Fixed

- Fix crash if phone has no telephony manager

## [1.1.2](https://github.com/OpenLocate/openlocate-ios/tag/1.1.2)

#### Fixed

- Fix crash that may occour if a null `Activity` is passed to `startTracking(...)` method

## [1.1.1](https://github.com/OpenLocate/openlocate-ios/tag/1.1.1)

#### Fixed

- Fix crash that may occour if phone has no active network.

---

## [1.1.0](https://github.com/OpenLocate/openlocate-ios/tag/1.1.0)

#### Added

- Add ability to send location data to mulitple URL endpoints
- Change default transmission interval to 6 hours instead of 8 hours

---

## [1.0.1](https://github.com/OpenLocate/openlocate-android/tag/1.0.1)

#### Fixed

- `android:allowBackup` was set to false in the AndroidManifest.xml. Removed it so it doens't cause a manifest merge conflict.

---

## [1.0.0](https://github.com/OpenLocate/openlocate-android/tag/1.0.0)

#### Added

- A CHANGELOG to the project documenting each official release.
- Add support for passive location tracking accuracy (`LocationAccuracy.NO_POWER`) (#61)
- Non-Android O devices now no longer show a foreground notification when OpenLocate is running. (#60)
- The SDK now handles the location permission, and google play services alert dialog through a much simplified API. (#60)

#### Fixed

- The correct location provider gets logged 

---

## [0.1.0]

Initial release!
