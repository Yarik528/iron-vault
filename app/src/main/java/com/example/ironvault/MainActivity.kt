@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.ironvault

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

object CFColors {
    val Primary = Color(0xFF00D9FF)
    val Accent = Color(0xFFFF6B35)
    val Background = Color(0xFF0A0E27)
    val Surface = Color(0xFF151932)
    val SurfaceLight = Color(0xFF1E2342)
    val TextPrimary = Color.White
    val TextSecondary = Color(0xFF8B92A8)
    val Success = Color(0xFF00E676)
    val Error = Color(0xFFFF1744)
    val Warning = Color(0xFFFFAB00)

    val GradientPrimary = Brush.horizontalGradient(listOf(Primary, Color(0xFF0099CC)))
    val GradientAccent = Brush.horizontalGradient(listOf(Accent, Color(0xFFFF8A50)))
    val GradientLogo = Brush.horizontalGradient(listOf(Primary, Accent))
}

class MainActivity : ComponentActivity() {
    private val vm: VaultViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        vm.init(applicationContext)

        setContent {
            MaterialTheme(colorScheme = darkColorScheme()) {
                CryptoForgeApp(vm)
            }
        }
    }
}

@Composable
fun CryptoForgeApp(vm: VaultViewModel) {
    val isFirstLaunch by vm.isFirstLaunch.collectAsState()
    val isUnlocked by vm.isUnlocked.collectAsState()
    val authType by vm.currentAuthType.collectAsState()

    AnimatedContent(
        targetState = Triple(isFirstLaunch, isUnlocked, authType),
        label = "screen_transition"
    ) { (first, unlocked, type) ->
        when {
            first -> OnboardingScreen(vm)
            !unlocked -> LockScreen(vm, type)
            else -> VaultScreen(vm)
        }
    }
}

