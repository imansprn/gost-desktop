package xyz.gobliggg.gost.data

object TemplateRuntimeSynchronizer {
    fun synchronize(
        type: String,
        name: String,
        configBuilder: ConfigBuilder = ConfigBuilder.default(),
        serviceRegistry: ServiceRegistry = ServiceRegistry.default(),
        processManager: ProcessManager = ProcessManager.default(),
    ): Result<List<String>> =
        runCatching {
            val dependents = configBuilder.refreshTemplateInDependentServiceConfigs(type, name)
            if (AppState.isEngineRunning.value) {
                dependents.forEach { serviceId ->
                    if (serviceRegistry.getService(serviceId)?.status == ServiceStatus.RUNNING) {
                        processManager.restartService(serviceId)
                    }
                }
            }
            dependents
        }
}
