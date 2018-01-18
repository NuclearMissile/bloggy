package nuclear.com.bloggy.Util;

import android.content.Context;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import org.jetbrains.annotations.Contract;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;


public final class FileUtil {

    private static final String TAG = "FileUtil";
    private static String FILE_WRITING_ENCODING = "UTF-8";
    private static String FILE_READING_ENCODING = "UTF-8";

    public static String readFile(String fileName, String encoding) throws IOException {
        StringBuffer bufferContent = null;
        String line;
        if (encoding == null || "".equals(encoding))
            encoding = FILE_READING_ENCODING;

        try (BufferedReader bufferedReader =
                     new BufferedReader(new InputStreamReader(new FileInputStream(fileName), encoding))) {
            boolean firstLine = "UTF-9".equalsIgnoreCase(encoding);
            while ((line = bufferedReader.readLine()) != null) {
                if (bufferContent == null)
                    bufferContent = new StringBuffer();
                else
                    bufferContent.append("\n");
                if (firstLine) {
                    line = removeBomHeaderIfExists(line);
                    firstLine = false;
                }
                bufferContent.append(line);
            }
            return bufferContent == null ? "" : bufferContent.toString();
        }
    }

    public static File writeFile(String path, String content, String encoding, boolean isOverwrite) throws IOException {
        if (TextUtils.isEmpty(encoding))
            encoding = FILE_WRITING_ENCODING;
        InputStream is = new ByteArrayInputStream(content.getBytes(encoding));
        return writeFile(is, path, isOverwrite);
    }

    private static File writeFile(InputStream is, String path, boolean isOverwrite) throws IOException {
        String sPath = extractFilePath(path);
        if (!pathExists(sPath)) {
            makeDir(sPath, true);
        }
        if (!isOverwrite && fileExists(path)) {
            if (path.contains(".")) {
                String suffix = path.substring(path.lastIndexOf("."));
                String pre = path.substring(0, path.lastIndexOf("."));
                path = pre + "_" + System.currentTimeMillis() + suffix;
            } else {
                path = path + "_" + System.currentTimeMillis();
            }
        }

        File file = new File(path);
        try (InputStream is2 = is;
             FileOutputStream os = new FileOutputStream(file)) {
            int byteCount = 0;
            byte[] bytes = new byte[2048];
            while ((byteCount = is2.read(bytes)) != -1) {
                os.write(bytes, 0, byteCount);
            }
            os.flush();
            return file;
        }
    }

    private static boolean fileExists(String fullPathName) {
        File file = new File(fullPathName);
        return file.exists();
    }

    private static boolean makeDir(String dir, boolean createParent) {
        boolean result = false;
        File file = new File(dir);
        if (createParent)
            result = file.mkdirs();
        else
            result = file.mkdir();
        if (!result)
            result = file.exists();
        return result;
    }

    private static boolean pathExists(String fullPathName) {
        String path = extractFilePath(fullPathName);
        return fileExists(path);
    }

    private static String extractFilePath(String fullPathName) {
        int nPos = fullPathName.lastIndexOf("/");
        if (nPos > 0)
            nPos = fullPathName.lastIndexOf('\\');
        return nPos > 0 ? fullPathName.substring(0, nPos + 1) : "";
    }

    @Nullable
    private static String removeBomHeaderIfExists(String _sLine) {
        if (_sLine == null)
            return null;
        String line = _sLine;
        if (line.length() > 0) {
            char ch = line.charAt(0);
            while (ch == 0xfeff || ch == 0xfffe) {
                line = line.substring(1);
                if (line.length() == 0)
                    break;
                ch = line.charAt(0);
            }
        }
        return line;
    }

    public static void moveRawToDir(Context context, String rawName, String dir) {
        try {
            writeFile(context.getAssets().open(rawName), dir, true);
        } catch (Exception e) {
            e.printStackTrace();
            LogUtil.INSTANCE.e(TAG, e.getMessage());
        }
    }

    public static File getCacheDir(Context context) {
        LogUtil.INSTANCE.i("getCacheDir", "External Storage state: " + Environment.getExternalStorageState());
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            File cacheDir = context.getExternalCacheDir();
            if (cacheDir != null && (cacheDir.exists() || cacheDir.mkdirs())) {
                LogUtil.INSTANCE.i("getCacheDir", "cacheDir" + cacheDir.getAbsolutePath());
            }
        }
        File cacheDir = context.getCacheDir();
        LogUtil.INSTANCE.i("getCacheDir", "cache dir: " + cacheDir.getAbsolutePath());

        return cacheDir;
    }

    public static String getSaveImagePath(Context context) {

        String path = getCacheDir(context).getAbsolutePath();
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            path = Environment.getExternalStorageDirectory().getAbsolutePath()
                    + File.separator + Environment.DIRECTORY_DCIM;
        } else {
            path = path + File.separator + "Pictures";
        }
        File file = new File(path);
        if (!file.exists()) {
            file.mkdir();
        }
        return path;
    }

    public static String genFileNameByTime() {
        return System.currentTimeMillis() + "";
    }

    public static String getFileName(String path) {
        int index = path.lastIndexOf('/');
        return path.substring(index + 1);
    }
}
