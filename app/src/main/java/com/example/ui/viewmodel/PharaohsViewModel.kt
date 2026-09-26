package com.example.ui.viewmodel

import android.app.Application
import android.content.Context
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.cloud.CloudflareR2Manager
import com.example.data.db.AppDatabase
import com.example.data.model.AcademicCloudFile
import com.example.data.model.Assignment
import com.example.data.model.AssignmentSubmission
import com.example.data.model.Department
import com.example.data.model.DoctorProfile
import com.example.data.model.ExamSchedule
import com.example.data.model.Lecture
import com.example.data.model.Student
import com.example.data.model.VideoLecture
import com.example.data.model.YearGroup
import com.example.data.repository.PharaohsRepository
import com.example.data.security.NationalIdInfo
import com.example.data.security.SecurityUtils
import com.example.data.security.StudentSessionManager
import com.example.data.supabase.SupabaseManager
import com.example.data.supabase.SupabaseStudent
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class UserRole {
    GUEST,
    STUDENT,
    DOCTOR_ADMIN
}

enum class ContentAddType {
    LECTURE,
    VIDEO,
    EXAM,
    CLOUD_FILE,
    SUPABASE_STUDENT,
    ASSIGNMENT,
    SUBMIT_ASSIGNMENT
}

data class UiState(
    val currentRole: UserRole = UserRole.GUEST,
    val currentStudent: Student? = null,
    val currentDoctorProfile: DoctorProfile? = null,
    val selectedDepartment: Department = Department.MIS,
    val selectedYear: YearGroup = YearGroup.YEAR_1,
    val activeNavTab: Int = 0, // 0: Academic Content, 1: Exams, 2: Student ID Card, 3: Admin Dashboard

    // Supabase Cloud Student Registration State
    val supabaseFullName: String = "",
    val supabaseNationalId: String = "",
    val supabaseAcademicYear: YearGroup = YearGroup.YEAR_1,
    val supabaseDepartment: Department = Department.MIS,
    val supabasePhone: String = "",
    val supabaseNationalIdInfo: NationalIdInfo? = null,
    val supabasePhoneError: String? = null,
    val supabaseGeneratedStudentCode: String = "",
    val isSavingToSupabase: Boolean = false,
    val supabaseSuccessStudent: SupabaseStudent? = null,
    val supabaseErrorMessage: String? = null,
    val supabaseStudentsList: List<SupabaseStudent> = emptyList(),
    val isLoadingSupabaseStudents: Boolean = false,

    // Student Login State (Real Supabase query)
    val loginCodeInput: String = "",
    val isLoggingIn: Boolean = false,
    val loginError: String? = null,

    // Doctor Portal (No PIN: Name, National ID, Specialization)
    val doctorNameInput: String = "",
    val doctorNationalIdInput: String = "",
    val doctorDepartmentInput: Department = Department.MIS,
    val doctorNationalIdInfo: NationalIdInfo? = null,
    val doctorLoginError: String? = null,
    val isDoctorLoggingIn: Boolean = false,

    // Cloudflare R2 Upload State (Direct S3 SigV4)
    val uploadSelectedUri: Uri? = null,
    val uploadSelectedFileName: String = "",
    val uploadSelectedFileSize: String = "",
    val uploadSelectedExtension: String = "",
    val uploadTitleInput: String = "",
    val uploadSubjectInput: String = "",
    val uploadCategoryInput: String = "محاضرة",
    val isUploadingToCloud: Boolean = false,
    val uploadProgressPercent: Float = 0f,
    val uploadErrorMessage: String? = null,
    val lastUploadedCloudFile: AcademicCloudFile? = null,
    val activeCloudFileForDetails: AcademicCloudFile? = null,

    // Add Lecture with Real Cloudflare File
    val lectureSelectedFileUri: Uri? = null,
    val lectureSelectedFileName: String = "",
    val lectureSelectedFileSize: String = "",
    val isUploadingLectureFile: Boolean = false,
    val lectureUploadProgress: Float = 0f,

    // Assignments / Google Classroom System
    val activeAssignmentForDetails: Assignment? = null,
    val activeAssignmentSubmissions: List<AssignmentSubmission> = emptyList(),
    val assignmentSelectedAttachmentUri: Uri? = null,
    val assignmentSelectedAttachmentName: String = "",
    val assignmentSelectedAttachmentSize: String = "",
    val isUploadingAssignmentAttachment: Boolean = false,
    val assignmentAttachmentProgress: Float = 0f,

    // Student Submission of Assignment
    val activeAssignmentToSubmit: Assignment? = null,
    val submissionFileUri: Uri? = null,
    val submissionFileName: String = "",
    val submissionFileSize: String = "",
    val submissionNotes: String = "",
    val isSubmittingAssignment: Boolean = false,
    val submissionProgress: Float = 0f,

    // Modal viewers & Feedback
    val activeLectureForViewer: Lecture? = null,
    val activeVideoForPlayer: VideoLecture? = null,
    val showAddDialog: ContentAddType? = null,
    val snackbarMessage: String? = null
)

