package ir.madreseyar.teacher

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items as listItems
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items as gridItems
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.AutoGraph
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Class
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.QuestionAnswer
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.MoreHoriz
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import kotlinx.coroutines.delay

private val Purple900 = Color(0xFF2B0A67)
private val Purple800 = Color(0xFF3A0D88)
private val Purple700 = Color(0xFF5B20D6)
private val Purple500 = Color(0xFF7C3AED)
private val AppBackground = Color(0xFFF6F7FC)
private val TextPrimary = Color(0xFF151B2D)
private val TextSecondary = Color(0xFF6B7280)
private val Border = Color(0xFFE5E7EB)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContent {
            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                MaterialTheme {
                    TeacherNativeApp()
                }
            }
        }
    }
}

private enum class AppScreen { Splash, Login, Home, Classes, Messages, More }

private data class FeatureItem(
    val title: String,
    val icon: ImageVector,
    val badge: String? = null,
    val routeHint: String? = null,
)

@Composable
private fun TeacherNativeApp() {
    val context = LocalContext.current
    var screen by remember { mutableStateOf(AppScreen.Splash) }
    var serverReady by remember { mutableStateOf(false) }
    val securePrefs = remember { context.getSharedPreferences("madreseyar_teacher_secure_session", Context.MODE_PRIVATE) }
    fun readBiometricSession(): Boolean =
        securePrefs.getBoolean("biometric_enabled", false) &&
            !securePrefs.getString("session_ciphertext", null).isNullOrBlank()
    var hasBiometricSession by remember { mutableStateOf(readBiometricSession()) }

    val portalLauncher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        hasBiometricSession = readBiometricSession()
        if (result.resultCode == Activity.RESULT_OK) {
            serverReady = true
            screen = AppScreen.Home
        }
    }

    fun openPortal(username: String? = null, password: String? = null, restore: Boolean = false) {
        val intent = Intent(context, WebPortalActivity::class.java).apply {
            putExtra(WebPortalActivity.EXTRA_RETURN_ON_READY, true)
            putExtra(WebPortalActivity.EXTRA_NATIVE_LOGIN, username != null && password != null)
            putExtra(WebPortalActivity.EXTRA_RESTORE_ONLY, restore)
            username?.let { putExtra(WebPortalActivity.EXTRA_USERNAME, it) }
            password?.let { putExtra(WebPortalActivity.EXTRA_PASSWORD, it) }
        }
        portalLauncher.launch(intent)
    }

    fun openFullPortal() {
        context.startActivity(Intent(context, WebPortalActivity::class.java))
    }

    LaunchedEffect(Unit) {
        delay(1150)
        screen = AppScreen.Login
    }

    AnimatedContent(
        targetState = screen,
        transitionSpec = { fadeIn() togetherWith fadeOut() },
        label = "screen"
    ) { target ->
        when (target) {
            AppScreen.Splash -> SplashScreen()
            AppScreen.Login -> LoginScreen(
                hasBiometricSession = hasBiometricSession,
                onLogin = { user, pass -> openPortal(user, pass, false) },
                onBiometric = { openPortal(restore = true) },
                onAdvancedLogin = { openPortal() },
            )
            AppScreen.Home, AppScreen.Classes, AppScreen.Messages, AppScreen.More -> NativeShell(
                screen = target,
                isServerReady = serverReady,
                onScreenChange = { screen = it },
                onOpenPortal = { openFullPortal() },
                onRefresh = { openFullPortal() },
            )
        }
    }
}

