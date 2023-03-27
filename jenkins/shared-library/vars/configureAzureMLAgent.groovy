def call() {
    echo('================ Installing AzureML extension ================')
    sh '''
        set -e
        az version
        az extension add -n ml -y
        az extension list
    '''
}
