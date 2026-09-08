import com.vanniktech.maven.publish.JavaLibrary
import com.vanniktech.maven.publish.JavadocJar
import org.gradle.internal.extensions.stdlib.capitalized

plugins {
    alias(libs.plugins.publisher)
    signing
}

dependencies {
    compileOnly(libs.towny) {
        exclude("com.palmergames.adventure")
    }
}

mavenPublishing {
    coordinates(
        groupId = "io.github.alathra",
        artifactId = base.archivesName.get().lowercase(),
        version = version.toString().let { originalVersion ->
            if (!originalVersion.contains("-SNAPSHOT"))
                originalVersion
            else
                originalVersion.substringBeforeLast("-SNAPSHOT") + "-SNAPSHOT" // Force append just -SNAPSHOT if snapshot version
        }
    )

    pom {
        name.set(base.archivesName.get().split("-").map { it.capitalized() }.joinToString("-"))
        description.set(rootProject.description.orEmpty())
        url.set("https://github.com/milkdrinkers/AlathranWars")
        inceptionYear.set("2026")

        licenses {
            license {
                name.set("GNU Affero General Public License v3.0")
                url.set("https://www.gnu.org/licenses/agpl-3.0.en.html#license-text")
                distribution.set("https://www.gnu.org/licenses/agpl-3.0.en.html#license-text")
            }
        }

        developers {
            developer {
                id.set("darksaid98")
                name.set("darksaid98")
                url.set("https://github.com/darksaid98")
                organization.set("milkdrinkers")
            }
        }

        scm {
            url.set("https://github.com/milkdrinkers/AlathranWars")
            connection.set("scm:git:git://github.com/milkdrinkers/AlathranWars.git")
            developerConnection.set("scm:git:ssh://github.com:milkdrinkers/AlathranWars.git")
        }
    }

    configure(JavaLibrary(
        javadocJar = JavadocJar.None(), // We want to use our own javadoc jar
    ))

    // Publish to Maven Central
    publishToMavenCentral(automaticRelease = true)

    // Sign all publications
    signAllPublications()
}

signing {
    isRequired = false // Skip signing if no credentials
}
