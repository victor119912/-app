package com.example.myapplication

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas as AndroidCanvas
import android.graphics.Color as AndroidColor
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccountCircle
import androidx.compose.material.icons.rounded.Analytics
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.ConfirmationNumber
import androidx.compose.material.icons.rounded.DeleteForever
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.EventSeat
import androidx.compose.material.icons.rounded.FilterList
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.LocalActivity
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Logout
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material.icons.rounded.Mail
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.OpenInNew
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Sell
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material.icons.rounded.Sort
import androidx.compose.material.icons.rounded.Stars
import androidx.compose.material.icons.rounded.TrendingUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import com.example.myapplication.ui.theme.MyApplicationTheme
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.net.URLEncoder
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

private const val API_BASE_URL = "http://10.0.2.2:4000"

private val Canvas = Color(0xFFF6F1E8)
private val SurfaceIvory = Color(0xFFFFFCF6)
private val StageBlack = Color(0xFF08111F)
private val Ink = Color(0xFF111827)
private val Muted = Color(0xFF6B7280)
private val Mist = Color(0xFFE9DFD1)
private val FineLine = Color(0xFFD8CBBE)
private val Porcelain = Color(0xFFFFFFFF)
private val Gold = Color(0xFFD8A84E)
private val GoldSoft = Color(0xFFFFF2CF)
private val Cobalt = Color(0xFF245CFF)
private val CobaltSoft = Color(0xFFE8EEFF)
private val Success = Color(0xFF0F8A5F)
private val Rose = Color(0xFFD64545)
private val Plum = Color(0xFF7C3AED)

