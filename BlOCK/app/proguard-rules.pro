# BlOCK ProGuard rules

# Keep Room entities
-keep class com.block.app.data.db.** { *; }

# Keep Rule model for JSON parsing
-keep class com.block.app.data.rules.** { *; }

# Keep accessibility service
-keep class com.block.app.engine.BlockAccessibilityService { *; }

# Keep device admin receiver
-keep class com.block.app.admin.BlockDeviceAdminReceiver { *; }

# Keep widget receiver
-keep class com.block.app.widget.** { *; }
