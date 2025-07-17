package com.utkarsh.chatapp.googleSign

import android.content.Context
import android.content.Intent
import android.content.IntentSender
import com.google.android.gms.auth.api.identity.BeginSignInRequest
import com.google.android.gms.auth.api.identity.BeginSignInRequest.GoogleIdTokenRequestOptions
import com.google.android.gms.auth.api.identity.SignInClient
import com.google.firebase.Firebase
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.auth
import com.utkarsh.chatapp.AuthViewModel
import com.utkarsh.chatapp.ChatViewModel
import com.utkarsh.chatapp.R
import com.utkarsh.chatapp.SignInResult
import com.utkarsh.chatapp.UserData
import kotlinx.coroutines.tasks.await
import kotlin.coroutines.cancellation.CancellationException

class GoogleAuthUiClient(
    private val context: Context, // if we need to like show a toast or something
    private val oneTapClient: SignInClient,
    val viewModel: ChatViewModel
) {
    private val auth = Firebase.auth


    suspend fun signIn(): IntentSender?{
        val result = try {
            oneTapClient.beginSignIn(
                buildSignInRequest()
            ).await()
        }catch (e : Exception){
            e.printStackTrace()
            if(e is CancellationException) throw e // meaning if we cancelled signin then do not throw any error
            null
        }
        return result?.pendingIntent?.intentSender
        }

    private fun buildSignInRequest(): BeginSignInRequest {
        return BeginSignInRequest.builder().setGoogleIdTokenRequestOptions(
            GoogleIdTokenRequestOptions.builder().setSupported(true)
                .setFilterByAuthorizedAccounts(false).setServerClientId("540931381594-tggo0f9a1ks36ap9i43kqnmmp2869vk2.apps.googleusercontent.com").build()
        ).setAutoSelectEnabled(true).build()
    }


    suspend fun signInWithIntent(intent: Intent): SignInResult {
//        viewModel.resetState()
        val cred = oneTapClient.getSignInCredentialFromIntent(intent)
        val googleIdToken = cred.googleIdToken
        val  googleCred = GoogleAuthProvider.getCredential(googleIdToken,null)
        return try {
            val user = auth.signInWithCredential(googleCred).await().user
            SignInResult(
                errorMessage = null,
                data = user?.run {
                    UserData(
                        userId = uid,
                        username = displayName.toString(),
                        ppurl = photoUrl.toString().substring(0, photoUrl.toString().length-6), // as it can have old information as well
                        email = email.toString()
                    )
                }
            )
        } catch (e: Exception)
        {
            e.printStackTrace()
            if(e is CancellationException) throw e
            SignInResult(
                errorMessage = e.message,
                data = null)
        }
    }

    fun getSignedInUser(): UserData?= auth.currentUser?.run {
        UserData(
            email = email.toString(),
            username = displayName,
            userId = uid,
            ppurl = photoUrl.toString(),
        )

    }
    suspend fun signOut() {
        try {
            oneTapClient.signOut().await()
            auth.signOut()
            viewModel.setSigningIn(false) // If you sign in and sign out without leaving the app then this is to enable the sign in button again
        } catch(e: Exception) {
            e.printStackTrace()
            if(e is CancellationException) throw e
        }
    }


}




