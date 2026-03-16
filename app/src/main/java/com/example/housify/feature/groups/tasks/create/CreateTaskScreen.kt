package com.example.housify.feature.groups.tasks.create

import android.annotation.SuppressLint
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.housify.R
import com.example.housify.convertMillisToDate
import com.example.housify.convertMillisToDayName
import com.example.housify.domain.model.Repeat
import com.example.housify.domain.model.Space
import com.example.housify.domain.model.User
import com.example.housify.ui.components.ErrorFullScreen
import com.example.housify.ui.components.LoadingFullScreen
import com.example.housify.ui.components.UserAvatar
import com.example.housify.ui.theme.HousifyTheme

data class RadioOption(
    val displayText: String,
    val repeatType: Repeat
)

@Composable
fun CreateTaskScreen(
    onShowSnackbar: (String) -> Unit,
    onBack: () -> Unit,
    viewModel: CreateTaskViewModel = hiltViewModel()
) {
    val uiState = viewModel.uiState

    when {
        uiState.loading -> {
            LoadingFullScreen()
        }

        uiState.error != null -> {
            ErrorFullScreen(uiState.error, viewModel::refreshData)
        }

        else -> {
            var title by rememberSaveable { mutableStateOf("") }
            var rotationEnabled by rememberSaveable { mutableStateOf(false) }
            var selectedSpace by remember {
                mutableStateOf(uiState.availableSpaces.first())
            }
            var selectedAssignees by remember {
                mutableStateOf<List<User>>(emptyList())
            }
            var selectAssigneesError by remember { mutableStateOf<String?>(null) }
            var showDatePickerModal by remember { mutableStateOf(false) }
            var selectedDate by remember { mutableStateOf<Long?>(null) }
            val radioOptions = remember(selectedDate) {
                listOfNotNull(
                    RadioOption(Repeat.NONE.displayText, Repeat.NONE),
                    RadioOption(Repeat.DAILY.displayText, Repeat.DAILY),
                    selectedDate?.let {
                        RadioOption(
                            "Weekly on ${convertMillisToDayName(it)}",
                            Repeat.WEEKLY
                        )
                    }
                )
            }
            var selectedRepeatOption by remember { mutableStateOf(radioOptions.first()) }

            LaunchedEffect(Unit) {
                viewModel.successMessage.collect { message ->
                    onShowSnackbar(message)
                }
            }

            LaunchedEffect(Unit) {
                viewModel.onBackEvent.collect {
                    onBack()
                }
            }

            if (showDatePickerModal) {
                DatePickerModal(
                    onDateSelected = { selectedDate = it },
                    onDismiss = { showDatePickerModal = false }
                )
            }

            Box(modifier = Modifier.fillMaxSize()) {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(20.dp)) {
                    item {
                        TaskTitleTextField(
                            title = title,
                            titleChange = { title = it })
                    }

                    item {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(5.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    "Start from",
                                    style = MaterialTheme.typography.titleMedium
                                )
                                Text(
                                    "(At least start from the next day)",
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                            SelectDateField(
                                selectedDate = selectedDate,
                                onShowDatePickerModal = { showDatePickerModal = true })
                        }
                    }



                    if (selectedDate != null) {
                        item {
                            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                Text("Repeat Setting", style = MaterialTheme.typography.titleMedium)
                                SelectRepeatRadios(
                                    radioOptions = radioOptions,
                                    selectedRepeatOption = selectedRepeatOption,
                                    onSelectedRepeatOptionChange = { selectedRepeatOption = it },
                                    disableRotation = { rotationEnabled = false }
                                )
                            }
                        }
                    }

                    item {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(5.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Assign to", style = MaterialTheme.typography.titleMedium)
                                Text(
                                    "(Multiple Select)",
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                            ScrollableSelectAssignees(
                                availableUsers = uiState.availableUsers,
                                selectedAssignees = selectedAssignees,
                                rotationEnabled = rotationEnabled,
                                selectedRepeatType = selectedRepeatOption.repeatType,
                                onNewAssigneesSelected = { selectedAssignees = it },
                                onSelectAssigneesError = { selectAssigneesError = it }
                            )


                            if (selectedRepeatOption.repeatType !== Repeat.NONE &&
                                selectedAssignees.size > 1
                            ) {
                                RotationCheckbox(
                                    rotationEnabled = rotationEnabled,
                                    rotationEnabledChange = { rotationEnabled = it }
                                )
                            }

                            selectAssigneesError?.let {
                                Text(
                                    text = it,
                                    color = MaterialTheme.colorScheme.error,
                                )
                            }
                        }
                    }

                    item {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(5.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Space", style = MaterialTheme.typography.titleMedium)
                                Text("(Select One)", style = MaterialTheme.typography.bodySmall)
                            }
                            ScrollableSelectSpaces(
                                availableSpaces = uiState.availableSpaces,
                                selectedSpace = selectedSpace,
                                onSelectSpace = { selectedSpace = it }
                            )
                        }
                    }

                    item {
                        Spacer(modifier = Modifier.height(50.dp))
                    }
                }

                Column(modifier = Modifier.align(Alignment.BottomCenter)) {
                    if (uiState.createTaskError != null) {
                        Text(
                            text = uiState.createTaskError,
                            color = MaterialTheme.colorScheme.error,
                        )
                    }

                    ConfirmButton(
                        onClick = {
                            viewModel.createTask(
                                title = title,
                                space = selectedSpace,
                                assignees = selectedAssignees,
                                rotationEnabled = rotationEnabled,
                                repeatSetting = selectedRepeatOption.repeatType,
                                startFrom = selectedDate
                            )
                        },
                        loading = uiState.createTaskLoading,
                    )
                }
            }
        }
    }
}

