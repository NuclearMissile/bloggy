package nuclear.com.bloggy.Util

import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

/**
 * Created by torri on 2017/10/26.
 */

object DateUtil {
    val TimeStamp: Long
        get() = System.currentTimeMillis()
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    private val dateTimeFormat = SimpleDateFormat("yyyy-MM-dd HH-mm-ss", Locale.getDefault())
    private fun formatDate(date: Date) = dateFormat.format(date)
    private fun formatDateTime(date: Date) = dateTimeFormat.format(date)

    fun isToday(date: Date): Boolean {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)
        calendar.time = date
        return day == calendar.get(Calendar.DAY_OF_MONTH) &&
                month == calendar.get(Calendar.MONTH) &&
                year == calendar.get(Calendar.YEAR)
    }

    fun timeStamp2DateTime(timeInMillis: Long): String {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = timeInMillis
        val date = calendar.time
        return formatDateTime(date)
    }

    fun timeStamp2Date(timeInMillis: Long): String {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = timeInMillis
        val date = calendar.time
        return formatDate(date)
    }

    /**
     * @param date yyyy-MM-dd
     * @return Date
     */
    fun parseDate(date: String): Date? {
        var mDate: Date? = null
        try {
            mDate = dateFormat.parse(date)
        } catch (e: ParseException) {
            e.printStackTrace()
        }

        return mDate
    }

    /**
     * @param dateTime yyyy-MM-dd HH-mm-ss
     * @return
     */
    fun parseDateTime(dateTime: String): Date? {
        var mDate: Date? = null
        try {
            mDate = dateTimeFormat.parse(dateTime)
        } catch (e: ParseException) {
            e.printStackTrace()
        }

        return mDate
    }

    fun getFriendlyTime(timeStamp: Long) =
            getFriendlyTime(timeStamp2DateTime(timeStamp))

    /**
     * @param dateTime yyyy-MM-dd HH-mm-ss
     * @return
     */
    fun getFriendlyTime(dateTime: String): String {
        val time = parseDateTime(dateTime.replace(":", "-")) ?: return "Parse Error."
        val result: String
        val calendar = Calendar.getInstance()
        val today = formatDate(calendar.time)

        if (today == formatDate(time)) {
            val hourDiff = (calendar.timeInMillis - time.time) / 3600000
            result = if (hourDiff == 0L) {
                Math.max((calendar.timeInMillis - time.time) / 60000, 1).toString() + " min(s) ago"
            } else {
                hourDiff.toString() + " hour(s) ago"
            }
            return result
        }

        val dayDiff = (calendar.timeInMillis - time.time) / 86400000
        result = if (dayDiff == 0L) {
            val hourDiff = (calendar.timeInMillis - time.time) / 3600000
            if (hourDiff == 0L) {
                Math.max((calendar.timeInMillis - time.time) / 60000, 1).toString() + " min(s) ago"
            } else {
                hourDiff.toString() + " hour(s) ago"
            }
        } else if (dayDiff <= 7) {
            dayDiff.toString() + " days ago"
        } else {
            formatDate(time)
        }
        return result
    }
}