@OptIn(ExperimentalCoroutinesApi::class)
class PharaohsViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: PharaohsRepository
    private val r2Manager = CloudflareR2Manager()
    private val supabaseManager = SupabaseManager()

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    init {
        val database = AppDatabase.getDatabase(application, viewModelScope)
        repository = PharaohsRepository(database)
        loadSupabaseStudents()

        // استعادة الجلسة المحلية للطالب المسجل على هذا الجهاز إن وجدت
        val savedStudent = StudentSessionManager.getSavedStudentSession(application)
        if (savedStudent != null) {
            val dept = Department.fromCode(savedStudent.departmentCode)
            val year = YearGroup.fromNumber(savedStudent.yearGroup)
            _uiState.update {
                it.copy(
                    currentStudent = savedStudent,
                    currentRole = UserRole.STUDENT,
                    selectedDepartment = dept,
                    selectedYear = year,
                    activeNavTab = 2 // التوجيه الفوري لكارنيه الطالب
                )
            }
        }
    }

    // Dynamic Flow streams based on selected Department & Year
    val currentLectures: StateFlow<List<Lecture>> = _uiState
        .flatMapLatest { state ->
            repository.getLectures(state.selectedDepartment.code, state.selectedYear.number)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // تدفق محاضرات الدكتور الحصرية المربوطة برقمه القومي
    val doctorIsolatedLectures: StateFlow<List<Lecture>> = _uiState
        .flatMapLatest { state ->
            val docNationalId = state.currentDoctorProfile?.nationalId ?: ""
            if (docNationalId.isNotBlank()) {
                repository.getLecturesForDoctor(docNationalId)
            } else {
                repository.getLectures(state.selectedDepartment.code, state.selectedYear.number)
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val doctorIsolatedAssignments: StateFlow<List<Assignment>> = _uiState
        .flatMapLatest { state ->
            val docNationalId = state.currentDoctorProfile?.nationalId ?: ""
            if (docNationalId.isNotBlank()) {
                repository.getAssignmentsForDoctor(docNationalId)
            } else {
                repository.getAssignments(state.selectedDepartment.code, state.selectedYear.number)
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val currentVideos: StateFlow<List<VideoLecture>> = _uiState
        .flatMapLatest { state ->
            repository.getVideoLectures(state.selectedDepartment.code, state.selectedYear.number)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val currentExams: StateFlow<List<ExamSchedule>> = _uiState
        .flatMapLatest { state ->
            repository.getExamSchedules(state.selectedDepartment.code, state.selectedYear.number)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val currentCloudFiles: StateFlow<List<AcademicCloudFile>> = _uiState
        .flatMapLatest { state ->
            repository.getCloudFiles(state.selectedDepartment.code, state.selectedYear.number)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val currentAssignments: StateFlow<List<Assignment>> = _uiState
        .flatMapLatest { state ->
            repository.getAssignments(state.selectedDepartment.code, state.selectedYear.number)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val currentStudentSubmissions: StateFlow<List<AssignmentSubmission>> = _uiState
        .flatMapLatest { state ->
            val nationalId = state.currentStudent?.rawNationalId ?: ""
            repository.getSubmissionsForStudent(nationalId)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allSubmissions: StateFlow<List<AssignmentSubmission>> = repository.getAllSubmissions()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allCloudFiles: StateFlow<List<AcademicCloudFile>> = repository.getAllCloudFiles()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allExams: StateFlow<List<ExamSchedule>> = repository.getAllExams()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allAssignments: StateFlow<List<Assignment>> = repository.getAllAssignments()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allStudents: StateFlow<List<Student>> = repository.getAllStudents()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // UI actions: تقييد واجهة الطالب بتخصصه وفرقته ومنع التبديل العشوائي
    fun setDepartment(dept: Department) {
        if (_uiState.value.currentRole == UserRole.STUDENT) {
            return // حجب التبديل عن واجهة الطالب
        }
        _uiState.update { it.copy(selectedDepartment = dept) }
    }

    fun setYearGroup(year: YearGroup) {
        if (_uiState.value.currentRole == UserRole.STUDENT) {
            return // حجب التبديل عن واجهة الطالب
        }
        _uiState.update { it.copy(selectedYear = year) }
    }

    fun setActiveNavTab(tabIndex: Int) {
        _uiState.update { it.copy(activeNavTab = tabIndex) }
    }

    // ==========================================
    // Supabase Cloud Student Registration & Validation
    // ==========================================
    fun updateSupabaseFullName(name: String) {
        _uiState.update {
            it.copy(
                supabaseFullName = name,
                supabaseErrorMessage = null
            )
        }
    }

    fun updateSupabaseNationalId(id: String) {
        val filtered = id.filter { it.isDigit() }.take(14)
        val info = if (filtered.length == 14) {
            SecurityUtils.validateEgyptianNationalId(filtered)
        } else if (filtered.isNotEmpty()) {
            NationalIdInfo(
                isValid = false,
                errorMessage = "المتبقي ${14 - filtered.length} أرقام لاكتمال الرقم القومي (14 رقماً)"
            )
        } else {
            null
        }

        val code = if (filtered.length >= 4) {
            val dept = _uiState.value.supabaseDepartment
            val yr = _uiState.value.supabaseAcademicYear
            "${dept.code}-${yr.number}-${filtered.takeLast(4)}"
        } else ""

        _uiState.update {
            it.copy(
                supabaseNationalId = filtered,
                supabaseNationalIdInfo = info,
                supabaseGeneratedStudentCode = code,
                supabaseErrorMessage = null
            )
        }
    }

    fun updateSupabaseAcademicYear(year: YearGroup) {
        _uiState.update {
            val code = if (it.supabaseNationalId.length >= 4) {
                "${it.supabaseDepartment.code}-${year.number}-${it.supabaseNationalId.takeLast(4)}"
            } else ""
            it.copy(
                supabaseAcademicYear = year,
                supabaseGeneratedStudentCode = code,
                supabaseErrorMessage = null
            )
        }
    }

    fun updateSupabaseDepartment(dept: Department) {
        _uiState.update {
            val code = if (it.supabaseNationalId.length >= 4) {
                "${dept.code}-${it.supabaseAcademicYear.number}-${it.supabaseNationalId.takeLast(4)}"
            } else ""
            it.copy(
                supabaseDepartment = dept,
                supabaseGeneratedStudentCode = code,
                supabaseErrorMessage = null
            )
        }
    }

    fun updateSupabasePhone(phone: String) {
        val filtered = phone.filter { it.isDigit() }.take(11)
        val err = if (filtered.length == 11) {
            SecurityUtils.validateEgyptianPhone(filtered)
        } else null

        _uiState.update {
            it.copy(
                supabasePhone = filtered,
                supabasePhoneError = err,
                supabaseErrorMessage = null
            )
        }
    }

    fun registerStudentToSupabase() {
        val s = _uiState.value
        val name = s.supabaseFullName.trim()
        val nationalId = s.supabaseNationalId.trim()
        val phone = s.supabasePhone.trim()
        val dept = s.supabaseDepartment
        val year = s.supabaseAcademicYear

        // Validation
        if (name.isBlank() || name.length < 5) {
            _uiState.update { it.copy(supabaseErrorMessage = "يرجى كتابة اسم الطالب ثلاثياً أو رباعياً على الأقل") }
            return
        }

        val nationalIdValidation = SecurityUtils.validateEgyptianNationalId(nationalId)
        if (!nationalIdValidation.isValid) {
            _uiState.update {
                it.copy(
                    supabaseErrorMessage = nationalIdValidation.errorMessage ?: "الرقم القومي غير صالح وفقاً للمعيار المصري"
                )
            }
            return
        }

        val phoneErr = SecurityUtils.validateEgyptianPhone(phone)
        if (phoneErr != null) {
            _uiState.update { it.copy(supabaseErrorMessage = phoneErr) }
            return
        }

        val academicYearText = "${year.arabicName} - ${dept.arabicName}"
        val studentCode = "${dept.code}-${year.number}-${nationalId.takeLast(4)}"

        _uiState.update { it.copy(isSavingToSupabase = true, supabaseErrorMessage = null) }

        viewModelScope.launch {
            val result = supabaseManager.insertStudent(
                fullName = name,
                nationalId = nationalId,
                academicYear = academicYearText,
                phone = phone
            )

            result.onSuccess { supaStudent ->
                // Construct and cache local student entity
                val nameParts = name.split("\\s+".toRegex())
                val fName = nameParts.getOrNull(0) ?: "طالب"
                val faName = nameParts.getOrNull(1) ?: ""
                val gName = nameParts.getOrNull(2) ?: ""
                val famName = nameParts.drop(3).joinToString(" ").ifEmpty { faName }
                val masked = SecurityUtils.maskNationalId(nationalId)
                val hash = SecurityUtils.hashNationalId(nationalId)

                val localStudent = Student(
                    studentCode = studentCode,
                    fullName = name,
                    firstName = fName,
                    fatherName = faName,
                    grandFatherName = gName,
                    familyName = famName,
                    nationalIdHash = hash,
                    nationalIdMasked = masked,
                    departmentCode = dept.code,
                    yearGroup = year.number,
                    registrationDate = System.currentTimeMillis(),
                    academicYear = academicYearText,
                    phone = phone,
                    rawNationalId = nationalId
                )

                repository.saveStudent(localStudent)
                StudentSessionManager.saveStudentSession(getApplication(), localStudent)

                _uiState.update {
                    it.copy(
                        isSavingToSupabase = false,
                        supabaseSuccessStudent = supaStudent,
                        currentStudent = localStudent,
                        currentRole = UserRole.STUDENT,
                        selectedDepartment = dept,
                        selectedYear = year,
                        activeNavTab = 2, // Take directly to their Digital Student ID Card!
                        supabaseFullName = "",
                        supabaseNationalId = "",
                        supabasePhone = "",
                        showAddDialog = null,
                        snackbarMessage = "تم القيد بنجاح في سحابة Supabase! كود الطالب: $studentCode"
                    )
                }
                loadSupabaseStudents()
            }.onFailure { err ->
                _uiState.update {
                    it.copy(
                        isSavingToSupabase = false,
                        supabaseErrorMessage = err.message ?: "فشلت عملية الحفظ في Supabase"
                    )
                }
            }
        }
    }

    fun dismissSupabaseSuccessDialog() {
        _uiState.update { it.copy(supabaseSuccessStudent = null) }
    }

    fun loadSupabaseStudents() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingSupabaseStudents = true) }
            val result = supabaseManager.getStudents()
            result.onSuccess { list ->
                _uiState.update { it.copy(supabaseStudentsList = list, isLoadingSupabaseStudents = false) }
            }.onFailure {
                _uiState.update { it.copy(isLoadingSupabaseStudents = false) }
            }
        }
    }

    // ==========================================
    // Real Supabase Student Login (محمي تماماً ضد الكراش مع حفظ الجلسة محلياً)
    // ==========================================
    fun updateLoginInput(input: String) {
        _uiState.update { it.copy(loginCodeInput = input, loginError = null) }
    }

    fun loginStudent() {
        val input = _uiState.value.loginCodeInput.trim()
        if (input.isEmpty()) {
            _uiState.update { it.copy(loginError = "يرجى إدخال الرقم القومي (14 رقماً)") }
            return
        }

        _uiState.update { it.copy(isLoggingIn = true, loginError = null) }

        viewModelScope.launch {
            try {
                android.util.Log.d("PharaohsLogin", "Initiating login request for: $input")
                var foundStudent: Student? = null
                var networkErrorDetail: String? = null

                // 1. Direct query to Supabase by national ID
                if (input.length == 14 && input.all { it.isDigit() }) {
                    val supaResult = supabaseManager.getStudentByNationalId(input)
                    supaResult.onSuccess { supaStudent ->
                        if (supaStudent != null) {
                            android.util.Log.d("PharaohsLogin", "Student found via direct query: ${supaStudent.fullName}")
                            foundStudent = mapSupabaseStudentToStudent(supaStudent)
                        } else {
                            android.util.Log.d("PharaohsLogin", "No direct student returned for national ID: $input")
                        }
                    }.onFailure { err ->
                        android.util.Log.e("PharaohsLogin", "Supabase direct query failed", err)
                        networkErrorDetail = err.message
                    }
                }

                // 2. If not found yet, query all Supabase students to check by code, phone, or ID
                if (foundStudent == null) {
                    val allSupa = supabaseManager.getStudents()
                    allSupa.onSuccess { list ->
                        val match = list.firstOrNull { s ->
                            s.nationalId.trim() == input ||
                            s.phone.trim() == input ||
                            (s.studentCode != null && s.studentCode.equals(input, ignoreCase = true)) ||
                            generateStudentCodeFromSupabase(s).equals(input, ignoreCase = true)
                        }
                        if (match != null) {
                            android.util.Log.d("PharaohsLogin", "Student found in Supabase roster: ${match.fullName}")
                            foundStudent = mapSupabaseStudentToStudent(match)
                        }
                    }.onFailure { err ->
                        android.util.Log.e("PharaohsLogin", "Supabase getStudents query failed", err)
                        if (networkErrorDetail == null) networkErrorDetail = err.message
                    }
                }

                // 3. Fallback to local Room database (for offline use)
                if (foundStudent == null) {
                    try {
                        if (input.length == 14 && input.all { it.isDigit() }) {
                            foundStudent = repository.loginWithNationalId(input).getOrNull()
                        } else {
                            foundStudent = repository.loginWithStudentCode(input).getOrNull()
                        }
                        if (foundStudent != null) {
                            android.util.Log.d("PharaohsLogin", "Student authenticated via local Room database cache")
                        }
                    } catch (dbErr: Exception) {
                        android.util.Log.e("PharaohsLogin", "Local database query exception", dbErr)
                    }
                }

                if (foundStudent != null) {
                    val student = foundStudent!!
                    try {
                        repository.saveStudent(student)
                    } catch (e: Exception) {
                        android.util.Log.e("PharaohsLogin", "Notice: Room cache update exception", e)
                    }

                    // حفظ وتحديث جلسة الطالب محلياً في ذاكرة الهاتف فقط (Local Session)
                    try {
                        StudentSessionManager.saveStudentSession(getApplication(), student)
                    } catch (e: Exception) {
                        android.util.Log.e("PharaohsLogin", "Notice: Prefs session update exception", e)
                    }

                    _uiState.update {
                        it.copy(
                            isLoggingIn = false,
                            currentStudent = student,
                            currentRole = UserRole.STUDENT,
                            selectedDepartment = Department.fromCode(student.departmentCode),
                            selectedYear = YearGroup.fromNumber(student.yearGroup),
                            loginCodeInput = "",
                            activeNavTab = 2, // التوجيه الفوري للكارنيه الجامعي
                            snackbarMessage = "مرحباً بك يا ${student.fullName} - تم تسجيل الدخول بنجاح"
                        )
                    }
                } else {
                    val errorExplanation = if (networkErrorDetail != null && (networkErrorDetail!!.contains("اتصال") || networkErrorDetail!!.contains("timeout") || networkErrorDetail!!.contains("Connect") || networkErrorDetail!!.contains("Unable to resolve host"))) {
                        "تعذر الاتصال بخادم قاعدة بيانات Supabase. يرجى التحقق من اتصالك بالإنترنت والمحاولة مجدداً."
                    } else {
                        "الرقم القومي ($input) غير مقيد في السحابة. يرجى التوجه لزر 'قيد طالب جديد' للتسجيل أولاً."
                    }
                    android.util.Log.e("PharaohsLogin", "Login denied: $errorExplanation | Details: $networkErrorDetail")
                    _uiState.update {
                        it.copy(
                            isLoggingIn = false,
                            loginError = errorExplanation
                        )
                    }
                }
            } catch (e: Throwable) {
                android.util.Log.e("PharaohsLogin", "Unexpected error during loginStudent", e)
                _uiState.update {
                    it.copy(
                        isLoggingIn = false,
                        loginError = "خطأ أثناء تسجيل الدخول: ${e.localizedMessage ?: "يرجى التحقق من الرقم والمحاولة ثانية"}"
                    )
                }
            }
        }
    }

    private fun generateStudentCodeFromSupabase(s: SupabaseStudent): String {
        val yr = when {
            s.academicYear.contains("الرابعة") || s.academicYear.contains("4") -> 4
            s.academicYear.contains("الثالثة") || s.academicYear.contains("3") -> 3
            s.academicYear.contains("الثانية") || s.academicYear.contains("2") -> 2
            else -> 1
        }
        val dept = when {
            s.academicYear.contains("علوم حاسب") || s.academicYear.contains("CS", ignoreCase = true) -> "CS"
            s.academicYear.contains("إدارة أعمال") || s.academicYear.contains("BA", ignoreCase = true) -> "BA"
            else -> "MIS"
        }
        val suffix = if (s.nationalId.trim().length >= 4) s.nationalId.trim().takeLast(4) else "0001"
        return "$dept-$yr-$suffix"
    }

    private fun mapSupabaseStudentToStudent(s: SupabaseStudent): Student {
        val cleanNationalId = s.nationalId.trim().ifEmpty { "30101010101234" }
        val nationalIdHash = SecurityUtils.hashNationalId(cleanNationalId)
        val nationalIdMasked = SecurityUtils.maskNationalId(cleanNationalId)

        val yearGroup = when {
            s.academicYear.contains("الرابعة") || s.academicYear.contains("4") -> 4
            s.academicYear.contains("الثالثة") || s.academicYear.contains("3") -> 3
            s.academicYear.contains("الثانية") || s.academicYear.contains("2") -> 2
            else -> 1
        }

        val deptCode = when {
            s.academicYear.contains("علوم حاسب") || s.academicYear.contains("CS", ignoreCase = true) -> "CS"
            s.academicYear.contains("إدارة أعمال") || s.academicYear.contains("BA", ignoreCase = true) -> "BA"
            else -> "MIS"
        }

        val suffix = if (cleanNationalId.length >= 4) cleanNationalId.takeLast(4) else "0001"
        val studentCode = s.studentCode?.ifBlank { null } ?: "$deptCode-$yearGroup-$suffix"
        val fullNameSafe = s.fullName.trim().ifEmpty { "طالب مقيد" }
        val nameParts = fullNameSafe.split("\\s+".toRegex()).filter { it.isNotBlank() }
        val fName = nameParts.getOrNull(0) ?: "طالب"
        val faName = nameParts.getOrNull(1) ?: ""
        val gName = nameParts.getOrNull(2) ?: ""
        val famName = if (nameParts.size > 3) nameParts.drop(3).joinToString(" ") else faName
        val safePhone = s.phone.trim()

        return Student(
            studentCode = studentCode,
            fullName = fullNameSafe,
            firstName = fName,
            fatherName = faName,
            grandFatherName = gName,
            familyName = famName,
            nationalIdHash = nationalIdHash,
            nationalIdMasked = nationalIdMasked,
            departmentCode = deptCode,
            yearGroup = yearGroup,
            registrationDate = System.currentTimeMillis(),
            academicYear = s.academicYear.ifBlank { "الفرقة ${YearGroup.fromNumber(yearGroup).arabicName}" },
            phone = safePhone,
            rawNationalId = cleanNationalId
        )
    }

    // ==========================================
    // Doctor Portal (No PIN: Name + National ID + Dept)
    // ==========================================
    fun updateDoctorName(name: String) {
        _uiState.update { it.copy(doctorNameInput = name, doctorLoginError = null) }
    }

    fun updateDoctorNationalId(id: String) {
        val filtered = id.filter { it.isDigit() }.take(14)
        val info = if (filtered.length == 14) {
            SecurityUtils.validateEgyptianNationalId(filtered)
        } else if (filtered.isNotEmpty()) {
            NationalIdInfo(
                isValid = false,
                errorMessage = "المتبقي ${14 - filtered.length} أرقام لاكتمال الرقم القومي"
            )
        } else null

        _uiState.update {
            it.copy(
                doctorNationalIdInput = filtered,
                doctorNationalIdInfo = info,
                doctorLoginError = null
            )
        }
    }

    fun updateDoctorDepartment(dept: Department) {
        _uiState.update { it.copy(doctorDepartmentInput = dept, doctorLoginError = null) }
    }

    fun loginDoctorAdmin() {
        val s = _uiState.value
        val name = s.doctorNameInput.trim()
        val nationalId = s.doctorNationalIdInput.trim()
        val dept = s.doctorDepartmentInput

        if (name.isBlank() || name.length < 3) {
            _uiState.update { it.copy(doctorLoginError = "يرجى إدخال اسم الدكتور / عضو هيئة التدريس") }
            return
        }

        val idInfo = SecurityUtils.validateEgyptianNationalId(nationalId)
        if (!idInfo.isValid) {
            _uiState.update {
                it.copy(
                    doctorLoginError = idInfo.errorMessage ?: "الرقم القومي غير صحيح (يجب أن يتكون من 14 رقماً مصرياً)"
                )
            }
            return
        }

        val doctorProfile = DoctorProfile(
            name = name,
            nationalId = nationalId,
            departmentCode = dept.code,
            departmentName = dept.arabicName
        )

        _uiState.update {
            it.copy(
                currentRole = UserRole.DOCTOR_ADMIN,
                currentDoctorProfile = doctorProfile,
                selectedDepartment = dept,
                doctorLoginError = null,
                activeNavTab = 0,
                snackbarMessage = "مرحباً بك د. $name - صلاحيات التدريس لقسم ${dept.arabicName}"
            )
        }
    }

    fun logout() {
        StudentSessionManager.clearStudentSession(getApplication())
        _uiState.update {
            it.copy(
                currentRole = UserRole.GUEST,
                currentStudent = null,
                currentDoctorProfile = null,
                activeNavTab = 0,
                loginCodeInput = "",
                snackbarMessage = "تم تسجيل الخروج بنجاح"
            )
        }
    }

    fun openLectureViewer(lecture: Lecture?) {
        _uiState.update { it.copy(activeLectureForViewer = lecture) }
    }

    fun openVideoPlayer(video: VideoLecture?) {
        _uiState.update { it.copy(activeVideoForPlayer = video) }
    }

    fun setShowAddDialog(type: ContentAddType?) {
        _uiState.update { it.copy(showAddDialog = type) }
    }

    fun clearSnackbar() {
        _uiState.update { it.copy(snackbarMessage = null) }
    }

    // ==========================================
    // Lecture Management with Real Cloudflare R2 Upload
    // ==========================================
    fun selectLectureFile(uri: Uri, name: String, sizeStr: String) {
        _uiState.update {
            it.copy(
                lectureSelectedFileUri = uri,
                lectureSelectedFileName = name,
                lectureSelectedFileSize = sizeStr
            )
        }
    }

    fun clearLectureSelectedFile() {
        _uiState.update {
            it.copy(
                lectureSelectedFileUri = null,
                lectureSelectedFileName = "",
                lectureSelectedFileSize = ""
            )
        }
    }

    fun addLectureWithCloudFile(
        context: Context,
        subject: String,
        title: String,
        doctor: String,
        lectureNum: Int,
        date: String,
        summary: String,
        slidesCount: Int,
        slideDetails: String,
        allowSubmissions: Boolean = false
    ) {
        val s = _uiState.value
        val fileUri = s.lectureSelectedFileUri
        val docNationalId = s.currentDoctorProfile?.nationalId ?: ""

        _uiState.update {
            it.copy(
                isUploadingLectureFile = true,
                lectureUploadProgress = 0.05f
            )
        }

        viewModelScope.launch {
            var fileUrl = ""
            var fileName = s.lectureSelectedFileName
            var fileSize = s.lectureSelectedFileSize
            var fileType = "PDF / شرائح عرض"

            if (fileUri != null) {
                val uploadResult = r2Manager.uploadAcademicFile(
                    context = context,
                    uri = fileUri,
                    title = title,
                    subjectName = subject,
                    departmentCode = s.selectedDepartment.code,
                    yearGroup = s.selectedYear.number,
                    category = "محاضرة",
                    onProgress = { p ->
                        _uiState.update { it.copy(lectureUploadProgress = p) }
                    }
                )

                uploadResult.onSuccess { cloudFile ->
                    fileUrl = cloudFile.downloadUrl
                    fileName = cloudFile.originalFileName
                    fileSize = cloudFile.formattedSize
                    fileType = "${cloudFile.fileExtension.uppercase()} ملف محاضرة"
                    repository.saveCloudFile(cloudFile)
                }
            }

            val lecture = Lecture(
                departmentCode = s.selectedDepartment.code,
                yearGroup = s.selectedYear.number,
                subjectName = subject,
                title = title,
                doctorName = doctor.ifBlank { s.currentDoctorProfile?.name ?: "هيئة التدريس" },
                lectureNumber = lectureNum,
                date = date,
                summary = summary,
                slidesCount = slidesCount,
                slideDetails = slideDetails,
                fileType = fileType,
                fileUrl = fileUrl,
                fileName = fileName,
                fileSize = fileSize,
                allowSubmissions = allowSubmissions,
                doctorNationalId = docNationalId
            )

            repository.addLecture(lecture)

            _uiState.update {
                it.copy(
                    isUploadingLectureFile = false,
                    lectureUploadProgress = 1.0f,
                    lectureSelectedFileUri = null,
                    lectureSelectedFileName = "",
                    lectureSelectedFileSize = "",
                    showAddDialog = null,
                    snackbarMessage = "تم نشر المحاضرة بنجاح مع ملف Cloudflare R2 المرفق!"
                )
            }
        }
    }

    fun addLecture(
        subject: String,
        title: String,
        doctor: String,
        lectureNum: Int,
        date: String,
        summary: String,
        slidesCount: Int,
        slideDetails: String,
        allowSubmissions: Boolean = false
    ) {
        val s = _uiState.value
        val docNationalId = s.currentDoctorProfile?.nationalId ?: ""
        val lecture = Lecture(
            departmentCode = s.selectedDepartment.code,
            yearGroup = s.selectedYear.number,
            subjectName = subject,
            title = title,
            doctorName = doctor.ifBlank { s.currentDoctorProfile?.name ?: "هيئة التدريس" },
            lectureNumber = lectureNum,
            date = date,
            summary = summary,
            slidesCount = slidesCount,
            slideDetails = slideDetails,
            fileType = "PDF / مستند",
            fileUrl = "",
            fileName = "$title.pdf",
            fileSize = "2.4 MB",
            allowSubmissions = allowSubmissions,
            doctorNationalId = docNationalId
        )
        viewModelScope.launch {
            repository.addLecture(lecture)
            _uiState.update {
                it.copy(
                    showAddDialog = null,
                    snackbarMessage = "تم إضافة المحاضرة بنجاح"
                )
            }
        }
    }

    fun deleteLecture(lecture: Lecture) {
        viewModelScope.launch {
            repository.deleteLecture(lecture)
            _uiState.update { it.copy(snackbarMessage = "تم حذف المحاضرة بنجاح") }
        }
    }

    // ==========================================
    // Google Classroom: Assignments & Submissions
    // ==========================================
    fun selectAssignmentAttachment(uri: Uri, name: String, sizeStr: String) {
        _uiState.update {
            it.copy(
                assignmentSelectedAttachmentUri = uri,
                assignmentSelectedAttachmentName = name,
                assignmentSelectedAttachmentSize = sizeStr
            )
        }
    }

    fun clearAssignmentAttachment() {
        _uiState.update {
            it.copy(
                assignmentSelectedAttachmentUri = null,
                assignmentSelectedAttachmentName = "",
                assignmentSelectedAttachmentSize = ""
            )
        }
    }

    fun addAssignment(
        context: Context,
        subject: String,
        title: String,
        doctor: String,
        description: String,
        dueDate: String
    ) {
        val s = _uiState.value
        val uri = s.assignmentSelectedAttachmentUri
        val docNationalId = s.currentDoctorProfile?.nationalId ?: ""

        _uiState.update {
            it.copy(
                isUploadingAssignmentAttachment = true,
                assignmentAttachmentProgress = 0.05f
            )
        }

        viewModelScope.launch {
            var attachmentUrl = ""
            var attachmentName = s.assignmentSelectedAttachmentName

            if (uri != null) {
                val uploadResult = r2Manager.uploadAcademicFile(
                    context = context,
                    uri = uri,
                    title = "تكليف: $title",
                    subjectName = subject,
                    departmentCode = s.selectedDepartment.code,
                    yearGroup = s.selectedYear.number,
                    category = "واجب",
                    onProgress = { p ->
                        _uiState.update { it.copy(assignmentAttachmentProgress = p) }
                    }
                )

                uploadResult.onSuccess { cloudFile ->
                    attachmentUrl = cloudFile.downloadUrl
                    attachmentName = cloudFile.originalFileName
                    repository.saveCloudFile(cloudFile)
                }
            }

            val assignment = Assignment(
                departmentCode = s.selectedDepartment.code,
                yearGroup = s.selectedYear.number,
                subjectName = subject,
                title = title,
                doctorName = doctor.ifBlank { s.currentDoctorProfile?.name ?: "هيئة التدريس" },
                description = description,
                dueDate = dueDate,
                attachmentUrl = attachmentUrl,
                attachmentName = attachmentName,
                doctorNationalId = docNationalId
            )

            repository.addAssignment(assignment)

            _uiState.update {
                it.copy(
                    isUploadingAssignmentAttachment = false,
                    assignmentAttachmentProgress = 1.0f,
                    assignmentSelectedAttachmentUri = null,
                    assignmentSelectedAttachmentName = "",
                    assignmentSelectedAttachmentSize = "",
                    showAddDialog = null,
                    snackbarMessage = "تم نشر التكليف الدراسي بنجاح للطلاب!"
                )
            }
        }
    }

    fun deleteAssignment(assignment: Assignment) {
        viewModelScope.launch {
            repository.deleteAssignment(assignment)
            _uiState.update { it.copy(snackbarMessage = "تم حذف التكليف الدراسي") }
        }
    }

    fun openAssignmentDetails(assignment: Assignment?) {
        _uiState.update { it.copy(activeAssignmentForDetails = assignment) }
        if (assignment != null) {
            viewModelScope.launch {
                repository.getSubmissionsForAssignment(assignment.id).collect { list ->
                    _uiState.update { it.copy(activeAssignmentSubmissions = list) }
                }
            }
        }
    }

    fun openSubmitLectureDialog(lecture: Lecture) {
        val tempAssignment = Assignment(
            id = -lecture.id, // معرف مميز لتسليم تكليف المحاضرة
            departmentCode = lecture.departmentCode,
            yearGroup = lecture.yearGroup,
            subjectName = lecture.subjectName,
            title = "تكليف: ${lecture.title}",
            doctorName = lecture.doctorName,
            description = "تسليم الواجب/الحل المطلوب في المحاضرة (${lecture.title})",
            dueDate = lecture.date
        )
        openSubmitAssignmentDialog(tempAssignment)
    }

    fun openSubmitAssignmentDialog(assignment: Assignment) {
        _uiState.update {
            it.copy(
                activeAssignmentToSubmit = assignment,
                submissionFileUri = null,
                submissionFileName = "",
                submissionFileSize = "",
                submissionNotes = "",
                showAddDialog = ContentAddType.SUBMIT_ASSIGNMENT
            )
        }
    }

    fun selectSubmissionFile(uri: Uri, name: String, sizeStr: String) {
        _uiState.update {
            it.copy(
                submissionFileUri = uri,
                submissionFileName = name,
                submissionFileSize = sizeStr
            )
        }
    }

    fun updateSubmissionNotes(notes: String) {
        _uiState.update { it.copy(submissionNotes = notes) }
    }

    fun submitAssignmentSolution(context: Context) {
        val s = _uiState.value
        val assignment = s.activeAssignmentToSubmit ?: return
        val uri = s.submissionFileUri
        val student = s.currentStudent

        if (uri == null) {
            _uiState.update { it.copy(snackbarMessage = "يرجى اختيار ملف الحل (PDF أو صورة)") }
            return
        }

        if (student == null) {
            _uiState.update { it.copy(snackbarMessage = "يجب تسجيل الدخول كطالب لتسليم الواجب") }
            return
        }

        _uiState.update {
            it.copy(
                isSubmittingAssignment = true,
                submissionProgress = 0.05f
            )
        }

        viewModelScope.launch {
            val uploadResult = r2Manager.uploadAcademicFile(
                context = context,
                uri = uri,
                title = "حل واجب: ${assignment.title} - ${student.fullName}",
                subjectName = assignment.subjectName,
                departmentCode = student.departmentCode,
                yearGroup = student.yearGroup,
                category = "حل واجب",
                onProgress = { p ->
                    _uiState.update { it.copy(submissionProgress = p) }
                }
            )

            uploadResult.onSuccess { cloudFile ->
                val submission = AssignmentSubmission(
                    assignmentId = assignment.id,
                    studentName = student.fullName,
                    studentNationalId = student.rawNationalId.ifBlank { student.nationalIdMasked },
                    studentCode = student.studentCode,
                    fileUrl = cloudFile.downloadUrl,
                    fileName = cloudFile.originalFileName,
                    formattedSize = cloudFile.formattedSize,
                    submissionDate = System.currentTimeMillis(),
                    notes = s.submissionNotes
                )

                repository.submitAssignment(submission)

                _uiState.update {
                    it.copy(
                        isSubmittingAssignment = false,
                        submissionProgress = 1.0f,
                        activeAssignmentToSubmit = null,
                        submissionFileUri = null,
                        submissionFileName = "",
                        submissionFileSize = "",
                        submissionNotes = "",
                        showAddDialog = null,
                        snackbarMessage = "تم تسليم الواجب بنجاح ورفعه إلى السحابة!"
                    )
                }
            }.onFailure { err ->
                _uiState.update {
                    it.copy(
                        isSubmittingAssignment = false,
                        snackbarMessage = "فشل رفع الواجب: ${err.message}"
                    )
                }
            }
        }
    }

    // ==========================================
    // Videos & Exam Schedules
    // ==========================================
    fun addVideoLecture(
        subject: String,
        title: String,
        doctor: String,
        durationMinutes: Int,
        url: String,
        description: String
    ) {
        viewModelScope.launch {
            val state = _uiState.value
            val video = VideoLecture(
                departmentCode = state.selectedDepartment.code,
                yearGroup = state.selectedYear.number,
                subjectName = subject,
                title = title,
                doctorName = doctor.ifBlank { state.currentDoctorProfile?.name ?: "هيئة التدريس" },
                durationMinutes = durationMinutes,
                videoUrl = url.ifEmpty { "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4" },
                description = description,
                addedDate = "2025-10-20"
            )
            repository.addVideoLecture(video)
            _uiState.update {
                it.copy(
                    showAddDialog = null,
                    snackbarMessage = "تم نشر فيديو المحاضرة بنجاح"
                )
            }
        }
    }

    fun deleteVideo(video: VideoLecture) {
        viewModelScope.launch {
            repository.deleteVideoLecture(video)
            _uiState.update { it.copy(snackbarMessage = "تم حذف الفيديو بنجاح") }
        }
    }

    fun addExamSchedule(
        subject: String,
        date: String,
        startTime: String,
        durationHours: Int,
        hall: String,
        notes: String
    ) {
        viewModelScope.launch {
            val state = _uiState.value
            val exam = ExamSchedule(
                departmentCode = state.selectedDepartment.code,
                yearGroup = state.selectedYear.number,
                subjectName = subject,
                examDate = date,
                startTime = startTime,
                durationHours = durationHours,
                hall = hall,
                notes = notes
            )
            repository.addExamSchedule(exam)
            _uiState.update {
                it.copy(
                    showAddDialog = null,
                    snackbarMessage = "تم إضافة موعد الامتحان بنجاح إلى الجدول"
                )
            }
        }
    }

    fun deleteExam(exam: ExamSchedule) {
        viewModelScope.launch {
            repository.deleteExamSchedule(exam)
            _uiState.update { it.copy(snackbarMessage = "تم حذف الامتحان من الجدول") }
        }
    }

    // ==========================================
    // Cloudflare R2 File Management
    // ==========================================
    fun selectFileForUpload(uri: Uri, name: String, sizeStr: String, ext: String) {
        _uiState.update {
            it.copy(
                uploadSelectedUri = uri,
                uploadSelectedFileName = name,
                uploadSelectedFileSize = sizeStr,
                uploadSelectedExtension = ext,
                uploadTitleInput = if (it.uploadTitleInput.isBlank()) name.substringBeforeLast('.') else it.uploadTitleInput,
                uploadErrorMessage = null
            )
        }
    }

    fun clearSelectedUploadFile() {
        _uiState.update {
            it.copy(
                uploadSelectedUri = null,
                uploadSelectedFileName = "",
                uploadSelectedFileSize = "",
                uploadSelectedExtension = "",
                uploadErrorMessage = null
            )
        }
    }

    fun updateUploadTitle(title: String) {
        _uiState.update { it.copy(uploadTitleInput = title, uploadErrorMessage = null) }
    }

    fun updateUploadSubject(subject: String) {
        _uiState.update { it.copy(uploadSubjectInput = subject, uploadErrorMessage = null) }
    }

    fun updateUploadCategory(category: String) {
        _uiState.update { it.copy(uploadCategoryInput = category) }
    }

    fun dismissLastUploadedCard() {
        _uiState.update { it.copy(lastUploadedCloudFile = null) }
    }

    fun openCloudFileDetails(file: AcademicCloudFile?) {
        _uiState.update { it.copy(activeCloudFileForDetails = file) }
    }

    fun uploadCurrentFile(context: Context) {
        val s = _uiState.value
        val uri = s.uploadSelectedUri
        if (uri == null) {
            _uiState.update { it.copy(uploadErrorMessage = "يرجى اختيار ملف لرفعه أولاً") }
            return
        }

        val title = s.uploadTitleInput.trim().ifEmpty { s.uploadSelectedFileName.substringBeforeLast('.') }
        val subject = s.uploadSubjectInput.trim().ifEmpty { "مادة دراسية عامة" }

        _uiState.update {
            it.copy(
                isUploadingToCloud = true,
                uploadProgressPercent = 0.05f,
                uploadErrorMessage = null
            )
        }

        viewModelScope.launch {
            val result = r2Manager.uploadAcademicFile(
                context = context,
                uri = uri,
                title = title,
                subjectName = subject,
                departmentCode = s.selectedDepartment.code,
                yearGroup = s.selectedYear.number,
                category = s.uploadCategoryInput,
                onProgress = { progress ->
                    _uiState.update { it.copy(uploadProgressPercent = progress) }
                }
            )

            result.onSuccess { cloudFile ->
                repository.saveCloudFile(cloudFile)
                _uiState.update {
                    it.copy(
                        isUploadingToCloud = false,
                        uploadProgressPercent = 1.0f,
                        uploadSelectedUri = null,
                        uploadSelectedFileName = "",
                        uploadSelectedFileSize = "",
                        uploadSelectedExtension = "",
                        uploadTitleInput = "",
                        uploadSubjectInput = "",
                        lastUploadedCloudFile = cloudFile,
                        showAddDialog = null,
                        snackbarMessage = "تم رفع الملف بنجاح إلى Cloudflare R2!"
                    )
                }
            }.onFailure { err ->
                _uiState.update {
                    it.copy(
                        isUploadingToCloud = false,
                        uploadErrorMessage = err.message ?: "فشل الرفع إلى Cloudflare R2"
                    )
                }
            }
        }
    }

    fun deleteCloudFile(file: AcademicCloudFile) {
        viewModelScope.launch {
            repository.deleteCloudFile(file)
            _uiState.update { it.copy(snackbarMessage = "تم حذف الملف من سحابة الكلية") }
        }
    }
}
