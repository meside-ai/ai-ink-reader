plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.dagger.hilt.android")
    id("com.google.devtools.ksp")
}

android {
    namespace = "com.newbiechen.inkreader"
    compileSdk = Version.COMPILE_SDK

    defaultConfig {
        applicationId = "com.newbiechen.inkreader"
        minSdk = Version.MIN_SDK
        targetSdk = Version.TARGET_SDK
        versionCode = Version.CODE
        versionName = Version.NAME

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        
        // MVP版本标识
        buildConfigField("String", "BUILD_TYPE", "\"MVP\"")
        buildConfigField("boolean", "IS_DEBUG_BUILD", "false")
    }

    signingConfigs {
        create("release") {
            // 开发阶段使用debug签名，简化流程
            storeFile = file("debug.keystore")
            storePassword = "android"
            keyAlias = "androiddebugkey"
            keyPassword = "android"
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("release")
        }
        
        debug {
            applicationIdSuffix = ".debug"
            isDebuggable = true
            buildConfigField("boolean", "IS_DEBUG_BUILD", "true")
        }
    }

    // 简化的构建变体 - 只保留必要的
    flavorDimensions += "version"
    productFlavors {
        create("mvp") {
            dimension = "version"
            versionNameSuffix = "-mvp"
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    
    kotlinOptions {
        jvmTarget = "1.8"
    }
    
    buildFeatures {
        buildConfig = true
        viewBinding = true
    }
}

dependencies {
    // Android Core
    implementation(Dependencies.Android.coreKtx)
    implementation(Dependencies.Android.lifecycleRuntimeKtx)
    implementation(Dependencies.Android.lifecycleViewModelKtx)
    implementation(Dependencies.Android.lifecycleLiveDataKtx)
    implementation(Dependencies.Android.activityKtx)
    implementation(Dependencies.Android.fragmentKtx)
    
    // UI & Material Design
    implementation(Dependencies.UI.material)
    implementation(Dependencies.UI.constraintLayout)
    implementation(Dependencies.UI.recyclerView)
    
    // Room Database
    implementation(Dependencies.Room.runtime)
    implementation(Dependencies.Room.ktx)
    ksp(Dependencies.Room.compiler)
    
    // Hilt Dependency Injection
    implementation(Dependencies.Hilt.android)
    ksp(Dependencies.Hilt.compiler)
    
    // Network
    implementation(Dependencies.Network.retrofit)
    implementation(Dependencies.Network.retrofitGson)
    implementation(Dependencies.Network.okhttp)
    implementation(Dependencies.Network.okhttpLogging)
    implementation(Dependencies.Network.gson)
    
    // Coroutines
    implementation(Dependencies.Coroutines.core)
    implementation(Dependencies.Coroutines.android)
    
    // EPUB Processing
    implementation(Dependencies.Epub.epublib)
    
    // Image Loading
    implementation(Dependencies.Image.glide)
    ksp(Dependencies.Image.glideCompiler)
    
    // Security
    implementation(Dependencies.Security.sqlcipher)
    
    // Debug Tools (only in debug builds)
    debugImplementation(Dependencies.Debug.leakCanary)
    implementation(Dependencies.Debug.timber)
    
    // Testing
    testImplementation(Dependencies.Testing.junit)
    testImplementation(Dependencies.Testing.mockito)
    testImplementation(Dependencies.Testing.mockitoKotlin)
    testImplementation(Dependencies.Testing.truth)
    testImplementation(Dependencies.Testing.coroutinesTest)
    testImplementation(Dependencies.Testing.roomTesting)
    
    // Android Testing
    androidTestImplementation(Dependencies.Testing.junitExt)
    androidTestImplementation(Dependencies.Testing.espressoCore)
    androidTestImplementation(Dependencies.Testing.hiltTesting)
} 