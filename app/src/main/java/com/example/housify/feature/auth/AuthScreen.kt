package com.example.housify.feature.auth

import android.util.Patterns
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.housify.ui.theme.DarkBlue
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import com.example.housify.R
import androidx.compose.ui.draw.clip
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.unit.Dp
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun AuthScreen(
    viewModel: AuthViewModel = hiltViewModel(),
    onSuccessLogin: () -> Unit
) {
    var selectedTab by remember { mutableStateOf(0) }
    val authState by viewModel.authState.collectAsState()

    LaunchedEffect(authState) {
        if (authState is AuthState.LoginSuccess) {
            onSuccessLogin()
        }
    }

//    Box(
//        modifier = Modifier.fillMaxSize(),
//    ) {
//        Column(
//            modifier = Modifier
//                .fillMaxWidth()
//                .padding(horizontal = 40.dp, vertical = 30.dp),
//            verticalArrangement = Arrangement.spacedBy(16.dp),
//            horizontalAlignment = Alignment.CenterHorizontally
//        ) {
//            // App Logo
//            Image(
//                painter = painterResource(id = R.drawable.app_logo_with_text),
//                contentDescription = "App Logo",
//                colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.primary),
//                modifier = Modifier
//                    .size(200.dp)
////                    .padding(bottom = 16.dp)
//            )
//
//            val tabs = listOf("LOGIN", "SIGNUP")
//
//            Box(
//                modifier = Modifier
//                    .clip(RoundedCornerShape(12.dp)) // rounded corners
//                    .border(
//                        1.dp,
//                        Color.Transparent,
//                        RoundedCornerShape(12.dp)
//                    ) // border
//            ) {
//                // Toggling between login and signup
//                TabRow(
//                    selectedTabIndex = selectedTab,
//                    indicator = {},
//                    containerColor = MaterialTheme.colorScheme.secondary,
//                ) {
//
//                    tabs.forEachIndexed { index, title ->
//                        Tab(
//                            selected = selectedTab == index,
//                            onClick = { selectedTab = index }
//                        ) {
//                            Box(
//                                modifier = Modifier
//                                    .fillMaxSize()
//                                    .padding(4.dp)
//                                    .background(
//                                        color = if (selectedTab == index) MaterialTheme.colorScheme.onPrimary else Color.Transparent,
//                                        shape = RoundedCornerShape(12.dp)
//                                    )
//                                    .padding(vertical = 8.dp, horizontal = 16.dp),
//                                contentAlignment = Alignment.Center
//                            ) {
//                                Text(
//                                    text = title,
//                                    color = if (selectedTab == index) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onBackground,
//                                    style = MaterialTheme.typography.bodyLarge
//                                )
//                            }
//                        }
//                    }
//                }
//            }
//
//            // --- Form toggle ---
//            if (selectedTab == 0) {
//                LoginForm(viewModel, authState)
//            } else {
//                SignupForm(viewModel, authState)
//            }
//        }
//    }

    BoxWithConstraints(
        modifier = Modifier.fillMaxSize()
    ) {
        val isCompactHeight = maxHeight < 760.dp

        // Adaptive values based on height
        val logoSize: Dp = if (isCompactHeight) maxHeight * 0.22f else 200.dp
        val horizontalPadding = if (isCompactHeight) 24.dp else 40.dp
        val verticalPadding = if (isCompactHeight) 16.dp else 30.dp
        val elementSpacing = if (isCompactHeight) 8.dp else 16.dp

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = horizontalPadding, vertical = verticalPadding),
            verticalArrangement = Arrangement.spacedBy(elementSpacing),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(elementSpacing),
                modifier = Modifier.fillMaxWidth()
            ) {
                Image(
                    painter = painterResource(id = R.drawable.app_logo_with_text),
                    contentDescription = "App Logo",
                    colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.primary),
                    modifier = Modifier.size(logoSize)
                )

                val tabs = listOf("LOGIN", "SIGNUP")

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .border(
                            1.dp,
                            Color.Transparent,
                            RoundedCornerShape(12.dp)
                        )
                ) {
                    TabRow(
                        selectedTabIndex = selectedTab,
                        indicator = {},
                        containerColor = MaterialTheme.colorScheme.secondary,
                    ) {
                        tabs.forEachIndexed { index, title ->
                            Tab(
                                selected = selectedTab == index,
                                onClick = { selectedTab = index }
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(4.dp)
                                        .background(
                                            color = if (selectedTab == index) MaterialTheme.colorScheme.onPrimary else Color.Transparent,
                                            shape = RoundedCornerShape(12.dp)
                                        )
                                        .padding(vertical = 8.dp, horizontal = 16.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = title,
                                        color = if (selectedTab == index) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onBackground,
                                        style = MaterialTheme.typography.bodyLarge
                                    )
                                }
                            }
                        }
                    }
                }
            }

            if (!isCompactHeight) {
                Spacer(modifier = Modifier.height(8.dp))
            }

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(elementSpacing)
            ) {
                if (selectedTab == 0) {
                    LoginForm(viewModel, authState)
                } else {
                    SignupForm(viewModel, authState, isCompactHeight)
                }
            }
        }
    }
}

