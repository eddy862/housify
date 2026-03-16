package com.example.housify

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.lifecycle.SavedStateHandle
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.housify.domain.repository_interfaces.GroupRepository
import com.example.housify.domain.repository_interfaces.TaskRepository
import com.example.housify.feature.groups.tasks.create.CreateTaskScreen
import com.example.housify.feature.groups.tasks.create.CreateTaskViewModel
import com.example.housify.ui.theme.HousifyTheme
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CreateTaskScreenTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    private lateinit var taskRepository: TaskRepository
    private lateinit var groupRepository: GroupRepository
    private lateinit var viewModel: CreateTaskViewModel

    @Before
    fun setup() {
        taskRepository = mockk()
        groupRepository = mockk()

        coEvery { groupRepository.getGroupAllUsers(any()) } returns
                flowOf(Resource.Success(emptyList()))

        coEvery {
            taskRepository.createTask(
                groupId = any(),
                title = any(),
                place = any(),
                assigneeIds = any(),
                startDate = any(),
                isRotational = any(),
                repeat = any()
            )
        } returns flowOf(Resource.Success(Unit))

        val savedStateHandle = SavedStateHandle(
            mapOf("groupId" to "group123")
        )

        viewModel = CreateTaskViewModel(
            taskRepository = taskRepository,
            groupRepository = groupRepository,
            savedStateHandle = savedStateHandle
        )
    }

    @Test
    fun createTaskScreen_showsBasicSections() {
        // Arrange
        composeRule.setContent {
            HousifyTheme {
                CreateTaskScreen(
                    onShowSnackbar = {},
                    onBack = {},
                    viewModel = viewModel
                )
            }
        }

        // Assert
        composeRule.onNodeWithText("Task Title").assertIsDisplayed()
        composeRule.onNodeWithText("Start from").assertIsDisplayed()
        composeRule.onNodeWithText("Assign to").assertIsDisplayed()
        composeRule.onNodeWithText("Space").assertIsDisplayed()
        composeRule.onNodeWithText("CREATE").assertIsDisplayed()
    }

    @Test
    fun clickingCreate_withEmptyTitle_showsErrorFromViewModel() {
        // Arrange
        composeRule.setContent {
            HousifyTheme {
                CreateTaskScreen(
                    onShowSnackbar = {},
                    onBack = {},
                    viewModel = viewModel
                )
            }
        }

        // Act
        composeRule.onNodeWithText("CREATE").performClick()

        // Assert
        composeRule
            .onNodeWithText("Task title cannot be empty.")
            .assertIsDisplayed()
    }
}
