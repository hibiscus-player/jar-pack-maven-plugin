# jar-pack-maven-plugin
A maven plugin to assist in creating jar files which contain other jar files.
For example, you can use this maven plugin if you want to add one of your
dependencies to the final jar in a way that you want to achieve the following file structure:
```
example-result-artifact-1.0.jar
├── com
│   └── example
│       └── ExampleClass.class
└── dependency.jar
```
## Usage
To use this plugin, add the Jitpack repository as a plugin repository:
```xml
<pluginRepositories>
    <pluginRepository>
        <id>jitpack</id>
        <url>https://jitpack.io/</url>
    </pluginRepository>
</pluginRepositories>
```
Then you can reference this in your build, in the following way:
```xml
<build>
    <plugins>
        <plugin>
            <groupId>com.github.hibiscus-player</groupId>
            <artifactId>jar-pack-maven-plugin</artifactId>
            <version>2.0.0</version>
            <executions>
                <execution>
                    <phase>package</phase>
                    <goals>
                        <goal>jar-pack</goal>
                    </goals>
                    <configuration>
                        <entries>
                            <entry>
                                <groupId>com.example</groupId>
                                <artifactId>example-artifact</artifactId>
                                <version>1.0</version>
                            </entry>
                        </entries>
                    </configuration>
                </execution>
            </executions>
        </plugin>
    </plugins>
</build>
```

You need to specify entries, which will be put into the final jar file. Entries are declared in the `<entries>` tag, and each entry is an `<entry>` tag.
You can specify as many entries as you want. Each entry is required to have a `<groupId>`, and an `<artifactId>`, which represent the target artifact's
`groupId` and `artifactId` respectively. You can optionally specify a version filter (with `<version>`), and a classifier filter (with `<classifier>`). If
you don't specify a version/classifier filter, and you have multiple dependencies with the same `groupId` and `artifactId`, then all of them will be packed.
```xml
<entries>
    <entry>
        <groupId>com.example</groupId>
        <artifactId>example-artifact</artifactId>
        <version>1.0</version>
        <classifier>example</classifier>
    </entry>
</entries>
```
You can also specify where to store these files in the final jar, using the tags `<folderName>` and `<fileName>`. With `<folderName>`, you can specify which
folder to put the jar in. With `<fileName>`, you can specify what the file name will be. By default, the file name will be the same as
the artifact's original filename.
```xml
<entries>
    <entry>
        <groupId>com.example</groupId>
        <artifactId>example-artifact</artifactId>
        <folderName>/where/to/put/the/artifact</folderName>
        <fileName>example.jar</fileName>
    </entry>
</entries>
```

Entries are evaluated sequentially, therefore you can have a fallback entry if you put it after any specific entries:
```xml
<entries>
    <entry>
        <groupId>com.example</groupId>
        <artifactId>example-artifact</artifactId>
        <version>1.0</version>
        <folderName>/specific/version</folderName>
    </entry>
    <entry>
        <groupId>com.example</groupId>
        <artifactId>example-artifact</artifactId>
        <classifier>special</classifier>
        <folderName>/specific/classifier</folderName>
    </entry>
    <entry>
        <groupId>com.example</groupId>
        <artifactId>example-artifact</artifactId>
        <folderName>/everything/else</folderName>
    </entry>
</entries>
```

## Configuration
### `<printArtifacts>`
An optional boolean, indicating whether to print every single artifact or not. This is used for debugging purposes. Defaults to `false`.
### `<entries>`*
A list of `<entry>` tags, which represent the target artifacts. This is required.
`<entry>` tags are made up from the following:

- #### `<groupId>`*
    The `groupId` of the target artifact. This is required for all entries.
- #### `<artifactId>`*
    The `artifactId` of the target artifact. This is required for all entries.
- #### `<version>`
    An optional version filter. Can be used to only match a specific version. If not present, all versions will be matched.
- #### `<classifier>`
    An optional classifier filter. Can be used to only match a specific classifier. If not present, all classifiers (including the default) will be matched.
- #### `<folderName>`
    An optional path. This will be the path in the final jar, at which the artifact will be located.
- #### `<fileName>`
    An optional file name. This will be the filename of the artifact in the final jar. (includes the `.jar`)
