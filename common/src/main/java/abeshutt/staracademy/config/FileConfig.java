package abeshutt.staracademy.config;

import abeshutt.staracademy.StarAcademyMod;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Path;

public abstract class FileConfig extends Config {

    public abstract String getPath();

    private Path getConfigFile() {
        return Path.of("config", StarAcademyMod.ID, this.getPath()
                .replace(".", File.separator) + ".json");
    }

    @Override
    public void write() throws IOException {
        this.writeFile(this.getConfigFile(), this);
    }

    @Override
    public <T extends Config> T read() {
        try {
            return this.readFile(this.getConfigFile(), this.getClass());
        } catch(FileNotFoundException ignored) {
            this.reset();

            try {
                this.write();
            } catch(IOException e) {
                e.printStackTrace();
            }
        }

        return (T)this;
    }

}
