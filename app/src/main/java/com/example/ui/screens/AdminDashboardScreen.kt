package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
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
import androidx.compose.material.icons.filled.AssignmentTurnedIn
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Assignment
import com.example.data.model.AssignmentSubmission
import com.example.data.model.Department
import com.example.data.model.Student
import com.example.data.model.YearGroup
import com.example.ui.components.AddAssignmentDialog
import com.example.ui.components.AddExamDialog
import com.example.ui.components.AddLectureDialog
import com.example.ui.components.AddVideoDialog
import com.example.ui.components.AssignmentSubmissionsDialog
import com.example.ui.components.CloudUploadDialog
import com.example.ui.components.SupabaseRegistrationCard
import com.example.ui.components.SupabaseSuccessDialog
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import com.example.ui.theme.PharaohGold
import com.example.ui.theme.PharaohGoldContainer
import com.example.ui.theme.PharaohGoldDark
import com.example.ui.theme.PharaohGoldLight
import com.example.ui.theme.PharaohNavy
import com.example.ui.theme.PharaohNavyDark
import com.example.ui.theme.PharaohNavyLight
import com.example.ui.theme.PharaohTextFieldStyles
import com.example.ui.viewmodel.ContentAddType
import com.example.ui.viewmodel.PharaohsViewModel
import com.example.ui.viewmodel.UiState

