freeStyleJob('periodic-incremental-daily-development-trigger') {
    label('controller')
    properties {
        pipelineTriggers {
            triggers {
                cron {
                    spec('''
                        TZ=America/Los_Angeles 
                        H 22 * * *
                    '''.stripIndent().trim())
                }
            }
        }
    }
    publishers {
        downstream('O3DE_periodic-incremental-daily/development', 'FAILURE')
    }
}
