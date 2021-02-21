package com.reactlibrary

import com.facebook.react.ReactPackage
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.uimanager.ViewManager

class RNMailCorePackage : ReactPackage {
    override fun createNativeModules(reactContext: ReactApplicationContext) = listOf(RNMailCoreModule(reactContext))

    override fun createViewManagers(reactContext: ReactApplicationContext): List<ViewManager<*, *>> = emptyList()
}