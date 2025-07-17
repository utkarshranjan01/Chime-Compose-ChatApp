package com.utkarsh.chatapp

import java.sql.Timestamp


data class SignInResult(

    val data: UserData?,
    val errorMessage: String?

)

data class UserData(
    val userId: String="",
    val username: String?="",
    val ppurl: String?="",
    val email: String?="",
    val bio: String="Hey there, I am using Chime",
    val fcmToken: String? = null
)

data class AppState(
    val isSignedIn: Boolean = false,
    val userData: UserData?=null,
    val signInError: String? = null,
    val srEmail: String = "",
    val showDialog: Boolean = false,
    val User2: ChatUserData?=null,
    val chatId: String="",
) {

}


data class ChatData(
    val chatId: String="",
    val last: Message ?= null,
    val user1 : ChatUserData ?= null,
    val user2 : ChatUserData ?= null,
)
data class Message(
    val msgId: String="",
    val senderId: String="",
    val receiverId: String="",
    val senderName: String="",
    val repliedMessage: Message?=null,
    val reaction : List<Reaction> = emptyList(),
    val imageUrl : String="",
    val fileUrl: String="",
    val text: String="",
    val fileName: String="",
    val fileSize: String="",
    val vidUrl: String="",
    val progress: String="",
    val content: String="",
    val time: com.google.firebase.Timestamp?=null,
    val forwarded: Boolean=false,
    val read: Boolean=false,
    val first: Boolean=false,
    val ppurl: String="",
    val reactSender: String="",
    val reactReceiver: String="",
)
data class Reaction(
    val ppurl: String="",
    val userId: String="",
    val username: String="",
    val reaction: String="",

)

data class ChatUserData(
    val userId: String="",
    val typing : Boolean=false,
    val bio : String="",
    val username: String?="", // this could be null
    val ppurl: String="",
    val email: String="",
    val status: Boolean=false,
    val unread : Int=0,
)

data class Image(
    val imgUrl: String="",
    var time: com.google.firebase.Timestamp?= com.google.firebase.Timestamp.now()
)

data class Story(
    val id : String="",
    val userId : String="",
    val userName : String?="",
    val ppurl : String="",
    val images: List<Image> = emptyList(),
)