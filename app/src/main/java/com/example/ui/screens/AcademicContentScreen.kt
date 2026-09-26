package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.InsertDriveFile
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayCircleFilled
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Slideshow
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.SecondaryTabRow
import androidx.compose.material3.TabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CourseCatalog
import com.example.data.model.DoctorCourse
import androidx.compose.ui.text.style.TextOverflow
import com.example.data.cloud.CloudflareR2Config
import com.example.data.model.AcademicCloudFile
import com.example.data.model.Assignment
import com.example.data.model.AssignmentSubmission
import com.example.data.model.Department
import com.example.data.model.ExamSchedule
import com.example.data.model.Lecture
import com.example.data.model.Student
import com.example.data.model.VideoLecture
import com.example.data.model.YearGroup
import com.example.ui.components.AddAssignmentDialog
import com.example.ui.components.AddExamDialog
import com.example.ui.components.AddLectureDialog
import com.example.ui.components.AddVideoDialog
import com.example.ui.components.AssignmentCard
import com.example.ui.components.AssignmentSubmissionsDialog
import com.example.ui.components.CloudFileCard
import com.example.ui.components.CloudUploadDialog
import com.example.ui.components.SlideViewerDialog
import com.example.ui.components.SubmitAssignmentDialog
import com.example.ui.components.UploadedFileSuccessCard
import com.example.ui.components.VideoPlayerDialog
import com.example.ui.theme.PharaohGold
import com.example.ui.theme.PharaohGoldContainer
import com.example.ui.theme.PharaohGoldDark
import com.example.ui.theme.PharaohGoldLight
import com.example.ui.theme.PharaohNavy
import com.example.ui.theme.PharaohNavyDark
import com.example.ui.theme.PharaohNavyLight
import com.example.ui.theme.PharaohPapyrus
import com.example.ui.theme.PharaohTextFieldStyles
import com.example.ui.viewmodel.ContentAddType
import com.example.ui.viewmodel.PharaohsViewModel
import com.example.ui.viewmodel.UiState
import com.example.ui.viewmodel.UserRole

