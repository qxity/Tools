package id.friendtools.facebook

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val FacebookBlue = Color(0xFF1877F2)
private val DeepBlue = Color(0xFF0D47A1)
private val DarkBg = Color(0xFF0B1220)
private val DarkCard = Color(0xFF151F30)

data class FriendEntry(val label: String, val profileUrl: String, val status: String, val time: String)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            FriendToolsTheme {
                FriendToolsApp(
                    onOpenFacebook = { openUrl("https://www.facebook.com/friends/suggestions") },
                    onOpenUrl = { url -> openUrl(url) }
                )
            }
        }
    }

    private fun openUrl(url: String) {
        runCatching {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
        }
    }
}

@Composable
private fun FriendToolsTheme(content: @Composable () -> Unit) {
    var dark by remember { mutableStateOf(true) }
    val scheme = if (dark) darkColorScheme(
        primary = FacebookBlue,
        secondary = Color(0xFF8AB4F8),
        background = DarkBg,
        surface = DarkCard,
        onBackground = Color(0xFFEAF0FA),
        onSurface = Color(0xFFEAF0FA)
    ) else lightColorScheme(primary = FacebookBlue, secondary = DeepBlue)
    MaterialTheme(colorScheme = scheme) {
        Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
            content()
        }
    }
}

@Composable
private fun FriendToolsApp(onOpenFacebook: () -> Unit, onOpenUrl: (String) -> Unit) {
    var profileUrl by remember { mutableStateOf("") }
    var profileLabel by remember { mutableStateOf("") }
    var showAddDialog by remember { mutableStateOf(false) }
    var selectedTab by remember { mutableIntStateOf(0) }
    var entries by remember { mutableStateOf(listOf<FriendEntry>()) }
    var notice by remember { mutableStateOf<String?>(null) }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            Row(
                Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.surface).padding(horizontal = 18.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(Modifier.size(42.dp).background(FacebookBlue, CircleShape), contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.People, null, tint = Color.White, modifier = Modifier.size(25.dp))
                }
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text("Friend Tools", fontSize = 21.sp, fontWeight = FontWeight.Bold)
                    Text("Facebook • Companion", color = MaterialTheme.colorScheme.onSurface.copy(alpha = .65f), fontSize = 12.sp)
                }
                Icon(Icons.Default.Shield, null, tint = Color(0xFF4ADE80))
            }
        },
        bottomBar = {
            NavigationBar(containerColor = MaterialTheme.colorScheme.surface) {
                NavigationBarItem(selected = selectedTab == 0, onClick = { selectedTab = 0 }, icon = { Icon(Icons.Default.Dashboard, null) }, label = { Text("Beranda") })
                NavigationBarItem(selected = selectedTab == 1, onClick = { selectedTab = 1 }, icon = { Icon(Icons.Default.Bookmark, null) }, label = { Text("Daftar") })
                NavigationBarItem(selected = selectedTab == 2, onClick = { selectedTab = 2 }, icon = { Icon(Icons.Default.History, null) }, label = { Text("Riwayat") })
            }
        },
        floatingActionButton = {
            if (selectedTab == 1) FloatingActionButton(onClick = { showAddDialog = true }, containerColor = FacebookBlue) {
                Icon(Icons.Default.Add, contentDescription = "Tambah profil", tint = Color.White)
            }
        },
        snackbarHost = { SnackbarHost(remember { SnackbarHostState() }) }
    ) { padding ->
        when (selectedTab) {
            0 -> HomeScreen(
                modifier = Modifier.padding(padding),
                total = entries.size,
                reviewed = entries.count { it.status != "Menunggu" },
                onOpenFacebook = onOpenFacebook,
                onAdd = { selectedTab = 1; showAddDialog = true }
            )
            1 -> ListScreen(
                modifier = Modifier.padding(padding),
                entries = entries,
                onOpen = { entry ->
                    onOpenUrl(entry.profileUrl)
                    entries = entries.map { if (it == entry) it.copy(status = "Dibuka", time = now()) else it }
                    notice = "Profil dibuka. Kirim permintaan hanya jika kamu sudah meninjau profilnya."
                },
                onMarkSent = { entry ->
                    entries = entries.map { if (it == entry) it.copy(status = "Ditandai terkirim", time = now()) else it }
                },
                onDelete = { entry -> entries = entries.filterNot { it == entry } }
            )
            else -> HistoryScreen(Modifier.padding(padding), entries)
        }
    }

    if (showAddDialog) {
        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("Tambah profil") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Masukkan nama label dan tautan profil yang kamu miliki. Aplikasi tidak mengirim permintaan otomatis.")
                    OutlinedTextField(value = profileLabel, onValueChange = { profileLabel = it }, label = { Text("Nama / label") }, singleLine = true)
                    OutlinedTextField(value = profileUrl, onValueChange = { profileUrl = it }, label = { Text("URL profil Facebook") }, singleLine = true)
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    val url = profileUrl.trim()
                    if (profileLabel.isNotBlank() && (url.startsWith("https://www.facebook.com/") || url.startsWith("https://m.facebook.com/"))) {
                        entries = entries + FriendEntry(profileLabel.trim(), url, "Menunggu", now())
                        profileLabel = ""; profileUrl = ""; showAddDialog = false
                    } else {
                        notice = "Isi label dan gunakan URL Facebook yang valid."
                    }
                }) { Text("Simpan") }
            },
            dismissButton = { TextButton(onClick = { showAddDialog = false }) { Text("Batal") } }
        )
    }
    notice?.let { msg ->
        AlertDialog(onDismissRequest = { notice = null }, confirmButton = { TextButton(onClick = { notice = null }) { Text("OK") } }, title = { Text("Info") }, text = { Text(msg) })
    }
}

