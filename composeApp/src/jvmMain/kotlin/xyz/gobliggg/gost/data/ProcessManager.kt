package xyz.gobliggg.gost.data

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch
import java.io.BufferedReader
import java.io.InputStreamReader
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit

data class LogEvent(
    val serviceId: String,
    val text: String,
    val timestamp: Long = System.currentTimeMillis(),
)

class ProcessManager(
    private val serviceRegistry: ServiceRegistry,
) {
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val processes = ConcurrentHashMap<String, Process>()
    private val logJobs = ConcurrentHashMap<String, Job>()
    private val serviceLocks = ConcurrentHashMap<String, Any>()

    private val _logs = MutableSharedFlow<LogEvent>(replay = 1000, extraBufferCapacity = 1000)
    val logs: SharedFlow<LogEvent> = _logs

    companion object {
        private val defaultInstance by lazy { ProcessManager(ServiceRegistry.default()) }

        /** Convenience accessor for non-DI callers. */
        fun default(): ProcessManager = defaultInstance
    }

    fun initialize() {
        // Register shutdown hook to kill orphan processes on JVM exit
        Runtime.getRuntime().addShutdownHook(
            Thread(
                {
                    stopAll(preserveIntent = true)
                },
                "gost-process-shutdown-hook",
            ),
        )
    }

    private fun lockFor(serviceId: String): Any =
        serviceLocks.computeIfAbsent(serviceId) { Any() }

    fun startService(
        serviceId: String,
        recordIntent: Boolean = true,
    ) {
        synchronized(lockFor(serviceId)) {
            startServiceLocked(serviceId, recordIntent)
        }
    }

    private fun startServiceLocked(
        serviceId: String,
        recordIntent: Boolean,
    ) {
        val svc = serviceRegistry.getService(serviceId) ?: return
        if (!AppState.isEngineRunning.value) {
            serviceRegistry.updateServiceStatus(serviceId, ServiceStatus.IDLE, errorMessage = "Engine is stopped")
            return
        }
        if (processes[serviceId]?.isAlive == true) return

        if (recordIntent) {
            serviceRegistry.updateDesiredRunning(serviceId, true)
        }

        val runtime = AppState.settings.value.gostRuntime
        if (runtime.binaryPath.isBlank()) {
            serviceRegistry.updateServiceStatus(serviceId, ServiceStatus.ERROR, errorMessage = "GOST runtime path not set")
            return
        }

        try {
            val pb =
                ProcessBuilder(
                    runtime.binaryPath,
                    "-C",
                    svc.configPath,
                )

            if (runtime.workingDirectory.isNotBlank()) {
                pb.directory(java.io.File(runtime.workingDirectory))
            }

            pb.redirectErrorStream(true)

            val process = pb.start()
            processes[serviceId] = process
            val pid = process.pid()

            serviceRegistry.updateServiceStatus(serviceId, ServiceStatus.RUNNING, pid = pid, errorMessage = null)

            val job =
                scope.launch {
                    try {
                        BufferedReader(InputStreamReader(process.inputStream)).use { reader ->
                            var line: String?
                            while (reader.readLine().also { line = it } != null) {
                                _logs.tryEmit(LogEvent(serviceId, line!!))
                            }
                        }
                        val exitCode = process.waitFor()
                        synchronized(lockFor(serviceId)) {
                            if (processes[serviceId] === process) {
                                processes.remove(serviceId)
                                logJobs.remove(serviceId)
                                if (serviceRegistry.getService(serviceId)?.status == ServiceStatus.RUNNING) {
                                    serviceRegistry.updateServiceStatus(
                                        serviceId,
                                        ServiceStatus.ERROR,
                                        errorMessage = "Process exited with code $exitCode",
                                    )
                                }
                            }
                        }
                    } catch (_: Exception) {
                        // Expected when an intentional stop closes the process stream.
                    }
                }
            logJobs[serviceId] = job
        } catch (e: Exception) {
            serviceRegistry.updateServiceStatus(
                serviceId,
                ServiceStatus.ERROR,
                errorMessage = "Failed to start: ${e.message}",
            )
        }
    }

    fun stopService(
        serviceId: String,
        preserveIntent: Boolean = false,
    ) {
        synchronized(lockFor(serviceId)) {
            stopServiceLocked(serviceId, preserveIntent)
        }
    }

    private fun stopServiceLocked(
        serviceId: String,
        preserveIntent: Boolean,
    ) {
        val process = processes.remove(serviceId)
        if (!preserveIntent) {
            serviceRegistry.updateDesiredRunning(serviceId, false)
        }
        serviceRegistry.updateServiceStatus(serviceId, ServiceStatus.IDLE)

        if (process != null && process.isAlive) {
            process.destroy()
            if (!process.waitFor(1_500, TimeUnit.MILLISECONDS)) {
                process.destroyForcibly()
                process.waitFor(1_500, TimeUnit.MILLISECONDS)
            }
        }
        logJobs.remove(serviceId)?.cancel()
    }

    fun stopAll(preserveIntent: Boolean = false) {
        processes.keys.toList().forEach { id ->
            stopService(id, preserveIntent = preserveIntent)
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    fun clearLogs() {
        _logs.resetReplayCache()
    }

    fun restartService(serviceId: String) {
        synchronized(lockFor(serviceId)) {
            val desired = serviceRegistry.getService(serviceId)?.desiredRunning ?: false
            stopServiceLocked(serviceId, preserveIntent = true)
            startServiceLocked(serviceId, recordIntent = !desired)
        }
    }
}
