package net.suqatri.cloud.runner.dependency;

import lombok.Data;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.aether.graph.Dependency;
import org.eclipse.aether.resolution.ArtifactDescriptorException;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;

@Data
public class AdvancedDependencyDownloader {

    public final List<String> repositories;

    public void downloadFiles(AdvancedDependency dependency){
        if(dependency.getDownloadedFile().exists()) return;
        for (String repository : this.repositories) {
            try{
                downloadAnyways(dependency, repository);
                return;
            }catch (Exception e){}
        }
        throw new IllegalArgumentException("No valid repository was found for " + dependency.getName() + " repos: " + repositories);
    }

    private void downloadAnyways(AdvancedDependency advancedDependency, String repoUrl) throws Exception {
        advancedDependency.download(repoUrl);
        resolveDependenciesAndSaveToInfoFile(advancedDependency, repoUrl);
    }

    private void resolveDependenciesAndSaveToInfoFile(AdvancedDependency advancedDependency, String repoUrl) throws ArtifactDescriptorException {
        DefaultArtifact artifact = new DefaultArtifact(advancedDependency.getGroupId() + ":" + advancedDependency.getArtifactId() + ":" + advancedDependency.getVersion());
        List<Dependency> dependencies = new DependencyResolver(repoUrl, artifact).collectDependencies();
        List<CloudDependency> coreDependencies = new ArrayList<>();
        for (Dependency dependency : dependencies) {
            CloudDependency cloudDependency = new CloudDependency(dependency.getArtifact().getGroupId(), dependency.getArtifact().getArtifactId(), dependency.getArtifact().getVersion());
            coreDependencies.add(cloudDependency);
        }
        File file = advancedDependency.getDownloadedInfoFile();
        try{
            FileOutputStream writeData = new FileOutputStream(file);
            ObjectOutputStream writeStream = new ObjectOutputStream(writeData);

            writeStream.writeObject(coreDependencies);
            writeStream.flush();
            writeStream.close();

        }catch (IOException e) {
            e.printStackTrace();
        }
    }
}
