freeStyleJob('periodic-incremental-daily-stabilization-trigger') {
    label('controller')
    publishers {
        downstream('O3DE_periodic-incremental-daily/stabilization%2F2409', 'FAILURE')
    }
    triggers {
        pollSCM {
            scmpoll_spec('TZ=America/Los_Angeles \nH 22 * * *')
        }
    }
}
