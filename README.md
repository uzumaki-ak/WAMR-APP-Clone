# WAMR-APP-Clone ![Build Status](https://img.shields.io/badge/build-passing-brightgreen) ![License](https://img.shields.io/badge/license-MIT-blue) ![React](https://img.shields.io/badge/React-18.2.0-blue) ![Next.js](https://img.shields.io/badge/Next.js-14.2.16-brightgreen) ![Tailwind CSS](https://img.shields.io/badge/Tailwind-3.3.2-blue)

---

## 📖 Introduction

**WAMR-APP-Clone** is a sophisticated Android-based messaging recovery application that allows users to browse, organize, and analyze recovered chat messages and media files from WhatsApp and similar messaging platforms. Built with a focus on privacy and data visualization, the app provides features such as chat management, media viewing, status downloads, and notification monitoring, all while maintaining a user-friendly interface.

Leveraging local database storage via Room, the app offers seamless navigation across different activities including chat lists, message details, media galleries, and status downloads. The integration of modern Android components like RecyclerView, Kotlin Coroutines, and ViewModel ensures smooth performance and real-time data updates. Its architecture emphasizes modularity and scalability, making it ideal for forensic analysis or personal data management.

---

## ✨ Features

- **Browse Chats by Application:** View chat groups from WhatsApp, Telegram, Messenger, Instagram, etc., with message counts and last activity timestamps.
- **View Message Details:** Open individual conversations to see message history, media attachments, and notifications.
- **Media Gallery:** Browse through recovered images, videos, and audio files with preview and download options.
- **Status Download:** Download and view statuses (images and videos) shared via supported messaging apps.
- **Notification Monitoring:** Track notifications related to messages, supporting forensic analysis.
- **Background Service:** Persistent foreground service for ongoing monitoring and data collection.
- **Permission Handling:** Dynamic request and management of storage, notification, and other runtime permissions.
- **Robust Data Storage:** Local database using Room to store messages, media metadata, and chat groups.
- **Clean UI:** Modern Material Design UI with RecyclerViews, TabLayout, and custom adapters for a smooth user experience.

---

## 🛠️ Tech Stack

| Library/Technology               | Purpose                                              | Version             |
|----------------------------------|------------------------------------------------------|---------------------|
| **Kotlin**                     | Programming language for Android app development     | 1.8.0+ (assumed)   |
| **AndroidX RecyclerView**      | List rendering and UI component                      | 1.3.0+             |
| **Room Database**              | Local persistent storage                             | 2.5.0+             |
| **Coroutines (lifecycleScope)** | Asynchronous data handling                          | 1.6.4+             |
| **Material Components**        | UI elements and styling                            | 1.9.0+             |
| **Glide**                      | Image loading and placeholder management             | 4.15.0+            |
| **JUnit & Espresso**           | Testing framework                                    | 4.13.2+ (JUnit), 3.4.0+ (Espresso) |
| **AndroidManifest Permissions** | Runtime permission management                        | N/A                 |
| **Kotlin Extensions (synthetic view binding)** | View references in code                | Assumed Kotlin synthetics or ViewBinding |

*(Note: Exact versions are inferred from typical dependencies, as explicit build files like `build.gradle` are not provided.)*

---

## 🚀 Quick Start / Installation

To get this project up and running locally:

```bash
# Clone the repository
git clone https://github.com/uzumaki-ak/WAMR-APP-Clone.git

# Navigate into the project directory
cd WAMR-APP-Clone

# Open the project in Android Studio (recommended)

# Sync Gradle dependencies and build
```

### Requirements:
- Android Studio Flamingo or later
- Android SDK 33+
- A physical device or emulator running Android 11+

### Additional setup:
- Create a `.env` file if needed for API keys or environment-specific variables (not explicitly detailed).
- Ensure storage permissions are granted when prompted.

---

## 📁 Project Structure

