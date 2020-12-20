// https://timingapp.com/help/javascript_calendar
// https://stackoverflow.com/questions/38042236/delete-element-and-or-element-container-relationship-in-jxa-javascript-for-auto
(function iffe() {
    let result = ""
    try {
        let applicationName = "Calendar";
        let calendarName = "<<calendarName>>";
        let summary="<<summary>>";

        let app = Application.currentApplication();
        app.includeStandardAdditions = true;

        let Calendar = Application(applicationName);
        let targetCalendar = Calendar.calendars.whose({name: calendarName})[0];
        if (targetCalendar) {
            let event = targetCalendar.events.whose({summary: summary})[0];
            event.get()
            result = event.summary()
        }
    }
    catch (error) {
    }

    return result;
})();