@Composable
fun OnboardingScreen(vm: VaultViewModel) {
    var step by remember { mutableStateOf(0) }
    var pin by remember { mutableStateOf("") }
    var confirmPin by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }

    Box(
        Modifier.fillMaxSize().background(CFColors.Background),
        contentAlignment = Alignment.Center
    ) {
        when (step) {
            0 -> {
                Column(
                    Modifier.padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        Modifier.size(120.dp).clip(CircleShape).background(CFColors.GradientLogo),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Build,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(60.dp)
                        )
                    }

                    Spacer(Modifier.height(32.dp))

                    Text(
                        "CryptoForge",
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        color = CFColors.TextPrimary
                    )
                    Text(
                        "Менеджер паролей военного уровня",
                        fontSize = 16.sp,
                        color = CFColors.TextSecondary
                    )

                    Spacer(Modifier.height(48.dp))

                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        FeatureRow(Icons.Default.Lock, "AES-256 шифрование")
                        FeatureRow(Icons.Default.OfflineBolt, "Работает без интернета")
                        FeatureRow(Icons.Default.Shield, "Локальное хранение")
                        FeatureRow(Icons.Default.Fingerprint, "Биометрия (скоро)")
                    }

                    Spacer(Modifier.weight(1f))

                    Button(
                        onClick = { step = 1 },
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                        shape = RoundedCornerShape(16.dp),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Box(
                            Modifier.fillMaxSize().background(CFColors.GradientPrimary),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("НАЧАТЬ", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            1 -> {
                Column(
                    Modifier.padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        "Создайте PIN-код",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = CFColors.TextPrimary
                    )
                    Text(
                        "Минимум 4 цифры. Вы сможете изменить его позже в настройках.",
                        fontSize = 14.sp,
                        color = CFColors.TextSecondary
                    )

                    Spacer(Modifier.height(40.dp))

                    OutlinedTextField(
                        value = pin,
                        onValueChange = { if (it.all { c -> c.isDigit() }) pin = it },
                        label = { Text("PIN-код", color = CFColors.TextSecondary) },
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = CFColors.TextPrimary,
                            unfocusedTextColor = CFColors.TextPrimary,
                            cursorColor = CFColors.Primary,
                            focusedBorderColor = CFColors.Primary,
                            unfocusedBorderColor = CFColors.SurfaceLight
                        ),
                        shape = RoundedCornerShape(16.dp),
                        singleLine = true
                    )

                    Spacer(Modifier.height(16.dp))

                    OutlinedTextField(
                        value = confirmPin,
                        onValueChange = { if (it.all { c -> c.isDigit() }) confirmPin = it },
                        label = { Text("Подтвердите PIN", color = CFColors.TextSecondary) },
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = CFColors.TextPrimary,
                            unfocusedTextColor = CFColors.TextPrimary,
                            cursorColor = CFColors.Primary,
                            focusedBorderColor = CFColors.Primary,
                            unfocusedBorderColor = CFColors.SurfaceLight
                        ),
                        shape = RoundedCornerShape(16.dp),
                        singleLine = true
                    )

                    if (error != null) {
                        Spacer(Modifier.height(16.dp))
                        Text(error!!, color = CFColors.Error, fontSize = 14.sp)
                    }

                    Spacer(Modifier.height(32.dp))

                    Button(
                        onClick = {
                            when {
                                pin.length < 4 -> error = "PIN должен быть минимум 4 цифры"
                                pin != confirmPin -> error = "PIN-коды не совпадают"
                                else -> {
                                    error = null
                                    vm.setupPin(pin)
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                        shape = RoundedCornerShape(16.dp),
                        contentPadding = PaddingValues(0.dp),
                        enabled = pin.length >= 4
                    ) {
                        Box(
                            modifier = if (pin.length >= 4) {
                                Modifier.fillMaxSize().background(CFColors.GradientPrimary)
                            } else {
                                Modifier.fillMaxSize().background(CFColors.SurfaceLight)
                            },
                            contentAlignment = Alignment.Center
                        ) {
                            Text("СОЗДАТЬ PIN", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(Modifier.height(16.dp))

                    TextButton(onClick = { step = 0 }) {
                        Text("Назад", color = CFColors.TextSecondary)
                    }
                }
            }
        }
    }
}

@Composable
fun FeatureRow(icon: ImageVector, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            Modifier.size(40.dp).clip(CircleShape).background(CFColors.Primary.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = CFColors.Primary, modifier = Modifier.size(20.dp))
        }
        Spacer(Modifier.width(16.dp))
        Text(text, color = CFColors.TextPrimary, fontSize = 16.sp)
    }
}

@Composable
fun LockScreen(vm: VaultViewModel, authType: AuthType) {
    var pin by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var useDecoy by remember { mutableStateOf(false) }
    val error by vm.error.collectAsState()

    Box(
        Modifier.fillMaxSize().background(CFColors.Background),
        contentAlignment = Alignment.Center
    ) {
        Column(
            Modifier.padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                Modifier.size(100.dp).clip(CircleShape).background(CFColors.GradientLogo),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Lock,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(50.dp)
                )
            }

            Spacer(Modifier.height(24.dp))

            Text(
                "CryptoForge",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = CFColors.TextPrimary
            )

            Spacer(Modifier.height(32.dp))

            when (authType) {
                AuthType.PIN -> {
                    OutlinedTextField(
                        value = pin,
                        onValueChange = { if (it.all { c -> c.isDigit() }) pin = it },
                        label = { Text("Введите PIN", color = CFColors.TextSecondary) },
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = CFColors.TextPrimary,
                            unfocusedTextColor = CFColors.TextPrimary,
                            cursorColor = CFColors.Primary,
                            focusedBorderColor = CFColors.Primary,
                            unfocusedBorderColor = CFColors.SurfaceLight
                        ),
                        shape = RoundedCornerShape(16.dp),
                        singleLine = true
                    )

                    Spacer(Modifier.height(24.dp))

                    Button(
                        onClick = { vm.unlockWithPin(pin, useDecoy) },
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                        shape = RoundedCornerShape(16.dp),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Box(
                            Modifier.fillMaxSize().background(CFColors.GradientPrimary),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("РАЗБЛОКИРОВАТЬ", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                AuthType.PASSWORD -> {
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("Введите пароль", color = CFColors.TextSecondary) },
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = CFColors.TextPrimary,
                            unfocusedTextColor = CFColors.TextPrimary,
                            cursorColor = CFColors.Primary,
                            focusedBorderColor = CFColors.Primary,
                            unfocusedBorderColor = CFColors.SurfaceLight
                        ),
                        shape = RoundedCornerShape(16.dp),
                        singleLine = true
                    )

                    Spacer(Modifier.height(24.dp))

                    Button(
                        onClick = { vm.unlockWithPassword(password, useDecoy) },
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                        shape = RoundedCornerShape(16.dp),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Box(
                            Modifier.fillMaxSize().background(CFColors.GradientPrimary),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("РАЗБЛОКИРОВАТЬ", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                AuthType.PATTERN -> {
                    Text("Нарисуйте графический ключ", color = CFColors.TextSecondary, fontSize = 14.sp)
                    Spacer(Modifier.height(24.dp))

                    PatternLock(
                        size = 3,
                        onPatternComplete = { pattern -> vm.unlockWithPattern(pattern, useDecoy) },
                        modifier = Modifier.padding(16.dp)
                    )
                }

                AuthType.NONE -> {
                    Text("Метод защиты не установлен", color = CFColors.Error)
                }
            }

            Spacer(Modifier.height(24.dp))

            Row(
                Modifier.fillMaxWidth().padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Switch(
                    checked = useDecoy,
                    onCheckedChange = { useDecoy = it },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = CFColors.Accent,
                        checkedTrackColor = CFColors.Accent.copy(alpha = 0.5f)
                    )
                )
                Spacer(Modifier.width(12.dp))
                Text("Фейковый режим", color = CFColors.TextSecondary, fontSize = 14.sp)
            }

            if (error != null) {
                Spacer(Modifier.height(16.dp))
                Card(
                    colors = CardDefaults.cardColors(containerColor = CFColors.Error.copy(alpha = 0.2f)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        error!!,
                        color = CFColors.Error,
                        fontSize = 14.sp,
                        modifier = Modifier.padding(16.dp),
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Composable
fun VaultScreen(vm: VaultViewModel) {
    val entries by vm.entries.collectAsState()
    val isDecoy by vm.isDecoyMode.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }
    var showSettings by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Build,
                            contentDescription = null,
                            tint = CFColors.Primary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            if (isDecoy) "ФЕЙКОВЫЙ РЕЖИМ" else "CryptoForge",
                            color = if (isDecoy) CFColors.Accent else CFColors.Primary,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = CFColors.Background),
                actions = {
                    IconButton(onClick = { showSettings = true }) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings", tint = CFColors.TextSecondary)
                    }
                    IconButton(onClick = { vm.lock() }) {
                        Icon(Icons.Default.Lock, contentDescription = "Lock", tint = CFColors.TextSecondary)
                    }
                }
            )
        },
        floatingActionButton = {
            if (!isDecoy) {
                FloatingActionButton(
                    onClick = { showAddDialog = true },
                    containerColor = Color.Transparent,
                    shape = CircleShape,
                    modifier = Modifier.size(64.dp)
                ) {
                    Box(
                        Modifier.fillMaxSize().background(CFColors.GradientAccent),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Add", tint = Color.White, modifier = Modifier.size(32.dp))
                    }
                }
            }
        },
        containerColor = CFColors.Background
    ) { padding ->
        LazyColumn(
            Modifier.padding(padding).padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                StatsCard(entries.size, isDecoy)
            }

            items(entries, key = { it.id }) { entry ->
                EntryCard(entry, vm)
            }

            if (entries.isEmpty()) {
                item {
                    EmptyState(isDecoy)
                }
            }
        }
    }

    if (showAddDialog) {
        AddEntryDialog(vm) { showAddDialog = false }
    }

    if (showSettings) {
        SettingsScreen(vm) { showSettings = false }
    }
}

@Composable
fun StatsCard(count: Int, isDecoy: Boolean) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = CFColors.Surface),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                Modifier.size(48.dp).clip(CircleShape).background(CFColors.Primary.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Shield, contentDescription = null, tint = CFColors.Primary, modifier = Modifier.size(24.dp))
            }
            Spacer(Modifier.width(16.dp))
            Column {
                Text(
                    if (isDecoy) "Фейковые записи" else "Защищённые записи",
                    color = CFColors.TextSecondary,
                    fontSize = 12.sp
                )
                Text(
                    "$count записей",
                    color = CFColors.TextPrimary,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(Modifier.weight(1f))
            Box(
                Modifier.padding(8.dp).clip(RoundedCornerShape(8.dp)).background(
                    if (isDecoy) CFColors.Accent.copy(alpha = 0.2f) else CFColors.Success.copy(alpha = 0.2f)
                ).padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text(
                    if (isDecoy) "DECOY" else "AES-256",
                    color = if (isDecoy) CFColors.Accent else CFColors.Success,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun EntryCard(entry: VaultEntry, vm: VaultViewModel) {
    var showPassword by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth().animateContentSize(),
        colors = CardDefaults.cardColors(containerColor = CFColors.Surface),
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, CFColors.SurfaceLight)
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier.size(40.dp).clip(CircleShape).background(CFColors.Primary.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Key, contentDescription = null, tint = CFColors.Primary, modifier = Modifier.size(20.dp))
                }

                Spacer(Modifier.width(12.dp))

                Column(Modifier.weight(1f)) {
                    Text(
                        entry.title,
                        color = CFColors.TextPrimary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        entry.username,
                        color = CFColors.TextSecondary,
                        fontSize = 13.sp
                    )
                }

                IconButton(
                    onClick = { vm.deleteEntry(entry.id) },
                    modifier = Modifier.size(36.dp).clip(CircleShape).background(CFColors.Error.copy(alpha = 0.1f))
                ) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = CFColors.Error, modifier = Modifier.size(18.dp))
                }
            }

            Spacer(Modifier.height(12.dp))

            Row(
                Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(CFColors.Background).padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    if (showPassword) entry.password else "••••••••••••",
                    color = if (showPassword) CFColors.Primary else CFColors.TextSecondary,
                    fontSize = 14.sp,
                    fontFamily = FontFamily.Monospace,
                    modifier = Modifier.weight(1f)
                )

                IconButton(
                    onClick = { showPassword = !showPassword },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        if (showPassword) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                        contentDescription = "Toggle",
                        tint = CFColors.TextSecondary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            if (entry.notes.isNotBlank()) {
                Spacer(Modifier.height(8.dp))
                Text(
                    entry.notes,
                    color = CFColors.TextSecondary,
                    fontSize = 12.sp,
                    lineHeight = 18.sp
                )
            }
        }
    }
}

