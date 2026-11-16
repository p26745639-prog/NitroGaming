package com.offhack.nitrogaming;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.text.InputType;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends Activity {
    private static final String YOUTUBE = "https://youtube.com/@offhackhack?si=pVtcVNBp8pgNEryq";
    private static final int REQ_PICK_AVATAR = 2026;
    private static final String AVATAR_FILE = "orlando_profile_avatar.png";

    private static final int BG = Color.rgb(9, 6, 20);
    private static final int PANEL = Color.rgb(22, 15, 46);
    private static final int PANEL_ALT = Color.rgb(36, 22, 74);
    private static final int PURPLE_NEON = Color.rgb(138, 43, 226);
    private static final int BLUE_NEON = Color.rgb(0, 145, 234);
    private static final int CYAN = Color.rgb(32, 215, 255);
    private static final int GOLD = Color.rgb(255, 198, 41);
    private static final int GREEN = Color.rgb(64, 245, 158);
    private static final int ORANGE = Color.rgb(255, 145, 50);
    private static final int RED_WARN = Color.rgb(225, 55, 85);
    private static final int WHITE = Color.WHITE;
    private static final int MUTED = Color.rgb(185, 178, 218);

    private static final String PREF_PROFILE = "active_profile";
    private static final String PREF_GAMING_ACTIVE = "gaming_mode_active";
    private static final String PREF_NO_DISTURB = "gaming_dnd_hint";
    private static final String PREF_SENS_BOOST = "gaming_sens_boost";
    private static final String PREF_FAV_GAME_PKG = "fav_game_pkg";
    private static final String PREF_FAV_GAME_NAME = "fav_game_name";

    private LinearLayout root;
    private SharedPreferences prefs;
    private boolean onHomeScreen = false;

    @Override
    public void onCreate(Bundle state) {
        super.onCreate(state);
        getWindow().setStatusBarColor(BG);
        getWindow().setNavigationBarColor(BG);
        prefs = getSharedPreferences("orlando_hack_v2", MODE_PRIVATE);
        showWelcome();
    }

    private void pickProfilePhoto() {
        try {
            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setType("image/*");
            startActivityForResult(intent, REQ_PICK_AVATAR);
        } catch (Exception e) {
            Intent fallback = new Intent(Intent.ACTION_PICK);
            fallback.setType("image/*");
            startActivityForResult(fallback, REQ_PICK_AVATAR);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQ_PICK_AVATAR && resultCode == RESULT_OK && data != null && data.getData() != null) {
            saveAvatarFromUri(data.getData());
            Toast.makeText(this, "Foto de perfil actualizada", Toast.LENGTH_SHORT).show();
            if (onHomeScreen) {
                showHome();
            } else {
                showWelcome();
            }
        }
    }

    private void saveAvatarFromUri(Uri uri) {
        try (InputStream is = getContentResolver().openInputStream(uri)) {
            Bitmap raw = BitmapFactory.decodeStream(is);
            if (raw == null) return;
            Bitmap square = cropCenterSquare(raw, 360, 1.0f);
            File out = new File(getFilesDir(), AVATAR_FILE);
            try (FileOutputStream fos = new FileOutputStream(out)) {
                square.compress(Bitmap.CompressFormat.PNG, 100, fos);
            }
        } catch (Exception e) {
            Toast.makeText(this, "No se pudo cargar la imagen", Toast.LENGTH_SHORT).show();
        }
    }

    private Bitmap cropCenterSquare(Bitmap src, int targetSize, float innerRatio) {
        int base = Math.min(src.getWidth(), src.getHeight());
        int size = Math.max(10, Math.round(base * innerRatio));
        int x = (src.getWidth() - size) / 2;
        int y = (src.getHeight() - size) / 2;
        Bitmap cropped = Bitmap.createBitmap(src, x, y, size, size);
        return Bitmap.createScaledBitmap(cropped, targetSize, targetSize, true);
    }

    private Bitmap getCircularAvatarBitmap() {
        File file = new File(getFilesDir(), AVATAR_FILE);
        Bitmap source = null;
        float ratio = 1.0f;

        if (file.exists()) {
            source = BitmapFactory.decodeFile(file.getAbsolutePath());
        }
        if (source == null) {
            int resId = getResources().getIdentifier("logo_orlando", "drawable", getPackageName());
            if (resId != 0) {
                source = BitmapFactory.decodeResource(getResources(), resId);
                ratio = 0.92f;
            }
        }
        if (source == null) {
            source = createDefaultGamerAvatar(300);
            ratio = 1.0f;
        }
        Bitmap cropped = cropCenterSquare(source, 360, ratio);
        return makeCircularWithRoyalBorder(cropped);
    }

    private Bitmap createDefaultGamerAvatar(int size) {
        Bitmap bmp = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bmp);
        canvas.drawColor(PANEL_ALT);

        Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
        p.setColor(GOLD);
        p.setTextAlign(Paint.Align.CENTER);
        p.setTypeface(Typeface.DEFAULT_BOLD);
        p.setTextSize(size * 0.34f);
        canvas.drawText("OH", size / 2f, size * 0.56f, p);

        p.setColor(CYAN);
        p.setTextSize(size * 0.12f);
        canvas.drawText("2.0", size / 2f, size * 0.74f, p);
        return bmp;
    }

    private Bitmap makeCircularWithRoyalBorder(Bitmap bitmap) {
        int size = Math.min(bitmap.getWidth(), bitmap.getHeight());
        Bitmap output = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);

        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        Rect rect = new Rect(0, 0, size, size);
        float radius = size / 2f;

        canvas.drawARGB(0, 0, 0, 0);
        canvas.drawCircle(radius, radius, radius - 10f, paint);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(bitmap, rect, rect, paint);

        Paint goldBorder = new Paint(Paint.ANTI_ALIAS_FLAG);
        goldBorder.setStyle(Paint.Style.STROKE);
        goldBorder.setStrokeWidth(7f);
        goldBorder.setColor(GOLD);
        canvas.drawCircle(radius, radius, radius - 4f, goldBorder);

        Paint cyanBorder = new Paint(Paint.ANTI_ALIAS_FLAG);
        cyanBorder.setStyle(Paint.Style.STROKE);
        cyanBorder.setStrokeWidth(4f);
        cyanBorder.setColor(CYAN);
        canvas.drawCircle(radius, radius, radius - 10f, cyanBorder);

        return output;
    }

    private View buildAvatarView(int dpSize) {
        float density = getResources().getDisplayMetrics().density;
        int px = Math.round(dpSize * density);

        LinearLayout holder = new LinearLayout(this);
        holder.setOrientation(LinearLayout.VERTICAL);
        holder.setGravity(Gravity.CENTER);
        holder.setPadding(0, 6, 0, 6);

        ImageView avatar = new ImageView(this);
        avatar.setImageBitmap(getCircularAvatarBitmap());
        avatar.setLayoutParams(new LinearLayout.LayoutParams(px, px));
        avatar.setOnClickListener(v -> pickProfilePhoto());

        TextView hint = text("👑 Toca el logo para cambiar foto de perfil", 11, GOLD, false);
        hint.setOnClickListener(v -> pickProfilePhoto());

        holder.addView(avatar);
        holder.addView(hint);
        return holder;
    }

    private TextView text(String s, int size, int color, boolean bold) {
        TextView t = new TextView(this);
        t.setText(s);
        t.setTextSize(size);
        t.setTextColor(color);
        if (bold) t.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        t.setGravity(Gravity.CENTER);
        t.setPadding(8, 7, 8, 7);
        return t;
    }

    private LinearLayout page() {
        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(true);
        root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(24, 26, 24, 32);
        root.setBackgroundColor(BG);
        scroll.addView(root);
        setContentView(scroll);
        return root;
    }

    private LinearLayout card() {
        LinearLayout box = new LinearLayout(this);
        box.setOrientation(LinearLayout.VERTICAL);
        box.setBackgroundColor(PANEL);
        box.setPadding(20, 18, 20, 18);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-1, -2);
        lp.setMargins(0, 10, 0, 10);
        root.addView(box, lp);
        return box;
    }

    private void heading(String subtitle) {
        root.addView(text("👑 ORLANDO HACK 2.0 👑", 25, GOLD, true));
        root.addView(text(subtitle, 13, CYAN, true));
                
