package dev.redicloud.module;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.File;
import java.io.Serializable;
import java.util.List;

@Getter @NoArgsConstructor @Setter
public class ModuleDescription implements Serializable {

    private String name;
    private String version;
    private String mainClasse;
    private File file;
    private String description;
    private String author;

    private List<String> dependModules;
    private List<String> softDependModules;

    private ModuleDescription(String name){
        this.name = name;
        this.version = null;
        this.mainClasse = null;
        this.file = null;
        this.description = null;
        this.author = null;
        this.dependModules = null;
        this.softDependModules = null;
    }

    public static ModuleDescription ofName(String name){
        return new ModuleDescription(name);
    }

}
