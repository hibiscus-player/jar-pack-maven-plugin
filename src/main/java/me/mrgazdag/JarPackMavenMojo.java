package me.mrgazdag;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.model.Dependency;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Mojo(name= "jar-pack", defaultPhase = LifecyclePhase.PACKAGE)
public class JarPackMavenMojo extends AbstractMojo {
    @Parameter(defaultValue = "${project}", required = true, readonly = true)
    MavenProject project;
    /**
     * Remote repositories which will be searched for source attachments.
     */
    @Parameter( readonly = true, required = true, defaultValue = "${project.remoteArtifactRepositories}" )
    protected List<ArtifactRepository> remoteArtifactRepositories;

    /**
     * Local maven repository.
     */
    @Parameter( readonly = true, required = true, defaultValue = "${localRepository}" )
    protected ArtifactRepository localRepository;

    @Parameter(required = true)
    List<JarPackEntry> entries;

    @Parameter(defaultValue = "false")
    boolean printArtifacts;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        List<Dependency> deps = project.getDependencies();
        Set<Artifact> set = project.getDependencyArtifacts();
        Map<String, String> env = new HashMap<>();
        env.put("create", "true");
        File file = project.getArtifact().getFile();

        // Create mapped entries
        Map<String,JarPackEntry> map = mapEntries(entries);

        byte[] buffer = new byte[8192];
        int read;

        try (FileSystem fs = FileSystems.newFileSystem(URI.create("jar:" + file.toPath().toUri()), env)) {
            for (Artifact artifact : set) {
                String entryKey = getEntryKey(artifact);
                JarPackEntry entry = map.get(entryKey);
                if (printArtifacts) {
                    getLog().info("");
                    getLog().info(artifact.toString());
                    getLog().info("- also known as " + entryKey);
                    getLog().info("- " + artifact.getFile());
                    if (entry != null) {
                        getLog().info("- will be packed as: " + entry);
                    } else {
                        getLog().info("- will not be packed");
                    }
                }
                if (entry != null) {
                    String folder = entry.getFolderName();
                    String fileName = entry.getFileName();
                    if (fileName == null) fileName = artifact.getFile().getName();

                    String pathString = (folder == null ? "" : folder + "/") + fileName;

                    if (!printArtifacts) getLog().info("Packing '" + entry.getArtifact() + "' to '" + pathString + "'");
                    Path path = fs.getPath(pathString);
                    OutputStream out = Files.newOutputStream(path);
                    FileInputStream fis = new FileInputStream(artifact.getFile());

                    while ((read = fis.read(buffer, 0, buffer.length)) >= 0) {
                        out.write(buffer, 0, read);
                    }

                    fis.close();
                    out.close();
                }
            }
        } catch (IOException e) {
            throw new MojoExecutionException(e);
        }
    }

    private static String getEntryKey(Artifact artifact) {
        return artifact.getGroupId() + ":" + artifact.getArtifactId() + (artifact.hasClassifier() ? ":" + artifact.getClassifier() : "") + ":" + artifact.getVersion();
    }

    private static Map<String, JarPackEntry> mapEntries(List<JarPackEntry> list) {
        Map<String, JarPackEntry> map = new HashMap<>();
        for (JarPackEntry entry : list) {
            map.put(entry.getArtifact(), entry);
        }
        return map;
    }
}
