package com.example.data.supabase

/**
 * إعدادات الاتصال بقاعدة بيانات Supabase السحابية
 */
object SupabaseConfig {
    const val PROJECT_URL = "https://qenuugadpzkhicimlnek.supabase.co"
    const val PUBLISHABLE_ANON_KEY = "sb_publishable_1aDBCueKz3G-_rY6N7Tqhw_6esOd6jI"
    const val STUDENT_TABLE = "student"
    const val REST_STUDENT_URL = "$PROJECT_URL/rest/v1/$STUDENT_TABLE"
}
