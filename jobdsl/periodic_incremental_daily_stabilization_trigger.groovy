freeStyleJob('periodic-incremental-daily-stabilization-trigger') {
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
        downstream('O3DE_periodic-incremental-daily/stabilization%2F2305', 'FAILURE')
    }
}
