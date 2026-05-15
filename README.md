# Nimma-Guru 🪷

**Nimma-Guru** is a community-driven Android mentorship platform designed to bridge the generational gap by connecting retired professionals ("Gurus") with students and young professionals seeking guidance, career advice, and life lessons.

## ✨ Key Features

- **Elderly-Friendly UI/UX:** A thoughtfully designed, accessible interface utilizing Jetpack Compose. Includes large typography, high-contrast themes, and intuitive navigation specifically tailored for senior users.
- **Smart Mentorship Matching:** Advanced search and filtering capabilities to find the right Guru based on industry, expertise, and availability.
- **Session Booking & Calendar:** Seamless scheduling system for 1-on-1 mentorship sessions with integrated calendar views.
- **Real-time Chat & AI Assistant:** In-app messaging system to communicate directly with Gurus, augmented by an AI assistant to help draft messages and summarize sessions.
- **Appreciation Wall (Fame Screen):** A dedicated space for students to express gratitude, leave reviews, and highlight the impact their Gurus have made.
- **Smart Reminders:** Automated push notifications and reminders for upcoming sessions and messages.

## 🛠️ Tech Stack

- **Language:** [Kotlin](https://kotlinlang.org/)
- **UI Toolkit:** [Jetpack Compose](https://developer.android.com/jetpack/compose)
- **Architecture:** MVVM (Model-View-ViewModel) with Repository Pattern
- **Asynchronous Programming:** Kotlin Coroutines & Flow
- **Design System:** Material Design 3
- **Backend/Database:** Firebase (Firestore, Authentication, Cloud Messaging - *Mock Data/Integration Ready*)

## 🏗️ Architecture

The app follows a modern Android architecture:
- **Presentation Layer:** Jetpack Compose screens and ViewModels handling UI state and events.
- **Domain/Repository Layer:** Repositories abstracting data sources (e.g., `UserRepository`, `SessionRepository`, `ChatRepository`).
- **Data Layer:** Firebase integrations and local data managers (`BookingManager`, `ChatAIManager`, `SmartSearchManager`).

## 🚀 Getting Started

1. **Clone the repository:**
   ```bash
   git clone https://github.com/Dhyanbal/Nimma_Guru.git
   ```
2. **Open in Android Studio:** Open the cloned directory in the latest version of Android Studio.
3. **Firebase Setup:** 
   - Ensure the `google-services.json` file is placed in the `app/` directory.
   - Sync the project with Gradle files.
4. **Run the App:** Build and deploy to an Android emulator or physical device.

## 🤝 Contributing

Contributions, issues, and feature requests are welcome! 
Feel free to check [issues page](https://github.com/Dhyanbal/Nimma_Guru/issues) if you want to contribute.



## 🛣️ Roadmap

- [x] Initial UI/UX Design & Architecture Setup
- [x] Mock Data Integration
- [ ] Implement actual Firebase backend connectivity
- [ ] Push Notifications integration
- [ ] Video/Audio call features for mentorship sessions
- [ ] Multilingual support (Kannada, Hindi, etc.)

## 📜 License

This project is open-source and available under the [MIT License](LICENSE).

## 📫 Contact

If you have any questions, suggestions, or feedback, feel free to reach out or create an issue!
- **Developer:** [Dhyanbal](https://github.com/Dhyanbal)

---
*Built with ❤️ to empower generations through knowledge sharing.*
