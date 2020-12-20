package uk.co.ceilingcat.rrd.monolith.test

import java.io.File
import java.io.File.createTempFile
import java.util.UUID

val createTempDirectory: () -> File = { createTempDir().also { it.deleteOnExit() } }

val nonExistentDirectory: File = File("/${UUID.randomUUID()}")

val createTemporaryFile: (String, String, File?) -> File = { prefix, extension, directory ->
    if (directory != null) {
        createTempFile(prefix, ".$extension", directory)
    } else {
        createTempFile(prefix, ".$extension")
    }
}