@Composable
private fun HomeScreen(modifier: Modifier, total: Int, reviewed: Int, onOpenFacebook: () -> Unit, onAdd: () -> Unit) {
    LazyColumn(modifier.fillMaxSize().padding(horizontal = 18.dp), verticalArrangement = Arrangement.spacedBy(16.dp), contentPadding = PaddingValues(top = 18.dp, bottom = 24.dp)) {
        item {
            Column {
                Text("Kelola koneksi dengan lebih rapi", fontSize = 25.sp, fontWeight = FontWeight.Bold, lineHeight = 31.sp)
                Spacer(Modifier.height(6.dp))
                Text("Asisten daftar profil dan navigasi. Semua permintaan pertemanan tetap kamu konfirmasi di Facebook.", color = MaterialTheme.colorScheme.onBackground.copy(alpha = .7f))
            }
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                StatCard("Daftar profil", total.toString(), Icons.Default.People, Modifier.weight(1f))
                StatCard("Sudah ditinjau", reviewed.toString(), Icons.Default.CheckCircle, Modifier.weight(1f))
            }
        }
        item {
            Card(shape = RoundedCornerShape(22.dp), colors = CardDefaults.cardColors(containerColor = FacebookBlue)) {
                Column(Modifier.fillMaxWidth().padding(20.dp)) {
                    Icon(Icons.Default.People, null, tint = Color.White, modifier = Modifier.size(30.dp))
                    Spacer(Modifier.height(12.dp))
                    Text("Buka saran teman", color = Color.White, fontSize = 21.sp, fontWeight = FontWeight.Bold)
                    Text("Lanjutkan proses di Facebook dan konfirmasi setiap tindakan secara manual.", color = Color.White.copy(alpha = .85f))
                    Spacer(Modifier.height(18.dp))
                    Button(onClick = onOpenFacebook, colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = FacebookBlue)) {
                        Text("Buka Facebook")
                        Spacer(Modifier.width(8.dp))
                        Icon(Icons.Default.OpenInNew, null)
                    }
                }
            }
        }
        item {
            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), shape = RoundedCornerShape(18.dp)) {
                Column(Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Security, null, tint = Color(0xFF4ADE80))
                        Spacer(Modifier.width(8.dp))
                        Text("Penggunaan yang lebih aman", fontWeight = FontWeight.SemiBold)
                    }
                    Spacer(Modifier.height(8.dp))
                    Text("Tidak meminta kata sandi Facebook, tidak mengakses akun di latar belakang, dan tidak mengirim permintaan otomatis.", color = MaterialTheme.colorScheme.onSurface.copy(alpha = .72f))
                }
            }
        }
        item {
            OutlinedButton(onClick = onAdd, modifier = Modifier.fillMaxWidth()) {
                Icon(Icons.Default.Add, null)
                Spacer(Modifier.width(8.dp))
                Text("Tambahkan profil ke daftar")
            }
        }
    }
}

