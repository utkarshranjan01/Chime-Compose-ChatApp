package com.utkarsh.chatapp.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.utkarsh.chatapp.R
import com.utkarsh.chatapp.utilities.RequestNotificationPermissionOnce

//@Preview(showSystemUi = true)
@Composable
fun SignInScreenUI(
    onSignInClick: () -> Unit,
    isSigningIn : Boolean
) {
    // This requests the notification permission once
    RequestNotificationPermissionOnce()

        Box(modifier = Modifier.fillMaxSize()) {

            // Background Image (bottom-most layer)
            Image(
                painter = painterResource(id = R.drawable.background2),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
                    .alpha(0.11f) // Optional: lower to 0.8f or so if too strong
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
//                    .background(MaterialTheme.colorScheme.background)
                ,
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            )
            {
                Image(
                    painter = painterResource(id = R.drawable.play_store_512),
//                    painter = painterResource(id = R.drawable._023449),
                    contentDescription = "Login Image",
                    modifier = Modifier.size(320.dp)
                        .clip(CircleShape)
//                        .clip(RoundedCornerShape(24.dp))
//                        .border(5.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(24.dp))
                    ,
                    contentScale = ContentScale.Crop
                )
                Spacer(modifier = Modifier.height(90.dp))
                Text(
                    text = "Welcome to Chime",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 36.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Chatting has never been this easy",
                    style = MaterialTheme.typography.titleMedium.copy(fontSize = 18.sp),
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(70.dp))

                OutlinedButton(
                    onClick = { onSignInClick() },
                    enabled = !isSigningIn,
                    modifier = Modifier.fillMaxWidth(.7f).height(60.dp).border(
                        width = 4.dp,
                        color = MaterialTheme.colorScheme.primary,
                        CircleShape
                    ).background(MaterialTheme.colorScheme.background).shadow(10.dp),
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.goog_0ed88f7c),
                        contentDescription = null,
                        Modifier.padding(10.dp)
                    )
//                Spacer(modifier = Modifier.width(15.dp))
                    Text(
                        text = "Continue With Google",
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }
        }
}