@Composable
fun AcademicContentScreen(
    uiState: UiState,
    viewModel: PharaohsViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lectures by viewModel.currentLectures.collectAsState()
    val assignments by viewModel.currentAssignments.collectAsState()
    val studentSubmissions by viewModel.currentStudentSubmissions.collectAsState()
    val videos by viewModel.currentVideos.collectAsState()
    val exams by viewModel.currentExams.collectAsState()
    val cloudFiles by viewModel.currentCloudFiles.collectAsState()

    var selectedCourse by remember { mutableStateOf<DoctorCourse?>(null) }
    var courseSearchQuery by remember { mutableStateOf("") }
    var activeSubTab by remember { mutableIntStateOf(0) } // 0: Classes/Courses, 1: Exams, 2: Cloud R2

    if (selectedCourse != null) {
        DoctorClassDetailsScreen(
            course = selectedCourse!!,
            department = uiState.selectedDepartment,
            year = uiState.selectedYear,
            lectures = lectures,
            assignments = assignments,
            studentSubmissions = studentSubmissions,
            cloudFiles = cloudFiles,
            videos = videos,
            uiState = uiState,
            viewModel = viewModel,
            onBack = { selectedCourse = null }
        )
    } else {
        Box(modifier = modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
            ) {
                // Restriction for students: lock UI to registered department & year and prevent arbitrary switching
                if (uiState.currentRole == UserRole.STUDENT && uiState.currentStudent != null) {
                    StudentLockedAcademicHeader(student = uiState.currentStudent!!)
                } else {
                    // 1. Department Selector (3 main departments for staff/guests)
                    DepartmentSelectorRow(
                        selectedDepartment = uiState.selectedDepartment,
                        studentDepartmentCode = uiState.currentStudent?.departmentCode,
                        onDepartmentSelected = { viewModel.setDepartment(it) }
                    )

                    // 2. 4-Year Groups Filter
                    YearGroupSelectorRow(
                        selectedYear = uiState.selectedYear,
                        studentYear = uiState.currentStudent?.yearGroup,
                        onYearSelected = { viewModel.setYearGroup(it) }
                    )
                }

                // 3. Main Navigation Tabs: Doctor Classes | Exams | Cloud R2
                TabRow(
                    selectedTabIndex = activeSubTab,
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = PharaohNavyDark
                ) {
                    val currentCourses = CourseCatalog.getCoursesFor(
                        uiState.selectedDepartment.code,
                        uiState.selectedYear.number,
                        lectures,
                        assignments
                    )

                    Tab(
                        selected = activeSubTab == 0,
                        onClick = { activeSubTab = 0 },
                        text = {
                            Text(
                                text = "مقررات وفصول الدكاترة (${currentCourses.size})",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        },
                        icon = { Icon(Icons.Default.School, contentDescription = null, modifier = Modifier.size(16.dp)) },
                        modifier = Modifier.testTag("tab_sub_courses")
                    )
                    Tab(
                        selected = activeSubTab == 1,
                        onClick = { activeSubTab = 1 },
                        text = {
                            Text(
                                text = "جدول الامتحانات (${exams.size})",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        },
                        icon = { Icon(Icons.Default.CalendarMonth, contentDescription = null, modifier = Modifier.size(16.dp)) },
                        modifier = Modifier.testTag("tab_sub_exams")
                    )
                    Tab(
                        selected = activeSubTab == 2,
                        onClick = { activeSubTab = 2 },
                        text = {
                            Text(
                                text = "سحابة R2 (${cloudFiles.size})",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        },
                        icon = { Icon(Icons.Default.CloudUpload, contentDescription = null, modifier = Modifier.size(16.dp)) },
                        modifier = Modifier.testTag("tab_sub_cloud_files")
                    )
                }

                // 4. Content List for Selected Tab
                when (activeSubTab) {
                    0 -> {
                        val currentCourses = CourseCatalog.getCoursesFor(
                            uiState.selectedDepartment.code,
                            uiState.selectedYear.number,
                            lectures,
                            assignments
                        )
                        val filteredCourses = currentCourses.filter { course ->
                            courseSearchQuery.isBlank() ||
                            course.subjectName.contains(courseSearchQuery.trim(), ignoreCase = true) ||
                            course.doctorName.contains(courseSearchQuery.trim(), ignoreCase = true)
                        }

                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 14.dp, vertical = 10.dp)
                        ) {
                            // Info Card
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = PharaohGoldContainer),
                                border = androidx.compose.foundation.BorderStroke(1.dp, PharaohGold)
                            ) {
                                Row(
                                    modifier = Modifier.padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.Info, contentDescription = null, tint = PharaohNavyDark, modifier = Modifier.size(20.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "اضغط على كارت الدكتور / المقرر للدخول للقاعة الدراسية والاطلاع حصرياً على المحاضرات والتكليفات الخاصة به",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = PharaohNavyDark
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // Search bar
                            OutlinedTextField(
                                value = courseSearchQuery,
                                onValueChange = { courseSearchQuery = it },
                                placeholder = { Text("ابحث باسم المقرر أو أستاذ المادة...") },
                                colors = PharaohTextFieldStyles.colors(),
                                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = PharaohNavy) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("input_search_courses"),
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp)
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            if (filteredCourses.isEmpty()) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 40.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "لا توجد مقررات مطابقة للبحث",
                                        color = Color.Gray,
                                        fontSize = 13.sp
                                    )
                                }
                            } else {
                                LazyColumn(
                                    verticalArrangement = Arrangement.spacedBy(10.dp),
                                    modifier = Modifier.fillMaxSize()
                                ) {
                                    items(filteredCourses, key = { it.id }) { course ->
                                        val lCount = lectures.count { it.subjectName.trim().equals(course.subjectName.trim(), ignoreCase = true) }
                                        val aCount = assignments.count { it.subjectName.trim().equals(course.subjectName.trim(), ignoreCase = true) }
                                        val fCount = cloudFiles.count { it.subjectName.trim().equals(course.subjectName.trim(), ignoreCase = true) }

                                        DoctorCourseCard(
                                            course = course,
                                            lecturesCount = lCount,
                                            assignmentsCount = aCount,
                                            cloudFilesCount = fCount,
                                            onClick = { selectedCourse = course }
                                        )
                                    }
                                    item {
                                        Spacer(modifier = Modifier.height(70.dp))
                                    }
                                }
                            }
                        }
                    }
                    1 -> ExamsList(
                        exams = exams,
                        isDoctorAdmin = uiState.currentRole == UserRole.DOCTOR_ADMIN,
                        onDelete = { viewModel.deleteExam(it) }
                    )
                    2 -> CloudFilesList(
                        files = cloudFiles,
                        uiState = uiState,
                        viewModel = viewModel,
                        isDoctorAdmin = uiState.currentRole == UserRole.DOCTOR_ADMIN,
                        onUploadClick = { viewModel.setShowAddDialog(ContentAddType.CLOUD_FILE) },
                        onDelete = { viewModel.deleteCloudFile(it) }
                    )
                }
            }

        // Floating Action Button to add content
        if (uiState.currentRole == UserRole.DOCTOR_ADMIN || activeSubTab == 2) {
            ExtendedFloatingActionButton(
                onClick = {
                    val addType = when (activeSubTab) {
                        0 -> ContentAddType.LECTURE
                        1 -> ContentAddType.ASSIGNMENT
                        2 -> ContentAddType.CLOUD_FILE
                        3 -> ContentAddType.VIDEO
                        4 -> ContentAddType.EXAM
                        else -> ContentAddType.CLOUD_FILE
                    }
                    viewModel.setShowAddDialog(addType)
                },
                icon = { Icon(if (activeSubTab == 2) Icons.Default.CloudUpload else Icons.Default.Add, contentDescription = null) },
                text = {
                    val label = when (activeSubTab) {
                        0 -> "إضافة محاضرة"
                        1 -> "نشر تكليف جديد"
                        2 -> "رفع ملف إلى R2"
                        3 -> "رفع فيديو"
                        4 -> "إضافة امتحان"
                        else -> "إضافة محتوى"
                    }
                    Text(label, fontWeight = FontWeight.Bold)
                },
                containerColor = PharaohNavyDark,
                contentColor = PharaohGoldLight,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp)
                    .testTag("fab_add_content")
            )
        }
    }
}

    // Modal Dialogs
    if (uiState.showAddDialog == ContentAddType.CLOUD_FILE) {
        CloudUploadDialog(
            uiState = uiState,
            viewModel = viewModel,
            onDismiss = { viewModel.setShowAddDialog(null) }
        )
    }
    if (uiState.showAddDialog == ContentAddType.LECTURE) {
        AddLectureDialog(
            department = uiState.selectedDepartment,
            year = uiState.selectedYear,
            onDismiss = { viewModel.setShowAddDialog(null) },
            onConfirmWithFile = { subj, title, doc, num, dt, sum, sCount, sDet, fileUri, fileName, fileSize, allowSubmissions ->
                if (fileUri != null) {
                    viewModel.selectLectureFile(fileUri, fileName, fileSize)
                }
                viewModel.addLectureWithCloudFile(context, subj, title, doc, num, dt, sum, sCount, sDet, allowSubmissions)
            }
        )
    }

    if (uiState.showAddDialog == ContentAddType.ASSIGNMENT) {
        AddAssignmentDialog(
            department = uiState.selectedDepartment,
            year = uiState.selectedYear,
            doctorName = uiState.currentDoctorProfile?.name ?: "هيئة التدريس",
            onDismiss = { viewModel.setShowAddDialog(null) },
            onSubmit = { subj, title, desc, dueDate, uri, fName, fSize ->
                if (uri != null) {
                    viewModel.selectAssignmentAttachment(uri, fName, fSize)
                }
                viewModel.addAssignment(context, subj, title, uiState.currentDoctorProfile?.name ?: "هيئة التدريس", desc, dueDate)
            }
        )
    }

    if (uiState.showAddDialog == ContentAddType.SUBMIT_ASSIGNMENT && uiState.activeAssignmentToSubmit != null) {
        SubmitAssignmentDialog(
            assignment = uiState.activeAssignmentToSubmit!!,
            uiState = uiState,
            viewModel = viewModel,
            onDismiss = { viewModel.setShowAddDialog(null) }
        )
    }

    if (uiState.activeAssignmentForDetails != null) {
        AssignmentSubmissionsDialog(
            assignment = uiState.activeAssignmentForDetails!!,
            submissions = uiState.activeAssignmentSubmissions,
            onDismiss = { viewModel.openAssignmentDetails(null) }
        )
    }

    if (uiState.showAddDialog == ContentAddType.VIDEO) {
        AddVideoDialog(
            department = uiState.selectedDepartment,
            year = uiState.selectedYear,
            onDismiss = { viewModel.setShowAddDialog(null) },
            onConfirm = { subj, title, doc, dur, url, desc ->
                viewModel.addVideoLecture(subj, title, doc, dur, url, desc)
            }
        )
    }

    if (uiState.showAddDialog == ContentAddType.EXAM) {
        AddExamDialog(
            department = uiState.selectedDepartment,
            year = uiState.selectedYear,
            onDismiss = { viewModel.setShowAddDialog(null) },
            onConfirm = { subj, dt, time, dur, hall, notes ->
                viewModel.addExamSchedule(subj, dt, time, dur, hall, notes)
            }
        )
    }

    if (uiState.activeLectureForViewer != null) {
        SlideViewerDialog(
            lecture = uiState.activeLectureForViewer,
            onDismiss = { viewModel.openLectureViewer(null) }
        )
    }

    if (uiState.activeVideoForPlayer != null) {
        VideoPlayerDialog(
            video = uiState.activeVideoForPlayer,
            onDismiss = { viewModel.openVideoPlayer(null) }
        )
    }
}

