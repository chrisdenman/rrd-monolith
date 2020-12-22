(function iffe() {
    let result = "";
    try {
        let applicationName = "Calendar";
        let calendarName = "<<calendarName>>";

        let app = Application.currentApplication();
        app.includeStandardAdditions = true;

        let Calendar = Application(applicationName);
        Calendar.calendars.whose({name: calendarName})[0].delete();
    } catch (error) {
    }

    return result;
})();
