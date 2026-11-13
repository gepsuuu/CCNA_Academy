package com.ccnaacademy.app.ui.nav

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.ccnaacademy.app.AppViewModel
import com.ccnaacademy.app.data.Curriculum
import com.ccnaacademy.app.data.MemoryLibrary
import com.ccnaacademy.app.data.QuestionBank
import com.ccnaacademy.app.data.BankFilter
import com.ccnaacademy.app.data.StudyContent
import com.ccnaacademy.app.ui.components.DiagramBookmarks
import com.ccnaacademy.app.ui.components.LocalDiagramBookmarks
import com.ccnaacademy.app.data.ProgressState
import com.ccnaacademy.app.ui.screens.*
import com.ccnaacademy.app.ui.lab.*

data class TopDest(val route: String, val label: String, val icon: ImageVector)
val TOP = listOf(
    TopDest("dashboard", "Home", Icons.Filled.Dashboard),
    TopDest("learn", "Learn", Icons.AutoMirrored.Filled.MenuBook),
    TopDest("revise", "Revise", Icons.Filled.Psychology),
    TopDest("practice", "Practice", Icons.Filled.Quiz),
    TopDest("progress", "Progress", Icons.Filled.Insights),
)

val LocalAppScope = compositionLocalOf<AppScope> { error("AppScope not provided") }

/** Everything a screen needs: content, progress, view model and navigation. */
class AppScope(val c: Curriculum, val p: ProgressState, val vm: AppViewModel, val nav: NavHostController, val wide: Boolean, val lib: MemoryLibrary, val bank: QuestionBank, val study: StudyContent) {
    fun go(route: String) = nav.navigate(route)
    fun lesson(id: String) = nav.navigate("lesson/$id")
    fun domain(id: String) = nav.navigate("domain/$id")
    fun top(route: String) = nav.navigate(route) {
        popUpTo(nav.graph.findStartDestination().id) { saveState = true }
        launchSingleTop = true; restoreState = true
    }
}

