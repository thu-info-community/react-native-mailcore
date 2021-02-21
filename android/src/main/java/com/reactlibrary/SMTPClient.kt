package com.reactlibrary

import com.facebook.react.bridge.Promise
import com.facebook.react.bridge.ReadableMap
import java.util.*
import javax.mail.Message
import javax.mail.Session
import javax.mail.internet.InternetAddress
import javax.mail.internet.MimeBodyPart
import javax.mail.internet.MimeMessage
import javax.mail.internet.MimeMultipart

class SMTPClient : AbstractMailClient() {
    private lateinit var credential: UserCredential

    override fun init(userCredential: UserCredential, promise: Promise) {
        credential = userCredential
    }

    fun sendMail(obj: ReadableMap, promise: Promise) {
        safeThread(promise) {
            val fromObj = obj.getMap("from")!!
            val fromDisplayName = fromObj.getString("addressWithDisplayName")!!
            val fromMailbox = fromObj.getString("mailbox")!!

            val host = fromMailbox.run { substring(indexOf('@') + 1) }
            val session = Session.getDefaultInstance(Properties().apply { put("mail.smtp.host", host) })

            val message = MimeMessage(session)
            message.setFrom(InternetAddress(fromMailbox, fromDisplayName))

            fun addRecipients(type: Message.RecipientType, obj: ReadableMap) {
                val iterator = obj.keySetIterator()
                while (iterator.hasNextKey()) {
                    val toMail = iterator.nextKey()
                    val toName = obj.getString(toMail)
                    message.addRecipient(type, InternetAddress(toMail, toName))
                }
            }

            addRecipients(Message.RecipientType.TO, obj.getMap("to")!!)
            if (obj.hasKey("cc")) addRecipients(Message.RecipientType.CC, obj.getMap("cc")!!)
            if (obj.hasKey("bcc")) addRecipients(Message.RecipientType.BCC, obj.getMap("bcc")!!)

            if (obj.hasKey("subject")) message.subject = obj.getString("subject")

            val multipart = MimeMultipart()
            val contentPart = MimeBodyPart()

            val content = if (obj.hasKey("body")) obj.getString("body") else ""
            contentPart.setContent(content, "text/html;charset=utf-8")
            multipart.addBodyPart(contentPart)
            message.setContent(multipart)
            message.saveChanges()

            with(session.getTransport("smtp")) {
                connect(host, fromMailbox, credential.password)
                sendMessage(message, message.allRecipients)
                close()
            }

            promise.callback()
        }
    }
}