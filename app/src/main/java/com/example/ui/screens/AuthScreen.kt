package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Star
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ClipboardManager
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Department
import com.example.data.model.YearGroup
import com.example.data.supabase.SupabaseConfig
import com.example.ui.components.PharaohsHeader
import com.example.ui.components.SupabaseRegistrationCard
import com.example.ui.components.SupabaseSuccessDialog
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

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AuthScreen(
    uiState: UiState,
    viewModel: PharaohsViewModel,
    modifier: Modifier = Modifier
) {
    // 0: Student Login, 1: Supabase Cloud Registration, 2: Doctor Portal
    var selectedAuthTab by remember { mutableIntStateOf(0) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        PharaohsHeader(
            title = "الفراعنة",
            subtitle = "البوابة الأكاديمية الذكية للجامعة"
        )

        // Auth Navigation Tabs
        TabRow(
            selectedTabIndex = selectedAuthTab,
            containerColor = PharaohNavyDark,
            contentColor = PharaohGoldLight,
            modifier = Modifier.fillMaxWidth()
        ) {
            Tab(
                selected = selectedAuthTab == 0,
                onClick = { selectedAuthTab = 0 },
                text = { Text("دخول الطلاب", fontWeight = FontWeight.Bold, fontSize = 12.sp) },
                icon = { Icon(imageVector = Icons.Default.Badge, contentDescription = null, modifier = Modifier.size(18.dp)) },
                modifier = Modifier.testTag("tab_auth_login")
            )
            Tab(
                selected = selectedAuthTab == 1,
                onClick = { selectedAuthTab = 1 },
                text = { Text("قيد طالب جديد", fontWeight = FontWeight.Bold, fontSize = 12.sp) },
                icon = { Icon(imageVector = Icons.Default.Cloud, contentDescription = null, modifier = Modifier.size(18.dp)) },
                modifier = Modifier.testTag("tab_auth_supabase")
            )
            Tab(
                selected = selectedAuthTab == 2,
                onClick = { selectedAuthTab = 2 },
                text = { Text("بوابة الدكاترة", fontWeight = FontWeight.Bold, fontSize = 12.sp) },
                icon = { Icon(imageVector = Icons.Default.AdminPanelSettings, contentDescription = null, modifier = Modifier.size(18.dp)) },
                modifier = Modifier.testTag("tab_auth_doctor")
            )
        }

        // Body Content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            when (selectedAuthTab) {
                0 -> StudentLoginSection(uiState = uiState, viewModel = viewModel, onNavigateToRegister = { selectedAuthTab = 1 })
                1 -> SupabaseSection(uiState = uiState, viewModel = viewModel, onNavigateToLogin = { selectedAuthTab = 0 })
                2 -> DoctorPortalSection(uiState = uiState, viewModel = viewModel)
            }
        }
    }

    // Success Dialog on Supabase Cloud Registration
    if (uiState.supabaseSuccessStudent != null) {
        SupabaseSuccessDialog(
            student = uiState.supabaseSuccessStudent!!,
            onDismiss = { viewModel.dismissSupabaseSuccessDialog() }
        )
    }
}

/**
 * قسم القيد السحابي لطلاب الجامعة عبر Supabase
 */
@Composable
private fun SupabaseSection(
    uiState: UiState,
    viewModel: PharaohsViewModel,
    onNavigateToLogin: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        // Welcome and info banner
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A))
        ) {
            Row(
                modifier = Modifier.padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = CircleShape,
                    color = Color(0xFF10B981).copy(alpha = 0.2f),
                    modifier = Modifier.size(44.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Cloud,
                            contentDescription = null,
                            tint = Color(0xFF34D399),
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "نظام القيد السحابي الموحد",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    Text(
                        text = "يتم تسجيل الطلاب في قاعدة بيانات Supabase الرسمية وتوليد كود جامعي وكارنيه رقمي فوري",
                        color = Color(0xFF94A3B8),
                        fontSize = 11.sp
                    )
                }
            }
        }

        // Supabase Registration Form
        SupabaseRegistrationCard(
            uiState = uiState,
            viewModel = viewModel
        )
    }
}

/**
 * تسجيل دخول الطلاب بالرقم القومي (فحص جدول Supabase مباشرة)
 */
