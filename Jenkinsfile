pipeline {
    agent any

    parameters {
        string(defaultValue: 'dev', description: 'Target environment', name: 'ENVIRONMENT', trim: true)
        string(defaultValue: '', description: 'The zipcode service URL', name: 'ZIPCODE_URL', trim: true)
        string(defaultValue: '', description: 'The zipcode service environment', name: 'ZIPCODE_ENV', trim: true)
        string(defaultValue: 'JenkinsTest', description: 'Project', name: 'PROJECT', trim: true)
        string(defaultValue: 'AWS', description: 'Cloud', name: 'CLOUD', trim: true)
    }

    stages {
        stage('Init') {
            steps {
                script {
                    def gradle = readFile(file: 'build.gradle')
                    env.version = (gradle =~ /version\s*=\s*["'](.+)["']/)[0][1]
                    echo "Inferred version: ${env.version}"
                    env.ENVIRONMENT = params.ENVIRONMENT
                    env.ZIPCODE_ENV = params.ZIPCODE_ENV ? params.ZIPCODE_ENV : params.ENVIRONMENT
                    env.PROJECT = params.PROJECT ? params.PROJECT : "JenkinsTest" // TODO: Change to Virtual Express
                    env.CLOUD = params.CLOUD ? params.CLOUD : "AWS"
                }
            }
        }

        stage('Build') {
            steps {
                sh './gradlew clean assemble'
            }
        }

        stage('Test') {
            steps {
                sh './gradlew test'
                junit 'build/test-results/test/*.xml'
            }
        }

        stage('Publish') {
            steps {
                archiveArtifacts(artifacts: "build/libs/vexpress-pricing-${env.version}.jar", fingerprint: true, onlyIfSuccessful: true)
            }
        }

        stage("InitDeployment") {
            steps {
                withCredentials([usernamePassword(credentialsId: 'apiToken', passwordVariable: 'PASSWORD', usernameVariable: 'USER')]) {
                    script {
                        env.apiUser = USER
                        env.apiToken = PASSWORD
                    }
                }
            }
        }

        stage('DeployVMs') {
            steps {
                withCredentials([usernamePassword(credentialsId: 'sshCreds', passwordVariable: 'PASSWORD', usernameVariable: 'USER')]) {
                    script {
                        def depId = vraDeployFromCatalog(
                                trustSelfSignedCert: true,
                                configFormat: "yaml",
                                config: readFile('infra/appserver.yaml'))[0].id
                        vraWaitForAddress(
                                trustSelfSignedCert: true,
                                deploymentId: depId,
                                timeout: 1800,
                                resourceName: 'JavaServer')[0]
                        env.appIp = getInternalAddress(depId, "JavaServer")
                        echo "Deployed: ${depId} address: ${env.appIp}"
                        env.depId = depId
                    }
                }
            }
        }

        stage('Configure') {
            steps {
                withCredentials([usernamePassword(credentialsId: 'sshCreds', passwordVariable: 'PASSWORD', usernameVariable: 'USER')]) {
                    script {
                        def zipUrl = params.ZIPCODE_URL ? params.ZIPCODE_URL : getDefaultZipcodeUrl()
                        echo "Zipcode service URL: ${zipUrl}"
                        def txt = readFile(file: 'templates/application-properties.tpl')
                        txt = txt.replace('$ZIPCODE_URL', zipUrl)
                        writeFile(file: "application.properties", text: txt)

                        def remote = [:]
                        remote.name = 'appServer'
                        remote.host = env.appIp
                        remote.user = USER
                        remote.password = PASSWORD
                        remote.allowAnyHosts = true

                        // The first first attempt may fail if cloud-init hasn't created user account yet
                        retry(20) {
                            sleep time: 10, unit: 'SECONDS'
                            sshPut remote: remote, from: 'application.properties', into: '/tmp'
                        }
                        sshPut remote: remote, from: 'scripts/vexpress-pricing.service', into: '/tmp'
                        sshPut remote: remote, from: 'scripts/configureAppserver.sh', into: '/tmp'
                        sshCommand remote: remote, command: 'chmod +x /tmp/configureAppserver.sh'
                        sshCommand remote: remote, sudo: true, command: "/tmp/configureAppserver.sh ${USER} ${env.apiUser} ${env.apiToken} ${env.BUILD_URL} ${env.version}"
                    }
                }
            }
        }
        stage("Finalize") {
            steps {
                // Store build state
                withAWS(credentials: 'jenkins', region: 'us-west-1') {
                    writeJSON(file: 'state.json', json: ['url': "http://${env.appIp}:8080", 'deploymentIds': [env.depId]])
                    s3Upload(file: 'state.json', bucket: 'prydin-build-states', path: "vexpress/pricing/${env.ENVIRONMENT}/state.json")
                }
            }
        }
    }
}

def getInternalAddress(id, resourceName) {
    def dep = vraGetDeployment(
            trustSelfSignedCert: true,
            deploymentId: id,
            expandResources: true
    )
    return dep.resources.find({ it.name == resourceName }).properties.networks[0].address
}

def getDefaultZipcodeUrl() {
    // Store build state
    withAWS(credentials: 'jenkins', region: 'us-west-1') {
        s3Download(file: 'state.json', bucket: 'prydin-build-states', path: "vexpress/zipcode/${env.ZIPCODE_ENV}/state.json", force: true)
        def json = readJSON(file: 'state.json')
        print("Found deployment record: " + json)
        return json.url
    }
}

