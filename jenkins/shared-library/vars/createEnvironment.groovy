def call(String environment_name, String environment_location) {
        sh("az ml environment create --name ${environment_name} --file ${environment_location}/environment.yml")
}
