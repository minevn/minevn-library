package net.minevn.libs.bukkit.db

import kotlin.reflect.KClass
import kotlin.reflect.cast

abstract class AbstractDataAccess() {
    companion object {
        private var instanceList = mutableMapOf<KClass<out AbstractDataAccess>, AbstractDataAccess>()

        fun <T : AbstractDataAccess> getInstance(dbType: String, type: KClass<T>): T {
            var instance = instanceList[type]
            if (instance == null) {
                val basePackage = type.java.`package`.name
                val daoClass = Class.forName("$basePackage.$dbType.${type.simpleName}Impl")
                instance = type.cast(daoClass.getDeclaredConstructor().newInstance())
                instanceList[type] = instance
            }
            return type.cast(instance)
        }
    }
}