@Composable
private fun DoctorCourseCard(
    course: DoctorCourse,
    lecturesCount: Int,
    assignmentsCount: Int,
    cloudFilesCount: Int,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .testTag("course_card_${course.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Surface(
                        shape = CircleShape,
                        color = PharaohGold.copy(alpha = 0.2f),
                        modifier = Modifier.size(44.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(imageVector = Icons.Default.School, contentDescription = null, tint = PharaohNavy, modifier = Modifier.size(24.dp))
                        }
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = course.subjectName,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 15.sp,
                            color = PharaohNavyDark
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Person, contentDescription = null, tint = PharaohGoldDark, modifier = Modifier.size(13.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = course.doctorName,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = PharaohGoldDark
                            )
                        }
                    }
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = PharaohNavyDark
                ) {
                    Text(
                        text = "مقرر رسمي",
                        color = PharaohGoldLight,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            if (course.description.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = course.description,
                    fontSize = 12.sp,
                    color = Color.DarkGray,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 16.sp
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Badges row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFFF1F5F9),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFCBD5E1))
                ) {
                    Row(modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Slideshow, contentDescription = null, tint = PharaohNavy, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = "$lecturesCount محاضرة", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = PharaohNavyDark)
                    }
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (assignmentsCount > 0) Color(0xFFFFFBEB) else Color(0xFFF1F5F9),
                    border = androidx.compose.foundation.BorderStroke(1.dp, if (assignmentsCount > 0) Color(0xFFFDE68A) else Color(0xFFCBD5E1))
                ) {
                    Row(modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Assignment, contentDescription = null, tint = if (assignmentsCount > 0) Color(0xFFB45309) else Color.Gray, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = "$assignmentsCount تكليف", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = if (assignmentsCount > 0) Color(0xFFB45309) else Color.Gray)
                    }
                }

                if (cloudFilesCount > 0) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFFECFDF5),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFA7F3D0))
                    ) {
                        Row(modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.CloudUpload, contentDescription = null, tint = Color(0xFF047857), modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(text = "$cloudFilesCount ملف سحابي", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF047857))
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = onClick,
                colors = ButtonDefaults.buttonColors(containerColor = PharaohNavyDark),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth().height(42.dp)
            ) {
                Icon(imageVector = Icons.Default.School, contentDescription = null, modifier = Modifier.size(16.dp), tint = PharaohGoldLight)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "دخول قاعة المقرر والمحاضرات والتكليفات",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
    }
}

