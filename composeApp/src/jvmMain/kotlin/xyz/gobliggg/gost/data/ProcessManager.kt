package xyz.gobliggg.gost.data

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch
import java.io.BufferedReader
import java.io.InputStreamReader
import java.util.concurrent.ConcurrentHashMap

data class LogEvent(
    val serviceId: String,
    val text: String,
    val timestamp: Long = System.currentTimeMillis(),
)

class ProcessManager(
    private val serviceRegistry: ServiceRegistry,
) {
    private val scope = CoroutineScope(Dispatchers.IO + Job())
    private val processes = ConcurrentHashMap<String, Process>()
    private val logJobs = ConcurrentHashMap<String, Job>()

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
                    stopAll()
                },
                "gost-process-shutdown-hook",
            ),
        )
    }

    fun startService(serviceId: String) {
        val svc = serviceRegistry.getService(serviceId) ?: return
        if (processes.containsKey(serviceId)) return // Already running according to us

        // Safety: if there's a stale process entry, destroy it first
        processes[serviceId]?.destroyForcibly()

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

            // start tailing logs
            val job =
                scope.launch {
                    BufferedReader(InputStreamReader(process.inputStream)).use { reader ->
                        var line: String?
                        while (reader.readLine().also { line = it } != null) {
                            _logs.tryEmit(LogEvent(serviceId, line!!))
                        }
                    }

                    // wait for it to actually finish
                    val exitCode = process.waitFor()
                    processes.remove(serviceId)
                    logJobs.remove(serviceId)

                    // If it wasn't intentionally stopped
                    if (serviceRegistry.getService(serviceId)?.status == ServiceStatus.RUNNING) {
                        serviceRegistry.updateServiceStatus(
                            serviceId,
                            ServiceStatus.ERROR,
                            errorMessage = "Process exited with code $exitCode",
                        )
                    }
                }
            logJobs[serviceId] = job
        } catch (e: Exception) {
            serviceRegistry.updateServiceStatus(serviceId, ServiceStatus.ERROR, errorMessage = "Failed to start: ${e.message}")
        }
    }

    fun stopService(serviceId: String) {
        val process = processes.remove(serviceId)
        val job = logJobs.remove(serviceId)

        if (process != null) {
            serviceRegistry.updateServiceStatus(serviceId, ServiceStatus.IDLE)
            process.destroyForcibly()
        }

        job?.cancel()
    }

    fun stopAll() {
        processes.keys.toList().forEach { id ->
            stopService(id)
        }
    }

    fun restartService(serviceId: String) {
        stopService(serviceId)
        startService(serviceId)
    }
}
