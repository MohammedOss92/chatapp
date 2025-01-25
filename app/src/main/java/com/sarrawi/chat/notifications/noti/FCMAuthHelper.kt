package com.sarrawi.chat.notifications.noti

import com.google.auth.oauth2.GoogleCredentials
import java.io.File
import java.io.FileInputStream

object FCMAuthHelper {
    private const val SERVICE_ACCOUNT_PATH = "path/to/service-account.json"

    fun getAccessToken(): String {
        val serviceAccount = File(SERVICE_ACCOUNT_PATH)
        val googleCredentials = GoogleCredentials.fromStream(FileInputStream(serviceAccount))
            .createScoped(listOf("https://www.googleapis.com/auth/cloud-platform"))
        googleCredentials.refreshIfExpired()
        return "Bearer ${googleCredentials.accessToken.tokenValue}"
    }
}