class MainActivity : ComponentActivity() {
    private val client = OkHttpClient()
    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()
    private var activeToken: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = AndroidColor.rgb(246, 241, 232)
        window.navigationBarColor = AndroidColor.rgb(255, 252, 246)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            var flags = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                flags = flags or View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR
            }
            window.decorView.systemUiVisibility = flags
        }

        setContent {
            MyApplicationTheme {
                TicketFlowApp()
            }
        }
    }

    @Composable
    private fun TicketFlowApp() {
        var selectedTab by remember { mutableStateOf(AppTab.Home) }
        var events by remember { mutableStateOf<List<EventItem>>(emptyList()) }
        var reminders by remember { mutableStateOf<List<ReminderItem>>(emptyList()) }
        var summary by remember { mutableStateOf(SummaryStats()) }
        var priceStats by remember { mutableStateOf(PriceStats()) }
        var timeStats by remember { mutableStateOf(TimeStats()) }
        var venueStats by remember { mutableStateOf(VenueStats()) }
        var walletTickets by remember { mutableStateOf(loadWalletTickets()) }
        var keyword by remember { mutableStateOf("") }
        var loading by remember { mutableStateOf(false) }
        var session by remember { mutableStateOf(loadSavedSession()) }
        val currentSession = session
        activeToken = currentSession?.token.orEmpty()

        fun refreshHome() {
            loading = true
            loadEvents(limit = 24, keyword = "", featured = true) {
                events = it
                loading = false
            }
            loadSummary { summary = it }
            loadReminders { reminders = it }
        }

        fun refreshAnalysis() {
            loadSummary { summary = it }
            loadPriceStats { priceStats = it }
            loadTimeStats { timeStats = it }
            loadVenueStats { venueStats = it }
        }

        LaunchedEffect(currentSession?.token) {
            if (currentSession != null) {
                checkHealth(
                    onSuccess = {
                        refreshHome()
                        refreshAnalysis()
                    },
                    onError = {
                        loading = false
                        toast("目前無法取得資料，請確認服務已啟動")
                    }
                )
            }
        }

        if (currentSession == null) {
            Surface(modifier = Modifier.fillMaxSize(), color = Canvas) {
                AuthScreen(
                    onAuthenticated = { newSession ->
                        saveSession(newSession)
                        session = newSession
                        selectedTab = AppTab.Home
                    }
                )
            }
            return
        }

        Surface(modifier = Modifier.fillMaxSize(), color = Canvas) {
            Scaffold(
                containerColor = Canvas,
                bottomBar = {
                    TicketBottomBar(
                        selectedTab = selectedTab,
                        onSelect = { tab ->
                            selectedTab = tab
                            when (tab) {
                                AppTab.Home -> refreshHome()
                                AppTab.Search -> Unit
                                AppTab.Wallet -> Unit
                                AppTab.Reminders -> loadReminders { reminders = it }
                                AppTab.Analysis -> refreshAnalysis()
                                AppTab.Account -> Unit
                            }
                        }
                    )
                }
            ) { padding ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                ) {
                    when (selectedTab) {
                        AppTab.Home -> HomeScreen(
                            summary = summary,
                            events = events,
                            loading = loading,
                            onRefresh = { refreshHome() },
                            onOpenUrl = { openEventUrl(it) },
                            onAddReminder = { event ->
                                addReminder(event) {
                                    toast("已加入你的搶票提醒")
                                    loadReminders { reminders = it }
                                }
                            }
                        )

                        AppTab.Search -> SearchScreen(
                            keyword = keyword,
                            onKeywordChange = { keyword = it },
                            events = events,
                            loading = loading,
                            onQuickSearch = { quickKeyword ->
                                keyword = quickKeyword
                                loading = true
                                loadEvents(limit = 80, keyword = quickKeyword, featured = false) {
                                    events = it
                                    loading = false
                                }
                            },
                            onSearch = {
                                loading = true
                                loadEvents(limit = 100, keyword = keyword, featured = false) {
                                    events = it
                                    loading = false
                                }
                            },
                            onOpenUrl = { openEventUrl(it) },
                            onAddReminder = { event ->
                                addReminder(event) {
                                    toast("已加入你的搶票提醒")
                                    loadReminders { reminders = it }
                                }
                            }
                        )

                        AppTab.Wallet -> WalletScreen(
                            tickets = walletTickets,
                            onSave = { ticket ->
                                walletTickets = listOf(ticket) + walletTickets
                                saveWalletTickets(walletTickets)
                                toast("票券已加入票券夾")
                            },
                            onUpdate = { ticket ->
                                walletTickets = walletTickets.map { if (it.id == ticket.id) ticket else it }
                                saveWalletTickets(walletTickets)
                                toast("票券已更新")
                            },
                            onDelete = { ticket ->
                                walletTickets = walletTickets.filterNot { it.id == ticket.id }
                                saveWalletTickets(walletTickets)
                                toast("票券已移除")
                            },
                            onShare = { ticket ->
                                shareWalletTicket(ticket)
                            }
                        )

                        AppTab.Reminders -> RemindersScreen(
                            reminders = reminders,
                            onRefresh = { loadReminders { reminders = it } },
                            onDelete = { reminder ->
                                deleteReminder(reminder.id) {
                                    toast("提醒已移除")
                                    loadReminders { reminders = it }
                                }
                            }
                        )

                        AppTab.Analysis -> AnalysisScreen(
                            summary = summary,
                            priceStats = priceStats,
                            timeStats = timeStats,
                            venueStats = venueStats,
                            onRefresh = { refreshAnalysis() }
                        )

                        AppTab.Account -> AccountScreen(
                            session = currentSession,
                            onLogout = {
                                clearSession()
                                activeToken = ""
                                session = null
                                reminders = emptyList()
                                selectedTab = AppTab.Home
                            },
                            onDeleteAccount = { password ->
                                deleteAccount(
                                    password = password,
                                    onSuccess = {
                                        clearSession()
                                        activeToken = ""
                                        session = null
                                        reminders = emptyList()
                                        selectedTab = AppTab.Home
                                        toast("帳號已註銷")
                                    },
                                    onError = { toast("註銷失敗，請確認密碼") }
                                )
                            }
                        )
                    }
                }
            }
        }
    }

    @Composable
    private fun AuthScreen(onAuthenticated: (AuthSession) -> Unit) {
        var mode by remember { mutableStateOf(AuthMode.Login) }
        var email by remember { mutableStateOf("") }
        var password by remember { mutableStateOf("") }
        var confirmPassword by remember { mutableStateOf("") }
        var submitting by remember { mutableStateOf(false) }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 32.dp),
            verticalArrangement = Arrangement.Center
        ) {
            PageTitle(
                title = "登入 Ticket Flow",
                subtitle = if (mode == AuthMode.Login) "使用你的帳號進入 App" else "註冊後即可同步提醒資料"
            )
            Spacer(modifier = Modifier.height(18.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedButton(
                    onClick = { mode = AuthMode.Login },
                    border = BorderStroke(1.dp, if (mode == AuthMode.Login) Cobalt else FineLine)
                ) {
                    Text("登入", color = if (mode == AuthMode.Login) Cobalt else Ink)
                }
                OutlinedButton(
                    onClick = { mode = AuthMode.Register },
                    border = BorderStroke(1.dp, if (mode == AuthMode.Register) Cobalt else FineLine)
                ) {
                    Text("註冊", color = if (mode == AuthMode.Register) Cobalt else Ink)
                }
            }
            Spacer(modifier = Modifier.height(20.dp))
            Card(
                colors = CardDefaults.cardColors(containerColor = SurfaceIvory),
                shape = RoundedCornerShape(24.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("Email") },
                        placeholder = { Text("name@example.com") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        leadingIcon = { Icon(Icons.Rounded.Mail, contentDescription = null) },
                        colors = authTextFieldColors(),
                        shape = RoundedCornerShape(18.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("密碼") },
                        singleLine = true,
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        leadingIcon = { Icon(Icons.Rounded.Lock, contentDescription = null) },
                        colors = authTextFieldColors(),
                        shape = RoundedCornerShape(18.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                    if (mode == AuthMode.Register) {
                        OutlinedTextField(
                            value = confirmPassword,
                            onValueChange = { confirmPassword = it },
                            label = { Text("確認密碼") },
                            singleLine = true,
                            visualTransformation = PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                            leadingIcon = { Icon(Icons.Rounded.Lock, contentDescription = null) },
                            colors = authTextFieldColors(),
                            shape = RoundedCornerShape(18.dp),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    Button(
                        onClick = {
                            val finalEmail = email.trim()
                            val finalPassword = password.trim()
                            if (finalEmail.isEmpty() || finalPassword.isEmpty()) {
                                toast("請輸入 email 和密碼")
                                return@Button
                            }
                            if (mode == AuthMode.Register && finalPassword != confirmPassword.trim()) {
                                toast("兩次密碼不一致")
                                return@Button
                            }
                            submitting = true
                            val payload = JSONObject()
                                .put("email", finalEmail)
                                .put("password", finalPassword)
                                .toString()
                            val path = if (mode == AuthMode.Login) "/api/auth/login" else "/api/auth/register"
                            post(
                                path = path,
                                body = payload,
                                onSuccess = { body ->
                                    submitting = false
                                    val authSession = JSONObject(body).toAuthSession()
                                    onAuthenticated(authSession)
                                    toast(if (mode == AuthMode.Login) "登入成功" else "註冊成功")
                                },
                                onError = { body ->
                                    submitting = false
                                    toast(parseApiError(body, if (mode == AuthMode.Login) "登入失敗" else "註冊失敗"))
                                }
                            )
                        },
                        enabled = !submitting,
                        colors = ButtonDefaults.buttonColors(containerColor = Cobalt),
                        shape = RoundedCornerShape(18.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(if (submitting) "處理中..." else if (mode == AuthMode.Login) "登入" else "建立帳號")
                    }
                }
            }
        }
    }

    @Composable
    private fun AccountScreen(
        session: AuthSession,
        onLogout: () -> Unit,
        onDeleteAccount: (String) -> Unit
    ) {
        var showDeleteDialog by remember { mutableStateOf(false) }
        var confirmPassword by remember { mutableStateOf("") }

        if (showDeleteDialog) {
            AlertDialog(
                onDismissRequest = {
                    showDeleteDialog = false
                    confirmPassword = ""
                },
                title = { Text("註銷帳號") },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text("註銷後，這個帳號的提醒資料會一起刪除。")
                        OutlinedTextField(
                            value = confirmPassword,
                            onValueChange = { confirmPassword = it },
                            label = { Text("請輸入密碼確認") },
                            singleLine = true,
                            visualTransformation = PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                            colors = authTextFieldColors(),
                            shape = RoundedCornerShape(18.dp)
                        )
                    }
                },
                confirmButton = {
                    TextButton(onClick = {
                        onDeleteAccount(confirmPassword)
                        showDeleteDialog = false
                        confirmPassword = ""
                    }) {
                        Text("確認註銷", color = Rose)
                    }
                },
                dismissButton = {
                    TextButton(onClick = {
                        showDeleteDialog = false
                        confirmPassword = ""
                    }) {
                        Text("取消")
                    }
                }
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 18.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            PageTitle(title = "帳號", subtitle = "管理登入狀態與帳號安全")
            Card(
                colors = CardDefaults.cardColors(containerColor = SurfaceIvory),
                shape = RoundedCornerShape(24.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    InfoLine(Icons.Rounded.Mail, "Email", session.email)
                    InfoLine(Icons.Rounded.Lock, "狀態", "已登入")
                }
            }
            Button(
                onClick = onLogout,
                colors = ButtonDefaults.buttonColors(containerColor = Ink),
                shape = RoundedCornerShape(18.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Rounded.Logout, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("登出")
            }
            OutlinedButton(
                onClick = { showDeleteDialog = true },
                border = BorderStroke(1.dp, Rose),
                shape = RoundedCornerShape(18.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Rounded.DeleteForever, contentDescription = null, tint = Rose)
                Spacer(modifier = Modifier.width(8.dp))
                Text("註銷帳號", color = Rose)
            }
        }
    }

    @Composable
    private fun TicketBottomBar(selectedTab: AppTab, onSelect: (AppTab) -> Unit) {
        NavigationBar(
            containerColor = SurfaceIvory,
            tonalElevation = 0.dp
        ) {
            AppTab.entries.forEach { tab ->
                NavigationBarItem(
                    selected = selectedTab == tab,
                    onClick = { onSelect(tab) },
                    icon = {
                        Icon(
                            imageVector = tab.icon,
                            contentDescription = tab.title,
                            modifier = Modifier.size(24.dp)
                        )
                    },
                    label = { Text(tab.title, fontSize = 12.sp, fontWeight = FontWeight.SemiBold) }
                    ,
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = StageBlack,
                        selectedTextColor = StageBlack,
                        indicatorColor = GoldSoft,
                        unselectedIconColor = Muted,
                        unselectedTextColor = Muted
                    )
                )
            }
        }
    }

    @Composable
    private fun HomeScreen(
        summary: SummaryStats,
        events: List<EventItem>,
        loading: Boolean,
        onRefresh: () -> Unit,
        onOpenUrl: (EventItem) -> Unit,
        onAddReminder: (EventItem) -> Unit
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item { Spacer(modifier = Modifier.height(16.dp)) }
            item { StageHero(summary) }
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    MetricCard("活動資料", summary.events.toString(), "筆整合活動", Icons.Rounded.LocalActivity, Modifier.weight(1f))
                    MetricCard("藝人檔案", summary.artists.toString(), "組演出藝人", Icons.Rounded.Stars, Modifier.weight(1f))
                }
            }
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    MetricCard("合作場地", summary.venues.toString(), "個活動場館", Icons.Rounded.LocationOn, Modifier.weight(1f))
                    MetricCard("搶票提醒", summary.reminders.toString(), "筆追蹤提醒", Icons.Rounded.Notifications, Modifier.weight(1f))
                }
            }
            item {
                SectionHeader(
                    title = "精選售票活動",
                    subtitle = "依資料完整度與近期活動排序，先看最適合展示的內容。",
                    action = "更新",
                    actionIcon = Icons.Rounded.Refresh,
                    onAction = onRefresh
                )
            }
            if (loading) {
                item { LoadingCard("正在整理推薦活動") }
            }
            items(events) { event ->
                EventCard(
                    event = event,
                    onOpenUrl = { onOpenUrl(event) },
                    onAddReminder = { onAddReminder(event) }
                )
            }
            item { Spacer(modifier = Modifier.height(16.dp)) }
        }
    }

    @Composable
    private fun StageHero(summary: SummaryStats) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(30.dp),
            colors = CardDefaults.cardColors(containerColor = StageBlack),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.10f))
        ) {
            Column(
                modifier = Modifier
                    .background(
                        Brush.linearGradient(
                            listOf(StageBlack, Color(0xFF10284B), Color(0xFF1F1710))
                        )
                    )
                    .padding(24.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .background(GoldSoft.copy(alpha = 0.16f), RoundedCornerShape(99.dp))
                            .padding(horizontal = 12.dp, vertical = 7.dp)
                    ) {
                        Text("TICKETFLOW", color = Gold, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.weight(1f))
                    Text("即時票務", color = Color.White.copy(alpha = 0.70f), fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                }
                Spacer(modifier = Modifier.height(22.dp))
                Text(
                    "把分散的售票資訊\n整理成一個入口",
                    color = Color.White,
                    fontSize = 30.sp,
                    fontWeight = FontWeight.Black,
                    lineHeight = 37.sp
                )
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    "從活動搜尋、票價洞察到開賣提醒，讓老師一眼看懂這不是清單，而是一套完整票務服務。",
                    color = Color.White.copy(alpha = 0.82f),
                    fontSize = 14.sp,
                    lineHeight = 22.sp
                )
                Spacer(modifier = Modifier.height(22.dp))
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    HeroPill("${summary.events} 場活動")
                    HeroPill("票價洞察")
                    HeroPill("提醒追蹤")
                }
            }
        }
    }

    @Composable
    private fun HeroPill(text: String) {
        Box(
            modifier = Modifier
                .background(Color.White.copy(alpha = 0.11f), RoundedCornerShape(99.dp))
                .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            Text(text, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
        }
    }

    @Composable
    private fun SearchScreen(
        keyword: String,
        onKeywordChange: (String) -> Unit,
        events: List<EventItem>,
        loading: Boolean,
        onQuickSearch: (String) -> Unit,
        onSearch: () -> Unit,
        onOpenUrl: (EventItem) -> Unit,
        onAddReminder: (EventItem) -> Unit
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 18.dp)
        ) {
            Spacer(modifier = Modifier.height(18.dp))
            FeatureHeader(
                eyebrow = "探索中心",
                title = "找到下一場想去的演出",
                subtitle = "用活動、藝人、城市或場地搜尋，快速收斂到可購票的資訊。",
                icon = Icons.Rounded.Search,
                accent = Cobalt
            )
            Spacer(modifier = Modifier.height(14.dp))
            Card(
                colors = CardDefaults.cardColors(containerColor = SurfaceIvory),
                shape = RoundedCornerShape(26.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                border = BorderStroke(1.dp, FineLine.copy(alpha = 0.72f))
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    OutlinedTextField(
                        value = keyword,
                        onValueChange = onKeywordChange,
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("搜尋關鍵字") },
                        placeholder = { Text("例如：台北、Zepp、小巨蛋") },
                        singleLine = true,
                        shape = RoundedCornerShape(18.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Cobalt,
                            unfocusedBorderColor = Mist,
                            cursorColor = Cobalt,
                            focusedLabelColor = Cobalt,
                            focusedContainerColor = Porcelain,
                            unfocusedContainerColor = Porcelain
                        )
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = onSearch,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape = RoundedCornerShape(18.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = StageBlack)
                    ) {
                        Icon(Icons.Rounded.Search, contentDescription = null, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("搜尋售票資訊", fontWeight = FontWeight.Bold)
                    }
                }
            }
            Spacer(modifier = Modifier.height(14.dp))
            Text("熱門關鍵字", color = Ink, fontSize = 13.sp, fontWeight = FontWeight.Black)
            Spacer(modifier = Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("台北", "Zepp", "小巨蛋").forEach { item ->
                    OutlinedButton(
                        onClick = { onQuickSearch(item) },
                        shape = RoundedCornerShape(99.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Ink)
                    ) {
                        Text(item, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text("搜尋結果 ${events.size} 筆", color = Muted, fontSize = 13.sp)
            Spacer(modifier = Modifier.height(8.dp))
            if (loading) {
                LoadingCard("正在搜尋資料庫")
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (events.isEmpty()) {
                        item {
                            val emptyTitle = if (keyword.isBlank()) "開始探索" else "沒有符合的活動"
                            val emptyMessage = if (keyword.isBlank()) {
                                "輸入關鍵字，或點選上方快速篩選。"
                            } else {
                                "目前找不到「$keyword」，可以換成藝人、城市或場地名稱再試一次。"
                            }
                            EmptyStateCard(emptyTitle, emptyMessage)
                        }
                    }
                    items(events) { event ->
                        EventCard(
                            event = event,
                            onOpenUrl = { onOpenUrl(event) },
                            onAddReminder = { onAddReminder(event) }
                        )
                    }
                    item { Spacer(modifier = Modifier.height(16.dp)) }
                }
            }
        }
    }

    @Composable
    private fun WalletScreen(
        tickets: List<WalletTicket>,
        onSave: (WalletTicket) -> Unit,
        onUpdate: (WalletTicket) -> Unit,
        onDelete: (WalletTicket) -> Unit,
        onShare: (WalletTicket) -> Unit
    ) {
        var showAddDialog by remember { mutableStateOf(false) }
        var editingTicket by remember { mutableStateOf<WalletTicket?>(null) }
        var keyword by remember { mutableStateOf("") }
        var sortOption by remember { mutableStateOf(WalletSort.DateNear) }
        var platformFilter by remember { mutableStateOf("全部") }
        var showCalendar by remember { mutableStateOf(false) }
        var selectedDateKey by remember { mutableStateOf<String?>(null) }
        var focusedMonth by remember {
            mutableStateOf(Calendar.getInstance().apply {
                set(Calendar.DAY_OF_MONTH, 1)
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            })
        }
        val platforms = tickets
            .map { it.platform.trim() }
            .filter { it.isNotEmpty() }
            .distinct()
            .sorted()
        val filteredTickets = tickets
            .filter { matchesWalletQuery(it, keyword) }
            .filter { platformFilter == "全部" || it.platform == platformFilter }
            .let { list ->
                when (sortOption) {
                    WalletSort.DateNear -> list.sortedBy { parseWalletTimeMillis(it.date) }
                    WalletSort.DateFar -> list.sortedByDescending { parseWalletTimeMillis(it.date) }
                    WalletSort.PriceHigh -> list.sortedByDescending { it.price.toIntOrNull() ?: -1 }
                    WalletSort.PriceLow -> list.sortedBy { it.price.toIntOrNull() ?: Int.MAX_VALUE }
                }
            }
        val visibleTickets = if (showCalendar) {
            val selected = selectedDateKey
            if (selected == null) {
                filteredTickets.filter { isSameWalletMonth(it, focusedMonth) }
            } else {
                filteredTickets.filter { walletDateKey(it.date) == selected }
            }
        } else {
            filteredTickets
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item { Spacer(modifier = Modifier.height(18.dp)) }
            item {
                FeatureHeader(
                    eyebrow = "個人票券",
                    title = "票券夾",
                    subtitle = "把已購入或想保存的票券集中管理，支援活動、日期、場地、座位與票價紀錄。",
                    icon = Icons.Rounded.ConfirmationNumber,
                    accent = Gold,
                    action = "新增",
                    actionIcon = Icons.Rounded.LocalActivity,
                    onAction = { showAddDialog = true }
                )
            }
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = SurfaceIvory),
                    shape = RoundedCornerShape(24.dp),
                    border = BorderStroke(1.dp, FineLine.copy(alpha = 0.70f))
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedTextField(
                            value = keyword,
                            onValueChange = { keyword = it },
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text("搜尋票券") },
                            placeholder = { Text("活動、場地、座位、平台") },
                            singleLine = true,
                            leadingIcon = { Icon(Icons.Rounded.Search, contentDescription = null) },
                            shape = RoundedCornerShape(16.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Cobalt,
                                unfocusedBorderColor = Mist,
                                cursorColor = Cobalt,
                                focusedLabelColor = Cobalt,
                                focusedContainerColor = Porcelain,
                                unfocusedContainerColor = Porcelain
                            )
                        )
                        Row(
                            modifier = Modifier.horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = {
                                    showCalendar = !showCalendar
                                    selectedDateKey = null
                                },
                                shape = RoundedCornerShape(99.dp),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = Ink)
                            ) {
                                Icon(Icons.Rounded.CalendarMonth, contentDescription = null, modifier = Modifier.size(17.dp))
                                Spacer(modifier = Modifier.width(5.dp))
                                Text(if (showCalendar) "清單" else "月曆", fontWeight = FontWeight.SemiBold)
                            }
                            OutlinedButton(
                                onClick = { sortOption = sortOption.next() },
                                shape = RoundedCornerShape(99.dp),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = Ink)
                            ) {
                                Icon(Icons.Rounded.Sort, contentDescription = null, modifier = Modifier.size(17.dp))
                                Spacer(modifier = Modifier.width(5.dp))
                                Text(sortOption.label, fontWeight = FontWeight.SemiBold)
                            }
                            OutlinedButton(
                                onClick = { platformFilter = nextPlatformFilter(platformFilter, platforms) },
                                shape = RoundedCornerShape(99.dp),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = Ink)
                            ) {
                                Icon(Icons.Rounded.FilterList, contentDescription = null, modifier = Modifier.size(17.dp))
                                Spacer(modifier = Modifier.width(5.dp))
                                Text(platformFilter, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                }
            }
            if (showCalendar) {
                item {
                    WalletCalendarCard(
                        month = focusedMonth,
                        tickets = filteredTickets,
                        selectedDateKey = selectedDateKey,
                        onPrevious = {
                            focusedMonth = (focusedMonth.clone() as Calendar).apply { add(Calendar.MONTH, -1) }
                            selectedDateKey = null
                        },
                        onNext = {
                            focusedMonth = (focusedMonth.clone() as Calendar).apply { add(Calendar.MONTH, 1) }
                            selectedDateKey = null
                        },
                        onSelectDate = { selectedDateKey = it }
                    )
                }
                item {
                    SectionHeader(
                        title = selectedDateKey?.let { "當日票券 $it" } ?: "本月票券",
                        subtitle = selectedDateKey?.let { "顯示你在這一天保存的票券。" } ?: "點選月曆日期可以只看當天票券。",
                        action = "清除",
                        actionIcon = Icons.Rounded.Refresh,
                        onAction = {
                            selectedDateKey = null
                        }
                    )
                }
            }
            if (visibleTickets.isEmpty()) {
                item {
                    EmptyStateCard(
                        title = if (tickets.isEmpty()) "票券夾是空的" else "沒有符合的票券",
                        message = if (tickets.isEmpty()) "點選右上角新增，把你的票券資訊保存到這台裝置。" else "調整搜尋、排序、平台或月份條件後再試一次。"
                    )
                }
            }
            items(visibleTickets) { ticket ->
                WalletTicketCard(
                    ticket = ticket,
                    onEdit = { editingTicket = ticket },
                    onShare = { onShare(ticket) },
                    onDelete = { onDelete(ticket) }
                )
            }
            item { Spacer(modifier = Modifier.height(16.dp)) }
        }

        if (showAddDialog) {
            TicketFormDialog(
                ticket = null,
                onDismiss = { showAddDialog = false },
                onSave = { ticket ->
                    showAddDialog = false
                    onSave(ticket)
                }
            )
        }

        editingTicket?.let { ticket ->
            TicketFormDialog(
                ticket = ticket,
                onDismiss = { editingTicket = null },
                onSave = { updated ->
                    editingTicket = null
                    onUpdate(updated)
                }
            )
        }
    }

    @Composable
    private fun TicketFormDialog(
        ticket: WalletTicket?,
        onDismiss: () -> Unit,
        onSave: (WalletTicket) -> Unit
    ) {
        var title by remember(ticket?.id) { mutableStateOf(ticket?.title.orEmpty()) }
        var date by remember(ticket?.id) {
            mutableStateOf(ticket?.date ?: SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.TAIWAN).format(Calendar.getInstance().time))
        }
        var location by remember(ticket?.id) { mutableStateOf(ticket?.location.orEmpty()) }
        var seat by remember(ticket?.id) { mutableStateOf(ticket?.seat.orEmpty()) }
        var cast by remember(ticket?.id) { mutableStateOf(ticket?.cast.orEmpty()) }
        var platform by remember(ticket?.id) { mutableStateOf(ticket?.platform.orEmpty()) }
        var price by remember(ticket?.id) { mutableStateOf(ticket?.price.orEmpty()) }
        var notes by remember(ticket?.id) { mutableStateOf(ticket?.notes.orEmpty()) }
        fun showDateTimePicker() {
            val calendar = calendarFromWalletDate(date)
            DatePickerDialog(
                this,
                { _, year, month, dayOfMonth ->
                    calendar.set(Calendar.YEAR, year)
                    calendar.set(Calendar.MONTH, month)
                    calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                    TimePickerDialog(
                        this,
                        { _, hour, minute ->
                            calendar.set(Calendar.HOUR_OF_DAY, hour)
                            calendar.set(Calendar.MINUTE, minute)
                            date = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.TAIWAN).format(calendar.time)
                        },
                        calendar.get(Calendar.HOUR_OF_DAY),
                        calendar.get(Calendar.MINUTE),
                        true
                    ).show()
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            ).show()
        }

        AlertDialog(
            onDismissRequest = onDismiss,
            confirmButton = {
                Button(
                    onClick = {
                        if (title.trim().isEmpty()) {
                            toast("請輸入活動名稱")
                            return@Button
                        }
                        onSave(
                            WalletTicket(
                                id = ticket?.id ?: System.currentTimeMillis(),
                                title = title.trim(),
                                date = date.trim(),
                                location = location.trim(),
                                seat = seat.trim(),
                                cast = cast.trim(),
                                platform = platform.trim(),
                                price = price.trim(),
                                notes = notes.trim()
                            )
                        )
                    },
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = StageBlack)
                ) {
                    Text("儲存", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = onDismiss) {
                    Text("取消", color = Muted, fontWeight = FontWeight.SemiBold)
                }
            },
            title = {
                Text(if (ticket == null) "新增票券" else "編輯票券", color = Ink, fontWeight = FontWeight.Black)
            },
            text = {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    TicketInputField(title, { title = it }, "活動名稱", "例如：tuki. ASIA TOUR")
                    DateTimePickerField(
                        value = date,
                        onClick = { showDateTimePicker() }
                    )
                    TicketInputField(location, { location = it }, "場地", "例如：台北小巨蛋")
                    TicketInputField(seat, { seat = it }, "座位", "例如：A區 12排 8號")
                    TicketInputField(cast, { cast = it }, "演出者", "例如：tuki.")
                    TicketInputField(platform, { platform = it }, "售票平台", "例如：遠大售票")
                    TicketInputField(price, { price = it }, "票價", "例如：5280")
                    TicketInputField(notes, { notes = it }, "備註", "可填入入場提醒或付款狀態")
                }
            },
            containerColor = SurfaceIvory,
            shape = RoundedCornerShape(26.dp)
        )
    }

    @Composable
    private fun WalletCalendarCard(
        month: Calendar,
        tickets: List<WalletTicket>,
        selectedDateKey: String?,
        onPrevious: () -> Unit,
        onNext: () -> Unit,
        onSelectDate: (String) -> Unit
    ) {
        val days = walletCalendarDays(month)
        val ticketCounts = tickets.groupingBy { walletDateKey(it.date) }.eachCount()

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = SurfaceIvory),
            shape = RoundedCornerShape(26.dp),
            border = BorderStroke(1.dp, FineLine.copy(alpha = 0.70f))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    OutlinedButton(
                        onClick = onPrevious,
                        shape = RoundedCornerShape(99.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Ink)
                    ) {
                        Text("上月", fontWeight = FontWeight.SemiBold)
                    }
                    Text(
                        walletMonthTitle(month),
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center,
                        color = Ink,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black
                    )
                    OutlinedButton(
                        onClick = onNext,
                        shape = RoundedCornerShape(99.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Ink)
                    ) {
                        Text("下月", fontWeight = FontWeight.SemiBold)
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf("日", "一", "二", "三", "四", "五", "六").forEach {
                        Text(
                            it,
                            modifier = Modifier.weight(1f),
                            textAlign = TextAlign.Center,
                            color = Muted,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                days.chunked(7).forEach { week ->
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        week.forEach { day ->
                            val count = ticketCounts[day.key] ?: 0
                            val selected = day.key == selectedDateKey
                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp)
                                    .clickable(enabled = day.inMonth) { onSelectDate(day.key) }
                                    .background(
                                        when {
                                            selected -> StageBlack
                                            day.inMonth -> CobaltSoft.copy(alpha = if (count > 0) 0.90f else 0.28f)
                                            else -> Color.Transparent
                                        },
                                        RoundedCornerShape(14.dp)
                                    )
                                    .padding(vertical = 6.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    day.day.toString(),
                                    color = when {
                                        selected -> Color.White
                                        day.inMonth -> Ink
                                        else -> Muted.copy(alpha = 0.35f)
                                    },
                                    fontSize = 13.sp,
                                    fontWeight = if (count > 0) FontWeight.Black else FontWeight.SemiBold
                                )
                                if (count > 0) {
                                    Text(
                                        "$count 張",
                                        color = if (selected) Gold else Cobalt,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                }
            }
        }
    }

    @Composable
    private fun authTextFieldColors() = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = Cobalt,
        unfocusedBorderColor = Mist,
        cursorColor = Cobalt,
        focusedLabelColor = Cobalt,
        focusedContainerColor = Porcelain,
        unfocusedContainerColor = Porcelain
    )

    @Composable
    private fun TicketInputField(
        value: String,
        onValueChange: (String) -> Unit,
        label: String,
        placeholder: String
    ) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            label = { Text(label) },
            placeholder = { Text(placeholder) },
            singleLine = true,
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Cobalt,
                unfocusedBorderColor = Mist,
                cursorColor = Cobalt,
                focusedLabelColor = Cobalt,
                focusedContainerColor = Porcelain,
                unfocusedContainerColor = Porcelain
            )
        )
    }

    @Composable
    private fun DateTimePickerField(value: String, onClick: () -> Unit) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick),
            colors = CardDefaults.cardColors(containerColor = Porcelain),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, Mist)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Rounded.CalendarMonth, contentDescription = null, tint = Cobalt, modifier = Modifier.size(21.dp))
                Spacer(modifier = Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text("日期時間", color = Cobalt, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(value, color = Ink, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                }
                Text("選擇", color = Muted, fontSize = 13.sp, fontWeight = FontWeight.Bold)
            }
        }
    }

    @Composable
    private fun WalletTicketCard(
        ticket: WalletTicket,
        onEdit: () -> Unit,
        onShare: () -> Unit,
        onDelete: () -> Unit
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = SurfaceIvory),
            shape = RoundedCornerShape(28.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
            border = BorderStroke(1.dp, FineLine.copy(alpha = 0.72f))
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .background(GoldSoft, RoundedCornerShape(99.dp))
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text(
                            displayValue(ticket.platform).take(12),
                            color = Color(0xFF8A5A00),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                    Spacer(modifier = Modifier.weight(1f))
                    Text(ticket.date, color = Muted, fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
                Spacer(modifier = Modifier.height(14.dp))
                Text(ticket.title, fontWeight = FontWeight.Black, fontSize = 20.sp, color = Ink, lineHeight = 26.sp, maxLines = 3)
                if (ticket.cast.isNotBlank()) {
                    Spacer(modifier = Modifier.height(5.dp))
                    Text(ticket.cast, color = Muted, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
                Spacer(modifier = Modifier.height(14.dp))
                InfoLine(Icons.Rounded.CalendarMonth, "日期", displayValue(ticket.date))
                InfoLine(Icons.Rounded.LocationOn, "場地", displayValue(ticket.location))
                InfoLine(Icons.Rounded.EventSeat, "座位", displayValue(ticket.seat))
                InfoLine(Icons.Rounded.Sell, "票價", if (ticket.price.isBlank()) "尚未紀錄" else "NT$ ${ticket.price}")
                if (ticket.notes.isNotBlank()) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = CobaltSoft.copy(alpha = 0.72f)),
                        shape = RoundedCornerShape(18.dp),
                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.75f))
                    ) {
                        Text(ticket.notes, modifier = Modifier.padding(14.dp), color = Ink, fontSize = 13.sp, lineHeight = 19.sp)
                    }
                }
                Spacer(modifier = Modifier.height(14.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = onEdit,
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = StageBlack)
                    ) {
                        Icon(Icons.Rounded.Edit, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("編輯", fontWeight = FontWeight.Bold)
                    }
                    OutlinedButton(
                        onClick = onShare,
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Ink)
                    ) {
                        Icon(Icons.Rounded.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("分享", fontWeight = FontWeight.Bold)
                    }
                    OutlinedButton(
                        onClick = onDelete,
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Rose)
                    ) {
                        Icon(Icons.Rounded.DeleteOutline, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("移除", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }

    @Composable
    private fun RemindersScreen(
        reminders: List<ReminderItem>,
        onRefresh: () -> Unit,
        onDelete: (ReminderItem) -> Unit
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item { Spacer(modifier = Modifier.height(18.dp)) }
            item {
                FeatureHeader(
                    eyebrow = "提醒工作台",
                    title = "搶票提醒",
                    subtitle = "集中管理正在追蹤的開賣時間，讓加入、查看、移除都有明確回饋。",
                    icon = Icons.Rounded.Notifications,
                    accent = Gold,
                    action = "更新",
                    actionIcon = Icons.Rounded.Refresh,
                    onAction = onRefresh
                )
            }
            item { ReminderCoachCard(reminders.size) }
            if (reminders.isEmpty()) {
                item { EmptyStateCard("沒有提醒", "在活動卡片點選加入提醒，這裡會建立你的搶票清單。") }
            }
            items(reminders) { reminder ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = SurfaceIvory),
                    shape = RoundedCornerShape(24.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    border = BorderStroke(1.dp, FineLine.copy(alpha = 0.70f))
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .background(GoldSoft, RoundedCornerShape(14.dp))
                                    .padding(10.dp)
                            ) {
                                Icon(Icons.Rounded.Notifications, contentDescription = null, tint = Gold, modifier = Modifier.size(22.dp))
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(reminder.title, fontWeight = FontWeight.Bold, fontSize = 17.sp, color = Ink, maxLines = 2)
                                Text(formatReminderOffsets(reminder.offsetsMinutes), color = Muted, fontSize = 13.sp)
                            }
                        }
                        Spacer(modifier = Modifier.height(14.dp))
                        InfoLine(Icons.Rounded.CalendarMonth, "提醒時間", reminder.saleAt)
                        Spacer(modifier = Modifier.height(14.dp))
                        OutlinedButton(
                            onClick = { onDelete(reminder) },
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Rose)
                        ) {
                            Icon(Icons.Rounded.DeleteOutline, contentDescription = null, modifier = Modifier.size(19.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("移除提醒")
                        }
                    }
                }
            }
            item { Spacer(modifier = Modifier.height(16.dp)) }
        }
    }

    @Composable
    private fun AnalysisScreen(
        summary: SummaryStats,
        priceStats: PriceStats,
        timeStats: TimeStats,
        venueStats: VenueStats,
        onRefresh: () -> Unit
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Spacer(modifier = Modifier.height(18.dp))
            FeatureHeader(
                eyebrow = "資料分析",
                title = "資料洞察",
                subtitle = "把票價、月份與場地轉換成可展示的決策資訊，讓專題不只會查，也會分析。",
                icon = Icons.Rounded.Analytics,
                accent = Plum,
                action = "更新",
                actionIcon = Icons.Rounded.Refresh,
                onAction = onRefresh
            )
            AnalysisBrief(summary = summary, priceStats = priceStats)
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                MetricCard("活動樣本", summary.events.toString(), "筆資料", Icons.Rounded.Analytics, Modifier.weight(1f))
                MetricCard("平均最高票價", formatCurrency(priceStats.averageMaxPrice), "估算值", Icons.Rounded.TrendingUp, Modifier.weight(1f))
            }
            InsightCard("票價區間分布", "協助判斷售票活動的價位落點。") {
                priceStats.buckets.forEach {
                    ProgressRow(label = it.label, value = it.total, max = priceStats.total.coerceAtLeast(1), color = Cobalt)
                }
            }
            InsightCard("熱門活動月份", "看出活動集中在哪些月份，適合說明資料價值。") {
                timeStats.busiestMonths.forEach {
                    ProgressRow(label = it.label, value = it.total, max = timeStats.maxMonthTotal(), color = Gold)
                }
            }
            InsightCard("熱門場地排行", "統計資料庫中最常出現的演出場館。") {
                venueStats.venues.take(8).forEach {
                    ProgressRow(label = it.label, value = it.total, max = venueStats.maxVenueTotal(), color = Success)
                }
            }
            InsightCard("高票價活動 Top 5", "用排行呈現票價分析結果。") {
                priceStats.topExpensive.take(5).forEachIndexed { index, item ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 7.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        RankBadge(index + 1)
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(item.title, fontWeight = FontWeight.SemiBold, color = Ink, maxLines = 2)
                            Text("最高票價 ${formatCurrency(item.maxPrice)}", color = Muted, fontSize = 13.sp)
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }

    @Composable
    private fun PageTitle(title: String, subtitle: String) {
        Column {
            Text(title, color = Ink, fontSize = 28.sp, fontWeight = FontWeight.Black, lineHeight = 34.sp)
            Spacer(modifier = Modifier.height(5.dp))
            Text(subtitle, color = Muted, fontSize = 14.sp, lineHeight = 21.sp)
        }
    }

    @Composable
    private fun FeatureHeader(
        eyebrow: String,
        title: String,
        subtitle: String,
        icon: ImageVector,
        accent: Color,
        action: String? = null,
        actionIcon: ImageVector? = null,
        onAction: (() -> Unit)? = null
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = SurfaceIvory),
            shape = RoundedCornerShape(28.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            border = BorderStroke(1.dp, FineLine.copy(alpha = 0.72f))
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .background(accent.copy(alpha = 0.12f), RoundedCornerShape(16.dp))
                            .padding(11.dp)
                    ) {
                        Icon(icon, contentDescription = null, tint = accent, modifier = Modifier.size(23.dp))
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(eyebrow, color = accent, fontSize = 12.sp, fontWeight = FontWeight.Black)
                        Text(title, color = Ink, fontSize = 22.sp, fontWeight = FontWeight.Black, lineHeight = 28.sp)
                    }
                    if (action != null && onAction != null) {
                        OutlinedButton(
                            onClick = onAction,
                            shape = RoundedCornerShape(99.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Ink)
                        ) {
                            if (actionIcon != null) {
                                Icon(actionIcon, contentDescription = null, modifier = Modifier.size(17.dp))
                                Spacer(modifier = Modifier.width(5.dp))
                            }
                            Text(action, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
                Text(subtitle, color = Muted, fontSize = 14.sp, lineHeight = 21.sp)
            }
        }
    }

    @Composable
    private fun ReminderCoachCard(count: Int) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = StageBlack),
            shape = RoundedCornerShape(26.dp),
            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.10f))
        ) {
            Column(
                modifier = Modifier
                    .background(Brush.linearGradient(listOf(StageBlack, Color(0xFF18263D))))
                    .padding(20.dp)
            ) {
                Text("提醒節奏", color = Gold, fontSize = 12.sp, fontWeight = FontWeight.Black)
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    if (count > 0) "目前追蹤 $count 筆活動" else "先從活動卡片加入提醒",
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "系統會用 60、30、10 分鐘三段提醒概念，呈現完整的搶票流程設計。",
                    color = Color.White.copy(alpha = 0.78f),
                    fontSize = 13.sp,
                    lineHeight = 20.sp
                )
                Spacer(modifier = Modifier.height(14.dp))
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    HeroPill("60 分鐘前")
                    HeroPill("30 分鐘前")
                    HeroPill("10 分鐘前")
                }
            }
        }
    }

    @Composable
    private fun AnalysisBrief(summary: SummaryStats, priceStats: PriceStats) {
        val pricedRatio = if (priceStats.total <= 0) "整理中" else "${priceStats.priced * 100 / priceStats.total}%"
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = SurfaceIvory),
            shape = RoundedCornerShape(26.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            border = BorderStroke(1.dp, FineLine.copy(alpha = 0.72f))
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                BriefMetric("資料規模", formatNumber(summary.events), "活動筆數", Cobalt, Modifier.weight(1f))
                BriefMetric("票價覆蓋", pricedRatio, "可分析比例", Gold, Modifier.weight(1f))
            }
        }
    }

    @Composable
    private fun BriefMetric(title: String, value: String, note: String, accent: Color, modifier: Modifier = Modifier) {
        Column(
            modifier = modifier
                .background(accent.copy(alpha = 0.10f), RoundedCornerShape(20.dp))
                .padding(14.dp)
        ) {
            Text(title, color = Muted, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(4.dp))
            Text(value, color = Ink, fontSize = 22.sp, fontWeight = FontWeight.Black, maxLines = 1)
            Text(note, color = accent, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }
    }

    @Composable
    private fun SectionHeader(
        title: String,
        subtitle: String,
        action: String,
        actionIcon: ImageVector,
        onAction: () -> Unit
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(title, color = Ink, fontSize = 22.sp, fontWeight = FontWeight.Black)
                Spacer(modifier = Modifier.height(4.dp))
                Text(subtitle, color = Muted, fontSize = 13.sp, lineHeight = 19.sp)
            }
            OutlinedButton(
                onClick = onAction,
                shape = RoundedCornerShape(99.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Ink)
            ) {
                Icon(actionIcon, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(action, fontWeight = FontWeight.SemiBold)
            }
        }
    }

    @Composable
    private fun MetricCard(title: String, value: String, note: String, icon: ImageVector, modifier: Modifier = Modifier) {
        Card(
            modifier = modifier,
            colors = CardDefaults.cardColors(containerColor = SurfaceIvory),
            shape = RoundedCornerShape(24.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            border = BorderStroke(1.dp, FineLine.copy(alpha = 0.68f))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .background(CobaltSoft, RoundedCornerShape(12.dp))
                            .padding(8.dp)
                    ) {
                        Icon(icon, contentDescription = null, tint = Cobalt, modifier = Modifier.size(18.dp))
                    }
                    Spacer(modifier = Modifier.weight(1f))
                }
                Spacer(modifier = Modifier.height(12.dp))
                Text(title, color = Muted, fontSize = 13.sp)
                Spacer(modifier = Modifier.height(4.dp))
                Text(value, fontWeight = FontWeight.Black, fontSize = 26.sp, color = Ink, maxLines = 1)
                Spacer(modifier = Modifier.height(3.dp))
                Text(note, color = Color(0xFF9CA3AF), fontSize = 12.sp)
            }
        }
    }

    @Composable
    private fun EventCard(event: EventItem, onOpenUrl: () -> Unit, onAddReminder: () -> Unit) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = SurfaceIvory),
            shape = RoundedCornerShape(28.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
            border = BorderStroke(1.dp, FineLine.copy(alpha = 0.72f))
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    SourceBadge(displayValue(event.source))
                    Spacer(modifier = Modifier.width(9.dp))
                    Text(displayValue(event.venue), color = Muted, fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
                Spacer(modifier = Modifier.height(14.dp))
                Text(event.title, fontWeight = FontWeight.Black, fontSize = 20.sp, color = Ink, lineHeight = 26.sp, maxLines = 3)
                if (event.artist.isNotBlank() && event.artist != event.title) {
                    Spacer(modifier = Modifier.height(5.dp))
                    Text(event.artist, color = Muted, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
                Spacer(modifier = Modifier.height(14.dp))
                InfoLine(Icons.Rounded.CalendarMonth, "活動", displayValue(event.activityTime))
                InfoLine(Icons.Rounded.Notifications, "開賣", displayValue(event.saleTime))
                InfoLine(Icons.Rounded.LocationOn, "場地", displayValue(event.venue))
                TicketPricePanel(price = displayValue(event.price), ticketType = displayValue(event.ticketType))
                Spacer(modifier = Modifier.height(16.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Button(
                        onClick = onAddReminder,
                        shape = RoundedCornerShape(18.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = StageBlack)
                    ) {
                        Icon(Icons.Rounded.Notifications, contentDescription = null, modifier = Modifier.size(19.dp))
                        Spacer(modifier = Modifier.width(7.dp))
                        Text("加入提醒", fontWeight = FontWeight.Bold)
                    }
                    OutlinedButton(
                        onClick = onOpenUrl,
                        shape = RoundedCornerShape(18.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp),
                        enabled = event.url.startsWith("http"),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Ink)
                    ) {
                        Icon(Icons.Rounded.OpenInNew, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(7.dp))
                        Text(if (event.url.startsWith("http")) "購票連結" else "尚無連結", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }

    @Composable
    private fun TicketPricePanel(price: String, ticketType: String) {
        Spacer(modifier = Modifier.height(10.dp))
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = CobaltSoft.copy(alpha = 0.72f)),
            shape = RoundedCornerShape(20.dp),
            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.75f))
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text("票價資訊", color = Cobalt, fontSize = 12.sp, fontWeight = FontWeight.Black)
                Spacer(modifier = Modifier.height(4.dp))
                Text(price, color = Ink, fontSize = 15.sp, fontWeight = FontWeight.Bold, lineHeight = 21.sp, maxLines = 3)
                if (ticketType != "尚未公布") {
                    Spacer(modifier = Modifier.height(3.dp))
                    Text(ticketType, color = Muted, fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
            }
        }
    }

    @Composable
    private fun SourceBadge(source: String) {
        Box(
            modifier = Modifier
                .background(GoldSoft, RoundedCornerShape(99.dp))
                .padding(horizontal = 10.dp, vertical = 6.dp)
        ) {
            Text(source.take(12), color = Color(0xFF8A5A00), fontSize = 12.sp, fontWeight = FontWeight.Black)
        }
    }

    @Composable
    private fun InfoLine(icon: ImageVector, label: String, value: String) {
        Row(
            modifier = Modifier.padding(vertical = 4.dp),
            verticalAlignment = Alignment.Top
        ) {
            Icon(icon, contentDescription = null, tint = Color(0xFF9CA3AF), modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(label, modifier = Modifier.width(58.dp), color = Color(0xFF9CA3AF), fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
            Text(
                value,
                modifier = Modifier.weight(1f),
                color = Color(0xFF374151),
                fontSize = 13.sp,
                lineHeight = 19.sp,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )
        }
    }

    @Composable
    private fun InsightCard(title: String, subtitle: String, content: @Composable () -> Unit) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = SurfaceIvory),
            shape = RoundedCornerShape(26.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            border = BorderStroke(1.dp, FineLine.copy(alpha = 0.70f))
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Text(title, fontSize = 19.sp, fontWeight = FontWeight.Black, color = Ink)
                Spacer(modifier = Modifier.height(4.dp))
                Text(subtitle, fontSize = 13.sp, color = Muted, lineHeight = 19.sp)
                Spacer(modifier = Modifier.height(12.dp))
                content()
            }
        }
    }

    @Composable
    private fun ProgressRow(label: String, value: Int, max: Int, color: Color) {
        val ratio = if (max <= 0) 0f else value.toFloat() / max.toFloat()
        val animatedRatio by animateFloatAsState(
            targetValue = ratio.coerceIn(0f, 1f),
            animationSpec = tween(durationMillis = 650),
            label = "progress"
        )
        Column(modifier = Modifier.padding(vertical = 7.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(label, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f), color = Ink, fontSize = 14.sp)
                Text(formatNumber(value), fontWeight = FontWeight.Black, color = Ink)
            }
            Spacer(modifier = Modifier.height(7.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .background(Mist, RoundedCornerShape(99.dp))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(animatedRatio)
                        .height(8.dp)
                        .background(color, RoundedCornerShape(99.dp))
                )
            }
        }
    }

    @Composable
    private fun RankBadge(rank: Int) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .background(StageBlack, RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text(rank.toString(), color = Gold, fontSize = 13.sp, fontWeight = FontWeight.Black)
        }
    }

    @Composable
    private fun LoadingCard(message: String) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = SurfaceIvory),
            shape = RoundedCornerShape(24.dp),
            border = BorderStroke(1.dp, FineLine.copy(alpha = 0.65f))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(22.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                CircularProgressIndicator(modifier = Modifier.size(28.dp), color = Cobalt)
                Spacer(modifier = Modifier.width(14.dp))
                Text(message, color = Muted)
            }
        }
    }

    @Composable
    private fun EmptyStateCard(title: String, message: String) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = SurfaceIvory),
            shape = RoundedCornerShape(26.dp),
            border = BorderStroke(1.dp, FineLine.copy(alpha = 0.65f))
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .background(CobaltSoft, RoundedCornerShape(18.dp))
                        .padding(13.dp)
                ) {
                    Icon(Icons.Rounded.Search, contentDescription = null, tint = Cobalt, modifier = Modifier.size(28.dp))
                }
                Spacer(modifier = Modifier.height(12.dp))
                Text(title, fontWeight = FontWeight.Black, fontSize = 18.sp, color = Ink)
                Spacer(modifier = Modifier.height(6.dp))
                Text(message, color = Muted, fontSize = 14.sp, lineHeight = 21.sp)
            }
        }
    }

    private fun loadWalletTickets(): List<WalletTicket> {
        val prefs = getSharedPreferences("ticket_wallet", Context.MODE_PRIVATE)
        val raw = prefs.getString("tickets", "[]").orEmpty()
        return runCatching {
            val array = JSONArray(raw)
            val result = mutableListOf<WalletTicket>()
            for (i in 0 until array.length()) {
                val item = array.getJSONObject(i)
                result.add(
                    WalletTicket(
                        id = item.optLong("id"),
                        title = item.optString("title"),
                        date = item.optString("date"),
                        location = item.optString("location"),
                        seat = item.optString("seat"),
                        cast = item.optString("cast"),
                        platform = item.optString("platform"),
                        price = item.optString("price"),
                        notes = item.optString("notes")
                    )
                )
            }
            result
        }.getOrDefault(emptyList())
    }

    private fun saveWalletTickets(tickets: List<WalletTicket>) {
        val array = JSONArray()
        tickets.forEach { ticket ->
            array.put(
                JSONObject()
                    .put("id", ticket.id)
                    .put("title", ticket.title)
                    .put("date", ticket.date)
                    .put("location", ticket.location)
                    .put("seat", ticket.seat)
                    .put("cast", ticket.cast)
                    .put("platform", ticket.platform)
                    .put("price", ticket.price)
                    .put("notes", ticket.notes)
            )
        }
        getSharedPreferences("ticket_wallet", Context.MODE_PRIVATE)
            .edit()
            .putString("tickets", array.toString())
            .apply()
    }

    private fun loadSavedSession(): AuthSession? {
        val prefs = getSharedPreferences("ticket_auth", Context.MODE_PRIVATE)
        val token = prefs.getString("token", "").orEmpty()
        val email = prefs.getString("email", "").orEmpty()
        val userId = prefs.getInt("user_id", 0)
        if (token.isBlank() || email.isBlank() || userId <= 0) return null
        return AuthSession(token = token, userId = userId, email = email)
    }

    private fun saveSession(session: AuthSession) {
        getSharedPreferences("ticket_auth", Context.MODE_PRIVATE)
            .edit()
            .putString("token", session.token)
            .putString("email", session.email)
            .putInt("user_id", session.userId)
            .apply()
    }

    private fun clearSession() {
        getSharedPreferences("ticket_auth", Context.MODE_PRIVATE)
            .edit()
            .clear()
            .apply()
    }

    private fun matchesWalletQuery(ticket: WalletTicket, keyword: String): Boolean {
        val query = keyword.trim().lowercase(Locale.TAIWAN)
        if (query.isEmpty()) return true
        return listOf(
            ticket.title,
            ticket.date,
            ticket.location,
            ticket.seat,
            ticket.cast,
            ticket.platform,
            ticket.price,
            ticket.notes
        ).any { it.lowercase(Locale.TAIWAN).contains(query) }
    }

    private fun nextPlatformFilter(current: String, platforms: List<String>): String {
        val options = listOf("全部") + platforms
        val index = options.indexOf(current).takeIf { it >= 0 } ?: 0
        return options[(index + 1) % options.size]
    }

    private fun parseWalletTimeMillis(value: String): Long {
        val patterns = listOf("yyyy-MM-dd HH:mm", "yyyy/MM/dd HH:mm", "yyyy-MM-dd", "yyyy/MM/dd")
        for (pattern in patterns) {
            val parsed = runCatching {
                SimpleDateFormat(pattern, Locale.TAIWAN).apply { isLenient = false }.parse(value.trim())
            }.getOrNull()
            if (parsed != null) return parsed.time
        }
        return Long.MAX_VALUE
    }

    private fun calendarFromWalletDate(value: String): Calendar {
        val time = parseWalletTimeMillis(value)
        return Calendar.getInstance().apply {
            if (time != Long.MAX_VALUE) timeInMillis = time
        }
    }

    private fun walletDateKey(value: String): String {
        val time = parseWalletTimeMillis(value)
        return if (time == Long.MAX_VALUE) "" else SimpleDateFormat("yyyy-MM-dd", Locale.TAIWAN).format(time)
    }

    private fun isSameWalletMonth(ticket: WalletTicket, month: Calendar): Boolean {
        val time = parseWalletTimeMillis(ticket.date)
        if (time == Long.MAX_VALUE) return false
        val calendar = Calendar.getInstance().apply { timeInMillis = time }
        return calendar.get(Calendar.YEAR) == month.get(Calendar.YEAR) &&
            calendar.get(Calendar.MONTH) == month.get(Calendar.MONTH)
    }

    private fun walletMonthTitle(month: Calendar): String {
        return SimpleDateFormat("yyyy 年 MM 月", Locale.TAIWAN).format(month.time)
    }

    private fun walletCalendarDays(month: Calendar): List<WalletCalendarDay> {
        val first = (month.clone() as Calendar).apply {
            set(Calendar.DAY_OF_MONTH, 1)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val start = (first.clone() as Calendar).apply {
            add(Calendar.DAY_OF_MONTH, -(get(Calendar.DAY_OF_WEEK) - Calendar.SUNDAY))
        }
        return (0 until 42).map { index ->
            val day = (start.clone() as Calendar).apply { add(Calendar.DAY_OF_MONTH, index) }
            WalletCalendarDay(
                day = day.get(Calendar.DAY_OF_MONTH),
                key = SimpleDateFormat("yyyy-MM-dd", Locale.TAIWAN).format(day.time),
                inMonth = day.get(Calendar.MONTH) == first.get(Calendar.MONTH)
            )
        }
    }

    private fun shareWalletTicket(ticket: WalletTicket) {
        val bitmap = createWalletTicketBitmap(ticket)
        val directory = File(cacheDir, "shared_tickets").apply { mkdirs() }
        val file = File(directory, "ticket_${ticket.id}.png")
        FileOutputStream(file).use { output ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, output)
        }
        val uri = FileProvider.getUriForFile(this, "$packageName.fileprovider", file)
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "image/png"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        startActivity(Intent.createChooser(intent, "分享票券"))
    }

    private fun createWalletTicketBitmap(ticket: WalletTicket): Bitmap {
        val width = 1080
        val height = 1500
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = AndroidCanvas(bitmap)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)

        canvas.drawColor(AndroidColor.rgb(246, 241, 232))
        paint.color = AndroidColor.rgb(255, 252, 246)
        canvas.drawRoundRect(RectF(72f, 90f, 1008f, 1410f), 58f, 58f, paint)
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 4f
        paint.color = AndroidColor.rgb(216, 203, 190)
        canvas.drawRoundRect(RectF(72f, 90f, 1008f, 1410f), 58f, 58f, paint)
        paint.style = Paint.Style.FILL

        paint.color = AndroidColor.rgb(8, 17, 31)
        canvas.drawRoundRect(RectF(112f, 130f, 968f, 420f), 42f, 42f, paint)
        paint.color = AndroidColor.rgb(216, 168, 78)
        paint.textSize = 34f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("TICKET WALLET", 150f, 205f, paint)

        paint.color = AndroidColor.WHITE
        paint.textSize = 62f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        drawWrappedText(canvas, paint, ticket.title, 150f, 285f, 760f, 78f, 2)

        var y = 520f
        y = drawShareField(canvas, paint, "日期", displayValue(ticket.date), y)
        y = drawShareField(canvas, paint, "場地", displayValue(ticket.location), y)
        y = drawShareField(canvas, paint, "座位", displayValue(ticket.seat), y)
        y = drawShareField(canvas, paint, "演出者", displayValue(ticket.cast), y)
        y = drawShareField(canvas, paint, "平台", displayValue(ticket.platform), y)
        y = drawShareField(canvas, paint, "票價", if (ticket.price.isBlank()) "尚未紀錄" else "NT$ ${ticket.price}", y)

        if (ticket.notes.isNotBlank()) {
            paint.color = AndroidColor.rgb(232, 238, 255)
            canvas.drawRoundRect(RectF(132f, y + 20f, 948f, y + 210f), 32f, 32f, paint)
            paint.color = AndroidColor.rgb(36, 92, 255)
            paint.textSize = 30f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            canvas.drawText("備註", 168f, y + 76f, paint)
            paint.color = AndroidColor.rgb(17, 24, 39)
            paint.textSize = 34f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            drawWrappedText(canvas, paint, ticket.notes, 168f, y + 130f, 720f, 48f, 2)
        }

        paint.color = AndroidColor.rgb(107, 114, 128)
        paint.textSize = 28f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        canvas.drawText("由售票資訊 App 產生", 132f, 1340f, paint)
        return bitmap
    }

    private fun drawShareField(canvas: AndroidCanvas, paint: Paint, label: String, value: String, y: Float): Float {
        paint.color = AndroidColor.rgb(107, 114, 128)
        paint.textSize = 30f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText(label, 132f, y, paint)
        paint.color = AndroidColor.rgb(17, 24, 39)
        paint.textSize = 38f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        drawWrappedText(canvas, paint, value, 260f, y, 660f, 50f, 2)
        return y + 118f
    }

    private fun drawWrappedText(
        canvas: AndroidCanvas,
        paint: Paint,
        text: String,
        x: Float,
        y: Float,
        maxWidth: Float,
        lineHeight: Float,
        maxLines: Int
    ) {
        val words = text.ifBlank { "尚未公布" }.split(Regex("\\s+"))
        val lines = mutableListOf<String>()
        var current = ""
        for (word in words) {
            val next = if (current.isEmpty()) word else "$current $word"
            if (paint.measureText(next) <= maxWidth) {
                current = next
            } else {
                if (current.isNotEmpty()) lines.add(current)
                current = word
            }
        }
        if (current.isNotEmpty()) lines.add(current)

        lines.take(maxLines).forEachIndexed { index, line ->
            val suffix = if (index == maxLines - 1 && lines.size > maxLines) "..." else ""
            canvas.drawText(line.take(36) + suffix, x, y + index * lineHeight, paint)
        }
    }

    private fun checkHealth(onSuccess: () -> Unit, onError: () -> Unit) {
        get("/api/health", onSuccess = { onSuccess() }, onError = { onError() })
    }

    private fun loadEvents(limit: Int, keyword: String, featured: Boolean, onResult: (List<EventItem>) -> Unit) {
        val queryParts = mutableListOf("limit=$limit")
        if (featured) queryParts.add("featured=1")
        if (keyword.trim().isNotEmpty()) queryParts.add("keyword=${urlEncode(keyword)}")
        val path = "/api/events?${queryParts.joinToString("&")}"

        get(
            path = path,
            onSuccess = { body ->
                val root = JSONObject(body)
                val items = root.optJSONArray("items") ?: JSONArray()
                val result = mutableListOf<EventItem>()
                for (i in 0 until items.length()) {
                    result.add(items.getJSONObject(i).toEventItem())
                }
                onResult(result)
            },
            onError = {
                toast("活動資料讀取失敗")
                onResult(emptyList())
            }
        )
    }

    private fun loadReminders(onResult: (List<ReminderItem>) -> Unit) {
        get(
            path = "/api/reminders",
            onSuccess = { body ->
                val array = JSONArray(body)
                val result = mutableListOf<ReminderItem>()
                for (i in 0 until array.length()) {
                    val obj = array.getJSONObject(i)
                    result.add(
                        ReminderItem(
                            id = obj.optInt("id"),
                            title = obj.optString("title"),
                            saleAt = obj.optString("saleAt"),
                            offsetsMinutes = obj.optString("offsetsMinutes"),
                            enabled = obj.optInt("enabled", 1) == 1
                        )
                    )
                }
                onResult(result)
            },
            onError = {
                toast("提醒資料讀取失敗")
                onResult(emptyList())
            }
        )
    }

    private fun loadSummary(onResult: (SummaryStats) -> Unit) {
        get(
            path = "/api/stats/summary",
            onSuccess = { body ->
                val obj = JSONObject(body)
                onResult(
                    SummaryStats(
                        events = obj.optInt("events"),
                        artists = obj.optInt("artists"),
                        venues = obj.optInt("venues"),
                        reminders = obj.optInt("reminders")
                    )
                )
            },
            onError = { onResult(SummaryStats()) }
        )
    }

    private fun loadPriceStats(onResult: (PriceStats) -> Unit) {
        get(
            path = "/api/stats/price",
            onSuccess = { body ->
                val obj = JSONObject(body)
                val buckets = obj.optJSONObject("buckets") ?: JSONObject()
                val top = obj.optJSONArray("topExpensive") ?: JSONArray()
                val topItems = mutableListOf<PriceEvent>()
                for (i in 0 until top.length()) {
                    val item = top.getJSONObject(i)
                    topItems.add(PriceEvent(title = item.optString("title"), maxPrice = item.optInt("maxPrice")))
                }
                onResult(
                    PriceStats(
                        total = obj.optInt("total"),
                        priced = obj.optInt("priced"),
                        averageMaxPrice = obj.optInt("averageMaxPrice"),
                        buckets = listOf(
                            StatItem("未提供或免費", buckets.optInt("freeOrUnknown")),
                            StatItem("NT$ 1,000 以下", buckets.optInt("under1000")),
                            StatItem("NT$ 1,000 - 3,000", buckets.optInt("between1000And3000")),
                            StatItem("NT$ 3,000 - 6,000", buckets.optInt("between3000And6000")),
                            StatItem("NT$ 6,000 以上", buckets.optInt("over6000"))
                        ),
                        topExpensive = topItems
                    )
                )
            },
            onError = { onResult(PriceStats()) }
        )
    }

    private fun loadTimeStats(onResult: (TimeStats) -> Unit) {
        get(
            path = "/api/stats/time",
            onSuccess = { body ->
                val obj = JSONObject(body)
                val busiest = obj.optJSONArray("busiestMonths") ?: JSONArray()
                val result = mutableListOf<StatItem>()
                for (i in 0 until busiest.length()) {
                    val item = busiest.getJSONObject(i)
                    result.add(StatItem(item.optString("month"), item.optInt("total")))
                }
                onResult(TimeStats(total = obj.optInt("total"), busiestMonths = result))
            },
            onError = { onResult(TimeStats()) }
        )
    }

    private fun loadVenueStats(onResult: (VenueStats) -> Unit) {
        get(
            path = "/api/stats/venue",
            onSuccess = { body ->
                val obj = JSONObject(body)
                val venues = obj.optJSONArray("venues") ?: JSONArray()
                val result = mutableListOf<StatItem>()
                for (i in 0 until venues.length()) {
                    val item = venues.getJSONObject(i)
                    result.add(StatItem(item.optString("venue"), item.optInt("total")))
                }
                onResult(VenueStats(result))
            },
            onError = { onResult(VenueStats()) }
        )
    }

    private fun addReminder(event: EventItem, onSuccess: () -> Unit) {
        val payload = JSONObject()
            .put("title", event.title)
            .put("saleAt", guessReminderTime(event))
            .put("offsetsMinutes", JSONArray(listOf(60, 30, 10)))
            .toString()

        post(
            path = "/api/reminders",
            body = payload,
            onSuccess = { onSuccess() },
            onError = { toast("新增提醒失敗") }
        )
    }

    private fun deleteReminder(id: Int, onSuccess: () -> Unit) {
        request(
            request = Request.Builder()
                .url("$API_BASE_URL/api/reminders/$id")
                .delete()
                .build(),
            onSuccess = { onSuccess() },
            onError = { toast("刪除提醒失敗") }
        )
    }

    private fun deleteAccount(password: String, onSuccess: () -> Unit, onError: () -> Unit) {
        val payload = JSONObject()
            .put("password", password)
            .toString()

        request(
            request = Request.Builder()
                .url("$API_BASE_URL/api/auth/me")
                .delete(payload.toRequestBody(jsonMediaType))
                .build(),
            onSuccess = { onSuccess() },
            onError = { onError() }
        )
    }

    private fun get(path: String, onSuccess: (String) -> Unit, onError: (String) -> Unit) {
        request(
            request = Request.Builder().url("$API_BASE_URL$path").get().build(),
            onSuccess = onSuccess,
            onError = onError
        )
    }

    private fun post(path: String, body: String, onSuccess: (String) -> Unit, onError: (String) -> Unit) {
        request(
            request = Request.Builder()
                .url("$API_BASE_URL$path")
                .post(body.toRequestBody(jsonMediaType))
                .build(),
            onSuccess = onSuccess,
            onError = onError
        )
    }

    private fun request(request: Request, onSuccess: (String) -> Unit, onError: (String) -> Unit) {
        val authedRequest = if (activeToken.isNotBlank() && request.header("Authorization") == null) {
            request.newBuilder()
                .addHeader("Authorization", "Bearer $activeToken")
                .build()
        } else {
            request
        }

        client.newCall(authedRequest).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread { onError(e.message ?: "network error") }
            }

            override fun onResponse(call: Call, response: Response) {
                response.use {
                    val body = it.body?.string().orEmpty()
                    runOnUiThread {
                        if (it.isSuccessful) onSuccess(body) else onError(body)
                    }
                }
            }
        })
    }

    private fun JSONObject.toAuthSession(): AuthSession {
        val user = optJSONObject("user") ?: JSONObject()
        return AuthSession(
            token = optString("token"),
            userId = user.optInt("id"),
            email = user.optString("email")
        )
    }

    private fun parseApiError(body: String, fallback: String): String {
        return runCatching {
            when (JSONObject(body).optString("error")) {
                "email_exists" -> "這個 email 已經註冊"
                "invalid_credentials" -> "帳號或密碼錯誤"
                "invalid_fields" -> "Email 格式或密碼長度不正確"
                "unauthorized" -> "登入已失效，請重新登入"
                else -> fallback
            }
        }.getOrDefault(fallback)
    }

    private fun JSONObject.toEventItem(): EventItem {
        return EventItem(
            id = optInt("id"),
            title = optString("title"),
            artist = optString("artist"),
            saleTime = optString("saleTime"),
            activityTime = optString("activityTime"),
            venue = optString("venue"),
            address = optString("address"),
            price = optString("price"),
            ticketType = optString("ticketType"),
            url = optString("url"),
            source = optString("source")
        )
    }

    private fun guessReminderTime(event: EventItem): String {
        val source = "${event.saleTime} ${event.activityTime}"
        val regex = Regex("""(20\d{2})[./年/-]\s*(\d{1,2})[./月/-]\s*(\d{1,2})""")
        val match = regex.find(source)
        if (match != null) {
            val year = match.groupValues[1].toInt()
            val month = match.groupValues[2].toInt()
            val day = match.groupValues[3].toInt()
            val time = Regex("""(\d{1,2}):(\d{2})""").find(source)
            val hour = time?.groupValues?.getOrNull(1)?.toIntOrNull() ?: 12
            val minute = time?.groupValues?.getOrNull(2)?.toIntOrNull() ?: 0
            return "%04d-%02d-%02d %02d:%02d:00".format(year, month, day, hour, minute)
        }

        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_MONTH, 1)
        return SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.TAIWAN).format(calendar.time)
    }

    private fun openEventUrl(event: EventItem) {
        if (!event.url.startsWith("http")) {
            toast("這筆活動尚未提供購票連結")
            return
        }
        startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(event.url)))
    }

    private fun displayValue(value: String): String {
        val cleaned = value.trim()
        return if (cleaned.isEmpty() || cleaned == "未提供" || cleaned == "null") "尚未公布" else cleaned
    }

    private fun formatReminderOffsets(value: String): String {
        val minutes = Regex("""\d+""").findAll(value).map { it.value }.distinct().toList()
        return if (minutes.isEmpty()) "依預設時間通知" else "提前 ${minutes.joinToString("、")} 分鐘通知"
    }

    private fun formatNumber(value: Int): String = String.format(Locale.TAIWAN, "%,d", value.coerceAtLeast(0))

    private fun formatCurrency(value: Int): String {
        return if (value <= 0) "尚未估算" else "NT$ ${formatNumber(value)}"
    }

    private fun urlEncode(value: String): String = URLEncoder.encode(value, "UTF-8")

    private fun toast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}

