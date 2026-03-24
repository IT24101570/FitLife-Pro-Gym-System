# FitLife Pro - Gym & Fitness Management App

[![Build Status](https://img.shields.io/badge/build-passing-brightgreen)]()
[![Version](https://img.shields.io/badge/version-1.1.0-blue)]()
# FitLife Pro - Gym & Fitness Management App

Build Status • Version • Platform • License

A comprehensive gym and fitness management Android application featuring workout tracking, session booking, subscription management, progress analytics, and personalized fitness guidance.

---

## 📱 Features

### ✅ Implemented (13/14 = 93%)

---

### 1. Payment & Subscription System

* 3 subscription tiers (Basic, Premium, Elite)
* Monthly and yearly billing with discounts
* Promo code system
* Payment history tracking
* Stripe integration ready

---

### 2. Workout History & Progress Analytics

* Calendar view with workout markers
* Interactive progress charts
* Personal records tracking
* Body measurements with BMI
* Achievement system (15 types)
* Statistics dashboard

---

### 3. Booking History & Management

* Session booking with trainers/doctors
* Status-based filtering
* Cancel and reschedule functionality
* 5-star rating and review system
* Rebook completed sessions

---

### 4. Dark Mode

* Light, Dark, and System Default themes
* Material Design 3 color system
* AMOLED-friendly true black
* Persistent theme preferences

---

### 5. Exercise Library

* Browse 100+ exercises
* Filter by muscle group, equipment, difficulty
* Search functionality
* Favorite exercises
* Detailed instructions

---

### 6. Onboarding & Tutorial

* 5-screen welcome experience
* ViewPager2 smooth swiping
* Conversational onboarding option
* Skip functionality

---

### 7. Social Feed & Community

* Post workouts and progress
* Like and comment on posts
* Real-time community updates
* Social sharing of achievements

---

### 8. Video Library & Classes

* On-demand video classes
* Category-based browsing
* Professional instructor videos
* Progress tracking within videos

---

### 9. Meal Planning & Nutrition

* Nutrition tracker with macro breakdown
* Meal plan management
* Recipe details with nutrition info
* Integration with fitness goals

---

### 10. Medical Reports

* Upload and view medical reports
* Doctor-patient report sharing
* Health history tracking
* Secure document storage

---

### 11. Gamification

* Global and local leaderboards
* Achievement badges
* Experience points (XP) system
* Competitive challenges

---

### 12. AI-Powered Assistance

* AI Chatbot for fitness queries
* Personalized workout suggestions
* Intelligent meal planning assistant

---

### 13. Personalized Fitness Assessment & Guidance ⭐ NEW

A comprehensive personalized fitness planning system that connects members with trainers and doctors for goal-based guidance.

#### 👤 Member Features:

* Upload progress and body photos
* Select primary fitness goals:

    * Weight Gain
    * Weight Loss
    * Maintain Weight
    * Muscle Building
    * Fat Reduction
* Add health notes or special concerns
* View complete personalized plans

#### 🏋️ Trainer Features:

* Access member goal submissions
* View uploaded progress photos
* Provide:

    * Weekly workout targets
    * Exercise recommendations
    * Video guidance links
    * Custom fitness advice

#### 🩺 Doctor Features:

* Review member goals and health notes
* Create structured meal plans:

    * Breakfast / Lunch / Dinner / Snacks
* Provide personalized nutrition advice

#### 🔄 System Workflow:

* Member submits fitness assessment
* Trainer assigns workout guidance
* Doctor assigns meal plan
* Member views full plan and tracks progress

#### ⚙️ Technical Highlights:

* Role-based workflow (Member ↔ Trainer ↔ Doctor)
* Clean model-based architecture
* Dashboard-integrated experience
* Firebase-ready backend structure
* Extensible for future AI recommendations

---

## 🔐 Security Features

* Firebase Authentication (email/password)
* Password reset via email
* Email verification
* Role-based access control (RBAC)
* Secure token-based sessions
* Firebase Security Rules

---

## 🎨 UI/UX

* Material Design 3
* Dark mode support
* Smooth animations
* Intuitive navigation
* Loading states
* Error handling
* Empty state illustrations

---

## 🏗️ Architecture

### Tech Stack

* **Language:** Java
* **Platform:** Android (Min SDK 26, Target SDK 36)
* **Backend:** Firebase

    * Authentication
    * Realtime Database
    * Cloud Storage
    * Cloud Messaging
* **UI:** Material Design 3
* **Charts:** MPAndroidChart
* **Image Loading:** Glide
* **Networking:** Retrofit + OkHttp

---

### Project Structure

```
app/
├── src/main/
│   ├── java/com/example/fit_lifegym/
│   │   ├── adapters/
│   │   ├── models/
│   │   │   ├── FitnessGoalSubmission.java
│   │   │   ├── TrainerGuidance.java
│   │   │   ├── DoctorMealPlan.java
│   │   ├── services/
│   │   ├── utils/
│   │   ├── di/
│   │   ├── webrtc/
│   │   └── *.java (Activities)
│   └── res/
│       ├── layout/
│       │   ├── activity_personalized_plan.xml
│       │   ├── activity_trainer_guidance.xml
│       │   ├── activity_doctor_meal_plan.xml
│       │   ├── activity_personalized_plan_result.xml
│       ├── drawable/
│       ├── values/
│       └── values-night/
└── build.gradle.kts
```

---

### Key Components

#### Activities:

* Authentication (Login, Register, Onboarding)
* Dashboard & Social Feed
* Workout Tracking & History
* Booking & Professionals
* Payment & Subscriptions
* Exercise & Video Library
* Nutrition & Meal Plans
* Medical Reports & Leaderboard
* **Personalized Fitness Plan (NEW)**

#### Models:

* User, Booking, Professional
* WorkoutSession, Exercise, WorkoutPlan
* NutritionLog, MealPlan, Recipe
* MedicalReport, Achievement
* **FitnessGoalSubmission (NEW)**
* **TrainerGuidance (NEW)**
* **DoctorMealPlan (NEW)**

---

## 🚀 Getting Started

### Prerequisites

* Android Studio Hedgehog or later
* JDK 17 or later
* Android SDK 26+
* Firebase account

---

### Installation

```bash
git clone https://github.com/yourusername/fitlife-pro.git
cd fitlife-pro
```

---

### Firebase Setup

1. Create a Firebase project
2. Add Android app: `com.ex
