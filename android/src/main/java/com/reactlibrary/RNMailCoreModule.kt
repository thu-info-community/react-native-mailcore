package com.reactlibrary

import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.ReactContextBaseJavaModule
import com.reactlibrary.MailClient
import com.facebook.react.bridge.ReactMethod
import com.facebook.react.bridge.ReadableMap
import com.facebook.react.bridge.Promise

class RNMailCoreModule(reactContext: ReactApplicationContext) : ReactContextBaseJavaModule(reactContext) {
    private val mailClient = MailClient()

    override fun getName() = "RNMailCore"

    @ReactMethod
    fun loginImap(obj: ReadableMap, promise: Promise) {
        currentActivity!!.runOnUiThread {
            mailClient.initIMAPSession(UserCredential(obj), promise)
        }
    }

    @ReactMethod
    fun loginSmtp(obj: ReadableMap, promise: Promise) {
        currentActivity!!.runOnUiThread {
            mailClient.initSMTPSession(UserCredential(obj), promise)
        }
    }

    @ReactMethod
    fun createFolder(obj: ReadableMap, promise: Promise) {
        currentActivity!!.runOnUiThread { mailClient.createFolderLabel(obj, promise) }
    }

    @ReactMethod
    fun renameFolder(obj: ReadableMap, promise: Promise) {
        currentActivity!!.runOnUiThread { mailClient.renameFolderLabel(obj, promise) }
    }

    @ReactMethod
    fun deleteFolder(obj: ReadableMap, promise: Promise) {
        currentActivity!!.runOnUiThread { mailClient.deleteFolderLabel(obj, promise) }
    }

    @ReactMethod
    fun getFolders(promise: Promise?) {
        currentActivity!!.runOnUiThread { mailClient.getFolders(promise) }
    }

    @ReactMethod
    fun moveEmail(obj: ReadableMap, promise: Promise) {
        currentActivity!!.runOnUiThread { mailClient.moveEmail(obj, promise) }
    }

    @ReactMethod
    fun permantDeleteEmail(obj: ReadableMap, promise: Promise) {
        currentActivity!!.runOnUiThread { mailClient.permantDelete(obj, promise) }
    }

    @ReactMethod
    fun actionFlagMessage(obj: ReadableMap, promise: Promise) {
        currentActivity!!.runOnUiThread { mailClient.ActionFlagMessage(obj, promise) }
    }

    @ReactMethod
    fun actionLabelMessage(obj: ReadableMap, promise: Promise) {
        currentActivity!!.runOnUiThread { mailClient.ActionLabelMessage(obj, promise) }
    }

    @ReactMethod
    fun sendMail(obj: ReadableMap, promise: Promise) {
        currentActivity!!.runOnUiThread { mailClient.sendMail(obj, promise, currentActivity) }
    }

    @ReactMethod
    fun getMail(obj: ReadableMap, promise: Promise) {
        currentActivity!!.runOnUiThread { mailClient.getMail(obj, promise) }
    }

    @ReactMethod
    fun getAttachment(obj: ReadableMap, promise: Promise) {
        currentActivity!!.runOnUiThread { mailClient.getAttachment(obj, promise) }
    }

    @ReactMethod
    fun getAttachmentInline(obj: ReadableMap, promise: Promise) {
        currentActivity!!.runOnUiThread { mailClient.getAttachmentInline(obj, promise) }
    }

    @ReactMethod
    fun getMails(obj: ReadableMap, promise: Promise) {
        currentActivity!!.runOnUiThread { mailClient.getMails(obj, promise) }
    }

    @ReactMethod
    fun getMailsThread(obj: ReadableMap, promise: Promise) {
        currentActivity!!.runOnUiThread { mailClient.getMailsThread(obj, promise) }
    }

    @ReactMethod
    fun statusFolder(obj: ReadableMap, promise: Promise) {
        currentActivity!!.runOnUiThread { mailClient.statusFolder(obj, promise) }
    }

    @ReactMethod
    fun getMailsByRange(obj: ReadableMap, promise: Promise) {
        currentActivity!!.runOnUiThread { mailClient.getMailsByRange(obj, promise) }
    }

    @ReactMethod
    fun getMailsByThread(obj: ReadableMap, promise: Promise) {
        currentActivity!!.runOnUiThread { mailClient.getMailsByThread(obj, promise) }
    }

}