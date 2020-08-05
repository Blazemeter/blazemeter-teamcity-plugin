multibranchPipelineJob('TEAMCITY-PLUGIN-CI'){
    branchSources {
        git {
            id('1') // IMPORTANT: use a constant and unique identifier per branch source
            remote('https://github.com/Blazemeter/blazemeter-teamcity-plugin.git')
            credentialsId('github-token')
            includes('*')
        }
    }
    configure {
        it / factory(class: 'org.jenkinsci.plugins.workflow.multibranch.WorkflowBranchProjectFactory') {
            owner(class: 'org.jenkinsci.plugins.workflow.multibranch.WorkflowMultiBranchProject', reference: '../..')
            scriptPath("Jenkinsfile") //Set a specific scriptPath in MultiBranchPipelineJob DSL 
        }
    }
    orphanedItemStrategy {
        discardOldItems {
            daysToKeep(10)
            numToKeep(100)
        }
    }
    triggers {
        periodic(1)
    }
}
