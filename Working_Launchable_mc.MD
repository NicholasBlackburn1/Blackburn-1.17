## HOW TO GET LAUNCHER TO LAUNCH UR MC VERSION



## GRADLE
```
jar {
    configurations.shade.each { dep ->
        from(project.zipTree(dep)) {
          
        manifest {
        if (!configurations.compile.isEmpty()) {
            attributes(
                    'Class-Path': configurations.compile.collect{it.toURI().toString()}.join(' '),
                    'Main-Class': 'net.blackburn.Start')
        }
    }
        sourceSets {
        main {
        resources {
            srcDirs "src/main/resources/assets"
            srcDirs "src/main/resources/data"
        }
    }
    }
      exclude 'META-INF', 'META-INF/**'
        }

}
}
```