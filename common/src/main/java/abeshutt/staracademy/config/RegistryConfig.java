package abeshutt.staracademy.config;

import abeshutt.staracademy.StarAcademyMod;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public abstract class RegistryConfig<T> extends Config {

    private Map<Path, T> pathToValue = new HashMap<>();
    private Map<String, T> idToValue = new HashMap<>();

    public abstract String getFolder();

    public abstract Type getType();

    public abstract void process(String id, T value);

    public Map<String, T> getAll() {
        return this.idToValue;
    }

    public void put(String path, T value) {
        this.pathToValue.put(Paths.get(path.replace("/", File.separator) + ".json"), value);
        this.idToValue.put(path, value);
    }

    public Optional<T> get(String path) {
        return Optional.ofNullable(this.idToValue.get(path));
    }

    private Path getFolderFile() {
        return Path.of("config", StarAcademyMod.ID, this.getFolder());
    }

    @Override
    public void write() throws IOException {
        this.pathToValue.forEach((path, value) -> {
            try {
                Path out = this.getFolderFile().resolve(path);
                Files.createDirectories(out.getParent());
                this.writeFile(out, value);
            } catch(IOException e) {
                e.printStackTrace();
            }
        });
    }

    @Override
    public <C extends Config> C read() {
        Path folder = this.getFolderFile();

        if(Files.isDirectory(folder)) {
            try {
                Files.find(folder, 100, (path, attributes) -> attributes.isRegularFile())
                        .forEach(path -> {
                            try {
                                T value = this.readFile(path, this.getType());

                                path = folder.relativize(path);
                                String id = path.toString().replace(File.separator, "/");
                                id = id.substring(0, id.lastIndexOf("."));
                                this.process(id, value);
                                this.pathToValue.put(path, value);
                                this.idToValue.put(id, value);
                            } catch(FileNotFoundException ignored) { }
                        });
            } catch(IOException e) {
                e.printStackTrace();
            }
        } else {
            this.reset();

            try {
                this.write();
            } catch(IOException e) {
                e.printStackTrace();
            }
        }

        return (C)this;
    }

}
