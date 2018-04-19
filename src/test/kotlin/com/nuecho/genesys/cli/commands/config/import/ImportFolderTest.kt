package com.nuecho.genesys.cli.commands.config.import

import com.genesyslab.platform.applicationblocks.com.objects.CfgFolder
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjectMocks.DEFAULT_FOLDER_REFERENCE
import com.nuecho.genesys.cli.models.configuration.Folder
import com.nuecho.genesys.cli.services.ServiceMocks.mockConfService

class ImportFolderTest : ImportObjectSpec(
    CfgFolder(mockConfService()),
    Folder(name = "name", folder = DEFAULT_FOLDER_REFERENCE)
)
