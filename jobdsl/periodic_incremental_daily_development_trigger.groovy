freeStyleJob('periodic-incremental-daily-development-trigger') {
    label('controller')
    publishers {
        downstream('O3DE_periodic-incremental-daily/development', 'FAILURE')
    }
    triggers {
        cron {
            spec('TZ=America/Los_Angeles \nH 22 * * *')
        }
    }
}