@Composable
fun LoginForm(
    viewModel: AuthViewModel,
    authState: AuthState,
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    var passwordVisible by remember { mutableStateOf(false) }

    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        // Email TextField
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("abc@email.com") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Email,
                    contentDescription = "Email Icon",
                )
            },
            modifier = Modifier.fillMaxWidth(),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.onPrimary,
                unfocusedContainerColor = MaterialTheme.colorScheme.onPrimary,
            )
        )

        // Password
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            singleLine = true,
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = "Password Icon"
                )
            },
            trailingIcon = {
                val image =
                    if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                val description = if (passwordVisible) "Hide password" else "Show password"

                Icon(
                    imageVector = image,
                    contentDescription = description,
                    modifier = Modifier.clickable { passwordVisible = !passwordVisible }
                )
            },
            modifier = Modifier.fillMaxWidth(),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.onPrimary,
                unfocusedContainerColor = MaterialTheme.colorScheme.onPrimary,
            )
        )

        // Show error from Firebase
        if (authState is AuthState.Error) {
            Text(
                text = "Please check your credentials and try again",
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        }

        // Login Button
        Button(
            onClick = {
                viewModel.login(email, password)
            },
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = DarkBlue
            )
        ) {
            Text(
                "LOG IN",
                modifier = Modifier.padding(6.dp),
                style = MaterialTheme.typography.bodyMedium
            )
        }

        // Show loading indicator
        if (authState is AuthState.Loading) {
            CircularProgressIndicator(
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(8.dp)
            )
        }
    }
}

