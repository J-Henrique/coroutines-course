package com.techyourchance.coroutines.exercises.exercise7

import com.techyourchance.coroutines.common.TestUtils
import com.techyourchance.coroutines.common.TestUtils.printCoroutineScopeInfo
import com.techyourchance.coroutines.common.TestUtils.printJobsHierarchy
import kotlinx.coroutines.*
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Assert
import org.junit.Test
import java.lang.Exception
import kotlin.coroutines.EmptyCoroutineContext

class Exercise7Test {

    /*
    Write nested withContext blocks, explore the resulting Job's hierarchy, test cancellation
    of the outer scope
     */
    @Test
    fun nestedWithContext() {
        runBlocking {
            val scopeJob = Job()
            val scope = CoroutineScope(scopeJob + CoroutineName("outer scope") + Dispatchers.IO)
            scope.launch {
                withContext(CoroutineName("level 1") + Dispatchers.Default) {
                    try {
                        delay(100)
                        println("level 1 started")
                    } catch (e: CancellationException) {
                        println("level 1 cancelled")
                    }

                    withContext(CoroutineName("level 2") + Dispatchers.IO) {
                        try {
                            delay(100)
                            println("level 2 started")
                            println("level 2 completed")
                        } catch (e: CancellationException) {
                            println("level 2 cancelled")
                        }
                    }
                    println("level 1 completed")
                }
            }

            launch {
                delay(150)
                scope.cancel()
            }
            scopeJob.join()
            println("test done")
        }
    }

    /*
    Launch new coroutine inside another coroutine, explore the resulting Job's hierarchy, test cancellation
    of the outer scope, explore structured concurrency
     */
    @Test
    fun nestedLaunchBuilders() {
        runBlocking {
            val scopeJob = Job()
            val scope = CoroutineScope(scopeJob + CoroutineName("outer scope") + Dispatchers.IO)
            scope.launch {
                println("main coroutine started")

                withContext(CoroutineName("level 1")) {
                    delay(100)
                    try {
                        println("level 1 started")

                        /*
                        * - Lança nova coroutine porém mantendo uma hierarquia entre as duas (filha)
                        * - Não quebra a structure concurrency
                        * - Coroutine principal vai esperar a conclusão dessa antes de finalizar
                        */
                        launch {
                            delay(100)
                            try {
                                println("inner coroutine started")
                            } catch (e: CancellationException) {
                                println("inner coroutine cancelled")
                            }
                            println("inner coroutine completed")
                        }
                        println("level 1 completed")
                    } catch (e: CancellationException) {
                        println("level 1 cancelled")
                    }
                }
                println("main coroutine completed")
            }

            launch {
                delay(300)
                scope.cancel()
            }
            scopeJob.join()

            println("test done")
        }
    }

    /*
    Launch new coroutine on "outer scope" inside another coroutine, explore the resulting Job's hierarchy,
    test cancellation of the outer scope, explore structured concurrency
     */
    @Test
    fun nestedCoroutineInOuterScope() {
        runBlocking {
            val scopeJob = Job()
            val scope = CoroutineScope(scopeJob + CoroutineName("outer scope") + Dispatchers.IO)
            scope.launch {
                println("main coroutine started")

                withContext(CoroutineName("level 1")) {
                    delay(100)
                    try {
                        println("level 1 started")

                        /*
                        * - Lança nova coroutine não mantendo uma hierarquia entre as duas (irmãs)
                        * - Quebra a structure concurrency
                        * - Coroutine principal não vai esperar a conclusão dessa antes de finalizar
                        */
                        scope.launch {
                            delay(100)
                            try {
                                println("inner coroutine started")
                            } catch (e: CancellationException) {
                                println("inner coroutine cancelled")
                            }
                            println("inner coroutine completed")
                        }
                        println("level 1 completed")
                    } catch (e: CancellationException) {
                        println("level 1 cancelled")
                    }
                }
                println("main coroutine completed")
            }

            launch {
                delay(300)
                scope.cancel()
            }
            scopeJob.join()

            println("test done")
        }
    }
}