@Composable
private fun StatCard(title: String, value: String, icon: androidx.compose.ui.graphics.vector.ImageVector, modifier: Modifier) {
    Card(modifier, shape = RoundedCornerShape(18.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
        Column(Modifier.padding(16.dp)) {
            Icon(icon, null, tint = FacebookBlue)
            Spacer(Modifier.height(14.dp))
            Text(value, fontSize = 28.sp, fontWeight = FontWeight.Bold)
            Text(title, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = .7f))
        }
    }
}

@Composable
private fun ListScreen(
    modifier: Modifier,
    entries: List<FriendEntry>,
    onOpen: (FriendEntry) -> Unit,
    onMarkSent: (FriendEntry) -> Unit,
    onDelete: (FriendEntry) -> Unit
) {
    LazyColumn(modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp), contentPadding = PaddingValues(bottom = 20.dp)) {
        item {
            Text("Daftar profil", fontSize = 24.sp, fontWeight = FontWeight.Bold)
            Text("Tambahkan profil secara manual melalui tombol +.", color = MaterialTheme.colorScheme.onBackground.copy(alpha = .65f))
        }
        if (entries.isEmpty()) item {
            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                Column(Modifier.fillMaxWidth().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.PersonSearch, null, modifier = Modifier.size(44.dp), tint = FacebookBlue)
                    Spacer(Modifier.height(8.dp))
                    Text("Belum ada profil")
                    Text("Tekan + untuk menambahkan URL profil.", color = MaterialTheme.colorScheme.onSurface.copy(alpha = .65f))
                }
            }
        }
        items(entries) { entry ->
            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), shape = RoundedCornerShape(18.dp)) {
                Column(Modifier.fillMaxWidth().padding(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(Modifier.size(42.dp).background(FacebookBlue.copy(alpha = .18f), CircleShape), contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.Person, null, tint = FacebookBlue)
                        }
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text(entry.label, fontWeight = FontWeight.SemiBold)
                            Text(entry.status, color = MaterialTheme.colorScheme.onSurface.copy(alpha = .65f), fontSize = 12.sp)
                        }
                        IconButton(onClick = { onDelete(entry) }) { Icon(Icons.Default.DeleteOutline, "Hapus") }
                    }
                    Spacer(Modifier.height(8.dp))
                    Text(entry.profileUrl, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = .6f))
                    Spacer(Modifier.height(10.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(onClick = { onOpen(entry) }, modifier = Modifier.weight(1f)) {
                            Icon(Icons.Default.OpenInNew, null)
                            Spacer(Modifier.width(5.dp))
                            Text("Tinjau")
                        }
                        OutlinedButton(onClick = { onMarkSent(entry) }, modifier = Modifier.weight(1f)) {
                            Icon(Icons.Default.Check, null)
                            Spacer(Modifier.width(5.dp))
                            Text("Tandai terkirim")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun HistoryScreen(modifier: Modifier, entries: List<FriendEntry>) {
    Column(modifier.fillMaxSize().padding(18.dp)) {
        Text("Riwayat aktivitas", fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(6.dp))
        Text("Catatan pada sesi ini. Penyimpanan permanen dan ekspor CSV dapat ditambahkan pada iterasi berikutnya.", color = MaterialTheme.colorScheme.onBackground.copy(alpha = .7f))
        Spacer(Modifier.height(16.dp))
        if (entries.isEmpty()) {
            Text("Belum ada aktivitas.")
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items(entries.reversed()) { entry ->
                    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                        Row(Modifier.fillMaxWidth().padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.History, null, tint = FacebookBlue)
                            Spacer(Modifier.width(12.dp))
                            Column(Modifier.weight(1f)) {
                                Text(entry.label, fontWeight = FontWeight.SemiBold)
                                Text("${entry.status} • ${entry.time}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = .65f))
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun now(): String = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale("id", "ID")).format(Date())