private enum class AppTab(val title: String, val icon: ImageVector) {
    Home("首頁", Icons.Rounded.Home),
    Search("探索", Icons.Rounded.Search),
    Wallet("票券夾", Icons.Rounded.ConfirmationNumber),
    Reminders("提醒", Icons.Rounded.Notifications),
    Analysis("洞察", Icons.Rounded.Analytics),
    Account("帳號", Icons.Rounded.AccountCircle)
}

private enum class AuthMode {
    Login,
    Register
}

private data class AuthSession(
    val token: String,
    val userId: Int,
    val email: String
)

private data class WalletTicket(
    val id: Long,
    val title: String,
    val date: String,
    val location: String,
    val seat: String,
    val cast: String,
    val platform: String,
    val price: String,
    val notes: String
)

private enum class WalletSort(val label: String) {
    DateNear("日期近"),
    DateFar("日期遠"),
    PriceHigh("高票價"),
    PriceLow("低票價");

    fun next(): WalletSort {
        val values = entries
        return values[(ordinal + 1) % values.size]
    }
}

private data class WalletCalendarDay(
    val day: Int,
    val key: String,
    val inMonth: Boolean
)

private data class SummaryStats(
    val events: Int = 0,
    val artists: Int = 0,
    val venues: Int = 0,
    val reminders: Int = 0
)

private data class StatItem(
    val label: String,
    val total: Int
)

private data class PriceEvent(
    val title: String,
    val maxPrice: Int
)

private data class PriceStats(
    val total: Int = 0,
    val priced: Int = 0,
    val averageMaxPrice: Int = 0,
    val buckets: List<StatItem> = emptyList(),
    val topExpensive: List<PriceEvent> = emptyList()
)

private data class TimeStats(
    val total: Int = 0,
    val busiestMonths: List<StatItem> = emptyList()
) {
    fun maxMonthTotal(): Int = busiestMonths.maxOfOrNull { it.total } ?: 1
}

private data class VenueStats(
    val venues: List<StatItem> = emptyList()
) {
    fun maxVenueTotal(): Int = venues.maxOfOrNull { it.total } ?: 1
}
