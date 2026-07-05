package com.vfstr.smartclass.ui.screens.splash

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.filled.Update
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import com.vfstr.smartclass.data.preferences.AppPreferencesRepository
import com.vfstr.smartclass.data.preferences.SecurePreferences
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import javax.inject.Inject

enum class LaunchDestination {
    ONBOARDING,
    STAFF_DASHBOARD,
    STUDENT_PORTAL,
    LOGIN
}

@HiltViewModel
class SplashViewModel @Inject constructor(
    private val appPrefs: AppPreferencesRepository,
    private val securePrefs: SecurePreferences,
    private val repository: com.vfstr.smartclass.data.repositories.AppRepository
) : ViewModel() {
    
    val updateRequired = mutableStateOf<com.vfstr.smartclass.data.remote.api.AppVersionDto?>(null)

    suspend fun checkUpdate(): com.vfstr.smartclass.data.remote.api.AppVersionDto? {
        val latest = repository.getLatestVersion()
        // Simulate current version_code = 1
        if (latest.version_code > 1 && latest.mandatory) {
            updateRequired.value = latest
            return latest
        }
        return null
    }
    
    suspend fun getDestination(): LaunchDestination {
        val isFirstLaunch = appPrefs.isFirstLaunch.first()
        if (isFirstLaunch) return LaunchDestination.ONBOARDING
        
        val staffToken = securePrefs.getStaffToken()
        val studentToken = securePrefs.getStudentToken()
        
        return when {
            staffToken != null && !securePrefs.isTokenExpired(staffToken) -> LaunchDestination.STAFF_DASHBOARD
            studentToken != null && !securePrefs.isTokenExpired(studentToken) -> LaunchDestination.STUDENT_PORTAL
            else -> LaunchDestination.LOGIN
        }
    }
}

@Composable
fun SplashGatekeeperScreen(
    onNavigate: (LaunchDestination) -> Unit,
    viewModel: SplashViewModel = hiltViewModel()
) {
    val scale = remember { Animatable(0f) }
    val updateRequired by viewModel.updateRequired
    
    LaunchedEffect(Unit) {
        scale.animateTo(
            targetValue = 1f,
            animationSpec = tween(
                durationMillis = 1000,
                easing = { OvershootInterpolator(2f).getInterpolation(it) }
            )
        )
        
        val update = viewModel.checkUpdate()
        if (update == null) {
            delay(1500)
            onNavigate(viewModel.getDestination())
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0A0F1E)),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            // ... logo ...
        }

        if (updateRequired != null) {
            UpdateRequiredModal(
                version = updateRequired!!,
                onDownload = {
                    // Logic to trigger DownloadManager
                }
            )
        }
    }
}

@Composable
fun UpdateRequiredModal(
    version: com.vfstr.smartclass.data.remote.api.AppVersionDto,
    onDownload: () -> Unit
) {
    androidx.compose.ui.window.Dialog(onDismissRequest = {}) {
        androidx.compose.material3.Surface(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
            color = Color(0xFF1E293B)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                androidx.compose.material3.Icon(
                    androidx.compose.material.icons.Icons.Default.Update,
                    contentDescription = null,
                    tint = Color(0xFF00D4FF),
                    modifier = Modifier.size(48.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    "Mandatory Update Required",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "Version ${version.version_name} is now available. This update is required to ensure system compatibility and security.",
                    color = Color(0xFF94A3B8),
                    fontSize = 14.sp,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
                if (version.release_notes != null) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        version.release_notes,
                        color = Color(0xFF00D4FF),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
                Spacer(modifier = Modifier.height(24.dp))
                androidx.compose.material3.Button(
                    onClick = onDownload,
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    colors = androidx.compose.material3.ButtonDefaults.buttonColors(containerColor = Color(0xFF00D4FF))
                ) {
                    Text("Download & Install", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

// Simple OvershootInterpolator for Compose
class OvershootInterpolator(private val tension: Float = 2f) : android.view.animation.Interpolator {
    override fun getInterpolation(t: Float): Float {
        var tMutable = t
        tMutable -= 1.0f
        return tMutable * tMutable * ((tension + 1) * tMutable + tension) + 1.0f
    }
}
