package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Department
import com.example.data.model.YearGroup
import com.example.data.supabase.SupabaseConfig
import com.example.data.supabase.SupabaseStudent
import com.example.ui.theme.PharaohGold
import com.example.ui.theme.PharaohGoldDark
import com.example.ui.theme.PharaohGoldLight
import com.example.ui.theme.PharaohNavy
import com.example.ui.theme.PharaohNavyDark
import com.example.ui.theme.PharaohNavyLight
import com.example.ui.theme.PharaohTextFieldStyles
import com.example.ui.viewmodel.PharaohsViewModel
import com.example.ui.viewmodel.UiState

/**
 * قسم واجهة تسجيل الطالب في سحابة Supabase
 * مع التحقق الذكي للرقم القومي المصري ورقم الهاتف، وتحديد الكلية والفرقة
 * وتوليد كود طالب فريد يظهر مباشرة في الكارنيه
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SupabaseRegistrationCard(
    uiState: UiState,
    viewModel: PharaohsViewModel,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .animateContentSize()
            .testTag("card_supabase_registration"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0))
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            // Header with Supabase Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = CircleShape,
                        color = Color(0xFF3ECF8E).copy(alpha = 0.15f),
                        modifier = Modifier.size(42.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Cloud,
                                contentDescription = null,
                                tint = Color(0xFF10B981),
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "القيد السحابي للطلاب (Supabase)",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 15.sp,
                            color = PharaohNavyDark
                        )
                        Text(
                            text = "قاعدة بيانات PostgreSQL الرسمية للجامعة",
                            fontSize = 11.sp,
                            color = Color.Gray
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFF10B981).copy(alpha = 0.12f)
                ) {
                    Text(
                        text = "جدول: student",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF047857),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Field 1: اسم الطالب (full_name)
            OutlinedTextField(
                value = uiState.supabaseFullName,
                onValueChange = { viewModel.updateSupabaseFullName(it) },
                label = { Text("اسم الطالب بالكامل (ثلاثي أو رباعي) *", fontSize = 12.sp) },
                placeholder = { Text("مثال: إبراهيم خالد محمود حسن", fontSize = 12.sp) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        tint = PharaohNavy,
                        modifier = Modifier.size(20.dp)
                    )
                },
                singleLine = true,
                enabled = !uiState.isSavingToSupabase,
                shape = RoundedCornerShape(12.dp),
                colors = PharaohTextFieldStyles.colors(
                    focusedBorderColor = Color(0xFF10B981),
                    unfocusedBorderColor = Color(0xFFCBD5E1)
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("input_supabase_fullname")
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Field 2: الرقم القومي (national_id) - 14 رقم مصري
            OutlinedTextField(
                value = uiState.supabaseNationalId,
                onValueChange = { viewModel.updateSupabaseNationalId(it) },
                label = { Text("الرقم القومي المصري (14 رقماً - فريد) *", fontSize = 12.sp) },
                placeholder = { Text("مثال: 30401010101234", fontSize = 12.sp) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Fingerprint,
                        contentDescription = null,
                        tint = PharaohNavy,
                        modifier = Modifier.size(20.dp)
                    )
                },
                trailingIcon = {
                    Text(
                        text = "${uiState.supabaseNationalId.length}/14",
                        fontSize = 11.sp,
                        color = if (uiState.supabaseNationalId.length == 14 && uiState.supabaseNationalIdInfo?.isValid == true) {
                            Color(0xFF10B981)
                        } else Color.Gray,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(end = 8.dp)
                    )
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                enabled = !uiState.isSavingToSupabase,
                shape = RoundedCornerShape(12.dp),
                colors = PharaohTextFieldStyles.colors(
                    focusedBorderColor = Color(0xFF10B981),
                    unfocusedBorderColor = Color(0xFFCBD5E1)
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("input_supabase_national_id")
            )

            // National ID live info or error
            uiState.supabaseNationalIdInfo?.let { info ->
                Spacer(modifier = Modifier.height(6.dp))
                if (info.isValid) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFFECFDF5),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFA7F3D0)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.VerifiedUser, contentDescription = null, tint = Color(0xFF059669), modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "تاريخ الميلاد: ${info.birthDate} • ${info.governorate} • ${info.gender}",
                                fontSize = 11.sp,
                                color = Color(0xFF047857),
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                } else if (info.errorMessage != null) {
                    Text(
                        text = "⚠️ ${info.errorMessage}",
                        fontSize = 11.sp,
                        color = Color(0xFFDC2626),
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(start = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Field 3: الكلية والقسم / التخصص
            Text(
                text = "الكلية والتخصص الأكاديمي:",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = PharaohNavyDark
            )
            Spacer(modifier = Modifier.height(4.dp))
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Department.entries.forEach { dept ->
                    FilterChip(
                        selected = uiState.supabaseDepartment == dept,
                        onClick = { viewModel.updateSupabaseDepartment(dept) },
                        label = { Text(dept.arabicName, fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                        enabled = !uiState.isSavingToSupabase,
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = PharaohNavyDark,
                            selectedLabelColor = PharaohGoldLight
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Field 4: الفرق الدراسية الأربعة كاملة
            Text(
                text = "الفرقة الدراسية (المستوى الجامعي):",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = PharaohNavyDark
            )
            Spacer(modifier = Modifier.height(4.dp))
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                YearGroup.entries.forEach { year ->
                    FilterChip(
                        selected = uiState.supabaseAcademicYear == year,
                        onClick = { viewModel.updateSupabaseAcademicYear(year) },
                        label = { Text(year.arabicName, fontSize = 11.sp) },
                        enabled = !uiState.isSavingToSupabase,
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color(0xFF10B981),
                            selectedLabelColor = Color.White
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Field 5: رقم الهاتف (phone)
            OutlinedTextField(
                value = uiState.supabasePhone,
                onValueChange = { viewModel.updateSupabasePhone(it) },
                label = { Text("رقم الهاتف المحمول (11 رقماً مصرياً) *", fontSize = 12.sp) },
                placeholder = { Text("01012345678", fontSize = 12.sp) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Phone,
                        contentDescription = null,
                        tint = PharaohNavy,
                        modifier = Modifier.size(20.dp)
                    )
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                singleLine = true,
                enabled = !uiState.isSavingToSupabase,
                shape = RoundedCornerShape(12.dp),
                colors = PharaohTextFieldStyles.colors(
                    focusedBorderColor = Color(0xFF10B981),
                    unfocusedBorderColor = Color(0xFFCBD5E1)
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("input_supabase_phone")
            )

            // Phone Carrier badge or error
            if (uiState.supabasePhone.length == 11) {
                val prefix = uiState.supabasePhone.take(3)
                val carrier = when (prefix) {
                    "010" -> "شبكة فودافون مصر"
                    "011" -> "شبكة اتصالات مصر"
                    "012" -> "شبكة أورنج مصر"
                    "015" -> "شبكة المصرية للاتصالات (WE)"
                    else -> null
                }
                if (carrier != null) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "📱 $carrier",
                        fontSize = 11.sp,
                        color = Color(0xFF059669),
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(start = 4.dp)
                    )
                }
            }
            if (uiState.supabasePhoneError != null) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "⚠️ ${uiState.supabasePhoneError}",
                    fontSize = 11.sp,
                    color = Color(0xFFDC2626),
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(start = 4.dp)
                )
            }

            // Student Code Generated Preview
            if (uiState.supabaseGeneratedStudentCode.isNotBlank()) {
                Spacer(modifier = Modifier.height(12.dp))
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = PharaohGold.copy(alpha = 0.15f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, PharaohGold),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Badge, contentDescription = null, tint = PharaohGoldDark, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("كود الطالب الجامعي المُولد:", fontSize = 12.sp, color = PharaohNavyDark, fontWeight = FontWeight.Bold)
                        }
                        Text(
                            text = uiState.supabaseGeneratedStudentCode,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = PharaohNavyDark,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }

            // Warning or Error Box
            AnimatedVisibility(visible = uiState.supabaseErrorMessage != null) {
                uiState.supabaseErrorMessage?.let { errMsg ->
                    Spacer(modifier = Modifier.height(10.dp))
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFFFEF2F2),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFCA5A5)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.ErrorOutline,
                                contentDescription = null,
                                tint = Color(0xFFDC2626),
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = errMsg,
                                color = Color(0xFFB91C1C),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }

            // Loading Progress Bar
            AnimatedVisibility(visible = uiState.isSavingToSupabase) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp)
                ) {
                    Text(
                        text = "جاري الاتصال وحفظ السجل في سحابة Supabase...",
                        fontSize = 11.sp,
                        color = Color(0xFF047857),
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    LinearProgressIndicator(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = Color(0xFF10B981),
                        trackColor = Color(0xFFD1FAE5)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Save Button
            Button(
                onClick = { viewModel.registerStudentToSupabase() },
                enabled = !uiState.isSavingToSupabase,
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF10B981),
                    contentColor = Color.White
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("btn_save_supabase_student")
            ) {
                if (uiState.isSavingToSupabase) {
                    CircularProgressIndicator(
                        color = Color.White,
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("جاري الحفظ...", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                } else {
                    Icon(
                        imageVector = Icons.Default.CloudDone,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("إتمام القيد السحابي وتوليد الكود", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

/**
 * حوار نجاح تسجيل الطالب في سحابة Supabase
 */
