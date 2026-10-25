package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AppLanguage
import com.example.ui.navigation.Screen
import com.example.ui.theme.CrimsonPrimary
import com.example.ui.viewmodel.MainViewModel

@Composable
fun SignInScreen(
    viewModel: MainViewModel,
    language: AppLanguage,
    onSuccess: () -> Unit
) {
    val isTa = language == AppLanguage.TAMIL
    var email by remember { mutableStateOf("creator@youtubeautomation.ai") }
    var password by remember { mutableStateOf("••••••••••••") }
    var isLoading by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(72.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(CrimsonPrimary),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.PlayArrow,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(40.dp)
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = if (isTa) "ஸ்டுடியோ கணக்கில் உள்நுழையவும்" else "Sign In to Creator Studio",
            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold)
        )

        Text(
            text = if (isTa) "மேகக்கணி AI மற்றும் YouTube சேனலை நிர்வகிக்க உள்நுழையவும்" else "Access your automation workflows, render queues and channel analytics",
            style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant),
            modifier = Modifier.padding(top = 6.dp, bottom = 28.dp)
        )

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text(if (isTa) "மின்னஞ்சல் (Email)" else "Email Address") },
            leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth().testTag("signin_email")
        )

        Spacer(modifier = Modifier.height(14.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text(if (isTa) "கடவுச்சொல் (Password)" else "Password") },
            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
            visualTransformation = PasswordVisualTransformation(),
            singleLine = true,
            modifier = Modifier.fillMaxWidth().testTag("signin_password")
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                isLoading = true
                viewModel.showStatus(if (isTa) "வெற்றிகரமாக உள்நுழைந்துள்ளீர்கள்!" else "Welcome back, Creator!")
                onSuccess()
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .testTag("signin_submit_btn"),
            colors = ButtonDefaults.buttonColors(containerColor = CrimsonPrimary),
            shape = RoundedCornerShape(12.dp)
        ) {
            if (isLoading) {
                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
            } else {
                Text(
                    text = if (isTa) "உள்நுழையவும் (Sign In)" else "Sign In",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        TextButton(
            onClick = { onSuccess() },
            modifier = Modifier.testTag("signin_guest_btn")
        ) {
            Text(
                text = if (isTa) "விருந்தினராக தொடரவும் (Continue as Guest)" else "Continue as Guest Creator",
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}
