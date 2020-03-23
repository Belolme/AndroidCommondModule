package com.billin.www.commondmodual.utils.multilanguage

fun main() {
    config {

        // 在插入前检查其它 module 是否存在相同 key 的 string 资源。
        // 如果找到重复的 key，那么该 key 的字符串资源将会被跳过且输出在控制台中。
        checkModelPath(
                "module_framework",
                "module_common"
        )

        // 这里添加 excel 的路径名
        excelName = "V2.05.xlsx"

        // 这里表示输出 Module 的名字
        outputModuleName = "module_photoedit"
    }
}