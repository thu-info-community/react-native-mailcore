package com.reactlibrary

import com.facebook.react.bridge.Arguments
import com.facebook.react.bridge.Promise
import com.facebook.react.bridge.WritableMap
import kotlin.concurrent.thread

abstract class AbstractMailClient {
    abstract fun init(userCredential: UserCredential, promise: Promise)

    fun Promise.callback(addon: (WritableMap) -> Unit = {}) {
        val result = Arguments.createMap()
        result.putString("status", "SUCCESS")
        addon(result)
        resolve(result)
    }

    companion object {
        fun safeThread(promise: Promise, op: () -> Unit) {
            thread {
                try {
                    op()
                } catch (e: Exception) {
                    promise.reject(e)
                }
            }
        }
    }
}