//@Composable
//fun SignupForm(viewModel: AuthViewModel, authState: AuthState) {
//    var profileName by remember { mutableStateOf("") }
//    var email by remember { mutableStateOf("") }
//    var password by remember { mutableStateOf("") }
//    var confirmPassword by remember { mutableStateOf("") }
//    var passwordVisible by remember { mutableStateOf(false) }
//    var passwordError by remember { mutableStateOf<String?>(null) }
//
//    // Clear fields after successful registration
//    LaunchedEffect(authState) {
//        if (authState is AuthState.RegisterSuccess) {
//            profileName = ""
//            email = ""
//            password = ""
//            confirmPassword = ""
//        }
//    }
//
//    Column(
//        verticalArrangement = Arrangement.spacedBy(12.dp),
//        modifier = Modifier.fillMaxWidth()
//    ) {
//
//        // Profile Name
//        OutlinedTextField(
//            value = profileName,
//            onValueChange = { profileName = it },
//            label = { Text("Profile name") },
//            singleLine = true,
//            leadingIcon = {
//                Icon(
//                    imageVector = Icons.Default.Person,
//                    contentDescription = "Profile Icon"
//                )
//            },
//            modifier = Modifier
//                .fillMaxWidth()
//                .height(40.dp),
//            colors = TextFieldDefaults.colors(
//                focusedContainerColor = MaterialTheme.colorScheme.onPrimary,
//                unfocusedContainerColor = MaterialTheme.colorScheme.onPrimary,
//            )
//        )
//
//        // Email
//        OutlinedTextField(
//            value = email,
//            onValueChange = { email = it },
//            label = { Text("Email address") },
//            singleLine = true,
//            leadingIcon = {
//                Icon(
//                    imageVector = Icons.Default.Email,
//                    contentDescription = "Email Icon"
//                )
//            },
//            modifier = Modifier.fillMaxWidth(),
//            colors = TextFieldDefaults.colors(
//                focusedContainerColor = MaterialTheme.colorScheme.onPrimary,
//                unfocusedContainerColor = MaterialTheme.colorScheme.onPrimary,
//            )
//        )
//
//        // Password
//        OutlinedTextField(
//            value = password,
//            onValueChange = { password = it },
//            label = { Text("Password") },
//            singleLine = true,
//            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
//            leadingIcon = {
//                Icon(
//                    imageVector = Icons.Default.Lock,
//                    contentDescription = "Password Icon"
//                )
//            },
//            trailingIcon = {
//                val image =
//                    if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
//                val description = if (passwordVisible) "Hide password" else "Show password"
//
//                Icon(
//                    imageVector = image,
//                    contentDescription = description,
//                    modifier = Modifier.clickable { passwordVisible = !passwordVisible }
//                )
//            },
//            modifier = Modifier.fillMaxWidth(),
//            colors = TextFieldDefaults.colors(
//                focusedContainerColor = MaterialTheme.colorScheme.onPrimary,
//                unfocusedContainerColor = MaterialTheme.colorScheme.onPrimary,
//            )
//        )
//
//        // Confirm Password
//        OutlinedTextField(
//            value = confirmPassword,
//            onValueChange = { confirmPassword = it },
//            label = { Text("Confirm password") },
//            singleLine = true,
//            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
//            leadingIcon = {
//                Icon(
//                    imageVector = Icons.Default.Lock,
//                    contentDescription = "Confirm Password Icon"
//                )
//            },
//            trailingIcon = {
//                val image =
//                    if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
//                val description = if (passwordVisible) "Hide password" else "Show password"
//
//                Icon(
//                    imageVector = image,
//                    contentDescription = description,
//                    modifier = Modifier.clickable { passwordVisible = !passwordVisible }
//                )
//            },
//            modifier = Modifier.fillMaxWidth(),
//            colors = TextFieldDefaults.colors(
//                focusedContainerColor = MaterialTheme.colorScheme.onPrimary,
//                unfocusedContainerColor = MaterialTheme.colorScheme.onPrimary,
//            )
//        )
//
//        // Show password mismatch error
//        if (passwordError != null) {
//            Text(
//                text = passwordError!!,
//                color = MaterialTheme.colorScheme.error,
//                style = MaterialTheme.typography.bodySmall,
//                modifier = Modifier.padding(start = 16.dp)
//            )
//        }
//
//        if (authState is AuthState.Error) {
//            Text(
//                text = authState.message,
//                color = MaterialTheme.colorScheme.error,
//                style = MaterialTheme.typography.bodySmall,
//                modifier = Modifier.padding(start = 16.dp, top = 4.dp)
//            )
//        }
//
//        if (authState is AuthState.RegisterSuccess) {
//            Text(
//                text = "Registered successfully",
//                style = MaterialTheme.typography.bodySmall,
//                modifier = Modifier.padding(start = 16.dp)
//            )
//        }
//
//        // Sign Up Button
//        Button(
//            onClick = {
//                when {
//                    profileName.isBlank() -> {
//                        passwordError = "Profile name cannot be empty"
//                    }
//
//                    email.isBlank() -> {
//                        passwordError = "Email cannot be empty"
//                    }
//
//                    password.isBlank() -> {
//                        passwordError = "Password cannot be empty"
//                    }
//
//                    !Patterns.EMAIL_ADDRESS.matcher(email).matches() -> {
//                        passwordError = "Please enter a valid email address"
//                    }
//
//                    password.length < 6 -> {
//                        passwordError = "Password must be at least 6 characters"
//                    }
//
//                    password != confirmPassword -> {
//                        passwordError = "Passwords do not match"
//                    }
//
//                    else -> {
//                        passwordError = null
//                        viewModel.register(profileName, email, password)
//                    }
//                }
//            },
//            shape = RoundedCornerShape(12.dp),
//            modifier = Modifier
//                .fillMaxWidth()
//                .padding(vertical = 16.dp),
//            colors = ButtonDefaults.buttonColors(containerColor = DarkBlue)
//        ) {
//            Text(
//                "SIGN UP",
//                modifier = Modifier.padding(10.dp),
//                style = MaterialTheme.typography.bodyMedium
//            )
//        }
//
//        // Show loading indicator
//        if (authState is AuthState.Loading) {
//            CircularProgressIndicator(
//                modifier = Modifier
//                    .align(Alignment.CenterHorizontally)
//                    .padding(8.dp)
//            )
//        }
//    }
//}

