package com.example.filemanagement;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.text.InputType;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.example.filemanagement.databinding.ActivityMainBinding;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;


public class MainActivity extends AppCompatActivity implements OnClickListener {
    private ActivityMainBinding binding = null;
    private List<Model> modelList;
    String storageRootPath;
    List<String> pathHistory = new ArrayList<>();
    private File file;
    Adapter adapter;
    Model actionFile;
    String action;

    public final String[] EXTERNAL_PERMS = {Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE};
    public final int EXTERNAL_REQUEST = 138;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        super.onCreate(savedInstanceState);
        setContentView(binding.getRoot());

        requestForExternalStoragePermission();

        storageRootPath = Environment.getExternalStorageDirectory().getAbsolutePath();
        file = new File(storageRootPath);
        List<File> files = Utils.getListFiles(file);
        if (files != null) {
            modelList = files.stream().map(Utils::fromFileToFileModel).collect(Collectors.toList());
        }

        adapter = new Adapter(modelList, this);
        binding.fileRcv.setAdapter(adapter);

        registerForContextMenu(binding.fileRcv);
    }

    private void log(String message) {
        Log.d("Hanh Ngo", message);
    }

    public boolean requestForExternalStoragePermission() {

        boolean isPermissionOn = true;
        final int version = Build.VERSION.SDK_INT;
        if (version >= 23) {
            if (!canAccessExternalSd()) {
                isPermissionOn = false;
                requestPermissions(EXTERNAL_PERMS, EXTERNAL_REQUEST);
            }
        }

        return isPermissionOn;
    }

    public boolean canAccessExternalSd() {
        return (hasPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE));
    }

    private boolean hasPermission(String permission) {
        return (PackageManager.PERMISSION_GRANTED == ContextCompat.checkSelfPermission(this, permission));

    }

    @Override
    public void onItemClick(Model model) {
        if (model.getType() == Type.DIRECTORY) {
            pathHistory.add(storageRootPath);
            storageRootPath = storageRootPath + "/" + model.getFileName();
            setFileList(storageRootPath);
        } else {
            openFile(model);
        }
    }

    public void setFileList(String path) {
        file = new File(path);
        List<File> files = Utils.getListFiles(file);
        if (files != null) {
            modelList = files.stream().map(Utils::fromFileToFileModel).collect(Collectors.toList());
        }
        adapter.setFileList(modelList);
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onBackPressed() {
        if (pathHistory.size() <= 0) {
            super.onBackPressed();
            return;
        }
        int lastIndex = pathHistory.size() - 1;
        storageRootPath = pathHistory.get(lastIndex);
        pathHistory.remove(lastIndex);
        setFileList(storageRootPath);
    }

    @Override
    public boolean onContextItemSelected(@NonNull MenuItem item) {
        int position = adapter.getPosition();
        Model model = adapter.getFile(position);
        if (item.getTitle() == "Đổi tên") {
            StringBuilder newName = new StringBuilder(model.getFileName());
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Đổi tên");
            final EditText input = new EditText(this);
            input.setInputType(InputType.TYPE_CLASS_TEXT);
            input.setText(model.getFileName());
            builder.setView(input);
            builder.setPositiveButton("OK", (dialog, which) -> {
                newName.append(input.getText().toString());
                renameFile(model, newName.toString());
                setFileList(storageRootPath);
            });
            builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());
            builder.show();
        }

        if (item.getTitle() == "Xoá") {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Xoá");
            builder.setPositiveButton("OK", (dialog, which) -> {
                File toDelete = new File(model.getAbsolutePath());
                toDelete.delete();
                setFileList(storageRootPath);
            });
            builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());
            builder.show();
        }

        if (item.getTitle() == "Sao chép" || item.getTitle() == "Di chuyển") {
            actionFile = model;
            action = item.getTitle().toString();
            return true;
        }

        return super.onContextItemSelected(item);
    }

    public void renameFile(Model model, String newName) {
        File oldFile = new File(model.getDirectoryPath(), model.getFileName());
        File newFile;

        if (model.getType() == Type.DIRECTORY) {
            newFile = new File(model.getDirectoryPath(), newName);
        } else {
            newFile = new File(model.getDirectoryPath(), newName + "." + model.getExtension());
        }

        oldFile.renameTo(newFile);
        setFileList(storageRootPath);
    }

    public void copyFile(Model source, String target) {
        try {
            FileInputStream inStream = new FileInputStream(source.getAbsolutePath());
            FileOutputStream outStream = new FileOutputStream(target);
            FileChannel inChannel = inStream.getChannel();
            FileChannel outChannel = outStream.getChannel();
            inChannel.transferTo(0, inChannel.size(), outChannel);
        } catch (Exception e) {
            e.printStackTrace();
        }
        setFileList(storageRootPath);
    }

    public void moveFile(Model source, String target) {
        File fromFile = new File(source.getAbsolutePath());
        File toFile = new File(target);
        fromFile.renameTo(toFile);
        setFileList(storageRootPath);
    }

    public void makeDirectory(String target) {
        File directory = new File(target);
        directory.mkdirs();
        setFileList(storageRootPath);
    }

    public void createTextFile(String name, String body) {
        try {
            File txtFile = new File(name);
            FileWriter writer = new FileWriter(txtFile);
            writer.append(body);
            writer.flush();
            writer.close();
            setFileList(storageRootPath);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void openFile(Model model) {
        if (model.getType() == null) {
            return;
        }

        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_VIEW);

        if (model.getType() == Type.IMAGE) {
            intent.setDataAndType(
                    FileProvider.getUriForFile(getApplicationContext(),
                            BuildConfig.APPLICATION_ID + ".provider",
                            new File(model.getAbsolutePath())), "image/*"
            );
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        }

        if (model.getType() == Type.TEXT) {
            intent.setDataAndType(
                    FileProvider.getUriForFile(getApplicationContext(),
                            BuildConfig.APPLICATION_ID + ".provider",
                            new File(model.getAbsolutePath())), "text/plain"
            );
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        }

        startActivity(Intent.createChooser(intent, "Select chooser"));

    }

    @SuppressLint("ResourceType")
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.layout.menu_bar, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.create_directory) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Tạo thư mục");
            final EditText input = new EditText(this);
            input.setInputType(InputType.TYPE_CLASS_TEXT);
            builder.setView(input);
            builder.setPositiveButton("OK", (dialog, which) -> {
                makeDirectory(storageRootPath + "/" + input.getText().toString());
            });
            builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());
            builder.show();
            return true;
        }
        if (item.getItemId() == com.example.filemanagement.R.id.paste) {
            if (actionFile != null) {
                if (action.equals("Sao chép")) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setTitle("Bạn có chắc muốn sao chép đến đây?");
                    builder.setPositiveButton("OK", (dialog, which) -> {
                        copyFile(actionFile, storageRootPath + "/" + actionFile.getFileName());
                        actionFile = null;
                        action = null;
                    });
                    builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());
                    builder.show();
                } else if (action.equals("Di chuyển")) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setTitle("Bạn có chắc muốn di chuyển đến đây?");
                    builder.setPositiveButton("OK", (dialog, which) -> {
                        moveFile(actionFile, storageRootPath + "/" + actionFile.getFileName());
                        actionFile = null;
                        action = null;
                    });
                    builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());
                    builder.show();
                }
            }
            return true;
        }
        if (item.getItemId() == R.id.create_file) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Tạo file");
            TextView inputNameLabel = new TextView(this);
            inputNameLabel.setText("Tên file");
            final EditText inputName = new EditText(this);
            TextView inputBodyLabel = new TextView(this);
            inputBodyLabel.setText("Nội dung");
            final EditText inputBody = new EditText(this);
            inputName.setInputType(InputType.TYPE_CLASS_TEXT);
            inputBody.setInputType(InputType.TYPE_CLASS_TEXT);
            final LinearLayout layout = new LinearLayout(this);
            layout.setOrientation(LinearLayout.VERTICAL);
            layout.addView(inputNameLabel);
            layout.addView(inputName);
            layout.addView(inputBodyLabel);
            layout.addView(inputBody);
            builder.setView(layout);
            builder.setPositiveButton("OK", (dialog, which) -> {
                createTextFile(storageRootPath + "/" + inputName.getText().toString() + ".txt", inputBody.getText().toString());
            });
            builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());
            builder.show();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}