package com.nuecho.genesys.cli.models.configuration

import com.nuecho.genesys.cli.models.configuration.reference.ConfigurationObjectReference

class ConfigurationObjectNotFoundException(reference: ConfigurationObjectReference<*>?) :
    Exception("Cannot find configuration object: '$reference'")
