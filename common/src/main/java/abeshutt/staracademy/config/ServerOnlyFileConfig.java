package abeshutt.staracademy.config;

import abeshutt.staracademy.StarAcademyMod;
import dev.architectury.platform.Platform;
import dev.architectury.utils.Env;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Path;

public abstract class ServerOnlyFileConfig extends Config {

    public abstract String getPath();

    private Path getConfigFile() {
        return Path.of("config", StarAcademyMod.ID, this.getPath()
                .replace(".", File.separator) + ".json");
    }

    @Override
    public void write() throws IOException {
        // Only allow writing on server
        if (Platform.getEnvironment() == Env.SERVER) {
            this.writeFile(this.getConfigFile(), this);
        }
    }

    @Override
    public <T extends Config> T read() {
        // Only allow reading on server - client should never load these configs
        if (Platform.getEnvironment() == Env.SERVER) {
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
        } else {
            // On client, just reset to defaults without reading/writing files
            this.reset();
        }

        return (T)this;
    }

}
