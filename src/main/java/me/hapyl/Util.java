package me.hapyl;

import com.google.gson.JsonObject;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.File;
import java.util.Scanner;

public final class Util {

    private static final Scanner scanner;

    static {
        scanner = new Scanner(System.in);
    }

    @Nonnull
    public static String input(@Nonnull String prompt) {
        System.out.println(prompt + "\n");

        return scanner.nextLine();
    }

    @Nullable
    public static File findFile(@Nonnull File[] files, @Nonnull String name) {
        for (File file : files) {
            if (file.getName().equals(name)) {
                return file;
            }
        }

        return null;
    }

    public static JsonObject json(@Nonnull String name, @Nonnull String value) {
        final JsonObject object = new JsonObject();
        object.addProperty(name, value);

        return object;
    }

    public static void mkdirs(@Nonnull File file) {
        if (file.exists()) {
            return;
        }

        file.mkdirs();
    }
}
