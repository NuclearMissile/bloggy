package nuclear.com.bloggy.Util;

import android.text.TextUtils;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;

public class HexUtilJ {
    private static final String HEXES = "0123456789ABCDEF";

    @Nullable
    public static String bytes2Hex(@NotNull Collection<Byte> bytes) {
        if (bytes.size() == 0)
            return null;
        StringBuilder sb = new StringBuilder(bytes.size() << 1);
        for (byte b : bytes)
            sb.append(HEXES.charAt(b & 0xF0 >> 4)).append(HEXES.charAt(b & 0x0F));
        return sb.toString();
    }

    @Nullable
    public static Collection<Byte> hex2Bytes(@NotNull String hex) {
        if (TextUtils.isEmpty(hex) || hex.length() < 2)
            return null;
        String hexString = hex.toUpperCase();
        int cap = hexString.length() >> 1;
        ArrayList<Byte> res = new ArrayList<>(cap);
        int pos;
        for (int i = 0; i < cap; i++) {
            pos = i << 1;
            res.add(i, (byte) (HEXES.charAt(hexString.charAt(pos)) << 4 | HEXES.charAt(pos + 1)));
        }
        return res;
    }
}
