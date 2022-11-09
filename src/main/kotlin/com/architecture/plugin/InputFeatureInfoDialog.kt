package com.architecture.plugin

import com.architecture.plugin.models.CreateDataSourcesEnum
import com.architecture.plugin.models.FeatureData
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.openapi.ui.ValidationInfo
import java.awt.*
import javax.swing.*
import javax.swing.BoxLayout.Y_AXIS
import javax.swing.event.DocumentEvent
import javax.swing.event.DocumentListener


class InputFeatureInfoDialog : DialogWrapper(true) {

    private var featureName: String = ""
    private var isRemoteChecked: Boolean = true
    private var isLocalChecked: Boolean = false

    private lateinit var featureNameTextField: JTextField

    init {
        title = "Generate Feature Architecture"
        isResizable = false
        init()
    }

    fun getDialogData(): FeatureData = FeatureData(featureName, CreateDataSourcesEnum.from(isRemoteChecked, isLocalChecked))

    override fun createCenterPanel(): JComponent {
        return JPanel(BorderLayout()).apply {
            add(JPanel().apply {
                layout = GridLayout(0, 1)
                preferredSize = Dimension(350, 140)
                add(buildTextFieldPanel())
                add(buildCheckBoxes())
            })
        }
    }

    private fun buildTextFieldPanel(): JPanel {
        return JPanel(FlowLayout()).apply {
            add(JLabel("Feature name: "))
            add(JTextField().apply {
                featureNameTextField = this
                preferredSize = Dimension(200, 30)
                document.addDocumentListener(object : DocumentListener {
                    override fun insertUpdate(e: DocumentEvent?) {
                        onFeatureNameChanged(text)
                    }

                    override fun removeUpdate(e: DocumentEvent?) {
                        onFeatureNameChanged(text)
                    }

                    override fun changedUpdate(e: DocumentEvent?) {
                        onFeatureNameChanged(text)
                    }
                })
            })
        }
    }

    private fun buildCheckBoxes(): JPanel {
        return JPanel().apply {
            layout = BoxLayout(this, Y_AXIS)
            add(JCheckBox("Generate remote data source").apply {
                isSelected = isRemoteChecked
                addItemListener {
                    isRemoteChecked = isSelected
                }
            })
            add(JCheckBox("Generate local data source").apply {
                addItemListener {
                    isLocalChecked = isSelected
                }
            })
        }
    }

    private fun onFeatureNameChanged(name: String) {
        featureName = name
    }

    override fun doOKAction() {
        val validationInfo = doValidate()
        if (validationInfo == null) {
            super.close(OK_EXIT_CODE)
        }
    }

    override fun doValidate(): ValidationInfo? {
        if (featureName.isEmpty()) {
            return ValidationInfo("Feature name cannot be empty")
        }
        return super.doValidate()
    }

    override fun getPreferredFocusedComponent(): JComponent {
        return featureNameTextField
    }
}