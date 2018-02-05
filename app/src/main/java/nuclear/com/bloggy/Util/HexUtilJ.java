package nuclear.com.bloggy.Util;

import android.text.TextUtils;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class HexUtilJ {
    private static final String HEXES = "0123456789ABCDEF";

    @Nullable
    public static String bytes2Hex(@NotNull List<Byte> bytes) {
        int s = bytes.size();
        if (s == 0)
            return null;
        StringBuilder sb = new StringBuilder(s << 1);
        int i = 0;
        for (; i <= s - 4; i += 4) {
            sb.append(HEXES.charAt(bytes.get(i) & 0xF0 >> 4)).append(HEXES.charAt(bytes.get(i) & 0x0F))
                    .append(HEXES.charAt(bytes.get(i + 1) & 0xF0 >> 4)).append(HEXES.charAt(bytes.get(i + 1) & 0x0F))
                    .append(HEXES.charAt(bytes.get(i + 2) & 0xF0 >> 4)).append(HEXES.charAt(bytes.get(i + 2) & 0x0F))
                    .append(HEXES.charAt(bytes.get(i + 3) & 0xF0 >> 4)).append(HEXES.charAt(bytes.get(i + 3) & 0x0F));
        }
        for (; i < s; i++)
            sb.append(HEXES.charAt(bytes.get(i) & 0xF0 >> 4)).append(HEXES.charAt(bytes.get(i) & 0x0F));
        return sb.toString();
    }

    @Nullable
    public static List<Byte> hex2Bytes(@NotNull String hex) {
        if (TextUtils.isEmpty(hex) || hex.length() < 2 || hex.length() % 2 != 0)
            return null;
        String hexString = hex.toUpperCase();
        int cap = hexString.length() >> 1;
        ArrayList<Byte> res = new ArrayList<>(cap);
        int i = 0, pos = 0;
        for (; i <= cap - 4; i += 4) {
            pos = i << 1;
            res.add(i, (byte) (HEXES.charAt(hexString.charAt(pos)) << 4 | HEXES.charAt(pos + 1)));
            res.add(i + 1, (byte) (HEXES.charAt(hexString.charAt(pos + 2)) << 4 | HEXES.charAt(pos + 3)));
            res.add(i + 2, (byte) (HEXES.charAt(hexString.charAt(pos + 4)) << 4 | HEXES.charAt(pos + 5)));
            res.add(i + 3, (byte) (HEXES.charAt(hexString.charAt(pos + 6)) << 4 | HEXES.charAt(pos + 7)));
        }
        for (; i < cap; i++) {
            pos = i << 1;
            res.add(i, (byte) (HEXES.charAt(hexString.charAt(pos)) << 4 | HEXES.charAt(pos + 1)));
        }
        return res;
    }
}
