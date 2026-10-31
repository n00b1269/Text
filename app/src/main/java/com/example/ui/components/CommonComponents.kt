package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun CodeViewer(
    code: String,
    language: String = "swift",
    modifier: Modifier = Modifier
) {
    val clipboardManager = LocalClipboardManager.current
    var copied by remember { mutableStateOf(false) }

    val lines = remember(code) { code.lines() }
    val horizontalScroll = rememberScrollState()

    Card(
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(12.dp)),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Header bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF1E293B))
                    .padding(horizontal = 14.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(Color(0xFFEF4444)))
                    Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(Color(0xFFF59E0B)))
                    Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(Color(0xFF10B981)))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = language.uppercase(),
                        color = Color(0xFF94A3B8),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }

                IconButton(
                    onClick = {
                        clipboardManager.setText(AnnotatedString(code))
                        copied = true
                    },
                    modifier = Modifier.size(32.dp).testTag("copy_code_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.ContentCopy,
                        contentDescription = "Copy Code",
                        tint = if (copied) Color(0xFF38BDF8) else Color(0xFF94A3B8),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            // Code content with line numbers
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(horizontalScroll)
                    .padding(14.dp)
            ) {
                // Line numbers
                Column(modifier = Modifier.padding(end = 12.dp)) {
                    lines.indices.forEach { idx ->
                        Text(
                            text = "${idx + 1}",
                            color = Color(0xFF475569),
                            fontFamily = FontFamily.Monospace,
                            fontSize = 12.sp,
                            lineHeight = 18.sp
                        )
                    }
                }

                // Code lines
                Column {
                    lines.forEach { line ->
                        Text(
                            text = highlightKeywords(line, language),
                            fontFamily = FontFamily.Monospace,
                            fontSize = 12.sp,
                            lineHeight = 18.sp
                        )
                    }
                }
            }
        }
    }
}

private fun highlightKeywords(line: String, language: String): AnnotatedString {
    return buildAnnotatedString {
        if (line.trim().startsWith("//") || line.trim().startsWith("/*")) {
            withStyle(SpanStyle(color = Color(0xFF64748B))) {
                append(line)
            }
            return@buildAnnotatedString
        }

        val tokens = line.split(Regex("(?<=[\\s(),.:;=<>!&|{}])|(?=[\\s(),.:;=<>!&|{}])"))
        for (token in tokens) {
            when (token) {
                "import", "struct", "class", "var", "let", "func", "val", "fun", "return",
                "some", "View", "Scene", "App", "if", "else", "switch", "case", "for", "in",
                "package", "data", "override", "private", "public", "true", "false", "nil", "null" -> {
                    withStyle(SpanStyle(color = Color(0xFFF472B6), fontWeight = FontWeight.SemiBold)) {
                        append(token)
                    }
                }
                "@Observable", "@main", "@State", "@Bindable", "@Published", "@Composable", "@Entity", "@Dao" -> {
                    withStyle(SpanStyle(color = Color(0xFF38BDF8), fontWeight = FontWeight.Bold)) {
                        append(token)
                    }
                }
                "VStack", "HStack", "ZStack", "List", "ScrollView", "Text", "Button", "Image",
                "Column", "Row", "Box", "LazyColumn", "Card", "Scaffold", "TextField" -> {
                    withStyle(SpanStyle(color = Color(0xFFFBBF24), fontWeight = FontWeight.Medium)) {
                        append(token)
                    }
                }
                else -> {
                    if (token.startsWith("\"") && token.endsWith("\"")) {
                        withStyle(SpanStyle(color = Color(0xFF34D399))) {
                            append(token)
                        }
                    } else {
                        withStyle(SpanStyle(color = Color(0xFFE2E8F0))) {
                            append(token)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StatusBanner(
    message: String,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Info,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.weight(1f)
            )
            TextButton(onClick = onDismiss) {
                Text("OK")
            }
        }
    }
}
