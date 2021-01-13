package com.billin.www.library.utils.multilanguage

import java.util.*

class MultiLanguageController(
        private val excelPath: String,
        private val outputModulePath: String,
        private val checkModulePath: List<String> = listOf()
) {

    fun run() {

        // 生成中间文件
        val generator = MultiLanguageGenerator()
        val intermediateFile = generator.run(excelPath) ?: return

        println("确定是否把 $intermediateFile 中的内容插入到资源文件中，确定按 'Y'，取消按 'N'")
        val scanner = Scanner(System.`in`)
        while (true) {
            val firstChar = scanner.next()[0].toUpperCase()
            if (firstChar == 'Y') {

                // 写入资源文件
                val writer = MultiLanguageWriter()
                writer.addExtraCheckRootDir(checkModulePath)
                writer.run(intermediateFile, outputModulePath)
                break

            } else if (firstChar == 'N') {
                break
            }
        }
    }
}

class MultiLanguageControllerConfig {

    var excelName: String = ""

    var outputModuleName = ""

    var checkModuleName: MutableList<String> = mutableListOf()

    fun checkModelPath(vararg path: String) {
        checkModuleName.addAll(path)
    }
}

fun config(config: MultiLanguageControllerConfig.() -> Unit) {
    val builder = MultiLanguageControllerConfig()
    config(builder)

    if (builder.excelName == "") {
        println("请输入 excelName")
        return
    }

    if (builder.outputModuleName == "") {
        println("请输入 outputModuleName")
        return
    }

    val excelPath = builder.excelName
    val outputModulePath = getModulePath(builder.outputModuleName)
    val checkModulePath = builder.checkModuleName.map { getModulePath(it) }

    MultiLanguageController(excelPath, outputModulePath, checkModulePath).run()
}

private fun getModulePath(moduleName: String) = "./$moduleName/src/main/res/"