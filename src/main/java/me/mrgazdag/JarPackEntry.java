package me.mrgazdag;

import org.apache.maven.plugins.annotations.Parameter;

public class JarPackEntry {
    @Parameter
    private String artifact;
    @Parameter(defaultValue = "/")
    private String folderName;
    @Parameter
    private String fileName;

    public String getArtifact() {
        return artifact;
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
                "artifact='" + artifact + '\'' +
                ", folderName='" + folderName + '\'' +
                ", fileName='" + fileName + '\'' +
                '}';
    }
}