@Composable
fun ConfirmButton(
    onClick: () -> Unit,
    loading: Boolean,
    modifier: Modifier = Modifier,
) {
    Button(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(vertical = 15.dp),
        shape = RoundedCornerShape(30)
    ) {
        if (loading) {
            CircularProgressIndicator(
                modifier = Modifier.size(24.dp),
                color = MaterialTheme.colorScheme.onPrimary,
                strokeWidth = 2.dp
            )
        } else {
            Text("create".uppercase())
        }
    }
}

@Composable
fun RotationCheckbox(
    rotationEnabled: Boolean,
    rotationEnabledChange: (Boolean) -> Unit
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Checkbox(
            checked = rotationEnabled,
            onCheckedChange = rotationEnabledChange
        )
        Text("In rotation")
    }
}

@Composable
fun ScrollableSelectSpaces(
    availableSpaces: List<Space>,
    selectedSpace: Space,
    onSelectSpace: (Space) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 60.dp, max = 150.dp)
            .clip(RoundedCornerShape(10)),
    ) {
        items(availableSpaces) { space ->
            val selected = space == selectedSpace
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp)
                    .selectable(
                        selected = selected,
                        onClick = {
                            onSelectSpace(space)
                        },
                        role = Role.RadioButton
                    )
                    .background(
                        if (selected) {
                            MaterialTheme.colorScheme.surfaceVariant
                        } else MaterialTheme.colorScheme.surface
                    )
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,

                ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(15.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        painter = painterResource(space.icon),
                        contentDescription = space.name,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        (space.name).replaceFirstChar { it.uppercase() },
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                if (selected) {
                    Icon(
                        painter = painterResource(R.drawable.tick),
                        contentDescription = "Selected",
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(30.dp)

                    )
                }
            }
        }
    }
}