@Composable
fun EmptyState(isDecoy: Boolean) {
    Column(
        Modifier.fillMaxWidth().padding(vertical = 64.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            if (isDecoy) Icons.Default.TheaterComedy else Icons.Default.Lock,
            contentDescription = null,
            tint = CFColors.TextSecondary,
            modifier = Modifier.size(64.dp)
        )
        Spacer(Modifier.height(16.dp))
        Text(
            if (isDecoy) "Фейковый сейф пуст" else "Сейф пуст",
            color = CFColors.TextPrimary,
            fontSize = 20.sp,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(Modifier.height(8.dp))
        Text(
            if (isDecoy) "Добавьте фейковые записи для отвода глаз" else "Нажмите + чтобы добавить первую запись",
            color = CFColors.TextSecondary,
            fontSize = 14.sp
        )
    }
}

@Composable
fun AddEntryDialog(vm: VaultViewModel, onDismiss: () -> Unit) {
    var title by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = { Icon(Icons.Default.Add, contentDescription = null, tint = CFColors.Primary, modifier = Modifier.size(32.dp)) },
        title = {
            Text("Новая запись", color = CFColors.TextPrimary, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Название", color = CFColors.TextSecondary) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = CFColors.TextPrimary,
                        unfocusedTextColor = CFColors.TextPrimary,
                        focusedBorderColor = CFColors.Primary,
                        unfocusedBorderColor = CFColors.SurfaceLight
                    ),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )
                OutlinedTextField(
                    value = username,
                    onValueChange = { username = it },
                    label = { Text("Логин / Email", color = CFColors.TextSecondary) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = CFColors.TextPrimary,
                        unfocusedTextColor = CFColors.TextPrimary,
                        focusedBorderColor = CFColors.Primary,
                        unfocusedBorderColor = CFColors.SurfaceLight
                    ),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("Пароль", color = CFColors.TextSecondary) },
                        modifier = Modifier.weight(1f),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = CFColors.TextPrimary,
                            unfocusedTextColor = CFColors.TextPrimary,
                            focusedBorderColor = CFColors.Primary,
                            unfocusedBorderColor = CFColors.SurfaceLight
                        ),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )
                    Spacer(Modifier.width(8.dp))
                    IconButton(
                        onClick = { password = vm.generatePassword() },
                        modifier = Modifier.size(48.dp).clip(CircleShape).background(CFColors.Primary.copy(alpha = 0.15f))
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = "Generate", tint = CFColors.Primary)
                    }
                }
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Заметки (опционально)", color = CFColors.TextSecondary) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = CFColors.TextPrimary,
                        unfocusedTextColor = CFColors.TextPrimary,
                        focusedBorderColor = CFColors.Primary,
                        unfocusedBorderColor = CFColors.SurfaceLight
                    ),
                    shape = RoundedCornerShape(12.dp),
                    minLines = 2
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    vm.addEntry(title, username, password, notes)
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                shape = RoundedCornerShape(12.dp),
                contentPadding = PaddingValues(0.dp),
                modifier = Modifier.height(44.dp)
            ) {
                Box(
                    Modifier.fillMaxSize().background(CFColors.GradientPrimary).padding(horizontal = 24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Сохранить", color = Color.White, fontWeight = FontWeight.SemiBold)
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Отмена", color = CFColors.TextSecondary)
            }
        },
        containerColor = CFColors.Surface,
        shape = RoundedCornerShape(20.dp)
    )
}

