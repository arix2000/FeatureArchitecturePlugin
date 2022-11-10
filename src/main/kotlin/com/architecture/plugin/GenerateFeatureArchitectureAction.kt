package com.architecture.plugin

import com.architecture.plugin.Constants.FILE_NAME_ARG_KEY
import com.architecture.plugin.Constants.getDataSourceDirectoryPath
import com.architecture.plugin.Constants.getLocalDirectoryPath
import com.architecture.plugin.Constants.getRemoteDirectoryPath
import com.architecture.plugin.Constants.getRepositoryDirectoryPath
import com.architecture.plugin.Constants.getRepositoryImplDirectoryPath
import com.architecture.plugin.extensions.forceLowerCase
import com.architecture.plugin.extensions.replaceTemplateArguments
import com.architecture.plugin.extensions.toSnakeCase
import com.architecture.plugin.models.CreateDataSourcesEnum
import com.architecture.plugin.models.CreateDataSourcesEnum.*
import com.architecture.plugin.models.FeatureData
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.PlatformDataKeys
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.guessProjectDir
import com.intellij.openapi.vfs.VirtualFile
import com.jetbrains.lang.dart.util.PubspecYamlUtil
import org.apache.commons.io.FilenameUtils
import java.io.File
import java.io.FileNotFoundException
import java.net.URL


class GenerateFeatureArchitectureAction : AnAction() {

    private lateinit var moduleName: String

    override fun actionPerformed(e: AnActionEvent) {
        setupModuleName(e.project)

        val dialog = InputFeatureInfoDialog()
        if (!dialog.showAndGet())
            return
        val inputData = dialog.getDialogData()

        val selectedDirectoryPath = getCurrentDirectory(e)
        val path = if (File(selectedDirectoryPath).list().isNullOrEmpty())
            selectedDirectoryPath
        else
            "$selectedDirectoryPath/${inputData.name.forceLowerCase()}"
        File(path).mkdir()
        createDirectoriesArchitecture(path, inputData.createDataSourcesEnum)
        createDartFiles(path, inputData)
    }

    private fun setupModuleName(project: Project?) {
        try {
            project?.let {
                val projectDir = it.guessProjectDir() ?: throw FileNotFoundException("Can't find project directory")
                val pubspecYamlFile = PubspecYamlUtil.findPubspecYamlFile(it, projectDir)
                    ?: throw FileNotFoundException("Can't find pubspec.yaml in project base directory")
                moduleName = PubspecYamlUtil.getDartProjectName(pubspecYamlFile).toString()
            }
        } catch (e: FileNotFoundException) {
          thisLogger().error(e.message)
        }
    }

    private fun getCurrentDirectory(event: AnActionEvent): String {
        val folder: VirtualFile? = PlatformDataKeys.VIRTUAL_FILE.getData(event.dataContext)
        folder?.run {
            val selectedItem = File(path)
            return if (selectedItem.isFile)
                folder.parent.path
            else folder.path
        }
        throw Exception("Generate feature architecture: Current path is empty!")
    }

    private fun createDirectoriesArchitecture(path: String, createDataSourcesEnum: CreateDataSourcesEnum) {
        Constants.getBaseDirectoriesPaths(path).forEach { File(it).mkdirs() }

        if (createDataSourcesEnum == BOTH) {
            File(getRemoteDirectoryPath(path)).mkdirs()
            File(getLocalDirectoryPath(path)).mkdirs()
        }
    }

    private fun createDartFiles(path: String, inputData: FeatureData) {
        val classLoader = this.javaClass.classLoader
        val repositoryTemplate = classLoader.getResource("template_repository.dart")
        val repositoryImplTemplate = classLoader.getResource("template_repository_impl.dart")
        createDartFileFrom(repositoryTemplate, inputData.name, getRepositoryDirectoryPath(path))
        createDartFileFrom(repositoryImplTemplate, inputData.name, getRepositoryImplDirectoryPath(path))

        createDataSourceFiles(path, inputData)
    }

    private fun createDartFileFrom(template: URL?, name: String, directoryPath: String) {
        val fileName = FilenameUtils.getName(template?.path).replaceFirst(FILE_NAME_ARG_KEY, name.toSnakeCase())
        File(
            "$directoryPath/$fileName"
        ).writeText(template?.readText()?.replaceTemplateArguments(name, directoryPath, fileName, moduleName) ?: "")
    }

    private fun createDataSourceFiles(path: String, inputData: FeatureData) {
        val classLoader = this.javaClass.classLoader
        val dataSourceTemplate = classLoader.getResource("template_data_source.dart")
        val dataSourceImplTemplate = classLoader.getResource("template_data_source_impl.dart")
        when (inputData.createDataSourcesEnum) {
            REMOTE -> {
                createDartFileFrom(dataSourceTemplate, inputData.name + "_remote", getDataSourceDirectoryPath(path))
                createDartFileFrom(dataSourceImplTemplate, inputData.name + "_remote", getDataSourceDirectoryPath(path))
            }

            LOCAL -> {
                createDartFileFrom(dataSourceTemplate, inputData.name + "_local", getDataSourceDirectoryPath(path))
                createDartFileFrom(dataSourceImplTemplate, inputData.name + "_local", getDataSourceDirectoryPath(path))
            }

            BOTH -> {
                createDartFileFrom(dataSourceTemplate, inputData.name + "_remote", getRemoteDirectoryPath(path))
                createDartFileFrom(dataSourceImplTemplate, inputData.name + "_remote", getRemoteDirectoryPath(path))

                createDartFileFrom(dataSourceTemplate, inputData.name + "_local", getLocalDirectoryPath(path))
                createDartFileFrom(dataSourceImplTemplate, inputData.name + "_local", getLocalDirectoryPath(path))
            }

            NONE -> { /*Do nothing*/ }
        }
    }
}