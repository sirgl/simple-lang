package sirgl

import sirgl.ast.AstNode
import kotlin.reflect.KClass
import kotlin.reflect.memberProperties


class AstWalker {
    val listenersMap = mutableMapOf<KClass<*>, MutableList<(AstNode) -> (() -> Unit)?>>()
    val postListenersMap = mutableMapOf<KClass<*>, MutableList<(AstNode) -> (() -> Unit)?>>()

    fun walk(node: AstNode) {
        val exitActions = node.acceptListeners(listenersMap)
        node.javaClass.kotlin.memberProperties
                .filter { it.name != "parent" }
                .map { it.get(node) }
                .forEach { value ->
                    when (value) {
                        is AstNode -> walk(value)
                        is Collection<*> -> value.forEach { if (it is AstNode) walk(it) }
                    }
                }
        exitActions.forEach { it() }
        node.acceptListeners(postListenersMap)
    }

    private fun AstNode.acceptListeners(map: Map<KClass<*>, MutableList<(AstNode) -> (() -> Unit)?>>): MutableList<() -> Unit> {
        val classes = getAllSuperclasses(javaClass).map { it.kotlin }
        val exitActions = mutableListOf<() -> Unit>()
        classes.forEach {
            val listeners: MutableList<(AstNode) -> (() -> Unit)?>? = map[it]
            listeners?.forEach { action ->
                val onExit = action(this)
                if (onExit != null) {
                    exitActions.add(onExit)
                }
            }
        }
        return exitActions
    }

    inline fun <reified T : AstNode> addListenerFor(noinline action: (T) -> (() -> Unit)?) {
        val listeners = listenersMap.getOrPut(T::class, { mutableListOf() })
        @Suppress("UNCHECKED_CAST")
        listeners.add(action as (AstNode) -> (() -> Unit)?)
    }

    inline fun <reified T : AstNode> addSimpleListenerFor(noinline action: (T) -> Unit) {
        val listeners = postListenersMap.getOrPut(T::class, { mutableListOf() })
        @Suppress("UNCHECKED_CAST")
        val function = action as (AstNode) -> Unit
        listeners.add({
            function(it)
            null
        })
    }
}