@Composable
fun SignupForm(
    viewModel: AuthViewModel,
    authState: AuthState,
    isCompactHeight: Boolean
) {
    var profileName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var passwordError by remember { mutableStateOf<String?>(null) }

    val fieldHeight = if (isCompactHeight) 48.dp else 56.dp
    val fieldTextStyle = if (isCompactHeight) {
        MaterialTheme.typography.bodySmall
    } else {
        MaterialTheme.typography.bodyMedium
    }

    LaunchedEffect(authState) {
        if (authState is AuthState.RegisterSuccess) {
            profileName = ""
            email = ""
            password = ""
            confirmPassword = ""
        }
    }

    Column(
        verticalArrangement = Arrangement.spacedBy(if (isCompactHeight) 8.dp else 12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        // Profile Name
        OutlinedTextField(
            value = profileName,
            onValueChange = { profileName = it },
            label = { Text("Profile name", style = fieldTextStyle) },
            singleLine = true,
            textStyle = fieldTextStyle,
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "Profile Icon"
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = fieldHeight),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.onPrimary,
                unfocusedContainerColor = MaterialTheme.colorScheme.onPrimary,
            )
        )

        // Email
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email address", style = fieldTextStyle) },
            singleLine = true,
            textStyle = fieldTextStyle,
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Email,
                    contentDescription = "Email Icon"
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = fieldHeight),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.onPrimary,
                unfocusedContainerColor = MaterialTheme.colorScheme.onPrimary,
            )
        )

        // Password
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password", style = fieldTextStyle) },
            singleLine = true,
            textStyle = fieldTextStyle,
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = "Password Icon"
                )
            },
            trailingIcon = {
                val image =
                    if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                val description = if (passwordVisible) "Hide password" else "Show password"

                Icon(
                    imageVector = image,
                    contentDescription = description,
                    modifier = Modifier.clickable { passwordVisible = !passwordVisible }
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = fieldHeight),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.onPrimary,
                unfocusedContainerColor = MaterialTheme.colorScheme.onPrimary,
            )
        )

        // Confirm Password
        OutlinedTextField(
            value = confirmPassword,
            onValueChange = { confirmPassword = it },
            label = { Text("Confirm password", style = fieldTextStyle) },
            singleLine = true,
            textStyle = fieldTextStyle,
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = "Confirm Password Icon"
                )
            },
            trailingIcon = {
                val image =
                    if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                val description = if (passwordVisible) "Hide password" else "Show password"

                Icon(
                    imageVector = image,
                    contentDescription = description,
                    modifier = Modifier.clickable { passwordVisible = !passwordVisible }
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = fieldHeight),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.onPrimary,
                unfocusedContainerColor = MaterialTheme.colorScheme.onPrimary,
            )
        )

//      Show password mismatch error
        if (passwordError != null) {
            Text(
                text = passwordError!!,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(start = 16.dp)
            )
        }

        if (authState is AuthState.Error) {
            Text(
                text = authState.message,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(start = 16.dp, top = 4.dp)
            )
        }

        if (authState is AuthState.RegisterSuccess) {
            Text(
                text = "Registered successfully",
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(start = 16.dp)
            )
        }

        Button(
            onClick = {
                when {
                    profileName.isBlank() -> {
                        passwordError = "Profile name cannot be empty"
                    }
                    email.isBlank() -> {
                        passwordError = "Email cannot be empty"
                    }
                    password.isBlank() -> {
                        passwordError = "Password cannot be empty"
                    }
                    !Patterns.EMAIL_ADDRESS.matcher(email).matches() -> {
                        passwordError = "Please enter a valid email address"
                    }
                    password.length < 6 -> {
                        passwordError = "Password must be at least 6 characters"
                    }
                    password != confirmPassword -> {
                        passwordError = "Passwords do not match"
                    }
                    else -> {
                        passwordError = null
                        viewModel.register(profileName, email, password)
                    }
                }
            },
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = if (isCompactHeight) 8.dp else 16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = DarkBlue)
        ) {
            Text(
                "SIGN UP",
                modifier = Modifier.padding(if (isCompactHeight) 6.dp else 10.dp),
                style = if (isCompactHeight) {
                    MaterialTheme.typography.bodySmall
                } else {
                    MaterialTheme.typography.bodyMedium
                }
            )
        }

        if (authState is AuthState.Loading) {
            CircularProgressIndicator(
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(4.dp)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AuthScreenPreview() {
    AuthScreen(onSuccessLogin = {})
}