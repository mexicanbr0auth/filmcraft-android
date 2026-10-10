package ai.storyteller.filmcraft.android;

import android.app.NativeActivity;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

/** Native Rust host with immersive UI and Android's permission-free document picker. */
public class FilmCraftActivity extends NativeActivity {
    private static final int PICK_MEDIA = 1001;
    private void enterImmersiveMode() {
        getWindow().getDecorView().setSystemUiVisibility(
            View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
    }
    @Override protected void onCreate(Bundle state) {
        super.onCreate(state);
        enterImmersiveMode();
        Button importButton = new Button(this);
        importButton.setText("+ Media");
        importButton.setTextSize(11);
        importButton.setAllCaps(false);
        importButton.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.setType("*/*");
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
            intent.putExtra(Intent.EXTRA_MIME_TYPES, new String[]{"video/*", "audio/*", "image/*"});
            startActivityForResult(intent, PICK_MEDIA);
        });
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(110, 48, Gravity.TOP | Gravity.RIGHT);
        params.topMargin = 3;
        params.rightMargin = 8;
        addContentView(importButton, params);
    }
    @Override public void onWindowFocusChanged(boolean focused) {
        super.onWindowFocusChanged(focused);
        if (focused) enterImmersiveMode();
    }
    @Override protected void onActivityResult(int request, int result, Intent data) {
        super.onActivityResult(request, result, data);
        if (request != PICK_MEDIA || result != RESULT_OK || data == null) return;
        if (data.getClipData() != null) {
            for (int i = 0; i < data.getClipData().getItemCount(); i++) {
                importDocument(data.getClipData().getItemAt(i).getUri());
            }
        } else if (data.getData() != null) {
            importDocument(data.getData());
        }
    }
    private void importDocument(Uri uri) {
        String filename = "media";
        try (Cursor cursor = getContentResolver().query(uri, new String[]{OpenableColumns.DISPLAY_NAME}, null, null, null)) {
            if (cursor != null && cursor.moveToFirst()) filename = cursor.getString(0);
        } catch (Exception ignored) {}
        if (filename == null) filename = "media";
        filename = filename.replaceAll("[^a-zA-Z0-9._-]", "_");
        if (filename.equals(".") || filename.equals("..") || filename.isEmpty()) filename = "media";
        File dir = new File(getFilesDir(), "incoming");
        if (!dir.exists() && !dir.mkdirs()) return;
        File target = new File(dir, System.currentTimeMillis() + "_" + filename);
        File partial = new File(target.getAbsolutePath() + ".part");
        try (InputStream in = getContentResolver().openInputStream(uri);
             FileOutputStream out = new FileOutputStream(partial)) {
            if (in == null) return;
            byte[] buffer = new byte[65536];
            int n;
            while ((n = in.read(buffer)) != -1) out.write(buffer, 0, n);
            if (!partial.renameTo(target)) partial.delete();
        } catch (Exception e) { partial.delete(); android.util.Log.e("FilmCraft", "Media import failed", e); }
    }
}
