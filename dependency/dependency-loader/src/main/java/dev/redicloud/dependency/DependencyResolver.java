package dev.redicloud.dependency;

import org.apache.maven.repository.internal.MavenRepositorySystemUtils;
import org.eclipse.aether.DefaultRepositorySystemSession;
import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.RepositorySystemSession;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.aether.connector.basic.BasicRepositoryConnectorFactory;
import org.eclipse.aether.graph.Dependency;
import org.eclipse.aether.impl.DefaultServiceLocator;
import org.eclipse.aether.repository.LocalRepository;
import org.eclipse.aether.repository.RemoteRepository;
import org.eclipse.aether.resolution.ArtifactDescriptorException;
import org.eclipse.aether.resolution.ArtifactDescriptorRequest;
import org.eclipse.aether.resolution.VersionRangeRequest;
import org.eclipse.aether.resolution.VersionRangeResolutionException;
import org.eclipse.aether.spi.connector.RepositoryConnectorFactory;
import org.eclipse.aether.spi.connector.transport.TransporterFactory;
import org.eclipse.aether.transport.file.FileTransporterFactory;
import org.eclipse.aether.transport.http.HttpTransporterFactory;

import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class DependencyResolver {

    private final String repositoryUrl;
    private final DefaultArtifact dependency;
    private final DefaultServiceLocator locator;
    private final RepositorySystem system;
    private final RepositorySystemSession session;
    private final RemoteRepository repository;

    public DependencyResolver(String repositoryUrl, DefaultArtifact defaultArtifact) {
        this.repositoryUrl = repositoryUrl;
        this.dependency = defaultArtifact;
        this.locator = MavenRepositorySystemUtils.newServiceLocator();
        this.system = newRepositorySystem(this.locator);
        this.session = newSession(this.system);
        this.repository = new RemoteRepository.Builder(UUID.randomUUID().toString(), "default", this.repositoryUrl).build();
    }

    public List<Dependency> collectDependencies() throws ArtifactDescriptorException {
        return collectDependencies(dependency)
                .parallelStream()
                .filter(d -> !d.isOptional())
                .filter(d -> d.getScope().equalsIgnoreCase("compile"))
                .collect(Collectors.toList());
    }

    public String determinateLatestVersion() throws VersionRangeResolutionException {
        VersionRangeRequest request = new VersionRangeRequest(dependency, Collections.singletonList(repository), null);
        return this.system.resolveVersionRange(session, request).getHighestVersion().toString();
    }

    private List<Dependency> collectDependencies(DefaultArtifact defaultArtifact) throws ArtifactDescriptorException {
        ArtifactDescriptorRequest request = new ArtifactDescriptorRequest();
        request.setArtifact(defaultArtifact);
        request.setRepositories(Collections.singletonList(this.repository));
        return this.system.readArtifactDescriptor(session, request).getDependencies();
    }

    private RepositorySystem newRepositorySystem(DefaultServiceLocator locator) {
        locator.addService(RepositoryConnectorFactory.class, BasicRepositoryConnectorFactory.class);
        locator.addService(TransporterFactory.class, FileTransporterFactory.class);
        locator.addService(TransporterFactory.class, HttpTransporterFactory.class);
        return locator.getService(RepositorySystem.class);
    }

    private RepositorySystemSession newSession(RepositorySystem system) {
        DefaultRepositorySystemSession session = MavenRepositorySystemUtils.newSession();
        LocalRepository localRepository = new LocalRepository(DependencyLoader.getLoader().getRepositoryFolder());
        session.setLocalRepositoryManager(system.newLocalRepositoryManager(session, localRepository));
        return session;
    }

}
