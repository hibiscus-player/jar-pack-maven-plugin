package me.mrgazdag;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugins.annotations.Parameter;

public class JarPackEntry {
    @Parameter(required = true)
    private String groupId;
    @Parameter(required = true)
    private String artifactId;
    @Parameter
    private String version;
    @Parameter
    private String classifier;
    @Parameter(defaultValue = "/")
    private String folderName;
    @Parameter
    private String fileName;

    public boolean matches(Artifact artifact) {
        if (!groupId.equals(artifact.getGroupId())) return false;
        if (!artifactId.equals(artifact.getArtifactId())) return false;
        if (version != null && !version.equals(artifact.getVersion())) return false;
        if (classifier != null && classifier.equals(artifact.getClassifier())) return false;
        return true;
    }

    public String getFolderName() {
        return folderName;
    }

    public String getFileName() {
        return fileName;
    }

    @Override
    public String toString() {
        return "JarPackEntry{" +
                "groupId='" + groupId + '\'' +
                ", artifactId='" + artifactId + '\'' +
                ", version='" + version + '\'' +
                ", classifier='" + classifier + '\'' +
                ", folderName='" + folderName + '\'' +
                ", fileName='" + fileName + '\'' +
                '}';
    }
}