@Composable
fun ScrollableSelectAssignees(
    availableUsers: List<User>,
    selectedAssignees: List<User>,
    rotationEnabled: Boolean,
    selectedRepeatType: Repeat,
    onNewAssigneesSelected: (List<User>) -> Unit,
    onSelectAssigneesError: (String?) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 60.dp, max = 150.dp)
            .clip(RoundedCornerShape(10)),
    ) {
        items(availableUsers) { user ->
            val selected = selectedAssignees.contains(user)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp)
                    .selectable(
                        selected = selected,
                        onClick = {
                            val newAssignees = if (selected) {
                                selectedAssignees - user
                            } else {
                                selectedAssignees + user
                            }

                            if (newAssignees.size <= 1 &&
                                rotationEnabled &&
                                selectedRepeatType != Repeat.NONE
                            ) {
                                onSelectAssigneesError("At least 2 assignees must be selected if rotation is enabled")
                                return@selectable
                            }

                            onSelectAssigneesError(null)
                            onNewAssigneesSelected(newAssignees)
                        },
                        role = Role.Checkbox
                    )
                    .background(
                        if (selected) {
                            MaterialTheme.colorScheme.surfaceVariant
                        } else MaterialTheme.colorScheme.surface
                    )
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(15.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    UserAvatar(user.name)
                    Text(
                        user.name,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                if (selected) {
                    if (!rotationEnabled) {
                        Icon(
                            painter = painterResource(R.drawable.tick),
                            contentDescription = "Selected",
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.size(30.dp)
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .size(26.dp)
                                .background(
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    shape = CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = (selectedAssignees.indexOf(user) + 1).toString(),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SelectRepeatRadios(
    radioOptions: List<RadioOption>,
    selectedRepeatOption: RadioOption,
    onSelectedRepeatOptionChange: (RadioOption) -> Unit,
    disableRotation: () -> Unit,
) {
    Column(modifier = Modifier.selectableGroup()) {
        radioOptions.forEach { option ->
            Row(
                Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .selectable(
                        selected = (option == selectedRepeatOption),
                        onClick = {
                            onSelectedRepeatOptionChange(option)
                            if (option.repeatType == Repeat.NONE) {
                                disableRotation()
                            }
                        },
                        role = Role.RadioButton
                    ),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(
                    selected = (option == selectedRepeatOption),
                    onClick = null
                )
                Text(
                    text = option.displayText,
                    modifier = Modifier.padding(start = 16.dp),
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }
    }
}

@Composable
fun SelectDateField(
    selectedDate: Long?,
    onShowDatePickerModal: () -> Unit
) {
    OutlinedTextField(
        value = selectedDate?.let { convertMillisToDate(it) } ?: "",
        onValueChange = { },
        modifier = Modifier
            .fillMaxWidth()
            .pointerInput(selectedDate) {
                awaitEachGesture {
                    awaitFirstDown(pass = PointerEventPass.Initial)
                    val upEvent = waitForUpOrCancellation(pass = PointerEventPass.Initial)
                    if (upEvent != null) {
                        onShowDatePickerModal()
                    }
                }
            },
        placeholder = { Text("DD/MM/YYYY") },
        trailingIcon = {
            Icon(Icons.Default.DateRange, contentDescription = "Select date")
        },
        shape = RoundedCornerShape(20),
        colors = OutlinedTextFieldDefaults.colors(
            unfocusedContainerColor = MaterialTheme.colorScheme.surface,
            focusedContainerColor = MaterialTheme.colorScheme.surface
        )
    )
}


@Composable
fun TaskTitleTextField(
    title: String,
    titleChange: (String) -> Unit
) {
    OutlinedTextField(
        value = title,
        onValueChange = titleChange,
        singleLine = true,
        modifier = Modifier.fillMaxWidth(),
        placeholder = { Text("Task Title") },
        prefix = {
            Row() {
                Icon(
                    painter = painterResource(R.drawable.task_title),
                    contentDescription = "Task title",
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
            }
        },
        shape = RoundedCornerShape(20),
        colors = OutlinedTextFieldDefaults.colors(
            unfocusedContainerColor = MaterialTheme.colorScheme.surface,
            focusedContainerColor = MaterialTheme.colorScheme.surface
        )
    )
}

@Composable
fun DatePickerModal(
    onDateSelected: (Long?) -> Unit,
    onDismiss: () -> Unit
) {
    val datePickerState = rememberDatePickerState()

    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = {
                onDateSelected(datePickerState.selectedDateMillis)
                onDismiss()
            }) {
                Text("OK")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    ) {
        DatePicker(state = datePickerState)
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@SuppressLint("ViewModelConstructorInComposable")
@Preview(showBackground = true)
@Composable
fun CreateTaskScreenPreview() {
    HousifyTheme {
        CreateTaskScreen(
            onShowSnackbar = {},
            onBack = {}
        )
    }
}