@Composable
private fun SplashScreen() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.linearGradient(
                    listOf(Purple900, Purple800, Purple500)
                )
            )
    ) {
        Box(
            modifier = Modifier
                .size(330.dp)
                .align(Alignment.TopEnd)
                .background(Color.White.copy(alpha = 0.035f), CircleShape)
        )
        Column(
            modifier = Modifier.fillMaxSize().statusBarsPadding().padding(horizontal = 28.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Surface(
                modifier = Modifier.size(126.dp),
                shape = RoundedCornerShape(34.dp),
                color = Color.White.copy(alpha = 0.12f),
                shadowElevation = 16.dp
            ) {
                Image(
                    painter = painterResource(R.mipmap.ic_launcher),
                    contentDescription = null,
                    modifier = Modifier.padding(18.dp),
                    contentScale = ContentScale.Fit
                )
            }
            Spacer(Modifier.height(24.dp))
            Text("مدرسه‌یار", color = Color.White, fontSize = 38.sp, fontWeight = FontWeight.Black)
            Text("پنل معلم", color = Color.White.copy(alpha = 0.9f), fontSize = 22.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(38.dp))
            CircularProgressIndicator(color = Color.White, strokeWidth = 3.dp, modifier = Modifier.size(34.dp))
            Spacer(Modifier.height(14.dp))
            Text("در حال آماده‌سازی برنامه…", color = Color.White.copy(alpha = 0.78f), fontSize = 13.sp)
        }
        Text(
            "نسخه Native Preview 2.0",
            modifier = Modifier.align(Alignment.BottomCenter).navigationBarsPadding().padding(bottom = 18.dp),
            color = Color.White.copy(alpha = 0.7f),
            fontSize = 11.sp
        )
    }
}

