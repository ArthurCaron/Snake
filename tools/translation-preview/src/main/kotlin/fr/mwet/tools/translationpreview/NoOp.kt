package fr.mwet.tools.translationpreview

import java.lang.reflect.Proxy

inline fun <reified T> noOp(): T =
    Proxy.newProxyInstance(T::class.java.classLoader, arrayOf(T::class.java)) { _, _, _ -> null } as T