@Composable
fun AdminDashboardScreen(
    uiState: UiState,
    viewModel: PharaohsViewModel,
    modifier: Modifier = Modifier
) {
    val allStudents by viewModel.allStudents.collectAsState()
    val allExams by viewModel.allExams.collectAsState()
    val allCloudFiles by viewModel.allCloudFiles.collectAsState()
    val allAssignments by viewModel.allAssignments.collectAsState()
    val allSubmissions by viewModel.allSubmissions.collectAsState()
    val lectures by viewModel.currentLectures.collectAsState()
    val doctorLectures by viewModel.doctorIsolatedLectures.collectAsState()
    val doctorAssignments by viewModel.doctorIsolatedAssignments.collectAsState()
    val videos by viewModel.currentVideos.collectAsState()
    val context = LocalContext.current

    var selectedDashboardTab by remember { mutableIntStateOf(0) } // 0: Submissions Dashboard, 1: Student Roster
    var searchQuery by remember { mutableStateOf("") }
    var selectedDeptFilter by remember { mutableStateOf<String?>("ALL") }
    var assignmentSearchQuery by remember { mutableStateOf("") }

    val filteredStudents = allStudents.filter { student ->
        val matchesDept = selectedDeptFilter == "ALL" || student.departmentCode == selectedDeptFilter
        val matchesSearch = searchQuery.isBlank() ||
                student.fullName.contains(searchQuery.trim(), ignoreCase = true) ||
                student.studentCode.contains(searchQuery.trim(), ignoreCase = true)
        matchesDept && matchesSearch
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
    ) {
        // Doctor Isolated Profile Banner (عزل محتوى الدكتور وربطه برقمه القومي)
        uiState.currentDoctorProfile?.let { doc ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
                    .testTag("card_doctor_isolated_profile"),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = PharaohGoldContainer),
                border = androidx.compose.foundation.BorderStroke(1.dp, PharaohGold)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = CircleShape,
                                color = PharaohNavyDark,
                                modifier = Modifier.size(36.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(Icons.Default.School, contentDescription = null, tint = PharaohGoldLight, modifier = Modifier.size(20.dp))
                                }
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "د. ${doc.name}",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = PharaohNavyDark
                                )
                                Text(
                                    text = "قسم: ${doc.departmentName} • الرقم القومي: ${doc.nationalId}",
                                    fontSize = 11.sp,
                                    color = Color.DarkGray
                                )
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = PharaohNavyDark
                        ) {
                            Text(
                                text = "محتوى معزول",
                                color = PharaohGoldLight,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color.White,
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(modifier = Modifier.padding(8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(text = "${doctorLectures.size}", fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, color = PharaohNavyDark)
                                Text(text = "محاضراتي المسجلة", fontSize = 10.sp, color = Color.Gray)
                            }
                        }
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color.White,
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(modifier = Modifier.padding(8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(text = "${doctorAssignments.size}", fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, color = PharaohNavyDark)
                                Text(text = "تكليفاتي المنشورة", fontSize = 10.sp, color = Color.Gray)
                            }
                        }
                    }
                }
            }
        }
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = PharaohNavyDark,
                modifier = Modifier.size(40.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.AdminPanelSettings,
                        contentDescription = null,
                        tint = PharaohGoldLight,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(
                    text = "لوحة تحكم أعضاء هيئة التدريس والإدارة",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = PharaohNavyDark
                )
                Text(
                    text = "إدارة المحتوى الأكاديمي وسجلات الطلاب للجامعة",
                    fontSize = 11.sp,
                    color = Color.Gray
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Quick Stats Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            StatCard(
                title = "الطلاب",
                count = allStudents.size.toString(),
                icon = Icons.Default.Group,
                modifier = Modifier.weight(1f)
            )
            StatCard(
                title = "المحاضرات",
                count = lectures.size.toString(),
                icon = Icons.Default.MenuBook,
                modifier = Modifier.weight(1f)
            )
            StatCard(
                title = "التكليفات",
                count = "${allAssignments.size} (${allSubmissions.size})",
                icon = Icons.Default.AssignmentTurnedIn,
                modifier = Modifier.weight(1.1f)
            )
            StatCard(
                title = "الامتحانات",
                count = allExams.size.toString(),
                icon = Icons.Default.CalendarMonth,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Quick Action Buttons to add content
        Text(
            text = "إجراءات سريعة لإضافة المحتوى الأكاديمي والتكليفات:",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = PharaohNavyDark
        )
        Spacer(modifier = Modifier.height(6.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            Button(
                onClick = { viewModel.setShowAddDialog(ContentAddType.ASSIGNMENT) },
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PharaohGoldDark),
                modifier = Modifier
                    .weight(1.1f)
                    .testTag("admin_add_assignment_btn")
            ) {
                Icon(Icons.Default.Assignment, contentDescription = null, tint = Color.White, modifier = Modifier.size(13.dp))
                Spacer(modifier = Modifier.width(3.dp))
                Text("تكليف", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White)
            }

            Button(
                onClick = { viewModel.setShowAddDialog(ContentAddType.LECTURE) },
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PharaohNavy),
                modifier = Modifier
                    .weight(1f)
                    .testTag("admin_add_lecture_btn")
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(13.dp))
                Spacer(modifier = Modifier.width(3.dp))
                Text("محاضرة", fontSize = 10.sp)
            }

            Button(
                onClick = { viewModel.setShowAddDialog(ContentAddType.VIDEO) },
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PharaohNavyLight),
                modifier = Modifier
                    .weight(0.9f)
                    .testTag("admin_add_video_btn")
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(13.dp))
                Spacer(modifier = Modifier.width(3.dp))
                Text("فيديو", fontSize = 10.sp)
            }

            Button(
                onClick = { viewModel.setShowAddDialog(ContentAddType.CLOUD_FILE) },
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PharaohGold),
                modifier = Modifier
                    .weight(1.1f)
                    .testTag("admin_add_cloud_file_btn")
            ) {
                Icon(Icons.Default.CloudUpload, contentDescription = null, tint = PharaohNavyDark, modifier = Modifier.size(13.dp))
                Spacer(modifier = Modifier.width(3.dp))
                Text("رفع R2", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = PharaohNavyDark)
            }

            Button(
                onClick = { viewModel.setShowAddDialog(ContentAddType.SUPABASE_STUDENT) },
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                modifier = Modifier
                    .weight(1.2f)
                    .testTag("admin_add_supabase_student_btn")
            ) {
                Icon(Icons.Default.Cloud, contentDescription = null, tint = Color.White, modifier = Modifier.size(13.dp))
                Spacer(modifier = Modifier.width(3.dp))
                Text("Supabase", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White)
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Main Admin Dashboard Tabs: Submissions vs Students Roster
        TabRow(
            selectedTabIndex = selectedDashboardTab,
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = PharaohNavyDark,
            modifier = Modifier.clip(RoundedCornerShape(10.dp))
        ) {
            Tab(
                selected = selectedDashboardTab == 0,
                onClick = { selectedDashboardTab = 0 },
                text = {
                    Text(
                        text = "التسليمات والحلول المستلمة (${allSubmissions.size})",
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp
                    )
                },
                icon = { Icon(Icons.Default.AssignmentTurnedIn, contentDescription = null, modifier = Modifier.size(16.dp)) },
                modifier = Modifier.testTag("tab_doctor_submissions")
            )
            Tab(
                selected = selectedDashboardTab == 1,
                onClick = { selectedDashboardTab = 1 },
                text = {
                    Text(
                        text = "سجل قيد الطلاب (${filteredStudents.size})",
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp
                    )
                },
                icon = { Icon(Icons.Default.Group, contentDescription = null, modifier = Modifier.size(16.dp)) },
                modifier = Modifier.testTag("tab_doctor_students_roster")
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (selectedDashboardTab == 0) {
            // 2. لوحة الدكتور لاستلام ومتابعة تكليفات الطلاب (Submissions Dashboard)
            val currentDoc = uiState.currentDoctorProfile
            val relevantAssignments = if (currentDoc != null) {
                allAssignments.filter { it.doctorNationalId == currentDoc.nationalId || it.doctorName.contains(currentDoc.name) }
            } else {
                allAssignments
            }

            val filteredAssignments = relevantAssignments.filter { assignment ->
                assignmentSearchQuery.isBlank() ||
                        assignment.subjectName.contains(assignmentSearchQuery.trim(), ignoreCase = true) ||
                        assignment.title.contains(assignmentSearchQuery.trim(), ignoreCase = true)
            }

            if (relevantAssignments.isEmpty()) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(Icons.Default.AssignmentTurnedIn, contentDescription = null, tint = PharaohGoldDark, modifier = Modifier.size(48.dp))
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("لا توجد تكليفات منشورة حالياً لمتابعة تسليماتها", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = PharaohNavyDark)
                        Spacer(modifier = Modifier.height(6.dp))
                        Text("يمكنك طرح شيتات أو تكليفات دراسية للطلاب واستلام ملفات الحلول المرفوعة على Cloudflare R2", fontSize = 12.sp, color = Color.Gray, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                        Spacer(modifier = Modifier.height(14.dp))
                        Button(
                            onClick = { viewModel.setShowAddDialog(ContentAddType.ASSIGNMENT) },
                            colors = ButtonDefaults.buttonColors(containerColor = PharaohNavyDark),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("نشر تكليف دراسي جديد")
                        }
                    }
                }
            } else {
                Column(modifier = Modifier.fillMaxSize()) {
                    OutlinedTextField(
                        value = assignmentSearchQuery,
                        onValueChange = { assignmentSearchQuery = it },
                        placeholder = { Text("ابحث في التكليفات باسم المقرر أو العنوان...") },
                        colors = PharaohTextFieldStyles.colors(),
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = PharaohNavy) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_search_assignments"),
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(filteredAssignments, key = { it.id }) { assignment ->
                            val submissionsForThis = allSubmissions.filter { it.assignmentId == assignment.id }
                            val totalStudentsInYear = allStudents.count {
                                it.yearGroup == assignment.yearGroup &&
                                        (assignment.departmentCode.isBlank() || assignment.departmentCode == "ALL" || it.departmentCode == assignment.departmentCode)
                            }.let { if (it == 0) allStudents.count { s -> s.yearGroup == assignment.yearGroup }.coerceAtLeast(1) else it }
                            val percent = (submissionsForThis.size * 100) / totalStudentsInYear

                            var isExpanded by remember { mutableStateOf(false) }

                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("doctor_assignment_card_${assignment.id}"),
                                shape = RoundedCornerShape(14.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                elevation = CardDefaults.cardElevation(2.dp)
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Surface(
                                                shape = RoundedCornerShape(6.dp),
                                                color = PharaohNavyDark
                                            ) {
                                                Text(
                                                    text = assignment.subjectName,
                                                    color = PharaohGoldLight,
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                                )
                                            }
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Surface(
                                                shape = RoundedCornerShape(6.dp),
                                                color = PharaohGoldContainer
                                            ) {
                                                Text(
                                                    text = "${Department.fromCode(assignment.departmentCode).arabicName} • ${YearGroup.fromNumber(assignment.yearGroup).arabicName}",
                                                    fontSize = 10.sp,
                                                    color = PharaohNavyDark,
                                                    fontWeight = FontWeight.Bold,
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                                                )
                                            }
                                        }

                                        IconButton(
                                            onClick = { viewModel.deleteAssignment(assignment) },
                                            modifier = Modifier.size(28.dp)
                                        ) {
                                            Icon(Icons.Default.Delete, contentDescription = "حذف التكليف", tint = Color.Red.copy(alpha = 0.7f), modifier = Modifier.size(18.dp))
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))

                                    Text(
                                        text = assignment.title,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = PharaohNavyDark
                                    )

                                    if (assignment.description.isNotBlank()) {
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = assignment.description,
                                            fontSize = 11.sp,
                                            color = Color.DarkGray,
                                            maxLines = 2,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "آخر موعد: ${assignment.dueDate}",
                                            fontSize = 11.sp,
                                            color = Color.Gray
                                        )
                                        Text(
                                            text = "د. ${assignment.doctorName}",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = PharaohGoldDark
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(10.dp))

                                    // Quick Statistics (إظهار إحصائية سريعة: عدد الطلاب الذين سلموا الواجب مقارنة بإجمالي طلاب الفرقة)
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = Color(0xFFF1F5F9),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Column(modifier = Modifier.padding(10.dp)) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Icon(Icons.Default.AssignmentTurnedIn, contentDescription = null, tint = if (submissionsForThis.isNotEmpty()) Color(0xFF16A34A) else Color.Gray, modifier = Modifier.size(16.dp))
                                                    Spacer(modifier = Modifier.width(6.dp))
                                                    Text(
                                                        text = "نسبة تسليم الطلاب للواجب:",
                                                        fontSize = 11.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = PharaohNavyDark
                                                    )
                                                }
                                                Text(
                                                    text = "${submissionsForThis.size} من $totalStudentsInYear طالب ($percent%)",
                                                    fontSize = 12.sp,
                                                    fontWeight = FontWeight.ExtraBold,
                                                    color = if (submissionsForThis.isNotEmpty()) Color(0xFF15803D) else Color.DarkGray
                                                )
                                            }
                                            Spacer(modifier = Modifier.height(6.dp))
                                            LinearProgressIndicator(
                                                progress = { (submissionsForThis.size.toFloat() / totalStudentsInYear.toFloat()).coerceIn(0f, 1f) },
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .height(6.dp)
                                                    .clip(RoundedCornerShape(3.dp)),
                                                color = if (submissionsForThis.isNotEmpty()) Color(0xFF10B981) else PharaohNavyDark
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(10.dp))

                                    // زر مخصص لكل تكليف: 'التسليمات والحلول المستلمة'
                                    Button(
                                        onClick = { isExpanded = !isExpanded },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = if (isExpanded) PharaohGoldDark else PharaohNavyDark,
                                            contentColor = Color.White
                                        ),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(38.dp)
                                            .testTag("btn_toggle_submissions_${assignment.id}")
                                    ) {
                                        Icon(
                                            imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                            contentDescription = null,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = if (isExpanded) "إخفاء قائمة التسليمات" else "التسليمات والحلول المستلمة (${submissionsForThis.size})",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }

                                    // عرض جدول/قائمة بالطلاب الذين قاموا بالرفع: (اسم الطالب الكامل، كود الطالب، تاريخ وساعة التسليم بدقة، وزر مباشر لتحميل ملف الحل المرفوع على Cloudflare R2)
                                    if (isExpanded) {
                                        Spacer(modifier = Modifier.height(10.dp))
                                        if (submissionsForThis.isEmpty()) {
                                            Surface(
                                                shape = RoundedCornerShape(8.dp),
                                                color = Color(0xFFFEF2F2),
                                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFCA5A5)),
                                                modifier = Modifier.fillMaxWidth()
                                            ) {
                                                Text(
                                                    text = "لم يقم أي طالب برفع وتسليم الحل لهذا التكليف حتى الآن.",
                                                    fontSize = 11.sp,
                                                    color = Color(0xFFB91C1C),
                                                    modifier = Modifier.padding(12.dp),
                                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                                )
                                            }
                                        } else {
                                            Column(
                                                modifier = Modifier.fillMaxWidth(),
                                                verticalArrangement = Arrangement.spacedBy(8.dp)
                                            ) {
                                                Text(
                                                    text = "جدول الطلاب الذين قاموا برفع الحلول (${submissionsForThis.size}):",
                                                    fontSize = 12.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = PharaohNavyDark
                                                )
                                                submissionsForThis.forEach { sub ->
                                                    val dateFormatted = SimpleDateFormat("yyyy/MM/dd hh:mm:ss a", Locale("ar")).format(Date(sub.submissionDate))
                                                    Surface(
                                                        shape = RoundedCornerShape(8.dp),
                                                        color = Color(0xFFF8FAFC),
                                                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
                                                        modifier = Modifier.fillMaxWidth()
                                                    ) {
                                                        Column(modifier = Modifier.padding(10.dp)) {
                                                            Row(
                                                                modifier = Modifier.fillMaxWidth(),
                                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                                verticalAlignment = Alignment.CenterVertically
                                                            ) {
                                                                Text(
                                                                    text = sub.studentName,
                                                                    fontWeight = FontWeight.Bold,
                                                                    fontSize = 13.sp,
                                                                    color = PharaohNavyDark
                                                                )
                                                                Surface(
                                                                    shape = RoundedCornerShape(4.dp),
                                                                    color = PharaohGoldContainer
                                                                ) {
                                                                    Text(
                                                                        text = "كود: ${sub.studentCode}",
                                                                        fontSize = 10.sp,
                                                                        fontWeight = FontWeight.ExtraBold,
                                                                        color = PharaohNavyDark,
                                                                        fontFamily = FontFamily.Monospace,
                                                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                                    )
                                                                }
                                                            }

                                                            Spacer(modifier = Modifier.height(4.dp))

                                                            Row(
                                                                modifier = Modifier.fillMaxWidth(),
                                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                                verticalAlignment = Alignment.CenterVertically
                                                            ) {
                                                                Text(
                                                                    text = "تاريخ وساعة التسليم بدقة: $dateFormatted",
                                                                    fontSize = 10.sp,
                                                                    color = Color(0xFF475569)
                                                                )
                                                                if (sub.formattedSize.isNotBlank()) {
                                                                    Text(text = sub.formattedSize, fontSize = 10.sp, color = Color.Gray)
                                                                }
                                                            }

                                                            if (sub.notes.isNotBlank()) {
                                                                Spacer(modifier = Modifier.height(4.dp))
                                                                Text(
                                                                    text = "ملاحظة الطالب: ${sub.notes}",
                                                                    fontSize = 10.sp,
                                                                    color = Color.DarkGray
                                                                )
                                                            }

                                                            Spacer(modifier = Modifier.height(8.dp))

                                                            // زر مباشر لتحميل ملف الحل المرفوع على Cloudflare R2
                                                            Button(
                                                                onClick = {
                                                                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(sub.fileUrl))
                                                                    context.startActivity(intent)
                                                                },
                                                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF16A34A)),
                                                                shape = RoundedCornerShape(6.dp),
                                                                modifier = Modifier
                                                                    .fillMaxWidth()
                                                                    .height(34.dp)
                                                                    .testTag("btn_download_solution_${sub.id}")
                                                            ) {
                                                                Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(14.dp))
                                                                Spacer(modifier = Modifier.width(6.dp))
                                                                Text("تحميل ملف الحل من سحابة R2 (${sub.fileName})", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        item { Spacer(modifier = Modifier.height(40.dp)) }
                    }
                }
            }
        } else {
            // Students Roster Section
            Text(
                text = "سجل قيد الطلاب المعتمدين (${filteredStudents.size} طالب):",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = PharaohNavyDark
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Search in students
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("ابحث باسم الطالب أو كود الطالب...") },
                colors = PharaohTextFieldStyles.colors(),
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = PharaohNavy) },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("input_search_students"),
                singleLine = true,
                shape = RoundedCornerShape(10.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Filter chips for Department
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                FilterChip(
                    selected = selectedDeptFilter == "ALL",
                    onClick = { selectedDeptFilter = "ALL" },
                    label = { Text("الكل", fontSize = 11.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = PharaohNavy,
                        selectedLabelColor = PharaohGoldLight
                    )
                )

                Department.entries.forEach { dept ->
                    FilterChip(
                        selected = selectedDeptFilter == dept.code,
                        onClick = { selectedDeptFilter = dept.code },
                        label = { Text(dept.code, fontSize = 11.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = PharaohNavy,
                            selectedLabelColor = PharaohGoldLight
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Student Roster List
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(filteredStudents, key = { it.studentCode }) { student ->
                    StudentRosterItemCard(student = student)
                }
                item {
                    Spacer(modifier = Modifier.height(60.dp))
                }
            }
        }
    }

    // Dialogs
    if (uiState.showAddDialog == ContentAddType.ASSIGNMENT) {
        AddAssignmentDialog(
            department = uiState.selectedDepartment,
            year = uiState.selectedYear,
            doctorName = uiState.currentDoctorProfile?.name ?: "هيئة التدريس",
            onDismiss = { viewModel.setShowAddDialog(null) },
            onSubmit = { subj, title, desc, due, uri, name, size ->
                if (uri != null) {
                    viewModel.selectAssignmentAttachment(uri, name, size)
                }
                viewModel.addAssignment(context, subj, title, uiState.currentDoctorProfile?.name ?: "هيئة التدريس", desc, due)
            }
        )
    }

    if (uiState.activeAssignmentForDetails != null) {
        AssignmentSubmissionsDialog(
            assignment = uiState.activeAssignmentForDetails!!,
            submissions = uiState.activeAssignmentSubmissions,
            onDismiss = { viewModel.openAssignmentDetails(null) }
        )
    }

    // Dialogs
    if (uiState.showAddDialog == ContentAddType.SUPABASE_STUDENT) {
        AlertDialog(
            onDismissRequest = { viewModel.setShowAddDialog(null) },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { viewModel.setShowAddDialog(null) }) {
                    Text("إغلاق", color = Color.Gray)
                }
            },
            text = {
                SupabaseRegistrationCard(
                    uiState = uiState,
                    viewModel = viewModel
                )
            }
        )
    }

    if (uiState.supabaseSuccessStudent != null) {
        SupabaseSuccessDialog(
            student = uiState.supabaseSuccessStudent!!,
            onDismiss = { viewModel.dismissSupabaseSuccessDialog() }
        )
    }

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
            onConfirm = { subj, title, doc, num, dt, sum, sCount, sDet, allowSubmissions ->
                viewModel.addLecture(subj, title, doc, num, dt, sum, sCount, sDet, allowSubmissions)
            }
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
}

