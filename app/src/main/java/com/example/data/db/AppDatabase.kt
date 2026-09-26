package com.example.data.db

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.Update
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.model.AcademicCloudFile
import com.example.data.model.Assignment
import com.example.data.model.AssignmentSubmission
import com.example.data.model.ExamSchedule
import com.example.data.model.Lecture
import com.example.data.model.Student
import com.example.data.model.VideoLecture
import com.example.data.security.SecurityUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

@Dao
interface StudentDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStudent(student: Student)

    @Query("SELECT * FROM students WHERE studentCode = :code LIMIT 1")
    suspend fun getStudentByCode(code: String): Student?

    @Query("SELECT * FROM students WHERE nationalIdHash = :hash LIMIT 1")
    suspend fun getStudentByNationalIdHash(hash: String): Student?

    @Query("SELECT * FROM students ORDER BY registrationDate DESC")
    fun getAllStudents(): Flow<List<Student>>

    @Query("SELECT * FROM students WHERE departmentCode = :deptCode AND yearGroup = :year ORDER BY registrationDate DESC")
    fun getStudentsByDeptAndYear(deptCode: String, year: Int): Flow<List<Student>>

    @Query("SELECT COUNT(*) FROM students")
    suspend fun getStudentCount(): Int
}

@Dao
interface LectureDao {
    @Query("SELECT * FROM lectures WHERE departmentCode = :deptCode AND yearGroup = :year ORDER BY lectureNumber ASC")
    fun getLectures(deptCode: String, year: Int): Flow<List<Lecture>>

    @Query("SELECT * FROM lectures ORDER BY id DESC")
    fun getAllLectures(): Flow<List<Lecture>>

    @Query("SELECT * FROM lectures WHERE doctorNationalId = :doctorNationalId ORDER BY id DESC")
    fun getLecturesForDoctor(doctorNationalId: String): Flow<List<Lecture>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLecture(lecture: Lecture): Long

    @Update
    suspend fun updateLecture(lecture: Lecture)

    @Delete
    suspend fun deleteLecture(lecture: Lecture)
}

@Dao
interface VideoLectureDao {
    @Query("SELECT * FROM video_lectures WHERE departmentCode = :deptCode AND yearGroup = :year ORDER BY id DESC")
    fun getVideos(deptCode: String, year: Int): Flow<List<VideoLecture>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVideo(video: VideoLecture): Long

    @Delete
    suspend fun deleteVideo(video: VideoLecture)
}

@Dao
interface ExamDao {
    @Query("SELECT * FROM exam_schedules WHERE departmentCode = :deptCode AND yearGroup = :year ORDER BY examDate ASC")
    fun getExams(deptCode: String, year: Int): Flow<List<ExamSchedule>>

    @Query("SELECT * FROM exam_schedules ORDER BY examDate ASC")
    fun getAllExams(): Flow<List<ExamSchedule>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExam(exam: ExamSchedule): Long

    @Delete
    suspend fun deleteExam(exam: ExamSchedule)
}

@Dao
interface AcademicCloudFileDao {
    @Query("SELECT * FROM academic_cloud_files WHERE departmentCode = :deptCode AND yearGroup = :year ORDER BY id DESC")
    fun getFiles(deptCode: String, year: Int): Flow<List<AcademicCloudFile>>

    @Query("SELECT * FROM academic_cloud_files ORDER BY id DESC")
    fun getAllFiles(): Flow<List<AcademicCloudFile>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFile(file: AcademicCloudFile): Long

    @Delete
    suspend fun deleteFile(file: AcademicCloudFile)
}

@Dao
interface AssignmentDao {
    @Query("SELECT * FROM assignments WHERE departmentCode = :deptCode AND yearGroup = :year ORDER BY id DESC")
    fun getAssignments(deptCode: String, year: Int): Flow<List<Assignment>>

    @Query("SELECT * FROM assignments ORDER BY id DESC")
    fun getAllAssignments(): Flow<List<Assignment>>

    @Query("SELECT * FROM assignments WHERE doctorNationalId = :doctorNationalId ORDER BY id DESC")
    fun getAssignmentsForDoctor(doctorNationalId: String): Flow<List<Assignment>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAssignment(assignment: Assignment): Long

    @Delete
    suspend fun deleteAssignment(assignment: Assignment)
}

@Dao
interface AssignmentSubmissionDao {
    @Query("SELECT * FROM assignment_submissions WHERE assignmentId = :assignmentId ORDER BY submissionDate DESC")
    fun getSubmissionsForAssignment(assignmentId: Long): Flow<List<AssignmentSubmission>>

    @Query("SELECT * FROM assignment_submissions WHERE studentNationalId = :nationalId ORDER BY submissionDate DESC")
    fun getSubmissionsForStudent(nationalId: String): Flow<List<AssignmentSubmission>>

    @Query("SELECT * FROM assignment_submissions ORDER BY submissionDate DESC")
    fun getAllSubmissions(): Flow<List<AssignmentSubmission>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSubmission(submission: AssignmentSubmission): Long
}

@Database(
    entities = [
        Student::class,
        Lecture::class,
        VideoLecture::class,
        ExamSchedule::class,
        AcademicCloudFile::class,
        Assignment::class,
        AssignmentSubmission::class
    ],
    version = 4,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun studentDao(): StudentDao
    abstract fun lectureDao(): LectureDao
    abstract fun videoLectureDao(): VideoLectureDao
    abstract fun examDao(): ExamDao
    abstract fun academicCloudFileDao(): AcademicCloudFileDao
    abstract fun assignmentDao(): AssignmentDao
    abstract fun assignmentSubmissionDao(): AssignmentSubmissionDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "pharaohs_university_db"
                )
                    .fallbackToDestructiveMigration()
                    .addCallback(DatabaseCallback(scope))
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }

    private class DatabaseCallback(
        private val scope: CoroutineScope
    ) : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            INSTANCE?.let { database ->
                scope.launch(Dispatchers.IO) {
                    populateInitialData(database)
                }
            }
        }
    }
}

// نظيف تماماً من البيانات الوهمية - يتم الاعتماد كلياً على Supabase والرفع الفعلي
suspend fun populateInitialData(database: AppDatabase) {
    // No mock students, no fake lectures, no fake exams
}
