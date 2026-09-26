package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.OpenableColumns
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.AssignmentTurnedIn
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.InsertDriveFile
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.Assignment
import com.example.data.model.AssignmentSubmission
import com.example.data.model.Department
import com.example.data.model.YearGroup
import com.example.ui.theme.PharaohGold
import com.example.ui.theme.PharaohGoldContainer
import com.example.ui.theme.PharaohGoldDark
import com.example.ui.theme.PharaohGoldLight
import com.example.ui.theme.PharaohNavy
import com.example.ui.theme.PharaohNavyDark
import com.example.ui.theme.PharaohNavyLight
import com.example.ui.theme.PharaohPapyrus
import com.example.ui.theme.PharaohTextFieldStyles
import com.example.ui.viewmodel.PharaohsViewModel
import com.example.ui.viewmodel.UiState
import com.example.ui.viewmodel.UserRole
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * حوار إضافة تكليف / واجب جديد من جانب الدكتور (Google Classroom)
 */
@Composable
fun AddAssignmentDialog(
    department: Department,
    year: YearGroup,
    doctorName: String,
    onDismiss: () -> Unit,
    onSubmit: (subject: String, title: String, description: String, dueDate: String, fileUri: Uri?, fileName: String, fileSize: String) -> Unit
) {
    val context = LocalContext.current
    var subject by remember { mutableStateOf("") }
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var dueDate by remember { mutableStateOf("2025-11-20") }
    var selectedUri by remember { mutableStateOf<Uri?>(null) }
    var selectedFileName by remember { mutableStateOf("") }
    var selectedFileSize by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }

    val filePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            selectedUri = uri
            var name = "assignment_attachment"
            var size = ""
            context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                val sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE)
                if (cursor.moveToFirst()) {
                    if (nameIndex != -1) name = cursor.getString(nameIndex) ?: name
                    if (sizeIndex != -1) {
                        val s = cursor.getLong(sizeIndex)
                        size = if (s > 1024 * 1024) String.format("%.2f MB", s / (1024.0 * 1024.0))
                        else String.format("%.1f KB", s / 1024.0)
                    }
                }
            }
            selectedFileName = name
            selectedFileSize = size
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = CircleShape,
                    color = PharaohGold.copy(alpha = 0.2f),
                    modifier = Modifier.size(36.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(imageVector = Icons.Default.Assignment, contentDescription = null, tint = PharaohNavyDark)
                    }
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "نشر تكليف دراسي جديد",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = PharaohNavyDark
                    )
                    Text(
                        text = "${department.arabicName} - ${year.arabicName}",
                        fontSize = 11.sp,
                        color = Color.Gray
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = subject,
                    onValueChange = { subject = it; error = null },
                    label = { Text("اسم المادة / المقرر") },
                    placeholder = { Text("مثال: البرمجة المتقدمة") },
                    colors = PharaohTextFieldStyles.colors(),
                    modifier = Modifier.fillMaxWidth().testTag("input_assignment_subject"),
                    singleLine = true
                )

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it; error = null },
                    label = { Text("عنوان التكليف / الواجب") },
                    placeholder = { Text("مثال: مشروع الفصل: نظام إدارة قواعد البيانات") },
                    colors = PharaohTextFieldStyles.colors(),
                    modifier = Modifier.fillMaxWidth().testTag("input_assignment_title"),
                    singleLine = true
                )

                OutlinedTextField(
                    value = dueDate,
                    onValueChange = { dueDate = it },
                    label = { Text("آخر موعد للتسليم (Due Date)") },
                    colors = PharaohTextFieldStyles.colors(),
                    leadingIcon = { Icon(Icons.Default.DateRange, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("وصف التكليف والتعليمات المطلوبة") },
                    placeholder = { Text("اكتب شرح المطلوب من الطالب بدقة...") },
                    colors = PharaohTextFieldStyles.colors(),
                    modifier = Modifier.fillMaxWidth().heightIn(min = 90.dp),
                    maxLines = 4
                )

                // Attachment Section (Cloudflare R2 sheet)
                Text(
                    text = "إرفاق شيت أو ملف التكليف (اختياري - يرفع إلى R2):",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = PharaohNavyDark
                )

                if (selectedUri == null) {
                    OutlinedButton(
                        onClick = {
                            filePicker.launch("*/*")
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(imageVector = Icons.Default.AttachFile, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("اختر ملف الشيت من الهاتف (PDF / Word / ZIP)", fontSize = 12.sp)
                    }
                } else {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Color(0xFFF1F5F9),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFCBD5E1)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                Icon(Icons.Default.InsertDriveFile, contentDescription = null, tint = PharaohNavy)
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = selectedFileName,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(text = selectedFileSize, fontSize = 10.sp, color = Color.Gray)
                                }
                            }
                            IconButton(onClick = {
                                selectedUri = null
                                selectedFileName = ""
                                selectedFileSize = ""
                            }) {
                                Icon(Icons.Default.Close, contentDescription = "إزالة", tint = Color.Red, modifier = Modifier.size(18.dp))
                            }
                        }
                    }
                }

                if (error != null) {
                    Text(text = error!!, color = MaterialTheme.colorScheme.error, fontSize = 12.sp)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (subject.isBlank() || title.isBlank()) {
                        error = "يرجى إدخال اسم المادة وعنوان التكليف"
                    } else {
                        onSubmit(
                            subject.trim(),
                            title.trim(),
                            description.trim().ifEmpty { "لا توجد تفاصيل إضافية" },
                            dueDate.trim(),
                            selectedUri,
                            selectedFileName,
                            selectedFileSize
                        )
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = PharaohNavyDark)
            ) {
                Text("نشر التكليف للطلاب")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("إلغاء")
            }
        }
    )
}