@Composable
private fun StatCard(
    title: String,
    count: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(
            modifier = Modifier.padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = PharaohNavy, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = count, fontSize = 15.sp, fontWeight = FontWeight.ExtraBold, color = PharaohNavyDark)
            Text(text = title, fontSize = 9.sp, color = Color.Gray, textAlign = androidx.compose.ui.text.style.TextAlign.Center, maxLines = 1)
        }
    }
}

@Composable
private fun StudentRosterItemCard(student: Student) {
    val dept = Department.fromCode(student.departmentCode)
    val year = YearGroup.fromNumber(student.yearGroup)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("student_roster_${student.studentCode}"),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = CircleShape,
                    color = PharaohNavyLight.copy(alpha = 0.15f),
                    modifier = Modifier.size(36.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(imageVector = Icons.Default.Person, contentDescription = null, tint = PharaohNavy)
                    }
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column {
                    Text(
                        text = student.fullName,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = PharaohNavyDark
                    )
                    Text(
                        text = "${dept.arabicName} • ${year.arabicName}",
                        fontSize = 11.sp,
                        color = Color.Gray
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Lock, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(10.dp))
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = "الرقم القومي: ${student.nationalIdMasked}",
                            fontSize = 10.sp,
                            color = Color.Gray,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }

            Surface(
                shape = RoundedCornerShape(6.dp),
                color = PharaohGoldContainer
            ) {
                Text(
                    text = student.studentCode,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = PharaohNavyDark,
                    fontFamily = FontFamily.Monospace,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }
    }
}
