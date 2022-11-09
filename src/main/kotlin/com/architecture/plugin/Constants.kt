package com.architecture.plugin

object Constants {

    const val FILE_NAME_ARG_KEY = "template"

    const val FILE_CONTENT_ARG_KEY = "{name}"

    const val FILE_IMPORT_PATH_ARG_KEY = "{importPath}"

    fun getBaseDirectoriesPaths(path: String) = listOf(
        "bloc",
        "data",
        "data/datasources",
        "data/models",
        "data/repositories",
        "domain",
        "domain/repositories",
        "domain/usecases",
        "presentation"
    ).map { "$path/$it" }

    fun getRemoteDirectoryPath(path: String) = "$path/data/datasources/remote"

    fun getLocalDirectoryPath(path: String) = "$path/data/datasources/local"

    fun getRepositoryImplDirectoryPath(path: String) = "$path/data/repositories"

    fun getRepositoryDirectoryPath(path: String) = "$path/domain/repositories"

    fun getDataSourceDirectoryPath(path: String) = "$path/data/datasources"
}