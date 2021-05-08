package com.reactlibrary

import com.facebook.react.bridge.*

class RNMailCoreModule(reactContext: ReactApplicationContext) : ReactContextBaseJavaModule(reactContext) {
    private val imapClient = IMAPClient()

    private val smtpClient = SMTPClient()

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
        safeWrapper(promise) { imapClient.init(UserCredential(obj), promise) }
    }

    @ReactMethod
    fun loginSmtp(obj: ReadableMap, promise: Promise) {
        safeWrapper(promise) { smtpClient.init(UserCredential(obj), promise) } // This is almost a no-op
    }

    @ReactMethod
    fun createFolder(obj: ReadableMap, promise: Promise) {
        safeWrapper(promise) { imapClient.createFolder(obj, promise) }
    }

    @ReactMethod
    fun renameFolder(obj: ReadableMap, promise: Promise) {
        safeWrapper(promise) { imapClient.renameFolder(obj, promise) }
    }

    @ReactMethod
    fun deleteFolder(obj: ReadableMap, promise: Promise) {
        safeWrapper(promise) { imapClient.deleteFolder(obj, promise) }
    }

    @ReactMethod
    fun getFolders(promise: Promise) {
        safeWrapper(promise) { imapClient.getFolders(promise) }
    }

    @ReactMethod
    fun moveEmail(obj: ReadableMap, promise: Promise) {
        safeWrapper(promise) { imapClient.moveEmail(obj, promise) }
    }

    @ReactMethod
    fun permanentDeleteEmail(obj: ReadableMap, promise: Promise) {
        TODO("`permanentDeleteEmail` is not supported yet.")
    }

    @ReactMethod
    fun actionFlagMessage(obj: ReadableMap, promise: Promise) {
        TODO("`actionFlagMessage` is not supported yet.")
    }

    @ReactMethod
    fun actionLabelMessage(obj: ReadableMap, promise: Promise) {
        TODO("`actionLabelMessage` is not supported yet.")
    }

    @ReactMethod
    fun sendMail(obj: ReadableMap, promise: Promise) {
        safeWrapper(promise) { smtpClient.sendMail(obj, promise) }
    }

    @ReactMethod
    fun getMail(obj: ReadableMap, promise: Promise) {
        safeWrapper(promise) { imapClient.getMail(obj, promise) }
    }

    @ReactMethod
    fun getAttachment(obj: ReadableMap, promise: Promise) {
        TODO("`getAttachment` is not supported yet.")
    }

    @ReactMethod
    fun getAttachmentInline(obj: ReadableMap, promise: Promise) {
        TODO("`getAttachmentInline` is not supported yet.")
    }

    @ReactMethod
    fun getMails(obj: ReadableMap, promise: Promise) {
        safeWrapper(promise) { imapClient.getMails(obj, promise) }
    }

    @ReactMethod
    fun getMailsThread(obj: ReadableMap, promise: Promise) {
        TODO("`getMailsThread` is not supported yet.")
    }

    @ReactMethod
    fun statusFolder(obj: ReadableMap, promise: Promise) {
        TODO("`statusFolder` is not supported yet.")
    }

    @ReactMethod
    fun getMailsByRange(obj: ReadableMap, promise: Promise) {
        TODO("`getMailsByRange` is not supported yet.")
    }

    @ReactMethod
    fun getMailsByThread(obj: ReadableMap, promise: Promise) {
        TODO("`getMailsByThread` is not supported yet.")
    }

}