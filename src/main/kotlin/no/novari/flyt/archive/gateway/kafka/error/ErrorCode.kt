package no.novari.flyt.archive.gateway.kafka.error

enum class ErrorCode {
    GENERAL_SYSTEM_ERROR,
    INSTANCE_DISPATCH_DECLINED_ERROR,
    INSTANCE_DISPATCH_FAILED_ERROR,
    ;

    fun getCode(): String = ERROR_PREFIX + name

    companion object {
        private const val ERROR_PREFIX = "FINT_FLYT_DISPATCH_GATEWAY_"
    }
}
