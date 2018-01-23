package nuclear.com.bloggy.Util

import android.content.Context
import android.os.Environment
import java.io.File

fun getCacheSize(context: Context): Long {
    var cacheSize: Long = getDirSize(context.cacheDir)
    if (Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED)
        cacheSize += getDirSize(context.externalCacheDir)
    return cacheSize
}

fun getFormatCacheSize(context: Context): String = formatSize(getCacheSize(context))

fun formatSize(size: Long, SIFlag: Boolean = false): String {
    val unit = if (SIFlag) 1000 else 1024
    if (size < 0)
        return "wrong size: $size"
    else if (size < unit)
        return "$size B"
    val exp = (Math.log10(size.toDouble()) / Math.log10(unit.toDouble())).toInt()
    val pre = (if (SIFlag) "kMGTPE" else "KMGTPE")[exp - 1] 
    return String.format("%.1f %sB", size / Math.pow(unit.toDouble(), exp.toDouble()), pre)
}

fun clearAllCache(context: Context) {
    deleteDir(context.cacheDir)
    if (Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED)
        deleteDir(context.externalCacheDir)
}

fun deleteDir(file: File) {
    if (file.isDirectory)
        file.listFiles().forEach { deleteDir(it) }
    file.delete()
}

/*fun deleteDir(file: File) {
    val stack = Stack<File>()
    val currList = ArrayList<File>()
    stack.push(file)
    while (stack.isNotEmpty()) {
        if (stack.lastElement().isDirectory) {
            currList.clear()
            currList.addAll(stack.lastElement().listFiles())
            if (currList.size > 0) {
                currList.forEach { stack.push(it) }
            } else {
                stack.pop().delete()
            }
        } else {
            stack.pop().delete()
        }
    }
}*/

fun getDirSize(file: File): Long {
    var size = 0L
    file.listFiles().forEach { size += if (it.isDirectory) getDirSize(it) else it.length() }
    return size
}