@Composable
private fun DepartmentSelectorRow(
    selectedDepartment: Department,
    studentDepartmentCode: String?,
    onDepartmentSelected: (Department) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(0.dp),
        colors = CardDefaults.cardColors(containerColor = PharaohNavyDark)
    ) {
        Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
            Text(
                text = "الأقسام الرئيسية (3 أقسام):",
                color = PharaohGoldLight,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Department.entries.forEach { dept ->
                    val isSelected = selectedDepartment == dept
                    val isStudentEnrolledDept = studentDepartmentCode.equals(dept.code, ignoreCase = true)

                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .clickable { onDepartmentSelected(dept) }
                            .testTag("dept_select_${dept.code.lowercase()}"),
                        shape = RoundedCornerShape(10.dp),
                        color = if (isSelected) PharaohGold else Color.White.copy(alpha = 0.12f)
                    ) {
                        Column(
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = dept.code,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = if (isSelected) PharaohNavyDark else Color.White
                            )
                            Text(
                                text = dept.arabicName,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) PharaohNavyDark else Color.White.copy(alpha = 0.85f),
                                maxLines = 1
                            )
                            if (isStudentEnrolledDept) {
                                Text(
                                    text = "★ قسمك",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = if (isSelected) PharaohNavyDark else PharaohGoldLight
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun YearGroupSelectorRow(
    selectedYear: YearGroup,
    studentYear: Int?,
    onYearSelected: (YearGroup) -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            YearGroup.entries.forEach { year ->
                val isSelected = selectedYear == year
                val isStudentYear = studentYear == year.number

                FilterChip(
                    selected = isSelected,
                    onClick = { onYearSelected(year) },
                    label = {
                        Text(
                            text = year.arabicName + if (isStudentYear) " (فرقتك)" else "",
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = PharaohNavy,
                        selectedLabelColor = PharaohGoldLight
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("academic_year_${year.number}")
                )
            }
        }
    }
}

@Composable
private fun StudentLockedAcademicHeader(student: Student) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("student_locked_header"),
        shape = RoundedCornerShape(0.dp),
        colors = CardDefaults.cardColors(containerColor = PharaohNavyDark)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = CircleShape,
                        color = PharaohGold.copy(alpha = 0.2f),
                        modifier = Modifier.size(36.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = null,
                                tint = PharaohGoldLight,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "المحتوى الأكاديمي المخصص لك",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                        Text(
                            text = "${student.fullName} • كود: ${student.studentCode}",
                            color = Color(0xFF94A3B8),
                            fontSize = 11.sp
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = PharaohGold
                ) {
                    Text(
                        text = "مقيد رسمياً",
                        color = PharaohNavyDark,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 10.sp,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color.White.copy(alpha = 0.12f),
                    modifier = Modifier.weight(1f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.School, contentDescription = null, tint = PharaohGoldLight, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "القسم: ${Department.fromCode(student.departmentCode).arabicName}",
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color.White.copy(alpha = 0.12f),
                    modifier = Modifier.weight(1f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.CalendarMonth, contentDescription = null, tint = PharaohGoldLight, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "الفرقة: ${YearGroup.fromNumber(student.yearGroup).arabicName}",
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

/**
 * هيكل كلاسات الدكاترة المستقلة (Google Classroom Structure):
 * - عزل محتوى كل دكتور ومحاضراته المربوطة برقمه القومي
 * - تنظيم المقررات للطلاب كـ "فصول دراسية / كلاسات"، وكل كلاس يحمل اسم المادة واسم الدكتور
 * - دعم خاصية allowSubmissions: إتاحة زر تسليم الواجب فقط عند التفعيل، أو إظهارها للقراءة فقط
 */
@Composable
private fun LecturesList(
    lectures: List<Lecture>,
    isDoctorAdmin: Boolean,
    doctorNationalId: String? = null,
    doctorName: String? = null,
    departmentName: String,
    yearName: String,
    onOpenViewer: (Lecture) -> Unit,
    onSubmitAssignment: (Lecture) -> Unit,
    onDelete: (Lecture) -> Unit
) {
    var showOnlyDoctorLectures by remember { mutableStateOf(isDoctorAdmin && !doctorNationalId.isNullOrBlank()) }

    val displayedLectures = if (isDoctorAdmin && showOnlyDoctorLectures && !doctorNationalId.isNullOrBlank()) {
        lectures.filter { it.doctorNationalId == doctorNationalId }
    } else {
        lectures
    }

    // Grouping into Google Classroom subjects
    val classrooms = displayedLectures.groupBy { it.subjectName }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Doctor Isolated Filter Toggle
        if (isDoctorAdmin && !doctorNationalId.isNullOrBlank()) {
            item {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = PharaohGoldContainer,
                    border = androidx.compose.foundation.BorderStroke(1.dp, PharaohGold),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.AdminPanelSettings,
                                    contentDescription = null,
                                    tint = PharaohNavyDark,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = "نطاق عرض المحتوى لدكتور المادة:",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = PharaohNavyDark
                                    )
                                    Text(
                                        text = "د. ${doctorName ?: "المحاضر"} • الرقم القومي: $doctorNationalId",
                                        fontSize = 11.sp,
                                        color = Color.DarkGray
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            FilterChip(
                                selected = showOnlyDoctorLectures,
                                onClick = { showOnlyDoctorLectures = true },
                                label = { Text("محاضراتي الحصرية (${lectures.count { it.doctorNationalId == doctorNationalId }})", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = PharaohNavyDark,
                                    selectedLabelColor = PharaohGoldLight
                                ),
                                modifier = Modifier.weight(1f)
                            )
                            FilterChip(
                                selected = !showOnlyDoctorLectures,
                                onClick = { showOnlyDoctorLectures = false },
                                label = { Text("جميع مقررات القسم (${lectures.size})", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = PharaohNavyDark,
                                    selectedLabelColor = PharaohGoldLight
                                ),
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }
        }

        if (displayedLectures.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.MenuBook,
                            contentDescription = null,
                            tint = Color.Gray,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = if (isDoctorAdmin && showOnlyDoctorLectures)
                                "لم تقم بنشر محاضرات خاصة بك لهذه الفرقة بعد."
                            else
                                "لا توجد محاضرات مضافة لهذه الفرقة والقسم بعد.",
                            fontWeight = FontWeight.Bold,
                            color = Color.Gray
                        )
                        if (isDoctorAdmin) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "اضغط على زر (إضافة محاضرة) لنشر أول درس في كلاسك الدراسي.",
                                fontSize = 12.sp,
                                color = PharaohNavyLight
                            )
                        }
                    }
                }
            }
        } else {
            // Render Classrooms (Google Classroom structure)
            classrooms.forEach { (subjectName, subjectLectures) ->
                item(key = "classroom_$subjectName") {
                    val primaryDoctor = subjectLectures.firstOrNull()?.doctorName ?: doctorName ?: "أستاذ المادة"
                    ClassroomCard(
                        subjectName = subjectName,
                        doctorName = primaryDoctor,
                        departmentName = departmentName,
                        yearName = yearName,
                        lecturesCount = subjectLectures.size,
                        lectures = subjectLectures,
                        isDoctorAdmin = isDoctorAdmin,
                        onOpenViewer = onOpenViewer,
                        onSubmitAssignment = onSubmitAssignment,
                        onDelete = onDelete
                    )
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(72.dp))
        }
    }
}

/**
 * كارت الكلاس الأكاديمي (Google Classroom Card)
 * يجمع محاضرات المادة الواحدة تحت إشراف أستاذ المادة
 */
@Composable
private fun ClassroomCard(
    subjectName: String,
    doctorName: String,
    departmentName: String,
    yearName: String,
    lecturesCount: Int,
    lectures: List<Lecture>,
    isDoctorAdmin: Boolean,
    onOpenViewer: (Lecture) -> Unit,
    onSubmitAssignment: (Lecture) -> Unit,
    onDelete: (Lecture) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(3.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0))
    ) {
        Column {
            // Google Classroom Style Header Banner
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.horizontalGradient(
                            listOf(
                                PharaohNavyDark,
                                PharaohNavy,
                                Color(0xFF1E3A8A)
                            )
                        )
                    )
                    .padding(16.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = PharaohGold
                        ) {
                            Text(
                                text = "فصل دراسي / Classroom",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = PharaohNavyDark,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color.White.copy(alpha = 0.2f)
                        ) {
                            Text(
                                text = "$lecturesCount محاضرة",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = subjectName,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.School,
                            contentDescription = null,
                            tint = PharaohGoldLight,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "أستاذ المادة: د. $doctorName",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = PharaohGoldLight
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "$departmentName • $yearName",
                        fontSize = 11.sp,
                        color = Color(0xFF94A3B8)
                    )
                }
            }

            // Lectures within this classroom
            Column(
                modifier = Modifier.padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                lectures.forEach { lecture ->
                    LectureCard(
                        lecture = lecture,
                        isDoctorAdmin = isDoctorAdmin,
                        onOpenViewer = { onOpenViewer(lecture) },
                        onSubmitAssignment = { onSubmitAssignment(lecture) },
                        onDelete = { onDelete(lecture) }
                    )
                }
            }
        }
    }
}

@Composable
private fun LectureCard(
    lecture: Lecture,
    isDoctorAdmin: Boolean,
    onOpenViewer: () -> Unit,
    onSubmitAssignment: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("lecture_card_${lecture.id}"),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
        elevation = CardDefaults.cardElevation(1.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header Row: Lecture Number & Date
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = PharaohNavy.copy(alpha = 0.1f)
                ) {
                    Text(
                        text = "الدرس / محاضرة رقم ${lecture.lectureNumber}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = PharaohNavy,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }

                Text(
                    text = lecture.date,
                    fontSize = 11.sp,
                    color = Color.Gray,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = lecture.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = PharaohNavyDark
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "المحاضر: ${lecture.doctorName}",
                fontSize = 11.sp,
                color = Color(0xFF475569)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = lecture.summary,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 18.sp,
                maxLines = 3
            )

            // R2 File Attachment Banner
            if (lecture.fileUrl.isNotBlank()) {
                val context = LocalContext.current
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFFF1F5F9),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFCBD5E1)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(lecture.fileUrl))
                            context.startActivity(intent)
                        }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.CloudUpload, contentDescription = null, tint = Color(0xFF047857), modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "ملف المحاضرة: ${lecture.fileName.ifBlank { "تحميل من Cloudflare R2" }} (${lecture.fileSize.ifBlank { "R2" }})",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF065F46)
                            )
                        }
                        Icon(Icons.Default.Download, contentDescription = "تحميل", tint = Color(0xFF047857), modifier = Modifier.size(16.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Submissions Logic (Google Classroom allowSubmissions switch)
            if (lecture.allowSubmissions) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = Color(0xFFECFDF5),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFA7F3D0)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = Color(0xFF059669),
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "متاح تسليم حل وتكليف لهذا الدرس",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF047857)
                            )
                        }

                        Button(
                            onClick = onSubmitAssignment,
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF059669)),
                            modifier = Modifier.testTag("btn_submit_lecture_${lecture.id}")
                        ) {
                            Icon(
                                imageVector = Icons.Default.CloudUpload,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("رفع الحل / تسليم التكليف", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                }
            } else {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFFF1F5F9),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.MenuBook,
                            contentDescription = null,
                            tint = Color.Gray,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "محتوى للقراءة والتحميل فقط (التسليم غير مطلوب)",
                            fontSize = 11.sp,
                            color = Color.Gray,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Action row: Slides count & Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Slideshow,
                        contentDescription = null,
                        tint = PharaohGoldDark,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${lecture.slidesCount} شريحة عرض",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = PharaohGoldDark
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (isDoctorAdmin) {
                        IconButton(
                            onClick = onDelete,
                            modifier = Modifier
                                .size(32.dp)
                                .testTag("btn_delete_lecture_${lecture.id}")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "حذف المحاضرة",
                                tint = Color.Red,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                    }

                    Button(
                        onClick = onOpenViewer,
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = PharaohNavy),
                        modifier = Modifier.testTag("btn_view_slides_${lecture.id}")
                    ) {
                        Text("استعراض الشرائح (Slides)", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun AssignmentsList(
    assignments: List<Assignment>,
    submissions: List<AssignmentSubmission>,
    isDoctorAdmin: Boolean,
    onOpenSubmit: (Assignment) -> Unit,
    onViewSubmissions: (Assignment) -> Unit,
    onDelete: (Assignment) -> Unit,
    onAddClick: () -> Unit
) {
    if (assignments.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Surface(
                    shape = CircleShape,
                    color = PharaohGold.copy(alpha = 0.15f),
                    modifier = Modifier.size(56.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Assignment,
                            contentDescription = null,
                            tint = PharaohNavyDark,
                            modifier = Modifier.size(30.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(14.dp))
                Text(
                    text = "لا توجد تكليفات أو واجبات دراسية منشورة لهذه الفرقة حالياً.",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = Color.Gray,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = "نظام التكليفات الرقمي الشبيه بـ Google Classroom",
                    fontSize = 11.sp,
                    color = Color.LightGray
                )
                if (isDoctorAdmin) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = onAddClick,
                        colors = ButtonDefaults.buttonColors(containerColor = PharaohNavyDark),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("نشر أول تكليف للطلاب")
                    }
                }
            }
        }
    } else {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(assignments) { assignment ->
                val submission = submissions.firstOrNull { it.assignmentId == assignment.id }
                AssignmentCard(
                    assignment = assignment,
                    submission = submission,
                    isDoctor = isDoctorAdmin,
                    onOpenSubmit = onOpenSubmit,
                    onViewSubmissions = onViewSubmissions,
                    onDelete = onDelete
                )
            }
            item { Spacer(modifier = Modifier.height(72.dp)) }
        }
    }
}

