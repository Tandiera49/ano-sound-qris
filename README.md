# ANO SOUND QRIS

Android app that listens for the genuine GoPay Merchant QRIS-success notification and speaks the amount in Indonesian.

## Build APK

The project includes `.github/workflows/build-apk.yml`. Push this folder to a GitHub repository, then open **Actions → Build ANO SOUND QRIS APK → Run workflow**. Download the `ano-sound-qris-debug-apk` artifact after the workflow succeeds.

## Install and configure

1. Install `app-debug.apk`.
2. Open ANO SOUND QRIS.
3. Tap **Aktifkan Akses Notifikasi** and enable ANO SOUND QRIS.
4. Ensure GoPay Merchant notifications are enabled.
5. Tap **Tes Suara** to check Indonesian TTS.
6. Use the Android Text-to-Speech settings to select/download an Indonesian female voice if available on the device.

## Important

- The app only processes notifications whose sender app label/package identifies GoPay Merchant/GoPay and whose title indicates a QRIS payment was received.
- Amount extraction expects a notification body containing a value such as `Rp 2.500`.
- Female voice availability depends on the Android TTS engine and installed Indonesian voices; the app cannot force a voice that the device does not provide.
- Notification access is powerful. This app reads notification content locally and does not upload it anywhere.
- This is an independently built utility, not an official GoPay product. Always verify transactions in GoPay Merchant history as well.
