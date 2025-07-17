package com.utkarsh.chatapp.dialogs

import android.graphics.Paint.Align
import android.provider.ContactsContract.CommonDataKinds.Email
import android.widget.CheckBox
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.utkarsh.chatapp.AppState

@Composable
fun CustomDialogBox(
    state: AppState,
    setEmail: (String) -> Unit,
    hideDialog: () -> Unit,
    addChat : () -> Unit
) {

    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    Dialog(onDismissRequest = { hideDialog.invoke() },
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnClickOutside = false,

        )
        ) {

        Card(
            elevation = CardDefaults.cardElevation(
                defaultElevation = 8.dp
            ),
            shape = MaterialTheme.shapes.large,
            modifier = Modifier.fillMaxWidth(.9f),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
//                containerColor = MaterialTheme.colorScheme.surfaceVariant,
            )
        ) {

            Column (
                modifier = Modifier.padding(start=24.dp, end = 24.dp, top = 24.dp, bottom = 8.dp),
//                verticalArrangement = Arrangement.spacedBy(25.dp)
            ){

                Text(text = "Enter Email ID",
                style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    label = {
                        Text(text = "Enter Email", color = MaterialTheme.colorScheme.onSurface, style = MaterialTheme.typography.bodyMedium)
                    },
                    value = state.srEmail,
                    onValueChange = {
                        setEmail(it)
                    },
                    shape = MaterialTheme.shapes.large,
                    modifier = Modifier.align(Alignment.CenterHorizontally).focusRequester(focusRequester),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Email,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = { addChat() }
                    ),
//                    maxLines = 5
//                    colors = TextFieldDefaults.colors(
//                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLow
//                    )
                )
                Spacer(modifier = Modifier.height(24.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically,
                )
                {
                    TextButton(onClick = {hideDialog.invoke()}) {
                        Text(text = "Cancel",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary
                            )
                    }
                    Spacer(modifier = Modifier.width(16.dp))

                    val isEnabled = state.srEmail.isNotBlank() && state.srEmail.contains("@") && state.srEmail.contains(".")

                    TextButton(
                        enabled = isEnabled,
                        onClick = {addChat.invoke() }
                    )
                    {
                        Text(text = "Add",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary.copy(if(isEnabled) 1f else .7f)
                        )
                    }
                }
            }
        }
    }

}