@Composable
fun SettingsScreen(vm: VaultViewModel, onDismiss: () -> Unit) {
    val currentAuthType by vm.currentAuthType.collectAsState()
    var showChangeAuth by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Settings, contentDescription = null, tint = CFColors.Primary)
                Spacer(Modifier.width(8.dp))
                Text("Настройки", color = CFColors.TextPrimary, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Безопасность", color = CFColors.TextSecondary, fontSize = 12.sp, fontWeight = FontWeight.Bold)

                SettingsRow(
                    icon = Icons.Default.Lock,
                    title = "Метод защиты",
                    subtitle = when (currentAuthType) {
                        AuthType.PIN -> "PIN-код"
                        AuthType.PASSWORD -> "Пароль"
                        AuthType.PATTERN -> "Графический ключ"
                        AuthType.NONE -> "Не установлен"
                    },
                    onClick = { showChangeAuth = true }
                )

                Spacer(Modifier.height(16.dp))
                Text("О приложении", color = CFColors.TextSecondary, fontSize = 12.sp, fontWeight = FontWeight.Bold)

                SettingsRow(
                    icon = Icons.Default.Info,
                    title = "Версия",
                    subtitle = "1.0.0",
                    onClick = {}
                )

                SettingsRow(
                    icon = Icons.Default.Shield,
                    title = "Шифрование",
                    subtitle = "AES-256-GCM + PBKDF2",
                    onClick = {}
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Закрыть", color = CFColors.Primary)
            }
        },
        containerColor = CFColors.Surface,
        shape = RoundedCornerShape(20.dp)
    )

    if (showChangeAuth) {
        ChangeAuthDialog(vm) { showChangeAuth = false }
    }
}

