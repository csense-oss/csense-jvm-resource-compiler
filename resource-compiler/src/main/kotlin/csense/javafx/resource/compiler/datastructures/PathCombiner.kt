package csense.javafx.resource.compiler.datastructures

//TODO better name ?
/**
 * Purpose:
 */
class PathCombiner<T> : Iterable<PathCombinerItem<T>> {

    val root: PathCombinerItem<T> = PathCombinerItem("")

    override fun iterator(): Iterator<PathCombinerItem<T>> = PathCombinerIterator(root)


    fun add(relativePath: List<String>, item: T) {
        computeNodeFor(relativePath).items.add(item)
    }

    fun remove(relativePath: List<String>, item: T) {
        computeNodeFor(relativePath).items.remove(item)
    }

    fun clear() {
        root.subParts.clear()
        root.items.clear()
    }

    private fun computeNodeFor(relativePath: List<String>): PathCombinerItem<T> {
        var parent = root
        relativePath.asSequence().filter { it.isNotBlank() }.forEach {
            parent = parent.subParts.getOrPut(it) { PathCombinerItem(it) }
        }
        return parent
    }

    fun createStrucuturedResult(createNode: Function2<PathCombinerItem<T>, List<T>, T>): List<T> = resolveFromRoot(root, createNode)

    private fun resolveFromRoot(current: PathCombinerItem<T>, createNode: Function2<PathCombinerItem<T>, List<T>, T>): List<T> {

        return current.items + current.subParts.values.map {
            createNode(it, resolveFromRoot(it, createNode))
        }
    }

}

class PathCombinerIterator<T>(root: PathCombinerItem<T>) : Iterator<PathCombinerItem<T>> {

    private var missingToVisit = mutableListOf<PathCombinerItem<T>>()

    init {
        missingToVisit.add(root)
    }

    override fun hasNext(): Boolean = missingToVisit.isNotEmpty()

    override fun next(): PathCombinerItem<T> {
        val item = missingToVisit.removeAt(0)
        missingToVisit.addAll(item.subParts.values)
        return item
    }

}

class PathCombinerItem<T>(
        val pathPart: String,
        val items: MutableList<T> = mutableListOf(),
        internal val subParts: MutableMap<String, PathCombinerItem<T>> = mutableMapOf())