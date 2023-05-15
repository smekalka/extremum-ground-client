package io.extremum.ground.client.storage

object Paths {
    private const val STORAGE = "/storage"

    const val OBJECTS = "$STORAGE/keys"
    const val OBJECT = "$OBJECTS/%s"
    const val OBJECT_META = "$OBJECT/meta"
    const val OBJECT_UPLOAD_FORM = "$OBJECT/upload-form"
    const val OBJECT_MULTIPART = "$OBJECT/multipart"
    const val OBJECT_MULTIPART_UPLOAD = "$OBJECT/multipart/%s"
    const val OBJECT_PRESIGN_URL = "$OBJECT/presign"

    const val JOBS = "$STORAGE/jobs"
    const val JOB = "$JOBS/%s"
}