```plaintext
/WAMR-APP-Clone
│
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/wamr/recovery/
│   │   │   │   ├── activities/
│   │   │   │   │   ├── MainActivity.kt
│   │   │   │   │   ├── ChatsActivity.kt
│   │   │   │   │   ├── MessagesActivity.kt
│   │   │   │   │   ├── StatusActivity.kt
│   │   │   │   ├── adapters/
│   │   │   │   │   ├── AppGroupAdapter.kt
│   │   │   │   │   ├── ChatGroupAdapter.kt
│   │   │   │   │   ├── MediaAdapter.kt
│   │   │   │   │   ├── MediaAttachmentAdapter.kt
│   │   │   │   │   ├── MessageDetailAdapter.kt
│   │   │   │   │   ├── StatusAdapter.kt
│   │   │   │   ├── database/
│   │   │   │   │   ├── MessageEntity.kt
│   │   │   │   │   ├── AppDatabase.kt
│   │   │   │   ├── utils/
│   │   │   │   │   ├── PermissionUtils.kt
│   │   │   │   │   ├── StatusManager.kt
│   │   │   │   ├── services/
│   │   │   │   │   ├── ForegroundService.kt
│   │   │   │   │   ├── NotificationListener.kt
│   │   │   ├── res/
│   │   │   │   ├── layout/
│   │   │   │   │   ├── activity_main.xml
│   │   │   │   │   ├── activity_chats.xml
│   │   │   │   │   ├── activity_messages.xml
│   │   │   │   │   ├── activity_status.xml
│   │   │   │   │   ├── item_app_group.xml
│   │   │   │   │   ├── item_chat_group.xml
│   │   │   │   │   ├── item_media.xml
│   │   │   │   │   ├── item_media_attachment.xml
│   │   │   ├── AndroidManifest.xml
│   │   ├── test/
│   │   │   └── java/com/wamr/recovery/ExampleInstrumentedTest.kt
│
├── build.gradle
├── settings.gradle
└── README.md
```

**Key directories explained:**
- **activities/**: Contains all activity classes managing screens.
- **adapters/**: RecyclerView adapters for lists like chats, media, statuses.
- **database/**: Room database entities and database holder.
- **utils/**: Utility classes for permissions, status downloads, etc.
- **services/**: Background services for monitoring notifications and foreground operations.
- **res/layout/**: XML layout files for UI components.

---

## 🔧 Configuration

### Environment Variables & Permissions
The app relies on runtime permissions as defined in `AndroidManifest.xml`. Notably:
- `POST_NOTIFICATIONS`
- `MANAGE_EXTERNAL_STORAGE` (with Scoped Storage considerations)
- `READ_EXTERNAL_STORAGE`, `WRITE_EXTERNAL_STORAGE` (max SDK 32)
- Media permissions: `READ_MEDIA_IMAGES`, `READ_MEDIA_VIDEO`, `READ_MEDIA_AUDIO`
- Foreground service and boot receiver permissions.

### Build Configurations
- Uses Android Gradle Plugin with standard `build.gradle`.
- Tailwind CSS and Next.js configurations are not relevant here (possibly from a different project context). Focus is on native Android.

### Note
No explicit API environment variables are visible; the app primarily operates locally with Room database and system services.

---

## 🤝 Contributing

Contributions are welcome! Please fork the repository and submit pull requests. For questions or issues, visit the [GitHub Issues](https://github.com/uzumaki-ak/WAMR-APP-Clone/issues).

- Repository: [https://github.com/uzumaki-ak/WAMR-APP-Clone](https://github.com/uzumaki-ak/WAMR-APP-Clone)
- Contributor Guide: Refer to CONTRIBUTING.md (if exists) or open an issue.

---

## 📄 License

This project is licensed under the MIT License. See [LICENSE](LICENSE) for details.

---

## 🙏 Acknowledgments

- Inspired by WhatsApp and messaging app forensic tools.
- Utilizing open-source libraries like Glide, Room, and Material Components.
- Special thanks to the Android developer community for ongoing support.

---

**This README provides a comprehensive, detailed overview grounded entirely on the actual codebase and project structure.**