@Composable
fun AppRoot(vm: AppViewModel) {
    val curriculum by vm.curriculum.collectAsStateWithLifecycle()
    val progress by vm.progress.collectAsStateWithLifecycle()
    val c = curriculum
    val lib = vm.library
    val bankRaw by vm.bank.collectAsStateWithLifecycle()
    val loadError by vm.loadError.collectAsStateWithLifecycle()
    val study = vm.study
    val ready by vm.progressReady.collectAsStateWithLifecycle()
    if (loadError != null) {
        Box(Modifier.fillMaxSize().padding(32.dp), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Filled.ErrorOutline, null, Modifier.size(48.dp), tint = MaterialTheme.colorScheme.error)
                Text(loadError ?: "", textAlign = androidx.compose.ui.text.style.TextAlign.Center, modifier = Modifier.padding(top = 12.dp))
            }
        }
        return
    }
    if (c == null || lib == null || bankRaw == null || study == null || !ready) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
        return
    }
    val nav = rememberNavController()
    val wide = LocalConfiguration.current.screenWidthDp >= 600
    val bank = remember(bankRaw, progress.disabledPacks) { bankRaw!!.withDisabled(progress.disabledPacks) }
    val scope = AppScope(c, progress, vm, nav, wide, lib, bank, study)
    val backStack by nav.currentBackStackEntryAsState()
    val route = backStack?.destination?.route ?: "dashboard"
    val showNav = TOP.any { it.route == route }

    CompositionLocalProvider(LocalAppScope provides scope,
        LocalDiagramBookmarks provides DiagramBookmarks(progress.diagramBookmarks) { vm.toggleDiagramBookmark(it) }) {
    Row(Modifier.fillMaxSize().windowInsetsPadding(WindowInsets.displayCutout.union(WindowInsets.navigationBars).only(WindowInsetsSides.Horizontal))) {
        if (wide && showNav) {
            NavigationRail(Modifier.fillMaxHeight()) {
                Spacer(Modifier.height(12.dp))
                TOP.forEach { d ->
                    NavigationRailItem(selected = route == d.route, onClick = { scope.top(d.route) },
                        icon = { Icon(d.icon, d.label) }, label = { Text(d.label) })
                }
            }
        }
        Scaffold(
            modifier = Modifier.weight(1f),
            bottomBar = {
                if (!wide && showNav) NavigationBar {
                    TOP.forEach { d ->
                        NavigationBarItem(selected = route == d.route, onClick = { scope.top(d.route) },
                            icon = { Icon(d.icon, d.label) }, label = { Text(d.label) })
                    }
                }
            },
            contentWindowInsets = WindowInsets(0)
        ) { pad ->
            NavHost(nav, startDestination = "dashboard", modifier = Modifier.padding(pad),
                enterTransition = { androidx.compose.animation.fadeIn(androidx.compose.animation.core.tween(180)) },
                exitTransition = { androidx.compose.animation.fadeOut(androidx.compose.animation.core.tween(120)) }) {
                composable("dashboard") { DashboardScreen(LocalAppScope.current) }
                composable("learn") { LearnScreen(LocalAppScope.current) }
                composable("domain/{id}") { DomainScreen(LocalAppScope.current, it.arguments?.getString("id") ?: "d1") }
                composable("lesson/{id}") { LessonScreen(LocalAppScope.current, it.arguments?.getString("id") ?: "") }
                composable("practice") { BankHomeScreen(LocalAppScope.current) }
                composable(
                    "bank?mode={mode}&count={count}&minutes={minutes}&domain={domain}&topic={topic}&diff={diff}&format={format}&pack={pack}",
                    arguments = listOf(
                        navArgument("mode") { type = NavType.StringType; defaultValue = "quick" },
                        navArgument("count") { type = NavType.IntType; defaultValue = 10 },
                        navArgument("minutes") { type = NavType.IntType; defaultValue = 0 },
                        navArgument("domain") { type = NavType.StringType; defaultValue = "all" },
                        navArgument("topic") { type = NavType.StringType; defaultValue = "all" },
                        navArgument("diff") { type = NavType.IntType; defaultValue = 0 },
                        navArgument("format") { type = NavType.StringType; defaultValue = "all" },
                        navArgument("pack") { type = NavType.StringType; defaultValue = "all" },
                    )
                ) {
                    val a = it.arguments
                    BankSessionScreen(LocalAppScope.current, a?.getString("mode") ?: "quick", a?.getInt("count") ?: 10, a?.getInt("minutes") ?: 0,
                        BankFilter(a?.getString("domain") ?: "all", a?.getString("topic") ?: "all", a?.getInt("diff") ?: 0,
                            a?.getString("format") ?: "all", a?.getString("pack") ?: "all"))
                }
                composable("examsetup") { ExamSetupScreen(LocalAppScope.current) }
                composable("exam?count={count}&minutes={minutes}&level={level}", arguments = listOf(
                    navArgument("count") { type = NavType.IntType; defaultValue = 60 },
                    navArgument("minutes") { type = NavType.IntType; defaultValue = 72 },
                    navArgument("level") { type = NavType.IntType; defaultValue = 1 })) {
                    val a = it.arguments
                    ExamRunScreen(LocalAppScope.current, a?.getInt("count") ?: 60, a?.getInt("minutes") ?: 72, a?.getInt("level") ?: 1)
                }
                composable("packs") { PackManagerScreen(LocalAppScope.current) }
                composable("question/{id}") { SingleQuestionScreen(LocalAppScope.current, it.arguments?.getString("id") ?: "") }
                composable("planner") { PlannerScreen(LocalAppScope.current) }
                composable("plannersetup") { PlannerSetupScreen(LocalAppScope.current) }
                composable("tutor") { TutorScreen(LocalAppScope.current) }
                composable("comparisons") { ComparisonListScreen(LocalAppScope.current) }
                composable("compare/{id}") { ComparisonScreen(LocalAppScope.current, it.arguments?.getString("id") ?: "") }
                composable("mistakes?focus={focus}", arguments = listOf(navArgument("focus") { type = NavType.StringType; defaultValue = "" })) {
                    MistakesScreen(LocalAppScope.current, it.arguments?.getString("focus") ?: "")
                }
                composable("glossary?term={term}", arguments = listOf(navArgument("term") { type = NavType.StringType; defaultValue = "" })) {
                    GlossaryScreen(LocalAppScope.current, it.arguments?.getString("term") ?: "")
                }
                composable("daily") { DailyChallengeScreen(LocalAppScope.current) }
                composable("library") { LibraryScreen(LocalAppScope.current) }
                composable("note/{id}") { NoteEditorScreen(LocalAppScope.current, it.arguments?.getString("id") ?: "new", "") }
                composable("newnote?lesson={lesson}", arguments = listOf(navArgument("lesson") { type = NavType.StringType; defaultValue = "" })) {
                    NoteEditorScreen(LocalAppScope.current, "new", it.arguments?.getString("lesson") ?: "")
                }
                composable("search") { SearchScreen(LocalAppScope.current) }
                composable("achievements") { AchievementsScreen(LocalAppScope.current) }
                composable("bankstats") { BankAnalyticsScreen(LocalAppScope.current) }
                composable("revise") { ReviseHubScreen(LocalAppScope.current) }
                composable("plan") { DailyPlanScreen(LocalAppScope.current) }
                composable("flashcards") { FlashcardsHomeScreen(LocalAppScope.current) }
                composable("cardsession?mode={mode}&arg={arg}", arguments = listOf(
                    navArgument("mode") { type = NavType.StringType; defaultValue = "due" },
                    navArgument("arg") { type = NavType.StringType; defaultValue = "" })) {
                    CardSessionScreen(LocalAppScope.current, it.arguments?.getString("mode") ?: "due", it.arguments?.getString("arg") ?: "")
                }
                composable("cardbrowser?deck={deck}&q={q}", arguments = listOf(navArgument("deck") { type = NavType.StringType; defaultValue = "" },
                    navArgument("q") { type = NavType.StringType; defaultValue = "" })) {
                    CardBrowserScreen(LocalAppScope.current, it.arguments?.getString("deck") ?: "", it.arguments?.getString("q") ?: "")
                }
                composable("memory") { MemoryCenterScreen(LocalAppScope.current) }
                composable("guide/{id}") { GuideScreen(LocalAppScope.current, it.arguments?.getString("id") ?: "") }
                composable("challenge") { MemoryChallengeScreen(LocalAppScope.current) }
                composable("sheets") { CheatsheetListScreen(LocalAppScope.current) }
                composable("sheet/{id}") { CheatsheetScreen(LocalAppScope.current, it.arguments?.getString("id") ?: "") }
                composable("speed") { SpeedListScreen(LocalAppScope.current) }
                composable("technique/{id}") { TechniqueScreen(LocalAppScope.current, it.arguments?.getString("id") ?: "") }
                composable("subnet") { SubnetCenterScreen(LocalAppScope.current) }
                composable("subnetlesson/{id}") { SubnetLessonScreen(LocalAppScope.current, it.arguments?.getString("id") ?: "") }
                composable("drill?kind={kind}&ref={ref}&level={level}&types={types}&count={count}&limit={limit}&typed={typed}", arguments = listOf(
                    navArgument("kind") { type = NavType.StringType; defaultValue = "subnet" },
                    navArgument("ref") { type = NavType.StringType; defaultValue = "" },
                    navArgument("level") { type = NavType.IntType; defaultValue = 1 },
                    navArgument("types") { type = NavType.StringType; defaultValue = "" },
                    navArgument("count") { type = NavType.IntType; defaultValue = 10 },
                    navArgument("limit") { type = NavType.IntType; defaultValue = 0 },
                    navArgument("typed") { type = NavType.BoolType; defaultValue = false })) {
                    val a = it.arguments
                    DrillScreen(LocalAppScope.current, DrillConfig(
                        kind = a?.getString("kind") ?: "subnet", ref = a?.getString("ref") ?: "", level = a?.getInt("level") ?: 1,
                        types = (a?.getString("types") ?: "").split(",").filter { t -> t.isNotBlank() }, count = a?.getInt("count") ?: 10,
                        limitSec = a?.getInt("limit") ?: 0, typed = a?.getBoolean("typed") ?: false))
                }
                composable("labs") { LabsHubScreen(LocalAppScope.current) }
                composable("lab/{id}") { LabScreen(LocalAppScope.current, it.arguments?.getString("id") ?: "") }
                composable("labref") { SupportedCommandsScreen(LocalAppScope.current) }
                composable("labnotebook") { LabNotebookScreen(LocalAppScope.current) }
                composable("progress") { ProgressScreen(LocalAppScope.current) }
                composable("settings") { SettingsScreen(LocalAppScope.current) }
            }
        }
    }
    }
}

/** Shared top bar used by all screens. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppTopBar(title: String, scope: AppScope, back: Boolean = false, actions: @Composable RowScope.() -> Unit = {}) {
    TopAppBar(
        title = { Text(title, maxLines = 1) },
        navigationIcon = {
            if (back) IconButton(onClick = { scope.nav.popBackStack() }) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
            }
        },
        actions = {
            actions()
            if (!back) {
                IconButton(onClick = { scope.nav.navigate("search") }) { Icon(Icons.Filled.Search, "Search everything") }
                IconButton(onClick = { scope.nav.navigate("library") }) { Icon(Icons.Filled.BookmarkBorder, "Notes and bookmarks") }
                IconButton(onClick = { scope.nav.navigate("settings") }) { Icon(Icons.Filled.Settings, "Settings") }
            }
        }
    )
}
