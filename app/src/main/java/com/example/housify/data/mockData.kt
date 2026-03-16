package com.example.housify.data

import android.os.Build
import androidx.annotation.RequiresApi
import com.example.housify.domain.model.Group
import com.example.housify.domain.model.Leaderboard
import com.example.housify.domain.model.LeaderboardEntry
import com.example.housify.domain.model.PredefinedSpace
import com.example.housify.domain.model.Repeat
import com.example.housify.domain.model.Task
import com.example.housify.domain.model.User
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

val mockUsers = listOf<User>(
    User("123", "user1"),
    User("456", "user2"),
    User("789", "user3"),
    User("101", "user4"),
    User("112", "user5"),
    User("131", "user6"),
    User("142", "user7"),
    User("153", "user8"),
    User("164", "user9"),
    User("175", "user10"),
)

val currentUser = mockUsers[0]

@RequiresApi(Build.VERSION_CODES.O)
val mockGroups = listOf<Group>(
    Group(
        "123",
        "group1",
        1,
        mockUsers[0],
        mockUsers.subList(1, 4),
        LocalDate.now().atStartOfDay().toInstant(ZoneOffset.UTC).toEpochMilli(),
        "1234567890",
        true
    ),
    Group(
        "456",
        "group2",
        2,
        mockUsers[0],
        mockUsers.subList(3, 6),
        LocalDate.now().atStartOfDay().toInstant(ZoneOffset.UTC).toEpochMilli(),
        "1234567890",
        true
    ),
    Group(
        "789",
        "group3",
        0,
        mockUsers[8],
        mockUsers.subList(0, 8),
        LocalDate.now().atStartOfDay().toInstant(ZoneOffset.UTC).toEpochMilli(),
        "1234567890",
        false
    ),
    Group(
        "111",
        "group4",
        4,
        mockUsers[0],
        mockUsers.subList(2, 6),
        LocalDate.now().atStartOfDay().toInstant(ZoneOffset.UTC).toEpochMilli(),
        "1234567890",
        true
    ),
    Group(
        "222",
        "group5",
        4,
        mockUsers[0],
        mockUsers.subList(2, 6),
        LocalDate.now().atStartOfDay().toInstant(ZoneOffset.UTC).toEpochMilli(),
        "1234567890",
        true
    ),
    Group(
        "333",
        "group6",
        4,
        mockUsers[0],
        mockUsers.subList(2, 6),
        LocalDate.now().atStartOfDay().toInstant(ZoneOffset.UTC).toEpochMilli(),
        "1234567890",
        true
    ),
    Group(
        "444",
        "group7",
        4,
        mockUsers[0],
        mockUsers.subList(2, 6),
        LocalDate.now().atStartOfDay().toInstant(ZoneOffset.UTC).toEpochMilli(),
        "1234567890",
        true
    ),
    Group(
        "555",
        "group8",
        4,
        mockUsers[0],
        mockUsers.subList(2, 6),
        LocalDate.now().atStartOfDay().toInstant(ZoneOffset.UTC).toEpochMilli(),
        "1234567890",
        true
    ),
)

@RequiresApi(Build.VERSION_CODES.O)
val mockLeaderboards = listOf<Leaderboard>(
    Leaderboard(
        "123",
        "123",
        LocalDate.now().minusDays(7).format(DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT)),
        LocalDate.now().format(DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT)),
        listOf(
            LeaderboardEntry(mockUsers[1], 1F),
            LeaderboardEntry(mockUsers[2], 2F),
            LeaderboardEntry(mockUsers[3], 3F),
            LeaderboardEntry(mockUsers[4], 4F),
            LeaderboardEntry(mockUsers[5], 5F),
        ),
        1
    ),
    Leaderboard(
        "456",
        "123",
        LocalDate.now().minusDays(15)
            .format(DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT)),
        LocalDate.now().minusDays(8).format(DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT)),
        listOf(
            LeaderboardEntry(mockUsers[3], 1F),
            LeaderboardEntry(mockUsers[4], 2F),
            LeaderboardEntry(mockUsers[5], 3F),
        ),
        2
    ),
    Leaderboard(
        "789",
        "123",
        LocalDate.now().minusDays(23)
            .format(DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT)),
        LocalDate.now().minusDays(16)
            .format(DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT)),
        listOf(
            LeaderboardEntry(mockUsers[5], 1F),
            LeaderboardEntry(mockUsers[6], 2F),
            LeaderboardEntry(mockUsers[7], 3F),
        ),
        3
    ),
)

@RequiresApi(Build.VERSION_CODES.O)
val mockTasks = listOf<Task>(
    Task(
        "123",
        "123",
        "task1",
        LocalDate.now().format(DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT)),
        Repeat.NONE,
        false,
        PredefinedSpace.Bathroom,
        listOf(mockUsers[1], mockUsers[2], mockUsers[0]),
        false
    ), Task(
        "456",
        "123",
        "task2",
        LocalDate.now().format(DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT)),
        Repeat.DAILY,
        true,
        PredefinedSpace.LivingRoom,
        listOf(mockUsers[0], mockUsers[2], mockUsers[3]),
        true
    ), Task(
        "789",
        "123",
        "task3",
        LocalDate.now().format(DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT)),
        Repeat.DAILY,
        true,
        PredefinedSpace.Bathroom,
        listOf(mockUsers[0], mockUsers[1], mockUsers[2], mockUsers[3]),
        true
    )
)
