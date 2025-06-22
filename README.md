

https://github.com/user-attachments/assets/7eb864f4-fb67-46e4-8f6d-b3d59a42b33b

[WhisprNet_Presentation.pptx](https://github.com/user-attachments/files/20837539/WhisprNet_Presentation.pptx)


# WhisprNet

WhisprNet is a privacy-first, real-time chat app for Android. It enables **anonymous, temporary conversations** with no sign-up, no data trails, and no permanent storage. Share a secret room code with trusted contacts and instantly join a secure chatroom—when everyone leaves, all messages disappear forever.

---

## Features

- **No Sign-Up Required:** Start chatting instantly with just a nickname.
- **Secret Room Codes:** Join or create private chatrooms using a unique code.
- **Real-Time Messaging:** Messages appear instantly for all room participants.
- **Automatic Message Deletion:** All messages vanish when the last user leaves the room.
- **No Data Storage:** Conversations are never stored on the server.
- **Share Room Code:** Invite friends easily with a built-in share feature.
- **Modern UI:** Clean, dark-themed interface with Material Design components.

---

## How It Works

1. **Enter a nickname** and a secret room code.
2. **Share the room code** privately with your contacts.
3. **Chat securely**—all messages are delivered in real time.
4. **Leave the room**—once all users exit, the chat is deleted forever.

---

## Technical Overview

- **Android (Kotlin)**
- **Firebase Realtime Database** for real-time, ephemeral messaging
- **Material Design** UI components
- **No persistent storage**: All messages are temporary and privacy-focused

---

## Getting Started

### Prerequisites

- Android Studio
- Firebase project with Realtime Database enabled
- `google-services.json` file in the `app/` directory

### Build & Run

1. Clone this repository:
   ```
   git clone https://github.com/MangalamGSinha/WhisprNet.git
   ```
2. Open in Android Studio.
3. Add your `google-services.json` file to the `app/` folder.
4. Sync Gradle and run the app on your device or emulator.

---

## Customization

- **App Icon:** Changeable via Android Studio's Image Asset Studio.
- **Colors & Theme:** Configurable in `res/values/colors.xml` and `styles.xml`.
- **Firebase Region:** Update the database URL in code if your Firebase database is not in the default region.

---

## Security & Privacy

- No account creation, no personal data collection.
- Messages are not stored after the session ends.
- Room codes are randomly generated and must be shared privately for access.

---

## License

MIT License

---

## Acknowledgments

- [Firebase](https://firebase.google.com/) for real-time backend
- [Material Design](https://material.io/) for UI inspiration
- All contributors and open-source libraries

---

**WhisprNet: Whisper in. Speak freely. Disappear without a trace.**