@Composable
private fun VideosList(
    videos: List<VideoLecture>,
    isDoctorAdmin: Boolean,
    onOpenPlayer: (VideoLecture) -> Unit,
    onDelete: (VideoLecture) -> Unit
) {
    if (videos.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = Icons.Default.VideoLibrary,
                    contentDescription = null,
                    tint = Color.Gray,
                    modifier = Modifier.size(48.dp)
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "لا توجد فيديوهات مسجلة لهذه الفرقة حالياً.",
                    fontWeight = FontWeight.Bold,
                    color = Color.Gray
                )
                if (isDoctorAdmin) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "يمكنك رفع رابط فيديو جديد بالضغط على الزر أدناه.",
                        fontSize = 12.sp,
                        color = PharaohNavyLight
                    )
                }
            }
        }
    } else {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(videos, key = { it.id }) { video ->
                VideoCard(
                    video = video,
                    isDoctorAdmin = isDoctorAdmin,
                    onOpenPlayer = { onOpenPlayer(video) },
                    onDelete = { onDelete(video) }
                )
            }
            item {
                Spacer(modifier = Modifier.height(72.dp))
            }
        }
    }
}

@Composable
private fun VideoCard(
    video: VideoLecture,
    isDoctorAdmin: Boolean,
    onOpenPlayer: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("video_card_${video.id}"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(3.dp)
    ) {
        Column {
            // Video Thumbnail Simulation banner
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .background(PharaohNavyDark)
                    .clickable { onOpenPlayer() },
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Surface(
                        shape = CircleShape,
                        color = PharaohGold,
                        modifier = Modifier.size(44.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.PlayCircleFilled,
                                contentDescription = "تشغيل",
                                tint = PharaohNavyDark,
                                modifier = Modifier.size(36.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "مشاهدة الفيديو الآن",
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Duration badge
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = Color.Black.copy(alpha = 0.7f),
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(8.dp)
                ) {
                    Text(
                        text = "${video.durationMinutes} دقيقة",
                        color = Color.White,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = PharaohNavy.copy(alpha = 0.1f)
                    ) {
                        Text(
                            text = video.subjectName,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = PharaohNavy,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }

                    if (isDoctorAdmin) {
                        IconButton(
                            onClick = onDelete,
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "حذف الفيديو",
                                tint = Color.Red,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = video.title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = PharaohNavyDark
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "المحاضر: ${video.doctorName}",
                    fontSize = 11.sp,
                    color = Color.Gray
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = video.description,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 17.sp,
                    maxLines = 2
                )
            }
        }
    }
}

@Composable
private fun ExamsList(
    exams: List<ExamSchedule>,
    isDoctorAdmin: Boolean,
    onDelete: (ExamSchedule) -> Unit
) {
    if (exams.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = Icons.Default.CalendarMonth,
                    contentDescription = null,
                    tint = Color.Gray,
                    modifier = Modifier.size(48.dp)
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "لا توجد امتحانات معلنة لهذه الفرقة في الوقت الحالي.",
                    fontWeight = FontWeight.Bold,
                    color = Color.Gray
                )
            }
        }
    } else {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(exams, key = { it.id }) { exam ->
                ExamScheduleCard(
                    exam = exam,
                    isDoctorAdmin = isDoctorAdmin,
                    onDelete = { onDelete(exam) }
                )
            }
            item {
                Spacer(modifier = Modifier.height(72.dp))
            }
        }
    }
}

