# AirPods Pro 3 Android Companion (Legacy)

אפליקציית Android מקורית ב-Java, בלי AndroidX/Compose, עם יעד מינימלי Android 4.4 (API 19) והתאמה לטלפונים עם מקשים פיזיים.

## מה עובד בפועל ב-Android 4.4

- סריקת BLE של משדרי AirPods.
- זיהוי AirPods / AirPods Pro / AirPods Pro 2 / AirPods Pro 3 לפי Apple Manufacturer Data.
- מצב סוללה של שמאל, ימין וקופסה כשנתוני הפרסום זמינים.
- זיהוי טעינה וזיהוי באוזן מתוך המידע המשודר.
- RSSI, כתובת Bluetooth, שם התקן ו-last seen.
- ממשק שניתן לנווט בו עם D-pad/מקשים.
- שמירת התקן נבחר והעדפות מקומיות.

## פיצרים מתקדמים

הממשק מציג גם ANC, Transparency, Adaptive Audio, Conversation Awareness, Spatial Audio והגדרות נוספות, אבל שינוי firmware דרך הפרוטוקול הקנייני של Apple אינו מובטח ב-Android 4.4. בגרסה זו הכפתורים מסבירים את מגבלת הפלטפורמה במקום להעמיד פנים שפקודה נשלחה.

AirPods Pro 3 כוללות לפי Apple Active Noise Cancellation, Adaptive Audio, Transparency, Conversation Awareness, Voice Isolation, Personalized Volume, Personalized Spatial Audio with dynamic head tracking, Hearing Health ו-Live Translation.

## Build

הפרויקט נבנה עם Gradle 8.7 + Android Gradle Plugin 8.6.1 + compileSdk 35.

    gradle --no-daemon assembleDebug assembleRelease

GitHub Actions בונה APK בכל push ומעלה Artifact בשם AirPodsPro3-APK.

## מקורות

- Apple AirPods Pro 3 technical specifications: https://www.apple.com/airpods-pro/specs/
- OpenPods (GPL) reverse-engineering reference: https://github.com/adolfintel/OpenPods
- LibrePods protocol/features reference: https://github.com/librepods-org/librepods

## License

This project is released under GPL-3.0-or-later.

Build trigger: 2026-10-04.
