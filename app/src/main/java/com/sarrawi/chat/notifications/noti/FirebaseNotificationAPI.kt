import com.sarrawi.chat.notifications.noti.NotificationRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

interface FirebaseNotificationAPI {
    @POST("v1/projects/YOUR_PROJECT_ID/messages:send")
    suspend fun sendNotification(
        @Header("Authorization") accessToken: String,
        @Body notificationRequest: NotificationRequest
    ): Response<Unit>
}
