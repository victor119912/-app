package com.example.myapplication

import android.content.Intent
import android.graphics.Color as AndroidColor
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Analytics
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.LocalActivity
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.OpenInNew
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Stars
import androidx.compose.material.icons.rounded.TrendingUp
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
        var keyword by remember { mutableStateOf("") }
        var loading by remember { mutableStateOf(false) }

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

        LaunchedEffect(Unit) {
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
                                AppTab.Reminders -> loadReminders { reminders = it }
                                AppTab.Analysis -> refreshAnalysis()
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
                    }
                }
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
        client.newCall(request).enqueue(object : Callback {
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
    Reminders("提醒", Icons.Rounded.Notifications),
    Analysis("洞察", Icons.Rounded.Analytics)
}

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
