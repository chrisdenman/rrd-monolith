(function iffe() {
    let result = "";
    try {
        let applicationName = "Calendar";
        let calendarName = "<<calendarName>>";

        let app = Application.currentApplication();
        app.includeStandardAdditions = true;

        let Calendar = Application(applicationName);
        let newCalendar = Calendar.Calendar({name: calendarName, description: calendarName}).make();

        newCalendar.get();

        result = newCalendar.name();
    } catch (error) {
    }

    return result;
})();