@Composable
fun ExamScheduleCard(
    exam: ExamSchedule,
    isDoctorAdmin: Boolean,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("exam_card_${exam.id}"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(3.dp),
        border = CardDefaults.outlinedCardBorder().copy(brush = Brush.linearGradient(listOf(PharaohGold, PharaohNavyLight)))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = exam.subjectName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = PharaohNavyDark
                )

                if (isDoctorAdmin) {
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "حذف الامتحان",
                            tint = Color.Red,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Details grid
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = PharaohGoldContainer,
                    modifier = Modifier.weight(1f)
                ) {
                    Column(modifier = Modifier.padding(8.dp)) {
                        Text(text = "تاريخ الامتحان", fontSize = 10.sp, color = PharaohNavyDark, fontWeight = FontWeight.Bold)
                        Text(text = exam.examDate, fontSize = 12.sp, fontWeight = FontWeight.ExtraBold, color = PharaohNavy)
                    }
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = PharaohNavy.copy(alpha = 0.08f),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(modifier = Modifier.padding(8.dp)) {
                        Text(text = "توقيت البدء والمدة", fontSize = 10.sp, color = PharaohNavyDark, fontWeight = FontWeight.Bold)
                        Text(text = "${exam.startTime} (${exam.durationHours} ساعات)", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = PharaohNavy)
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Hall info
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.School,
                    contentDescription = null,
                    tint = PharaohGoldDark,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "مكان الامتحان: ${exam.hall}",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = PharaohNavyDark
                )
            }

            if (exam.notes.isNotEmpty()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "ملاحظات: ${exam.notes}",
                    fontSize = 11.sp,
                    color = Color.DarkGray
                )
            }
        }
    }
}

