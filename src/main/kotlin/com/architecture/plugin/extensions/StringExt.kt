package com.architecture.plugin.extensions

import com.architecture.plugin.Constants

private val camelRegex = "(?<=[a-zA-Z])[A-Z]".toRegex()
private val snakeRegex = "_[a-zA-Z]".toRegex()

fun String.forceLowerCase() = this.replace("_", "").lowercase()

fun String.toSnakeCase(): String {
    return camelRegex.replace(this) {
        "_${it.value}"
    }.lowercase()
}

fun String.toCamelCase(): String {
    return snakeRegex.replace(this) {
        it.value.replace("_", "")
            .uppercase()
    }.replaceFirstChar { it.titlecase() }
}

fun String.replaceTemplateArguments(name: String, directoryPath: String, fileName: String, moduleName: String): String {
    val importPathFirstFormatted =
        "$moduleName/" + directoryPath.substringAfter("/lib/") + "/${fileName.replace("_impl.dart", ".dart")}"

    val importPathSecondFormatted = if (fileName.endsWith("_repository_impl.dart"))
        importPathFirstFormatted.replaceLast("/data/", "/domain/")
    else importPathFirstFormatted

    return this.replace(Constants.FILE_CONTENT_ARG_KEY, name.toCamelCase())
        .replace(Constants.FILE_IMPORT_PATH_ARG_KEY, importPathSecondFormatted)
}

private fun String.replaceLast(old: String, new: String): String {
    return this.reversed().replaceFirst(old.reversed(), new.reversed()).reversed()
}
