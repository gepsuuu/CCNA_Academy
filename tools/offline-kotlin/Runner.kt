import java.io.File
fun main(args: Array<String>) {
    var pass = 0; var failN = 0
    for (cn in args) {
        val c = Class.forName(cn)
        for (m in c.declaredMethods.filter { it.isAnnotationPresent(org.junit.Test::class.java) }.sortedBy { it.name }) {
            val inst = c.getDeclaredConstructor().newInstance()
            try { m.invoke(inst); pass++; println("PASS ${c.simpleName}.${m.name}") }
            catch (e: java.lang.reflect.InvocationTargetException) { failN++; println("FAIL ${c.simpleName}.${m.name}: ${e.targetException}") ; e.targetException.stackTrace.take(4).forEach { println("    at $it") } }
        }
    }
    println("TOTAL ${pass + failN} PASSED $pass FAILED $failN")
    if (failN > 0) kotlin.system.exitProcess(1)
}
