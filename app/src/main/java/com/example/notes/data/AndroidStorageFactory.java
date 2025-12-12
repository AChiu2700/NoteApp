package com.example.notes.data;

import android.content.Context;
import java.io.File;
import java.nio.file.Path;

public final class AndroidStorageFactory {
    private AndroidStorageFactory() {}

    public static LocalStorage create(Context context) {
        File file = new File(context.getFilesDir(), "notes.dat");
        Path path = file.toPath();
        return new InMemoryLocalStorage(path);
    }
}
