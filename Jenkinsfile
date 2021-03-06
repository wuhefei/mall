pipeline {
  agent {
    node {
      label 'maven'
    }
  }
  parameters {
    string(name: 'PROJECT_VERSION', defaultValue: 'v0.0Beta', description: '项目版本号')
    string(name: 'PROJECT_NAME', defaultValue: 'gulimall-gateway', description: '项目名')
  }
  environment {
    DOCKER_CREDENTIAL_ID = 'aliyun-image-hub-id'
    GITEE_CREDENTIAL_ID = 'gitee-id'
    KUBECONFIG_CREDENTIAL_ID = 'demo-kubeconfig'
    REGISTRY = 'registry.cn-hangzhou.aliyuncs.com'
    DOCKERHUB_NAMESPACE = 'gulimall-yuhl'
    GITEE_ACCOUNT = 'yuhlgitee'
    SONAR_CREDENTIAL_ID = 'sonar-token'
    BRANCH_NAME = 'master'
   }

  stages {
    stage('拉取代码') {
      steps {
        git(url: 'https://gitee.com/yuhlgitee/gulimall.git', credentialsId: 'gitee-id', branch: 'master', changelog: true, poll: false)
        sh 'echo 正在构建项目：$PROJECT_NAME，版本号为：$PROJECT_VERSION,将要提交给$REGISTRY仓库'
        container ('maven') {
            sh "echo 正在完整编译项目"
            sh 'mvn clean install -Dmaven.test.skip=true -gs `pwd`/mvn-setting.xml'
        }
      }
    }

stage ('build & push 构建镜像 & 推动镜像-直接推送最新镜像') {
    steps {
        container ('maven') {
            sh 'mvn -Dmaven.test.skip=true -gs `pwd`/mvn-setting.xml clean package'
            sh 'cd $PROJECT_NAME && docker build -f Dockerfile -t $REGISTRY/$DOCKERHUB_NAMESPACE/$PROJECT_NAME:SNAPSHOT-$BRANCH_NAME-$BUILD_NUMBER .'
            withCredentials([usernamePassword(passwordVariable : 'DOCKER_PASSWORD' ,usernameVariable : 'DOCKER_USERNAME' ,credentialsId : "$DOCKER_CREDENTIAL_ID" ,)]) {
                sh 'echo "$DOCKER_PASSWORD" | docker login $REGISTRY -u "$DOCKER_USERNAME" --password-stdin'
                sh 'docker tag  $REGISTRY/$DOCKERHUB_NAMESPACE/$PROJECT_NAME:SNAPSHOT-$BRANCH_NAME-$BUILD_NUMBER $REGISTRY/$DOCKERHUB_NAMESPACE/$PROJECT_NAME:latest '
                sh 'docker push  $REGISTRY/$DOCKERHUB_NAMESPACE/$PROJECT_NAME:latest'
            }
        }
    }
}

stage('deploy to dev 部署到k8s中') {
  steps {
    sh 'echo 开始将项目：$PROJECT_NAME，版本号为：$PROJECT_VERSION,部署到k8s中'
    input(id: "deploy-to-dev-$PROJECT_NAME", message: "是否将 $PROJECT_NAME 部署到k8s集群中?")
    kubernetesDeploy(configs: "$PROJECT_NAME/deploy/**", enableConfigSubstitution: true, kubeconfigId: "$KUBECONFIG_CREDENTIAL_ID")
    sh 'echo 项目：$PROJECT_NAME，版本号为：$PROJECT_VERSION,部署到k8s中成功完成！'
  }
}

stage('push with tag 发布版本'){
  when{
    expression{
      return params.PROJECT_VERSION =~ /v.*/
    }
  }
  steps {
      container ('maven') {
        input(id: 'release-image-with-tag', message: '发布当前版本镜像吗?')
         sh 'docker tag  $REGISTRY/$DOCKERHUB_NAMESPACE/$PROJECT_NAME:SNAPSHOT-$BRANCH_NAME-$BUILD_NUMBER $REGISTRY/$DOCKERHUB_NAMESPACE/$PROJECT_NAME:$PROJECT_VERSION '
         sh 'docker push  $REGISTRY/$DOCKERHUB_NAMESPACE/$PROJECT_NAME:$PROJECT_VERSION '

          withCredentials([usernamePassword(credentialsId: "$GITEE_CREDENTIAL_ID", passwordVariable: 'GIT_PASSWORD', usernameVariable: 'GIT_USERNAME')]) {
            sh 'git config --global user.email "fsjwin@163.com" '
            sh 'git config --global user.name "18237964056" '
            sh 'git tag -a $PROJECT_NAME-$PROJECT_VERSION -m "$PROJECT_VERSION" '
            sh 'git push http://$GIT_USERNAME:$GIT_PASSWORD@gitee.com/$GITEE_ACCOUNT/gulimall.git --tags --ipv4'
          }

       }
  }
}

  }

}