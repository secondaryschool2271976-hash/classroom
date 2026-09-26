package com.example.data.security

import android.content.Context
import android.content.SharedPreferences
import com.example.data.model.Student

/**
 * مدير الجلسة المحلية للطالب (Local Session Manager)
 * يحفظ بيانات الطالب المسجل محلياً في الهاتف فقط عبر SharedPreferences
 * بحيث يفتح الهاتف مباشرة على كارنيه هذا الطالب دون مشاركة بياناته مع أي مستخدم آخر على جهاز مختلف.
 */
object StudentSessionManager {

    private const val PREF_NAME = "pharaohs_student_local_session"
    private const val KEY_IS_LOGGED_IN = "is_logged_in"
    private const val KEY_STUDENT_CODE = "student_code"
    private const val KEY_FULL_NAME = "full_name"
    private const val KEY_FIRST_NAME = "first_name"
    private const val KEY_FATHER_NAME = "father_name"
    private const val KEY_GRAND_FATHER_NAME = "grand_father_name"
    private const val KEY_FAMILY_NAME = "family_name"
    private const val KEY_NATIONAL_ID_HASH = "national_id_hash"
    private const val KEY_NATIONAL_ID_MASKED = "national_id_masked"
    private const val KEY_RAW_NATIONAL_ID = "raw_national_id"
    private const val KEY_DEPARTMENT_CODE = "department_code"
    private const val KEY_YEAR_GROUP = "year_group"
    private const val KEY_ACADEMIC_YEAR = "academic_year"
    private const val KEY_PHONE = "phone"
    private const val KEY_REGISTRATION_DATE = "registration_date"

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    }

    /**
     * حفظ جلسة الطالب محلياً على هذا الجهاز فقط
     */
    fun saveStudentSession(context: Context, student: Student) {
        try {
            getPrefs(context).edit().apply {
                putBoolean(KEY_IS_LOGGED_IN, true)
                putString(KEY_STUDENT_CODE, student.studentCode)
                putString(KEY_FULL_NAME, student.fullName)
                putString(KEY_FIRST_NAME, student.firstName)
                putString(KEY_FATHER_NAME, student.fatherName)
                putString(KEY_GRAND_FATHER_NAME, student.grandFatherName)
                putString(KEY_FAMILY_NAME, student.familyName)
                putString(KEY_NATIONAL_ID_HASH, student.nationalIdHash)
                putString(KEY_NATIONAL_ID_MASKED, student.nationalIdMasked)
                putString(KEY_RAW_NATIONAL_ID, student.rawNationalId)
                putString(KEY_DEPARTMENT_CODE, student.departmentCode)
                putInt(KEY_YEAR_GROUP, student.yearGroup)
                putString(KEY_ACADEMIC_YEAR, student.academicYear)
                putString(KEY_PHONE, student.phone)
                putLong(KEY_REGISTRATION_DATE, student.registrationDate)
                apply()
            }
        } catch (_: Exception) {
            // Safe fallback
        }
    }

    /**
     * جلب بيانات الطالب المخزن محلياً كجلسة مستمرة
     */
    fun getSavedStudentSession(context: Context): Student? {
        return try {
            val prefs = getPrefs(context)
            if (!prefs.getBoolean(KEY_IS_LOGGED_IN, false)) {
                return null
            }

            val code = prefs.getString(KEY_STUDENT_CODE, null) ?: return null
            val fullName = prefs.getString(KEY_FULL_NAME, "طالب") ?: "طالب"
            val rawId = prefs.getString(KEY_RAW_NATIONAL_ID, "") ?: ""
            val maskedId = prefs.getString(KEY_NATIONAL_ID_MASKED, "") ?: SecurityUtils.maskNationalId(rawId)
            val hashId = prefs.getString(KEY_NATIONAL_ID_HASH, "") ?: SecurityUtils.hashNationalId(rawId)
            val deptCode = prefs.getString(KEY_DEPARTMENT_CODE, "MIS") ?: "MIS"
            val yearGroup = prefs.getInt(KEY_YEAR_GROUP, 1).coerceIn(1, 4)
            val academicYear = prefs.getString(KEY_ACADEMIC_YEAR, "الفرقة الأولى - نظم معلومات إدارية") ?: "الفرقة الأولى"
            val phone = prefs.getString(KEY_PHONE, "") ?: ""
            val fName = prefs.getString(KEY_FIRST_NAME, fullName.split(" ").firstOrNull() ?: "طالب") ?: "طالب"
            val faName = prefs.getString(KEY_FATHER_NAME, "") ?: ""
            val gName = prefs.getString(KEY_GRAND_FATHER_NAME, "") ?: ""
            val famName = prefs.getString(KEY_FAMILY_NAME, "") ?: ""
            val regDate = prefs.getLong(KEY_REGISTRATION_DATE, System.currentTimeMillis())

            Student(
                studentCode = code,
                fullName = fullName,
                firstName = fName,
                fatherName = faName,
                grandFatherName = gName,
                familyName = famName,
                nationalIdHash = hashId,
                nationalIdMasked = maskedId,
                departmentCode = deptCode,
                yearGroup = yearGroup,
                registrationDate = regDate,
                academicYear = academicYear,
                phone = phone,
                rawNationalId = rawId
            )
        } catch (_: Exception) {
            null
        }
    }

    /**
     * تسجيل خروج الطالب وحذف الجلسة المحلية من هذا الهاتف
     */
    fun clearStudentSession(context: Context) {
        try {
            getPrefs(context).edit().clear().apply()
        } catch (_: Exception) {
            // Ignore
        }
    }

    /**
     * التحقق مما إذا كانت هناك جلسة طالب نشطة
     */
    fun hasActiveStudentSession(context: Context): Boolean {
        return try {
            getPrefs(context).getBoolean(KEY_IS_LOGGED_IN, false)
        } catch (_: Exception) {
            false
        }
    }
}
