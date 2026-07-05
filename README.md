# CEMVA: Cavite Emergency Medical Volunteers Association

### Volunteer Management & Operational Duty Validation System

CEMVA is a modern, high-performance Android application built with **Jetpack Compose** designed specifically for the Cavite Emergency Medical Volunteers Association. It provides a robust, offline-first operational workflow allowing administrative personnel and officers to track volunteer deployments, log duty attendance, schedule training courses, and validate scheduled personnel status securely.

---

## 🚀 Key Features

### 📷 Live Camera QR Duty Validator
* **CameraX & ML Kit Integration**: Scan member-specific secure ID cards utilizing the physical device camera and Google's high-speed **ML Kit Barcode Scanning API**.
* **Active Operational Roster Cross-Referencing**: On scanning a QR badge, the system automatically checks the volunteer's identifier against the chosen active Duty Roster, Training, or Emergency Deployment.
* **Smart Access Clearances**:
  * **ACCESS GRANTED**: Displays a clean emerald green validation indicator if the volunteer is officially scheduled for the active operational roster.
  * **VALIDATION ALERT**: Displays a high-contrast crimson warning if the volunteer attempts validation but is missing from the active roster.
* **Bypass Operational Overrides**: Allows shift supervisors to bypass warning alerts to manually authorize and log volunteer attendance on the fly.
* **Custom Scanner UI**: Implements a darkened overlay mask, stylized neon-green reticles, a scanning laser line animation, and real-time interactive operation select dropdowns.

### 👥 Comprehensive Volunteer Directory
* Searchable and filterable roster of Cavite emergency medical personnel.
* Displays roles, emergency specializations, active certifications, and generated QR validation credentials.

### 📋 Operations & Duty Rosters
* Centralized hub for managing active Emergency Deployments, Event Standbys, and scheduled station Duty Rosters.
* Shows real-time metrics including total active personnel, on-scene locations, and live duty check-in counters.

### 🏛️ Off-Grid Performance & Synchronization
* **Room Local Database**: Complete local persistence ensuring the application remains 100% functional during emergency field deployments with zero cellular connectivity.
* **Intelligent Cloud Synchronization**: Synchronizes updates automatically with Firestore data once a network connection is re-established.

---

## 🛠️ Technical Architecture

* **UI Layer**: Modern, type-safe **Jetpack Compose** adhering to **Material Design 3 (M3)** guidelines.
* **Jetpack CameraX**: Fully lifecycle-aware camera implementation optimized for standard mobile form factors.
* **Google ML Kit Barcode Scanning**: Sub-millisecond localized visual barcode and QR parsing on-device.
* **Architecture Pattern**: Clean **Model-View-ViewModel (MVVM)** leveraging `StateFlow`, `collectAsStateWithLifecycle`, and asynchronous Kotlin Coroutines.
* **Database Layer**: Jetpack Room for reliable offline data caching.
* **Network & Sync**: Firebase Firestore integration (managed through a background sync system).

---

## 🏗️ Build & Run Instructions

To compile and run the CEMVA application locally:

### Prerequisites
* **Android Studio** (Ladybug or newer recommended)
* **Android SDK 34+**
* **Gradle 8.4+** with Kotlin DSL support

### Installation Steps

1. **Clone the Repository**:
   ```bash
   git clone https://github.com/your-username/cemva-android.git
   cd cemva-android
   ```

2. **Configure Environment Secrets**:
   Copy `.env.example` to `.env` and fill in your custom configurations if integrating external Firebase services:
   ```bash
   cp .env.example .env
   ```

3. **Open the Project**:
   * Open Android Studio.
   * Select **Open An Existing Project** and navigate to your cloned `cemva-android` root directory.

4. **Build the Project**:
   * Run a Gradle sync to download and align all dependencies.
   * Build the project using the command line or standard run configs:
     ```bash
     gradle assembleDebug
     ```

5. **Deploy**:
   * Attach a physical device (recommended for testing camera QR scanner functionality) or launch an emulator.
   * Press **Run** (`Shift + F10`) to deploy the application.

---

## 📄 License

This project is configured for Cavite Emergency Medical Volunteers Association. All rights reserved.
