package dev.redicloud.module;

import dev.redicloud.api.CloudAPI;
import dev.redicloud.api.utils.ApplicationType;
import lombok.Data;
import lombok.Getter;

import java.io.File;
import java.io.Serializable;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

@Getter
public class ModuleDescription implements Serializable {

    private final String name = null;
    private final String version = null;
    private final HashMap<ApplicationType, String> mainClasses = null;
    private final File file = null;
    private final String description = null;
    private final String author = null;

    private final List<String> dependModules = null;
    private final List<String> softDependModules = null;

    public ModuleDescription(){}
    private ModuleDescription(String name){

    }

    public boolean canLoad(){
        for (ApplicationType applicationType : mainClasses.keySet())
            if(applicationType == CloudAPI.getInstance().getApplicationType()) return true;

        return false;
    }

    public static ModuleDescription ofName(String name){
        return new ModuleDescription(name);
    }

}