/**
 * حوار تسليم الطالب للواجب (رفع الحل إلى Cloudflare R2 وتوثيقه)
 */
@Composable
fun SubmitAssignmentDialog(
    assignment: Assignment,
    uiState: UiState,
    viewModel: PharaohsViewModel,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var selectedUri by remember { mutableStateOf<Uri?>(null) }
    var selectedFileName by remember { mutableStateOf("") }
    var selectedFileSize by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }

    val filePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            selectedUri = uri
            var name = "solution.pdf"
            var size = ""
            context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                val sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE)
                if (cursor.moveToFirst()) {
                    if (nameIndex != -1) name = cursor.getString(nameIndex) ?: name
                    if (sizeIndex != -1) {
                        val s = cursor.getLong(sizeIndex)
                        size = if (s > 1024 * 1024) String.format("%.2f MB", s / (1024.0 * 1024.0))
                        else String.format("%.1f KB", s / 1024.0)
                    }
                }
            }
            selectedFileName = name
            selectedFileSize = size
            viewModel.selectSubmissionFile(uri, name, size)
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = CircleShape,
                    color = Color(0xFF10B981).copy(alpha = 0.2f),
                    modifier = Modifier.size(36.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(imageVector = Icons.Default.CloudUpload, contentDescription = null, tint = Color(0xFF047857))
                    }
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "تسليم الواجب / رفع الحل",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = PharaohNavyDark
                    )
                    Text(
                        text = assignment.title,
                        fontSize = 11.sp,
                        color = Color.Gray,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Info Banner
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = Color(0xFFF8FAFC),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text(text = "المقرر: ${assignment.subjectName}", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        Text(text = "الدكتور: ${assignment.doctorName}", fontSize = 11.sp, color = Color.Gray)
                        Text(text = "آخر موعد: ${assignment.dueDate}", fontSize = 11.sp, color = Color(0xFFB45309), fontWeight = FontWeight.Bold)
                    }
                }

                // File Chooser
                Text(
                    text = "ملف الحل (PDF أو صورة أو مستند) *:",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = PharaohNavyDark
                )

                if (selectedUri == null) {
                    OutlinedButton(
                        onClick = { filePicker.launch("*/*") },
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        enabled = !uiState.isSubmittingAssignment
                    ) {
                        Icon(imageVector = Icons.Default.UploadFile, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("اختر ملف الحل من جهازك", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                } else {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFFECFDF5),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFA7F3D0)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                Icon(Icons.Default.PictureAsPdf, contentDescription = null, tint = Color(0xFF047857))
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = selectedFileName,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF065F46),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(text = selectedFileSize, fontSize = 11.sp, color = Color.Gray)
                                }
                            }
                            IconButton(
                                onClick = {
                                    selectedUri = null
                                    selectedFileName = ""
                                    selectedFileSize = ""
                                },
                                enabled = !uiState.isSubmittingAssignment
                            ) {
                                Icon(Icons.Default.Close, contentDescription = "إلغاء", tint = Color.Red, modifier = Modifier.size(18.dp))
                            }
                        }
                    }
                }

                OutlinedTextField(
                    value = notes,
                    onValueChange = {
                        notes = it
                        viewModel.updateSubmissionNotes(it)
                    },
                    label = { Text("ملاحظات للطبيب / المحاضر (اختياري)") },
                    placeholder = { Text("مثال: تم إرفاق حل التمارين 1 و 2 و 3") },
                    colors = PharaohTextFieldStyles.colors(),
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !uiState.isSubmittingAssignment
                )

                // Upload Progress Bar
                AnimatedVisibility(visible = uiState.isSubmittingAssignment) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = "جاري رفع ملف الحل إلى Cloudflare R2...", fontSize = 11.sp, color = Color(0xFF047857))
                            Text(text = "${(uiState.submissionProgress * 100).toInt()}%", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        LinearProgressIndicator(
                            progress = { uiState.submissionProgress },
                            modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
                            color = Color(0xFF10B981)
                        )
                    }
                }

                if (error != null) {
                    Text(text = error!!, color = MaterialTheme.colorScheme.error, fontSize = 12.sp)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (selectedUri == null) {
                        error = "يرجى اختيار ملف الحل أولاً"
                    } else {
                        viewModel.submitAssignmentSolution(context)
                    }
                },
                enabled = !uiState.isSubmittingAssignment && selectedUri != null,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981))
            ) {
                if (uiState.isSubmittingAssignment) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("جاري التسليم...")
                } else {
                    Icon(imageVector = Icons.Default.Send, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("تسليم الواجب واعتماده")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !uiState.isSubmittingAssignment) {
                Text("إلغاء")
            }
        }
    )
}

