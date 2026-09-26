package com.example.data.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

enum class Department(
    val code: String,
    val arabicName: String,
    val englishName: String,
    val description: String
) {
    MIS(
        code = "MIS",
        arabicName = "نظم معلومات إدارية",
        englishName = "Management Information Systems",
        description = "دمج بين تكنولوجيا المعلومات والحلول الإدارية وذكاء الأعمال"
    ),
    BUSINESS_ADMIN(
        code = "BA",
        arabicName = "إدارة أعمال",
        englishName = "Business Administration",
        description = "القيادة الاستراتيجية، التسويق، الإدارة المالية، وريادة الأعمال"
    ),
    COMPUTER_SCIENCE(
        code = "CS",
        arabicName = "علوم حاسب",
        englishName = "Computer Science",
        description = "هندسة البرمجيات، هياكل البيانات، الذكاء الاصطناعي، والشبكات"
    );

    companion object {
        fun fromCode(code: String): Department {
            return entries.firstOrNull { it.code.equals(code, ignoreCase = true) } ?: MIS
        }
    }
}

enum class YearGroup(val number: Int, val arabicName: String, val levelDescription: String) {
    YEAR_1(1, "الفرقة الأولى", "المستوى التأسيسي والمفاهيم العامة"),
    YEAR_2(2, "الفرقة الثانية", "المستوى المتوسط والتطبيقات المتخصصة"),
    YEAR_3(3, "الفرقة الثالثة", "المستوى المتقدم والأنظمة المتكاملة"),
    YEAR_4(4, "الفرقة الرابعة", "مستوى التخرج ومشاريع التخرج والجاهزية لسوق العمل");

    companion object {
        fun fromNumber(num: Int): YearGroup {
            return entries.firstOrNull { it.number == num } ?: YEAR_1
        }
    }
}

@Entity(
    tableName = "students",
    indices = [
        Index(value = ["nationalIdHash"], unique = true)
    ]
)
data class Student(
    @PrimaryKey
    val studentCode: String, // e.g. "MIS-3-0451"
    val fullName: String,
    val firstName: String,
    val fatherName: String,
    val grandFatherName: String,
    val familyName: String,
    val nationalIdHash: String, // SHA-256 for secure duplicate prevention
    val nationalIdMasked: String, // e.g. "3020512******4"
    val departmentCode: String,
    val yearGroup: Int,
    val registrationDate: Long = System.currentTimeMillis(),
    val academicYear: String = "2025 / 2026",
    val phone: String = "",
    val rawNationalId: String = ""
)

@Entity(tableName = "lectures")
data class Lecture(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val departmentCode: String,
    val yearGroup: Int,
    val subjectName: String,
    val title: String,
    val doctorName: String,
    val lectureNumber: Int,
    val date: String,
    val summary: String,
    val slidesCount: Int,
    val slideDetails: String, // JSON or descriptive list of slides content
    val fileType: String = "PDF / شرائح عرض",
    val fileUrl: String = "",
    val fileName: String = "",
    val fileSize: String = "",
    val allowSubmissions: Boolean = false, // مفتاح تحكم: السماح للطلاب بتسليم ملفات/واجبات لهذا الدرس
    val doctorNationalId: String = "" // الرقم القومي للدكتور لعزل المحتوى في لوحة التحكم
)

@Entity(tableName = "video_lectures")
data class VideoLecture(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val departmentCode: String,
    val yearGroup: Int,
    val subjectName: String,
    val title: String,
    val doctorName: String,
    val durationMinutes: Int,
    val videoUrl: String,
    val description: String,
    val addedDate: String
)

@Entity(tableName = "exam_schedules")
data class ExamSchedule(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val departmentCode: String,
    val yearGroup: Int,
    val subjectName: String,
    val examDate: String, // YYYY-MM-DD
    val startTime: String, // e.g. "09:00 ص"
    val durationHours: Int = 2,
    val hall: String, // e.g. "مدرج الفراعنة رقم (1)"
    val notes: String = "الالتزام بالزي الرسمي وإحضار الكارنيه الجامعي والأدوات الشخصية"
)

@Entity(tableName = "assignments")
data class Assignment(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val departmentCode: String,
    val yearGroup: Int,
    val subjectName: String,
    val title: String,
    val doctorName: String,
    val description: String,
    val dueDate: String,
    val attachmentUrl: String = "",
    val attachmentName: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val doctorNationalId: String = ""
)

@Entity(tableName = "assignment_submissions")
data class AssignmentSubmission(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val assignmentId: Long,
    val studentName: String,
    val studentNationalId: String,
    val studentCode: String,
    val fileUrl: String,
    val fileName: String,
    val formattedSize: String = "",
    val submissionDate: Long = System.currentTimeMillis(),
    val notes: String = ""
)

data class DoctorProfile(
    val name: String,
    val nationalId: String,
    val departmentCode: String,
    val departmentName: String
)


@Entity(tableName = "academic_cloud_files")
data class AcademicCloudFile(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val subjectName: String,
    val departmentCode: String,
    val yearGroup: Int,
    val category: String, // "محاضرة", "واجب", "شيت عملي", "ملخص"
    val originalFileName: String,
    val uniqueFileName: String,
    val fileExtension: String,
    val fileSize: Long,
    val formattedSize: String,
    val downloadUrl: String,
    val uploadedAt: Long = System.currentTimeMillis()
)

