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

object ProcessManager {
    private val scope = CoroutineScope(Dispatchers.IO + Job())
    private val processes = ConcurrentHashMap<String, Process>()
    private val logJobs = ConcurrentHashMap<String, Job>()

    private val _logs = MutableSharedFlow<LogEvent>(replay = 1000, extraBufferCapacity = 1000)
    val logs: SharedFlow<LogEvent> = _logs

    fun initialize() {
        // Register shutdown hook as a safety measure
        Runtime.getRuntime().addShutdownHook(
            Thread {
                stopAll()
            },
        )
    }

    fun startService(serviceId: String) {
        val svc = ServiceRegistry.getService(serviceId) ?: return
        if (processes.containsKey(serviceId)) return // Already running according to us

        val runtime = AppState.settings.value.gostRuntime
        if (runtime.binaryPath.isBlank()) {
            ServiceRegistry.updateServiceStatus(serviceId, ServiceStatus.ERROR, errorMessage = "GOST runtime path not set")
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

            ServiceRegistry.updateServiceStatus(serviceId, ServiceStatus.RUNNING, pid = pid, errorMessage = null)

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
                    if (ServiceRegistry.getService(serviceId)?.status == ServiceStatus.RUNNING) {
                        ServiceRegistry.updateServiceStatus(
                            serviceId,
                            ServiceStatus.ERROR,
                            errorMessage = "Process exited with code $exitCode",
                        )
                    }
                }
            logJobs[serviceId] = job
        } catch (e: Exception) {
            ServiceRegistry.updateServiceStatus(serviceId, ServiceStatus.ERROR, errorMessage = "Failed to start: ${e.message}")
        }
    }

    fun stopService(serviceId: String) {
        val process = processes.remove(serviceId)
        val job = logJobs.remove(serviceId)

        if (process != null) {
            ServiceRegistry.updateServiceStatus(serviceId, ServiceStatus.IDLE)
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
