package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.security.SecurityUtils
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("الفراعنة", appName)
    }

    @Test
    fun `test valid Egyptian national ID validation`() {
        // Sample valid 14-digit Egyptian National ID for birth year 2002, May 12, Cairo (01)
        val validId = "30205120101234"
        val info = SecurityUtils.validateEgyptianNationalId(validId)

        assertTrue(info.isValid)
        assertEquals("12/05/2002", info.birthDate)
        assertEquals("القاهرة", info.governorate)
    }

    @Test
    fun `test invalid Egyptian national ID validation length and format`() {
        // Less than 14 digits
        val shortId = "302051201"
        val infoShort = SecurityUtils.validateEgyptianNationalId(shortId)
        assertFalse(infoShort.isValid)

        // Invalid century (starts with 1)
        val invalidCentury = "10205120101234"
        val infoCentury = SecurityUtils.validateEgyptianNationalId(invalidCentury)
        assertFalse(infoCentury.isValid)
    }

    @Test
    fun `test national ID hashing and masking`() {
        val id = "30205120101234"
        val hash = SecurityUtils.hashNationalId(id)
        assertNotNull(hash)
        assertEquals(64, hash.length) // SHA-256 produces 64 hex characters

        val masked = SecurityUtils.maskNationalId(id)
        assertEquals("3020512******4", masked)
    }

    @Test
    fun `test student code generation`() {
        val codeMIS = SecurityUtils.generateStudentCode("MIS", 3, 10)
        assertTrue(codeMIS.startsWith("MIS-3-"))

        val codeCS = SecurityUtils.generateStudentCode("CS", 1, 5)
        assertTrue(codeCS.startsWith("CS-1-"))
    }

    @Test
    fun `test Cloudflare R2 configurations and public domain URL formatting`() {
        val r2Manager = com.example.data.cloud.CloudflareR2Manager()
        val formatted1 = r2Manager.formatBytes(1024 * 1024 * 2) // 2 MB
        assertEquals("2.00 MB", formatted1)

        val formatted2 = r2Manager.formatBytes(512 * 1024) // 512 KB
        assertEquals("512.00 KB", formatted2)

        assertEquals("lecture-data", com.example.data.cloud.CloudflareR2Config.BUCKET_NAME)
        assertEquals("https://pub-f9710a4d35b34defa42b5ea768a0c9e9.r2.dev", com.example.data.cloud.CloudflareR2Config.PUBLIC_DOMAIN)

        val sampleFile = com.example.data.model.AcademicCloudFile(
            title = "المحاضرة الأولى",
            subjectName = "نظم معلومات",
            departmentCode = "MIS",
            yearGroup = 3,
            category = "محاضرة",
            originalFileName = "lecture1.pdf",
            uniqueFileName = "unique_123.pdf",
            fileExtension = "pdf",
            fileSize = 1048576L,
            formattedSize = "1.00 MB",
            downloadUrl = "${com.example.data.cloud.CloudflareR2Config.PUBLIC_DOMAIN}/uploads/unique_123.pdf"
        )
        assertTrue(sampleFile.downloadUrl.contains("pub-f9710a4d35b34defa42b5ea768a0c9e9.r2.dev"))
        assertTrue(sampleFile.downloadUrl.endsWith("unique_123.pdf"))
    }

    @Test
    fun `test Supabase configuration and student model creation`() {
        assertEquals("https://qenuugadpzkhicimlnek.supabase.co", com.example.data.supabase.SupabaseConfig.PROJECT_URL)
        assertEquals("student", com.example.data.supabase.SupabaseConfig.STUDENT_TABLE)
        assertEquals("sb_publishable_1aDBCueKz3G-_rY6N7Tqhw_6esOd6jI", com.example.data.supabase.SupabaseConfig.PUBLISHABLE_ANON_KEY)

        val student = com.example.data.supabase.SupabaseStudent(
            id = 101L,
            fullName = "طالب جامعي مسجل بالسحابة",
            nationalId = "30205120101234",
            academicYear = "الفرقة الثالثة - نظم معلومات إدارية",
            phone = "01012345678"
        )

        assertEquals("طالب جامعي مسجل بالسحابة", student.fullName)
        assertEquals("30205120101234", student.nationalId)
        assertEquals("الفرقة الثالثة - نظم معلومات إدارية", student.academicYear)
        assertEquals("01012345678", student.phone)
        assertEquals(101L, student.id)

        // Validate National ID
        val nationalIdValidation = com.example.data.security.SecurityUtils.validateEgyptianNationalId("30205120101234")
        assertTrue(nationalIdValidation.isValid)
        assertEquals(2002, nationalIdValidation.birthYear)

        // Validate Phone
        val phoneValidation = com.example.data.security.SecurityUtils.validateEgyptianPhoneNumber("01012345678")
        assertTrue(phoneValidation.isValid)
    }
}

