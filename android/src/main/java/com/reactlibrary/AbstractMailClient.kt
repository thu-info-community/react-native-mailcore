package com.reactlibrary

import com.facebook.react.bridge.Promise
import kotlin.concurrent.thread

abstract class AbstractMailClient {
    abstract fun init(userCredential: UserCredential, promise: Promise)

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