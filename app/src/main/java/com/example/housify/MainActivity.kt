package com.example.housify

import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navDeepLink
import androidx.navigation.toRoute
import com.example.housify.data.local.datastore.ThemePreferenceManager
import com.example.housify.data.remote.dto.TodayTask
import com.example.housify.data.remote.dto.UncompletedRating
import com.example.housify.feature.auth.AuthScreen
import com.example.housify.feature.groups.tasks.create.CreateTaskScreen
import com.example.housify.feature.groups.group_entry.GroupEntryScreen
import com.example.housify.feature.groups.group_entry.GroupEntryViewModel
import com.example.housify.feature.groups.groups_list.GroupScreen
import com.example.housify.feature.groups.groups_list.GroupsViewModel
import com.example.housify.feature.groups.leaderboard.history_entry.LeaderboardHistoryEntryScreen
import com.example.housify.feature.groups.leaderboard.history_list.LeaderboardHistoryScreen
import com.example.housify.feature.groups.tasks.details.TaskDetailsScreen
import com.example.housify.feature.hostIp.UpdateHostIpScreen
import com.example.housify.feature.joinGroup.JoinGroupScreen
import com.example.housify.feature.profile.CompletedRatingsScreen
import com.example.housify.feature.profile.CompletedTasksScreen
import com.example.housify.feature.profile.ProfileScreen
import com.example.housify.feature.tasks.RatingToDoScreen
import com.example.housify.feature.tasks.TaskToDoScreen
import com.example.housify.feature.tasks.TasksScreen
import com.example.housify.ui.components.BottomNavBar
import com.example.housify.ui.components.TopNavBar
import com.example.housify.ui.theme.AppTheme
import com.example.housify.ui.theme.HousifyTheme
import com.example.housify.util.NetworkConnectivityObserver
import com.example.housify.util.NetworkStatus
import dagger.hilt.android.AndroidEntryPoint
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.String

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @Inject
    lateinit var themePreferenceManager: ThemePreferenceManager

    @OptIn(ExperimentalMaterial3Api::class)
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val networkConnectivityObserver = NetworkConnectivityObserver(applicationContext)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    android.Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(android.Manifest.permission.POST_NOTIFICATIONS),
                    1001
                )
            }
        }

        enableEdgeToEdge()
        setContent {
            val userTheme by themePreferenceManager.themeFlow.collectAsState(initial = AppTheme.SYSTEM)

            val darkTheme = when (userTheme) {
                AppTheme.LIGHT -> false
                AppTheme.DARK -> true
                AppTheme.SYSTEM -> isSystemInDarkTheme()
            }

            // Check if has user logged in
            val auth = FirebaseAuth.getInstance()
            val startDestination = if (auth.currentUser != null) Tasks else Auth

            HousifyTheme(
                darkTheme = darkTheme,
                dynamicColor = false
            ) {
                val navController = rememberNavController()
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination

                val bottomBarScreenRoutes =
                    bottomNavItems.map { it.route::class.qualifiedName }.toSet()

                val snackbarHostState = remember { SnackbarHostState() }
                val scope = rememberCoroutineScope()

                val networkStatus by networkConnectivityObserver.observe().collectAsState(
                    initial = NetworkStatus.Available
                )

                LaunchedEffect(networkStatus) {
                    if (networkStatus == NetworkStatus.Unavailable) {
                        scope.launch {
                            snackbarHostState.showSnackbar(
                                message = "⚠️ You're not connected to the network",
                                duration = SnackbarDuration.Indefinite // Keep it shown until network is back
                            )
                        }
                    } else {
                        // Dismiss any existing Snackbar when the network is back
                        snackbarHostState.currentSnackbarData?.dismiss()
                    }
                }

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    topBar = {
                        currentDestination?.route?.let { currentRoute ->
                            val topNavItem = topNavItems.firstOrNull { navItem ->
                                var navItemRouteName = navItem.route::class.java.name
                                if (navItemRouteName.endsWith("\$Companion")) {
                                    navItemRouteName = navItemRouteName.removeSuffix("\$Companion")
                                }
                                currentRoute.startsWith(navItemRouteName)
                            }

                            topNavItem?.let { item ->
                                TopNavBar(
                                    title = item.title,
                                    onBack = { navController.popBackStack() }
                                )
                            }
                        }
                    },
                    snackbarHost = {
                        SnackbarHost(snackbarHostState) { snackbarData ->
                            Snackbar(
                                snackbarData = snackbarData,
                                contentColor = MaterialTheme.colorScheme.onPrimary,
                                actionColor = MaterialTheme.colorScheme.background,
                            )
                        }
                    },
                    // only show bottom bar in certain screens
                    bottomBar = {
                        // 2. The visibility check remains similar
                        if (currentDestination?.route in bottomBarScreenRoutes) {
                            BottomNavBar(
                                items = bottomNavItems,
                                onSelected = { routeObject ->
                                    navController.navigate(routeObject) {
                                        launchSingleTop = true
                                    }
                                },
                                currentRoute = currentDestination?.route
                                    ?: bottomBarScreenRoutes.first()!!
                            )
                        }
                    }) { innerPadding ->
                    NavHost(
                        navController = navController,
                        startDestination = startDestination,
                        modifier = Modifier
                            .padding(innerPadding)
                            .padding(horizontal = 20.dp, vertical = 32.dp) // extra padding
                    ) {
                        composable<Auth> {
                            AuthScreen(
                                onSuccessLogin = { navController.navigate(Tasks) })
                        }

                        composable<Tasks> {
                            TasksScreen(
                                onTaskSelect = { task: TodayTask ->
                                    navController.navigate(
                                        TaskToDo(
                                            groupId = task.groupId,
                                            taskId = task.taskId,
                                            taskInstanceId = task.taskInstanceId,
                                            groupName = task.groupName,
                                            title = task.title,
                                            place = task.place,
                                            scheduleType = task.scheduleType
                                        )
                                    )
                                },
                                onRatingSelect = { rating: UncompletedRating ->
                                    navController.navigate(
                                        RatingToDo(
                                            reviewId = rating.reviewId,
                                            groupId = rating.groupId,
                                            groupName = rating.groupName,
                                            revieweeName = rating.revieweeName,
                                            title = rating.title,
                                            place = rating.place,
                                            reviewCreatedAt = rating.reviewCreatedAt
                                        )
                                    )
                                }
                            )
                        }

                        composable<TaskToDo> { backStackEntry ->
                            val arg = backStackEntry.toRoute<TaskToDo>()

                            // Rebuild the TodayTask object from the flattened args
                            val todayTask = TodayTask(
                                groupId = arg.groupId,
                                taskId = arg.taskId,
                                taskInstanceId = arg.taskInstanceId,
                                groupName = arg.groupName,
                                title = arg.title,
                                place = arg.place,
                                scheduleType = arg.scheduleType
                            )

                            TaskToDoScreen(
                                task = todayTask,
                                onBack = {
                                    Log.d("Nav pop", "can")
                                    navController.popBackStack() }
                            )
                        }

                        composable<RatingToDo> { backStackEntry ->
                            val arg = backStackEntry.toRoute<RatingToDo>()

                            // Rebuild the UncompletedRating object from the flattened args
                            val uncompletedRating = UncompletedRating(
                                reviewId = arg.reviewId,
                                groupId = arg.groupId,
                                groupName = arg.groupName,
                                revieweeName = arg.revieweeName,
                                title = arg.title,
                                place = arg.place,
                                reviewCreatedAt = arg.reviewCreatedAt
                            )

                            RatingToDoScreen(
                                rating = uncompletedRating,
                                onBack = { navController.popBackStack() })
                        }

                        composable<Groups> {
                            GroupScreen(
                                onGroupSelect = { groupId ->
                                    navController.navigate(
                                        GroupEntry(
                                            groupId
                                        )
                                    )
                                },
                                onShowSnackbar = ({ message ->
                                    onShowSnackbar(
                                        scope,
                                        snackbarHostState,
                                        message
                                    )
                                })
                            )
                        }

                        composable<GroupEntry> { backStackEntry ->
                            val groupsViewModel: GroupsViewModel =
                                hiltViewModel(
                                    navController.getBackStackEntry(Groups::class.qualifiedName!!)
                                )
                            val arg = backStackEntry.toRoute<GroupEntry>()

                            // Get the ViewModel for THIS screen
                            val groupEntryViewModel: GroupEntryViewModel = hiltViewModel()

                            // Check for the result from CreateTaskScreen
                            val taskCreatedResult =
                                backStackEntry.savedStateHandle.get<Boolean>("task_created")
                            val taskDeletedResult =
                                backStackEntry.savedStateHandle.get<Boolean>("task_deleted")

                            if (taskCreatedResult == true || taskDeletedResult == true) {
                                LaunchedEffect(Unit) {
                                    groupEntryViewModel.refreshData()
                                    backStackEntry.savedStateHandle.remove<Boolean>("task_created")
                                    backStackEntry.savedStateHandle.remove<Boolean>("task_deleted")
                                }
                            }
                            GroupEntryScreen(
                                viewModel = groupEntryViewModel,
                                onSelectLeaderboardHistory = {
                                    navController.navigate(
                                        LeaderboardHistory(arg.groupId)
                                    )
                                },
                                onSelectCreateTask = { navController.navigate(CreateTask(arg.groupId)) },
                                onSelectTaskDetails = {
                                    navController.navigate(
                                        TaskDetails(
                                            arg.groupId,
                                            it
                                        )
                                    )
                                },
                                onShowSnackbar = ({ message ->
                                    onShowSnackbar(
                                        scope,
                                        snackbarHostState,
                                        message
                                    )
                                }),
                                onBack = {
                                    navController.popBackStack()
                                    groupsViewModel.refreshData()
                                }
                            )
                        }

                        composable<LeaderboardHistory> { backStackEntry ->
                            val arg = backStackEntry.toRoute<GroupEntry>()
                            LeaderboardHistoryScreen(
                                onSelectLeaderboardHistoryEntry = {
                                    navController.navigate(
                                        LeaderboardHistoryEntry(arg.groupId, it)
                                    )
                                })
                        }

                        composable<LeaderboardHistoryEntry> { LeaderboardHistoryEntryScreen() }

                        composable<CreateTask> { backStackEntry ->
                            CreateTaskScreen(
                                onShowSnackbar = { message ->
                                    onShowSnackbar(
                                        scope,
                                        snackbarHostState,
                                        message
                                    )
                                },
                                onBack = {
                                    navController.previousBackStackEntry
                                        ?.savedStateHandle
                                        ?.set("task_created", true)
                                    navController.popBackStack()
                                }
                            )
                        }

                        composable<TaskDetails> {
                            TaskDetailsScreen(
                                onBack = {
                                    navController.previousBackStackEntry
                                        ?.savedStateHandle
                                        ?.set("task_deleted", true)
                                    navController.popBackStack()
                                },
                                onShowSnackbar = { message ->
                                    onShowSnackbar(
                                        scope,
                                        snackbarHostState,
                                        message
                                    )
                                }
                            )
                        }

                        composable<Profile> {
                            ProfileScreen(
                                onSelectCompletedTasks = { navController.navigate(CompletedTasks) },
                                onSelectCompletedRatings = { navController.navigate(CompletedRatings) },
                                onSuccessLogout = { navController.navigate(Auth) },
                                currentTheme = userTheme,
                                onSwitchTheme = { newTheme ->
                                    scope.launch {
                                        themePreferenceManager.saveTheme(newTheme)
                                    }
                                },
                                onEnterUpdateIpScreen = { navController.navigate(UpdateHostIp) }
                            )
                        }

                        composable<CompletedTasks> {
                            CompletedTasksScreen(
                                onBack = { navController.popBackStack() })
                        }

                        composable<CompletedRatings> {
                            CompletedRatingsScreen(
                                onBack = { navController.popBackStack() })
                        }

                        composable<JoinGroup>(
                            deepLinks = listOf(
                                navDeepLink {
                                    uriPattern = joinGroupURI
                                })
                        ) {
                            JoinGroupScreen(
                                onShowSnackbar = { message ->
                                    onShowSnackbar(
                                        scope,
                                        snackbarHostState,
                                        message
                                    )
                                },
                                onBack = {
                                    navController.navigate(Groups)
                                }
                            )
                        }

                        composable<UpdateHostIp> {
                            UpdateHostIpScreen(
                                onShowSnackbar = { message ->
                                    onShowSnackbar(
                                        scope,
                                        snackbarHostState,
                                        message
                                    )
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

fun onShowSnackbar(
    scope: CoroutineScope,
    snackbarHostState: SnackbarHostState,
    message: String
) =
    scope.launch {
        snackbarHostState.showSnackbar(message)
    }


