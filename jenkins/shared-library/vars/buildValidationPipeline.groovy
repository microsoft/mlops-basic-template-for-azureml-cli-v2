def call() {
    sh '''
        echo "================ Load Python Dependencies ================"
        python -m pip install --upgrade pip
        pip install -r devops/pipeline/requirements/build_validation_requirements.txt
    '''
    sh '''
        echo "================ Lint with flake8 ================"
        flake8 .
    '''
    sh '''
        echo "================ Run Unit Tests ================"
        pytest --ignore=sandbox/ --junitxml=junit/test-results.xml --cov=. --cov-report=xml
    '''
}
