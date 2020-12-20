package uk.co.ceilingcat.rrd.monolith.test

import arrow.core.flatMap
import uk.co.ceilingcat.kalendarapi.EventSummary
import uk.co.ceilingcat.kalendarapi.createDuration
import uk.co.ceilingcat.kalendarapi.createKalendarApi
import uk.co.ceilingcat.rrd.gateways.calendaroutputgateway.CalendarName
import java.util.concurrent.TimeUnit.SECONDS
import kotlin.io.path.ExperimentalPathApi

class CalendarHelper {

    companion object {
        @ExperimentalPathApi
        fun eventFoundWithSummary(
            calendarName: CalendarName,
            summaryText: String
        ) =
            createDuration(TIMEOUT_SECONDS, SECONDS).flatMap { duration ->
                createKalendarApi(duration)
                    .retrieveEventsBySummary(
                        uk.co.ceilingcat.kalendarapi.CalendarName(calendarName.text),
                        EventSummary(summaryText)
                    ).map {
                        (it.list.size == 1)
                    }
            }.fold({ false }) { it }

        @ExperimentalPathApi
        fun eventFoundWithSummaryAndRemoved(
            calendarName: CalendarName,
            summaryText: String
        ) =
            createDuration(TIMEOUT_SECONDS, SECONDS).flatMap { duration ->
                createKalendarApi(duration).run {
                    retrieveEventsBySummary(
                        uk.co.ceilingcat.kalendarapi.CalendarName(calendarName.text),
                        EventSummary(summaryText)
                    ).map {
                        if (it.list.size != 1) {
                            false
                        } else {
                            deleteEvent(
                                uk.co.ceilingcat.kalendarapi.CalendarName(calendarName.text),
                                it.list[0].identifier
                            ).isRight()
                        }
                    }
                }
            }.fold({ false }) { it }

        private const val TIMEOUT_SECONDS = 20L
    }
}
