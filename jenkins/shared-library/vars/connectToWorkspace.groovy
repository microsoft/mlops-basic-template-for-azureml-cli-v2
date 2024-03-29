def call(String resourceGroup, String workspace) {
    echo ("================ Connect to AzureML Workspace ================")
    sh("az configure --defaults group=${resourceGroup} workspace=${workspace}")
    withCredentials([string(credentialsId: 'sp-app-id', variable: 'app_id'), string(credentialsId: 'sp-tenant-id', variable: 'tenant_id'), string(credentialsId: 'sp-password', variable: 'password')]) {
	    sh('az login --service-principal -u $app_id -p $password --tenant $tenant_id')
    }
}
