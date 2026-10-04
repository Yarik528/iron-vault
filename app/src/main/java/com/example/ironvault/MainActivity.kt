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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// Цветовая палитра CryptoForge
object CFColors {
    val Primary = Color(0xFF00D9FF)      // Электрический голубой
    val Accent = Color(0xFFFF6B35)       // Оранжевый огонь
    val Background = Color(0xFF0A0E27)   // Глубокий синий
    val Surface = Color(0xFF151932)      // Карточки
    val SurfaceLight = Color(0xFF1E2342) // Ховер
    val TextPrimary = Color.White
    val TextSecondary = Color(0xFF8B92A8)
    val Success = Color(0xFF00E676)
    val Error = Color(0xFFFF1744)
    
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
    val isUnlocked by vm.isUnlocked.collectAsState()

    AnimatedContent(targetState = isUnlocked, label = "screen_transition") { unlocked ->
        if (!unlocked) LockScreen(vm) else VaultScreen(vm)
    }
}

// ===== ЭКРАН БЛОКИРОВКИ =====
@Composable
fun LockScreen(vm: VaultViewModel) {
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
            // Логотип с градиентом
            Box(
                Modifier.size(120.dp).clip(CircleShape).background(CFColors.GradientLogo),
                contentAlignment = Alignment.Center
            ) {
                Text("⚒️", fontSize = 60.sp)
            }
            
            Spacer(Modifier.height(24.dp))
            
            Text(
                "CryptoForge",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = CFColors.TextPrimary
            )
            Text(
                "Выковано для защиты",
                fontSize = 14.sp,
                color = CFColors.TextSecondary
            )
            
            Spacer(Modifier.height(40.dp))

            // Поле ввода с подсветкой
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Мастер-пароль", color = CFColors.TextSecondary) },
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

            // Фейковый режим с красивым переключателем
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
                Column {
                    Text("Фейковый режим", color = CFColors.TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                    Text("Открывает пустой сейф под давлением", color = CFColors.TextSecondary, fontSize = 12.sp)
                }
            }

            Spacer(Modifier.height(24.dp))

            // Кнопка с градиентом
            Button(
                onClick = { vm.unlock(password, useDecoy) },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                shape = RoundedCornerShape(16.dp),
                contentPadding = PaddingValues(0.dp)
            ) {
                Box(
                    Modifier.fillMaxSize().background(CFColors.GradientPrimary),
                    contentAlignment = Alignment.Center
                ) {
                    Text("ОТКРЫТЬ СЕЙФ", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
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

            Spacer(Modifier.height(32.dp))
            
            // Предупреждение
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("⚠️", fontSize = 16.sp)
                Spacer(Modifier.width(8.dp))
                Text(
                    "10 неудачных попыток = автоуничтожение",
                    color = CFColors.TextSecondary,
                    fontSize = 12.sp
                )
            }
        }
    }
}

// ===== ГЛАВНЫЙ ЭКРАН СЕЙФА =====
@Composable
fun VaultScreen(vm: VaultViewModel) {
    val entries by vm.entries.collectAsState()
    val isDecoy by vm.isDecoyMode.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("⚒️", fontSize = 24.sp)
                        Spacer(Modifier.width(8.dp))
                        Text(
                            if (isDecoy) "ФЕЙКОВЫЙ СЕЙФ" else "CryptoForge",
                            color = if (isDecoy) CFColors.Accent else CFColors.Primary,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = CFColors.Background),
                actions = {
                    IconButton(onClick = { vm.lock() }) {
                        Box(
                            Modifier.size(40.dp).clip(CircleShape).background(CFColors.Surface),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("🔒", fontSize = 20.sp)
                        }
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
                // Статистика
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
                Text("🔐", fontSize = 24.sp)
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
                // Иконка в круге
                Box(
                    Modifier.size(40.dp).clip(CircleShape).background(CFColors.Primary.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text("🔑", fontSize = 20.sp)
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
                
                // Кнопка удаления
                IconButton(
                    onClick = { vm.deleteEntry(entry.id) },
                    modifier = Modifier.size(36.dp).clip(CircleShape).background(CFColors.Error.copy(alpha = 0.1f))
                ) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = CFColors.Error, modifier = Modifier.size(18.dp))
                }
            }
            
            Spacer(Modifier.height(12.dp))
            
            // Пароль с кнопкой показа
            Row(
                Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(CFColors.Background).padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    if (showPassword) entry.password else "••••••••••••",
                    color = if (showPassword) CFColors.Primary else CFColors.TextSecondary,
                    fontSize = 14.sp,
                    fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                    modifier = Modifier.weight(1f)
                )
                
                TextButton(
                    onClick = { showPassword = !showPassword },
                    contentPadding = PaddingValues(horizontal = 8.dp),
                    modifier = Modifier.height(32.dp)
                ) {
                    Text(if (showPassword) "🙈" else "👁️", fontSize = 18.sp)
                }
            }
            
            if (entry.notes.isNotBlank()) {
                Spacer(Modifier.height(8.dp))
                Text(
                    " ${entry.notes}",
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
        Text(if (isDecoy) "🎭" else "", fontSize = 64.sp)
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
        icon = { Text("⚒️", fontSize = 32.sp) },
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