/**
 * بطاقة عرض التكليف الدراسي (Google Classroom Card)
 */
@Composable
fun AssignmentCard(
    assignment: Assignment,
    submission: AssignmentSubmission?,
    isDoctor: Boolean,
    onOpenSubmit: (Assignment) -> Unit,
    onViewSubmissions: (Assignment) -> Unit,
    onDelete: (Assignment) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header: Subject & Due Date Badge
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
                            Icon(imageVector = Icons.Default.Assignment, contentDescription = null, tint = PharaohNavyDark, modifier = Modifier.size(20.dp))
                        }
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = assignment.subjectName,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = PharaohNavy
                        )
                        Text(
                            text = "الدكتور: ${assignment.doctorName}",
                            fontSize = 11.sp,
                            color = Color.Gray
                        )
                    }
                }

                // Due date chip
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFFFFFBEB),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFDE68A))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.DateRange, contentDescription = null, tint = Color(0xFFB45309), modifier = Modifier.size(12.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "التسليم: ${assignment.dueDate}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFB45309)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Title
            Text(
                text = assignment.title,
                fontSize = 15.sp,
                fontWeight = FontWeight.ExtraBold,
                color = PharaohNavyDark
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Description
            Text(
                text = assignment.description,
                fontSize = 12.sp,
                color = Color.DarkGray,
                lineHeight = 18.sp
            )

            // Attachment sheet from doctor
            if (assignment.attachmentUrl.isNotBlank()) {
                Spacer(modifier = Modifier.height(10.dp))
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = Color(0xFFF8FAFC),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFCBD5E1)),
                    modifier = Modifier.fillMaxWidth().clickable {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(assignment.attachmentUrl))
                        context.startActivity(intent)
                    }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.AttachFile, contentDescription = null, tint = PharaohNavy, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "مرفق التكليف: ${assignment.attachmentName.ifBlank { "ورقة الأسئلة والشيت" }}",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = PharaohNavyDark
                            )
                        }
                        Icon(Icons.Default.Download, contentDescription = "تحميل", tint = PharaohNavy, modifier = Modifier.size(18.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Status & Actions
            if (isDoctor) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = { onViewSubmissions(assignment) },
                        colors = ButtonDefaults.buttonColors(containerColor = PharaohNavyDark),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.height(38.dp)
                    ) {
                        Icon(Icons.Default.AssignmentTurnedIn, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("عرض حلول الطلاب المسلمة", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }

                    IconButton(onClick = { onDelete(assignment) }) {
                        Icon(Icons.Default.Delete, contentDescription = "حذف", tint = Color.Red.copy(alpha = 0.7f))
                    }
                }
            } else {
                // Student View: show submission status
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    if (submission != null) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFFDCFCE7),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFA7F3D0))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF16A34A), modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "تم التسليم بنجاح ✓",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF15803D)
                                )
                            }
                        }

                        OutlinedButton(
                            onClick = {
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(submission.fileUrl))
                                context.startActivity(intent)
                            },
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.height(36.dp)
                        ) {
                            Icon(Icons.Default.OpenInNew, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("عرض الحل المرفوع", fontSize = 11.sp)
                        }
                    } else {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFFFEF2F2),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFCA5A5))
                        ) {
                            Text(
                                text = "مطلوب التسليم",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFB91C1C),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }

                        Button(
                            onClick = { onOpenSubmit(assignment) },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.height(38.dp)
                        ) {
                            Icon(Icons.Default.CloudUpload, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("تسليم الواجب / رفع الحل", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

/**
 * حوار عرض حلول الطلاب المسلمة للدكتور
 */
@Composable
fun AssignmentSubmissionsDialog(
    assignment: Assignment,
    submissions: List<AssignmentSubmission>,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text(
                    text = "سجل تسليمات الطلاب (${submissions.size})",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = PharaohNavyDark
                )
                Text(
                    text = assignment.title,
                    fontSize = 12.sp,
                    color = Color.Gray,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 400.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (submissions.isEmpty()) {
                    Text(
                        text = "لم يقم أي طالب بتسليم الحل حتى الآن.",
                        fontSize = 13.sp,
                        color = Color.Gray,
                        modifier = Modifier.padding(vertical = 16.dp)
                    )
                } else {
                    submissions.forEach { sub ->
                        val dateFormatted = SimpleDateFormat("yyyy/MM/dd hh:mm a", Locale("ar")).format(Date(sub.submissionDate))

                        Surface(
                            shape = RoundedCornerShape(10.dp),
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
                                    Text(text = sub.studentName, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = PharaohNavyDark)
                                    Text(text = sub.studentCode, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = PharaohGoldDark)
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(text = "الرقم القومي: ${sub.studentNationalId}", fontSize = 11.sp, color = Color.Gray)
                                    Text(text = dateFormatted, fontSize = 10.sp, color = Color.Gray)
                                }

                                if (sub.notes.isNotBlank()) {
                                    Text(text = "ملاحظة: ${sub.notes}", fontSize = 11.sp, color = Color.DarkGray)
                                }

                                Spacer(modifier = Modifier.height(6.dp))

                                OutlinedButton(
                                    onClick = {
                                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(sub.fileUrl))
                                        context.startActivity(intent)
                                    },
                                    modifier = Modifier.fillMaxWidth().height(34.dp),
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("تحميل ملف الحل (${sub.fileName})", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("إغلاق")
            }
        }
    )
}
