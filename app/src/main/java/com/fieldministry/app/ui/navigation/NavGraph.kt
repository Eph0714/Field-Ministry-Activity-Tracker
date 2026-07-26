package com.fieldministry.app.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.navigation.NavType
import com.fieldministry.app.data.session.SessionManager
import com.fieldministry.app.ui.admin.AdminHomeScreen
import com.fieldministry.app.ui.admin.ManageBarangaysScreen
import com.fieldministry.app.ui.admin.ManageMunicipalitiesScreen
import com.fieldministry.app.ui.admin.ManagePhBarangaysScreen
import com.fieldministry.app.ui.admin.ManagePhMunicipalitiesScreen
import com.fieldministry.app.ui.admin.ManagePhProvincesScreen
import com.fieldministry.app.ui.admin.ManagePhRegionsScreen
import com.fieldministry.app.ui.admin.ManageUsersScreen
import com.fieldministry.app.ui.admin.PendingApprovalsScreen
import com.fieldministry.app.ui.biblestudy.BibleStudyScreen
import com.fieldministry.app.ui.dashboard.DashboardScreen
import com.fieldministry.app.ui.householder.HouseholderListScreen
import com.fieldministry.app.ui.householder.HouseholderProfileScreen
import com.fieldministry.app.ui.login.LoginScreen
import com.fieldministry.app.ui.login.SignUpScreen
import com.fieldministry.app.ui.reports.ReportsScreen
import com.fieldministry.app.ui.returnvisit.ReturnVisitScreen
import com.fieldministry.app.ui.searching.SearchingScreen

@Composable
fun AppNavGraph(sessionManager: SessionManager) {
    val navController: NavHostController = rememberNavController()
    val startDestination = if (sessionManager.isLoggedIn()) Routes.DASHBOARD else Routes.LOGIN

    NavHost(navController = navController, startDestination = startDestination) {
        composable(Routes.LOGIN) {
            LoginScreen(
                onLoggedIn = {
                    navController.navigate(Routes.DASHBOARD) {
                        popUpTo(Routes.LOGIN) { inclusive = true }
                    }
                },
                onSignUp = { navController.navigate(Routes.SIGNUP) },
            )
        }

        composable(Routes.SIGNUP) {
            SignUpScreen(
                onBack = { navController.popBackStack() },
                onSubmitted = { navController.popBackStack() },
            )
        }

        composable(Routes.DASHBOARD) {
            DashboardScreen(
                onLoggedOut = {
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(Routes.DASHBOARD) { inclusive = true }
                    }
                },
                onOpenHouseholders = { navController.navigate(Routes.HOUSEHOLDER_LIST) },
                onLogSearching = { navController.navigate(Routes.SEARCHING_NEW) },
                onLogBibleStudy = { navController.navigate(Routes.BIBLE_STUDY_NEW) },
                onLogReturnVisit = { navController.navigate(Routes.RETURN_VISIT_NEW) },
                onOpenAdmin = { navController.navigate(Routes.ADMIN_HOME) },
                onOpenReports = { navController.navigate(Routes.REPORTS) },
            )
        }

        composable(Routes.HOUSEHOLDER_LIST) {
            HouseholderListScreen(
                onBack = { navController.popBackStack() },
                onOpenHouseholder = { uuid -> navController.navigate(Routes.householderProfile(uuid)) },
                onAddNew = { navController.navigate(Routes.SEARCHING_NEW) },
            )
        }

        composable(
            route = Routes.HOUSEHOLDER_PROFILE_PATTERN,
            arguments = listOf(navArgument("uuid") { type = NavType.StringType }),
        ) { backStackEntry ->
            val uuid = Routes.decode(backStackEntry.arguments?.getString("uuid") ?: "")
            HouseholderProfileScreen(uuid = uuid, onBack = { navController.popBackStack() })
        }

        composable(Routes.SEARCHING_NEW) {
            SearchingScreen(
                onBack = { navController.popBackStack() },
                onSaved = { navController.popBackStack() },
            )
        }

        composable(Routes.BIBLE_STUDY_NEW) {
            BibleStudyScreen(
                onBack = { navController.popBackStack() },
                onSaved = { navController.popBackStack() },
            )
        }

        composable(Routes.RETURN_VISIT_NEW) {
            ReturnVisitScreen(
                onBack = { navController.popBackStack() },
                onSaved = { navController.popBackStack() },
            )
        }

        composable(Routes.ADMIN_HOME) {
            AdminHomeScreen(
                onBack = { navController.popBackStack() },
                onPendingApprovals = { navController.navigate(Routes.ADMIN_PENDING_APPROVALS) },
                onManageUsers = { navController.navigate(Routes.ADMIN_MANAGE_USERS) },
                onManageMunicipalities = { navController.navigate(Routes.ADMIN_MANAGE_MUNICIPALITIES) },
                onManageBarangays = { navController.navigate(Routes.ADMIN_MANAGE_BARANGAYS) },
                onManagePhRegions = { navController.navigate(Routes.ADMIN_MANAGE_PH_REGIONS) },
                onManagePhProvinces = { navController.navigate(Routes.ADMIN_MANAGE_PH_PROVINCES) },
                onManagePhMunicipalities = { navController.navigate(Routes.ADMIN_MANAGE_PH_MUNICIPALITIES) },
                onManagePhBarangays = { navController.navigate(Routes.ADMIN_MANAGE_PH_BARANGAYS) },
            )
        }

        composable(Routes.ADMIN_PENDING_APPROVALS) {
            PendingApprovalsScreen(onBack = { navController.popBackStack() })
        }

        composable(Routes.ADMIN_MANAGE_USERS) {
            ManageUsersScreen(onBack = { navController.popBackStack() })
        }

        composable(Routes.ADMIN_MANAGE_MUNICIPALITIES) {
            ManageMunicipalitiesScreen(onBack = { navController.popBackStack() })
        }

        composable(Routes.ADMIN_MANAGE_BARANGAYS) {
            ManageBarangaysScreen(onBack = { navController.popBackStack() })
        }

        composable(Routes.ADMIN_MANAGE_PH_REGIONS) {
            ManagePhRegionsScreen(onBack = { navController.popBackStack() })
        }

        composable(Routes.ADMIN_MANAGE_PH_PROVINCES) {
            ManagePhProvincesScreen(onBack = { navController.popBackStack() })
        }

        composable(Routes.ADMIN_MANAGE_PH_MUNICIPALITIES) {
            ManagePhMunicipalitiesScreen(onBack = { navController.popBackStack() })
        }

        composable(Routes.ADMIN_MANAGE_PH_BARANGAYS) {
            ManagePhBarangaysScreen(onBack = { navController.popBackStack() })
        }

        composable(Routes.REPORTS) {
            ReportsScreen(onBack = { navController.popBackStack() })
        }
    }
}
