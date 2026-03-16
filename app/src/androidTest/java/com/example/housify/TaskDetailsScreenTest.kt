package com.example.housify

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.lifecycle.SavedStateHandle
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.housify.domain.model.PredefinedSpace
import com.example.housify.domain.model.Repeat
import com.example.housify.domain.model.Space
import com.example.housify.domain.model.Task
import com.example.housify.domain.model.TaskDetails
import com.example.housify.domain.model.TaskSchedule
import com.example.housify.domain.model.User
import com.example.housify.domain.repository_interfaces.TaskRepository
import com.example.housify.feature.groups.tasks.details.TaskDetailsScreen
import com.example.housify.feature.groups.tasks.details.TaskDetailsViewModel
import com.example.housify.ui.theme.HousifyTheme
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class TaskDetailsScreenTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    private lateinit var taskRepository: TaskRepository
    private lateinit var viewModel: TaskDetailsViewModel

    @Before
    fun setup() {
        taskRepository = mockk()

        val groupId = "group123"
        val taskId = "task123"

        val assignees = listOf(
            User(id = "u1", name = "Alice", isCurrentUser = false),
            User(id = "u2", name = "Bob", isCurrentUser = true),
        )

        val task = Task(
            id = taskId,
            groupId = groupId,
            title = "Clean bathroom",
            startDate = "2025/01/01",
            repeat = Repeat.WEEKLY,
            rotationEnabled = true,
            space = PredefinedSpace.Bathroom,
            assignees = assignees,
            assignedToCurrentUser = true
        )

        val schedules = listOf(
            TaskSchedule(
                date = "2025/01/08",
                user = assignees[0]
            ),
            TaskSchedule(
                date = "2025/01/15",
                user = assignees[1]
            )
        )

        val fakeDetails = TaskDetails(
            task = task,
            recentSchedule = schedules,
            isUserAdmin = true
        )

        coEvery {
            taskRepository.getTaskDetails(taskId, groupId)
        } returns flowOf(Resource.Success(fakeDetails))

        coEvery {
            taskRepository.deleteTask(taskId, groupId)
        } returns flowOf(Resource.Success(Unit))

        val savedStateHandle = SavedStateHandle(
            mapOf(
                "groupId" to groupId,
                "taskId" to taskId
            )
        )

        viewModel = TaskDetailsViewModel(
            taskRepository = taskRepository,
            savedStateHandle = savedStateHandle
        )
    }

    @Test
    fun clickingDelete_showsConfirmationDialog() {
        // Act
        composeRule.setContent {
            HousifyTheme {
                TaskDetailsScreen(
                    viewModel = viewModel,
                    onShowSnackbar = {},
                    onBack = {}
                )
            }
        }

        // Assert
        composeRule.onNodeWithText("Clean bathroom").assertIsDisplayed()

        // Act
        composeRule
            .onNodeWithContentDescription("Delete task")
            .performClick()

        // Assert
        composeRule
            .onNodeWithText("Delete Task")
            .assertIsDisplayed()

        composeRule
            .onNodeWithText("Are you sure you want to delete this task?")
            .assertIsDisplayed()
    }
}
