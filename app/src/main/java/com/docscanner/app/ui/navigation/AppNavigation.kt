package com.docscanner.app.ui.navigation

import android.graphics.Bitmap
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.docscanner.app.data.DocumentStore
import com.docscanner.app.ui.screens.CropScreen
import com.docscanner.app.ui.screens.DocumentDetailScreen
import com.docscanner.app.ui.screens.ExtractedTextScreen
import com.docscanner.app.ui.screens.FolderDetailScreen
import com.docscanner.app.ui.screens.HomeScreen
import com.docscanner.app.ui.screens.LibraryScreen
import com.docscanner.app.ui.screens.OnboardingScreen
import com.docscanner.app.ui.screens.ScanSessionScreen
import com.docscanner.app.ui.screens.SplashScreen
import com.docscanner.app.viewmodel.ScanSessionViewModel

object AppRoutes {
    const val SPLASH = "splash"
    const val ONBOARDING = "onboarding"
    const val HOME = "home"
    const val CROP = "crop"
    const val SESSION = "session"
    const val LIBRARY = "library"
    const val DOCUMENT_DETAIL = "document/{id}"
    const val FOLDER_DETAIL = "folder/{id}"
    const val EXTRACTED_TEXT = "extracted_text"

    fun documentDetail(id: String) = "document/$id"
    fun folderDetail(id: String) = "folder/$id"
}

@Composable
fun AppNavHost(
    navController: NavHostController,
    documentStore: DocumentStore,
    onboardingComplete: Boolean,
    onOnboardingComplete: () -> Unit
) {
    var sessionViewModel by remember { mutableStateOf<ScanSessionViewModel?>(null) }
    var cropBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var ocrBitmaps by remember { mutableStateOf<List<Bitmap>>(emptyList()) }

    NavHost(
        navController = navController,
        startDestination = AppRoutes.SPLASH
    ) {
        composable(AppRoutes.SPLASH) {
            SplashScreen(
                onFinished = {
                    val dest = if (onboardingComplete) AppRoutes.HOME else AppRoutes.ONBOARDING
                    navController.navigate(dest) {
                        popUpTo(AppRoutes.SPLASH) { inclusive = true }
                    }
                }
            )
        }

        composable(AppRoutes.ONBOARDING) {
            OnboardingScreen(
                onComplete = {
                    onOnboardingComplete()
                    navController.navigate(AppRoutes.HOME) {
                        popUpTo(AppRoutes.ONBOARDING) { inclusive = true }
                    }
                }
            )
        }

        composable(AppRoutes.HOME) {
            HomeScreen(
                onStartSession = { bitmaps ->
                    val vm = ScanSessionViewModel()
                    vm.addPages(bitmaps)
                    sessionViewModel = vm
                    navController.navigate(AppRoutes.SESSION)
                },
                onCropImage = { bitmap ->
                    cropBitmap = bitmap
                    navController.navigate(AppRoutes.CROP)
                },
                onNavigateToLibrary = {
                    navController.navigate(AppRoutes.LIBRARY)
                }
            )
        }

        composable(AppRoutes.CROP) {
            cropBitmap?.let { bitmap ->
                CropScreen(
                    bitmap = bitmap,
                    onFinish = { cropped ->
                        val vm = sessionViewModel ?: ScanSessionViewModel()
                        vm.addPages(listOf(cropped))
                        sessionViewModel = vm
                        navController.navigate(AppRoutes.SESSION) {
                            popUpTo(AppRoutes.HOME)
                        }
                    },
                    onBack = { navController.popBackStack() }
                )
            }
        }

        composable(AppRoutes.SESSION) {
            sessionViewModel?.let { vm ->
                ScanSessionScreen(
                    viewModel = vm,
                    documentStore = documentStore,
                    onAddMoreScans = { bitmaps -> vm.addPages(bitmaps) },
                    onEditPage = { index ->
                        cropBitmap = vm.pages.value[index].displayBitmap
                        navController.navigate(AppRoutes.CROP)
                    },
                    onExtractText = { bitmaps ->
                        ocrBitmaps = bitmaps
                        navController.navigate(AppRoutes.EXTRACTED_TEXT)
                    },
                    onSaved = {
                        sessionViewModel = null
                        navController.popBackStack(AppRoutes.HOME, false)
                    },
                    onBack = { navController.popBackStack() }
                )
            }
        }

        composable(AppRoutes.LIBRARY) {
            LibraryScreen(
                documentStore = documentStore,
                onDocumentClick = { doc ->
                    navController.navigate(AppRoutes.documentDetail(doc.id))
                },
                onFolderClick = { folder ->
                    navController.navigate(AppRoutes.folderDetail(folder.id))
                },
                onBack = { navController.popBackStack() }
            )
        }

        composable(
            route = AppRoutes.DOCUMENT_DETAIL,
            arguments = listOf(navArgument("id") { type = NavType.StringType })
        ) { backStackEntry ->
            val docId = backStackEntry.arguments?.getString("id") ?: return@composable
            DocumentDetailScreen(
                documentId = docId,
                documentStore = documentStore,
                onExtractText = { bitmaps ->
                    ocrBitmaps = bitmaps
                    navController.navigate(AppRoutes.EXTRACTED_TEXT)
                },
                onBack = { navController.popBackStack() }
            )
        }

        composable(
            route = AppRoutes.FOLDER_DETAIL,
            arguments = listOf(navArgument("id") { type = NavType.StringType })
        ) { backStackEntry ->
            val folderId = backStackEntry.arguments?.getString("id") ?: return@composable
            FolderDetailScreen(
                folderId = folderId,
                documentStore = documentStore,
                onDocumentClick = { doc ->
                    navController.navigate(AppRoutes.documentDetail(doc.id))
                },
                onBack = { navController.popBackStack() }
            )
        }

        composable(AppRoutes.EXTRACTED_TEXT) {
            ExtractedTextScreen(
                bitmaps = ocrBitmaps,
                onBack = { navController.popBackStack() }
            )
        }
    }
}
