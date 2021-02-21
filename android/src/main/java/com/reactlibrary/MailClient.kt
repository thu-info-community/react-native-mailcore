package com.reactlibrary

import android.app.Activity
import android.net.Uri
import android.util.Base64
import com.facebook.react.bridge.*
import com.libmailcore.*
import java.io.*
import java.util.*

class MailClient {
    var smtpSession: SMTPSession? = null
    var imapSession: IMAPSession? = null
    fun initIMAPSession(userCredential: UserCredential, promise: Promise) {
        imapSession = IMAPSession()
        imapSession!!.setHostname(userCredential.hostname)
        imapSession!!.setPort(userCredential.port)
        imapSession!!.setConnectionType(ConnectionType.ConnectionTypeTLS)
        val authType = userCredential.authType
        imapSession!!.setAuthType(authType)
        if (authType == AuthType.AuthTypeXOAuth2) {
            imapSession!!.setOAuth2Token(userCredential.password)
        } else {
            imapSession!!.setPassword(userCredential.password)
        }
        imapSession!!.setUsername(userCredential.username)
        val imapOperation = imapSession!!.checkAccountOperation()
        imapOperation.start(object : OperationCallback {
            override fun succeeded() {
                val result = Arguments.createMap()
                result.putString("status", "SUCCESS")
                promise.resolve(result)
            }

            override fun failed(e: MailException) {
                promise.reject(e.errorCode().toString(), e.message)
            }
        })
    }

    fun initSMTPSession(userCredential: UserCredential, promise: Promise) {
        smtpSession = SMTPSession()
        smtpSession!!.setHostname(userCredential.hostname)
        smtpSession!!.setPort(userCredential.port)
        smtpSession!!.setConnectionType(ConnectionType.ConnectionTypeTLS)
        val authType = userCredential.authType
        smtpSession!!.setAuthType(authType)
        if (authType == AuthType.AuthTypeXOAuth2) {
            smtpSession!!.setOAuth2Token(userCredential.password)
        } else {
            smtpSession!!.setPassword(userCredential.password)
        }
        smtpSession!!.setUsername(userCredential.username)
        val smtpOperation = smtpSession!!.loginOperation()
        smtpOperation.start(object : OperationCallback {
            override fun succeeded() {
                val result = Arguments.createMap()
                result.putString("status", "SUCCESS")
                promise.resolve(result)
            }

            override fun failed(e: MailException) {
                promise.reject(e.errorCode().toString(), e.message)
            }
        })
    }

