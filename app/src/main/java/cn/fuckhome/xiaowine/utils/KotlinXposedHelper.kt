package cn.fuckhome.xiaowine.utils

import cn.fuckhome.xiaowine.hook.MainHook
import java.lang.reflect.Constructor
import java.lang.reflect.Field
import java.lang.reflect.Method
import java.lang.reflect.Modifier

/** 在当前被 Hook 进程中查找类 */
fun findClass(name: String): Class<*> = Class.forName(name, false, MainHook.targetClassLoader)

fun findClassOrNull(name: String): Class<*>? = runCatching { findClass(name) }.getOrNull()

/** 查找类中满足条件的声明方法，不存在时抛出 [NoSuchMethodException] */
fun Class<*>.findMethod(predicate: Method.() -> Boolean): Method =
    declaredMethods.firstOrNull { it.predicate() } ?: throw NoSuchMethodException("$simpleName")

fun Class<*>.findMethodOrNull(predicate: Method.() -> Boolean): Method? =
    declaredMethods.firstOrNull { it.predicate() }

/** 查找类中满足条件的声明构造函数，不存在时抛出 [NoSuchMethodException] */
fun Class<*>.findConstructor(predicate: Constructor<*>.() -> Boolean): Constructor<*> =
    declaredConstructors.firstOrNull { it.predicate() } ?: throw NoSuchMethodException("$simpleName#<init>")

private fun Class<*>.findField(name: String): Field {
    var clazz: Class<*>? = this
    while (clazz != null) {
        declaredFields.firstOrNull { it.name == name }?.let {
            it.isAccessible = true
            return it
        }
        clazz = clazz.superclass
    }
    throw NoSuchFieldException("$simpleName#$name")
}

fun Class<*>.findFieldOrNull(name: String): Field? = runCatching { findField(name) }.getOrNull()

fun Any.getObjectField(name: String): Any? = javaClass.findField(name).get(this)

fun Any.getObjectFieldOrNull(name: String): Any? = runCatching { getObjectField(name) }.getOrNull()

@Suppress("UNCHECKED_CAST")
fun <T> Any.getObjectFieldAs(name: String): T = getObjectField(name) as T

@Suppress("UNCHECKED_CAST")
fun <T> Any.getObjectFieldOrNullAs(name: String): T? = getObjectFieldOrNull(name) as T?

fun Any.setObjectField(name: String, value: Any?) = apply {
    javaClass.findField(name).set(this, value)
}

fun Any?.setObjectFieldIfExist(name: String, value: Any?) {
    if (this == null) return
    runCatching { javaClass.findField(name).set(this, value) }
}

fun Class<*>.getStaticObjectField(name: String): Any? = findField(name).get(null)

@Suppress("UNCHECKED_CAST")
fun <T> Class<*>.getStaticObjectFieldAs(name: String): T = getStaticObjectField(name) as T

fun Class<*>.setStaticObjectField(name: String, value: Any?) = apply {
    findField(name).set(null, value)
}

private val primitiveWrappers = mapOf(
    Integer.TYPE to Integer::class.java,
    java.lang.Long.TYPE to java.lang.Long::class.java,
    java.lang.Boolean.TYPE to java.lang.Boolean::class.java,
    java.lang.Byte.TYPE to java.lang.Byte::class.java,
    Character.TYPE to Character::class.java,
    java.lang.Short.TYPE to java.lang.Short::class.java,
    java.lang.Float.TYPE to java.lang.Float::class.java,
    java.lang.Double.TYPE to java.lang.Double::class.java
)

private fun Class<*>.accepts(arg: Any?): Boolean {
    if (arg == null) return !isPrimitive
    if (isInstance(arg)) return true
    return primitiveWrappers[this]?.isInstance(arg) == true
}

private fun Class<*>.findMethod(name: String, parameterCount: Int, args: Array<out Any?>, static: Boolean): Method {
    var clazz: Class<*>? = this
    while (clazz != null) {
        val candidates = clazz.declaredMethods.filter {
            it.name == name && it.parameterCount == parameterCount && Modifier.isStatic(it.modifiers) == static
        }
        candidates.firstOrNull { method ->
            method.parameterTypes.withIndex().all { (i, type) -> type.accepts(args[i]) }
        }?.let {
            it.isAccessible = true
            return it
        }
        clazz = clazz.superclass
    }
    throw NoSuchMethodException("${if (static) "static " else ""}$name($parameterCount)")
}

fun Any.callMethod(name: String, vararg args: Any?): Any? =
    javaClass.findMethod(name, args.size, args, static = false).invoke(this, *args)

fun Any.callMethodOrNull(name: String, vararg args: Any?): Any? =
    runCatching { callMethod(name, *args) }.getOrNull()

@Suppress("UNCHECKED_CAST")
fun <T> Any.callMethodAs(name: String, vararg args: Any?): T = callMethod(name, *args) as T

@Suppress("UNCHECKED_CAST")
fun <T> Any.callMethodOrNullAs(name: String, vararg args: Any?): T? =
    callMethodOrNull(name, *args) as T?

fun Class<*>.callStaticMethod(name: String, vararg args: Any?): Any? =
    findMethod(name, args.size, args, static = true).invoke(null, *args)

fun Class<*>.callStaticMethodOrNull(name: String, vararg args: Any?): Any? =
    runCatching { callStaticMethod(name, *args) }.getOrNull()

@Suppress("UNCHECKED_CAST")
fun <T> Class<*>.callStaticMethodAs(name: String, vararg args: Any?): T =
    callStaticMethod(name, *args) as T

@Suppress("UNCHECKED_CAST")
fun <T> Class<*>.callStaticMethodOrNullAs(name: String, vararg args: Any?): T? =
    callStaticMethodOrNull(name, *args) as T?

/** 使用匹配参数个数的声明构造函数创建实例 */
fun <T> Class<T>.new(vararg args: Any?): T {
    val constructor = declaredConstructors.firstOrNull { it.parameterCount == args.size }
        ?: throw NoSuchMethodException("$simpleName#<init>(${args.size})")
    constructor.isAccessible = true
    return constructor.newInstance(*args) as T
}
