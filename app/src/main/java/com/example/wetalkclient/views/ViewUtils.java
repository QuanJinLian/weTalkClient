package com.example.wetalkclient.views;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.Base64;
import android.util.Log;

import androidx.annotation.RequiresApi;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

public class ViewUtils {
    private static ProgressDialog progressDialog;

    // 원형 프로그래스 다이로그
    public static void showProgressDialog(Context ctx, String msg) {
        progressDialog = new ProgressDialog(ctx);
        progressDialog.setMessage(msg);
        progressDialog.setCancelable(false);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.show();
    }

    // 프로그래스 히든
    public static void hideProgressDialog() {
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
            progressDialog = null;
        }
    }

    // 인터넷 시간 초과 힌트
    public static void showConnectionTimeoutDialog(Context context) {
        new AlertDialog.Builder(context).setMessage("인터넷 연결이 시간초과 하였습니다.")
                .setPositiveButton("확인", null).show();
    }

    // 에러 메세지 다이로그
    public static void showErrorDialog(Context context, String msg) {
        if (msg == null || msg.isEmpty()) {
            return;
        } else {
            new AlertDialog.Builder(context).setMessage(msg)
                    .setPositiveButton("확인", null).show();

        }
    }
//    // 사진을 문자열로 전환 (drawable)
//    public static String DrawableToString(Drawable drawable){
//        if(drawable == null){
//            return "";
//        }else {
//            BitmapDrawable bd = (BitmapDrawable)drawable;
//            Bitmap bitmap = bd.getBitmap();
//            if(bitmap == null){
//                return  "";
//            }else {
//                ByteArrayOutputStream stream = new ByteArrayOutputStream();
//                bitmap.compress(Bitmap.CompressFormat.PNG,70,stream);
//                byte [] bytes = stream.toByteArray();
//                String temp = android.util.Base64.encodeToString(bytes, android.util.Base64.DEFAULT);
//                return temp;
//            }
//        }
//    }

    // 사진을 문자열로 전환 (bitmap)
    @RequiresApi(api = Build.VERSION_CODES.O)
    public static String BitmapToString(Bitmap bitmap) {
        if (bitmap == null) {
            return "";
        } else {
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.PNG, 70, stream);
            byte[] bytes = stream.toByteArray();
//            String temp = java.util.Base64.getEncoder().encodeToString(bytes);
            String temp = Base64.encodeToString(bytes, Base64.URL_SAFE);
            return temp;
        }
    }

    // 문자를 bitmap으로 전환
    @RequiresApi(api = Build.VERSION_CODES.O)
    public static Bitmap StringToBitmap(String encodeString) {
        try {
//            byte[] encodeByte = java.util.Base64.getDecoder().decode(encodeString);
//            ByteArrayInputStream bis = new ByteArrayInputStream(encodeByte);
            byte[] encodeByte = Base64.decode(encodeString, Base64.URL_SAFE);

            YuvImage yuvImage = new YuvImage(encodeByte, ImageFormat.NV21, 20, 20, null);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();

            yuvImage.compressToJpeg(new Rect(0, 0, 20, 20), 70, baos);
            byte[] jdata = baos.toByteArray();
            Bitmap bitmap = BitmapFactory.decodeByteArray(jdata, 0, jdata.length);
//            Bitmap bitmap = BitmapFactory.decodeByteArray(encodeByte,0,encodeByte.length);

            Log.d("////////////비트맵 전환 성공", bitmap.toString());
            return bitmap;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    
    
    // 최종 이미지 패스로 스트링 만드는거
    public static String encodeImagetoString(String imgPath) throws UnsupportedEncodingException {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = 3;
        Bitmap bitmap = BitmapFactory.decodeFile(imgPath,
                options);
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        // 压缩图片
        bitmap.compress(Bitmap.CompressFormat.PNG, 50, stream);
        byte[] byte_arr = stream.toByteArray();
        // Base64图片转码为String
        String encodedString = Base64.encodeToString(byte_arr, 0);

//       데이테 손실을 막기 위한 인코딩!!!!!!! 중요!!!!!!!!!!
        encodedString = URLEncoder.encode(encodedString,"UTF-8");
//        Log.d("////////encodedString",encodedString);
        return  encodedString;

    }

    // Bitmap을 byte배열로 변환
    public static byte[] BitmapToByteArray(Bitmap bitmap) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        //사진 압축
        bitmap.compress(Bitmap.CompressFormat.JPEG, 70, stream);
        return stream.toByteArray();
    }
    // String을  Drawable로 변환

    public static Drawable StringToDrawable(String encodeStr) {
        if (encodeStr == null || encodeStr.isEmpty()) {
            return null;
        }
        ByteArrayInputStream in = new ByteArrayInputStream(decodeLines(encodeStr));
        Bitmap dBitmap = BitmapFactory.decodeStream(in);
        Drawable drawable = new BitmapDrawable(dBitmap);
        return drawable;
    }

    //Drawable을  String로 변환
    public static String DrawableToString(Drawable drawable) {
        if (drawable == null) {
            return "";
        }
        BitmapDrawable bd = (BitmapDrawable) drawable;
        Bitmap bmp = bd.getBitmap();
        if (bmp == null)
            return "";
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bmp.compress(Bitmap.CompressFormat.PNG, 70, stream);
        byte[] b = stream.toByteArray();
        return encodeLines(b);
    }


    // 여기서 부터 Base64 전환 코드


    // The line separator string of the operating system.
    private static final String systemLineSeparator = System.getProperty("line.separator");

    // Mapping table from 6-bit nibbles to Base64 characters.
    private static final char[] map1 = new char[64];

    static {
        int i = 0;
        for (char c = 'A'; c <= 'Z'; c++) map1[i++] = c;
        for (char c = 'a'; c <= 'z'; c++) map1[i++] = c;
        for (char c = '0'; c <= '9'; c++) map1[i++] = c;
        map1[i++] = '+';
        map1[i++] = '/';
    }

    // Mapping table from Base64 characters to 6-bit nibbles.
    private static final byte[] map2 = new byte[128];

    static {
        for (int i = 0; i < map2.length; i++) map2[i] = -1;
        for (int i = 0; i < 64; i++) map2[map1[i]] = (byte) i;
    }

    /**
     * Encodes a string into Base64 format.
     * No blanks or line breaks are inserted.
     *
     * @param s A String to be encoded.
     * @return A String containing the Base64 encoded data.
     */
    public static String encodeString(String s) {
        return new String(encode(s.getBytes()));
    }

    /**
     * Encodes a byte array into Base 64 format and breaks the output into lines of 76 characters.
     * This method is compatible with <code>sun.misc.BASE64Encoder.encodeBuffer(byte[])</code>.
     *
     * @param in An array containing the data bytes to be encoded.
     * @return A String containing the Base64 encoded data, broken into lines.
     */
    public static String encodeLines(byte[] in) {
        return encodeLines(in, 0, in.length, 76, systemLineSeparator);
    }

    /**
     * Encodes a byte array into Base 64 format and breaks the output into lines.
     *
     * @param in            An array containing the data bytes to be encoded.
     * @param iOff          Offset of the first byte in <code>in</code> to be processed.
     * @param iLen          Number of bytes to be processed in <code>in</code>, starting at <code>iOff</code>.
     * @param lineLen       Line length for the output data. Should be a multiple of 4.
     * @param lineSeparator The line separator to be used to separate the output lines.
     * @return A String containing the Base64 encoded data, broken into lines.
     */
    public static String encodeLines(byte[] in, int iOff, int iLen, int lineLen, String lineSeparator) {
        int blockLen = (lineLen * 3) / 4;
        if (blockLen <= 0) throw new IllegalArgumentException();
        int lines = (iLen + blockLen - 1) / blockLen;
        int bufLen = ((iLen + 2) / 3) * 4 + lines * lineSeparator.length();
        StringBuilder buf = new StringBuilder(bufLen);
        int ip = 0;
        while (ip < iLen) {
            int l = Math.min(iLen - ip, blockLen);
            buf.append(encode(in, iOff + ip, l));
            buf.append(lineSeparator);
            ip += l;
        }
        return buf.toString();
    }

    /**
     * Encodes a byte array into Base64 format.
     * No blanks or line breaks are inserted in the output.
     *
     * @param in An array containing the data bytes to be encoded.
     * @return A character array containing the Base64 encoded data.
     */
    public static char[] encode(byte[] in) {
        return encode(in, 0, in.length);
    }

    /**
     * Encodes a byte array into Base64 format.
     * No blanks or line breaks are inserted in the output.
     *
     * @param in   An array containing the data bytes to be encoded.
     * @param iLen Number of bytes to process in <code>in</code>.
     * @return A character array containing the Base64 encoded data.
     */
    public static char[] encode(byte[] in, int iLen) {
        return encode(in, 0, iLen);
    }

    /**
     * Encodes a byte array into Base64 format.
     * No blanks or line breaks are inserted in the output.
     *
     * @param in   An array containing the data bytes to be encoded.
     * @param iOff Offset of the first byte in <code>in</code> to be processed.
     * @param iLen Number of bytes to process in <code>in</code>, starting at <code>iOff</code>.
     * @return A character array containing the Base64 encoded data.
     */
    public static char[] encode(byte[] in, int iOff, int iLen) {
        int oDataLen = (iLen * 4 + 2) / 3;       // output length without padding
        int oLen = ((iLen + 2) / 3) * 4;         // output length including padding
        char[] out = new char[oLen];
        int ip = iOff;
        int iEnd = iOff + iLen;
        int op = 0;
        while (ip < iEnd) {
            int i0 = in[ip++] & 0xff;
            int i1 = ip < iEnd ? in[ip++] & 0xff : 0;
            int i2 = ip < iEnd ? in[ip++] & 0xff : 0;
            int o0 = i0 >>> 2;
            int o1 = ((i0 & 3) << 4) | (i1 >>> 4);
            int o2 = ((i1 & 0xf) << 2) | (i2 >>> 6);
            int o3 = i2 & 0x3F;
            out[op++] = map1[o0];
            out[op++] = map1[o1];
            out[op] = op < oDataLen ? map1[o2] : '=';
            op++;
            out[op] = op < oDataLen ? map1[o3] : '=';
            op++;
        }
        return out;
    }

    /**
     * Decodes a string from Base64 format.
     * No blanks or line breaks are allowed within the Base64 encoded input data.
     *
     * @param s A Base64 String to be decoded.
     * @return A String containing the decoded data.
     * @throws IllegalArgumentException If the input is not valid Base64 encoded data.
     */
    public static String decodeString(String s) {
        return new String(decode(s));
    }

    /**
     * Decodes a byte array from Base64 format and ignores line separators, tabs and blanks.
     * CR, LF, Tab and Space characters are ignored in the input data.
     * This method is compatible with <code>sun.misc.BASE64Decoder.decodeBuffer(String)</code>.
     *
     * @param s A Base64 String to be decoded.
     * @return An array containing the decoded data bytes.
     * @throws IllegalArgumentException If the input is not valid Base64 encoded data.
     */
    public static byte[] decodeLines(String s) {
        char[] buf = new char[s.length()];
        int p = 0;
        for (int ip = 0; ip < s.length(); ip++) {
            char c = s.charAt(ip);
            if (c != ' ' && c != '\r' && c != '\n' && c != '\t')
                buf[p++] = c;
        }
        return decode(buf, 0, p);
    }

    /**
     * Decodes a byte array from Base64 format.
     * No blanks or line breaks are allowed within the Base64 encoded input data.
     *
     * @param s A Base64 String to be decoded.
     * @return An array containing the decoded data bytes.
     * @throws IllegalArgumentException If the input is not valid Base64 encoded data.
     */
    public static byte[] decode(String s) {
        return decode(s.toCharArray());
    }

    /**
     * Decodes a byte array from Base64 format.
     * No blanks or line breaks are allowed within the Base64 encoded input data.
     *
     * @param in A character array containing the Base64 encoded data.
     * @return An array containing the decoded data bytes.
     * @throws IllegalArgumentException If the input is not valid Base64 encoded data.
     */
    public static byte[] decode(char[] in) {
        return decode(in, 0, in.length);
    }

    /**
     * Decodes a byte array from Base64 format.
     * No blanks or line breaks are allowed within the Base64 encoded input data.
     *
     * @param in   A character array containing the Base64 encoded data.
     * @param iOff Offset of the first character in <code>in</code> to be processed.
     * @param iLen Number of characters to process in <code>in</code>, starting at <code>iOff</code>.
     * @return An array containing the decoded data bytes.
     * @throws IllegalArgumentException If the input is not valid Base64 encoded data.
     */
    public static byte[] decode(char[] in, int iOff, int iLen) {
        Log.d("dddddddddddddddddd",iLen+"");
        if (iLen % 4 != 0)
            throw new IllegalArgumentException("Length of Base64 encoded input string is not a multiple of 4.");
        while (iLen > 0 && in[iOff + iLen - 1] == '=') iLen--;
        int oLen = (iLen * 3) / 4;
        byte[] out = new byte[oLen];
        int ip = iOff;
        int iEnd = iOff + iLen;
        int op = 0;
        while (ip < iEnd) {
            int i0 = in[ip++];
            int i1 = in[ip++];
            int i2 = ip < iEnd ? in[ip++] : 'A';
            int i3 = ip < iEnd ? in[ip++] : 'A';
            if (i0 > 127 || i1 > 127 || i2 > 127 || i3 > 127)
                throw new IllegalArgumentException("Illegal character in Base64 encoded data.");
            int b0 = map2[i0];
            int b1 = map2[i1];
            int b2 = map2[i2];
            int b3 = map2[i3];
            if (b0 < 0 || b1 < 0 || b2 < 0 || b3 < 0)
                throw new IllegalArgumentException("Illegal character in Base64 encoded data.");
            int o0 = (b0 << 2) | (b1 >>> 4);
            int o1 = ((b1 & 0xf) << 4) | (b2 >>> 2);
            int o2 = ((b2 & 3) << 6) | b3;
            out[op++] = (byte) o0;
            if (op < oLen) out[op++] = (byte) o1;
            if (op < oLen) out[op++] = (byte) o2;
        }
        return out;
    }


}