@Composable
private fun LoginScreen(
    hasBiometricSession: Boolean,
    onLogin: (String, String) -> Unit,
    onBiometric: () -> Unit,
    onAdvancedLogin: () -> Unit,
) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier.fillMaxSize().background(AppBackground).statusBarsPadding(),
        contentPadding = PaddingValues(horizontal = 24.dp, vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        item {
            Spacer(Modifier.height(18.dp))
            Surface(
                modifier = Modifier.size(112.dp),
                shape = RoundedCornerShape(32.dp),
                color = Color.White,
                shadowElevation = 8.dp
            ) {
                Image(painterResource(R.mipmap.ic_launcher), null, Modifier.padding(14.dp))
            }
            Spacer(Modifier.height(16.dp))
            Text("مدرسه‌یار", color = Purple900, fontWeight = FontWeight.Black, fontSize = 34.sp)
            Text("ورود به پنل معلم", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 20.sp)
            Spacer(Modifier.height(28.dp))
        }
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(26.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
            ) {
                Column(Modifier.padding(18.dp)) {
                    OutlinedTextField(
                        value = username,
                        onValueChange = { username = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("نام کاربری") },
                        leadingIcon = { Icon(Icons.Default.Person, null) },
                        singleLine = true,
                        shape = RoundedCornerShape(16.dp),
                    )
                    Spacer(Modifier.height(12.dp))
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("رمز عبور") },
                        leadingIcon = { Icon(Icons.Default.Lock, null) },
                        trailingIcon = {
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(Icons.Default.Visibility, null)
                            }
                        },
                        singleLine = true,
                        visualTransformation = if (passwordVisible) androidx.compose.ui.text.input.VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        shape = RoundedCornerShape(16.dp),
                    )
                    Spacer(Modifier.height(16.dp))
                    Button(
                        onClick = { if (username.isNotBlank() && password.isNotBlank()) onLogin(username.trim(), password) },
                        modifier = Modifier.fillMaxWidth().height(54.dp),
                        shape = RoundedCornerShape(17.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Purple700),
                    ) {
                        Text("ورود به حساب", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                    if (hasBiometricSession) {
                        Spacer(Modifier.height(12.dp))
                        Surface(
                            modifier = Modifier.fillMaxWidth().clickable(onClick = onBiometric),
                            shape = RoundedCornerShape(17.dp),
                            color = Color(0xFFF4F0FF),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFD8CCFF))
                        ) {
                            Row(
                                Modifier.padding(vertical = 15.dp, horizontal = 16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(Icons.Default.Fingerprint, null, tint = Purple700, modifier = Modifier.size(28.dp))
                                Spacer(Modifier.width(8.dp))
                                Text("ورود با اثر انگشت", color = Purple700, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
        item {
            Spacer(Modifier.height(18.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.CheckCircle, null, tint = Color(0xFF0EA870), modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(7.dp))
                Text("اتصال امن به سامانه اصلی مدرسه‌یار", color = TextSecondary, fontSize = 12.sp)
            }
            TextButton(onClick = onAdvancedLogin) { Text("ورود کامل / MFA", color = Purple700, fontWeight = FontWeight.Bold) }
            Text("اگر حساب شما تأیید دومرحله‌ای دارد، از ورود کامل استفاده کنید.", color = TextSecondary, fontSize = 11.sp)
            Spacer(Modifier.height(32.dp))
        }
    }
}

@Composable
private fun NativeShell(
    screen: AppScreen,
    isServerReady: Boolean,
    onScreenChange: (AppScreen) -> Unit,
    onOpenPortal: () -> Unit,
    onRefresh: () -> Unit,
) {
    Scaffold(
        containerColor = AppBackground,
        bottomBar = {
            NavigationBar(containerColor = Color.White, tonalElevation = 8.dp, modifier = Modifier.navigationBarsPadding()) {
                NavigationBarItem(
                    selected = screen == AppScreen.Home,
                    onClick = { onScreenChange(AppScreen.Home) },
                    icon = { Icon(if (screen == AppScreen.Home) Icons.Default.Home else Icons.Outlined.Home, null) },
                    label = { Text("خانه") }
                )
                NavigationBarItem(
                    selected = screen == AppScreen.Classes,
                    onClick = { onScreenChange(AppScreen.Classes) },
                    icon = { Icon(if (screen == AppScreen.Classes) Icons.Default.Groups else Icons.Outlined.Groups, null) },
                    label = { Text("کلاس‌ها") }
                )
                NavigationBarItem(
                    selected = screen == AppScreen.Messages,
                    onClick = { onScreenChange(AppScreen.Messages) },
                    icon = { Icon(if (screen == AppScreen.Messages) Icons.Default.ChatBubble else Icons.Outlined.ChatBubbleOutline, null) },
                    label = { Text("پیام‌ها") }
                )
                NavigationBarItem(
                    selected = screen == AppScreen.More,
                    onClick = { onScreenChange(AppScreen.More) },
                    icon = { Icon(if (screen == AppScreen.More) Icons.Default.MoreHoriz else Icons.Outlined.MoreHoriz, null) },
                    label = { Text("بیشتر") }
                )
            }
        }
    ) { pad ->
        when (screen) {
            AppScreen.Home -> DashboardScreen(Modifier.padding(pad), isServerReady, onOpenPortal, onRefresh)
            AppScreen.Classes -> ClassesScreen(Modifier.padding(pad), onOpenPortal)
            AppScreen.Messages -> MessagesScreen(Modifier.padding(pad), onOpenPortal)
            AppScreen.More -> MoreScreen(Modifier.padding(pad), onOpenPortal)
            else -> Unit
        }
    }
}

@Composable
private fun DashboardScreen(
    modifier: Modifier,
    isServerReady: Boolean,
    onOpenPortal: () -> Unit,
    onRefresh: () -> Unit,
) {
    val features = remember {
        listOf(
            FeatureItem("کلاس‌های من", Icons.Default.Groups, routeHint = "myTeaching"),
            FeatureItem("تکالیف", Icons.Default.Assignment),
            FeatureItem("پیام‌ها", Icons.Default.ChatBubble, badge = "جدید"),
            FeatureItem("حضور و غیاب", Icons.Default.CheckCircle, routeHint = "classroom"),
            FeatureItem("نمرات", Icons.Default.AutoGraph, routeHint = "gradeCenterT07"),
            FeatureItem("سؤالات دانش‌آموزان", Icons.Default.QuestionAnswer),
            FeatureItem("گزارش‌ها", Icons.Default.School, routeHint = "reports"),
            FeatureItem("اعلانات", Icons.Default.Campaign),
            FeatureItem("فایل‌ها", Icons.Default.Folder),
        )
    }

    LazyColumn(
        modifier = modifier.fillMaxSize().background(AppBackground),
        contentPadding = PaddingValues(bottom = 20.dp)
    ) {
        item {
            Box(
                Modifier
                    .fillMaxWidth()
                    .background(Brush.horizontalGradient(listOf(Purple900, Purple700)))
                    .statusBarsPadding()
                    .padding(horizontal = 20.dp, vertical = 18.dp)
            ) {
                Column(Modifier.fillMaxWidth()) {
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Surface(shape = CircleShape, color = Color.White.copy(alpha = 0.15f), modifier = Modifier.size(48.dp)) {
                            Icon(Icons.Default.Person, null, tint = Color.White, modifier = Modifier.padding(10.dp))
                        }
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text("سلام، معلم عزیز", color = Color.White, fontWeight = FontWeight.Black, fontSize = 20.sp)
                            Text(
                                if (isServerReady) "نشست امن سامانه فعال است" else "پنل Native مدرسه‌یار",
                                color = Color.White.copy(alpha = 0.78f),
                                fontSize = 12.sp
                            )
                        }
                        IconButton(onClick = onRefresh) { Icon(Icons.Default.Refresh, null, tint = Color.White) }
                        IconButton(onClick = onOpenPortal) { Icon(Icons.Default.Notifications, null, tint = Color.White) }
                    }
                    Spacer(Modifier.height(18.dp))
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(22.dp),
                        color = Color.White.copy(alpha = 0.12f)
                    ) {
                        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            Surface(shape = RoundedCornerShape(14.dp), color = Color.White.copy(alpha = 0.16f)) {
                                Icon(Icons.Default.CalendarMonth, null, tint = Color.White, modifier = Modifier.padding(10.dp))
                            }
                            Spacer(Modifier.width(12.dp))
                            Column(Modifier.weight(1f)) {
                                Text("امروز", color = Color.White.copy(alpha = 0.8f), fontSize = 11.sp)
                                Text("داشبورد آموزشی معلم", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                            }
                            Text("همگام‌سازی ۵ ثانیه‌ای", color = Color.White.copy(alpha = 0.85f), fontSize = 10.sp)
                        }
                    }
                }
            }
        }
        item {
            Row(
                Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(Modifier.weight(1f)) {
                    Text("دسترسی سریع", color = TextPrimary, fontWeight = FontWeight.Black, fontSize = 19.sp)
                    Text("مدیریت روزانه کلاس و آموزش", color = TextSecondary, fontSize = 12.sp)
                }
                TextButton(onClick = onOpenPortal) { Text("همه امکانات", color = Purple700, fontWeight = FontWeight.Bold) }
            }
        }
        item {
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                modifier = Modifier.fillMaxWidth().height(382.dp).padding(horizontal = 14.dp),
                userScrollEnabled = false,
                verticalArrangement = Arrangement.spacedBy(10.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                gridItems(features) { feature -> FeatureCard(feature, onOpenPortal) }
            }
        }
        item {
            Spacer(Modifier.height(12.dp))
            Card(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 18.dp),
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(1.dp)
            ) {
                Column(Modifier.padding(18.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.CloudUpload, null, tint = Purple700)
                        Spacer(Modifier.width(9.dp))
                        Text("اتصال به پنل عملیاتی", fontWeight = FontWeight.Black, color = TextPrimary)
                    }
                    Spacer(Modifier.height(7.dp))
                    Text(
                        "ظاهر اصلی این نسخه Native است. تا زمان تکمیل API مستقیم برای همه ماژول‌ها، عملیات کامل و داده‌های زنده از پنل امن فعلی باز می‌شوند.",
                        color = TextSecondary,
                        fontSize = 12.sp,
                        lineHeight = 19.sp
                    )
                    Spacer(Modifier.height(12.dp))
                    Button(
                        onClick = onOpenPortal,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = Purple700),
                        shape = RoundedCornerShape(14.dp)
                    ) { Text("باز کردن امکانات کامل") }
                }
            }
        }
    }
}

