def call(String environment_name, String environment_file) {
        sh("az ml environment create --name ${environment_name} --file ${environment_file}")
}
