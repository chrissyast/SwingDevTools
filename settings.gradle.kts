import java.util.Properties

rootProject.name = "SwingDevTools"

plugins {
    id("com.gradleup.nmcp.settings") version "1.4.4"
}



val props = Properties()

// load project gradle.properties if it exists
val propFile = rootDir.resolve("gradle.properties")
if (propFile.exists()) {
    propFile.inputStream().use { props.load(it) }
}

nmcpSettings {
    centralPortal {
        val user = props.getProperty("mavenCentralUsername")
        val pass = props.getProperty("mavenCentralPassword")

        if (user != null) username = user
        if (pass != null) password = pass

        // Optional: choose publishing mode
        publishingType = "USER_MANAGED" // or "AUTOMATIC"
    }
}