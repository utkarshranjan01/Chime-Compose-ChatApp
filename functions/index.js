const functions = require("firebase-functions/v1");
const admin = require("firebase-admin");

admin.initializeApp();

// Firestore trigger when a new message is added
exports.sendMessageNotification = functions.firestore
  .document("chats/{chatId}/message/{messageId}")
  .onCreate(async (snap, context) => {
    const message = snap.data();

    const receiverId = message.receiverId;
    const senderName = message.senderName || "Someone";
    const messageText =
      message.imageUrl !== ""
        ? "Sent a photo"
        : message.vidUrl !== ""
        ? "Sent a video"
        : message.fileUrl !== ""
        ? "Sent a file"
        : message.text || "Sent a message";
//    const imageUrl = message.ppurl

    // Log the message to confirm trigger ran
    console.log(`New message by ${senderName} to ${receiverId}`);

    // Fetch the receiver's FCM token from Firestore
    const userDoc = await admin.firestore().collection("users").doc(receiverId).get();
    const fcmToken = userDoc.data()?.fcmToken;

    if (!fcmToken) {
      console.log(`No FCM token for user ${receiverId}`);
      return;
    }

    const payload = {
      token: fcmToken,
      android: {
        priority: "high",
        notification: {
          title: `${senderName}`,
          body: messageText,
          channelId: "high_importance_channel",
//          image: imageUrl
        }
      }
    };

    try {
      await admin.messaging().send(payload);
      console.log(`Notification sent to ${receiverId}`);
    } catch (error) {
      console.error("Error sending FCM:", error);
    }
  });
