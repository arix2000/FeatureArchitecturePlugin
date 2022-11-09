package com.architecture.plugin.models

enum class CreateDataSourcesEnum {
    REMOTE,
    LOCAL,
    BOTH,
    NONE;

    companion object {
        fun from(createRemote: Boolean, createLocal: Boolean): CreateDataSourcesEnum {
            return when {
                createRemote && !createLocal -> REMOTE
                !createRemote && createLocal -> LOCAL
                createRemote && createLocal -> BOTH
                else -> NONE
            }
        }
    }
}