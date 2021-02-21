package com.reactlibrary

import com.facebook.react.bridge.*

class RNMailCoreModule(reactContext: ReactApplicationContext) : ReactContextBaseJavaModule(reactContext) {
    private val mailClient = MailClient()

    override fun getName() = "RNMailCore"

    private fun safeWrapper(promise: Promise, action: () -> Unit) {
        currentActivity?.runOnUiThread {
            try {
                action()
            } catch (e: Exception) {
                promise.reject(e)
            }
        } ?: promise.reject(Exception("Current activity is null!"))
    }

    @ReactMethod
    fun loginImap(obj: ReadableMap, promise: Promise) {
        safeWrapper(promise) { mailClient.initIMAPSession(UserCredential(obj), promise) }
    }

    @ReactMethod
    fun loginSmtp(obj: ReadableMap, promise: Promise) {
        safeWrapper(promise) { mailClient.initSMTPSession(UserCredential(obj), promise) }
    }

    @ReactMethod
    fun createFolder(obj: ReadableMap, promise: Promise) {
        safeWrapper(promise) { mailClient.createFolderLabel(obj, promise) }
    }

    @ReactMethod
    fun renameFolder(obj: ReadableMap, promise: Promise) {
        safeWrapper(promise) { mailClient.renameFolderLabel(obj, promise) }
    }

    @ReactMethod
    fun deleteFolder(obj: ReadableMap, promise: Promise) {
        safeWrapper(promise) { mailClient.deleteFolderLabel(obj, promise) }
    }

    @ReactMethod
    fun getFolders(promise: Promise) {
        safeWrapper(promise) { mailClient.getFolders(promise) }
    }

    @ReactMethod
    fun moveEmail(obj: ReadableMap, promise: Promise) {
        safeWrapper(promise) { mailClient.moveEmail(obj, promise) }
    }

    @ReactMethod
    fun permanentDeleteEmail(obj: ReadableMap, promise: Promise) {
        safeWrapper(promise) { mailClient.permanentDelete(obj, promise) }
    }

    @ReactMethod
    fun actionFlagMessage(obj: ReadableMap, promise: Promise) {
        safeWrapper(promise) { mailClient.actionFlagMessage(obj, promise) }
    }

    @ReactMethod
    fun actionLabelMessage(obj: ReadableMap, promise: Promise) {
        safeWrapper(promise) { mailClient.actionLabelMessage(obj, promise) }
    }

    @ReactMethod
    fun sendMail(obj: ReadableMap, promise: Promise) {
        safeWrapper(promise) { mailClient.sendMail(obj, promise, currentActivity!!) }
    }

    @ReactMethod
    fun getMail(obj: ReadableMap, promise: Promise) {
        safeWrapper(promise) { mailClient.getMail(obj, promise) }
    }

    @ReactMethod
    fun getAttachment(obj: ReadableMap, promise: Promise) {
        safeWrapper(promise) { mailClient.getAttachment(obj, promise) }
    }

    @ReactMethod
    fun getAttachmentInline(obj: ReadableMap, promise: Promise) {
        safeWrapper(promise) { mailClient.getAttachmentInline(obj, promise) }
    }

    @ReactMethod
    fun getMails(obj: ReadableMap, promise: Promise) {
        safeWrapper(promise) { mailClient.getMails(obj, promise) }
    }

    @ReactMethod
    fun getMailsThread(obj: ReadableMap, promise: Promise) {
        safeWrapper(promise) { mailClient.getMailsThread(obj, promise) }
    }

    @ReactMethod
    fun statusFolder(obj: ReadableMap, promise: Promise) {
        safeWrapper(promise) { mailClient.statusFolder(obj, promise) }
    }

    @ReactMethod
    fun getMailsByRange(obj: ReadableMap, promise: Promise) {
        safeWrapper(promise) { mailClient.getMailsByRange(obj, promise) }
    }

    @ReactMethod
    fun getMailsByThread(obj: ReadableMap, promise: Promise) {
        safeWrapper(promise) { mailClient.getMailsByThread(obj, promise) }
    }

}