    fun sendMail(obj: ReadableMap, promise: Promise, currentActivity: Activity) {
        val messageHeader = MessageHeader()
        if (obj.hasKey("headers")) {
            val headerObj = obj.getMap("headers")
            val headerIterator = headerObj!!.keySetIterator()
            while (headerIterator.hasNextKey()) {
                val header = headerIterator.nextKey()
                val headerValue = headerObj.getString(header)
                messageHeader.setExtraHeader(header, headerValue)
            }
        }
        val fromObj = obj.getMap("from")
        val fromAddress = Address()
        fromAddress.setDisplayName(fromObj!!.getString("addressWithDisplayName"))
        fromAddress.setMailbox(fromObj.getString("mailbox"))
        messageHeader.setFrom(fromAddress)
        val toObj = obj.getMap("to")
        var iterator = toObj!!.keySetIterator()
        val toAddressList: ArrayList<Address?> = ArrayList()
        while (iterator.hasNextKey()) {
            val toMail = iterator.nextKey()
            val toName = toObj.getString(toMail)
            val toAddress = Address()
            toAddress.setDisplayName(toName)
            toAddress.setMailbox(toMail)
            toAddressList.add(toAddress)
        }
        messageHeader.setTo(toAddressList)
        val ccAddressList: ArrayList<Address?> = ArrayList()
        if (obj.hasKey("cc")) {
            val ccObj = obj.getMap("cc")
            iterator = ccObj!!.keySetIterator()
            while (iterator.hasNextKey()) {
                val ccMail = iterator.nextKey()
                val ccName = ccObj.getString(ccMail)
                val ccAddress = Address()
                ccAddress.setDisplayName(ccName)
                ccAddress.setMailbox(ccMail)
                ccAddressList.add(ccAddress)
            }
            messageHeader.setCc(ccAddressList)
        }
        val bccAddressList: ArrayList<Address?> = ArrayList()
        if (obj.hasKey("bcc")) {
            val bccObj = obj.getMap("bcc")
            iterator = bccObj!!.keySetIterator()
            while (iterator.hasNextKey()) {
                val bccMail = iterator.nextKey()
                val bccName = bccObj.getString(bccMail)
                val bccAddress = Address()
                bccAddress.setDisplayName(bccName)
                bccAddress.setMailbox(bccMail)
                bccAddressList.add(bccAddress)
            }
            messageHeader.setBcc(bccAddressList)
        }
        if (obj.hasKey("subject")) {
            messageHeader.setSubject(obj.getString("subject"))
        }
        val messageBuilder = MessageBuilder()
        messageBuilder.setHeader(messageHeader)
        if (obj.hasKey("body")) {
            messageBuilder.setHTMLBody(obj.getString("body"))
        }
        if (obj.hasKey("attachments")) {
            val attachments = obj.getArray("attachments")
            for (i in 0 until attachments!!.size()) {
                val attachment = attachments.getMap(i)
                if (attachment!!.getString("uniqueId") != null) continue
                val pathName = attachment.getString("uri")
                val fileName = attachment.getString("filename")
                val file = File(pathName)
                try {
                    var size: Long
                    var buf: InputStream
                    val uri = Uri.parse(pathName)
                    if (uri.scheme == "content") {
                        val contentResolver = currentActivity.contentResolver
                        @Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
                        buf = contentResolver.openInputStream(uri)
                        @Suppress("RECEIVER_NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
                        size = contentResolver.openFileDescriptor(uri, "r").statSize
                    } else {
                        buf = BufferedInputStream(FileInputStream(file))
                        size = file.length()
                    }
                    val bytes = ByteArray(size.toInt())
                    buf.read(bytes, 0, bytes.size)
                    buf.close()
                    val result = Arguments.createMap()
                    result.putString("status", bytes.toString())
                    promise.resolve(result)
                    messageBuilder.addAttachment(Attachment.attachmentWithData(fileName, bytes))
                } catch (e: FileNotFoundException) {
                    promise.reject("Attachments", e.message)
                    return
                } catch (e: IOException) {
                    promise.reject("Attachments", e.message)
                    return
                }
            }
        }
        val allRecipients = ArrayList<Address?>()
        allRecipients.addAll(toAddressList)
        allRecipients.addAll(ccAddressList)
        allRecipients.addAll(bccAddressList)
        if (obj.isNull("original_id")) {
            val smtpOperation = smtpSession!!.sendMessageOperation(fromAddress, allRecipients, messageBuilder.data())
            currentActivity.runOnUiThread {
                smtpOperation.start(object : OperationCallback {
                    override fun succeeded() {
                        val result = Arguments.createMap()
                        result.putString("status", "SUCCESS")
                        promise.resolve(result)
                    }

                    override fun failed(e: MailException) {
                        promise.reject(e.errorCode().toString(), e.message)
                    }
                })
            }
        } else {
            currentActivity.runOnUiThread {
                val originalId = obj.getInt("original_id").toLong()
                val originalFolder = obj.getString("original_folder")
                val fetchOriginalMessageOperation = imapSession!!.fetchMessageByUIDOperation(originalFolder, originalId)
                currentActivity.runOnUiThread {
                    fetchOriginalMessageOperation.start(object : OperationCallback {
                        override fun succeeded() {
                            val messageParser = MessageParser.messageParserWithData(fetchOriginalMessageOperation.data())

                            // https://github.com/MailCore/mailcore2/blob/master/src/core/abstract/MCMessageHeader.cpp#L1197
                            if (messageParser.header().messageID() != null) {
                                messageBuilder.header().setInReplyTo(ArrayList(listOf(messageParser.header().messageID())))
                            }
                            if (messageParser.header().references() != null) {
                                val newReferences = ArrayList(messageParser.header().references())
                                if (messageParser.header().messageID() != null) {
                                    newReferences.add(messageParser.header().messageID())
                                }
                                messageBuilder.header().setReferences(newReferences)
                            }

                            // set original attachments if they were any left
                            if (obj.hasKey("attachments")) {
                                val attachments = obj.getArray("attachments")
                                for (i in 0 until attachments!!.size()) {
                                    val attachment = attachments.getMap(i)
                                    if (attachment!!.getString("uniqueId") == null) continue
                                    for (abstractPart in messageParser.attachments()) {
                                        if (abstractPart is Attachment) {
                                            if (abstractPart.uniqueID() != attachment.getString("uniqueId")) continue
                                            messageBuilder.addAttachment(abstractPart)
                                        }
                                    }
                                }
                            }
                            val smtpOperation = smtpSession!!.sendMessageOperation(fromAddress, allRecipients, messageBuilder.data())
                            smtpOperation.start(object : OperationCallback {
                                override fun succeeded() {
                                    val result = Arguments.createMap()
                                    result.putString("status", "SUCCESS")
                                    promise.resolve(result)
                                }

                                override fun failed(e: MailException) {
                                    promise.reject(e.errorCode().toString(), e.message)
                                }
                            })
                        }

                        override fun failed(e: MailException) {
                            promise.reject(e.errorCode().toString(), e.message)
                        }
                    })
                }
            }
        }
    }

    fun getMail(obj: ReadableMap, promise: Promise) {
        val folder = obj.getString("folder")
        val messageId = obj.getInt("messageId")
        val requestKind = obj.getInt("requestKind")
        val messagesOperation = imapSession!!.fetchMessagesByUIDOperation(folder, requestKind, IndexSet.indexSetWithIndex(messageId.toLong()))
        if (obj.hasKey("headers")) {
            val headersArray = obj.getArray("headers")
            val extraHeaders: MutableList<String?> = ArrayList()
            var i = 0
            while (headersArray!!.size() > i) {
                extraHeaders.add(headersArray.getString(i))
                i++
            }
            messagesOperation.setExtraHeaders(extraHeaders)
        }
        messagesOperation.start(object : OperationCallback {
            override fun succeeded() {
                val messages = messagesOperation.messages()
                if (messages.isEmpty()) {
                    promise.reject(Exception("Mail not found!"))
                    return
                }
                val message = messages[0]
                val imapFetchParsedContentOperation = imapSession!!.fetchParsedMessageByUIDOperation(folder, message.uid())
                imapFetchParsedContentOperation.start(object : OperationCallback {
                    override fun succeeded() {
                        val mailData = Arguments.createMap()
                        val uid = message.uid()
                        mailData.putInt("id", uid.toInt())
                        mailData.putString("date", message.header().date().toString())
                        val fromData = Arguments.createMap()
                        fromData.putString("mailbox", message.header().from().mailbox())
                        mailData.putInt("flags", message.flags())
                        fromData.putString("displayName", message.header().from().displayName())
                        mailData.putMap("from", fromData)
                        val toData = Arguments.createMap()
                        val toIterator: ListIterator<Address> = message.header().to().listIterator()
                        while (toIterator.hasNext()) {
                            val toAddress = toIterator.next()
                            toData.putString(toAddress.mailbox(), toAddress.displayName())
                        }
                        mailData.putMap("to", toData)
                        if (message.header().cc() != null) {
                            val ccData = Arguments.createMap()
                            val ccIterator: ListIterator<Address> = message.header().cc().listIterator()
                            while (ccIterator.hasNext()) {
                                val ccAddress = ccIterator.next()
                                ccData.putString(ccAddress.mailbox(), ccAddress.displayName())
                            }
                            mailData.putMap("cc", ccData)
                        }
                        if (message.header().bcc() != null) {
                            val bccData = Arguments.createMap()
                            val bccIterator: ListIterator<Address> = message.header().bcc().listIterator()
                            while (bccIterator.hasNext()) {
                                val bccAddress = bccIterator.next()
                                bccData.putString(bccAddress.mailbox(), bccAddress.displayName())
                            }
                            mailData.putMap("bcc", bccData)
                        }
                        mailData.putString("subject", message.header().subject())
                        val parser = imapFetchParsedContentOperation.parser()
                        mailData.putString("body", parser.htmlBodyRendering())
                        val attachmentsData = Arguments.createMap()
                        val attachments = message.attachments()
                        if (attachments.isNotEmpty()) {
                            for (attachment in message.attachments()) {
                                val part = attachment as IMAPPart
                                val attachmentData = Arguments.createMap()
                                attachmentData.putString("filename", attachment.filename())
                                val size = part.size()
                                attachmentData.putString("size", size.toString())
                                attachmentData.putInt("encoding", part.encoding())
                                attachmentData.putString("uniqueId", part.uniqueID())
                                attachmentsData.putMap(part.partID(), attachmentData)
                            }
                        }
                        mailData.putMap("attachments", attachmentsData)
                        if (message.htmlInlineAttachments().isNotEmpty()) {
                            val attachmentsDataInline = Arguments.createMap()
                            val attachmentsInline = message.htmlInlineAttachments()
                            for (attachment in attachmentsInline) {
                                val part = attachment as IMAPPart
                                val attachmentData = Arguments.createMap()
                                attachmentData.putString("filename", attachment.filename())
                                val size = part.size()
                                attachmentData.putString("size", size.toString())
                                attachmentData.putString("cid", attachment.contentID())
                                attachmentData.putString("partID", attachment.partID())
                                attachmentData.putInt("encoding", part.encoding())
                                attachmentData.putString("uniqueId", part.uniqueID())
                                attachmentData.putString("mimepart", attachment.mimeType())
                                attachmentsDataInline.putMap(part.partID(), attachmentData)
                            }
                            mailData.putMap("inline", attachmentsDataInline)
                        }

                        // Process fetched headers from mail
                        val headerData = Arguments.createMap()
                        headerData.putString("gmailMessageID", message.gmailMessageID().toString())
                        headerData.putString("gmailThreadID", message.gmailThreadID().toString())
                        val headerIterator: ListIterator<String> = message.header().allExtraHeadersNames().listIterator()
                        while (headerIterator.hasNext()) {
                            val headerKey = headerIterator.next()
                            headerData.putString(headerKey, message.header().extraHeaderValueForName(headerKey))
                        }
                        mailData.putMap("headers", headerData)
                        mailData.putString("status", "success")
                        promise.resolve(mailData)
                    }

                    override fun failed(e: MailException) {
                        promise.reject(e.errorCode().toString(), e.message)
                    }
                })
            }

            override fun failed(e: MailException) {
                promise.reject(e.errorCode().toString(), e.message)
            }
        })
    }

    fun createFolderLabel(obj: ReadableMap, promise: Promise) {
        val imapOperation = imapSession!!.createFolderOperation(obj.getString("folder"))
        imapOperation.start(object : OperationCallback {
            override fun succeeded() {
                val result = Arguments.createMap()
                result.putString("status", "SUCCESS")
                promise.resolve(result)
            }

            override fun failed(e: MailException) {
                promise.reject(e.errorCode().toString(), e.message)
            }
        })
    }

    fun renameFolderLabel(obj: ReadableMap, promise: Promise) {
        val imapOperation = imapSession!!.renameFolderOperation(obj.getString("folderOldName"), obj.getString("folderNewName"))
        imapOperation.start(object : OperationCallback {
            override fun succeeded() {
                val result = Arguments.createMap()
                result.putString("status", "SUCCESS")
                promise.resolve(result)
            }

            override fun failed(e: MailException) {
                promise.reject(e.errorCode().toString(), e.message)
            }
        })
    }

    fun deleteFolderLabel(obj: ReadableMap, promise: Promise) {
        val imapOperation = imapSession!!.deleteFolderOperation(obj.getString("folder"))
        imapOperation.start(object : OperationCallback {
            override fun succeeded() {
                val result = Arguments.createMap()
                result.putString("status", "SUCCESS")
                promise.resolve(result)
            }

            override fun failed(e: MailException) {
                promise.reject(e.errorCode().toString(), e.message)
            }
        })
    }

    fun getFolders(promise: Promise) {
        val foldersOperation = imapSession!!.fetchAllFoldersOperation()
        foldersOperation.start(object : OperationCallback {
            override fun succeeded() {
                val folders = foldersOperation.folders()
                val result = Arguments.createMap()
                val a: WritableArray = WritableNativeArray()
                result.putString("status", "SUCCESS")
                for (folder in folders) {
                    val mapFolder = Arguments.createMap()
                    mapFolder.putString("path", folder.path())
                    mapFolder.putInt("flags", folder.flags())
                    a.pushMap(mapFolder)
                }
                result.putArray("folders", a)
                promise.resolve(result)
            }

            override fun failed(e: MailException) {
                promise.reject(e.errorCode().toString(), e.message)
            }
        })
    }

    fun moveEmail(obj: ReadableMap, promise: Promise) {
        val from = obj.getString("folderFrom")
        val messageId = obj.getInt("messageId")
        val to = obj.getString("folderTo")
        val imapOperation: IMAPOperation = imapSession!!.copyMessagesOperation(from, IndexSet.indexSetWithIndex(messageId.toLong()), to)
        imapOperation.start(object : OperationCallback {
            override fun succeeded() {}
            override fun failed(e: MailException) {
                promise.reject(e.errorCode().toString(), e.message)
            }
        })
        val permanentDeleteRequest = Arguments.createMap()
        permanentDeleteRequest.putString("folder", from)
        permanentDeleteRequest.putInt("messageId", messageId)
        permanentDelete(permanentDeleteRequest, promise)
    }

    fun permanentDelete(obj: ReadableMap, promise: Promise) {
        val folder = obj.getString("folder")
        val messageId = obj.getInt("messageId")
        var imapOperation = imapSession!!.storeFlagsByUIDOperation(folder, IndexSet.indexSetWithIndex(messageId.toLong()), 0, MessageFlag.MessageFlagDeleted)
        imapOperation.start(object : OperationCallback {
            override fun succeeded() {}
            override fun failed(e: MailException) {
                promise.reject(e.errorCode().toString(), e.message)
            }
        })
        imapOperation = imapSession!!.expungeOperation(folder)
        imapOperation.start(object : OperationCallback {
            override fun succeeded() {
                val result = Arguments.createMap()
                result.putString("status", "SUCCESS")
                promise.resolve(result)
            }

            override fun failed(e: MailException) {
                promise.reject(e.errorCode().toString(), e.message)
            }
        })
    }

    fun actionLabelMessage(obj: ReadableMap, promise: Promise) {
        val folder = obj.getString("folder")
        val messageId = obj.getInt("messageId")
        val flag = obj.getInt("flagsRequestKind")
        val listTags = obj.getArray("tags")
        val tags: MutableList<String?> = ArrayList()
        for (i in 0 until listTags!!.size()) {
            tags.add(listTags.getString(i))
        }
        val imapOperation = imapSession!!.storeLabelsByUIDOperation(folder, IndexSet.indexSetWithIndex(messageId.toLong()), flag, tags)
        imapOperation.start(object : OperationCallback {
            override fun succeeded() {
                val result = Arguments.createMap()
                result.putString("status", "SUCCESS")
                promise.resolve(result)
            }

            override fun failed(e: MailException) {
                promise.reject(e.errorCode().toString(), e.message)
            }
        })
    }

    fun actionFlagMessage(obj: ReadableMap, promise: Promise) {
        val folder = obj.getString("folder")
        val messageId = obj.getInt("messageId")
        val flag = obj.getInt("flagsRequestKind")
        val messageFlag = obj.getInt("messageFlag")
        val imapOperation = imapSession!!.storeFlagsByUIDOperation(folder, IndexSet.indexSetWithIndex(messageId.toLong()), flag, messageFlag)
        imapOperation.start(object : OperationCallback {
            override fun succeeded() {
                val result = Arguments.createMap()
                result.putString("status", "SUCCESS")
                promise.resolve(result)
            }

            override fun failed(e: MailException) {
                promise.reject(e.errorCode().toString(), e.message)
            }
        })
    }

    fun getMails(obj: ReadableMap, promise: Promise) {
        val folder = obj.getString("folder")
        val requestKind = obj.getInt("requestKind")
        val threadId = if (obj.hasKey("threadId")) obj.getString("threadId")!!.toLong() else null
        if (threadId == null) {
            val indexSet = IndexSet.indexSetWithRange(Range(1, Long.MAX_VALUE))
            val messagesOperation = imapSession!!.fetchMessagesByUIDOperation(folder, requestKind, indexSet)
            if (obj.hasKey("headers")) {
                val headersArray = obj.getArray("headers")
                val extraHeaders: MutableList<String?> = ArrayList()
                var i = 0
                while (headersArray!!.size() > i) {
                    extraHeaders.add(headersArray.getString(i))
                    i++
                }
                messagesOperation.setExtraHeaders(extraHeaders)
            }
            val result = Arguments.createMap()
            val mails = Arguments.createArray()
            messagesOperation.start(object : OperationCallback {
                override fun succeeded() {
                    val messages = messagesOperation.messages()
                    if (messages.isEmpty()) {
                        promise.reject(Exception("Mails not found!"))
                        return
                    }
                    for (message in messages) {
                        val mailData = Arguments.createMap()
                        val headerData = Arguments.createMap()
                        val headerIterator: ListIterator<String> = message.header().allExtraHeadersNames().listIterator()
                        while (headerIterator.hasNext()) {
                            val headerKey = headerIterator.next()
                            headerData.putString(headerKey, message.header().extraHeaderValueForName(headerKey))
                        }
                        mailData.putMap("headers", headerData)
                        val mailId = message.uid()
                        mailData.putInt("id", mailId.toInt())
                        mailData.putInt("flags", message.flags())
                        mailData.putString("from", message.header().from().displayName())
                        mailData.putString("subject", message.header().subject())
                        mailData.putString("date", message.header().date().toString())
                        mailData.putInt("attachments", message.attachments().size)
                        mails.pushMap(mailData)
                    }
                    result.putString("status", "SUCCESS")
                    result.putArray("mails", mails)
                    promise.resolve(result)
                }

                override fun failed(e: MailException) {
                    promise.reject(e.errorCode().toString(), e.message)
                }
            })
        } else {
            val imapOperation = imapSession!!.searchOperation(folder, IMAPSearchExpression.searchGmailThreadID(threadId))
            imapOperation.start(object : OperationCallback {
                override fun succeeded() {
                    val messagesOperation = imapSession!!.fetchMessagesByUIDOperation(folder, requestKind, imapOperation.uids())
                    if (obj.hasKey("headers")) {
                        val headersArray = obj.getArray("headers")
                        val extraHeaders: MutableList<String?> = ArrayList()
                        var i = 0
                        while (headersArray!!.size() > i) {
                            extraHeaders.add(headersArray.getString(i))
                            i++
                        }
                        messagesOperation.setExtraHeaders(extraHeaders)
                    }
                    val result = Arguments.createMap()
                    val mails = Arguments.createArray()
                    messagesOperation.start(object : OperationCallback {
                        override fun succeeded() {
                            val messages = messagesOperation.messages()
                            if (messages.isEmpty()) {
                                promise.reject(Exception("Mails not found!"))
                                return
                            }
                            for (message in messages) {
                                val mailData = Arguments.createMap()
                                val headerData = Arguments.createMap()
                                val headerIterator: ListIterator<String> = message.header().allExtraHeadersNames().listIterator()
                                while (headerIterator.hasNext()) {
                                    val headerKey = headerIterator.next()
                                    headerData.putString(headerKey, message.header().extraHeaderValueForName(headerKey))
                                }
                                mailData.putMap("headers", headerData)
                                val mailId = message.uid()
                                mailData.putInt("id", mailId.toInt())
                                mailData.putInt("flags", message.flags())
                                mailData.putString("from", message.header().from().displayName())
                                mailData.putString("subject", message.header().subject())
                                mailData.putString("date", message.header().date().toString())
                                mailData.putInt("attachments", message.attachments().size)
                                mails.pushMap(mailData)
                            }
                            result.putString("status", "SUCCESS")
                            result.putArray("mails", mails)
                            promise.resolve(result)
                        }

                        override fun failed(e: MailException) {
                            promise.reject(e.errorCode().toString(), e.message)
                        }
                    })
                }

                override fun failed(e: MailException) {}
            })
        }
    }

    fun getMailsThread(obj: ReadableMap, promise: Promise) {
        val folder = obj.getString("folder")
        val requestKind = obj.getInt("requestKind")
        val lastUId = if (obj.hasKey("lastUId")) obj.getInt("lastUId") else 1
        val indexSet = IndexSet.indexSetWithRange(Range(lastUId.toLong(), Long.MAX_VALUE))
        val messagesOperation = imapSession!!.fetchMessagesByUIDOperation(folder, requestKind, indexSet)
        if (obj.hasKey("headers")) {
            val headersArray = obj.getArray("headers")
            val extraHeaders: MutableList<String?> = ArrayList()
            var i = 0
            while (headersArray!!.size() > i) {
                extraHeaders.add(headersArray.getString(i))
                i++
            }
            messagesOperation.setExtraHeaders(extraHeaders)
        }
        val result = Arguments.createMap()
        val mails = Arguments.createArray()
        val listThreads: MutableList<String> = ArrayList()
        messagesOperation.start(object : OperationCallback {
            override fun succeeded() {
                val messages = messagesOperation.messages()
                messages.reverse()
                if (messages.isEmpty()) {
                    promise.reject(Exception("Mails not found!"))
                    return
                }
                for (message in messages) {
                    if (!listThreads.contains(message!!.header().messageID())) {
                        val mailData = Arguments.createMap()
                        val headerData = Arguments.createMap()
                        listThreads.add(message.header().messageID())
                        if (message.header().references() != null) {
                            listThreads.addAll(message.header().references())
                            mailData.putInt("thread", message.header().references().size + 1)
                        }
                        val headerIterator: ListIterator<String> = message.header().allExtraHeadersNames().listIterator()
                        headerData.putString("gmailMessageID", message.gmailMessageID().toString())
                        headerData.putString("gmailThreadID", message.gmailThreadID().toString())
                        while (headerIterator.hasNext()) {
                            val headerKey = headerIterator.next()
                            headerData.putString(headerKey, message.header().extraHeaderValueForName(headerKey))
                        }
                        mailData.putMap("headers", headerData)
                        val mailId = message.uid()
                        mailData.putInt("id", mailId.toInt())
                        mailData.putInt("flags", message.flags())
                        mailData.putString("from", message.header().from().displayName())
                        mailData.putString("subject", message.header().subject())
                        mailData.putString("date", message.header().date().toString())
                        mailData.putInt("attachments", message.attachments().size)
                        mails.pushMap(mailData)
                    }
                }
                result.putString("status", "SUCCESS")
                result.putArray("mails", mails)
                promise.resolve(result)
            }

            override fun failed(e: MailException) {
                promise.reject(e.errorCode().toString(), e.message)
            }
        })
    }

    fun getAttachment(obj: ReadableMap, promise: Promise) {
        val filename = obj.getString("filename")
        val folderId = obj.getString("folder")
        val messageId = obj.getInt("messageId").toLong()
        val partID = obj.getString("partID")
        val encoding = obj.getInt("encoding")
        val folderOutput = obj.getString("folderOutput")
        val imapOperation = imapSession!!.fetchMessageAttachmentByUIDOperation(folderId, messageId, partID, encoding, true)
        imapOperation.start(object : OperationCallback {
            override fun succeeded() {
                val file = File(folderOutput, filename)
                try {
                    val outputStream = FileOutputStream(file)
                    outputStream.write(imapOperation.data())
                    outputStream.close()
                    if (file.canWrite()) {
                        val result = Arguments.createMap()
                        result.putString("status", "SUCCESS")
                        promise.resolve(result)
                    }
                } catch (e: FileNotFoundException) {
                    promise.reject(Exception(e.message))
                } catch (e: IOException) {
                    promise.reject(Exception(e.message))
                } catch (e: Exception) {
                    promise.reject(Exception(e.message))
                }
            }

            override fun failed(e: MailException) {
                promise.reject(e.errorCode().toString(), e.message)
            }
        })
    }

    fun getAttachmentInline(obj: ReadableMap, promise: Promise) {
        val folderId = obj.getString("folder")
        val messageId = obj.getInt("messageId").toLong()
        val partID = obj.getString("partID")
        val encoding = obj.getInt("encoding")
        val mimepart = obj.getString("mimepart")
        val imapOperation = imapSession!!.fetchMessageAttachmentByUIDOperation(folderId, messageId, partID, encoding, true)
        imapOperation.start(object : OperationCallback {
            override fun succeeded() {
                val result = Arguments.createMap()
                val data = "data:" + mimepart + ";base64, " + Base64.encodeToString(imapOperation.data(), Base64.DEFAULT)
                result.putString("data", data)
                promise.resolve(result)
            }

            override fun failed(e: MailException) {
                promise.reject(e.errorCode().toString(), e.message)
            }
        })
    }

    fun statusFolder(obj: ReadableMap, promise: Promise) {
        val folder = obj.getString("folder")
        val folderStatusOperation = imapSession!!.folderStatusOperation(folder)
        folderStatusOperation.start(object : OperationCallback {
            override fun succeeded() {
                val result = Arguments.createMap()
                result.putString("status", "SUCCESS")
                result.putInt("unseenCount", folderStatusOperation.status().unseenCount().toInt())
                result.putInt("messageCount", folderStatusOperation.status().messageCount().toInt())
                result.putInt("recentCount", folderStatusOperation.status().recentCount().toInt())
                promise.resolve(result)
            }

            override fun failed(e: MailException) {
                promise.reject(e.errorCode().toString(), e.message)
            }
        })
    }

    fun getMailsByRange(obj: ReadableMap, promise: Promise) {
        // Get arguments
        val folder = obj.getString("folder")
        val requestKind = obj.getInt("requestKind")
        val from = obj.getInt("from")
        val length = obj.getInt("length")

        // Build operation
        val indexSet = IndexSet.indexSetWithRange(Range(from.toLong(), length.toLong()))
        val messagesOperation = imapSession!!.fetchMessagesByNumberOperation(folder, requestKind, indexSet)
        if (obj.hasKey("headers")) {
            val headersArray = obj.getArray("headers")
            val extraHeaders: MutableList<String?> = ArrayList()
            var i = 0
            while (headersArray!!.size() > i) {
                extraHeaders.add(headersArray.getString(i))
                i++
            }
            messagesOperation.setExtraHeaders(extraHeaders)
        }

        // Start operation
        messagesOperation.start(object : OperationCallback {
            override fun succeeded() {
                parseMessages(messagesOperation.messages(), promise)
            }

            override fun failed(e: MailException) {
                promise.reject(e.errorCode().toString(), e.message)
            }
        })
    }

    fun getMailsByThread(obj: ReadableMap, promise: Promise) {
        // Get arguments
        val folder = obj.getString("folder")
        val requestKind = obj.getInt("requestKind")
        val threadId = obj.getString("threadId")!!.toLong()

        // Build operation
        val searchOperation = imapSession!!.searchOperation(
                folder, IMAPSearchExpression.searchGmailThreadID(threadId))

        // Start searchOperation
        searchOperation.start(object : OperationCallback {
            override fun succeeded() {
                // Build operation
                val messagesOperation = imapSession!!
                        .fetchMessagesByUIDOperation(folder, requestKind, searchOperation.uids())
                if (obj.hasKey("headers")) {
                    val headersArray = obj.getArray("headers")
                    val extraHeaders: MutableList<String?> = ArrayList()
                    var i = 0
                    while (headersArray!!.size() > i) {
                        extraHeaders.add(headersArray.getString(i))
                        i++
                    }
                    messagesOperation.setExtraHeaders(extraHeaders)
                }

                // Start messagesOperation
                messagesOperation.start(object : OperationCallback {
                    override fun succeeded() {
                        parseMessages(messagesOperation.messages(), promise)
                    }

                    override fun failed(e: MailException) {
                        promise.reject(e.errorCode().toString(), e.message)
                    }
                })
            }

            override fun failed(e: MailException) {
                promise.reject(e.errorCode().toString(), e.message)
            }
        })
    }

    private fun parseMessages(messages: List<IMAPMessage>?, promise: Promise) {
        val result = Arguments.createMap()
        val mails = Arguments.createArray()
        if (messages == null) {
            result.putString("status", "SUCCESS")
            result.putArray("mails", mails)
            promise.resolve(result)
        }
        for (message in messages!!) {

            // Process fetched headers from mail
            val headerData = Arguments.createMap()
            headerData.putString("gmailMessageID", message.gmailMessageID().toString())
            headerData.putString("gmailThreadID", message.gmailThreadID().toString())
            val headerIterator: ListIterator<String> = message.header().allExtraHeadersNames().listIterator()
            while (headerIterator.hasNext()) {
                val headerKey = headerIterator.next()
                headerData.putString(headerKey, message.header().extraHeaderValueForName(headerKey))
            }

            // Process fetched data from mail
            val mailData = Arguments.createMap()
            mailData.putMap("headers", headerData)
            mailData.putInt("id", message.uid().toInt())
            mailData.putInt("flags", message.flags())
            mailData.putString("from", message.header().from().displayName())
            mailData.putString("subject", message.header().subject())
            mailData.putString("date", message.header().date().toString())
            mailData.putInt("attachments", message.attachments().size)
            mails.pushMap(mailData)
        }
        result.putString("status", "SUCCESS")
        result.putArray("mails", mails)
        promise.resolve(result)
    }
}