@Composable
private fun StudentLoginSection(
    uiState: UiState,
    viewModel: PharaohsViewModel,
    onNavigateToRegister: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = CircleShape,
                    color = PharaohGold.copy(alpha = 0.2f),
                    modifier = Modifier.size(42.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(imageVector = Icons.Default.Badge, contentDescription = null, tint = PharaohNavy, modifier = Modifier.size(22.dp))
                    }
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "تسجيل دخول الطلاب",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = PharaohNavyDark
                    )
                    Text(
                        text = "أدخل الرقم القومي المسجل في سحابة Supabase",
                        fontSize = 11.sp,
                        color = Color.Gray
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            OutlinedTextField(
                value = uiState.loginCodeInput,
                onValueChange = { viewModel.updateLoginInput(it) },
                label = { Text("الرقم القومي (14 رقماً)") },
                placeholder = { Text("أدخل الرقم القومي الخاص بك") },
                colors = PharaohTextFieldStyles.colors(),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("input_student_login"),
                singleLine = true,
                leadingIcon = {
                    Icon(imageVector = Icons.Default.Fingerprint, contentDescription = null, tint = PharaohNavy)
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                shape = RoundedCornerShape(12.dp)
            )

            if (uiState.loginError != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFFFEF2F2),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFCA5A5)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.ErrorOutline, contentDescription = null, tint = Color(0xFFDC2626), modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = uiState.loginError,
                            color = Color(0xFFB91C1C),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = { viewModel.loginStudent() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("btn_submit_login"),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PharaohNavyDark),
                enabled = !uiState.isLoggingIn
            ) {
                if (uiState.isLoggingIn) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(22.dp), strokeWidth = 2.dp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("جاري التحقق في Supabase...", color = Color.White, fontSize = 13.sp)
                } else {
                    Icon(imageVector = Icons.Default.Key, contentDescription = null, tint = PharaohGoldLight)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "تسجيل الدخول",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            OutlinedButton(
                onClick = onNavigateToRegister,
                modifier = Modifier.fillMaxWidth().height(46.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(imageVector = Icons.Default.PersonAdd, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("قيد طالب جديد", fontSize = 13.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

/**
 * بوابة الدكاترة وأعضاء هيئة التدريس (بدون PIN وبناءً على الاسم والرقم القومي والتخصص)
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun DoctorPortalSection(
    uiState: UiState,
    viewModel: PharaohsViewModel
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = CircleShape,
                    color = PharaohGold.copy(alpha = 0.2f),
                    modifier = Modifier.size(42.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(imageVector = Icons.Default.AdminPanelSettings, contentDescription = null, tint = PharaohNavy, modifier = Modifier.size(24.dp))
                    }
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "بوابة أعضاء هيئة التدريس (الدكاترة)",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = PharaohNavyDark
                    )
                    Text(
                        text = "دخول رسمي معتمد بالرقم القومي والتخصص الأكاديمي (بدون PIN)",
                        fontSize = 11.sp,
                        color = Color.Gray
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Card(
                colors = CardDefaults.cardColors(containerColor = PharaohGoldContainer),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = "🎓 ميزات بوابة عضو هيئة التدريس:",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = PharaohNavyDark
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = "• رفع محاضرات جديدة وملفات الـ PDF إلى Cloudflare R2", fontSize = 11.sp, color = Color.DarkGray)
                    Text(text = "• نشر تكليفات وواجبات دراسية مع تحديد مواعيد التسليم واستلام الحلول", fontSize = 11.sp, color = Color.DarkGray)
                    Text(text = "• إدارة وإضافة مواعيد جدول الامتحانات الرسمية للقسم", fontSize = 11.sp, color = Color.DarkGray)
                    Text(text = "• التحكم محصور حصرياً بالقسم والمواد التابعة لتخصص الدكتور", fontSize = 11.sp, color = Color.DarkGray)
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 1. Doctor Name
            OutlinedTextField(
                value = uiState.doctorNameInput,
                onValueChange = { viewModel.updateDoctorName(it) },
                label = { Text("اسم الدكتور / المحاضر بالكامل *") },
                placeholder = { Text("مثال: أ.د. سامح عبد الفتاح إبراهيم") },
                colors = PharaohTextFieldStyles.colors(),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("input_doctor_name"),
                singleLine = true,
                leadingIcon = {
                    Icon(imageVector = Icons.Default.Person, contentDescription = null, tint = PharaohNavy)
                },
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // 2. Doctor National ID (14 digits)
            OutlinedTextField(
                value = uiState.doctorNationalIdInput,
                onValueChange = { viewModel.updateDoctorNationalId(it) },
                label = { Text("الرقم القومي للدكتور (14 رقماً مصرياً) *") },
                placeholder = { Text("14 رقماً قومياً") },
                colors = PharaohTextFieldStyles.colors(),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("input_doctor_national_id"),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                leadingIcon = {
                    Icon(imageVector = Icons.Default.Fingerprint, contentDescription = null, tint = PharaohNavy)
                },
                trailingIcon = {
                    Text(
                        text = "${uiState.doctorNationalIdInput.length}/14",
                        fontSize = 11.sp,
                        color = if (uiState.doctorNationalIdInput.length == 14 && uiState.doctorNationalIdInfo?.isValid == true) Color(0xFF10B981) else Color.Gray,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(end = 8.dp)
                    )
                },
                shape = RoundedCornerShape(12.dp)
            )

            uiState.doctorNationalIdInfo?.let { info ->
                Spacer(modifier = Modifier.height(4.dp))
                if (info.isValid) {
                    Text(
                        text = "✓ تم التحقق: مواليد ${info.birthDate} - محافظة ${info.governorate}",
                        fontSize = 11.sp,
                        color = Color(0xFF047857),
                        fontWeight = FontWeight.Bold
                    )
                } else if (info.errorMessage != null) {
                    Text(
                        text = "⚠️ ${info.errorMessage}",
                        fontSize = 11.sp,
                        color = Color(0xFFDC2626)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 3. Department / Specialization
            Text(
                text = "القسم والتخصص التابع له الدكتور:",
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
                        selected = uiState.doctorDepartmentInput == dept,
                        onClick = { viewModel.updateDoctorDepartment(dept) },
                        label = { Text(dept.arabicName, fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = PharaohNavyDark,
                            selectedLabelColor = PharaohGoldLight
                        )
                    )
                }
            }

            if (uiState.doctorLoginError != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFFFEF2F2),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFCA5A5)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.ErrorOutline, contentDescription = null, tint = Color(0xFFDC2626), modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = uiState.doctorLoginError, color = Color(0xFFB91C1C), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = { viewModel.loginDoctorAdmin() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("btn_doctor_login"),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PharaohNavyDark)
            ) {
                Icon(imageVector = Icons.Default.AdminPanelSettings, contentDescription = null, tint = PharaohGoldLight)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "دخول بوابة هيئة التدريس والأدمن",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = PharaohGoldLight
                )
            }
        }
    }
}
