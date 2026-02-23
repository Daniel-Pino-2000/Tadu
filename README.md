# 📋 Tadu – Smart Task Management App

![Android](https://img.shields.io/badge/Platform-Android-green)
![Kotlin](https://img.shields.io/badge/Language-Kotlin-blue)
![Firebase](https://img.shields.io/badge/Backend-Firebase-orange)

## 🚀 Overview

**Tadu** is a modern task management Android application designed to help users stay organized and productive.

With Tadu, you can quickly create tasks, set deadlines, choose priority levels, and add locations.

The app supports **cloud synchronization**, **local offline storage**, and **task reminders**.

### ✨ Core Functionalities

- ✅ User authentication (Sign up, Login, Logout, Delete account) using Firebase  
- ☁️ Cloud task storage  
- 📂 Local persistence using Room database  
- 🔔 Task reminders using BroadcastReceiver, AlarmManager, NotificationManager  
- 📅 Deadline and priority management  
- 📍 Location tagging  
- 📤 Calendar integration

---

## 📸 Screenshots

---

### 🔐 Authentication Screens

#### Login / Registration
![Login Screen](screenshots/login.png)

---

### 🏠 Main Task Dashboard

![Home Screen](screenshots/home.png)

---

### ✏️ Task Detail & Reminder

![Task Detail Screen](screenshots/editor.png)

---

### 📅 Calendar View

![Calendar Screen](screenshots/calendar.png)

---

### ⚙️ Settings Screen

![Settings Screen](screenshots/settings.png)

---

### 📜 Task History

![History Screen](screenshots/history.png)

---

## 🏗️ Architecture Highlights

- MVVM (Model-View-ViewModel) pattern  
- Repository abstraction layer  
- Separation of UI, business logic, and data storage  
- Offline-first data design

---

## 🛠️ Tech Stack

- Kotlin / Android SDK  
- Jetpack Compose (if applicable)  
- Firebase Authentication & Cloud Storage  
- Room Persistence Library  
- Notification / Reminder Library (**<REMINDER_LIBRARY_NAME>**)

---

## 📦 Installation

### 1. Clone the Repository

```bash
git clone https://github.com/yourusername/tadu.git
