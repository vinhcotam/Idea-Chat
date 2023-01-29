package com.example.chatproject.utilities

class Constants {


    companion object {
        const val KEY_COLLECTION_USERS = "users"
        const val KEY_NAME = "name"
        const val KEY_EMAIL = "email"
        const val KEY_PASSWORD = "password"
        const val KEY_PREFERENCE_NAME = "chatProjectPreference"
        const val KEY_IS_SIGNED_IN = "isSignedIn"
        const val KEY_USER_ID = "userId"
        const val KEY_IMAGE = "image"
        const val KEY_FCM_TOKEN = "fcmToken"
        const val KEY_USER = "user"
        const val KEY_COLLECTION_CHAT = "chat"
        const val KEY_SENDER_ID = "senderId"
        const val KEY_RECEIVER_ID = "receiverId"
        const val KEY_MESSAGE = "message"
        const val KEY_TIMESTAMP = "timestamp"
        const val KEY_CONVERSATION = "conversations"
        const val KEY_SENDER_NAME = "senderName"
        const val KEY_RECEIVER_NAME = "receivedName"
        const val KEY_SENDER_IMAGE = "senderImage"
        const val KEY_RECEIVER_IMAGE = "receiverImage"
        const val KEY_LAST_MESSAGE = "lastMessage"
        const val KEY_AVAILABILITY = "availability"
        private const val REMOTE_MSG_AUTHORIZATION = "Authorization"
        private const val REMOTE_MSG_CONTENT_TYPE = "Content-Type"
        const val REMOTE_MSG_DATA = "data"
        const val REMOTE_MSG_REGISTRATION_IDS = "registration_ids"
        private var remoteMsgHeaders: HashMap<String, String>? = null
        fun getRemoteMsgHeaders(): HashMap<String, String> {
            if (remoteMsgHeaders == null) {
                remoteMsgHeaders = HashMap()
                remoteMsgHeaders!![REMOTE_MSG_AUTHORIZATION] = "key=AAAA5UG_a5U:APA91bGBavFuBBf0YAxt_KNE3bwu0QEFOFaseeYwo4NpCNmPdM79Vp0XoxfSLsd0qYhIIivmN0aBB-xlENATlK8yxLajEgjzZ1SD3se4EZYuMLlR487Rq2_doPiOooaFQzmMHaElI2la"
                remoteMsgHeaders!![REMOTE_MSG_CONTENT_TYPE] ="application/json"
            }
            return remoteMsgHeaders as HashMap<String, String>
        }

    }
}