@Composable
fun SettingsRow(icon: ImageVector, title: String, subtitle: String, onClick: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(CFColors.Background).clickable(onClick = onClick).padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = CFColors.Primary, modifier = Modifier.size(24.dp))
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(title, color = CFColors.TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Medium)
            Text(subtitle, color = CFColors.TextSecondary, fontSize = 13.sp)
        }
        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = CFColors.TextSecondary)
    }
}

@Composable
fun ChangeAuthDialog(vm: VaultViewModel, onDismiss: () -> Unit) {
    val currentAuthType by vm.currentAuthType.collectAsState()
    var selectedType by remember { mutableStateOf(currentAuthType) }
    var pin by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var pattern by remember { mutableStateOf<List<Int>>(emptyList()) }
    var error by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Изменить метод защиты", color = CFColors.TextPrimary, fontSize = 18.sp, fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                AuthTypeSelector(selectedType) { selectedType = it }

                when (selectedType) {
                    AuthType.PIN -> {
                        OutlinedTextField(
                            value = pin,
                            onValueChange = { if (it.all { c -> c.isDigit() }) pin = it },
                            label = { Text("Новый PIN (мин. 4 цифры)", color = CFColors.TextSecondary) },
                            visualTransformation = PasswordVisualTransformation(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = CFColors.TextPrimary,
                                unfocusedTextColor = CFColors.TextPrimary,
                                focusedBorderColor = CFColors.Primary
                            ),
                            shape = RoundedCornerShape(12.dp)
                        )
                    }
                    AuthType.PASSWORD -> {
                        OutlinedTextField(
                            value = password,
                            onValueChange = { password = it },
                            label = { Text("Новый пароль", color = CFColors.TextSecondary) },
                            visualTransformation = PasswordVisualTransformation(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = CFColors.TextPrimary,
                                unfocusedTextColor = CFColors.TextPrimary,
                                focusedBorderColor = CFColors.Primary
                            ),
                            shape = RoundedCornerShape(12.dp)
                        )
                    }
                    AuthType.PATTERN -> {
                        Text("Нарисуйте новый ключ (мин. 4 точки)", color = CFColors.TextSecondary, fontSize = 14.sp)
                        PatternLock(
                            size = 3,
                            onPatternComplete = { pattern = it },
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                    AuthType.NONE -> {
                        Text("Защита будет отключена", color = CFColors.Warning, fontSize = 14.sp)
                    }
                }

                if (error != null) {
                    Text(error!!, color = CFColors.Error, fontSize = 14.sp)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    error = null
                    when (selectedType) {
                        AuthType.PIN -> {
                            if (pin.length >= 4) {
                                vm.setupPin(pin)
                                onDismiss()
                            } else {
                                error = "PIN должен быть минимум 4 цифры"
                            }
                        }
                        AuthType.PASSWORD -> {
                            if (password.length >= 6) {
                                vm.setupPassword(password)
                                onDismiss()
                            } else {
                                error = "Пароль должен быть минимум 6 символов"
                            }
                        }
                        AuthType.PATTERN -> {
                            if (pattern.size >= 4) {
                                vm.setupPattern(pattern)
                                onDismiss()
                            } else {
                                error = "Ключ должен содержать минимум 4 точки"
                            }
                        }
                        AuthType.NONE -> {
                            vm.setupPin("")
                            onDismiss()
                        }
                    }
                },
                colors = ButtonDefaults.buttonColors(CFColors.Primary)
            ) {
                Text("Сохранить", color = Color.White)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Отмена", color = CFColors.TextSecondary) }
        },
        containerColor = CFColors.Surface,
        shape = RoundedCornerShape(20.dp)
    )
}

