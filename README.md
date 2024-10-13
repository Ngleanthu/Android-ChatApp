# Chat App Application

## Description
The Chat App is a real-time messaging application built with Android Studio and Firebase, enabling users to communicate seamlessly via text. It supports group chats, private messaging, and multimedia sharing, ensuring fast, secure, and reliable communication.

## Member
- Lương Thị Diệu Thảo - 22120337
- Nguyễn Lê Anh Thư - 22120354
- Trần Hoàng Minh Thư - 22120356
- Nguyễn Lê Thanh Trúc - 22120393

## Technologies
- Android Studio: Development environment for building the Android application.
- Firebase Realtime Database: For real-time data synchronization and storing messages.
- Firebase Authentication: For secure user sign-in and authentication.
- Firebase Cloud Storage: For multimedia (images, videos) sharing.
- Firebase Cloud Messaging (FCM): For sending notifications.
- Java: Main programming language for the app development.
- XML: For designing the user interface.

## Feature
- Real-time Messaging: Instant messaging with support for private and group chats.
- Multimedia Sharing: Share images, videos, and other media through the chat.
- User Authentication: Secure login system using Firebase Authentication (email/password, Google sign-in).
- Push Notifications: Receive real-time notifications for new messages via Firebase Cloud Messaging.
- Responsive UI: User-friendly and adaptive interface to fit various screen sizes.

## Database modeling
- User: Stores user information such as user ID, name, email, and profile picture.
- Messages: Stores message content, sender ID, receiver ID (or group ID), and timestamp.
- Groups: Stores group chat information, including group name, list of members, and group ID.
- Media: Stores URLs of shared media files (images/videos) hosted on Firebase Cloud Storage.

## Run
- *Step 1*: Clone the repository.
- *Step 2*: Open the project in Android Studio.
- *Step 3*: Configure Firebase in the project by adding the google-services.json file.
- *Step 4*: Run the project on an Android emulator or physical device.

## References

### Documentation & Tools
1. **Firebase Documentation**:
   - [Firebase Realtime Database Documentation](https://firebase.google.com/docs/database)
   - [Firebase Authentication Documentation](https://firebase.google.com/docs/auth)
   - [Firebase Cloud Storage Documentation](https://firebase.google.com/docs/storage)
   - [Firebase Cloud Messaging Documentation](https://firebase.google.com/docs/cloud-messaging)

2. **Android Development Documentation**:
   - [Android Studio Official Documentation](https://developer.android.com/studio)
   - [Android Developer Guide](https://developer.android.com/guide)

3. **Java Documentation**:
   - [Java SE Documentation](https://docs.oracle.com/javase/8/docs/)

### Best Practices & Standards
1. **Google Material Design Guidelines**:
   - [Material Design Guidelines](https://material.io/design)

2. **Firebase Security Rules**:
   - [Firebase Security Rules Documentation](https://firebase.google.com/docs/rules)

3. **Clean Code Principles**:
   - [Clean Code Book](https://www.amazon.com/Clean-Code-Handbook-Software-Craftsmanship/dp/0136083239)

### Additional Learning Resources
1. **Stack Overflow**:
   - [Stack Overflow](https://stackoverflow.com/)

2. **GitHub**:
   - [GitHub](https://github.com/)

3. **YouTube**:
   - [YouTube Android Development Tutorials](https://www.youtube.com/results?search_query=android+development)
