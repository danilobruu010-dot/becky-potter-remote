# Becky Potter Remote Access - Getting Started Guide

## 🎯 Setup Android Development

### 1. Prerequisites
- Android Studio 4.2+
- JDK 11+
- Android SDK API 24+
- Gradle 7.0+

### 2. Clone & Setup
```bash
git clone https://github.com/danilobruu010-dot/becky-potter-remote.git
cd becky-potter-remote/android
```

### 3. Firebase Configuration
1. Go to [Firebase Console](https://console.firebase.google.com/)
2. Create new project: "Becky Potter"
3. Add Android app
4. Download `google-services.json`
5. Place in `android/app/google-services.json`

### 4. Build & Run
```bash
./gradlew build
./gradlew installDebug
```

## 🔐 Firebase Setup

### 1. Authentication
- Enable Email/Password in Firebase Console
- Firebase Auth will auto-handle registration/login

### 2. Firestore Database
- Create Firestore database (Production mode)
- Deploy security rules:
```bash
firebase deploy --only firestore:rules
```

### 3. Cloud Functions
```bash
cd backend/functions
npm install
firebase deploy --only functions
```

## 📱 App Modules

### Activities
- **MainActivity** - Home/Navigation hub
- **LoginActivity** - Email/Password login
- **SignupActivity** - User registration
- **QRCodeLoginActivity** - QR Code + PIN authentication
- **ScreenShareActivity** - Real-time screen capture
- **RemoteControlActivity** - Remote gesture control
- **ChatActivity** - Text messaging
- **AdminDashboard** - Session management

### Services
- **ScreenCaptureService** - Background screen capturing
- **RemoteControlService** - Execute remote commands
- **AudioService** - Audio recording/playback

### Utilities
- **EncryptionUtil** - AES-256 encryption/decryption
- **QRCodeUtil** - QR code generation
- **CryptoUtil** - Cryptographic operations

## 🔑 Key Features

✅ **Authentication**
- Email/Password with Firebase Auth
- QR Code + 6-digit PIN pairing
- Session tokens for security

✅ **Screen Sharing**
- Real-time screen capture at 2 FPS
- Compressed JPEG transmission
- Encrypted data transfer

✅ **Remote Control**
- Gesture-based control (tap, scroll, swipe, fling)
- Long-press support
- Event streaming to Firestore

✅ **Communication**
- Real-time chat with timestamps
- Audio bidirectional support
- Message encryption

✅ **Admin Panel**
- View all active sessions
- Monitor technician activity
- Session analytics

## 🔒 Security

- **E2E Encryption** - AES-256 for all data
- **RBAC** - Role-based access control (Technician/Client/Admin)
- **Rate Limiting** - Automatic cleanup of old sessions
- **Token Management** - JWT-based session tokens

## 📊 Firestore Collections

```
users/
  └── userId
      ├── name
      ├── email
      ├── userType (Technician/Client/Admin)
      ├── createdAt
      └── status

sessions/
  └── sessionId
      ├── deviceId
      ├── token
      ├── pin
      ├── status (waiting/active/idle)
      ├── screenImage (encrypted)
      ├── lastUpdate
      ├── messages/ (subcollection)
      └── gestures/ (subcollection)
```

## 🚀 Deployment

### Android App Release
1. Generate signing key:
```bash
keytool -genkey -v -keystore release.keystore -keyalg RSA -keysize 2048 -validity 10000 -alias release
```

2. Update `gradle.properties` with keystore path
3. Build release APK:
```bash
./gradlew assembleRelease
```

### Firebase Deploy
```bash
firebase deploy
```

## 📞 Support

- GitHub Issues: [Report Bugs](https://github.com/danilobruu010-dot/becky-potter-remote/issues)
- Documentation: See `/docs` folder

## 📄 License

Proprietary - 2026
