object Dependencies {
    
    // Kotlin & Coroutines
    const val KOTLIN_VERSION = "1.9.22"
    const val COROUTINES_VERSION = "1.7.3"
    
    // Android Core
    const val CORE_KTX_VERSION = "1.12.0"
    const val LIFECYCLE_VERSION = "2.7.0"
    const val ACTIVITY_COMPOSE_VERSION = "1.8.2"
    const val FRAGMENT_KTX_VERSION = "1.6.2"
    
    // UI & Material Design
    const val MATERIAL_VERSION = "1.11.0"
    const val CONSTRAINT_LAYOUT_VERSION = "2.1.4"
    const val RECYCLER_VIEW_VERSION = "1.3.2"
    
    // Architecture Components
    const val ROOM_VERSION = "2.6.1"
    
    // Dependency Injection
    const val HILT_VERSION = "2.48"
    
    // Image Loading
    const val GLIDE_VERSION = "4.16.0"
    
    // Testing
    const val JUNIT_VERSION = "4.13.2"
    const val JUNIT_EXT_VERSION = "1.1.5"
    const val ESPRESSO_VERSION = "3.5.1"
    
    // Debug Tools
    const val TIMBER_VERSION = "5.0.1"
    
    // Build Tools
    const val KSP_VERSION = "1.9.22-1.0.17"
    
    object Android {
        const val coreKtx = "androidx.core:core-ktx:$CORE_KTX_VERSION"
        const val lifecycleRuntimeKtx = "androidx.lifecycle:lifecycle-runtime-ktx:$LIFECYCLE_VERSION"
        const val lifecycleViewModelKtx = "androidx.lifecycle:lifecycle-viewmodel-ktx:$LIFECYCLE_VERSION"
        const val lifecycleLiveDataKtx = "androidx.lifecycle:lifecycle-livedata-ktx:$LIFECYCLE_VERSION"
        const val activityKtx = "androidx.activity:activity-ktx:$ACTIVITY_COMPOSE_VERSION"
        const val fragmentKtx = "androidx.fragment:fragment-ktx:$FRAGMENT_KTX_VERSION"
    }
    
    object UI {
        const val material = "com.google.android.material:material:$MATERIAL_VERSION"
        const val constraintLayout = "androidx.constraintlayout:constraintlayout:$CONSTRAINT_LAYOUT_VERSION"
        const val recyclerView = "androidx.recyclerview:recyclerview:$RECYCLER_VIEW_VERSION"
    }
    
    object Room {
        const val runtime = "androidx.room:room-runtime:$ROOM_VERSION"
        const val compiler = "androidx.room:room-compiler:$ROOM_VERSION"
        const val ktx = "androidx.room:room-ktx:$ROOM_VERSION"
    }
    
    object Hilt {
        const val android = "com.google.dagger:hilt-android:$HILT_VERSION"
        const val compiler = "com.google.dagger:hilt-compiler:$HILT_VERSION"
    }
    
    object Coroutines {
        const val core = "org.jetbrains.kotlinx:kotlinx-coroutines-core:$COROUTINES_VERSION"
        const val android = "org.jetbrains.kotlinx:kotlinx-coroutines-android:$COROUTINES_VERSION"
    }
    
    object Image {
        const val glide = "com.github.bumptech.glide:glide:$GLIDE_VERSION"
        const val glideCompiler = "com.github.bumptech.glide:compiler:$GLIDE_VERSION"
    }
    
    object Testing {
        const val junit = "junit:junit:$JUNIT_VERSION"
        const val junitExt = "androidx.test.ext:junit:$JUNIT_EXT_VERSION"
        const val espressoCore = "androidx.test.espresso:espresso-core:$ESPRESSO_VERSION"
    }
    
    object Debug {
        const val timber = "com.jakewharton.timber:timber:$TIMBER_VERSION"
    }
} 