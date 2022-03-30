package tga.tools.ssm_scaner

import com.amazonaws.services.simplesystemsmanagement.AWSSimpleSystemsManagement
import com.amazonaws.services.simplesystemsmanagement.AWSSimpleSystemsManagementClientBuilder
import com.amazonaws.services.simplesystemsmanagement.model.GetParametersByPathRequest
import com.amazonaws.services.simplesystemsmanagement.model.Parameter
import com.typesafe.config.Config
import com.typesafe.config.ConfigFactory

val config: Config = ConfigFactory.load()

fun main(args: Array<String>) {
    val ssm = AWSSimpleSystemsManagementClientBuilder.defaultClient()

    val path   = config.getString("ssm")
    val filter = config.getString("filter")

    val keyPrintLen = 65

    ssm.readAllProps(path) {
        if (it.value.contains(filter))
            println(it.name.padEnd(keyPrintLen) + " : " + it.value)
    }

}

private fun AWSSimpleSystemsManagement.readAllProps(path: String, consumer: (Parameter) -> Unit) {
    var nextToken: String? = null
    do {
        val result = this.getParametersByPath(
            GetParametersByPathRequest()
                .withPath(path)
                .withWithDecryption(true)
                .withRecursive(true)
                .withNextToken(nextToken)
        )
        nextToken = result.nextToken
        result.parameters?.forEach(consumer)
    } while (nextToken != null)
}
