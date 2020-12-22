(function iffe() {

    function getDate(app) {
        var date = app.currentDate();
        date.setHours(0)
        date.setMinutes(0)
        date.setSeconds(0)

        return date;
    }

    function getAllDayEventData(app, summary) {
        return {
            summary: summary,
            startDate: getDate(app),
            endDate: getDate(app),
            alldayEvent: true
        };
    }

    let exitCode = 0;
    let summary="<<summary>>";
    let calendarName = "<<calendarName>>";
    let applicationName = "Calendar";

    let app = Application.currentApplication();
    app.includeStandardAdditions = true;

    let Calendar = Application(applicationName);
    try {
        let targetCalendar = Calendar.calendars.whose({name: calendarName})[0];
        if (targetCalendar) {
            let event = Calendar.Event(getAllDayEventData(app, summary));
            targetCalendar.events.push(event);
        } else {
            exitCode = -1
        }
    } catch (error) {
        exitCode = -2
    }

    return exitCode;
})();