@Composable
fun SupabaseSuccessDialog(
    student: SupabaseStudent,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = CircleShape,
                    color = Color(0xFFDCFCE7),
                    modifier = Modifier.size(36.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = Color(0xFF16A34A),
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "تم القيد بنجاح في Supabase!",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 16.sp,
                    color = PharaohNavyDark
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "تم إدراج بيانات الطالب رسمياً في جدول (student) السحابي، وأصبح بإمكانك الدخول الفوري وعرض الكارنيه الجامعي:",
                    fontSize = 13.sp,
                    color = Color.DarkGray
                )

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFFF8FAFC),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("الاسم:", fontSize = 12.sp, color = Color.Gray)
                            Text(student.fullName, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = PharaohNavyDark)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("الرقم القومي:", fontSize = 12.sp, color = Color.Gray)
                            Text(student.nationalId, fontSize = 12.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("الفرقة والتخصص:", fontSize = 12.sp, color = Color.Gray)
                            Text(student.academicYear, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF047857))
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("رقم الهاتف:", fontSize = 12.sp, color = Color.Gray)
                            Text(student.phone, fontSize = 12.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                        }
                        student.id?.let { idVal ->
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("معرف السجل (ID):", fontSize = 12.sp, color = Color.Gray)
                                Text("#$idVal", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = PharaohGoldDark)
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981))
            ) {
                Text("الانتقال إلى الكارنيه الجامعي", fontWeight = FontWeight.Bold)
            }
        }
    )
}

