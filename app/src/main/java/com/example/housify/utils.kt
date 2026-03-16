package com.example.housify

import android.graphics.Bitmap
import android.graphics.Color
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.core.graphics.set
import androidx.core.graphics.createBitmap
import java.time.LocalDate
import java.util.Calendar
import java.util.TimeZone

fun todayLocalDate(): String {
    val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    return formatter.format(Date())
}

fun convertMillisToDate(millis: Long, convert: Boolean = false): String {
    val formatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    formatter.timeZone = if (convert) TimeZone.getTimeZone("GMT+8") else TimeZone.getTimeZone("UTC")
    return formatter.format(Date(millis))
}

fun convertDateToDayOfWeek(date: String, convert: Boolean = false): String {
    val formatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    formatter.timeZone = if (convert) TimeZone.getTimeZone("GMT+8") else TimeZone.getTimeZone("UTC")
    val parsedDate = formatter.parse(date)

    val calendar = Calendar.getInstance()
    if (parsedDate != null) {
        calendar.time = parsedDate
    }

    val dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)

    return when (dayOfWeek) {
        Calendar.SUNDAY -> "Sunday"
        Calendar.MONDAY -> "Monday"
        Calendar.TUESDAY -> "Tuesday"
        Calendar.WEDNESDAY -> "Wednesday"
        Calendar.THURSDAY -> "Thursday"
        Calendar.FRIDAY -> "Friday"
        Calendar.SATURDAY -> "Saturday"
        else -> ""
    }
}

@RequiresApi(Build.VERSION_CODES.O)
fun convertMillisToLocalDate(millis: Long): LocalDate {
    val calendar = Calendar.getInstance()
    calendar.timeInMillis = millis
    return LocalDate.of(
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH) + 1,
        calendar.get(Calendar.DAY_OF_MONTH)
    )
}


fun convertMillisToDayName(millis: Long): String {
    val formatter = SimpleDateFormat("EEEE", Locale.getDefault())
    return formatter.format(Date(millis))
}

object QrCodeGenerator {
    fun generate(content: String, width: Int = 512, height: Int = 512): ImageBitmap? {
        val qrCodeWriter = QRCodeWriter()
        try {
            val bitMatrix = qrCodeWriter.encode(content, BarcodeFormat.QR_CODE, width, height)
            val bmp = createBitmap(width, height, Bitmap.Config.RGB_565)
            for (x in 0 until width) {
                for (y in 0 until height) {
                    bmp[x, y] = if (bitMatrix[x, y]) Color.BLACK else Color.WHITE
                }
            }
            return bmp.asImageBitmap()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }
}

sealed class Resource<T>(val data: T? = null, val message: String? = null) {
    class Success<T>(data: T) : Resource<T>(data)
    class Error<T>(message: String, data: T? = null) : Resource<T>(data, message)
    class Loading<T>(data: T? = null) : Resource<T>(data)
}