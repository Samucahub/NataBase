package com.exemplo.natabase

import java.io.File
import java.util.*
import javax.mail.*
import javax.mail.internet.InternetAddress
import javax.mail.internet.MimeBodyPart
import javax.mail.internet.MimeMessage
import javax.mail.internet.MimeMultipart

class EmailService(
    private val senderEmail: String,
    private val senderAppPassword: String,
    private val toAddresses: List<String>
) {
    private val smtpHost = "smtp.gmail.com"
    private val smtpPort = "465"

    fun sendExcel(file: File, subject: String, body: String) {
        val props = Properties()
        props["mail.smtp.host"] = smtpHost
        props["mail.smtp.socketFactory.port"] = smtpPort
        props["mail.smtp.socketFactory.class"] = "javax.net.ssl.SSLSocketFactory"
        props["mail.smtp.auth"] = "true"
        props["mail.smtp.port"] = smtpPort

        val session = Session.getDefaultInstance(props,
            object : Authenticator() {
                override fun getPasswordAuthentication(): PasswordAuthentication {
                    return PasswordAuthentication(senderEmail, senderAppPassword)
                }
            })

        try {
            val message = MimeMessage(session)
            message.setFrom(InternetAddress(senderEmail))
            message.setRecipients(
                Message.RecipientType.TO,
                toAddresses.joinToString(",") { it }
            )
            message.subject = subject

            val multipart = MimeMultipart()

            val textPart = MimeBodyPart()
            textPart.setText(body)
            multipart.addBodyPart(textPart)

            val attachPart = MimeBodyPart()
            attachPart.attachFile(file)
            multipart.addBodyPart(attachPart)

            message.setContent(multipart)

            Transport.send(message)
            println("Email enviado com sucesso.")

        } catch (e: Exception) {
            e.printStackTrace()
            throw RuntimeException("Erro ao enviar email: ${e.message}")
        }
    }
}
