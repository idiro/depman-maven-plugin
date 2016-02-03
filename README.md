# depman-maven-plugin
Maven plugin for linking the jar with the compiled dependencies instead of the default one.

This project have been build as a work-around for a dependency issue related to maven. 
If somebody knows a better method, I would be the first one interested.

Problem
-------

In a nutshell, the dynamic variables you use when you install a maven project are not stored and it can create serious issues. 

Imagine you have a maven project with the following dependencies:
```
<project>
    <properties>
        <hadoop.version>1.0.4</hadoop.version>
        <idiro-hadoop.version>0.3</idiro-hadoop.version>
    </properties>
    .
    .
    .
    <dependencies>
        <!-- Idiro Dep -->
        <dependency>
            <groupId>idiro</groupId>
            <artifactId>idiro-hadoop</artifactId>
            <version>${idiro-hadoop.version}</version>
            <scope>compile</scope>
        </dependency>
        <dependency>
            <groupId>idiro</groupId>
            <artifactId>redsqirl-wf-interface</artifactId>
            <version>${project.version}</version>
            <scope>compile</scope>
        </dependency>
    </dependencies>
</project>
```


When compiling, you will be able to dynamically assign a version for the idiro-hadoop project and the redsqirl-wf-interface project.
However if this project A is a dependency of another project B. When compiling and installing B, the default versions (1.0.4 for hadoop and 0.3 for idiro-hadoop) will be reused, 
not the one you compiled with. 

In which case your project loose in flexibility and you cannot propose several B versions that depends on different A versions.

Solution
--------

The project will simply replace all the dynamic variables you have with their values in a new pom file.

How to Use
----------

The dependency manager will make all this possible, with two changes:
* Make the project version dynamic - which will create bad maven warnings by the way, but as I said it is a work-around
* Add the dependency manager plugin once installed on your environment. See below how the pom should look like.

```    
<project>
    <properties>
        <hadoop.version>1.0.4</hadoop.version>
        <idiro-hadoop.version>${hadoop.version}-0.3</idiro-hadoop.version>
    </properties>
    <groupId>idiro</groupId>
    <artifactId>redsqirl-workflow</artifactId>
    <packaging>jar</packaging>
    <version>${hadoop.version}-0.7</version>

    <plugin>
        <groupId>idiro.maven</groupId>
        <artifactId>depman-maven-plugin</artifactId>
        <version>1.0</version>
        <executions>
            <execution>
              <phase>process-resources</phase>
                <goals>
                  <goal>depman</goal>
                </goals>
            </execution>
        </executions>
    </plugin>
</project>
```

To install the solution on your machine, execute maven install in a terminal.
```
$mvn install
```

Notes
-----

* It has been tested on maven 3.0.4, to my knowledge this should work on any maven version.