/**
 * قائمة سحابة Cloudflare R2 لملفات المحاضرات والواجبات
 */
@Composable
private fun CloudFilesList(
    files: List<AcademicCloudFile>,
    uiState: UiState,
    viewModel: PharaohsViewModel,
    isDoctorAdmin: Boolean,
    onUploadClick: () -> Unit,
    onDelete: (AcademicCloudFile) -> Unit
) {
    var selectedCategoryFilter by remember { mutableStateOf("الكل") }
    var searchQuery by remember { mutableStateOf("") }

    val filteredFiles = files.filter { file ->
        val matchesCategory = (selectedCategoryFilter == "الكل") || (file.category == selectedCategoryFilter)
        val matchesSearch = searchQuery.isBlank() ||
                file.title.contains(searchQuery, ignoreCase = true) ||
                file.subjectName.contains(searchQuery, ignoreCase = true) ||
                file.originalFileName.contains(searchQuery, ignoreCase = true)
        matchesCategory && matchesSearch
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // 0. Search Bar in the top (بحث حسب اسم المادة أو اسم المحاضرة)
        item {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = {
                    Text(
                        text = "بحث باسم المادة أو اسم المحاضرة...",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "بحث",
                        tint = PharaohNavy,
                        modifier = Modifier.size(20.dp)
                    )
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "مسح البحث",
                                tint = Color.Gray,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                colors = PharaohTextFieldStyles.colors(
                    focusedBorderColor = PharaohGold,
                    unfocusedBorderColor = Color(0xFFCBD5E1)
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("input_search_cloud_files")
            )
        }

        // Active search feedback
        if (searchQuery.isNotBlank()) {
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "نتائج البحث عن: \"$searchQuery\" (${filteredFiles.size} ملف)",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = PharaohNavyDark
                    )
                    TextButton(onClick = { searchQuery = "" }) {
                        Text("إلغاء البحث", fontSize = 11.sp, color = PharaohGoldDark, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // 1. Success card if just uploaded
        if (uiState.lastUploadedCloudFile != null) {
            item {
                UploadedFileSuccessCard(
                    file = uiState.lastUploadedCloudFile,
                    onDismiss = { viewModel.dismissLastUploadedCard() }
                )
            }
        }

        // 2. Cloudflare R2 Connection Banner
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = PharaohNavyDark),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = CircleShape,
                                color = PharaohGold,
                                modifier = Modifier.size(36.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.CloudUpload,
                                        contentDescription = null,
                                        tint = PharaohNavyDark,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "سحابة المحاضرات والواجبات",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = Color.White
                                )
                                Text(
                                    text = "Cloudflare R2 • ${CloudflareR2Config.BUCKET_NAME}",
                                    fontSize = 11.sp,
                                    color = PharaohGoldLight
                                )
                            }
                        }

                        // Upload Button
                        Button(
                            onClick = onUploadClick,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = PharaohGold,
                                contentColor = PharaohNavyDark
                            ),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CloudUpload,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("رفع ملف", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "يتم رفع المستندات والفيديوهات وحفظها بروابط عامة سريعة ومباشرة على دومين: ${CloudflareR2Config.PUBLIC_DOMAIN}",
                        fontSize = 11.sp,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                }
            }
        }

        // 3. Category Filter Chips
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                listOf("الكل", "محاضرة", "واجب / تكليف", "شيت وسكاشن", "ملخص ومراجعة").forEach { cat ->
                    FilterChip(
                        selected = selectedCategoryFilter == cat,
                        onClick = { selectedCategoryFilter = cat },
                        label = { Text(cat, fontSize = 11.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = PharaohGold,
                            selectedLabelColor = PharaohNavyDark
                        )
                    )
                }
            }
        }

        // 4. File items or Empty State
        if (filteredFiles.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.CloudUpload,
                            contentDescription = null,
                            tint = Color.LightGray,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "لا توجد ملفات مرفوعة في هذا القسم حالياً",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = Color.DarkGray
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "اضغط على زر 'رفع ملف' لاختيار محاضرة أو واجب (PDF, MP4, PPTX, DOCX, ZIP) ورفعها للسحابة فورياً",
                            fontSize = 11.sp,
                            color = Color.Gray,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                        Button(
                            onClick = onUploadClick,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = PharaohNavyDark,
                                contentColor = PharaohGoldLight
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("رفع أول ملف الآن", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        } else {
            items(filteredFiles, key = { it.id }) { cloudFile ->
                CloudFileCard(
                    file = cloudFile,
                    canDelete = isDoctorAdmin,
                    onDelete = { onDelete(cloudFile) }
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(60.dp))
        }
    }
}
