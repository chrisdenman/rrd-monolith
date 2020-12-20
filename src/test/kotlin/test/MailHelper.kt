package uk.co.ceilingcat.rrd.monolith.test

import java.util.Properties
import javax.mail.FetchProfile
import javax.mail.Flags
import javax.mail.Folder
import javax.mail.Session
import javax.mail.search.FlagTerm

class MailHelper {

    companion object {
        private fun repeatUntil(
            f: () -> Boolean,
            maximumDuration: Long,
            waitDuration: Long
        ): Boolean {
            val currentTimeMillis: Long = System.currentTimeMillis()
            val startTime = currentTimeMillis
            fun elapsedDuration() = currentTimeMillis - startTime
            fun wait() = Thread.sleep(waitDuration)
            if (f()) {
                return true
            } else {
                while (elapsedDuration() < (maximumDuration - startTime)) {
                    wait()
                    if (f()) {
                        return true
                    }
                }
            }
            return false
        }

        fun mailFoundAndRemoved(
            username: String,
            password: String,
            subjectText: String,
            maximumDuration: Long,
            waitDuration: Long
        ): Boolean {
            fun foundAndRemoveMail(): Boolean {
                val session = Session.getInstance(
                    Properties().apply {
                        this["mail.store.protocol"] = "imaps"
                        this["mail.imap.ssl.enable"] = true
                    }
                )
                val store = session.getStore("imap")
                store.connect(
                    "imap.mail.me.com",
                    993,
                    username,
                    password
                )
                store.use {
                    it.getFolder("INBOX").let { folder ->
                        folder!!.open(Folder.READ_WRITE)

                        val messages = folder.search(UNREAD_FLAG_SEARCH_TERM)

                        folder.fetch(
                            messages,
                            FetchProfile().apply {
                                add(FetchProfile.Item.ENVELOPE)
                                add(FetchProfile.Item.CONTENT_INFO)
                            }
                        )

                        val passed = messages.fold(false) { acc, message ->
                            if (acc || (message.subject == subjectText)) {
                                message.setFlag(Flags.Flag.DELETED, true)
                                true
                            } else {
                                acc
                            }
                        }

                        folder.close(true)

                        return passed
                    }
                }
            }
            return repeatUntil(::foundAndRemoveMail, maximumDuration, waitDuration)
        }

        private val UNREAD_FLAG_SEARCH_TERM = FlagTerm(Flags(Flags.Flag.SEEN), false)
    }
}
