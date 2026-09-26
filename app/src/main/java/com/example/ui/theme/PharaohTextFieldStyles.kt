package com.example.ui.theme

import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.TextFieldColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

/**
 * ألوان حقول الإدخال الموحدة والمصممة لتوفير وضوح تام للنصوص ومنع ظهور النصوص البيضاء على الخلفيات الفاتحة:
 * focusedTextColor = Color(0xFF0F172A) و unfocusedTextColor = Color(0xFF0F172A)
 * مع ألوان placeholder و label متناسقة وواضحة تماماً.
 */
object PharaohTextFieldStyles {

    val DarkTextColor = Color(0xFF0F172A) // نص داكن مقروء وفائق الوضوح
    val LabelColor = Color(0xFF334155) // كحلي رمادي واضح للعنوان
    val PlaceholderColor = Color(0xFF64748B) // رمادي وسطي واضح
    val BorderFocused = Color(0xFF0F1C3F) // كحلي فرعوني عميق عند التركيز
    val BorderUnfocused = Color(0xFFCBD5E1) // رمادي خفيف عند عدم التركيز
    val CursorColor = Color(0xFFD4AF37) // ذهبي فرعوني للمؤشر

    @Composable
    fun colors(
        focusedTextColor: Color = DarkTextColor,
        unfocusedTextColor: Color = DarkTextColor,
        focusedContainerColor: Color = Color.White,
        unfocusedContainerColor: Color = Color.White,
        focusedBorderColor: Color = BorderFocused,
        unfocusedBorderColor: Color = BorderUnfocused,
        focusedLabelColor: Color = DarkTextColor,
        unfocusedLabelColor: Color = LabelColor,
        focusedPlaceholderColor: Color = PlaceholderColor,
        unfocusedPlaceholderColor: Color = PlaceholderColor,
        cursorColor: Color = CursorColor
    ): TextFieldColors {
        return OutlinedTextFieldDefaults.colors(
            focusedTextColor = focusedTextColor,
            unfocusedTextColor = unfocusedTextColor,
            focusedContainerColor = focusedContainerColor,
            unfocusedContainerColor = unfocusedContainerColor,
            focusedBorderColor = focusedBorderColor,
            unfocusedBorderColor = unfocusedBorderColor,
            focusedLabelColor = focusedLabelColor,
            unfocusedLabelColor = unfocusedLabelColor,
            focusedPlaceholderColor = focusedPlaceholderColor,
            unfocusedPlaceholderColor = unfocusedPlaceholderColor,
            cursorColor = cursorColor
        )
    }
}
