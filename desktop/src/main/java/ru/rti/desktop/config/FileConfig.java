package ru.rti.desktop.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import dagger.Module;
import dagger.Provides;

import java.nio.file.Paths;
import javax.inject.Singleton;

import ru.rti.desktop.helper.FilesHelper;
import ru.rti.desktop.helper.ReportHelper;

@Module
public class FileConfig {

    @Provides
    @Singleton
    public FilesHelper getFilesHelper() {
        return new FilesHelper(Paths.get(".").toAbsolutePath().normalize().toString());
    }

    @Provides
    @Singleton
    public Gson getGson() {
        return new GsonBuilder()
                .setPrettyPrinting()
                .create();
    }

    @Provides
    @Singleton
    public ReportHelper getReportHelper() {
        return new ReportHelper();
    }
}
