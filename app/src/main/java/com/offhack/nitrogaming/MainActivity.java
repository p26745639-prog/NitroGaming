
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
import com.startapp.sdk.adsbase.StartAppSDK;
import com.startapp.sdk.adsbase.StartAppAd;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends Activity {
    private static final String YOUTUBE = "https://youtube.com";
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
        getWindow().setNavigationBarColor(BG);StartAppSDK.init(this, "210323637", true);
StartAppAd.showAd(this);
        
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
    }
private void addButton(LinearLayout container, String label, int bgColor, View.OnClickListener click) {
        Button b = new Button(this);
        b.setText(label);
        b.setAllCaps(false);
        b.setTextSize(14);
        b.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        b.setTextColor(WHITE);
        b.setBackgroundTintList(ColorStateList.valueOf(bgColor));
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-1, -2);
        lp.setMargins(0, 8, 0, 8);
        container.addView(b, lp);
        b.setOnClickListener(click);
    }

    private void addButton(String label, boolean primary, View.OnClickListener click) {
        addButton(root, label, primary ? PURPLE_NEON : PANEL, click);
    }

    private void showWelcome() {
        onHomeScreen = false;
        page();
        heading("EDICIÓN REAL · POTENCIA GAMING");
        root.addView(buildAvatarView(125));
        root.addView(text("¡BIENVENIDO A ORLANDO HACK 2.0!", 21, WHITE, true));
        root.addView(text("Apoya el canal oficial OFFHACK en YouTube y entra al nuevo panel con Perfiles Inteligentes, Lanzador Gaming y Módulo ROOT Seguro.", 14, MUTED, false));
        addButton("▶  SUSCRIBIRME A OFFHACK", true, v -> openUrl(YOUTUBE));
        addButton("⚡ ENTRAR AL PANEL 2.0", false, v -> showHome());
        root.addView(text("Versión 2.0 · Morado Eléctrico & Dorado Corona", 12, GOLD, false));
    }

    private void showHome() {
        onHomeScreen = true;
        page();
        heading("PANEL DE CONTROL GAMING 2.0");

        LinearLayout devCard = card();
        devCard.addView(buildAvatarView(96));
        String device = Build.MANUFACTURER.toUpperCase() + " " + Build.MODEL;
        devCard.addView(text("PERFIL GAMER · DISPOSITIVO DETECTADO", 12, GOLD, true));
        devCard.addView(text(device, 18, WHITE, true));
        devCard.addView(text("Android " + Build.VERSION.RELEASE + " · " + getRamStatus() + " · ROOT: " + (isRootAvailable() ? "DETECTADO" : "NO DETECTADO"), 12, MUTED, false));

        String currentProfile = prefs.getString(PREF_PROFILE, "EQUILIBRADO");
        LinearLayout profCard = card();
        profCard.addView(text("PERFILES DE RENDIMIENTO", 15, GOLD, true));
        profCard.addView(text("Perfil activo: " + currentProfile, 14, GREEN, true));
        profCard.addView(text(getProfileDescription(currentProfile), 13, MUTED, false));

        addButton(profCard,
                (currentProfile.equals("AHORRO") ? "✔ " : "") + "🔋 PERFIL AHORRO (Batería y Temperatura)",
                currentProfile.equals("AHORRO") ? Color.rgb(25, 135, 95) : PANEL_ALT,
                v -> applyProfile("AHORRO"));

        addButton(profCard,
                (currentProfile.equals("EQUILIBRADO") ? "✔ " : "") + "⚖️ PERFIL EQUILIBRADO (Uso Diario / Estable)",
                currentProfile.equals("EQUILIBRADO") ? BLUE_NEON : PANEL_ALT,
                v -> applyProfile("EQUILIBRADO"));

        addButton(profCard,
                (currentProfile.equals("ALTO RENDIMIENTO") ? "✔ " : "") + "🚀 PERFIL ALTO RENDIMIENTO (Máxima Respuesta)",
                currentProfile.equals("ALTO RENDIMIENTO") ? PURPLE_NEON : PANEL_ALT,
                v -> applyProfile("ALTO RENDIMIENTO"));

        boolean gamingOn = prefs.getBoolean(PREF_GAMING_ACTIVE, false);
        addButton("🎮  MODO GAMING Y LANZADOR DE JUEGOS " + (gamingOn ? "[ACTIVO]" : "[OFF]"), true, v -> showGaming());
        addButton("🛡️  OPTIMIZACIONES ROOT AVANZADAS (CON CONFIRMACIÓN)", false, v -> showRootMenu());
        addButton("📘  GUÍA PARA ACTIVAR OPCIONES DE DESARROLLADOR", false, v -> showDeveloperGuide());
        addButton("↩  RESTABLECER PARÁMETROS DE ORLANDO HACK", false, v -> confirmReset());
        addButton("▶  YouTube · OFFHACK", false, v -> openUrl(YOUTUBE));
    }

    private void applyProfile(String profile) {
        prefs.edit().putString(PREF_PROFILE, profile).apply();
        String summary;
        if (profile.equals("AHORRO")) {
            summary = "Perfil AHORRO activado:\n• Animaciones recomendadas: 1x\n• Segundo plano: Conservador\n• Ideal para sesiones largas sin calentar el equipo.";
        } else if (profile.equals("ALTO RENDIMIENTO")) {
            summary = "Perfil ALTO RENDIMIENTO activado:\n• Animaciones recomendadas: 0.5x o Desactivadas\n• Respuesta táctil y puntero al máximo\n• Prioridad de memoria para juegos.";
        } else {
            summary = "Perfil EQUILIBRADO activado:\n• Animaciones recomendadas: 0.5x\n• Balance entre fluidez y autonomía\n• Recomendado para uso general.";
        }
        new AlertDialog.Builder(this)
                .setTitle("Perfil " + profile)
                .setMessage(summary + "\n\n¿Deseas abrir Opciones de desarrollador para aplicar las escalas de animación ahora?")
                .setPositiveButton("Ir a Desarrollador", (d, w) -> openDeveloper("Ajusta las 3 escalas de animación según el perfil " + profile + "."))
                .setNegativeButton("Solo guardar perfil", (d, w) -> showHome())
                .show();
    }

    private String getProfileDescription(String profile) {
        switch (profile) {
            case "AHORRO":
                return "Prioriza menor consumo energético, control térmico y procesos ligeros.";
            case "ALTO RENDIMIENTO":
                return "Prioriza respuesta táctil rápida, transiciones 0.5x y liberación de RAM.";
            default:
                return "Balance óptimo entre fluidez visual, estabilidad de FPS y batería.";
        }
    }

    private void showGaming() {
        onHomeScreen = false;
        page();
        heading("MODO GAMING · CENTRO DE CONTROL");

        boolean gamingOn = prefs.getBoolean(PREF_GAMING_ACTIVE, false);
        boolean dndHint = prefs.getBoolean(PREF_NO_DISTURB, false);
        boolean sensBoost = prefs.getBoolean(PREF_SENS_BOOST, false);
        String favPkg = prefs.getString(PREF_FAV_GAME_PKG, "");
        String favName = prefs.getString(PREF_FAV_GAME_NAME, "Ningún juego seleccionado");

        LinearLayout statusCard = card();
        statusCard.addView(text("ESTADO DEL MODO GAMING", 13, GOLD, true));
        statusCard.addView(text(gamingOn ? "● ACTIVADO Y LISTO PARA JUGAR" : "○ DESACTIVADO", 16, gamingOn ? GREEN : ORANGE, true));
        statusCard.addView(text("RAM Disponible: " + getRamStatus(), 13, MUTED, false));

        addButton(statusCard,
                gamingOn ? "⏹ DESACTIVAR MODO GAMING" : "▶ ACTIVAR MODO GAMING",
                gamingOn ? RED_WARN : Color.rgb(25, 135, 95),
                v -> {
                    boolean next = !gamingOn;
                    prefs.edit().putBoolean(PREF_GAMING_ACTIVE, next).apply();
                    Toast.makeText(this, next ? "MODO GAMING ACTIVADO" : "MODO GAMING DESACTIVADO", Toast.LENGTH_SHORT).show();
                    showGaming();
                });

        LinearLayout launcherCard = card();
        launcherCard.addView(text("🚀 LANZADOR DIRECTO DE JUEGOS", 14, GOLD, true));
        launcherCard.addView(text("Juego seleccionado: " + favName, 13, CYAN, true));

        addButton(launcherCard,
                "🎯 ELEGIR JUEGO INSTALADO",
                PANEL_ALT,
                v -> showGameSelectorDialog());

        addButton(launcherCard,
                favPkg.isEmpty() ? "▶ SELECCIONA UN JUEGO ARRIBA PARA LANZAR" : "🔥 INICIAR " + favName.toUpperCase() + " CON BOOST",
                favPkg.isEmpty() ? PANEL_ALT : PURPLE_NEON,
                v -> launchSelectedGame());

        LinearLayout controlsCard = card();
        controlsCard.addView(text("CONTROLES PROPIOS DE SESIÓN", 14, GOLD, true));

        addButton(controlsCard, "🧹 LIBERAR MEMORIA EN SEGUNDO PLANO", PANEL_ALT, v -> cleanBackgroundMemory());

        addButton(controlsCard,
                "🔕 MODO NO MOLESTAR: " + (dndHint ? "ACTIVADO" : "CONFIGURAR"),
                PANEL_ALT,
                v -> {
                    prefs.edit().putBoolean(PREF_NO_DISTURB, !dndHint).apply();
                    openSettings(Settings.ACTION_SOUND_SETTINGS, "Activa 'No molestar' para evitar notificaciones flotantes mientras juegas.");
                });

        addButton(controlsCard,
                "🎯 ASISTENTE DE SENSIBILIDAD / DPI: " + (sensBoost ? "LISTO" : "AJUSTAR"),
                PANEL_ALT,
                v -> {
                    prefs.edit().putBoolean(PREF_SENS_BOOST, true).apply();
                    openDeveloper("Busca 'Ancho mínimo' (DPI) y 'Escalas de animación'. Anota tu valor original antes de subirlo.");
                });

        addButton(controlsCard,
                "🖱 VELOCIDAD DEL PUNTERO Y CLIC AUTOMÁTICO",
                PANEL_ALT,
                v -> openSettings(Settings.ACTION_ACCESSIBILITY_SETTINGS, "Configura Tiempo de permanencia / Clic automático o busca Velocidad del puntero en Idioma y entrada."));

        addButton(controlsCard,
                "☀️ AJUSTES DE PANTALLA Y BRILLO FIJO",
                PANEL_ALT,
                v -> openSettings(Settings.ACTION_DISPLAY_SETTINGS, "Desactiva el brillo automático para evitar caídas de brillo durante la partida."));

        addButton("← VOLVER AL PANEL PRINCIPAL", false, v -> showHome()); 
    }
        private void showGameSelectorDialog() {
         PackageManager pm = getPackageManager();
        List<ApplicationInfo> apps = pm.getInstalledApplications(PackageManager.GET_META_DATA);
        List<String> labels = new ArrayList<>();
        List<String> packages = new ArrayList<>();

        for (ApplicationInfo app : apps) {
            if (pm.getLaunchIntentForPackage(app.packageName) == null) continue;
            if (app.packageName.equals(getPackageName())) continue;

            boolean isGame = (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && app.category == ApplicationInfo.CATEGORY_GAME)
                    || (app.flags & ApplicationInfo.FLAG_IS_GAME) != 0
                    || app.packageName.toLowerCase().contains("freefire")
                    || app.packageName.toLowerCase().contains("game")
                    || app.packageName.toLowerCase().contains("pubg")
                    || app.packageName.toLowerCase().contains("cod")
                    || app.packageName.toLowerCase().contains("roblox")
                    || app.packageName.toLowerCase().contains("minecraft");

            if (isGame) {
                labels.add("🎮 " + pm.getApplicationLabel(app).toString());
                packages.add(app.packageName);
            }
        }

        if (labels.isEmpty()) {
            for (ApplicationInfo app : apps) {
                if (pm.getLaunchIntentForPackage(app.packageName) == null) continue;
                if (app.packageName.equals(getPackageName())) continue;
                if ((app.flags & ApplicationInfo.FLAG_SYSTEM) == 0) {
                    labels.add(pm.getApplicationLabel(app).toString());
                    packages.add(app.packageName);
                }
            }
        }

        if (labels.isEmpty()) {
            Toast.makeText(this, "No se encontraron juegos instalados", Toast.LENGTH_SHORT).show();
            return;
        }

        String[] items = new String[labels.size()];
        labels.toArray(items);
        new AlertDialog.Builder(this)
                .setTitle("Selecciona tu juego favorito")
                .setItems(items, (dialog, which) -> {
                    String cleanName = labels.get(which).replace("🎮 ", "");
                    prefs.edit()
                            .putString(PREF_FAV_GAME_PKG, packages.get(which))
                            .putString(PREF_FAV_GAME_NAME, cleanName)
                            .apply();
                    Toast.makeText(this, "Juego guardado: " + cleanName, Toast.LENGTH_SHORT).show();
                    showGaming();
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void launchSelectedGame() {
        String pkg = prefs.getString(PREF_FAV_GAME_PKG, "");
        String name = prefs.getString(PREF_FAV_GAME_NAME, "Juego");
        if (pkg.isEmpty()) {
            showGameSelectorDialog();
            return;
        }

        prefs.edit().putBoolean(PREF_GAMING_ACTIVE, true).apply();
        System.gc();
        try {
            ActivityManager am = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
            if (am != null) {
                am.killBackgroundProcesses(getPackageName());
            }
        } catch (Exception ignored) {}

        Intent launchIntent = getPackageManager().getLaunchIntentForPackage(pkg);
        if (launchIntent != null) {
            Toast.makeText(this, "⚡ ORLANDO HACK 2.0: Lanzando " + name + "...", Toast.LENGTH_SHORT).show();
            startActivity(launchIntent);
        } else {
            Toast.makeText(this, "No se pudo abrir " + name + ". Selecciónalo de nuevo.", Toast.LENGTH_SHORT).show();
        }
    }

    private void cleanBackgroundMemory() {
        String before = getRamStatus();
        System.gc();
        try {
            ActivityManager am = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
            if (am != null) {
                am.killBackgroundProcesses(getPackageName());
            }
        } catch (Exception ignored) {}
        String after = getRamStatus();
        new AlertDialog.Builder(this)
                .setTitle("Optimización de Memoria Completada")
                .setMessage("Estado anterior: " + before + "\nEstado actual: " + after + "\n\nConsejo: Cierra manualmente las apps pesadas recientes antes de abrir tu juego.")
                .setPositiveButton("Listo", (d, w) -> showGaming())
                .show();
    }

    private void showRootMenu() {
        onHomeScreen = false;
        page();
        heading("MÓDULO ROOT AVANZADO");

        boolean hasRoot = isRootAvailable();
        LinearLayout infoCard = card();
        infoCard.addView(text("ESTADO DE SUPERUSUARIO (SU)", 13, GOLD, true));
        infoCard.addView(text(hasRoot ? "✔ Binario ROOT detectado en el sistema" : "✖ ROOT no detectado (Requiere Magisk / KernelSU)", 15, hasRoot ? GREEN : ORANGE, true));
        infoCard.addView(text("SEGURIDAD: Ningún comando se ejecutará sin tu confirmación explícita previa.", 12, MUTED, false));

        addButton("1️⃣  CPU GOVERNOR: PERFORMANCE (Máxima frecuencia)", false, v ->
                confirmRootAction(
                        "Activar Gobernador CPU Performance",
                        "Fija el gobernador de los núcleos de CPU en modo 'performance' para evitar caídas de frecuencia.\n\nComando a ejecutar:\nfor cpu in /sys/devices/system/cpu/cpu*/cpufreq/scaling_governor; do echo performance > $cpu; done\n\nAdvertencia: Puede aumentar consumo y temperatura.",
                        "for cpu in /sys/devices/system/cpu/cpu*/cpufreq/scaling_governor; do echo performance > $cpu; done"
                ));

        addButton("2️⃣  LIMPIEZA PROFUNDA DE CACHÉ DE KERNEL (drop_caches)", false, v ->
                confirmRootAction(
                        "Limpiar Caché de Página del Kernel",
                        "Libera memoria caché del sistema Linux subyacente para dejar más RAM disponible.\n\nComando a ejecutar:\nsync && echo 3 > /proc/sys/vm/drop_caches\n\nEs seguro y reversible al reiniciar.",
                        "sync && echo 3 > /proc/sys/vm/drop_caches"
                ));

        addButton("3️⃣  PRIORIDAD DE ENTRADA Y COMPOSITOR (SurfaceFlinger)", false, v ->
                confirmRootAction(
                        "Optimizar Latencia de Renderizado",
                        "Ajusta propiedades temporales de superficie para reducir latencia táctil.\n\nComando a ejecutar:\nsetprop debug.sf.latch_unsignaled 1\n\nSe restablece automáticamente al reiniciar.",
                        "setprop debug.sf.latch_unsignaled 1"
                ));

        addButton("4️⃣  LIMPIAR CACHÉ DE APPS DEL SISTEMA (pm trim-caches)", false, v ->
                confirmRootAction(
                        "Limpieza Global de Caché de Aplicaciones",
                        "Solicita al gestor de paquetes liberar hasta 1 GB de archivos temporales acumulados por aplicaciones.\n\nComando a ejecutar:\npm trim-caches 1024M",
                        "pm trim-caches 1024M"
                ));

        addButton("5️⃣  AJUSTE RÁPIDO DE DPI CON ROOT (wm density)", false, v ->
                promptRootDpiDialog());

        addButton("6️⃣  RESTAURAR CPU Y DPI POR DEFECTO", false, v ->
                confirmRootAction(
                        "Restaurar Gobernador CPU y Densidad Original",
                        "Devuelve los núcleos de CPU a 'schedutil' y restablece el DPI original de pantalla.\n\nComando a ejecutar:\nfor cpu in /sys/devices/system/cpu/cpu*/cpufreq/scaling_governor; do echo schedutil > $cpu; done && wm density reset",
                        "for cpu in /sys/devices/system/cpu/cpu*/cpufreq/scaling_governor; do echo schedutil > $cpu; done && wm density reset"
                ));

        addButton("← VOLVER AL PANEL PRINCIPAL", false, v -> showHome());
                              }
        
    
    private void promptRootDpiDialog() {
        int currentDpi = getResources().getDisplayMetrics().densityDpi;
        EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_NUMBER);
        input.setHint("Ejemplo: 440 (Actual: " + currentDpi + " dpi)");

        new AlertDialog.Builder(this)
                .setTitle("Configurar Densidad DPI (ROOT)")
                .setMessage("DPI actual detectado: " + currentDpi + "\n\nIngresa el nuevo valor de densidad (entre 240 y 640). Luego te pediremos confirmación antes de ejecutar 'wm density'.")
                .setView(input)
                .setPositiveButton("Continuar", (d, w) -> {
                    String val = input.getText().toString().trim();
                    if (val.isEmpty()) return;
                    int dpi = Integer.parseInt(val);
                    if (dpi < 240 || dpi > 640) {
                        Toast.makeText(this, "Por seguridad usa un valor entre 240 y 640", Toast.LENGTH_LONG).show();
                        return;
                    }
                    confirmRootAction(
                            "Cambiar Densidad de Pantalla a " + dpi + " DPI",
                            "Aplica una nueva densidad de pantalla inmediata.\n\nComando a ejecutar:\nwm density " + dpi + "\n\nPuedes revertirlo cuando quieras con la opción 6 ('wm density reset').",
                            "wm density " + dpi
                    );
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void confirmRootAction(String title, String details, String command) {
        new AlertDialog.Builder(this)
                .setTitle("⚠️ CONFIRMACIÓN DE SEGURIDAD ROOT")
                .setMessage(title + "\n\n" + details + "\n\n¿Confirmas que deseas ejecutar este comando ROOT ahora?")
                .setPositiveButton("SÍ, EJECUTAR CON ROOT", (d, w) -> executeRootCommand(title, command))
                .setNegativeButton("CANCELAR", null)
                .show();
    }

    private void executeRootCommand(String title, String command) {
        try {
            Process p = Runtime.getRuntime().exec("su");
            DataOutputStream os = new DataOutputStream(p.getOutputStream());
            os.writeBytes(command + "\n");
            os.writeBytes("exit\n");
            os.flush();
            int exitCode = p.waitFor();
            new AlertDialog.Builder(this)
                    .setTitle(exitCode == 0 ? "✅ Comando ROOT Ejecutado" : "⚠️ Resultado ROOT (Código " + exitCode + ")")
                    .setMessage("Acción: " + title + "\n\n" + (exitCode == 0
                            ? "La modificación se aplicó correctamente."
                            : "El kernel o SELinux bloqueó este parámetro específico en tu modelo, o se denegó el permiso su."))
                    .setPositiveButton("Entendido", null)
                    .show();
        } catch (Exception e) {
            new AlertDialog.Builder(this)
                    .setTitle("❌ No se pudo ejecutar ROOT")
                    .setMessage("Tu dispositivo no tiene acceso superusuario activo o Magisk/KernelSU rechazó la solicitud.\n\nDetalle: " + e.getMessage())
                    .setPositiveButton("Cerrar", null)
                    .show();
        }
    }

    private boolean isRootAvailable() {
        String[] paths = {"/system/bin/su", "/system/xbin/su", "/sbin/su", "/su/bin/su", "/magisk/.core/bin/su"};
        for (String path : paths) {
            if (new File(path).exists()) return true;
        }
        return false;
    }

    private String getRamStatus() {
        try {
            ActivityManager am = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
            ActivityManager.MemoryInfo mi = new ActivityManager.MemoryInfo();
            if (am != null) {
                am.getMemoryInfo(mi);
                long availMb = mi.availMem / (1024L * 1024L);
                long totalMb = mi.totalMem / (1024L * 1024L);
                return availMb + " MB libres / " + totalMb + " MB";
            }
        } catch (Exception ignored) {}
        return "RAM disponible";
    }

    private void showDeveloperGuide() {
        new AlertDialog.Builder(this)
                .setTitle("Activar opciones de desarrollador")
                .setMessage("1. Abre Ajustes del teléfono.\n2. Entra en Acerca del teléfono.\n3. Busca Versión de MIUI / HyperOS / OneUI o Número de compilación.\n4. Toca esa opción 7 veces hasta ver el aviso de desarrollador.\n5. Vuelve a Ajustes > Ajustes adicionales > Opciones de desarrollador.")
                .setPositiveButton("Abrir ajustes", (d, w) -> openSettings(Settings.ACTION_SETTINGS, "Busca Acerca del teléfono para activar las opciones de desarrollador."))
                .setNegativeButton("Cerrar", null)
                .show();
    }

    private void openDeveloper(String help) {
        try {
            startActivity(new Intent(Settings.ACTION_APPLICATION_DEVELOPMENT_SETTINGS));
        } catch (Exception e) {
            try {
                startActivity(new Intent(Settings.ACTION_SETTINGS));
            } catch (Exception ignored) {}
        }
        new AlertDialog.Builder(this).setTitle("Qué debes buscar").setMessage(help).setPositiveButton("Entendido", null).show();
    }

    private void openSettings(String action, String help) {
        try {
            startActivity(new Intent(action));
        } catch (Exception e) {
            startActivity(new Intent(Settings.ACTION_SETTINGS));
        }
        new AlertDialog.Builder(this).setTitle("Siguiente paso").setMessage(help).setPositiveButton("Entendido", null).show();
    }

    private void confirmReset() {
        new AlertDialog.Builder(this)
                .setTitle("Restablecer ORLANDO HACK 2.0")
                .setMessage("Este botón restablecerá el perfil activo, el juego favorito, la foto de perfil y las preferencias guardadas dentro de la app.")
                .setNegativeButton("Cancelar", null)
                .setPositiveButton("Restablecer", (d, w) -> {
                    prefs.edit().clear().apply();
                    File avatar = new File(getFilesDir(), AVATAR_FILE);
                    if (avatar.exists()) avatar.delete();
                    Toast.makeText(this, "Preferencias de ORLANDO HACK 2.0 restablecidas", Toast.LENGTH_LONG).show();
                    showHome();
                })
                .show();
    }

    private void openUrl(String url) {
        try {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
        } catch (Exception e) {
            Toast.makeText(this, "No se pudo abrir YouTube", Toast.LENGTH_SHORT).show();
        }
    }
            }