@Composable
fun AuthTypeSelector(selected: AuthType, onSelect: (AuthType) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        AuthTypeOption(
            icon = Icons.Default.Pin,
            title = "PIN-код",
            subtitle = "Быстрый доступ, 4+ цифр",
            selected = selected == AuthType.PIN,
            onClick = { onSelect(AuthType.PIN) }
        )
        AuthTypeOption(
            icon = Icons.Default.Password,
            title = "Пароль",
            subtitle = "Максимальная защита, 6+ символов",
            selected = selected == AuthType.PASSWORD,
            onClick = { onSelect(AuthType.PASSWORD) }
        )
        AuthTypeOption(
            icon = Icons.Default.Pattern,
            title = "Графический ключ",
            subtitle = "Удобно на телефоне, 4+ точки",
            selected = selected == AuthType.PATTERN,
            onClick = { onSelect(AuthType.PATTERN) }
        )
    }
}

@Composable
fun AuthTypeOption(icon: ImageVector, title: String, subtitle: String, selected: Boolean, onClick: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(
            if (selected) CFColors.Primary.copy(alpha = 0.15f) else CFColors.Background
        ).border(
            width = if (selected) 2.dp else 0.dp,
            color = if (selected) CFColors.Primary else Color.Transparent,
            shape = RoundedCornerShape(12.dp)
        ).clickable(onClick = onClick).padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            icon,
            contentDescription = null,
            tint = if (selected) CFColors.Primary else CFColors.TextSecondary,
            modifier = Modifier.size(24.dp)
        )
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(
                title,
                color = if (selected) CFColors.Primary else CFColors.TextPrimary,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )
            Text(subtitle, color = CFColors.TextSecondary, fontSize = 12.sp)
        }
        if (selected) {
            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = CFColors.Primary, modifier = Modifier.size(24.dp))
        }
    }
}
