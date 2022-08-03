package me.mrgazdag;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
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

    @Parameter(required = true)
    List<JarPackEntry> entries;

    @Parameter(defaultValue = "false")
    boolean printArtifacts;

    @SuppressWarnings("unchecked")
    @Override
    public void execute() throws MojoExecutionException {
        Set<Artifact> set = project.getDependencyArtifacts();
        Map<String, String> env = new HashMap<>();
        env.put("create", "true");
        File file = project.getArtifact().getFile();

        byte[] buffer = new byte[8192];
        int read;

        try (FileSystem fs = FileSystems.newFileSystem(URI.create("jar:" + file.toPath().toUri()), env)) {
            getLog().info("Packing artifacts...");
            int counter = 0;
            for (Artifact artifact : set) {
                String entryKey = getEntryKey(artifact);
                JarPackEntry entry = getMatching(entries, artifact);
                if (printArtifacts) {
                    getLog().info("");
                    getLog().info(artifact.toString());
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

                    if (!printArtifacts) getLog().info("Packing '" + entryKey + "' to '" + pathString + "'");
                    counter++;
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
            getLog().info("");
            if (counter == 0) {
                if (entries.size() == 0) {
                    getLog().info("Found no artifact to pack.");
                } else {
                    getLog().warn("Found no matching artifacts to pack. Be sure to check that the entries were specified correctly!");
                }
            } else {
                getLog().info("Successfully packed " + counter + " artifacts!");
            }
        } catch (IOException e) {
            throw new MojoExecutionException(e);
        }
    }

    private static String getEntryKey(Artifact artifact) {
        return artifact.getGroupId() + ":" + artifact.getArtifactId() + (artifact.hasClassifier() ? ":" + artifact.getClassifier() : "") + ":" + artifact.getVersion();
    }

    private static JarPackEntry getMatching(List<JarPackEntry> entries, Artifact artifact) {
        for (JarPackEntry entry : entries) {
            if (entry.matches(artifact)) return entry;
        }
        return null;
    }
}
