package com.example.billin.opengl.base

inline fun <T : Program> T.apply(block: T.() -> Unit): T {
    useProgram()
    block()
    return this
}

inline fun <T : Program> T.draw(block: T.() -> Unit) {
    apply(block)
    draw()
}