/**
 * بطاقة عرض طالب من قائمة Supabase
 */
@Composable
fun SupabaseStudentCard(
    student: SupabaseStudent,
    onQuickLogin: (String) -> Unit = {}
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0))
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
                        color = Color(0xFF10B981).copy(alpha = 0.15f),
                        modifier = Modifier.size(34.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.Person, contentDescription = null, tint = Color(0xFF047857), modifier = Modifier.size(18.dp))
                        }
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(student.fullName, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = PharaohNavyDark)
                        Text(student.academicYear, fontSize = 11.sp, color = Color.Gray)
                    }
                }

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = Color(0xFFF1F5F9)
                ) {
                    Text(
                        text = "ID: ${student.id ?: "-"}",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.DarkGray,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Fingerprint, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(student.nationalId, fontSize = 11.sp, fontFamily = FontFamily.Monospace, color = Color.DarkGray)
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Phone, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(student.phone, fontSize = 11.sp, fontFamily = FontFamily.Monospace, color = Color.DarkGray)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedButton(
                onClick = { onQuickLogin(student.nationalId) },
                modifier = Modifier.fillMaxWidth().height(36.dp),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF047857))
            ) {
                Icon(Icons.Default.Badge, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("تسجيل الدخول بهذا الحساب وعرض الكارنيه", fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}
