@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.ironvault

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

class MainActivity : ComponentActivity() {

    private val vm: VaultViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        vm.init(applicationContext)

        setContent {
            MaterialTheme(colorScheme = darkColorScheme()) {
                IronVaultApp(vm)
            }
        }
    }
}

@Composable
fun IronVaultApp(vm: VaultViewModel) {
    val isUnlocked by vm.isUnlocked.collectAsState()

    if (!isUnlocked) {
        LockScreen(vm)
    } else {
        VaultScreen(vm)
    }
}

@Composable
fun LockScreen(vm: VaultViewModel) {
    var password by remember { mutableStateOf("") }
    var useDecoy by remember { mutableStateOf(false) }
    val error by vm.error.collectAsState()

    Column(
        Modifier.fillMaxSize().background(Color(0xFF0A0A0F)).padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(Icons.Default.Lock, contentDescription = null, tint = Color(0xFF00FFAA), modifier = Modifier.size(80.dp))
        Spacer(Modifier.height(24.dp))
        Text("ЖЕЛЕЗНЫЙ СЕЙФ", color = Color(0xFF00FFAA), fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(8.dp))
        Text("AES-256-GCM + PBKDF2-600K", color = Color.Gray, fontSize = 12.sp)
        Spacer(Modifier.height(40.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Мастер-пароль") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                cursorColor = Color(0xFF00FFAA),
                focusedBorderColor = Color(0xFF00FFAA)
            ),
            shape = RoundedCornerShape(12.dp)
        )

        Spacer(Modifier.height(16.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            Checkbox(
                checked = useDecoy, 
                onCheckedChange = { useDecoy = it },
                colors = CheckboxDefaults.colors(checkedColor = Color(0xFF00FFAA))
            )
            Text("Фейковый режим (под давлением)", color = Color.Gray, fontSize = 12.sp)
        }

        Spacer(Modifier.height(24.dp))

        Button(
            onClick = { vm.unlock(password, useDecoy) },
            modifier = Modifier.fillMaxWidth().height(50.dp),
            colors = ButtonDefaults.buttonColors(Color(0xFF00FFAA)),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("ОТКРЫТЬ СЕЙФ", color = Color.Black, fontWeight = FontWeight.Bold)
        }

        if (error != null) {
            Spacer(Modifier.height(16.dp))
            Text(error!!, color = Color(0xFFFF1744), fontSize = 14.sp, fontWeight = FontWeight.Bold)
        }

        Spacer(Modifier.weight(1f))
        Text("⚠️ 10 неудачных попыток = автоуничтожение", color = Color(0xFFFF1744), fontSize = 11.sp)
    }
}

@Composable
fun VaultScreen(vm: VaultViewModel) {
    val entries by vm.entries.collectAsState()
    val isDecoy by vm.isDecoyMode.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        if (isDecoy) "⚠️ ФЕЙКОВЫЙ СЕЙФ" else "🔐 ЖЕЛЕЗНЫЙ СЕЙФ",
                        color = if (isDecoy) Color(0xFFFFAA00) else Color(0xFF00FFAA)
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF12121A)),
                actions = {
                    IconButton(onClick = { vm.lock() }) {
                        Icon(Icons.Default.Lock, contentDescription = "Lock", tint = Color.White)
                    }
                }
            )
        },
        floatingActionButton = {
            if (!isDecoy) {
                FloatingActionButton(
                    onClick = { showAddDialog = true },
                    containerColor = Color(0xFF00FFAA)
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add", tint = Color.Black)
                }
            }
        },
        containerColor = Color(0xFF0A0A0F)
    ) { padding ->
        LazyColumn(
            Modifier.padding(padding).padding(horizontal = 12.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(entries) { entry -> EntryCard(entry, vm) }
        }
    }

    if (showAddDialog) {
        AddEntryDialog(vm) { showAddDialog = false }
    }
}

@Composable
fun EntryCard(entry: VaultEntry, vm: VaultViewModel) {
    var showPassword by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF12121A)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Key, contentDescription = null, tint = Color(0xFF00FFAA))
                Spacer(Modifier.width(8.dp))
                Text(entry.title, color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                
                // Кнопка удаления
                IconButton(onClick = { vm.deleteEntry(entry.id) }, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color(0xFFFF1744), modifier = Modifier.size(20.dp))
                }
            }
            Spacer(Modifier.height(8.dp))
            Text("👤 ${entry.username}", color = Color.Gray, fontSize = 14.sp)
            
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    if (showPassword) entry.password else "🔑 ••••••••", 
                    color = Color(0xFF00FFAA), 
                    fontSize = 14.sp,
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = { showPassword = !showPassword }, modifier = Modifier.size(32.dp)) {
                    Icon(
                        if (showPassword) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                        contentDescription = "Toggle",
                        tint = Color.Gray,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
            
            if (entry.notes.isNotBlank()) {
                Spacer(Modifier.height(4.dp))
                Text("📝 ${entry.notes}", color = Color.Gray, fontSize = 12.sp)
            }
        }
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
        title = { Text("Новая запись", color = Color.White) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = title, 
                    onValueChange = { title = it }, 
                    label = { Text("Название") }, 
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                )
                OutlinedTextField(
                    value = username, 
                    onValueChange = { username = it }, 
                    label = { Text("Логин/Email") }, 
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(
                        value = password, 
                        onValueChange = { password = it }, 
                        label = { Text("Пароль") },
                        modifier = Modifier.weight(1f),
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                    )
                    IconButton(onClick = { password = vm.generatePassword() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Generate", tint = Color(0xFF00FFAA))
                    }
                }
                OutlinedTextField(
                    value = notes, 
                    onValueChange = { notes = it }, 
                    label = { Text("Заметки") }, 
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    vm.addEntry(title, username, password, notes)
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(Color(0xFF00FFAA))
            ) {
                Text("Сохранить", color = Color.Black)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Отмена", color = Color.Gray) }
        },
        containerColor = Color(0xFF12121A)
    )
}
