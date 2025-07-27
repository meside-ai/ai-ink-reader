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
    const val PAGING_VERSION = "3.2.1"
    const val WORK_MANAGER_VERSION = "2.9.0"
    const val DATASTORE_VERSION = "1.0.0"
    
    // Dependency Injection
    const val HILT_VERSION = "2.48"
    
    // Network
    const val RETROFIT_VERSION = "2.9.0"
    const val OKHTTP_VERSION = "4.12.0"
    const val GSON_VERSION = "2.10.1"
    
    // EPUB Processing
    const val EPUBLIB_VERSION = "4.0"
    
    // Image Loading
    const val GLIDE_VERSION = "4.16.0"
    
    // Testing
    const val JUNIT_VERSION = "4.13.2"
    const val JUNIT_EXT_VERSION = "1.1.5"
    const val ESPRESSO_VERSION = "3.5.1"
    const val MOCKITO_VERSION = "5.8.0"
    const val TRUTH_VERSION = "1.1.5"
    
    // Debug Tools
    const val LEAK_CANARY_VERSION = "2.12"
    const val TIMBER_VERSION = "5.0.1"
    
    // Security
    const val SQLCIPHER_VERSION = "4.5.4"
    
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
        const val testing = "androidx.room:room-testing:$ROOM_VERSION"
    }
    
    object Hilt {
        const val android = "com.google.dagger:hilt-android:$HILT_VERSION"
        const val compiler = "com.google.dagger:hilt-compiler:$HILT_VERSION"
        const val testing = "com.google.dagger:hilt-android-testing:$HILT_VERSION"
    }
    
    object Network {
        const val retrofit = "com.squareup.retrofit2:retrofit:$RETROFIT_VERSION"
        const val retrofitGson = "com.squareup.retrofit2:converter-gson:$RETROFIT_VERSION"
        const val okhttp = "com.squareup.okhttp3:okhttp:$OKHTTP_VERSION"
        const val okhttpLogging = "com.squareup.okhttp3:logging-interceptor:$OKHTTP_VERSION"
        const val gson = "com.google.code.gson:gson:$GSON_VERSION"
    }
    
    object Coroutines {
        const val core = "org.jetbrains.kotlinx:kotlinx-coroutines-core:$COROUTINES_VERSION"
        const val android = "org.jetbrains.kotlinx:kotlinx-coroutines-android:$COROUTINES_VERSION"
        const val test = "org.jetbrains.kotlinx:kotlinx-coroutines-test:$COROUTINES_VERSION"
    }
    
    object Epub {
        const val epublib = "org.jsoup:jsoup:1.17.2" // 使用jsoup作为HTML解析库
        // 注意：epublib-android可能需要手动添加或使用其他EPUB库
    }
    
    object Image {
        const val glide = "com.github.bumptech.glide:glide:$GLIDE_VERSION"
        const val glideCompiler = "com.github.bumptech.glide:compiler:$GLIDE_VERSION"
    }
    
    object Testing {
        const val junit = "junit:junit:$JUNIT_VERSION"
        const val junitExt = "androidx.test.ext:junit:$JUNIT_EXT_VERSION"
        const val espressoCore = "androidx.test.espresso:espresso-core:$ESPRESSO_VERSION"
        const val mockito = "org.mockito:mockito-core:$MOCKITO_VERSION"
        const val mockitoKotlin = "org.mockito.kotlin:mockito-kotlin:5.2.1"
        const val truth = "com.google.truth:truth:$TRUTH_VERSION"
        const val coroutinesTest = Coroutines.test
        const val roomTesting = Room.testing
        const val hiltTesting = Hilt.testing
    }
    
    object Debug {
        const val leakCanary = "com.squareup.leakcanary:leakcanary-android:$LEAK_CANARY_VERSION"
        const val timber = "com.jakewharton.timber:timber:$TIMBER_VERSION"
    }
    
    object Security {
        const val sqlcipher = "net.zetetic:android-database-sqlcipher:$SQLCIPHER_VERSION"
    }
} 