package com.block.app.widget

import android.content.Context
import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.width
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import androidx.room.Room
import com.block.app.data.db.BlockDatabase
import java.time.LocalDate

/**
 * Glance-based home-screen widget showing tower height, today's
 * block count, and current streak. Neo-brutalist design with a
 * dark background and electric lime accents.
 */
class BlockWidget : GlanceAppWidget() {

    companion object {
        // Neo-brutalist palette
        private val COLOR_DARK = android.graphics.Color.parseColor("#1A1A1A")
        private val COLOR_LIME = android.graphics.Color.parseColor("#CCFF00")
        private val COLOR_OFF_WHITE = android.graphics.Color.parseColor("#F5F0E8")
        private val COLOR_LIME_DIM = android.graphics.Color.parseColor("#99CC00")
    }

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        // Direct database access — Glance runs in a worker context
        val db = Room.databaseBuilder(
            context.applicationContext,
            BlockDatabase::class.java,
            BlockDatabase.DATABASE_NAME
        ).build()

        val dao = db.blockAttemptDao()

        val today = LocalDate.now()
        val todayCount = dao.getCountToday(today.dayOfYear, today.year)
        val totalBlocks = dao.getTotalCount()

        // Calculate streak: count consecutive days going back from today
        val thirtyDaysAgoMs = System.currentTimeMillis() - (30L * 24 * 60 * 60 * 1000)
        val dailyCounts = dao.getDailyCountsSince(thirtyDaysAgoMs)
        val streakDays = calculateStreak(dailyCounts.map { it.dayOfYear to it.year }.toSet(), today)

        db.close()

        provideContent {
            GlanceTheme {
                WidgetContent(
                    todayCount = todayCount,
                    streakDays = streakDays,
                    totalBlocks = totalBlocks
                )
            }
        }
    }

    /**
     * Count consecutive days with at least one block, going backwards from today.
     */
    private fun calculateStreak(
        activeDays: Set<Pair<Int, Int>>,
        today: LocalDate
    ): Int {
        var streak = 0
        var current = today
        while (activeDays.contains(current.dayOfYear to current.year)) {
            streak++
            current = current.minusDays(1)
        }
        return streak
    }

    @Composable
    private fun WidgetContent(todayCount: Int, streakDays: Int, totalBlocks: Int) {
        val limeColor = ColorProvider(
            day = android.graphics.Color.parseColor("#CCFF00"),
            night = android.graphics.Color.parseColor("#CCFF00")
        )
        val offWhiteColor = ColorProvider(
            day = android.graphics.Color.parseColor("#F5F0E8"),
            night = android.graphics.Color.parseColor("#F5F0E8")
        )
        val dimColor = ColorProvider(
            day = android.graphics.Color.parseColor("#888888"),
            night = android.graphics.Color.parseColor("#888888")
        )
        val darkBg = ColorProvider(
            day = android.graphics.Color.parseColor("#1A1A1A"),
            night = android.graphics.Color.parseColor("#1A1A1A")
        )

        // Open main activity on tap
        val launchIntent = Intent().apply {
            setClassName("com.block.app", "com.block.app.MainActivity")
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }

        Box(
            modifier = GlanceModifier
                .fillMaxSize()
                .background(darkBg)
                .padding(2.dp)
                .clickable(actionStartActivity(launchIntent)),
            contentAlignment = Alignment.Center
        ) {
            // Lime border effect via nested box
            Box(
                modifier = GlanceModifier
                    .fillMaxSize()
                    .background(limeColor)
                    .padding(2.dp)
            ) {
                Box(
                    modifier = GlanceModifier
                        .fillMaxSize()
                        .background(darkBg)
                        .padding(12.dp)
                ) {
                    Column(
                        modifier = GlanceModifier.fillMaxSize(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Title
                        Text(
                            text = "BlOCK",
                            style = TextStyle(
                                color = limeColor,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        )

                        Spacer(modifier = GlanceModifier.height(8.dp))

                        // Today's count — big number
                        Row(
                            modifier = GlanceModifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "$todayCount",
                                style = TextStyle(
                                    color = offWhiteColor,
                                    fontSize = 32.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                            Spacer(modifier = GlanceModifier.width(6.dp))
                            Text(
                                text = "blocks today",
                                style = TextStyle(
                                    color = dimColor,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Normal
                                )
                            )
                        }

                        Spacer(modifier = GlanceModifier.height(4.dp))

                        // Streak
                        Text(
                            text = if (streakDays > 0) "\uD83D\uDD25 $streakDays day streak" else "Start your streak",
                            style = TextStyle(
                                color = limeColor,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium
                            )
                        )

                        Spacer(modifier = GlanceModifier.height(2.dp))

                        // Tower height
                        Text(
                            text = "$totalBlocks blocks tall",
                            style = TextStyle(
                                color = dimColor,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Normal
                            )
                        )
                    }
                }
            }
        }
    }
}