@Composable
private fun FeatureCard(item: FeatureItem, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().height(112.dp).clickable(onClick = onClick),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Box(Modifier.fillMaxSize().padding(12.dp)) {
            Column(Modifier.align(Alignment.Center), horizontalAlignment = Alignment.CenterHorizontally) {
                Surface(shape = RoundedCornerShape(16.dp), color = Color(0xFFF1ECFF)) {
                    Icon(item.icon, null, tint = Purple700, modifier = Modifier.padding(10.dp).size(24.dp))
                }
                Spacer(Modifier.height(8.dp))
                Text(item.title, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 11.sp, textAlign = TextAlign.Center)
            }
            if (item.badge != null) {
                Surface(shape = CircleShape, color = Color(0xFFE53935), modifier = Modifier.align(Alignment.TopEnd)) {
                    Text(item.badge, color = Color.White, fontSize = 8.sp, modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp))
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ClassesScreen(modifier: Modifier, onOpenPortal: () -> Unit) {
    NativeListPage(
        modifier = modifier,
        title = "کلاس‌های من",
        subtitle = "فهرست کلاس‌ها و درس‌های فعال",
        topIcon = Icons.Default.Groups,
        onOpenPortal = onOpenPortal,
        rows = listOf("کلاس‌ها و درس‌های واقعی", "حضور و غیاب", "دفتر کلاسی", "برنامه هفتگی")
    )
}

@Composable
private fun MessagesScreen(modifier: Modifier, onOpenPortal: () -> Unit) {
    NativeListPage(
        modifier = modifier,
        title = "پیام‌ها و سؤالات",
        subtitle = "ارتباط با دانش‌آموزان و خانواده",
        topIcon = Icons.Default.QuestionAnswer,
        onOpenPortal = onOpenPortal,
        rows = listOf("پیام‌های جدید", "سؤالات دانش‌آموزان", "ارتباطات", "ملاقات اولیا")
    )
}

@Composable
private fun MoreScreen(modifier: Modifier, onOpenPortal: () -> Unit) {
    NativeListPage(
        modifier = modifier,
        title = "امکانات بیشتر",
        subtitle = "ابزارهای تکمیلی پنل معلم",
        topIcon = Icons.Default.MoreHoriz,
        onOpenPortal = onOpenPortal,
        rows = listOf("مرکز نمرات", "آزمون‌ها", "گزارش‌ها", "کتابخانه دیجیتال", "تنظیمات امنیتی")
    )
}

@Composable
private fun NativeListPage(
    modifier: Modifier,
    title: String,
    subtitle: String,
    topIcon: ImageVector,
    onOpenPortal: () -> Unit,
    rows: List<String>,
) {
    Column(modifier.fillMaxSize().background(AppBackground)) {
        Row(
            Modifier.fillMaxWidth().background(Brush.horizontalGradient(listOf(Purple900, Purple700))).statusBarsPadding().padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(topIcon, null, tint = Color.White, modifier = Modifier.size(30.dp))
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(title, color = Color.White, fontWeight = FontWeight.Black, fontSize = 21.sp)
                Text(subtitle, color = Color.White.copy(alpha = .75f), fontSize = 11.sp)
            }
            IconButton(onClick = onOpenPortal) { Icon(Icons.Default.Search, null, tint = Color.White) }
        }
        LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            listItems(rows) { row ->
                Card(
                    modifier = Modifier.fillMaxWidth().clickable(onClick = onOpenPortal),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(1.dp)
                ) {
                    Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Surface(shape = CircleShape, color = Color(0xFFF1ECFF)) {
                            Icon(Icons.Default.Class, null, tint = Purple700, modifier = Modifier.padding(10.dp))
                        }
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text(row, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text("برای داده‌های زنده وارد پنل عملیاتی شوید", color = TextSecondary, fontSize = 11.sp)
                        }
                        Text("‹", color = Purple700, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
            item {
                Spacer(Modifier.height(6.dp))
                Button(
                    onClick = onOpenPortal,
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Purple700)
                ) { Text("نمایش اطلاعات واقعی سامانه", fontWeight = FontWeight.Bold) }
            }
        }
    }
}
