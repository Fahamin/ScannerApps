package com.scan.imagetotext.Utils;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Environment;
import android.util.Log;

import androidx.appcompat.app.AlertDialog;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;

public class FileDialog {
    private static final String PARENT_DIR = "..";
    /* access modifiers changed from: private */
    public final String TAG = getClass().getName();
    private final Activity activity;
    /* access modifiers changed from: private */
    public File currentPath;
    private ListenerList<DirectorySelectedListener> dirListenerList = new ListenerList<>();
    /* access modifiers changed from: private */
    public String fileEndsWith;
    /* access modifiers changed from: private */
    public String[] fileList;
    private ListenerList<FileSelectedListener> fileListenerList = new ListenerList<>();
    /* access modifiers changed from: private */
    public boolean selectDirectoryOption;

    public interface DirectorySelectedListener {
        void directorySelected(File file);
    }

    public interface FileSelectedListener {
        void fileSelected(File file);
    }

    public FileDialog(Activity activity2, File file) {
        this.activity = activity2;
        loadFileList(!file.exists() ? Environment.getExternalStorageDirectory() : file);
    }

    public Dialog createFileDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this.activity);
        builder.setTitle(this.currentPath.getPath());
        if (this.selectDirectoryOption) {
            builder.setPositiveButton("Select directory", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialogInterface, int i) {
                    Log.d(FileDialog.this.TAG, FileDialog.this.currentPath.getPath());
                    FileDialog.this.fireDirectorySelectedEvent(FileDialog.this.currentPath);
                }
            });
        }
        builder.setItems(this.fileList, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialogInterface, int i) {
                File access$400 = FileDialog.this.getChosenFile(FileDialog.this.fileList[i]);
                if (access$400.isDirectory()) {
                    FileDialog.this.loadFileList(access$400);
                    dialogInterface.cancel();
                    dialogInterface.dismiss();
                    FileDialog.this.showDialog();
                    return;
                }
                FileDialog.this.fireFileSelectedEvent(access$400);
            }
        });
        return builder.show();
    }

    public void addFileListener(FileSelectedListener fileSelectedListener) {
        this.fileListenerList.add(fileSelectedListener);
    }

    public void removeFileListener(FileSelectedListener fileSelectedListener) {
        this.fileListenerList.remove(fileSelectedListener);
    }

    public void setSelectDirectoryOption(boolean z) {
        this.selectDirectoryOption = z;
    }

    public void addDirectoryListener(DirectorySelectedListener directorySelectedListener) {
        this.dirListenerList.add(directorySelectedListener);
    }

    public void removeDirectoryListener(DirectorySelectedListener directorySelectedListener) {
        this.dirListenerList.remove(directorySelectedListener);
    }

    public void showDialog() {
        createFileDialog().show();
    }

    /* access modifiers changed from: private */
    public void fireFileSelectedEvent(final File file) {
        this.fileListenerList.fireEvent(new ListenerList.FireHandler<FileSelectedListener>() {
            public void fireEvent(FileSelectedListener fileSelectedListener) {
                fileSelectedListener.fileSelected(file);
            }
        });
    }

    /* access modifiers changed from: private */
    public void fireDirectorySelectedEvent(final File file) {
        this.dirListenerList.fireEvent(new ListenerList.FireHandler<DirectorySelectedListener>() {
            public void fireEvent(DirectorySelectedListener directorySelectedListener) {
                directorySelectedListener.directorySelected(file);
            }
        });
    }

    /* access modifiers changed from: private */
    public void loadFileList(File file) {
        this.currentPath = file;
        ArrayList arrayList = new ArrayList();
        if (file.exists()) {
            if (file.getParentFile() != null) {
                arrayList.add(PARENT_DIR);
            }
            try {
                for (String add : file.list(new FilenameFilter() {
                    public boolean accept(File file, String str) {
                        File file2 = new File(file, str);
                        if (!file2.canRead()) {
                            return false;
                        }
                        if (FileDialog.this.selectDirectoryOption) {
                            return file2.isDirectory();
                        }
                        if ((FileDialog.this.fileEndsWith != null ? str.toLowerCase().endsWith(FileDialog.this.fileEndsWith) : true) || file2.isDirectory()) {
                            return true;
                        }
                        return false;
                    }
                })) {
                    arrayList.add(add);
                }
            } catch (Exception unused) {
            }
        }
        this.fileList = (String[]) arrayList.toArray(new String[0]);
    }

    /* access modifiers changed from: private */
    public File getChosenFile(String str) {
        if (str.equals(PARENT_DIR)) {
            return this.currentPath.getParentFile();
        }
        return new File(this.currentPath, str);
    }

    public void setFileEndsWith(String str) {
        if (str != null) {
            str = str.toLowerCase();
        }
        this.fileEndsWith = str;
    }
}

